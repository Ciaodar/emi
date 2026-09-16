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

Fix it by generating the class list and rebuilding the jar once:

```
JAVA_HOME=/path/to/jdk-25 ./gradlew :xplat:generateClassLists :xplat:jar
```

This is only a dev-environment concern; shipped jars contain `GlobalMixin` already.

## Minecraft 26.3 API notes

Several systems EMI relied on became data driven or moved to SDL:

- Keyboard/mouse input uses SDL. `org.lwjgl.glfw` is gone; use
  `com.mojang.blaze3d.platform.InputConstants.KEY_*` / `MOD_*` and the single argument
  `InputConstants.isKeyDown(int)`. `KeyEvent.scancode()` is now `KeyEvent.keycode()` and
  `InputConstants.Type.SCANCODE` no longer exists.
- Tool block transformations (stripping, tilling, flattening) are defined by the
  `BlockTransformer` registry instead of maps on `AxeItem` / `ShovelItem` / `HoeItem`,
  which no longer exist.
- Brewing, fuels and composting are data driven through the `brewing` recipe type and the
  `COOKING_FUEL` / `COMPOSTABLE` data components.
- Clients no longer receive a `RecipeMap`; Fabric provides `SynchronizedRecipes` and
  NeoForge patches in `RecipeMap.createClient`. EMI therefore stores a flat
  `Collection<RecipeHolder<?>>` and filters by type itself.
- Rendering moved `GpuTexture` / `GpuBuffer` / `GpuBufferSlice` / `CommandEncoder` /
  `RenderPass` / `RenderPipeline` / `GpuFormat` into `com.mojang.renderpearl.api.*`, and
  item quads are grouped in `ItemQuads`.
