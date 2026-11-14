# Contributing

<kbd>[<- Back to README](../README.md)</kbd>

Thank you for your interest in sconfig! This guide explains how the project
works and what we look for in a contribution. Please read it before opening a pull request.

## Very important notice about Scala.js and Scala Native

Supporting Scala.js and Scala Native took years of careful work, in sconfig
and upstream in the Scala.js and Scala Native javalibs. The `java.io` and
`java.net` classes and the `Platform*` classes under `sconfig/js` and
`sconfig/native` let the shared code link and run on those platforms. These are adaptors and stubs and not supported in upstream and should not be in this project at all as they can conflict with real third party libraries so they should not be changed.

For example, `include` is not supported on these platforms (see
[Includes](SCALA_NATIVE.md#includes)). HOCON.md says:

> Implementations need not support files, Java resources, or URLs; and they
> need not support particular URL protocols.

## Philosophy

`sconfig` is a Scala port of [lightbend/config](https://github.com/lightbend/config).
Its goal is to behave like the original, so people moving between the two
libraries meet few surprises. As long as lightbend/config is maintained at a reasonable cadence, `sconfig` normally follows it: fixes and features land there first and are then ported here. `sconfig` can disagree with upstream or fix
things on its own when there is a good reason, but that is the exception.
Keeping the code close to the original also keeps it easy to maintain and easy
to compare.

`sconfig` was ported to Scala so `scalafmt` could run on Scala Native.
[scalafmt](https://scalameta.org/scalafmt/) reads its settings from HOCON with
config, and the port lets it be compiled into a native application. The
[original proposal](https://github.com/lightbend/config/pull/600) shows how the
port was done: one class at a time, with every test passing after every commit.
Scala.js support followed and then Scala 3.

The one deliberate addition is [HOCON formatting](NEW_FEATURES.md#hocon-formatting)
through `ConfigDocument`.

## Scala and Java code used in the implementation

The API and implementation are lightbend/config's Java code ported to Scala,
so they use the Java library. The main code (`src/main`) avoids the Scala
standard library (`Option`, `Vector`, tuples, Scala collections) and uses Java
types (`java.util.*`, `null`, and `Optional` in the API). The helpers in
`org.ekrich.config.impl.ScalaOps` allow some functional code over Java
collections, the same approach as the Scala.js and Scala Native javalib. The
test code (`src/test`) is in Scala, and may use Scala collections and other
Scala library code. Upstream's tests are also in Scala, so they port with few
changes.

## Pull request requirements

For a pull request to be considered, it has to:

1. Be small and focused on one change.
2. Come with tests. Each commit should build and pass all tests.
3. Pass `scalafmt` and the MiMa binary compatibility check (see below).
4. Explain what the change does and why, briefly and to the point.
5. Please make an issue for any item that may need discussion.

If these are not met, the pull request may be closed without review.

## Use of AI tools

AI tools are welcome, and we follow the
[Scala 3 LLM policy](https://github.com/scala/scala3/blob/main/LLM_POLICY.md)
in spirit. You are responsible for every line you submit, and you should be
able to explain it and answer review questions yourself. Test it locally
before submitting, keep the change small, and say in the PR whether AI tools
were used and how you checked the result. Edit generated text until it is
short and to the point, as long summaries and excessive detail are noise for
reviewers.

The following has tips and tricks for contributors to this repo.

## Cross Project and other `sbt` tips

Cross project is used for `sconfig` with the platforms JVM, Scala.js, and Scala Native. The project also builds for different Scala versions.

If you are already an sbt expert, some of this will be pretty basic, but if you aren't or forget between times you work on the project then this could be helpful. Typically, always start sbt first as follows at your command line:

```sh
% sbt
```

Compile or test for the default Scala version or the one selected by running at the `sbt` prompt `++3.9.0` as an example:
```sh
sbt> compile
sbt> test
```
Test for all versions - good to do prior to pushing your code:
```sh
sbt> +compile
sbt> +test
```
Note: In sbt 2, `test` only runs tests affected by your changes. Use `testFull` or `+testFull` to run every test, which is a good idea before pushing.

To run JVM test applications, you can list or run them interactively as follows:

```sh
sbt> sconfigJVM / Test / run

Multiple main classes detected. Select one to run:
 [1] CatchExceptionOnMissing
 [2] FileLoad
 [3] GetExistingPath
 [4] GetSeveralExistingPaths
 [5] HasPathOnMissing
 [6] RenderExample
 [7] RenderOptions
 [8] Resolve

Enter number: 
```
Note: You can add arguments to the end of the command if needed or the individual applications in the next section.

To run them individually from `sbt` prompt or the command line respectively:

```sh
sbt> sconfigJVM / Test / runMain RenderExample

% sbt "sconfigJVM / Test / runMain RenderExample"
```

There is also the `sbt` alias to run examples:
```sh
sbt> run-examples
```

## Binary Compatibility

Keeping compatibility is very important as we don't want to break people's code so we have a deprecation cycle before removal of any public APIs. We can also add to the API in a compatible way. To check compatibility use the MIMA plugin with the following command:

```sh
sbt> sconfigJVM / mimaReportBinaryIssues
```
For other MIMA commands type the above up through `mima` and hit tab to see other options.

## Formatting

This project uses `scalafmt` with a configuration similar to Scala Native. All code should be formatted prior to merging as CI checks. Everyone may not like the format so sometimes you can wrap lines to make things look better and `scalafmt` will leave it as such. Either way you just run `scalafmt` and forget about it. One less thing to worry about.

To install scalafmt please read the [official documentation](https://scalameta.org/scalafmt/docs/installation.html).

Alternately there is a script that can be run from the root of the project in your shell:

```sh
scripts/scalafmt
```

You can also use scalafmt within the sbt shell by running

```sbt
scalafmtAll
```

If you use VSCode you can format individual files for macOS via `shift-option F` or on Windows via `shift-alt F`

<kbd>[<- Back to README](../README.md)</kbd>
