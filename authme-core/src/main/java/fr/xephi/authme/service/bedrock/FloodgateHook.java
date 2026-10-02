package fr.xephi.authme.service.bedrock;

import org.geysermc.floodgate.api.FloodgateApi;
import org.geysermc.floodgate.api.player.FloodgatePlayer;

import java.util.UUID;

/**
 * Isolates all references to the Floodgate API, so that the classes are only loaded
 * when Floodgate is actually installed.
 */
final class FloodgateHook {

    private FloodgateHook() {
    }

    /**
     * Looks up the Bedrock information of the given player.
     *
     * @param uuid the player's UUID
     * @return the Bedrock information, or null if the player isn't a Floodgate player
     */
    static BedrockPlayerInfo lookup(UUID uuid) {
        FloodgateApi api = FloodgateApi.getInstance();
        if (api == null) {
            return null;
        }
        FloodgatePlayer player = api.getPlayer(uuid);
        if (player == null) {
            return null;
        }
        return new BedrockPlayerInfo(player.getUsername(), player.getXuid(), player.isLinked());
    }
}
