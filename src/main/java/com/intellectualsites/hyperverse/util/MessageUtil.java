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

package com.intellectualsites.hyperverse.util;

import com.intellectualsites.hyperverse.configuration.Message;
import com.intellectualsites.hyperverse.configuration.Messages;
import me.minidigger.minimessage.MiniMessageParser;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

/**
 * Message utility methods
 */
public final class MessageUtil {

    private MessageUtil() {
    }

    /**
     * Format a string. Replacements come in pairs of two, where
     * the first value is the string to be replaced, and the second
     * value is the replacement, example:
     * %key1%, value1, %key2%, value2
     *
     * @param message String to format
     * @param replacements Replacements, needs to be a multiple of 2
     * @return
     */
    @NotNull public static String format(@NotNull final String message, @NotNull final String... replacements) {
        if (replacements.length % 2 != 0) {
            throw new IllegalArgumentException("Replacement length must be a multiple of two");
        }
        String replacedMessage = Objects.requireNonNull(message);
        for (int i = 0; i < replacements.length; i += 2) {
            replacedMessage = replacedMessage.replace(replacements[i], replacements[i + 1]);
        }
        return replacedMessage;
    }

    /**
     * Send a message to a recipient
     *
     * @param recipient Receiver of the message
     * @param message Mesasge to send
     * @param replacements Replacements
     * @see #format(String, String...) for information about string replacements
     */
    public static void sendMessage(@NotNull final CommandSender recipient,
        @NotNull final Message message, @NotNull final String... replacements) {
        Objects.requireNonNull(recipient);
        final String replacedMessage = format(message.toString(), replacements);
        if (replacedMessage.isEmpty()) {
            return;
        }
        final String prefixedMessage = ChatColor.translateAlternateColorCodes('&',
            Messages.messagePrefix.toString() + replacedMessage);
        if (prefixedMessage.contains("<") && prefixedMessage.contains(">")) {
            recipient.spigot().sendMessage(MiniMessageParser.parseFormat(prefixedMessage));
        } else {
            recipient.sendMessage(prefixedMessage);
        }
    }

}
