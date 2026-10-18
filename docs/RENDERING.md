# Renderer Invariants

<kbd>[<- Back to README](../README.md)</kbd>

For named fields rendered as HOCON, output parses back and is a fixed point: rendering it
again with the same options gives the same text. A standalone unresolved delayed merge has no
key to express its repeated fields and renders a description instead; see
`ConfigDefaultRenderingTest.unresolvedMergeRenderedWithoutAKeyIsDescribed`.
Rendering never resolves, so `${...}` stays verbatim; unresolved output is not necessarily valid
JSON even with `setJson(true)`. Hiding environment values intentionally replaces their contents.
`ConfigFormatOptions` and `setSimplifyNestedObjects` exist only in sconfig.

## Decisions

- One space after `:`, spaces on either side of `=`, matching the output of Java properties.
  These are deliberate; do not "fix" them or add an option to change them.
- `render` was designed for debugging and made round-trip-safe here; a new rendering feature
  needs a demonstrated real-world use case and an example future users can copy.

<kbd>[<- Back to README](../README.md)</kbd>
