# Reviewing a Pull Request

<kbd>[<- Back to README](../README.md)</kbd>

- Settle each claim by running something: the PR's tests at its test commit and at its head, and
  `lightbend/config` at a named revision or the baseline branch. Quote the output. Conclusions from
  reading alone have been wrong.
- When the change is in shared code (parser, resolver, renderer), also run the same inputs through
  the baseline and the branch, and time one wide input. A change can pass its own tests and still
  change other results or turn linear work into quadratic work.
- Say which assumption the reviewer shares with the author, such as the same test suite or the
  baseline as the oracle. Two agents agreeing is not validation.
- Fix what is obvious on the PR itself: a stale sentence in the description, a regression with a
  clear fix. Add the test in its own commit before the fix, push on top of the branch without
  force, and report it. Do not split or rewrite the author's commits.
- Report what is the owner's decision: design, scope, merge order, and anything that changes
  behaviour beyond what the PR claims.
- End the review with a **Suggested improvements** section, in two parts:
  - *Applied*: obvious improvements made where the review happened, such as a wrong command in
    `AGENTS.md` or `docs/`, with the commit.
  - *For the owner*: practices that would have caught the problem earlier, rules worth adding to
    `AGENTS.md` or to the agents' project memory, and tooling such as a benchmark or a
    baseline comparison in CI. Propose them; the owner decides.
- After a fix, offer a self-review pass that tries to falsify it.

<kbd>[<- Back to README](../README.md)</kbd>
