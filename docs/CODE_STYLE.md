# Code Style

<kbd>[<- Back to README](../README.md)</kbd>

Which rules apply depends on where the code comes from:

- **Ported code** has a counterpart in `lightbend/config`: most of `impl` and the public API.
  It keeps the Java's shape, so later ports still diff line for line. See
  [PORTING.md](PORTING.md#scope).
- **sconfig-only code** covers what `lightbend/config` lacks: `ConfigFormatOptions` and the
  rendering paths that read it, and platform adaptations without a Java counterpart in
  `js/`, `jvm/`, `native/` and `jvm-native/`. A platform directory can also contain ported code,
  such as `ConfigBeanImpl` on the JVM; classify by the Java counterpart, not the directory.
  sconfig-only code is idiomatic Scala: `val` over `var`, expressions over statements, pattern
  matching, and `@tailrec` recursion over a `while` with a flag. Keep it in its own methods, so
  ported methods stay comparable with the Java.
- **All `main` code** avoids the Scala library: Java collections in the API and inside methods,
  no Scala collections, `Option` or `Try`, and no Java↔Scala conversions. For collection work use
  `ScalaOps` on Java collections (`exists`, `forall`, `foldLeft`, `findFold`); where Scala would
  use `Option`, use `null` or, in the public API, `java.util.Optional`. Scala language features
  are fine. Tests may use the Scala library freely.

Everywhere:

- Touch only what the change needs: no drive-by reformatting, import regrouping or renames.
  Imports stay one package per line, in alphabetical order.
- New Scala-only methods without side effects drop `()`. Preserve the calling convention of
  Java overrides and existing public methods; changing it can break Scala source compatibility
  even when MiMa passes. Names say what a value is, in the present tense; never `tmp`.
- An enum case added in `scala-2/` also goes in `scala-3/`.
- Avoid extending the public API unless there is no choice; what only internal code needs
  stays `private`.
- A `case class` in the public API is a binary-compatibility hazard: adding a field or a
  default parameter breaks it and costs a deprecation cycle. Model public data holders as a
  plain class with a companion, the way `ConfigRenderOptions` does.
- A public API change is checked with MiMa and named in the PR.
- Build-tooling and lint-setup changes (`scalafmt`/`scalafix` rules) are the maintainer's
  decisions; keep them out of PRs.
- Behaviour that diverges from `lightbend/config` is either a bug fix argued from the
  specification or a feature documented in `docs/NEW_FEATURES.md`.

<kbd>[<- Back to README](../README.md)</kbd>
