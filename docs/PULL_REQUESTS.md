# Pull Requests

<kbd>[<- Back to README](../README.md)</kbd>

- Tests come before the implementation, in their own commits.
- A PR carries only what it delivers: no investigation probes and no `@Ignore`d tests.
- The description opens with a summary a reviewer can stop after, then `---` and
  `## Full description`. The summary is two or three sentences in user terms: what went wrong,
  with one concrete input, and what happens now. When a user who upgrades could notice more than
  the fix, such as a config that used to load and now throws, it ends with
  `**Behaviour change:**` and one sentence.
- The full description shows a realistic config and its output before and after the change, taken
  from running it. When a fix could be read as new behaviour, quote the specification.

<kbd>[<- Back to README](../README.md)</kbd>
