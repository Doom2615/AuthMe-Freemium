package fr.xephi.authme.settings.properties;

import ch.jalu.configme.Comment;
import ch.jalu.configme.SettingsHolder;
import ch.jalu.configme.properties.Property;

import static ch.jalu.configme.properties.PropertyInitializer.newProperty;

public final class BedrockSettings implements SettingsHolder {

    @Comment({
        "Automatically log in registered Bedrock Edition players (Geyser + Floodgate).",
        "Only players that Floodgate detects as Bedrock players are logged in automatically;",
        "everyone else is treated as a Java player. New Bedrock players register normally.",
        "Requires the Floodgate plugin on this server (also when running behind a proxy).",
        "Without Floodgate, this setting has no effect."
    })
    public static final Property<Boolean> ENABLE_AUTO_LOGIN =
        newProperty("bedrock.autoLogin", true);

    @Comment({
        "Auto-login Bedrock players linked to a Java account (Floodgate account linking).",
        "Linked players join with the Java name and log into that Java player's account."
    })
    public static final Property<Boolean> AUTO_LOGIN_LINKED_ACCOUNTS =
        newProperty("bedrock.autoLoginLinkedAccounts", true);

    @Comment({
        "Show login, register and 2FA prompts to Bedrock players as native Bedrock forms.",
        "Used whenever a Bedrock player isn't logged in automatically (e.g. not registered yet).",
        "Works on every server version; Java dialog settings don't affect Bedrock players."
    })
    public static final Property<Boolean> USE_FORMS =
        newProperty("bedrock.forms", true);

    private BedrockSettings() {
    }

}
