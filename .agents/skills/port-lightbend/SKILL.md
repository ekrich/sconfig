---
name: port-lightbend
description: Port a lightbend/config PR or commit into sconfig, check whether one is already ported, or list the changes not yet ported.
---

# Port a lightbend/config change

## When to use

Use this skill when:

- porting a lightbend/config PR
- porting a lightbend/config commit
- checking whether a lightbend/config change is already ported
- identifying the changes not yet ported

## Important

Never port an upstream PR from its original diff alone. The PR may have been:

- corrected by a follow-up PR,
- reverted,
- partially superseded,
- changed by later commits.

Port the final effective upstream behaviour. When it depends on code sconfig does not have,
stop and report instead of guessing.

## Workflow

[docs/PORTING.md](../../../docs/PORTING.md) holds the rules, the Java to Scala tables and the PR
template.

1. Identify the source PR or commit.
2. Check sconfig issue #29.
3. Check existing and open PRs, and `git log --grep "#N"`.
4. Inspect the final upstream state.
5. Port the tests first, in their own commits. Add tests beyond the original's where they help;
   see "More tests than the original" in docs/PORTING.md.
6. Run the tests and record the failures, quoting the messages. For a bug fix, each test must
   fail on the pre-fix revision and pass after the implementation is ported. If one does not
   fail, investigate before continuing.
7. Port the implementation line for line, using the Java to Scala tables.
8. Compare every upstream diff hunk with its counterpart here.
9. Record each deviation and addition in the porting log.
10. Run the required matrix: JVM on Scala 2.13, 2.12 and 3; Scala.js; Scala Native unless the
    code is JVM-only; `scalafmtCheckAll`; MiMa against `main`; the scala-library check. Start
    each sbt command with its Scala version (`++2.13.18; ...`): sbt 2 keeps the last `++`.
11. Prepare the PR description from the template. Produce the example's before and after output
    by running it on `main` and on the branch with a throwaway test, then delete that test.

## Porting log

Keep a temporary porting log while working. It may live outside the repository or in `/tmp`.
Do not commit it unless the PR requires it.

Record each deviation and addition as you make it: where it is, what the original does, what the
port does, and why. The PR description is built from this log.

## Expected result

When finished, report:

- the upstream PR or commit ported
- the final upstream commit identified
- the tests added
- the tests that failed before the implementation
- the tests passing after the implementation
- the deviations from upstream
- the additions beyond upstream
- the platforms and Scala versions verified
- the MiMa result
- the remaining limitations
