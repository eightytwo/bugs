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

## License

Copyright © 2020 eightytwo

This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.

Additional permission under GNU AGPL version 3 section 7:

If you modify this Program, or any covered work, by linking or combining it with Clojure (or a modified version of that library), containing parts covered under the same terms as Clojure (currently, the Eclipse Public License version 1.0), the licensors of this Program grant you additional permission to convey the resulting work. Corresponding Source for a non-source form of such a combination shall include the source code for the parts of Clojure used as well as that of the covered work.

This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
