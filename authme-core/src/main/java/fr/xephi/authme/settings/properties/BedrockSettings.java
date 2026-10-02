package fr.xephi.authme.settings.properties;

import ch.jalu.configme.Comment;
import ch.jalu.configme.SettingsHolder;
import ch.jalu.configme.properties.Property;

import static ch.jalu.configme.properties.PropertyInitializer.newProperty;

public final class BedrockSettings implements SettingsHolder {

    @Comment({
        "Automatically log in Bedrock Edition players connecting through Geyser + Floodgate.",
        "Bedrock players are already authenticated by Xbox Live, so they don't need a password.",
        "Requires the Floodgate plugin on this server (also when running behind a proxy).",
        "Without Floodgate, this setting has no effect."
    })
    public static final Property<Boolean> ENABLE_AUTO_LOGIN =
        newProperty("bedrock.autoLogin", true);

    @Comment({
        "Automatically register Bedrock players who don't have an account yet.",
        "A random, unknown password is generated, so the account can only be used by this",
        "Bedrock player until an admin sets a password with /authme password."
    })
    public static final Property<Boolean> AUTO_REGISTER =
        newProperty("bedrock.autoRegister", true);

    @Comment({
        "Auto-login Bedrock players linked to a Java account (Floodgate account linking).",
        "Linked players join with the Java name and log into that Java player's account."
    })
    public static final Property<Boolean> AUTO_LOGIN_LINKED_ACCOUNTS =
        newProperty("bedrock.autoLoginLinkedAccounts", true);

    @Comment({
        "Allow auto-login when Floodgate's username-prefix is empty.",
        "Without a prefix a Bedrock player can share the name of a Java player and would be logged",
        "into that Java player's account. Only enable this if you understand the risk."
    })
    public static final Property<Boolean> ALLOW_WITHOUT_USERNAME_PREFIX =
        newProperty("bedrock.allowWithoutUsernamePrefix", false);

    private BedrockSettings() {
    }

}
