# Pull Requests

<kbd>[<- Back to README](../README.md)</kbd>

- Tests come before the implementation, in their own commits.
- A PR carries only what it delivers: no investigation probes and no `@Ignore`d tests.
- The description opens with a summary a reviewer can stop after, then `---` and
  `## Full description`. The summary is two or three sentences in user terms: what went wrong,
  with one concrete input, and what happens now. When a user who upgrades could notice more than
  the fix, such as a config that used to load and now throws, it ends with
  `**Behaviour change:**` and one sentence.
- A reference to an upstream `lightbend/config` pull request or issue links exactly once, in the
  first sentences of the summary. Later mentions of upstream items are in backticks so GitHub
  does not add cross-references to the upstream timeline, and a title never mentions lightbend.
- The full description shows a realistic config and its output before and after the change, taken
  from running it. When a fix could be read as new behaviour, quote the specification.
- Small PRs may be grouped into one larger PR when keeping them apart means resolving a
  non-obvious conflict whenever one merges before the other, for example two fixes in the same
  resolver loop. Each fix stays its own test commit and fix commit, so the group can still be
  split. The description names the grouped fixes and the conflict the grouping avoids. Unrelated
  fixes are not grouped for convenience.

<kbd>[<- Back to README](../README.md)</kbd>
