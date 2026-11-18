# Judging a Defect

<kbd>[<- Back to README](../README.md)</kbd>

- A bug that `lightbend/config` shares is still a bug here. Say in the PR that it is shared.
- The specification is `HOCON.md` on `lightbend/config`'s `main` branch. `docs/original/HOCON.md`
  is a 2018 snapshot.
- Verify behaviour with a named `lightbend/config` version or commit and record it. The coursier
  cache may hold released jars: `find ~/.cache/coursier -name "config-1.4.*.jar"`. A released jar
  cannot verify a change that has not been released; build the relevant upstream revision then.
  A shared bug does not override the specification. A gap in the specification is a candidate
  for a spec PR or a ticket on `lightbend/config`, not a local interpretation.
- [#29](https://github.com/ekrich/sconfig/issues/29) lists the `lightbend/config` PRs not yet
  ported. A gap may already be known.

<kbd>[<- Back to README](../README.md)</kbd>
