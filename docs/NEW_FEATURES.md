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

### Keeping detached comments

A comment block separated from the next object field or array element by a blank line is *detached*. `lightbend/config` attaches a comment block to the following object field or array element only when no blank line intervenes; a blank line drops the whole block, so a config file's licence header, which convention separates from the first setting, disappears from `render()`. sconfig keeps a detached block when the caller asks for it with `ConfigParseOptions.setKeepDetachedComments(true)`:

```scala
val options = ConfigParseOptions.defaults.setKeepDetachedComments(true)
val config = ConfigFactory.parseString(
  """# Copyright 2025 Example
    |# Licensed under Apache-2.0
    |
    |a = 1
    |""".stripMargin,
  options
)
config.root.render()
```

The render then keeps the two comment lines above `a = 1`. The option is off by default: parsing, `ConfigOrigin.comments` and rendering are then unchanged from sconfig's baseline. This option does not affect `ConfigDocument`.

The scope with the option on:

- Comment origins change. `ConfigOrigin.comments` gains the kept blocks, and a render with comments on prints them. The resolved values do not change in the tested cases.
- Order is kept within a block. Across duplicate fields or elements the merge policy decides: `# new` above `a = 2` replaces `# old`, while `a { x = 1 }` and `a { y = 2 }` merge into one object rendered with `# new` then `# old`.
- A header outside a braced root, as in `# header` then `{a=1}`, is kept in the root object's origin but is not rendered.
- A block before `include` attaches to the next local field, not to the included file.
- A comment above a `+=` field is printed twice, above the delayed assignment and above its element. This is an existing defect of `+=` rendering that preservation exposes, not new parser behaviour; it is pinned in `ConfigDefaultRenderingTest.aPlusEqualsCommentIsPrintedTwiceWithTheOption`.
- Blank lines themselves are still not preserved, and a block with no following field or element is dropped, as in `lightbend/config`.
- With `ConfigFormatOptions.setSimplifyNestedObjects(true)`, an object that carries a kept comment is no longer compressed to a dotted path, even when comments are not rendered.

<kbd>[<- Back to README](../README.md)</kbd>
