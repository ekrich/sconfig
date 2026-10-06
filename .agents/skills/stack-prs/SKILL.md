---
name: stack-prs
description: Stack dependent draft PRs on an upstream repository, keep the stack consistent when one of them changes, and fall back to one combined PR when the stack gets hard to maintain.
---

# Stack PRs

## When to use

Use this skill when:

- two or more PRs change the same code and merging them in any order would conflict
- a fix to one PR also has to reach another
- you are asked to update, rebase or review a PR that is part of a stack
- the stack is hard to maintain and you need the fallback

Independent PRs stay independent. Stack only when separating them costs non-obvious conflicts.

## Build the stack

1. Decide the order by running, not by reading: merge each pair in a scratch clone, then run the
   full suite. Pairs that merge cleanly and pass can be in any order; put the PR that others
   conflict with last.
2. Make the chain linear: each branch starts from the head of the one below and carries its own
   test commit and fix commit on top. Use `git cherry-pick` of the PR's own commits, so SHAs of
   the lower PRs stay the same.
3. Resolve conflicts by combining both intents, not by taking one side. Run the full suite at
   every head, then compare the top of the stack against the stack without the top PR on random
   inputs and one wide input for time.
4. Every PR targets the default branch, so its diff shows the lower PRs until they merge. Say so.
5. Push with `--force-with-lease=<branch>:<old sha>` only for branches you just restacked. Use the
   `gh auth git-credential` helper if plain `git push` has no credentials.

## Make it visible

Put one line at the top of every description, above the summary the PR rules call for, and one
comment on every PR:

- the merge order, and this PR's position
- its own commits, its head, the test count on the stack up to it
- the rules below
- the backup plan

## Update a stack

- Never rebase or force-push one branch alone. A change in a lower PR means re-stacking every PR
  above it.
- A fix to a lower PR is cherry-picked onto the PRs above it, or the stack is rebuilt.
- When a lower PR merges, rebase the next one; the shared commits drop out.
- A commit that is red only because of a lower PR's interaction is allowed in the middle of a
  stack when its message and the PR comment say so.

## Backup plan: one combined PR

If a lower PR changes materially, review wants another order, or maintaining the stack costs more
than it saves:

1. Open one draft from the head of the top PR.
2. Keep each fix as its own test commit and fix commit, so it can be split again.
3. Describe the grouped fixes and the conflict the grouping avoids, as in
   [docs/PULL_REQUESTS.md](../../../docs/PULL_REQUESTS.md).
4. Close the old PRs with a comment linking the combined one. Do this only with the owner's
   approval.

## Report

Say which PRs were force-pushed, with old and new SHAs, the suite result at each head, and what
the comparison on random inputs showed.
