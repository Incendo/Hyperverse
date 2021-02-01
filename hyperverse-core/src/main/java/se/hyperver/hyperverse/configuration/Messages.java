//
//  Hyperverse - A minecraft world management plugin
//
//  This program is free software: you can redistribute it and/or modify
//  it under the terms of the GNU General Public License as published by
//  the Free Software Foundation, either version 3 of the License, or
//  (at your option) any later version.
//
//  This program is distributed in the hope that it will be useful,
//  but WITHOUT ANY WARRANTY; without even the implied warranty of
//  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
//  GNU General Public License for more details.
//
//  You should have received a copy of the GNU General Public License
//  along with this program. If not, see <http://www.gnu.org/licenses/>.
//

package se.hyperver.hyperverse.configuration;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.text.DecimalFormat;
import java.util.Collection;
import java.util.Map;
import java.util.Objects;

public final class Messages {

    public static final DecimalFormat miscCoordinateDecimalFormat = new DecimalFormat("#.##");
    private static final Map<String, String> configuredMessages = Maps.newHashMap();
    private static final Collection<Message> messages = Lists.newLinkedList();
    public static final @NonNull Message messageConfigReloaded = createMessage(
            "config.reload",
            "&7Configuration has been reloaded"
    );
    public static final @NonNull Message messageMessagesReloaded = createMessage(
            "config.messages.reload",
            "&7Messages have been reloaded"
    );
    public static final @NonNull Message messageMessageReloadFailed = createMessage(
            "config.messages.reload.failure",
            "&cFailed to reload Hyperverse messages: %reason%"
    );
    public static final @NonNull Message messageAlreadyInWorld =
            createMessage("world.same-world", "&cYou are already in this world!");
    public static final @NonNull Message messagePlayerAlreadyInWorld = createMessage(
            "world.player-same-world",
            "&c%player% is already in that world!"
    );
    public static final @NonNull Message messagePrefix =
            createMessage("other.prefix", "&8[&6Hyperverse&8] ");
    public static final @NonNull Message messagePrefixFancy =
            createMessage("other.prefix-fancy", "<dark_gray>[<gold>Hyperverse</gold>] </dark_gray>");
    public static final @NonNull Message messageNoPlayerFound = createMessage(
            "other.no-player-found",
            "&cPlayer with name %name% was not found."
    );
    public static final @NonNull Message messageSpecifyPlayer = createMessage(
            "other.specify-player",
            "&cPlease specify a player!"
    );
    public static final @NonNull Message messageNoPlayersInWorld = createMessage(
            "other.no-players-in-world",
            "<gold>%world%</gold><gray>: There are no players in this world</gray>"
    );
    public static final @NonNull Message messagePlayerCurrentWorld = createMessage(
            "other.player-world",
            "<hover:show_text:\"<gray>Location:</gray> <white>%x%</white><gray>, </gray><white>%y%</white><gray>, </gray><white>%z%</white>\"><white>%player%</white> <gray>is currently in world:</gray> <white>%world%</white></hover> "
    );
    public static final @NonNull Message messageWorldExists =
            createMessage("world.exists", "&cA world with that name already exists");
    public static final @NonNull Message messageWorldNameInvalid = createMessage(
            "world.invalid_name",
            "&cA world name may only contain (up to) 16 alphanumerical characters, - and _"
    );
    public static final @NonNull Message messageGeneratorInvalid =
            createMessage("world.invalid_generator", "&cThere is no generator with that name: %generator% (World: %world%)");
    public static final @NonNull Message messageCreationUnknownFailure =
            createMessage("world.unknown_failure", "&cSomething went wrong when generating the world");
    public static final @NonNull Message messageWorldCreationStarted =
            createMessage("world.creation_started", "&7Creating a world with the following properties");
    public static final @NonNull Message messageWorldProperty =
            createMessage(
                    "world.world-property",
                    "<dark_gray>- </dark_gray><gray>%property%</gray><dark_gray>: </dark_gray><gray>%value%</gray>"
            );
    public static final @NonNull Message messageWorldCreationFinished =
            createMessage("world.creation_finished", "&7The world was created successfully");
    public static final @NonNull Message messageWorldImportedOnLoad = createMessage(
            "world.load_imported",
            "&7Hyperverse detected that %world% initialized, and successfully imported it (Generator: %generator%)"
    );
    public static final @NonNull Message messageWorldImportFinished = createMessage(
            "world.import_finished",
            "&7World import completed."
    );
    public static final @NonNull Message messageWorldLoaded =
            createMessage("world.loaded", "&7Loaded %num% worlds");
    public static final @NonNull Message messageWorldImportFailure = createMessage(
            "world.load_failure",
            "&cHyperverse failed to import world '%world%'. Result: %result%"
    );
    public static final @NonNull Message messageWorldLoadDetected =
            createMessage("world.load_detected", "&7Detected loading world: '%world%'");
    public static final @NonNull Message messageWorldsLoading = createMessage(
            "world.loading",
            "&7Loading stored worlds from configuration files. Path: %path%"
    );
    public static final @NonNull Message messageGeneratorNotAvailable = createMessage(
            "generator.not_available",
            "&7World '%world%' has requested generator '%generator%', but it is not yet available."
                    + " The world will be created as soon as the generator is available."
    );
    public static final @NonNull Message messageGeneratorAvailable = createMessage(
            "generator.available",
            "&7The generator for world '%world%' is now available. The world will be created."
    );
    public static final @NonNull Message messageListEntry = createMessage(
            "list.world-entry",
            "<dark_gray>-</dark_gray> <hover:show_text:\"<gray>Click to teleport to the world</gray>\"><click:run_command:/hvtp %name%>"
                    + "<gray>%display-name%</gray></click></hover> <dark_gray>(<gray>%generator%</gray>: <gray>%type%</gray>) - %load_status%"
    );
    public static final @NonNull Message messageListEntryCurrentWorld = createMessage(
            "list.world-entry-current",
            "<dark_gray>-</dark_gray> <white>%display-name% (%generator%<gray>:</gray> %type%)</white> <dark_gray>-</dark_gray> %load_status%"
    );
    public static final @NonNull Message messageListEntryPlayer = createMessage(
            "list.player-entry",
            "<white><hover:show_text:\"<gray>Location:</gray> <white>%x%</white><gray>, </gray><white>%y%</white><gray>,"
                    + " </gray><white>%z%</white>\">%player%</hover></white>"
    );
    public static final @NonNull Message messageListEntryWorld = createMessage(
            "list.entry.player-world",
            "<gold>%world%</gold><gray>: %players%</gray>"
    );
    public static final @NonNull Message messageNoSuchWorld = createMessage(
            "world.non_existent",
            "&cThere is no world with that name"
    );
    public static final @NonNull Message messageNotHyperWorld = createMessage(
            "world.not-hyperworld",
            "&cYou are not in a HyperWorld."
    );
    public static final @NonNull Message messageTeleporting = createMessage(
            "teleport.teleporting-to-world",
            "<gray>You are being teleported to %world%</gray>"
    );
    public static final @NonNull Message messageTeleportingPlayer = createMessage(
            "teleport.teleporting-other",
            "<gray>%player% is being teleported to %world%</gray>"
    );
    public static final @NonNull Message messageInfoHeader = createMessage(
            "info.header",
            "&7World Information"
    );
    public static final @NonNull Message messageWorldCreationFailed = createMessage(
            "world.creation_failed",
            "&cWorld creation failed: Reason: %reason%"
    );
    public static final @NonNull Message messageWorldUnloaded = createMessage(
            "world.unload_success",
            "&7The world was successfully unloaded"
    );
    public static final @NonNull Message messageWorldUnloadFailed = createMessage(
            "world.unload_failure",
            "&cThe world could not be unloaded: %reason%"
    );
    public static final @NonNull Message messageWorldNotLoaded = createMessage(
            "world.world_not_loaded",
            "&cThat world is not loaded"
    );
    public static final @NonNull Message messageWorldAlreadyLoaded = createMessage(
            "world.already_loaded",
            "&cThat world is already loaded"
    );
    public static final @NonNull Message messageWorldAlreadyImported = createMessage(
            "world.already_imported",
            "&cThat world has already been imported"
    );
    public static final @NonNull Message messageWorldLoadedSuccessfully = createMessage(
            "world.load_success",
            "&7The world has been loaded"
    );
    public static final @NonNull Message messageFlagParseError = createMessage(
            "flag.parse_error",
            "&cError when parsing flag '%flag%', value '%value%'. Reason: %reason%"
    );
    public static final @NonNull Message messageFlagUnknown = createMessage(
            "flag.unknown",
            "&cThere is no flag with that name"
    );
    public static final @NonNull Message messageFlagSet = createMessage(
            "flag.set",
            "&7The flag was updated"
    );
    public static final @NonNull Message messageFlagRemoved = createMessage(
            "flag.removed",
            "&7The flag was removed"
    );
    public static final @NonNull Message messageFlagDisplayInfo = createMessage(
            "flag.display_info",
            "&7Description: %description% \n" + messagePrefix + "&7Default: %default%, Current: %current%"
    );
    public static final @NonNull Message messageGameRuleParseError = createMessage(
            "gamerule.parse_error",
            "&cThat is not a valid value for the game rule"
    );
    public static final @NonNull Message messageGameRuleUnknown = createMessage(
            "gamerule.unknown",
            "&cThere is no game rule with that name"
    );
    public static final @NonNull Message messageGameRuleSet = createMessage(
            "gamerule.set",
            "&7The game rule was updated"
    );
    public static final @NonNull Message messageGameRuleRemoved = createMessage(
            "gamerule.removed",
            "&7The game rule was removed"
    );
    public static final @NonNull Message messageWorldNotRemoved = createMessage(
            "world.not_remoted",
            "&cThe world could not be removed. Reason: %reason%"
    );
    public static final @NonNull Message messageWorldRemoved = createMessage(
            "world.removed",
            "&7The world was removed successfully"
    );
    public static final @NonNull Message messageNotPermittedEntry = createMessage(
            "world.not_permitted",
            "&cYou are not allowed to enter that world"
    );
    public static final @NonNull Message messageTeleportNotSafe = createMessage(
            "world.not_safe",
            "&cTeleportation to that location is not safe. An attempt to find a safe destination will be made."
    );
    public static final @NonNull Message messageLogTooBig = createMessage(
            "paste.log_too_big",
            "&clatest.log is too big to be pasted, will ignore"
    );
    public static final @NonNull Message messagePasteUpload = createMessage(
            "paste.uploaded",
            "&7The paste file has been uploaded to: %paste%"
    );
    public static final @NonNull Message messagePasteFailed = createMessage(
            "paste.failed",
            "&cFailed to create the debug paste. Reason: %reason%"
    );
    public static final @NonNull Message messageInvalidProfileGroup =
            createMessage("invalid.profile_group", "&cThat is not a valid profile group");
    public static final @NonNull Message messageInvalidGameRule = createMessage(
            "invalid.game_rule",
            "&cThat is not a valid game rule"
    );
    public static final @NonNull Message messageInvalidWorldType = createMessage(
            "invalid.world_type",
            "&cThat is not a valid world type"
    );
    public static final @NonNull Message messageInvalidWorldFeatures = createMessage(
            "invalid.world_feature",
            "&cThat is not a valid feature type"
    );
    public static final @NonNull Message messageInvalidStructureSetting = createMessage(
            "invalid.structure_setting",
            "&cThat is not a valid world structure setting"
    );
    public static final @NonNull Message messagePortalNotLinked = createMessage(
            "portal.not_linked",
            "&cI'm sorry, that portal can't take you anywhere!"
    );
    public static final @NonNull Message messageRespawnWorldNonExistent = createMessage(
            "world.respawn_non_existent",
            "&cThe respawn world linked to this world does not exist"
    );
    public static final @NonNull Message messageGameModeOverride = createMessage(
            "override.gamemode",
            "<gray>The world has the game mode '<gold>%mode%</gold>' but your permission level prevented this from applying.</gray>"
    );
    public static final @NonNull Message messageListHeader = createMessage(
            "list.paginated-header",
            "<gold>World List <dark_gray>(</dark_gray><gray>%page%</gray>/<gray>%max%</gray><dark_gray>)</dark_gray>"
    );
    // Foreign plugin importer messages
    public static final @NonNull Message messageImportPluginMissing = createMessage(
            "importer.missing",
            "&cCannot import %plugin% configurations because %plugin% is not enabled on this server"
    );
    public static final @NonNull Message messageImportPluginInitializing = createMessage(
            "importer.initializing",
            "&7%plugin% importing process has started. Detected %worlds% worlds."
    );
    public static final @NonNull Message messageImportPluginCreating = createMessage(
            "importer.creating",
            "&7%plugin% world %world% was detected and had no equivalent in Hyperverse. It will be created."
    );
    public static final @NonNull Message messageImportingExternalWorld = createMessage(
            "importer.importing",
            "&7Hyperverse will now import the %plugin% configuration for %world%"
    );
    public static final @NonNull Message messageExternalImportCompleted = createMessage(
            "importer.imported",
            "&7Hyperverse finished importing %world%."
    );
    public static final @NonNull Message messageImportDone = createMessage(
            "importer.done",
            "&7Hyperverse finished importing %plugin% data."
    );
    // Feature messages
    public static final @NonNull Message messageFeaturesLoading = createMessage(
            "feature.loading",
            "&7Loading plugin features (%num% registered)"
    );
    public static final @NonNull Message messageFeatureLoaded = createMessage(
            "feature.loaded",
            "&7Enabled plugin feature '&6%feature%&7' for plugin '&6%plugin%&7'"
    );
    // Flag descriptions
    public static final @NonNull Message flagDescriptionGamemode = createMessage(
            "flags.gamemode",
            "World gamemode. Available values: survival, creative, adventure and spectator"
    );
    public static final @NonNull Message flagDescriptionLocalRespawn = createMessage(
            "flags.local-respawn",
            "Whether or not players should respawn at their beds/global spawn, or respawn in this world"
                    + " if they die inside it"
    );
    public static final @NonNull Message flagDescriptionForceSpawn = createMessage(
            "flags.force-spawn",
            "Whether or not players should be teleported to the world spawn each time they enter the world"
    );
    public static final @NonNull Message flagDescriptionPvp = createMessage(
            "flags.pvp",
            "Whether or not player vs. player combat is enabled"
    );
    public static final @NonNull Message flagDescriptionPve = createMessage(
            "flags.pve",
            "Whether or not player vs. entity combat is enabled"
    );
    public static final @NonNull Message flagDescriptionWorldPermission = createMessage(
            "flags.world-permission",
            "Permission node required to visit the world"
    );
    public static final @NonNull Message flagDescriptionNether = createMessage(
            "flags.nether",
            "Name of the dimension linked to this world's nether portals"
    );
    public static final @NonNull Message flagDescriptionEnd = createMessage(
            "flags.end",
            "Name of the dimension linked to this world's end portals"
    );
    public static final @NonNull Message flagDescriptionProfileGroup = createMessage(
            "flags.profile-group",
            "Name of the profile group that the world belongs to"
    );
    public static final @NonNull Message flagDescriptionDifficulty = createMessage(
            "flags.difficulty",
            "World difficulty. Available values are: peaceful, easy, normal and hard"
    );
    public static final @NonNull Message flagDescriptionCreatureSpawn = createMessage(
            "flags.creature-spawn",
            "Whether or not creatures are allowed to spawn in the world"
    );
    public static final @NonNull Message flagDescriptionMobSpawn = createMessage(
            "flags.mob-spawn",
            "Whether or not mobs are allowed to spawn in the world"
    );
    public static final @NonNull Message flagDescriptionAdvancements = createMessage(
            "flags.advancements",
            "Whether or not advancements can be obtained in the world"
    );
    public static final @NonNull Message flagDescriptionRespawnWorld = createMessage(
            "flags.respawn-world",
            "The world you respawn in if local-respawn is disabled"
    );
    public static final @NonNull Message flagDescriptionIgnoreBeds = createMessage(
            "flags.ignore-beds",
            "Forces players to respawn at the world spawn location rather than their bed spawns"
    );
    public static final @NonNull Message flagDescriptionAlias = createMessage(
            "flags.alias",
            "Name of the world as shown in lists, placeholders, etc"
    );
    public static final @NonNull Message flagDescriptionUnloadSpawn = createMessage(
            "flags.unload-spawn",
            "Force the world spawn to be unloaded"
    );
    // Command Descriptions
    public static final @NonNull Message commandDescriptionCreate = createMessage(
            "command.create",
            "Create a new world"
    );
    public static final @NonNull Message commandDescriptionList = createMessage(
            "command.list",
            "List Hyperverse worlds"
    );
    public static final @NonNull Message commandDescriptionImport = createMessage(
            "command.import",
            "Import a world into Hyperverse"
    );
    public static final @NonNull Message commandDescriptionTeleport = createMessage(
            "command.teleport",
            "Teleport between hyperverse worlds"
    );
    public static final @NonNull Message commandDescriptionTeleportGroup = createMessage(
            "command.teleportgroup", "Teleport to the last saved location in a specified profile-group");
    public static final @NonNull Message commandDescriptionUnload = createMessage(
            "command.unload",
            "Unload a world"
    );
    public static final @NonNull Message commandDescriptionLoad = createMessage(
            "command.load",
            "Load a world"
    );
    public static final @NonNull Message commandDescriptionFind = createMessage(
            "command.find",
            "See what world a player is in"
    );
    public static final @NonNull Message commandDescriptionWho = createMessage(
            "command.who",
            "Find the current players in a world"
    );
    public static final @NonNull Message commandDescriptionFlagSet = createMessage(
            "command.flag.set",
            "Set a world flag"
    );
    public static final @NonNull Message commandDescriptionFlagRemove = createMessage(
            "command.flag.remove",
            "Remove a world flag"
    );
    public static final @NonNull Message commandDescriptionGameRuleSet = createMessage(
            "command.gamerule.set",
            "Set a world game rule"
    );
    public static final @NonNull Message commandDescriptionGameRuleRemove = createMessage(
            "command.gamerule.remove",
            "Remove a world game rule"
    );
    public static final @NonNull Message commandDescriptionDelete = createMessage(
            "command.delete",
            "Delete a world"
    );
    public static final @NonNull Message commandDescriptionReload = createMessage(
            "command.reload",
            "Reload the Hyperverse configuration files"
    );
    public static final @NonNull Message commandDescriptionDebugPaste = createMessage(
            "command.debugpaste",
            "Create a debug paste. This will upload your configuration files to Athion. Beware!"
    );
    public static final @NonNull Message commandDescriptionMultiverse = createMessage(
            "command.multiverse",
            "Import Multiverse configurations"
    );
    public static final @NonNull Message commandDescriptionPlugin = createMessage(
            "command.plugin",
            "Show plugin information"
    );
    public static final @NonNull Message commandDescriptionRegenerate = createMessage(
            "command.regenerate",
            "Regenerate a world"
    );

    public static @NonNull Map<@NonNull String, @NonNull String> getConfiguredMessages() {
        return configuredMessages;
    }

    public static @NonNull Map<@NonNull Message, @NonNull String> getMessages() {
        final Map<Message, String> map = Maps.newHashMapWithExpectedSize(messages.size());
        for (final Message message : messages) {
            map.put(message, message.toString());
        }
        return map;
    }

    public static @NonNull Message createMessage(
            final @NonNull String key,
            final @NonNull String defaultValue
    ) {
        configuredMessages.put(key, defaultValue);
        final Message message = new Message(Objects.requireNonNull(key), Objects.requireNonNull(defaultValue));
        messages.add(message);
        return message;
    }

    public static @NonNull String getConfigured(final @NonNull Message message) {
        return configuredMessages.getOrDefault(message.getKey(), message.getDefaultValue());
    }

    private Messages() {
    }

}
