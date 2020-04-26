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

package se.hyperver.hyperverse.util;

import se.hyperver.hyperverse.configuration.Message;
import se.hyperver.hyperverse.configuration.Messages;

import net.kyori.text.adapter.bukkit.TextAdapter;
import net.md_5.bungee.api.chat.TextComponent;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

import me.minidigger.minimessage.bungee.MiniMessageSerializer;
import me.minidigger.minimessage.text.MiniMessageParser;

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
     * @return The formatted string
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
     * @param message Message to send
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
            if (prefixedMessage.contains(ChatColor.COLOR_CHAR + "")) {
                final String fixedMessage = MiniMessageSerializer.serialize(TextComponent.fromLegacyText(prefixedMessage));
                TextAdapter.sendComponent(recipient, MiniMessageParser.parseFormat(fixedMessage));
            } else {
                TextAdapter.sendComponent(recipient, MiniMessageParser.parseFormat(prefixedMessage));
            }
        } else {
            recipient.sendMessage(prefixedMessage);
        }
    }

}
