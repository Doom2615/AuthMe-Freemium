package fr.xephi.authme.service.bedrock;

/**
 * Bedrock (Floodgate) information about a connected player.
 *
 * @param bedrockUsername the player's Xbox gamertag (without Floodgate prefix)
 * @param xuid            the player's Xbox user id
 * @param linked          whether the Bedrock account is linked to a Java account
 * @param usernamePrefix  Floodgate's configured username prefix (empty if none)
 */
public record BedrockPlayerInfo(String bedrockUsername, String xuid, boolean linked, String usernamePrefix) {
}
