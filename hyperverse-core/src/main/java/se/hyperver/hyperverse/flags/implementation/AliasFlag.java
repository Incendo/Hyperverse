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

package se.hyperver.hyperverse.flags.implementation;

import org.checkerframework.checker.nullness.qual.NonNull;
import se.hyperver.hyperverse.configuration.Messages;
import se.hyperver.hyperverse.flags.WorldFlag;

public final class AliasFlag extends WorldFlag<String, AliasFlag> {

    public static final AliasFlag ALIAS_NONE = new AliasFlag("");

    private AliasFlag(final @NonNull String alias) {
        super(alias, Messages.flagDescriptionAlias);
    }

    @Override
    public AliasFlag parse(final @NonNull String input) {
        return this.flagOf(input.replaceAll("&[A-Za-z0-9]", ""));
    }

    @Override
    public AliasFlag merge(final @NonNull String newValue) {
        return this.flagOf(newValue);
    }

    @Override
    public String toString() {
        return this.getValue();
    }

    @Override
    public String getExample() {
        return "&cFancy World Name";
    }

    @Override
    protected AliasFlag flagOf(final @NonNull String value) {
        return new AliasFlag(value);
    }

}
