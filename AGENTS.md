# AGENTS.md

Guidance for coding agents working in this repository. Build tips for people are in
[docs/DEVELOPER.md](docs/DEVELOPER.md).

`sconfig` is a Scala port of [`lightbend/config`](https://github.com/lightbend/config) (HOCON)
for the JVM, Scala.js and Scala Native, on Scala 2.12, 2.13 and 3. Features that
`lightbend/config` lacks are in [docs/NEW_FEATURES.md](docs/NEW_FEATURES.md). Porting its
changes is covered in [docs/PORTING.md](docs/PORTING.md).

## Commands

```bash
sbt "sconfigJVM/testOnly org.ekrich.config.impl.SomeTest"
sbt 'sconfigJVM/testOnly org.ekrich.config.impl.* org.ekrich.config.* junit.*'  # whole JVM suite, 2.13
sbt -batch ++3.9.0 'sconfigJVM/testOnly org.ekrich.config.impl.* org.ekrich.config.* junit.*'  # also ++2.12.21
sbt 'sconfigJS/testOnly org.ekrich.config.impl.* org.ekrich.config.* junit.*'      # needs node on PATH
sbt 'sconfigNative/testOnly org.ekrich.config.impl.* org.ekrich.config.* junit.*'  # slow
sbt scalafmtAll                          # CI checks formatting
sbt sconfigJVM/mimaReportBinaryIssues
```

- CI runs `sbt +test +doc`. Run Scala 3 and 2.12 whenever `scala-2/` or `scala-3/` sources change.
- sbt 2 caches test results, so a repeated `test` can report `Total 0`. `testOnly` forces a real
  run.
- sbt 2 keeps a server running between commands, and `++` sticks to it: after
  `sbt -batch ++2.12.21 ...`, every later command still runs on 2.12. Start each command with
  the version you mean, such as `++2.13.18;`.
- MiMa can already report problems on `main`. Only an increase over `main` is yours.

## Judging a defect

- A bug that `lightbend/config` shares is still a bug here. Say in the PR that it is shared.
- The specification is `HOCON.md` on `lightbend/config`'s `main` branch. `docs/original/HOCON.md`
  is a 2018 snapshot.
- Settle behaviour by running the `lightbend/config` jar, not by reasoning about it. The coursier
  cache usually holds one: `find ~/.cache/coursier -name "config-1.4.*.jar"`.
- [#29](https://github.com/ekrich/sconfig/issues/29) lists the `lightbend/config` PRs not yet
  ported. A gap may already be known.

## Code style

Which rules apply depends on where the code comes from:

- **Ported code** has a counterpart in `lightbend/config`: most of `impl` and the public API.
  It keeps the Java's shape, so later ports still diff line for line. See
  [docs/PORTING.md](docs/PORTING.md#scope).
- **sconfig-only code** covers what `lightbend/config` lacks: `ConfigFormatOptions` and the
  rendering paths that read it, and the platform sources in `js/`, `jvm/`, `native/` and
  `jvm-native/`. It is idiomatic Scala: `val` over `var`, expressions over statements, pattern
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
- Methods without side effects drop `()`. Names say what a value is, in the present tense; never
  `tmp`.
- An enum case added in `scala-2/` also goes in `scala-3/`.
- A public API change is checked with MiMa and named in the PR.
- Behaviour that diverges from `lightbend/config` is either a bug fix argued from the
  specification or a feature documented in `docs/NEW_FEATURES.md`.

## Tests

Shared tests live in `sconfig/shared/src/test`. `sconfig/jvm/src/test` holds only what needs the
JVM: environment variables, files, system properties. Extend the suite that owns the behaviour
instead of adding a new one:

- `ConfigFormatOptionsTest`: features of `ConfigFormatOptions`, which only sconfig has
- `ConfigDefaultRenderingTest`: rendering with `ConfigFormatOptions.defaults`
- `ConfigSubstitutionSharedTest` and `ConfigSubstitutionTest`: `${...}` resolution
- `ConcatenationTest`, `ConfigDocumentFactorySharedTest`, `ConfParserTest`: as named

Assert the expected string with `checkEqualsAndStable` from `RenderingTestSuite`.
`checkReparses` alone lets through a regression that still parses. Tests carry almost no
comments.

## Renderer invariants

`render` output parses back and is a fixed point: rendering it again gives the same text.
Rendering never resolves, so `${...}` stays verbatim. `ConfigFormatOptions` and
`setSimplifyNestedObjects` exist only in sconfig.

## Pull requests

- Tests come before the implementation, in their own commits.
- A PR carries only what it delivers: no investigation probes and no `@Ignore`d tests.
- The description shows a realistic config and its output before and after the change, taken
  from running it. When a fix could be read as new behaviour, quote the specification.
