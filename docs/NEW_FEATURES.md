# New Features

<kbd>[<- Back to README](../README.md)</kbd>

This library has features that are not available in the original Lightbend
`config` library. This document explains and gives examples on how to use
these features.

## Scala Native and Scala.js Support

See the Scala Native and Scala.js [guide](SCALA_NATIVE.md) for features and
limitations of those platforms.

## HOCON Formatting

`ConfigDocument.format` (since 2.2.0) formats HOCON or JSON text. It works on
the text of one file, so it keeps everything you wrote: includes, comments,
substitutions such as `${?HOME}`, `+=`, duplicate keys, and the order of
fields. It never loads includes, resolves substitutions, or reads environment
variables, so it works the same on the JVM, Scala.js, and Scala Native. That
makes it usable from a command line tool or an editor extension.

```scala
import org.ekrich.config.ConfigFormatOptions
import org.ekrich.config.parser.ConfigDocumentFactory

val text =
  """include "base.conf"
    |app{
    |name="demo"   # the app name
    |ports=[80,443]
    |}""".stripMargin

val formatted = ConfigDocumentFactory
  .parseString(text)
  .format(ConfigFormatOptions.defaults)
  .render
```

The result is:

```hocon
include "base.conf"
app {
  name = "demo" # the app name
  ports = [80, 443]
}
```

`format` returns a new `ConfigDocument` and does not change the original.
Call it last: text added afterwards with `withValueText` or `withValue` is not
formatted.

### Options

| Option | Default | Effect |
|---|---|---|
| `doubleIndent` | `false` | Indent with 4 spaces instead of 2. |
| `colonAssign` | `false` | Write `key: value` instead of `key = value`. `+=` and `key { ... }` are not changed. |
| `simplifyNestedObjects` | `false` | Collapse objects with a single field into a path expression: `a { b { c = 1 } }` becomes `a.b.c = 1`. Not applied when there is a comment, an include, or `+=` in the nested objects. |

```scala
val options = ConfigFormatOptions.defaults
  .setDoubleIndent(true)
  .setSimplifyNestedObjects(true)
```

### What the formatter does

Everything else is fixed, so formatted files look the same everywhere:

- Indents by nesting depth, including comment and include lines.
- Writes `key = value`, or `key: value` with `colonAssign`, so a `:` or `=`
  in the input is changed to match. Writes `key {` before an object.
- Keeps your line breaks between fields and array elements, but collapses
  several blank lines into one and removes blank lines at the start of the
  file and at the start and end of an object or array.
- Joins a field that is split across lines, such as `a =` with the value
  on the next line.
- Keeps a field with a comment between its key and value exactly as
  written, so the comment stays with the value it describes. Only its first
  line is indented; its separator, its other lines, and any object or array
  value are not changed.
- Keeps objects and arrays that are on one line on one line, written as
  `{ a = 1, b = 2 }` and `[80, 443]`. An object or array that spans lines
  gets its opening and closing brackets on their own lines. Empty ones are
  written `{}` and `[]`.
- Removes commas at the end of a line, where HOCON does not need them.
  Commas between values on the same line are kept.
- Writes one space before a comment at the end of a line.
- Removes trailing whitespace and ends the file with exactly one newline.

It never changes the text of comments, values, strings (including
triple-quoted strings), substitutions, concatenations, keys, or include
statements.

For JSON documents, parse with `ConfigSyntax.JSON`. Only indentation and
spacing change, so the result is still JSON: the separator stays `:`, commas
are kept, and `simplifyNestedObjects` is ignored.

```scala
import org.ekrich.config.{ConfigParseOptions, ConfigSyntax}

val json = ConfigDocumentFactory
  .parseString(
    """{"a":1,"b":{"c":[1,2]}}""",
    ConfigParseOptions.defaults.setSyntax(ConfigSyntax.JSON)
  )
  .format(ConfigFormatOptions.defaults)
  .render
// { "a": 1, "b": { "c": [1, 2] } }
```

<kbd>[<- Back to README](../README.md)</kbd>
