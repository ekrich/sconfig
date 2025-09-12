# Developer / Contributor Information

The following has tips and tricks for contributors to this repo.

## Cross Project and other `sbt` tips

 Cross project is used for `sconfig` with the platforms JVM, Scala.js, and Scala Native. the project also builds for different Scala versions.

If you are already an sbt expert, some of this will be pretty basic, but if you aren't or forget between times you work on the project then this could be helpful. Typically, always start sbt first as follows at you command line:

```sh
% sbt
```

Compile or test for the default Scala version or the one selected by running at the `sbt` prompt `++3.3.6` as an example:
```sh
sbt> compile
sbt> test
```
Test for all versions - good to do prior to pushing your code:
```sh
sbt> +compile
sbt> +test
```
To run JVM test applications, you can list or run them interactively them as follows:

```sh
sbt> sconfigJVM / Test / test / run

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
sbt> sconfigJVM / Test / test / runMain RenderExample

% sbt "project sconfigJVM; test:runMain RenderExample;"
```

There is also the `sbt` alias to run examples:
```sh
sbt> run-examples
```

## Binary Compatibility

Keeping compatibility is very important as we don't want to break peoples code so we have a deprecation cycle before removal of any public APIs. We can also add to the API in a compatible way. To check compatibility use the MIMA plugin with the following command:

```sh
sbt> sconfigJVM / mimaReportBinaryIssues
```
For other MIMA commands type the above up through `mima` and hit tab to see other options.

## Formatting

This project uses `scalafmt` with a similar configuration similar to Scala Native. All code should be formatted prior to merging as CI checks. Everyone may not like the format so sometimes you can wrap lines to make things look better and `scalafmt` will leave it as such. Either way you just run `scalafmt` and forget about it. One less thing to worry about.

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

[Back to README](../README.md)
