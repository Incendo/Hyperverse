//
// Hyperverse - A Minecraft world management plugin
//
// This program is free software: you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation, either version 3 of the License, or
// (at your option) any later version.
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.
//
// You should have received a copy of the GNU General Public License
// along with this program. If not, see <http://www.gnu.org/licenses/>.
//

package se.hyperver.hyperverse.configuration;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.jetbrains.annotations.NotNull;

import java.text.DecimalFormat;
import java.util.Collection;
import java.util.Map;
import java.util.Objects;

public class Messages {

    private static final Map<String, String> configuredMessages = Maps.newHashMap();
    private static final Collection<Message> messages = Lists.newLinkedList();

    public static final Message messageConfigReloaded = createMessage("config.reload",
        "&7Configuration has been reloaded");
    public static final Message messageMessagesReloaded = createMessage("config.messages.reload",
        "&7Messages have been reloaded");
    public static final Message messageMessageReloadFailed = createMessage("config.messages.reload.failure",
        "&cFailed to reload Hyperverse messages: %reason%");
    public static final Message messageAlreadyInWorld =
        createMessage("world.same-world", "&cYou are already in this world!");
    public static final Message messagePrefix =
        createMessage("other.prefix", "&8[&6Hyperverse&8] ");
    public static final Message messageNoPlayerFound = createMessage("other.no-player-found",
        "&cPlayer with name %name% was not found.");
    public static final Message messageNoPlayersInWorld = createMessage("other.no-player-in-world",
        "&6%world%&7: There are no players in this world");
    public static final Message messagePlayerCurrentWorld = createMessage("other.player-current-world",
        "<hover:show_text:\"<gray>Location:</gray> <white>%x%</white><gray>, </gray><white>%y%</white><gray>, </gray><white>%z%</white>\"><white>%player%</white> <gray>is currently in world:</gray> <white>%world%</white></hover> ");
    public static final Message messageWorldExists =
        createMessage("world.exists", "&cA world with that name already exists");
    public static final Message messageWorldNameInvalid = createMessage("world.invalid_name",
        "&cA world name may only contain (up to) 16 alphanumerical characters, - and _");
    public static final Message messageGeneratorInvalid =
        createMessage("world.invalid_generator", "&cThere is no generator with that name: %generator% (World: %world%)");
    public static final Message messageCreationUnknownFailure =
        createMessage("world.unknown_failure", "&cSomething went wrong when generating the world");
    public static final Message messageWorldCreationStarted =
        createMessage("world.creation_started", "&7Creating a world with the following properties");
    public static final Message messageWorldProperty =
        createMessage("world.property", "&8- &7%property%&8: &7%value%");
    public static final Message messageWorldCreationFinished =
        createMessage("world.creation_finished", "&7The world was created successfully");
    public static final Message messageWorldImportedOnLoad = createMessage("world.load_imported",
        "&7Hyperverse detected that %world% initialized, and successfully imported it (Generator: %generator%)");
    public static final Message messageWorldImportFinished = createMessage("world.import_finished",
        "&7World import completed.");
    public static final Message messageWorldLoaded =
        createMessage("world.loaded", "&7Loaded %num% worlds");
    public static final Message messageWorldImportFailure = createMessage("world.load_failure",
        "&cHyperverse failed to import world '%world%'. Result: %result%");
    public static final Message messageWorldLoadDetected =
        createMessage("world.load_detected", "&7Detected loading world: '%world%'");
    public static final Message messageWorldsLoading = createMessage("world.loading",
        "&7Loading stored worlds from configuration files. Path: %path%");
    public static final Message messageGeneratorNotAvailable = createMessage("generator.not_available",
        "&7World '%world%' has requested generator '%generator%', but it is not yet available."
            + " The world will be created as soon as the generator is available.");
    public static final Message messageGeneratorAvailable = createMessage("generator.available",
        "&7The generator for world '%world%' is now available. The world will be created.");
    public static final Message messageListHeader = createMessage("list.header",
        "&7Available Worlds: ");
    public static final Message messageListEntry = createMessage("list.entry",
        "&8- <hover:show_text:\"<gray>Click to teleport to the world</gray>\"><click:run_command:/hvtp %name%>"
            + "&7%name%</click></hover> &8(&7%generator%&8: &7%type%&8) - %load_status%");
    public static final Message messageListEntryCurrentWorld = createMessage("list.entry.current-world",
        "<dark_gray>-</dark_gray> <white>%name% (%generator%&7: &f%type%)</white> <dark_gray>-</dark_gray> %load_status%");
    public static final Message messageListEntryPlayer = createMessage("list.entry.player",
        "&f<hover:show_text:\"<gray>Location:</gray> <white>%x%</white><gray>, </gray><white>%y%</white><gray>,"
            + " </gray><white>%z%</white>\">%player%</hover>");
    public static final Message messageListEntryWorld = createMessage("list.entry.player-current-world",
        "&6%world%&7: %players%");
    public static final Message messageNoSuchWorld = createMessage("world.non_existent",
        "&cThere is no world with that name");
    public static final Message messageTeleporting = createMessage("teleport.teleporting",
        "&7You are being teleported to %world%");
    public static final Message messageInfoHeader = createMessage("info.header",
        "&7World Information");
    public static final Message messageWorldCreationFailed = createMessage("world.creation_failed",
        "&cWorld creation failed: Reason: %reason%");
    public static final Message messageWorldUnloaded = createMessage("world.unload_success",
        "&7The world was successfully unloaded");
    public static final Message messageWorldUnloadFailed = createMessage("world.unload_failure",
        "&cThe world could not be unloaded: %reason%");
    public static final Message messageWorldNotLoaded = createMessage("world.world_not_loaded",
        "&cThat world is not loaded");
    public static final Message messageWorldAlreadyLoaded = createMessage("world.already_loaded",
        "&cThat world is already loaded");
    public static final Message messageWorldAlreadyImported = createMessage("world.already_imported",
        "&cThat world has already been imported");
    public static final Message messageWorldLoadedSuccessfully = createMessage("world.load_success",
        "&7The world has been loaded");
    public static final Message messageFlagParseError = createMessage("flag.parse_error",
        "&cError when parsing flag '%flag%', value '%value%'. Reason: %reason%");
    public static final Message messageFlagUnknown = createMessage("flag.unknown",
        "&cThere is no flag with that name");
    public static final Message messageFlagSet = createMessage("flag.set",
        "&7The flag was updated");
    public static final Message messageFlagRemoved = createMessage("flag.removed",
        "&7The flag was removed");
    public static final Message messageGameRuleParseError = createMessage("gamerule.parse_error",
        "&cThat is not a valid value for the game rule");
    public static final Message messageGameRuleUnknown = createMessage("gamerule.unknown",
        "&cThere is no game rule with that name");
    public static final Message messageGameRuleSet = createMessage("gamerule.set",
        "&7The game rule was updated");
    public static final Message messageGameRuleRemoved = createMessage("gamerule.removed",
        "&7The game rule was removed");
    public static final Message messageWorldNotRemoved = createMessage("world.not_remoted",
        "&cThe world could not be removed. Reason: %reason%");
    public static final Message messageWorldRemoved = createMessage("world.removed",
        "&7The world was removed successfully");
    public static final Message messageNotPermittedEntry = createMessage("world.not_permitted",
        "&cYou are not allowed to enter that world");
    public static final Message messageTeleportNotSafe = createMessage("world.not_safe",
        "&cTeleportation to that location is not safe. An attempt to find a safe destination will be made.");
    public static final Message messageLogTooBig = createMessage("paste.log_too_big",
        "&clatest.log is too big to be pasted, will ignore");
    public static final Message messagePasteUpload = createMessage("paste.uploaded",
        "&7The paste file has been uploaded to: %paste%");
    public static final Message messagePasteFailed = createMessage("paste.failed",
        "&cFailed to create the debug paste. Reason: %reason%");
    public static final Message messageInvalidWorldType = createMessage("invalid.world_type",
        "&cThat is not a valid world type");
    public static final Message messageInvalidWorldFeatures = createMessage("invalid.world_features",
        "&cThat is not a valid type");
    public static final Message messageInvalidGameRule = createMessage("invalid.game_rule",
        "&cThat is not a valid game rule");
    public static final Message messageInvalidStructureSetting = createMessage("invalid.structure_setting",
        "&cThat is not a valid world structure setting");
    public static final Message messagePortalNotLinked = createMessage("portal.not_linked",
        "&cI'm sorry, that portal can't take you anywhere!");

    // Multiverse importer messages
    public static final Message messageMultiverseMissing = createMessage("multiverse.missing",
        "&cCannot import Multiverse configurations because Multiverse-Core is not enabled on this server");
    public static final Message messageMultiverseInitializing = createMessage("multiverse.initializing",
        "&7Multiverse importing process has started. Detected %worlds% worlds.");
    public static final Message messageMultiverseCreating = createMessage("multiverse.creating",
        "&7Multiverse world %world% was detected and had no equivalent in Hyperverse. It will be created.");
    public static final Message messageMultiverseImporting = createMessage("multiverse.importing",
        "&7Hyperverse will now import the Multiverse configuration for %world%");
    public static final Message messageMultiverseImported = createMessage("multiverse.imported",
        "&7Hyperverse finished importing %world%.");
    public static final Message messageMultiverseDone = createMessage("multiverse.done",
        "&7Hyperverse finished importing Multiverse data.");

    // Flag descriptions
    public static final Message flagDescriptionGamemode = createMessage("flags.gamemode",
        "World gamemode. Available values: survival, creative, adventure and spectator");
    public static final Message flagDescriptionLocalRespawn = createMessage("flags.local-respawn",
        "Whether or not players should respawn at their beds/global spawn, or respawn in this world"
            + " if they die inside it");
    public static final Message flagDescriptionForceSpawn = createMessage("flags.force-spawn",
        "Whether or not players should be teleported to the world spawn each time they enter the world");
    public static final Message flagDescriptionPvp = createMessage("flags.pvp",
        "Whether or not player vs. player combat is enabled");
    public static final Message flagDescriptionPve = createMessage("flags.pve",
        "Whether or not player vs. entity combat is enabled");
    public static final Message flagDescriptionWorldPermission = createMessage("flags.world-permission",
        "Permission node required to visit the world");
    public static final Message flagDescriptionNether = createMessage("flags.nether",
        "Name of the dimension linked to this world's nether portals");
    public static final Message flagDescriptionEnd = createMessage("flags.end",
        "Name of the dimension linked to this world's end portals");
    public static final Message flagDescriptionProfileGroup = createMessage("flags.profile-group",
        "Name of the profile group that the world belongs to");
    public static final Message flagDescriptionDifficulty = createMessage("flags.difficulty",
        "World difficulty. Available values are: peaceful, easy, normal and hard");

    // Command Descriptions
    public static final Message commandDescriptionCreate = createMessage("command.create",
        "Create a new world");
    public static final Message commandDescriptionList = createMessage("command.list",
        "List Hyperverse worlds");
    public static final Message commandDescriptionImport = createMessage("command.import",
        "Import a world into Hyperverse");
    public static final Message commandDescriptionTeleport = createMessage("command.teleport",
        "Teleport between hyperverse worlds");
    public static final Message commandDescriptionUnload = createMessage("command.unload",
        "Unload a world");
    public static final Message commandDescriptionLoad = createMessage("command.load",
        "Load a world");
    public static final Message commandDescriptionFind = createMessage("command.find",
        "See what world a player is in");
    public static final Message commandDescriptionWho = createMessage("command.who",
        "Find the current players in a world");
    public static final Message commandDescriptionFlagSet = createMessage("command.flag.set",
        "Set a world flag");
    public static final Message commandDescriptionFlagRemove = createMessage("command.flag.remove",
        "Remove a world flag");
    public static final Message commandDescriptionGameRuleSet = createMessage("command.gamerule.set",
        "Set a world game rule");
    public static final Message commandDescriptionGameRuleRemove = createMessage("command.gamerule.remove",
        "Remove a world game rule");
    public static final Message commandDescriptionDelete = createMessage("command.delete",
        "Delete a world");
    public static final Message commandDescriptionReload = createMessage("command.reload",
        "Reload the Hyperverse configuration files");
    public static final Message commandDescriptionDebugPaste = createMessage("command.debugpaste",
        "Create a debug paste. This will upload your configuration files to Athion. Beware!");
    public static final Message commandDescriptionMultiverse = createMessage("command.multiverse",
        "Import Multiverse configurations");
    public static final Message commandDescriptionPlugin = createMessage("command.plugin",
        "Show plugin information");

    public static final DecimalFormat miscCoordinateDecimalFormat = new DecimalFormat("#.##");

    @NotNull public static Map<String, String> getConfiguredMessages() {
        return configuredMessages;
    }

    @NotNull public static Map<Message, String> getMessages() {
        final Map<Message, String> map = Maps.newHashMapWithExpectedSize(messages.size());
        for (final Message message : messages) {
            map.put(message, message.toString());
        }
        return map;
    }

    public static Message createMessage(@NotNull final String key,
        @NotNull final String defaultValue) {
        configuredMessages.put(key, defaultValue);
        final Message message = new Message(Objects.requireNonNull(key), Objects.requireNonNull(defaultValue));
        messages.add(message);
        return message;
    }

    public static String getConfigured(@NotNull final Message message) {
        return configuredMessages.getOrDefault(message.getKey(), message.getDefaultValue());
    }

}
