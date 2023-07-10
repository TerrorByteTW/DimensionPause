<p align="center">
    <a href="https://modrinth.com/plugin/dimensionpause">
        <img alt="Modrinth" src="https://img.shields.io/badge/Download%20at-Modrinth-brightgreen?style=for-the-badge&logo=modrinth">
    </a>
    <a href="https://polymart.org/resource/dimensionpause.4411">
        <img alt="Polymart" src="https://img.shields.io/badge/Download%20At-Polymart-%2303a092?style=for-the-badge">
    </a>
    <a href="https://builtbybit.com/resources/dimensionpause.30070/">
        <img alt="BuiltByBit" src="https://img.shields.io/badge/Download%20At-BuiltByBit-%232c86c1?style=for-the-badge">
    </a>
    <a href="https://www.spigotmc.org/resources/dimensionpause.111113/">
        <img alt="SpigotMC" src="https://img.shields.io/badge/Download%20At-SpigotMC-yellow?style=for-the-badge">
    </a>
</p>

# Dimension Pause 🌎⌚

## What is Dimension Pause?
Dimension Pause is a super simple, lightweight plugin that allows you to temporarily block players from creating dimension portals or entering dimensions.

It works by detecting players attempting to create portals, or detecting when a player switches worlds (Such as with using Essentials's /home feature). When this happens,
if the world is paused and certain criteria is not met, the player is either blocked from creating the portal, or kicked out of the world.

If the player is currently in a dimension when it is disabled, then they are kicked out to either their bed or a world defined in config.

## Future Features
* Support multiple worlds as well as dimensions
  * Currently, if you create multiple Nether worlds with a multi-world plugin, such as MultiVerse, you can only disable *all* Nether worlds, not just specific ones.
* Support Velocity / BungeeCord
* Temporarily disable dimensions (Disable dimensions for an hour, for example)
* Create an API for developers to integrate with DimensionPause

## Commands & Permissions
All commands may substitute `/dimensionpause` with `/dp` for conciseness

| Command                                | Permission              | Description                                                                                   |
|----------------------------------------|-------------------------|-----------------------------------------------------------------------------------------------|
| /dimensionpause                        | dimensionpause.commands | Displays help menu                                                                            |
| /dimensionpause toggle <end \| nether> | dimensionpause.toggle   | Pauses or unpauses a given dimension type                                                     |
| /dimensionpause state <end \| nether>  | dimensionpause.state    | Checks the state of a given dimension type                                                    |
| /dimensionpause reload                 | dimensionpause.reload   | Reloads DimensionPause configs and language files                                             |
|                                        | dimensionpause.bypass   | Allows players to bypass a bypassable world. If a world is not bypassable, only Ops may enter |
|                                        | dimensionpause.*        | Grants all permissions listed above                                                           |
