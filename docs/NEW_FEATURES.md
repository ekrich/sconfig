# New Features

<kbd>[<- Back to README](../README.md)</kbd>

This file is the specification of how sconfig differs from `lightbend/config`: every behaviour
that exists here and not upstream gets a section saying what it does and why it exists. A
feature without a section here is a divergence waiting to be "fixed" by the next port. For
features and limitations of Scala Native and Scala.js see the
[guide](SCALA_NATIVE.md).

## `ConfigFormatOptions`

Renders configuration with formatting choices the Java library does not offer: indentation
width, key-value separator (`=` or `:`), brace location, and simplification of nested objects
to dotted paths. sconfig-only; the renderer reads it through
[RENDERING.md](RENDERING.md#the-fixed-point).

<kbd>[<- Back to README](../README.md)</kbd>
