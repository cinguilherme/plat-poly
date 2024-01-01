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
openjdk 19.0.1 2022-10-18
OpenJDK Runtime Environment (build 19.0.1+10-21)
OpenJDK 64-Bit Server VM (build 19.0.1+10-21, mixed mode, sharing)
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

<p> ChatGPT Context </p>

```text
I'm giving you context of the codebase I'm working on.

This codebase is designed to build various types of applications using Clojure, including REST APIs, event consumers, command-line interfaces (CLI), and HTTP servers. The build process is managed through a combination of Clojure's built-in tooling and a Makefile.

The build.clj, deps.edn, and workspace.edn files are key to the Clojure build process. The build.clj file contains scripts for building the project, while deps.edn manages the project's dependencies. The workspace.edn file is typically used to configure the development environment.

The build process can be initiated in two ways. The first is by running the command clojure -T:build uberjar :project rest-api, which uses Clojure's built-in tooling to create an uberjar (a standalone JAR file that contains the project and all its dependencies) for the rest-api project. This is just one example; the :project parameter can be replaced with the name of any other project in the codebase.

The second way to build the project is by running the command make build-rest-api, which uses the Makefile. The Makefile likely contains a rule for build-rest-api that automates some tasks related to building the rest-api project. Again, build-rest-api is just one example; there are likely other rules for building other types of applications.

The codebase is structured into components, base, and projects. The components are reusable pieces of code that can be used across different projects. The base contains the core functionality that all projects rely on. The projects are the actual applications that are built using the components and base.

The codebase also uses the Poly tool from PolyLith for building and testing. PolyLith is a development environment for Clojure that encourages modularity, simplicity, and testability.

```

## Bases List

```text
cli (example of cli app using dependencies in the same project)
pedestal-server (example of pedestal server running with many components)
```

## Component List

```text
common
envs
files
client_http
pedestal
sqs_consumer
sqs_producer
dynamodb
logger
redis
kafka_producer
kafka_consumer
```

## Dependencies list

```edn
{
    org.clojure/clojure {:mvn/version "1.11.1"} ;; clojure
    org.clojure/core.async {:mvn/version "1.6.681"} ;; core.async
    poly/pedestal {:local/root "../../components/pedestal"} ;; pedestal
    ;; leaf components
    poly/common {:local/root "../../components/common"}
    poly/envs {:local/root "../../components/envs"}
    poly/client-http {:local/root "../../components/client_http"}
    poly/dynamodb {:local/root "../../components/dynamodb"}
    poly/elastic-search {:local/root "../../components/elastic_search"}
    poly/postgres {:local/root "../../components/postgres"}
    poly/mongo {:local/root "../../components/mongo"}
    poly/redis {:local/root "../../components/redis"}
    ;; components
    com.stuartsierra/component {:mvn/version "1.1.0"}
    ;;environment
    environ/environ {:mvn/version "1.2.0"}
    cprop/cprop {:mvn/version "0.1.19"}
    lynxeyes/dotenv  {:mvn/version "1.1.0"}
    ;; pedestal
    io.pedestal/pedestal.service {:mvn/version "0.6.1"}
    io.pedestal/pedestal.route {:mvn/version "0.6.1"}
    io.pedestal/pedestal.jetty {:mvn/version "0.6.1"}
}
```
