package fr.xephi.authme.service.bedrock;

import fr.xephi.authme.ConsoleLogger;
import fr.xephi.authme.output.ConsoleLoggerFactory;
import fr.xephi.authme.service.CommonService;
import fr.xephi.authme.settings.properties.BedrockSettings;
import org.bukkit.plugin.PluginManager;

import javax.inject.Inject;
import java.util.UUID;

/**
 * Detects Bedrock Edition players (Geyser + Floodgate) and decides whether they may be logged in
 * automatically. Only players that Floodgate reports as Bedrock players are treated as such;
 * every other player is handled as a Java player.
 */
public class BedrockService {

    private static final String FLOODGATE_PLUGIN = "floodgate";

    private final ConsoleLogger logger = ConsoleLoggerFactory.get(BedrockService.class);

    @Inject
    private PluginManager pluginManager;

    @Inject
    private CommonService service;

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
     * @return true if Floodgate reports the player as a Bedrock player
     */
    public boolean isBedrockPlayer(UUID uuid) {
        return getBedrockInfo(uuid) != null;
    }

    /**
     * Checks whether the given player may skip password authentication because Floodgate
     * reports it as a Bedrock player (authenticated by Xbox Live).
     *
     * @param uuid the player's UUID
     * @return true if the player should be logged in automatically when registered
     */
    public boolean canAutoLogin(UUID uuid) {
        if (!service.getProperty(BedrockSettings.ENABLE_AUTO_LOGIN)) {
            return false;
        }
        BedrockPlayerInfo info = getBedrockInfo(uuid);
        if (info == null) {
            return false;
        }
        return !info.linked() || service.getProperty(BedrockSettings.AUTO_LOGIN_LINKED_ACCOUNTS);
    }
}
