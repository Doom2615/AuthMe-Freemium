package fr.xephi.authme.service.bedrock;

import fr.xephi.authme.ConsoleLogger;
import fr.xephi.authme.data.auth.PlayerAuth;
import fr.xephi.authme.datasource.DataSource;
import fr.xephi.authme.events.RegisterEvent;
import fr.xephi.authme.output.ConsoleLoggerFactory;
import fr.xephi.authme.security.PasswordSecurity;
import fr.xephi.authme.security.crypts.HashedPassword;
import fr.xephi.authme.service.BukkitService;
import fr.xephi.authme.service.CommonService;
import fr.xephi.authme.settings.properties.BedrockSettings;
import fr.xephi.authme.settings.properties.RegistrationSettings;
import fr.xephi.authme.util.PlayerUtils;
import fr.xephi.authme.util.RandomStringUtils;
import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginManager;

import javax.inject.Inject;
import java.util.Locale;
import java.util.UUID;

/**
 * Detects Bedrock Edition players (Geyser + Floodgate) and decides whether they may be logged in
 * automatically. Bedrock players are authenticated by Xbox Live through Floodgate.
 */
public class BedrockService {

    private static final String FLOODGATE_PLUGIN = "floodgate";
    private static final int GENERATED_PASSWORD_LENGTH = 32;

    private final ConsoleLogger logger = ConsoleLoggerFactory.get(BedrockService.class);

    @Inject
    private PluginManager pluginManager;

    @Inject
    private CommonService service;

    @Inject
    private DataSource dataSource;

    @Inject
    private PasswordSecurity passwordSecurity;

    @Inject
    private BukkitService bukkitService;

    private volatile boolean emptyPrefixWarningLogged;


    /**
     * @return true if the Floodgate plugin is installed and enabled
     */
    public boolean isFloodgateAvailable() {
        return pluginManager.isPluginEnabled(FLOODGATE_PLUGIN);
    }

    /**
     * Returns the Bedrock information of the given player.
     *
     * @param uuid the player's UUID
     * @return the Bedrock information, or null if the player is not a Bedrock player or Floodgate is missing
     */
    public BedrockPlayerInfo getBedrockInfo(UUID uuid) {
        if (uuid == null || !isFloodgateAvailable()) {
            return null;
        }
        try {
            return FloodgateHook.lookup(uuid);
        } catch (LinkageError | RuntimeException e) {
            logger.logException("Could not query Floodgate for player " + uuid + ":", e);
            return null;
        }
    }

    /**
     * @param uuid the player's UUID
     * @return true if the player is connected through Geyser/Floodgate
     */
    public boolean isBedrockPlayer(UUID uuid) {
        return getBedrockInfo(uuid) != null;
    }

    /**
     * Checks whether the given player may skip password authentication because it is a
     * Bedrock player verified by Floodgate.
     *
     * @param uuid the player's UUID
     * @return true if the player should be logged in automatically
     */
    public boolean canAutoLogin(UUID uuid) {
        if (!service.getProperty(BedrockSettings.ENABLE_AUTO_LOGIN)) {
            return false;
        }
        BedrockPlayerInfo info = getBedrockInfo(uuid);
        if (info == null) {
            return false;
        }
        if (info.linked()) {
            return service.getProperty(BedrockSettings.AUTO_LOGIN_LINKED_ACCOUNTS);
        }
        if (info.usernamePrefix().isEmpty() && !service.getProperty(BedrockSettings.ALLOW_WITHOUT_USERNAME_PREFIX)) {
            if (!emptyPrefixWarningLogged) {
                emptyPrefixWarningLogged = true;
                logger.warning("Floodgate's username-prefix is empty: Bedrock auto-login is disabled to prevent "
                    + "Bedrock players from taking over Java accounts. Set a prefix in Floodgate's config or "
                    + "enable bedrock.allowWithoutUsernamePrefix in AuthMe's config.");
            }
            return false;
        }
        return true;
    }

    /**
     * Checks whether an unregistered Bedrock player should be registered automatically.
     *
     * @return true if auto-registration is enabled
     */
    public boolean isAutoRegisterEnabled() {
        return service.getProperty(BedrockSettings.AUTO_REGISTER)
            && service.getProperty(RegistrationSettings.IS_ENABLED);
    }

    /**
     * Registers the given Bedrock player with a random password. Must be called asynchronously.
     *
     * @param player the player to register
     * @return true if the account was created, false otherwise
     */
    public boolean registerBedrockPlayer(Player player) {
        String name = player.getName().toLowerCase(Locale.ROOT);
        if (dataSource.isAuthAvailable(name)) {
            return true;
        }
        HashedPassword hashedPassword = passwordSecurity.computeHash(
            RandomStringUtils.generate(GENERATED_PASSWORD_LENGTH), name);
        PlayerAuth auth = PlayerAuth.builder()
            .name(name)
            .realName(player.getName())
            .password(hashedPassword)
            .registrationIp(PlayerUtils.getPlayerIp(player))
            .registrationDate(System.currentTimeMillis())
            .uuid(player.getUniqueId())
            .build();
        if (!dataSource.saveAuth(auth)) {
            logger.warning("Could not auto-register Bedrock player " + player.getName());
            return false;
        }
        bukkitService.scheduleSyncTaskFromOptionallyAsyncTask(player,
            () -> bukkitService.callEvent(new RegisterEvent(player)));
        logger.info("Bedrock player " + player.getName() + " has been registered automatically.");
        return true;
    }
}
