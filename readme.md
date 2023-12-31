<img src="logo.png" width="30%" alt="Polylith" id="logo">

The Polylith documentation can be found here:

- The [high-level documentation](https://polylith.gitbook.io/polylith)
- The [Polylith Tool documentation](https://polylith.gitbook.io/polylith/poly)
- The [RealWorld example app documentation](https://github.com/furkan3ayraktar/clojure-polylith-realworld-example-app)

You can also get in touch with the Polylith Team on [Slack](https://clojurians.slack.com/archives/C013B7MQHJQ).

<h1>poly-platform</h1>

requires java 11
requires clojure 1.10.3.943
requires polylinth

```bash
brew install openjdk@11
brew install polyfy/polylith/poly
```

Tested with:

```
openjdk 11

openjdk 19.0.1 2022-10-18
OpenJDK Runtime Environment (build 19.0.1+10-21)
OpenJDK 64-Bit Server VM (build 19.0.1+10-21, mixed mode, sharing)

openjdk 21.0.1 2023-10-17
OpenJDK Runtime Environment Homebrew (build 21.0.1)
OpenJDK 64-Bit Server VM Homebrew (build 21.0.1, mixed mode, sharing)
```

<p>Add your workspace documentation here...</p>


### Building

<p>Build apps using Clojure, refer build.clj deps.edn and workspace.end. Also made available via Makefile</p>

```bash
clojure -T:build uberjar :project rest-api
```

or

```bash
make build-rest-api
```