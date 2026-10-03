# Renderer Invariants

<kbd>[<- Back to README](../README.md)</kbd>

For named fields rendered as HOCON, output parses back and is a fixed point: rendering it
again with the same options gives the same text. A standalone unresolved delayed merge has no
key to express its repeated fields and renders a description instead; see
`ConfigDefaultRenderingTest.unresolvedMergeRenderedWithoutAKeyIsDescribed`.
Rendering never resolves, so `${...}` stays verbatim; unresolved output is not necessarily valid
JSON even with `setJson(true)`. Hiding environment values intentionally replaces their contents.
`ConfigFormatOptions` and `setSimplifyNestedObjects` exist only in sconfig.

<kbd>[<- Back to README](../README.md)</kbd>
