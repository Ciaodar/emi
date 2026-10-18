# AGENTS.md

Notes for agents and contributors working in this repository.

## Building and running

The build requires **JDK 25** (`options.release = 25`). Point `JAVA_HOME` at a JDK 25
installation, otherwise `compileJava` fails with `error: release version 25 not supported`:

```
JAVA_HOME=/path/to/jdk-25 ./gradlew :fabric:build :neoforge:build
JAVA_HOME=/path/to/jdk-25 ./gradlew :fabric:runClient
JAVA_HOME=/path/to/jdk-25 ./gradlew :neoforge:runClient
```

## `GlobalMixin` and NeoForge's dev class loader

`dev/emi/emi/mixin/GlobalMixin` is not a source file. The root `build.gradle` synthesises
the class with a hand written class file inside the `generateClassLists` task, listing every
non-mixin class as a mixin target. The Gradle setup is deliberate — **do not "fix" it by
editing the Gradle scripts.**

The generated directory is an *extra* source set output. Loom only puts a source set's
classes and resources directories on the dev classpath, so:

- on Fabric the generated class is reachable because the whole runtime classpath is used;
- on NeoForge the mod is loaded by a module class loader that only sees the declared mod
  files, so the generated class has to reach it through the `:xplat` dev jar.

If the `:xplat` dev jar was built before `generateClassLists` ever ran, NeoForge crashes
during bootstrap with:

```
java.lang.NoClassDefFoundError: dev/emi/emi/mixin/GlobalMixin
  ... ModLoadingException: EMI (emi) has class loading errors
```

Fix it by generating the class list once:

```
JAVA_HOME=/path/to/jdk-25 ./gradlew :xplat:generateClassLists
```

This is only a dev-environment concern; shipped jars contain `GlobalMixin` already.
