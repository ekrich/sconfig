# AGENTS.md

Guidance for coding agents working in this repository. Build tips for people are in
[docs/DEVELOPER.md](docs/DEVELOPER.md).

`sconfig` is a Scala port of [`lightbend/config`](https://github.com/lightbend/config) (HOCON)
for the JVM, Scala.js and Scala Native, on Scala 2.12, 2.13 and 3. Features that
`lightbend/config` lacks are in [docs/NEW_FEATURES.md](docs/NEW_FEATURES.md). Porting its
changes is covered in [docs/PORTING.md](docs/PORTING.md).

## Commands

```bash
sbt '++2.13.18; sconfigJVM/testOnly org.ekrich.config.impl.SomeTest'
sbt '++2.13.18; sconfigJVM/testFull'       # whole JVM suite; also ++2.12.21 and ++3.9.0
sbt '++2.13.18; sconfigJS/testFull'        # needs node on PATH
sbt '++2.13.18; sconfigNative/testFull'    # slow
sbt '++2.13.18; scalafmtCheckAll'
sbt '++2.13.18; sconfigJVM/mimaReportBinaryIssues'
```

sbt 2 caches test results and keeps `++` between commands: use `testFull` or `testOnly`, never
trust zero executed tests, and start every command with its `++` version. Cache workarounds and
MiMa comparison: [docs/TESTING.md](docs/TESTING.md#running-the-build).

## Code style

- Ported code keeps the Java's shape; sconfig-only code is idiomatic Scala in its own methods.
- `main` code avoids the Scala library (collections, `Option`, `Try`): use `ScalaOps`, `null` or,
  in the public API, `java.util.Optional`. Tests may use it freely.
- Touch only what the change needs.

Which code counts as ported, and the remaining rules: [docs/CODE_STYLE.md](docs/CODE_STYLE.md).

## Tests

Shared tests go in `sconfig/shared/src/test`, in the suite that owns the behaviour:
[docs/TESTING.md](docs/TESTING.md#where-tests-go).

## Pull requests

- Tests come before the implementation, in their own commits.
- A PR carries only what it delivers. Small PRs whose conflicts would be non-obvious may be
  grouped, with a note saying why: [docs/PULL_REQUESTS.md](docs/PULL_REQUESTS.md).
- A PR to a repository the agent's owner does not control, such as upstream `lightbend/config`, is
  opened as a draft. The owner marks it ready.
- The description opens with a short summary and its behaviour change, then the full
  description with a realistic config before and after:
  [docs/PULL_REQUESTS.md](docs/PULL_REQUESTS.md).

## When needed

- Deciding whether something is a bug: [docs/DEFECTS.md](docs/DEFECTS.md)
- Changing the renderer: [docs/RENDERING.md](docs/RENDERING.md)
- Porting from `lightbend/config`: [docs/PORTING.md](docs/PORTING.md)
- Reviewing a PR, and where suggested improvements go: [docs/REVIEWS.md](docs/REVIEWS.md)
- Stacking dependent PRs, and the fallback of one combined PR: `.agents/skills/stack-prs/SKILL.md`
