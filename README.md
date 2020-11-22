# bugs.core

FIXME: my new application.

## Installation

Download from https://github.com/bugs/bugs.core.

## Usage

FIXME: explanation

### Clojure Application

Run the application directly:

    $ clj -m bugs.core

Run the application's tests (they'll fail until you edit them):

    $ clj -A:test

Build an uberjar:

    $ clj -A:uberjar

Run that uberjar:

    $ java -cp target/bugs.core-0.1.jar clojure.main -m bugs.core

### ClojureScript Application

Run the application:

    $ clj -A:fig:fig-build

> Auto-testing is enabled in `figwheel-main.edn` so once the application is running the test results can be viewed by opening [http://localhost:9500/figwheel-extra-main/auto-testing](http://localhost:9500/figwheel-extra-main/auto-testing).

Run the application's tests (they'll fail until you edit them):

    $ clj -A:fig:fig-test

Build a minified version of the application:

    $ clj -A:fig:fig-min

## Development Tools

If the project was generated with the `+dev-tools` attribute the following dependencies will be included in `deps.edn`.

#### [`cljfmt-runner`](https://github.com/JamesLaverack/cljfmt-runner)
Allows [`cljfmt`](https://github.com/weavejester/cljfmt) to be used in [`tools.deps`](https://clojure.org/reference/deps_and_cli) based Clojure projects.

Check for formatting issues:
```shell script
$ clj -A:fmt-check
```

Fix formatting issues:
```shell script
$ clj -A:fmt-check:fmt-fix
```

#### [`kibit`](https://github.com/jonase/kibit)
A static code analyser for Clojure(Script).

Analyse the code:
```shell script
$ clj -A:kibit
```
---

In addition to these dependencies, a `.pre-commit-config.yaml` file will be included [which uses pre-commit](https://pre-commit.com/) to run the following checks when committing changes to the project repository.

* [`end-of-file-fixer`](https://github.com/pre-commit/pre-commit-hooks#end-of-file-fixer): makes sure files end in a newline and only a newline.
* [`trailing-whitespace`](https://github.com/pre-commit/pre-commit-hooks#trailing-whitespace): trims trailing whitespace.
* [`clj-kondo`](https://github.com/borkdude/clj-kondo): lints Clojure(Script) code. **Please note**, `pre-commit-config.yaml` assumes a [pre-built binary of `clj-kondo`](https://github.com/borkdude/clj-kondo/blob/master/doc/install.md#installation) is available on your path.
* [`cljfmt`](https://github.com/weavejester/cljfmt): formats Clojure(Script) code.
* [`kibit`](https://github.com/jonase/kibit): analyses Clojure(Script) code for patterns that could be rewritten more idiomatically.

> As `cljfmt` and `kibit` are not run via pre-built binaries their execution really slows down the commit process which is far from ideal. Locating or creating pre-built binaries for these tools is on the TODO list.

Using `pre-commit` is entirely optional but it's very helpful when working in a team. To get up and running with `pre-commit`:
1. [Install the program](https://pre-commit.com/#install).
2. Set up the git hook scripts in the project repository:
    ```shell script
    $ cd hello.core
    $ pre-commit install
    ```

## Options

FIXME: listing of options this app accepts.

## Examples

...

### Bugs

...

### Any Other Sections
### That You Think
### Might be Useful

## License

Copyright © 2020 eightytwo

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.
