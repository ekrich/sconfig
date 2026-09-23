# Porting changes from lightbend/config

<kbd>[<- Back to README](../README.md)</kbd>

sconfig began as a translation of `lightbend/config` from Java, and fixes made there are still
ported here. [#29](https://github.com/ekrich/sconfig/issues/29) lists the ones not yet ported.

A port is faithful to the original change. The Scala keeps the Java's names, order and control
flow, so the next port still diffs line for line against the Java. Two kinds of difference are
allowed, and the PR names both:

- A **deviation** does the original's work in a different way, because Scala, sconfig's own code
  or a platform forces it.
- An **addition** does work the original does not do. It stays in the port only when the port
  needs it to work in sconfig, in its own commits after the port. Anything else is a separate PR.

## Scope

These rules cover ported code: any class, method or block with a counterpart in
`lightbend/config`, including one that sconfig has changed since. When a port touches such code,
its lines keep the Java's shape.

Code that exists only in sconfig, such as `ConfigFormatOptions`, the rendering paths that read it
and the platform sources, is not bound by the Java's shape. It follows the Scala style in
[AGENTS.md](../AGENTS.md#code-style). Where sconfig-only logic sits inside a ported class, keep
it in its own methods, so the ported methods still diff against the Java. The restriction on the
Scala library applies to both kinds of code.

Some older ported code already has Scala shapes, such as the `@tailrec` recursion in `Tokenizer`,
`BadMap` and `ConfigDelayedMergeObject`. Leave it as it is unless a port changes those lines.

## Steps

1. **Check it is not done yet**: #29, the open PRs, and `git log --grep "#N"`.
2. **Find the final form.** Never port a PR from its original diff alone. It may have been
   corrected by a follow-up PR, reverted, partly superseded or changed by later commits;
   lightbend/config#839, for example, was corrected by lightbend/config#846. Port the final
   effective behaviour, not the history.
3. **Port the tests first**, in their own commits, into the suite that owns the behaviour (see
   [AGENTS.md](../AGENTS.md#tests)). Skip a test that is already covered here and name the test
   that covers it. For a bug fix, each test must fail on the pre-fix revision and pass once the
   implementation is ported. If one does not fail, investigate before continuing.
4. **Translate the implementation line for line** with the tables below. Note each deviation in
   a porting log as you make it; notes written afterwards miss some.
5. **Map the diff.** Walk the original diff hunk by hunk and find each hunk's counterpart. A hunk
   left unported, or a change with no counterpart in the original, is a deviation or an
   addition.
6. **Verify**: JVM on 2.13, 2.12 and 3; Scala.js; Scala Native unless the code is JVM-only;
   `scalafmtCheckAll`; MiMa against `main`; and the scala-library check below. Name the Scala
   version in each sbt command (`++2.13.18; ...`), because sbt 2 keeps the last one. Check any
   behaviour the PR describes against the `lightbend/config` jar:

   ```bash
   find ~/.cache/coursier -name "config-1.4.*.jar"
   javac -cp <jar> -d /tmp Probe.java && java -cp <jar>:/tmp Probe
   ```

### More tests than the original

The original's tests are a floor, not a ceiling. Add tests for cases it did not foresee, for
regressions around the code the port touches, and for differences between platforms. They go in
their own commit after the ported tests, still before the implementation, and the PR lists them
apart from the original's. If one still fails after the port, it has found a bug the original
shares: note it and fix it in a separate PR rather than committing it ignored.

## Java to Scala

The Scala keeps the Java's method names, and Java collections stay Java collections in
signatures and inside methods, with no conversion to Scala collections. The tables show the rest.

### Loops

| Java | sconfig | Watch out |
|---|---|---|
| `for (T x : xs)` whose body assigns no outer local | `xs.forEach { x => … }` | |
| `for (T x : xs)` whose body assigns an outer local (`i++`, `changed = …`) | `val it = xs.iterator(); while (it.hasNext()) { val x = it.next(); … }` | A lambda that assigns an outer `var` boxes it in `scala.runtime.ObjectRef`, `IntRef` or `BooleanRef`, a heap allocation on every call. Java cannot write that shape, so the original had a loop there. Some existing code does this, such as `SimpleConfigList.modifyMayThrow`; new ports should not add more. |
| `return` inside a loop | the `while` form above, with a plain `return` | Never `return` inside a lambda: it becomes an exception-based non-local return, deprecated in Scala 3. |
| a loop that only searches: `for (x : xs) if (p(x)) return f(x); return d;` | `xs.scalaOps.findFold(p)(() => d)(f)`, or `exists`, `forall`, `indexWhere` | Only when the Java loop does nothing but search. Otherwise a `while` keeps the original's lines. |
| `break` | `var continue = true` with `while (cond && continue) { … continue = false }` | Here `continue` means "keep looping", the opposite of Java's `continue`. Setting it does not leave the iteration: the rest of the body still runs unless it sits in an `else`. |
| Java's `continue` | the rest of the body in an `if`/`else` | In an index loop the increment must still run on the skipped path. |
| `for (int i = 0; i < n; i++)` | `var i = 0; while (i < n) { …; i += 1 }` | `0 until n` goes through Predef's `Range`. The `until` in `ScalaOps` stays commented out until the library compiles without Predef. |
| labelled `break outer` | one flag that both loops check | |

### Values and types

| Java | sconfig | Watch out |
|---|---|---|
| `a == b` on objects | `a eq b`; `!=` becomes `ne` | Scala's `==` on objects calls `equals`. |
| `a.equals(b)` | `a == b`, which is null-safe | On boxed numbers Scala's `==` is cooperative: `Integer(1) == Long(1)` is `true` in Scala, while `equals` is `false` in Java. Keep `.equals` where the operands can be numbers of different boxed types. |
| `x instanceof T` and a cast | `isInstanceOf[T]` and `asInstanceOf[T]` | |
| `switch` | `match` | |
| `null` | `null` | No `Option`. |
| `StringBuilder` | `java.lang.StringBuilder` | Never Scala's `StringBuilder`. |
| package-private or `protected` | `private[impl]` | Java's `protected` includes the package; Scala's does not. |
| `static` | the companion `object` | |
| `enum` | a class in `scala-2/` and an `enum` in `scala-3/` | Both, as with `OriginType`. |
| `T... args` | `args: T*`, with `@varargs` when Java calls it | Pulls in `scala.collection.immutable.Seq`. |
| `throws Exception` | `@throws[Exception]` | As on `modifyMayThrow`. |
| an anonymous class with one method | a lambda | SAM conversion works on Scala 2.12 too. |

## What a port pulls in from scala-library

The compiled library already uses a few dozen `scala.*` classes. Most are generated by the
compiler (`ScalaSignature`, `Product`, enum and object machinery, `BoxesRunTime`), so sconfig
cannot drop scala-library. What a port controls is collections, `Option`, `StringOps`,
`ArrayOps` and the boxes for captured variables. Compare the build before and after the port:

```bash
jdeps -verbose:class -e 'scala\..*' target/out/jvm/scala-<version>/sconfig/classes \
  | awk '/->/ {print $3}' | sort | uniq -c | sort -rn
```

A new `ObjectRef`, `IntRef`, `BooleanRef`, `Option` or `scala.collection` entry is a deviation:
remove it, or name it in the PR with the reason.

## The pull request

Title: `Port lightbend/config#N: <what it fixes>`, with `#N+#M` for several PRs.

The body keeps these sections in this order:

1. **Source**: "Ports lightbend/config#N (merged as `abc1234`), which fixes lightbend/config#M."
   Name any follow-up PR folded in, and the item of #29 it closes.
2. **One sentence** saying what changes for a user.
3. **Example**: a realistic config, the call that uses it, and sconfig's output before and after
   the port, taken from running it.
4. **Deviations from the original**: a table of where, original, here and why. With none, write
   "None: translated line for line from `X.java`."
5. **Tests**: the commit that adds them and which of them fail there; each original test left
   unported, with the test here that already covers it; the tests added beyond the original's,
   each with the case it covers.
6. **Beyond the port**: each addition, why the port needs it, and its commits. With none, write
   "Nothing beyond the port."
7. **Behaviour change** a user could notice, and the platforms and Scala versions the tests ran
   on.

<kbd>[<- Back to README](../README.md)</kbd>
