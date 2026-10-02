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
| **Bedrock auto-login** | Players connecting through Geyser are detected with the Floodgate API and logged in automatically. |
| **Bedrock auto-register** | First-time Bedrock players get an account with a random password, so they never see a register prompt. |
| **Floodgate linked accounts** | Bedrock players linked to a Java account log into that Java account automatically (configurable). |
| **Account takeover protection** | Bedrock auto-login is disabled when Floodgate has no username prefix, unless you explicitly allow it. |
| **Bedrock-safe dialogs** | Bedrock players skip the Paper pre-join / post-join dialogs, which Bedrock clients can't display. |
| **Automatic builds & releases** | GitHub Actions builds every jar and publishes them on the [Releases](https://github.com/Doom2615/authme-freemium/releases) page. |
| **Dependency & upstream tracking** | Dependabot keeps dependencies up to date and a daily workflow opens an issue when AuthMeReloaded has new commits. |

## Bedrock auto-login (Geyser + Floodgate)

### Requirements
- [Geyser](https://geysermc.org/) and [Floodgate](https://geysermc.org/wiki/floodgate/) 2.x.
- **Floodgate must be installed on the server running AuthMe-Freemium.** Behind a proxy (Velocity/BungeeCord),
  install Floodgate on the proxy **and** on every backend server (as Floodgate's own setup guide describes),
  with the same `key.pem`.

### How it works
1. A Bedrock player joins; AuthMe-Freemium asks Floodgate whether the player is a Bedrock player.
2. **Registered** → logged in immediately (login commands, session and proxy messages work as with a normal login).
3. **Not registered** and `bedrock.autoRegister: true` → an account with a random 32-character password is created, then the player is logged in.
4. **Not registered** and `bedrock.autoRegister: false` → the normal register flow is used.

Java players are not affected and keep the normal password / premium flow.

### Configuration (`config.yml`)
```yml
bedrock:
    # Auto-login Bedrock players (needs Floodgate on this server)
    autoLogin: true
    # Create an account with a random password for new Bedrock players
    autoRegister: true
    # Auto-login Bedrock players linked to a Java account (Floodgate account linking)
    autoLoginLinkedAccounts: true
    # Allow auto-login when Floodgate's username-prefix is empty (NOT recommended)
    allowWithoutUsernamePrefix: false
```

> **Security note:** keep a Floodgate `username-prefix` (default `.`). Without a prefix, a Bedrock player called
> `Steve` would log into the Java player `Steve`'s account. AuthMe-Freemium refuses Bedrock auto-login in that
> case unless you set `bedrock.allowWithoutUsernamePrefix: true`.

A Bedrock player who wants to log in from Java later can get a password from an admin with
`/authme password <player> <password>`.

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
- **Bedrock auto-login / auto-register** through Floodgate
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
