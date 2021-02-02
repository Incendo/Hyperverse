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

package se.hyperver.hyperverse.flags;

import se.hyperver.hyperverse.util.MessageUtil;

public class FlagParseException extends Exception {

    private static final long serialVersionUID = 3442434713845764748L;

    private final WorldFlag<?, ?> flag;
    private final String value;
    private final String errorMessage;

    /**
     * Construct a new flag parse exception to indicate that an attempt to parse a world
     * flag was unsuccessful.
     *
     * @param flag         Flag instance
     * @param value        Value that failed ot parse
     * @param errorMessage An error message explaining the failure
     * @param args         Arguments used to format the error message
     */
    public FlagParseException(
            final WorldFlag<?, ?> flag, final String value,
            final String errorMessage, final String... args
    ) {
        super(String.format("Failed to parse flag of type '%s'. Value '%s' was not accepted.",
                flag.getName(), value
        ));
        this.flag = flag;
        this.value = value;
        this.errorMessage = MessageUtil.format(errorMessage, args);
    }

    /**
     * Returns the value that caused the parse exception
     *
     * @return Value that failed to parse
     */
    public String getValue() {
        return this.value;
    }

    /**
     * Returns the class that threw the exception
     *
     * @return Flag that threw the exception
     */
    public WorldFlag<?, ?> getFlag() {
        return this.flag;
    }

    /**
     * Get the error message that was supplied by the flag instance.
     *
     * @return Error message.
     */
    public String getErrorMessage() {
        return this.errorMessage;
    }

}
