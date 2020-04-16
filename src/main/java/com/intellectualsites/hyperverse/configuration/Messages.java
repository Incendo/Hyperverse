//
// Hyperverse - A minecraft world management plugin
// Copyright © 2020 Alexander Söderberg (sauilitired@gmail.com)
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

package com.intellectualsites.hyperverse.configuration;

import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class Messages {

    public static final Message messagePrefix =
        createMessage("other.prefix", "&8[&6Hyperverse&8] ");
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

    // Flag descriptions
    public static final Message flagDescriptionGamemode = createMessage("flags.gamemode",
        "World gamemode. Available values: survival, creative, adventure, spectator");
    public static final Message flagDescriptionLocalRespawn = createMessage("flags.local-respawn",
        "Whether or not players should respawn at their beds/global spawn, or respawn in this world"
            + " if they die inside it");


    public static Message createMessage(@NotNull final String key,
        @NotNull final String defaultValue) {
        return new Message(Objects.requireNonNull(key), Objects.requireNonNull(defaultValue));
    }

    public static String getConfigured(@NotNull final Message message) {
        // TODO: Actually load custom values
        return message.getDefaultValue();
    }

}
