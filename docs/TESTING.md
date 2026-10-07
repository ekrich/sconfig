# Building and Testing

<kbd>[<- Back to README](../README.md)</kbd>

## Running the build

The commands are in [AGENTS.md](../AGENTS.md#commands).

- Check `.github/workflows/ci.yml` for the current CI commands. Run Scala 3 and 2.12
  whenever `scala-2/` or `scala-3/` sources change.
- sbt 2 caches test results, so a repeated `test` can report `Total 0`. Use `testFull` for
  every suite or `testOnly` for selected suites; zero executed tests is not verification.
- sbt 2 also restores compiled test classes from its cache (`~/.cache/sbt/v2`). A probe test you
  deleted can come back, and Scala.js then fails at link time with `Referring to non-existent
  class`. Give that run a fresh cache:
  `sbt ';set Global / localCacheDirectory := file("/tmp/sconfig-sbtcache-<unique-run-id>") ;++2.13.18 ;sconfigJS/testOnly ...'`.
- sbt 2 keeps a server running between commands, and `++` sticks to it: after
  `sbt -batch ++2.12.21 ...`, every later command still runs on 2.12. Start each command with
  the version you mean, such as `++2.13.18;`.
- Compare MiMa findings with `main` using the same Scala version and baseline artifacts.
  Compare the individual problem signatures, not just the count: one new break can replace
  one existing finding without changing the total.

## Where tests go

New tests go in the shared source set by default, so one suite runs on the JVM, Scala.js and
Scala Native; `parseConfig` and the `TestUtilsShared` helpers work on all three. Move a test to
a platform source set only when it depends on something that platform owns or lacks:
environment variables, files and system properties stay on the JVM, and a case that fails
differently on one platform (an unimplemented shim raising `NotImplementedError`, for example)
splits out of the shared suite. Run the shared suite on Scala.js before relying on it; a
test that passes only on the JVM is a JVM test misplaced.

Shared tests live in `sconfig/shared/src/test`. `sconfig/jvm/src/test` holds only what needs the
JVM: environment variables, files, system properties. Extend the suite that owns the behaviour
instead of adding a new one:

- `ConfigFormatOptionsTest`: features of `ConfigFormatOptions`, which only sconfig has
- `ConfigDefaultRenderingTest`: rendering with `ConfigFormatOptions.defaults`
- `ConfigSubstitutionSharedTest` and `ConfigSubstitutionTest`: `${...}` resolution
- `ConcatenationTest`, `ConfigDocumentFactorySharedTest`, `ConfParserTest`: as named

For rendering tests, assert the expected string with `checkEqualsAndStable` from
`RenderingTestSuite`. A test that only checks that the output parses lets through a regression
that still parses. Tests carry almost no comments, the exception being a test for behaviour
lightbend/config does not have: it notes that, what it pins and where the fix is, so it can be
revisited when upstream gains the coverage.

<kbd>[<- Back to README](../README.md)</kbd>
