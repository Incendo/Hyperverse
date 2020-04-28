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

package se.hyperver.hyperverse.commands;

import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandPermission;
import co.aikar.commands.annotation.Default;
import co.aikar.commands.annotation.Description;
import co.aikar.commands.annotation.Optional;
import co.aikar.commands.annotation.Subcommand;
import co.aikar.commands.annotation.Syntax;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeMirror;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

@SupportedSourceVersion(SourceVersion.RELEASE_8)
@SupportedAnnotationTypes("co.aikar.commands.annotation.*")
public class CommandAnnotationProcessor extends AbstractProcessor {

    private final Map<TypeMirror, Command> commandMap = new HashMap<>();
    private final Gson gson = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();

    @Override public boolean process(final Set<? extends TypeElement> annotations,
        final RoundEnvironment roundEnv) {

        final Path commandOutput = Paths.get("./", "commands");
        if (!Files.exists(commandOutput)) {
            try {
                Files.createDirectory(commandOutput);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        JsonObject captions = new JsonObject();
        try (final BufferedReader reader = Files.newBufferedReader(commandOutput.resolve("captions.json"));
             final JsonReader jsonReader = gson.newJsonReader(reader)) {
            captions = gson.fromJson(jsonReader, JsonObject.class);
        } catch (final Exception e) {
            e.printStackTrace();
        }

        final Set<? extends Element> commandAliases =
            roundEnv.getElementsAnnotatedWith(CommandAlias.class);
        for (final Element commandAlias : commandAliases) {
            if (commandAlias.getKind() == ElementKind.CLASS) {
                final CommandAlias alias = commandAlias.getAnnotation(CommandAlias.class);
                final CommandPermission permission =
                    commandAlias.getAnnotation(CommandPermission.class);
                System.out.printf("Found base command: '%s' in type '%s'\n", alias.value(),
                    commandAlias.asType());
                this.commandMap.put(commandAlias.asType(),
                    new Command(alias.value().split("\\|"), permission.value()));
            }
        }

        final Set<? extends Element> subCommands =
            roundEnv.getElementsAnnotatedWith(Subcommand.class);
        for (final Element commandAlias : subCommands) {
            final Command owningCommand =
                commandMap.get(commandAlias.getEnclosingElement().asType());
            if (owningCommand == null) {
                System.err.printf("Could not find owning command for type %s\n",
                    commandAlias.getEnclosingElement().asType());
                continue;
            }
            final List<String> names = new ArrayList<>();
            final CommandAlias alias = commandAlias.getAnnotation(CommandAlias.class);
            if (alias != null) {
                names.addAll(Arrays.asList(alias.value().split("\\|")));
            }
            final Subcommand subCommand = commandAlias.getAnnotation(Subcommand.class);
            if (subCommand != null) {
                final String name = owningCommand.names[0];
                for (final String subName : subCommand.value().split("\\|")) {
                    names.add(name + " " + subName);
                }
            }

            final CommandPermission commandPermission =
                commandAlias.getAnnotation(CommandPermission.class);
            final Description description = commandAlias.getAnnotation(Description.class);
            final Syntax syntaxA = commandAlias.getAnnotation(Syntax.class);
            final String syntax;
            if (syntaxA != null) {
                syntax = syntaxA.value();
            } else {
                final StringBuilder syntaxBuilder = new StringBuilder();

                final VariableElement[] parameters =
                    ((ExecutableElement) commandAlias).getParameters()
                        .toArray(new VariableElement[0]);
                for (int i = 1; i < parameters.length; i++) {
                    final VariableElement parameter = parameters[i];

                    boolean optional = parameter.getAnnotation(Optional.class) != null
                        || parameter.getAnnotation(Default.class) != null;

                    if (optional) {
                        syntaxBuilder.append("[");
                    } else {
                        syntaxBuilder.append("<");
                    }

                    syntaxBuilder.append(parameter.getSimpleName());

                    if (optional) {
                        final Default defaultValue = parameter.getAnnotation(Default.class);
                        if (defaultValue != null) {
                            syntaxBuilder.append("=").append(defaultValue.value());
                        }

                        syntaxBuilder.append("]");
                    } else {
                        syntaxBuilder.append(">");
                    }

                    if ((i + 1) < parameters.length) {
                        syntaxBuilder.append(" ");
                    }
                }

                syntax = syntaxBuilder.toString();
            }

            owningCommand.subCommands.add(new SubCommand(names.toArray(new String[0]),
                commandPermission == null ? "" : commandPermission.value(),
                description == null ? "" : replace(description.value(), captions), replace(syntax, captions)));
        }

        System.out.printf("Writing commands to %s\n", commandOutput.toString());
        for (final Map.Entry<TypeMirror, Command> commandEntry : this.commandMap.entrySet()) {
            final Path commandFile =
                commandOutput.resolve(commandEntry.getKey().toString() + ".json");
            System.out
                .printf("Writing base command in class %s to file %s\n", commandEntry.getKey(),
                    commandFile);
            try (final BufferedWriter bufferedWriter = Files.newBufferedWriter(commandFile);
                final JsonWriter jsonWriter = this.gson.newJsonWriter(bufferedWriter)) {
                gson.toJson(commandEntry.getValue(), Command.class, jsonWriter);
            } catch (final IOException e) {
                e.printStackTrace();
            }
        }

        return true;
    }

    private String replace(String source, final JsonObject captions) {
        for (final String caption : captions.keySet()) {
            source = source.replace("{@@" + caption + "}", captions.get(caption).getAsString());
        }
        return source;
    }

    private final class Command {

        private final String command;
        private final String[] names;
        private final String permission;
        private final List<SubCommand> subCommands = new LinkedList<>();

        private Command(final String[] names, final String permission) {
            this.names = Objects.requireNonNull(names);
            this.command = names[0];
            this.permission = Objects.requireNonNull(permission);
        }

    }


    private final class SubCommand {

        private final String subCommand;
        private final String[] names;
        private final String permission;
        private final String description;
        private final String syntax;
        private final String fullSyntax;

        private SubCommand(final String[] names, final String permission, final String description,
            final String syntax) {
            this.names = names;
            this.subCommand = names[0];
            this.permission = permission;
            this.description = description;
            this.syntax = syntax;
            this.fullSyntax = "/" + names[0] + (syntax.isEmpty() ? "" : " ") + syntax;
        }

    }

}
