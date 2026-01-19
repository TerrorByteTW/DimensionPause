<p align="center">
    <a href="https://modrinth.com/plugin/dimensionpause">
        <img alt="Modrinth" src="https://img.shields.io/badge/Download%20at-Modrinth-brightgreen?style=for-the-badge&logo=modrinth">
    </a>
    <a href="https://hangar.papermc.io/TerrorByte/DimensionPause">
        <img alt="Hangar" src="https://img.shields.io/badge/Download%20At-Hangar-%23f29f22?style=for-the-badge">
    </a>    
</p>

# Dimension Pause 🌎⌚

## What is Dimension Pause?

Dimension Pause is a super simple, lightweight plugin that allows you to temporarily block players from creating
dimension portals or entering dimensions.

It works by detecting players attempting to create portals, or detecting when a player switches worlds (Such as entering
an already-existing portal, or using Essentials's `/home` feature). When this happens,
if that world's dimension is paused and certain criteria are not met, the player is either blocked from creating the
portal, or kicked out of the world.

If the player is currently in a dimension when it is disabled, then they are kicked out to either their respawn location
or a world defined in config if their respawn location is unavailable.

## Current Features

* Completely block access to dimensions _per world_. See the "World Setup" section below for details
    * Players cannot create portals, enter portals or teleport via commands (Such as `/home` or `/warp`) to other
      dimensions. If the player was in a dimension that was paused while they were logged off, upon logging back on they
      will be teleported out after a configurable delay.
* Pause worlds until manually unpaused by server staff, or after a delay
    * Dimensions, by default, are paused indefinitely. However, you can specify a delay to pause the world for a
      duration.
* Supports custom translations/formatting for chat messages and titles. You are not locked to "DimensionPause" branding!
    * Upon loading the server, check out the `plugins/DimensionPause/lang/` folder for configuration options.
* Persistent & resilient expiration timers. Dimensions will not stay paused by accident if your server restarts or
  crashes!
    * DimensionPause uses Paper's native scheduler, and will (re)schedule timers for ALL temporarily paused dimensions
      on server start, dimension toggle, and world load.
* Folia Support
    * NOTE: Folia is **NOT TESTED**. While the plugin has been written with Folia in mind, we have not actually run it on
      Folia yet due to dependencies like LuckPerms not working on Folia yet. Use at your own risk, Folia will be tested
      in the next version

## Future Features

* Support Velocity / BungeeCord
* Create an API for developers to integrate with DimensionPause

## Commands & Permissions

All commands may substitute `/dimensionpause` with `/dp` for conciseness

| Command                                                                                                                  | Permission                                | Description                                                                                                                                         |
|--------------------------------------------------------------------------------------------------------------------------|-------------------------------------------|-----------------------------------------------------------------------------------------------------------------------------------------------------|
| /dimensionpause                                                                                                          | dimensionpause.commands                   | Displays help menu                                                                                                                                  |
| /dimensionpause toggle <world> <end \| nether> \[\<weeks\>w\]\[\<days\>d\]\[\<hours\>h\]\[\<minutes\>m\]\[\<seconds\>s\] | dimensionpause.toggle                     | Pauses or unpauses a given dimension type for a specific world, with an optional duration. The duration is how long from now the pause will expire. |
| /dimensionpause state <world> <end \| nether>                                                                            | dimensionpause.state                      | Checks the state of a given dimension type for a specific world                                                                                     |
| /dimensionpause reload                                                                                                   | dimensionpause.reload                     | Reloads DimensionPause configs and language files                                                                                                   |
|                                                                                                                          | dimensionpause.bypass.[world].[dimension] | Allows players to bypass a pause for a given world & dimension                                                                                      |
|                                                                                                                          | dimensionpause.*                          | Grants all permissions listed above                                                                                                                 |

## Requirements

DimensionPause 1.1.2 works for 1.17 and up. However, DimensionPause 2.0.0 requires the latest version of Paper (At the
time of writing, 1.21.11 & Java 21). Supporting older versions of Paper while maintaining _forward_ compatibility is a
massive pain. Out of the almost 100 servers running DimensionPause, less than 5% of servers are running a version of
Paper
unsupported by this plugin, and almost 75% of servers are supported.

## World Setup

DimensionPause works under the assumption that your Nether and End dimensions will be connected to
an Overworld dimension. DimensionPause does *not* support pausing dimensions for nether-only or end-only worlds, as they
do not have an associated overworld.

Since Paper does not link worlds to each other, instead it's assumed nether and end worlds follow the standard "_nether"
or "_the_end" naming conventions. For example, the default `world` dimension worlds should be called `world_nether` and
`world_the_end`. **If your worlds are not named like this, you cannot use DimensionPause**.