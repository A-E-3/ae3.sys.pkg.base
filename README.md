# ae3.sys.pkg.base

The shared base package: the utilities `ae3.api` and `ae3.sdk` stand on, and the home of the `ae3.util` JavaScript module folder. `CLAUDE.md:3` calls it "Base package providing shared AE3 utilities, including the Settings caching mechanism used across the system". It holds 41 Java files and 3979 lines across 11 packages, and 40 CommonJS modules.

Its own descriptor is `/Volumes/workspace/myx/ae3.sys.pkg.base/ae3-packages/ae3.base/package.json`, reading "AE3 Base Package". `/Volumes/workspace/myx/ae3.sys.pkg.base/project.inf` requires `ae3.api` and `ae3.sdk`, and provides `ae3.base`.

## Why it is distinct from its siblings

Three things live here and nowhere else in the place.

- **The `ae3.util` modules.** All 21 `.js` files of `/Volumes/workspace/myx/ae3.sys.pkg.base/ae3-packages/ae3.base/resources/lib/ae3.util/` are here. None of the place's other 87 projects with a `project.inf` ships a folder of that name. Of the place's 7896 `.js` files, 148 call `require("ae3.util/…")`, and the heaviest two are `Settings` and `CollectConsole` at 49 files each.
- **The `ru.myx.ae3.util.*` Java packages.** `util/fn/` holds the single-file parse-and-cache suppliers, and `util/concurrent/` the lazy holder they build on. `util/settings/` and `util/queues/` hold the builders over them.
- **The engine-side helpers.** `/Volumes/workspace/myx/ae3.sys.pkg.base/java/ru/myx/ae3/report/EventIdentifier.java` (482 lines) generates the event keys, and `internal/` exposes host figures, crypto names and class API to scripts.

Nothing outside this project imports those Java packages in practice: 11 files carry an `import ru.myx.ae3.util.`, and 9 of them are this project's own.

## Its two siblings depend on it rather than sitting beside it

- `/Volumes/workspace/myx/ae3.sys.pkg.base.stats/` holds the process and VM counters, 12 Java files and 18 modules, and names `ae3.base` at `project.inf:5`.
- `/Volumes/workspace/myx/ae3.sys.pkg.base.vfs/` holds no Java at all, its `java/` and `bin/` empty, and ships the `ae3.vfs` and `ae3.vfs-docs` modules. It names `ae3.base` in `ae3-packages/ae3.vfs/package.json:5`, not in its `project.inf`.

Both share the name prefix and neither is a peer: each consumes what this project provides.

## How it reaches a running server

`/Volumes/workspace/myx/ae3-devel-tools/MAKE-AE3-DISTRO.xml:43` stages its `bin/` into the shared class tree. One of the axiom's 51 jars carries its classes, and it is `/Volumes/workspace/myx/acm-cvs/sys-current/axiom/ru.myx.ae3sys-5.0.jar`: 33 class entries under `ru/myx/ae3/util/` and 24 under `ru/myx/ae3/report/`.

Six `project.inf` files name `ae3.base` in `Requires:`: `ae3.sys.pkg.base.stats:5`, `ae3.sys.pkg.i3.net:6`, `ae3.sys.pkg.i3.web:6`, `ae3.sys.pkg.l2.tgt.xml:5`, `ae3.system:11` and `ae3.test:5`.

## One compile dependency its `project.inf` does not name

`project.inf` declares two requires, and `/Volumes/workspace/myx/ae3.sys.pkg.base/.classpath` names three source projects. The third is `/clean-java.crypto` at line 11, and `/Volumes/workspace/myx/ae3.sys.pkg.base/java/ru/myx/ae3/internal/crypto/CryptoApiHelper.java:13-15` is the only file that needs it, importing two `ru.myx.crypto` classes and `ru.myx.util.Base58`. Read the `.classpath`, not the `project.inf`, before moving that file or rebuilding the project outside Eclipse.
