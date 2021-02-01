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

public final class AdvancementFlag extends BooleanFlag<AdvancementFlag> {

    public static final AdvancementFlag ADVANCEMENTS_ALLOWED = new AdvancementFlag(true);
    public static final AdvancementFlag ADVANCEMENTS_FORBIDDEN = new AdvancementFlag(false);

    private AdvancementFlag(final boolean value) {
        super(value, Messages.flagDescriptionAdvancements);
    }

    @Override
    protected AdvancementFlag flagOf(final @NonNull Boolean value) {
        return value ? ADVANCEMENTS_ALLOWED : ADVANCEMENTS_FORBIDDEN;
    }

}
