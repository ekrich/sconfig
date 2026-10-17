# New Features

<kbd>[<- Back to README](../README.md)</kbd>

## Scala Native and Scala.js Support

This library has features that are not available in the original Lightbend `config` library. This document explains and gives examples on how to use these features. See the Scala Native and Scala.js [guide](SCALA_NATIVE.md) for features and limitations of those platforms.

## HOCON Formatting

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
