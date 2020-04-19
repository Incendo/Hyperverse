# Contribute

## NMS and CB References

Utility classes (such as GeneratorUtil) can either
make use of reflection, or added to the version specific
submodules.

If the code is likely to change between versions,
it should be added to a version specific module.

Code that rarely/never changes can use reflection directly,
but does not necessarily have to.

## World Interaction

Load chunks asynchronously using PaperLib whenever possible.

## Dependency Injection

Hypeverse uses Guice for dependency injection. 
Make sure to follow common DI principles.

## Style

This project uses the PlotSquared code style.
A profile for this can be found [here](https://github.com/IntellectualSites/PlotSquared/blob/v5/code_style.xml).

Other than that, try to keep annotations on the same line
as method and type declarations when possible. Mark
all non-mutable fields as final, and make liberal
use of @Nullable and @NotNull annotations.

## Licensing

The plugin is licensed under GPLv3. Each source file
should have a license header, and the template for this
can be found in the HEADER file.

By contributing to the project you guarantee that your
code follows the GPLv3 License requirements.
