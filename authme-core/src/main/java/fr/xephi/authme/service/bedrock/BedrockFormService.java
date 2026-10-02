package fr.xephi.authme.service.bedrock;

import fr.xephi.authme.ConsoleLogger;
import fr.xephi.authme.data.auth.PlayerCache;
import fr.xephi.authme.datasource.DataSource;
import fr.xephi.authme.output.ConsoleLoggerFactory;
import fr.xephi.authme.platform.DialogWindowSpec;
import fr.xephi.authme.process.register.RegisterSecondaryArgument;
import fr.xephi.authme.process.register.RegistrationType;
import fr.xephi.authme.service.BukkitService;
import fr.xephi.authme.service.CommonService;
import fr.xephi.authme.service.DialogWindowService;
import fr.xephi.authme.settings.properties.BedrockSettings;
import fr.xephi.authme.settings.properties.RegistrationSettings;
import org.bukkit.entity.Player;

import javax.inject.Inject;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Shows the login, register and two-factor prompts to Bedrock players as native Bedrock forms
 * (sent through Floodgate), since Bedrock clients can't display Java dialogs.
 * Submitted forms run the same commands as the Java dialogs, so all validation stays identical.
 */
public class BedrockFormService {

    /** Delay before re-showing a form after it was submitted (e.g. wrong password). */
    private static final long RESHOW_AFTER_SUBMIT_TICKS = 40L;
    /** Delay before re-showing a form that may not be closed. */
    private static final long RESHOW_AFTER_CLOSE_TICKS = 20L;

    private final ConsoleLogger logger = ConsoleLoggerFactory.get(BedrockFormService.class);
    /** Last form shown to each player, so a stale re-show doesn't replace a newer form (e.g. login -> 2FA). */
    private final Map<UUID, FormType> lastShownForm = new ConcurrentHashMap<>();

    @Inject
    private BedrockService bedrockService;

    @Inject
    private CommonService service;

    @Inject
    private DialogWindowService dialogWindowService;

    @Inject
    private BukkitService bukkitService;

    @Inject
    private PlayerCache playerCache;

    @Inject
    private DataSource dataSource;

    /**
     * Kind of authentication form.
     */
    public enum FormType {
        LOGIN, REGISTER, TOTP
    }

    /**
     * Returns whether the given player should get Bedrock forms instead of Java dialogs.
     *
     * @param player the player
     * @return true if Bedrock forms are enabled and Floodgate reports the player as a Bedrock player
     */
    public boolean shouldUseForms(Player player) {
        return service.getProperty(BedrockSettings.USE_FORMS) && bedrockService.isBedrockPlayer(player.getUniqueId());
    }

    /**
     * Shows the login or register form, depending on whether the player is registered.
     *
     * @param player the Bedrock player
     * @param isRegistered whether the player has an account
     */
    public void showAuthForm(Player player, boolean isRegistered) {
        showForm(player, isRegistered ? FormType.LOGIN : FormType.REGISTER);
    }

    /**
     * Shows the given form type to the player.
     *
     * @param player the Bedrock player
     * @param type the form to show
     */
    public void showForm(Player player, FormType type) {
        if (!player.isOnline() || playerCache.isAuthenticated(player.getName())) {
            lastShownForm.remove(player.getUniqueId());
            return;
        }
        lastShownForm.put(player.getUniqueId(), type);
        DialogWindowSpec spec;
        String commandTemplate;
        switch (type) {
            case LOGIN:
                spec = dialogWindowService.createLoginDialog(player);
                commandTemplate = "login $(password)";
                break;
            case TOTP:
                spec = dialogWindowService.createTotpDialog(player);
                commandTemplate = "2fa code $(code)";
                break;
            case REGISTER:
            default:
                RegistrationType registrationType = service.getProperty(RegistrationSettings.REGISTRATION_TYPE);
                RegisterSecondaryArgument secondArg = service.getProperty(RegistrationSettings.REGISTER_SECOND_ARGUMENT);
                spec = dialogWindowService.createRegisterDialog(player, registrationType, secondArg);
                commandTemplate = createRegisterTemplate(registrationType, secondArg);
                break;
        }

        try {
            boolean sent = BedrockFormSender.send(player.getUniqueId(), spec,
                values -> onSubmit(player, type, commandTemplate, values),
                () -> onClose(player, type, spec));
            if (!sent) {
                logger.debug("Could not send Bedrock " + type + " form to " + player.getName());
            }
        } catch (LinkageError | RuntimeException e) {
            logger.logException("Could not send Bedrock form to " + player.getName() + ":", e);
        }
    }

    private void onSubmit(Player player, FormType type, String commandTemplate, Map<String, String> values) {
        String command = fillTemplate(commandTemplate, values).trim();
        bukkitService.scheduleSyncTaskFromOptionallyAsyncTask(player, () -> player.performCommand(command));
        // Re-show the form if the attempt failed (wrong password, invalid email, ...)
        bukkitService.runTaskLater(player, () -> reshow(player, type), RESHOW_AFTER_SUBMIT_TICKS);
    }

    private void onClose(Player player, FormType type, DialogWindowSpec spec) {
        if (!spec.canCloseWithEscape()) {
            bukkitService.runTaskLater(player, () -> reshow(player, type), RESHOW_AFTER_CLOSE_TICKS);
        }
    }

    private void reshow(Player player, FormType previousType) {
        if (!player.isOnline() || playerCache.isAuthenticated(player.getName())) {
            lastShownForm.remove(player.getUniqueId());
            return;
        }
        if (lastShownForm.get(player.getUniqueId()) != previousType) {
            return;
        }
        if (previousType == FormType.TOTP) {
            showForm(player, FormType.TOTP);
            return;
        }
        // After registering, the player may still need to log in (e.g. forceLoginAfterRegister)
        bukkitService.runTaskOptionallyAsync(() -> {
            boolean isRegistered = dataSource.isAuthAvailable(player.getName().toLowerCase(Locale.ROOT));
            bukkitService.scheduleSyncTaskFromOptionallyAsyncTask(player, () -> showAuthForm(player, isRegistered));
        });
    }

    static String fillTemplate(String template, Map<String, String> values) {
        String result = template;
        for (Map.Entry<String, String> entry : values.entrySet()) {
            result = result.replace("$(" + entry.getKey() + ")", entry.getValue());
        }
        return result;
    }

    static String createRegisterTemplate(RegistrationType type, RegisterSecondaryArgument secondArg) {
        if (type == RegistrationType.EMAIL) {
            return secondArg == RegisterSecondaryArgument.CONFIRMATION
                ? "register $(email) $(confirm)" : "register $(email)";
        }
        if (secondArg == RegisterSecondaryArgument.CONFIRMATION) {
            return "register $(password) $(confirm)";
        }
        if (secondArg == RegisterSecondaryArgument.EMAIL_MANDATORY
            || secondArg == RegisterSecondaryArgument.EMAIL_OPTIONAL) {
            return "register $(password) $(email)";
        }
        return "register $(password)";
    }
}
