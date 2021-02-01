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

import co.aikar.locales.MessageKey;
import co.aikar.locales.MessageKeyProvider;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.Objects;

/**
 * Configurable messages
 */
public final class Message implements MessageKeyProvider {

    private final MessageKey messageKey;
    private final String key;
    private final String defaultValue;

    /**
     * Construct a new message
     *
     * @param key          The message key, used as a path in the configuration file
     * @param defaultValue The default message
     */
    public Message(
            final @NonNull String key,
            final @NonNull String defaultValue
    ) {
        this.key = Objects.requireNonNull(key);
        this.defaultValue = Objects.requireNonNull(defaultValue);
        this.messageKey = MessageKey.of(key);
    }

    /**
     * Get the configuration key
     *
     * @return Configuration key
     */
    public @NonNull String getKey() {
        return this.key;
    }

    /**
     * Get the default message
     *
     * @return Default message
     */
    public @NonNull String getDefaultValue() {
        return this.defaultValue;
    }

    @Override
    public @NonNull String toString() {
        return Messages.getConfigured(this);
    }

    public @NonNull String withoutColorCodes() {
        return this.toString().replaceAll("&[A-Za-z0-9]", "");
    }

    @Override
    public @NonNull MessageKey getMessageKey() {
        return this.messageKey;
    }

}
