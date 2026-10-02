# AuthMe-Freemium
**AuthMeReloaded fork with premium/freemium login and automatic Bedrock (Geyser + Floodgate) login.**

[![Build & Release](https://github.com/Doom2615/authme-freemium/actions/workflows/build.yml/badge.svg)](https://github.com/Doom2615/authme-freemium/actions/workflows/build.yml)
[![Latest release](https://img.shields.io/github/v/release/Doom2615/authme-freemium?include_prereleases&label=release)](https://github.com/Doom2615/authme-freemium/releases)
[![License: GPL v3](https://img.shields.io/badge/license-GPLv3-blue.svg)](LICENSE)

<img src="wallpaper.png?raw=true" alt="AuthMe logo"/>

## Description

AuthMe-Freemium is an authentication plugin for offline-mode (cracked) and mixed servers, based on
[AuthMeReloaded](https://github.com/AuthMe/AuthMeReloaded). It prevents username stealing while letting
players who are already verified skip the password prompt:

- **Java premium players** (official Mojang account) log in automatically after `/premium`.
- **Bedrock players** (via Geyser + Floodgate, verified by Xbox Live) log in automatically — no `/register` or `/login`.
- **Everyone else** ("freemium" / cracked players) registers and logs in with a password as usual.

Unauthenticated players can't move, chat, run commands, or use their inventory until they log in.

## What's new in this fork

| Feature | Description |
|---|---|
| **Bedrock auto-login** | Registered players that Floodgate detects as Bedrock players are logged in automatically. Everyone else is treated as a Java player. |
| **Bedrock forms** | Bedrock players get native Bedrock forms for login, register and 2FA instead of Java dialogs or chat commands. |
| **Floodgate linked accounts** | Bedrock players linked to a Java account log into that Java account automatically (configurable). |
| **Automatic builds & releases** | GitHub Actions builds every jar and publishes them on the [Releases](https://github.com/Doom2615/authme-freemium/releases) page. |
| **Dependency & upstream tracking** | Dependabot keeps dependencies up to date and a daily workflow opens an issue when AuthMeReloaded has new commits. |

## Bedrock auto-login (Geyser + Floodgate)

### Requirements
- [Geyser](https://geysermc.org/) and [Floodgate](https://geysermc.org/wiki/floodgate/) 2.x.
- **Floodgate must be installed on the server running AuthMe-Freemium.** Behind a proxy (Velocity/BungeeCord),
  install Floodgate on the proxy **and** on every backend server (as Floodgate's own setup guide describes),
  with the same `key.pem`.

### How it works
1. A player joins; AuthMe-Freemium asks Floodgate whether it is a real Bedrock player.
   Only players Floodgate reports as Bedrock players get Bedrock handling — **everyone else is a Java player**.
2. **Registered Bedrock player** → logged in immediately (login commands, sessions and proxy messages work as with a normal login).
3. **New Bedrock player** → registers normally, through a native Bedrock **register form** (password, confirmation and/or
   e-mail, depending on your registration settings).
4. If auto-login is disabled (`bedrock.autoLogin: false`, or a linked account with `autoLoginLinkedAccounts: false`),
   the Bedrock player gets a **login form**. Players with 2FA enabled get a **2FA code form**.

Java players are not affected and keep the normal password / premium flow and Java dialogs.

### Bedrock forms
Bedrock clients can't show Java dialogs, so AuthMe-Freemium sends native Bedrock forms through Floodgate
(the Cumulus form API):

- **Login form** — password field.
- **Register form** — the same fields as the Java register dialog (password, confirm password, e-mail).
- **2FA form** — authenticator code field.

Form titles and labels use the same translated texts as the Java dialogs. A submitted form runs the same
`/login`, `/register` or `/2fa code` command, so all password rules and checks are identical. If the attempt fails
(e.g. wrong password), the form is shown again. Forms are used on every server version (Spigot Legacy, Spigot 1.21,
Paper, Folia) and don't depend on the Java dialog settings. Like the Java dialogs, forms can't be dismissed: closing one opens it again.

### Configuration (`config.yml`)
```yml
bedrock:
    # Auto-login registered Bedrock players (needs Floodgate on this server)
    autoLogin: true
    # Auto-login Bedrock players linked to a Java account (Floodgate account linking)
    autoLoginLinkedAccounts: true
    # Native Bedrock forms for login / register / 2FA
    forms: true
```

## Premium / Freemium login
Players with a legitimate Mojang account can skip password authentication. Identity is verified with
Mojang's session server during the login phase.

- Enable with `settings.enablePremium: true`.
- Players opt in with `/premium` and out with `/freemium` (while logged in). Admins: `/authme premium <player>` / `/authme freemium <player>`.
- **Offline-mode, no proxy:** requires [PacketEvents](https://github.com/retrooper/packetevents) 2.x (fail-closed without it).
- **Online-mode proxy:** the proxy forwards the verified UUID; set `Hooks.bungeecord: true` on the backend.
- **Offline-mode proxy:** install the AuthMe-Freemium Velocity or Bungee jar on the proxy.
- Full documentation: [docs/premium.md](docs/premium.md)

## All features
- Builds for **Spigot Legacy** (1.16–1.19), **Spigot 1.21** (1.20–1.21), **Paper 1.21+** and **Folia 1.21+**
- Proxy plugins for **BungeeCord** and **Velocity**
- **Bedrock auto-login** and native **Bedrock login/register forms** through Floodgate
- **Premium bypass** for Mojang-account holders (`/premium`, `/freemium`)
- Graphical login/register dialogs, with optional Paper/Folia pre-join dialogs
- Session login, two-factor authentication (TOTP) and e-mail recovery
- Username spoofing protection, built-in AntiBot, country whitelist/blacklist
- MySQL, MariaDB, PostgreSQL and SQLite with cached queries
- Hashes: SHA256, ARGON2, BCRYPT, PBKDF2 and many forum/CMS formats — [full list](docs/hash_algorithms.md)
- Inventory protection and tab-complete blocking before login (PacketEvents)
- Messages in each player's client language — [translations](docs/translations.md)
- Importers for Auth+, LibreLogin, LimboAuth, nLogin, OpeNLogin, tiAuth and SQLite ↔ SQL migration — [converters](docs/converters.md)
- Automatic database backups

## Download
Grab the jars from the [Releases page](https://github.com/Doom2615/authme-freemium/releases):
- **Stable releases** are published when a `v*` tag is pushed (e.g. `v6.0.2`).
- **Development Build** (pre-release, tag `dev-build`) is updated on every push to `master`.

| Jar | Platform | Java |
|---|---|---|
| `AuthMe-Freemium-*-Spigot-Legacy.jar` | Spigot 1.16.x – 1.19.x | 17+ |
| `AuthMe-Freemium-*-Spigot-1.21.jar` | Spigot 1.20.x – 1.21.x | 21+ |
| `AuthMe-Freemium-*-Paper.jar` | Paper 1.21+ | 21+ |
| `AuthMe-Freemium-*-Folia.jar` | Folia 1.21+ | 21+ |
| `AuthMe-Freemium-*-Bungee.jar` | BungeeCord / Waterfall proxy | 21+ |
| `AuthMe-Freemium-*-Velocity.jar` | Velocity 3.4+ proxy | 21+ |

Optional plugins: [PacketEvents](https://github.com/retrooper/packetevents) 2.x, [Floodgate](https://geysermc.org/wiki/floodgate/) 2.x.

## Migrating from AuthMeReloaded
1. Stop the server and remove the old `AuthMe-*.jar`.
2. Drop in the matching `AuthMe-Freemium-*.jar`.
3. Start the server. The old `plugins/AuthMe` folder is moved to `plugins/AuthMe-Freemium` automatically,
   so your config, messages and database are kept.

The plugin declares `provides: [AuthMe]`, so plugins that depend on `AuthMe` keep working.
The Java API (`fr.xephi.authme.api.v3.AuthMeApi`) and commands are unchanged.
The Bungee/Velocity proxy plugins keep their original data folder names, so their configs are kept too.

## Configuration, commands and permissions
- [Configuration](docs/config.md)
- [Commands](docs/commands.md)
- [Permission nodes](docs/permission_nodes.md) — `authme.player.*` for user commands, `authme.admin.*` for admin commands
- [Proxy setup](docs/proxies)
- [Website integration](samples/website_integration)

## Building
Requirements: JDK 21+ (full build) or JDK 17 (core, tools and Spigot Legacy only), Maven 3.8.8+.

```sh
git clone https://github.com/Doom2615/authme-freemium.git
cd authme-freemium
mvn clean package
```

Jars are written to `<module>/target/AuthMe-Freemium-<version>-<Platform>.jar`. More commands: [docs/build.md](docs/build.md).

### CI / automation
- [`.github/workflows/build.yml`](.github/workflows/build.yml) — builds and tests on Java 17 and 21 for pull requests, pushes to `master` and `v*` tags, then publishes the jars to Releases.
- [`.github/dependabot.yml`](.github/dependabot.yml) — daily Maven and weekly GitHub Actions update PRs.
- [`.github/workflows/upstream-sync.yml`](.github/workflows/upstream-sync.yml) — daily check of [AuthMe/AuthMeReloaded](https://github.com/AuthMe/AuthMeReloaded); opens an issue listing new upstream commits.

## Support
Report bugs and request features on the [issue tracker](https://github.com/Doom2615/authme-freemium/issues).
Please don't report AuthMe-Freemium issues to the upstream AuthMeReloaded team.

## Credits
- Maintained by [Doom2615](https://github.com/Doom2615).
- Based on [AuthMeReloaded](https://github.com/AuthMe/AuthMeReloaded) by the AuthMe-Team
  ([developers](https://github.com/AuthMe/AuthMeReloaded/wiki/Development-team), [translators](https://github.com/AuthMe/AuthMeReloaded/wiki/Translators)).
- Bedrock support uses the [GeyserMC Floodgate API](https://github.com/GeyserMC/Floodgate).
- This product uses data from the GeoLite API created by MaxMind, available at https://www.maxmind.com

## License
GNU General Public License v3.0 — see [LICENSE](LICENSE).
