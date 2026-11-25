# MAGIC.md — ae3.sys.pkg.base

## For keeper-ae3

### A branch tip level with its upstream does not prove a clean tree

A reset to `origin/main` orphans the commits it drops. Those commits survive as objects, but
`git log --all` cannot reach them, because `--all` walks refs and never the reflog.

Measured in this checkout on 2026-11-04:

- `cdcfc42` (`* auth: malformed basic header answers the sign-in challenge`, 2026-10-31) passes
  `git cat-file -e cdcfc42^{commit}`, appears **0** times in `git log --all --oneline`, and is named by
  `git reflog --all` at `refs/heads/main@{1}`.
- `git status -sb` reads `## main...origin/main` with one unrelated file modified. The level tip is not
  evidence that nothing was thrown away.
- The dropped change is a guard around `AuthUtils.squeezeCredentials` in
  `ae3-packages/ae3.base/resources/lib/ae3.util/Auth.js`, and `HEAD` does not carry it.

The same shape sits in `ae3.sys.pkg.i3.web`, whose reflog names `8d1ddee` as the reset before
`12099f7`.

### Recovering an orphaned commit

1. `git reflog --all` names it, and `git cat-file -e <sha>^{commit}` proves which repo still holds it.
   Sweeping `git -C <repo> cat-file -e <sha>^{commit}` over every repo in the place finds the one holder.
2. Extract the blob to a sandbox, check `git hash-object` equals `git rev-parse <sha>:<path>`, then copy
   it over the working-tree file.
3. Copying leaves the change unstaged, so the owner reviews it with `git diff --stat`.
   `git checkout <sha> -- <path>` would stage it, and the review would then sit in `git diff --cached`.

A fix held only as a working-tree edit, or only as an orphaned commit, has no durable home. Say which of
the two it is before calling the work done.

### `EventIdentifier`: two key-ordering defects, the fix, and the check that holds it

`java/ru/myx/ae3/report/EventIdentifier.java` is 482 lines in the working tree against 473 at `HEAD`.
The difference is this fix, uncommitted.

- **The live counter was a `short`.** `lastSequence` was `private static short` (line 85 at `HEAD`)
  against `MAX_SEQUENCE` 46000 (line 74). A `short` holds `0` to `32767`, so the increment past `32767`
  wraps to `-32768` and the constructor throws `IllegalArgumentException: sequence must be >= 0`.
  Measured against the `HEAD` source compiled in a sandbox, from the class's own declared `-1` and the
  clock parked: 32768 calls returned a key and the 32769th threw.
- **`KEY_LOWEST` sorted after the key it bounds.** Not by width, which an earlier draft of this
  section claimed: both literals are 16 characters, and `getKeyString()` emitted 16 for every key
  measured. The pre-fix literal puts its `z` at index 8, while the encoder pads the date to 9 digits
  and puts `z` at index 9. That single position decides the order.
  - `"00000000z0000-00"` against the real lowest `"000000000z000---"` compares at 74, because index 8
    holds `z` against `0`.
  - What it skips is narrow, and not "every entry whose origin is -1". Measured over
    `dateMillis` 0 to 1000000: 36 keys sort below it, all with a date below `000000010`, so
    `dateMillis` 0 to 35. From 36 ms on the ninth date digit is at least `1`, and every such key
    sorts above the literal. Entries with any other origin produce the same date width, so the skip
    does not depend on `origin` at all.
  - `KEY_LOWEST_2015` is correct: it compares at `-1` against its own real lowest.
  - **The base-36 alphabet is uppercase, and the case is load-bearing for sort order.**
    `DIGITS` is `"0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ"` (`EventIdentifier.java:37`). A shell or
    script reimplementation that pads with lowercase digits produces keys that still look right and
    compare wrongly. Measured on the shipped class against a lowercase derivation of the same date:
    date 35 is `00000000Zz000---`, which sorts below the old literal, while the lowercase
    `00000000zz000---` sorts above it. The skip count moves with it, 36 keys over `dateMillis` 0 to
    1000000 on the real alphabet against 35 on the lowercase one. Reproduce the encoding from the
    `DIGITS` constant, never from a host language's own base-36 renderer.
- **The fix** widens the counter to `int`, moves `KEY_LOWEST` to `"000000000z000---"`, and makes
  `next()` roll the stored millisecond forward by one at `MAX_SEQUENCE` rather than overflow. The roll
  costs one millisecond per 46001 sequences: 138003 keys left the store 2 ms ahead of a parked clock.
  Sustained overload therefore dates keys in the future.
- **The check** is `ae3.test/java/ru/myx/ae3/test/report/TestEventIdentifier.java`. Run against the
  pre-fix class it reported 2 violated, with `KEY_HIGHEST` holding as the control. Run against the
  fixed class it reported 0. Both bounds are compile-time constants, so the check must be recompiled
  with the class it measures, or it compares the previous literal.
- **`bin/` still holds the pre-fix class.** `bin/` is ignored (`.gitignore:1`) and untracked, so a
  rebuild there is invisible to `git status` and has no `git restore` to undo it.
  `ae3-share/ae3-distro/common/ae3-classes/ru/myx/ae3/report/EventIdentifier.class` carries the same
  pre-fix bytes, sha256 `134d8291`, and is what the packaged axiom serves.
- **A hand `javac` is not this project's builder.** `javac --release 22` on the fixed source, against
  the project's real dependency classpath, produced 8120 bytes against the builder's 9245, and its
  `Object baseValue()` bridge came out without the `throws WaitTimeoutException` clause the builder's
  carries. Rebuild this project with its own builder, and rebuild the distro copy with the packaging
  step, not by copying one class across.
- `KEY_LOWEST` has no Java caller. Its only readers are packaged `RecentEventTracker.js` copies, and
  they use `KEY_LOWEST_2015`.
- The `ws-myx.ae3-devel` copy of this file is byte-identical to this checkout's `HEAD`, so it carries
  neither fix.

### `EventIdentifier`'s `origin` field is real, validated, and never set to anything but `-1`

The class name and its `origin` field suggest parallel multi-head timestamping. In this tree there is
one serial counter behind a `synchronized` block, and every caller passes `-1`.

- The field is `public final short origin` at line 295, with its own validation in the constructor:
  below `-1` throws, above `1295` throws (lines 312-318). The bounds are real, so the field is not a
  leftover.
- Both key encoders branch on it: `getKeyString` handles `origin == -1` by writing a single `z`
  group, and any other value by a base-36 group of its own width (lines 387-400). `getKeyString2015`
  does the same in base 16 (lines 437-452).
- The only caller in the tree passes `-1`. `next()` (`EventIdentifier.java:187`) calls
  `next((short) -1)`, and the JS caller,
  `/Volumes/workspace/myx/ae3.sys.pkg.base/ae3-packages/ae3.base/resources/lib/ae3.util/log/RecentEventTracker.js:182`,
  calls `EventIdentifier.next(this.org)`. That file declares `org` with `value : -1, writable : true`
  at line 72 and assigns it nowhere else.
- A rollover resets it too: `nextSequentialIdentifier` (`EventIdentifier.java:469-473`) returns
  `origin` unchanged when the sequence has room, and `(short) -1` when it rolls the millisecond.

So a scan or a key comparison that assumes a populated origin is comparing against a value no writer
in this place produces. Do not take the field's presence for a working mechanism.

### `EventIdentifier`: the sequence is an `int` internally and a `short` at every public edge

The counter is `int` in the working tree. Three places still take or produce a `short` sequence, and all
three now disagree with it.

| Place | Signature | Effect |
|---|---|---|
| `exact` (`EventIdentifier.java:150`) | `exact(long, short, short)` | cannot build a key above 32767; throws `IllegalArgumentException: sequence must be >= 0` |
| `parseKeyString` (`EventIdentifier.java:231`, `:238`, `:245`) | `Short.parseShort(...)` | 2015 keys with a sequence above 32767 throw `NumberFormatException` |
| `keyHighest` (`EventIdentifier.java:158`) | passes `MAX_SEQUENCE` (46000) into the `int` constructor | emits a 2015 key its own parser cannot read |

Measured against the shipped class in `bin/`, and against the fixed source compiled in a sandbox:

- `EventIdentifier.keyHighest(new Date()).getKeyString2015()` gives
  `20261104T194344301Z-04600050F`. `parseKeyString` on that string throws
  `NumberFormatException: Value out of range. Value:"046000" Radix:10`. This holds on the pre-fix class
  too, so it is not the fix's doing, and `keyStart(date, true)` returns the same unparseable key.
- Driving the fixed `next()` through its rollover emits, at counter 32768 and above,
  `20261105T194310204Z-032768---` and up. `parseKeyString` throws the same `NumberFormatException` for
  each. At counter 32767 the same call returns `...032767000`.
- `parseKeyString` catches `ParseException` only (`EventIdentifier.java:250`), so these arrive as a
  thrown `RuntimeException` and never as its documented `null`. A caller that reads a `null` as "this key
  is not an event id" gets an exception instead.
  - `RecentEventTracker.js:416` does exactly that, unguarded, in `materializeEntry`.
  - `RecentEventTracker.js:151` is inside a `try`, and the `catch` at `:162` re-parses the same key, so
    the exception escapes there too.
  - `RecentEventTracker.js:129` makes the same call, inside an `if(false)` block, so it is dead.

What this changes about the fix: before it, `next()` could not produce a sequence above 32767 at all,
because its counter was a `short` and the next increment returned a negative.
- The 2015-format key it wrote was therefore always readable by `parseKeyString`.
- After the fix, the 32769th key inside one millisecond, sequence 32768, is written and unreadable by the
  class's own parser. Its symptom moves from a thrown constructor to a thrown parser, at the same pressure.
- The rollover is what bounds the damage. The counter rolls to the next millisecond at 46000, so one
  millisecond can carry 46001 keys, and 13233 of them, sequences 32768 to 46000, are the unparseable
  range. The register moves on before a second millisecond's worth can pile up.
- Measured end to end, from the class's own declared counter and the clock parked: 40000 calls to the
  fixed `next()` wrote 40000 keys, of which 32768 parsed and 7232 threw `NumberFormatException`. The first
  unparseable one was call 32769, key `...Z-032768---`.

Widening `exact` and the three `parseInt`-style reads to `int`, or clamping `keyHighest` to
`Short.MAX_VALUE`, is a design call this file does not make. It is with `magic-coordinator`.

### A self-link child never reaches a settings map, and a container is dropped before any name test

Measured on JDK 25.0.2 through `SettingsBuilder.setInputFolderPath`, with
`ru.myx.ae3.properties.path.public` set to `/Volumes/workspace/myx/acm-cvs/sys-current`.

- `settings/manual` yields 28 own keys. That is its count of real `.json` files, not its 30 entries,
  the other two being `readme.txt` and the self-link `manual`.
- A scratch tree holding the same 28 files and no self-link yields the same 28 keys.
- Sensitivity control in that scratch tree: one added `.json` file yields 29 keys and its own name, so
  the instrument does report a new descriptor.
- A zero needs its folder read first. `settings/no-such-folder` and `settings/system` both give 0 keys,
  and `system` holds no `.json` at all.
- A container never reaches the name filter. `checkReload` tests `isContainer()` first and sends a
  container down the folder branch, where `runFolderFilter` answers `null` for every entry
  (`/Volumes/workspace/myx/ae3.sys.pkg.base/java/ru/myx/ae3/util/fn/SupplierVfsFolderMapCached.java:47-57`,
  `/Volumes/workspace/myx/ae3.sys.pkg.base/java/ru/myx/ae3/util/settings/SupplierVfsFolderSettingsCached.java:187-191`).
  A directory named `zzdir.json` in that scratch `settings/manual` gives 28 keys and no report line.
  All 49 of the axiom's links are directory links, so each one is dropped here and no name test sees it.
- A malformed `.json` file writes a report line that names no file. One holding a single `{` makes
  `JsonSAPI.parse` report `ASSEMBLY:EXCEPTION` with `Unmatched '{'` and a stack through
  `runDescriptorMapper` at `:124`.
- The catch at `:126` is what keeps that file from costing the folder. The same tree still gives its
  28 keys, and a recompiled copy whose `catch` rethrows fails the whole build with `RuntimeException`.
  That copy gives 28 keys on the clean tree, which is the control.
- Content that is no JSON at all is dropped in silence. A file holding plain text or raw bytes makes
  `JsonSAPI.parse` return `undefined`, so the folder gives 28 keys and no report line is written.
- Two more self-linked settings folders give their real counts in the same run, `settings/service` 8 and
  `settings/shell` 23. Scratch copies of their `.json` files, carrying no link, give the same two
  numbers.

So no settings folder in this axiom gains a descriptor from its self-link. That holds for the
`SettingsBuilder` route and for `ae3.util/Settings.js`, whose `filterDescriptors` is the same name test
over `getContentCollection`. The one reader that filters on content instead is the manual index, and
`/Volumes/workspace/myx/ae3.sdk/MAGIC.md` carries its measurement.

## Auth is the abstract half and AuthVfs the only provider here, and admin means one file per account

`ae3-packages/ae3.base/resources/lib/ae3.util/Auth.js` (624 lines) and `AuthVfs.js` (412 lines) are one
pair: `AuthVfs.js:39-43` declares `ae3.Class.create("AuthVfs", Auth, ...)`, and `Auth.js:392-396`
declares `Auth` with no parent.

- `Auth.js` leaves 21 methods as `throw new Error("Not Implemented")`. `AuthVfs.js` overrides all 21 and
  holds 0 occurrences of `Not Implemented`. So a `Not Implemented` thrown from a live
  `ae3.util/AuthVfs` instance means a third provider is in play, not this one.
- `Auth.authCheckMembership` (`Auth.js:563-567`) is a pure delegate, so every membership answer is the
  subclass's.
- `AuthVfs.checkMembership` (`AuthVfs.js:357-360`) tests one file's existence:
  `<root>/accounts/<user>/membership/<group>`, with `vfsAccounts` built at `AuthVfs.js:57-58`. A group
  grant is therefore an empty file, and `addMembership`/`dropMembership` (`:362`, `:378`) create and
  remove it plus the matching `groups/<group>/members/<user>`.
- **`admin` and `def.supervisor` are not interchangeable, and one mounted provider maps the first onto
  the second.** `Auth.js:585` reports the `getUserInfo` `admin` flag for either group, and `Auth.js:36`
  lets either through the `authUserId` sudo branch. `Auth.js:466-467`'s `checkAdmin` and
  `ae3.web/Share.js:213`'s exception gate test `admin` alone. On a share mounting the AE3 provider, a
  `def.supervisor` account is reported admin, may sudo, and still gets the short error body.
- **The exception gate's `admin` test is answered by whichever provider the host mounts.**
  `ae3.web/Share.js:43-45` leaves `authenticationProvider` undefined and reads it at `:213`.
  `ru.myx.ae3.web.iface/Ae3WebService.js:51-55` returns `./AE3`, the `AuthVfs` over
  `storage/data/ae3/clients`; `ru.myx.acm.iface/AcmWebService.js:51-55` returns `./ACM`.
  `ACM.js:8` binds `authCheckMembership` to `ru.acmcms.internal/util/AuthAcmAnyControllerJail.js:89-103`,
  and that function rewrites the name before looking it up:
  `getGroup(groupname == 'admin' ? 'def.supervisor' : groupname, true)` at `:101`.
  `AuthAcmJail.js:67-74` repeats it. Neither AE3-side provider rewrites anything:
  `AuthVfs.js:357-360` and `AuthMap.js:329-340` look up exactly the name they were given. So on a host
  mounting `acm.local.json:3`'s `AcmWebService`, a `def.supervisor` client passes the `admin` test.
- **No code in either tree creates a group named `admin`.** On the AE3 side it is a console step, not a
  shipped default: `addGroup` at `Auth.js:252-278` is the only creator, and `AuthVfs.addMembership`
  refuses an unknown group with "Group '<name>' is unknown!" at `AuthVfs.js:368-371`. A grep for
  `addGroup` across `ae3.api`, `ae3.sdk`, `ae3.sys`, `ae3.sys.pkg.base`, `ae3.sys.pkg.i3.web`,
  `ae3.web` and `ae3.pkg.net.ndm.client` returns one hit, a documentation URL at
  `auth-accounts/AddGroup.js:55`. The ACM side ships five defaults and no `admin`:
  `AccessManagerImpl.java:1876-1920` creates `def.guest`, `def.registered`, `def.handmade`,
  `def.system` and `def.supervisor`.
- **A third provider, outside both trees, does create a group named `admin`.** `magic-devops` read the
  NDSS share on the host serving `ndss.keenetic.net` at 16:38Z on 2026-11-10. There `NDSS.js:28` builds
  the provider over `ae3.util/AuthVfs`, and `:32` check-creates `admin` first among its groups. So the
  AE3 answer holds there and not the ACM one, and a `def.supervisor` account does not pass that gate.
  A copy of that module is on this machine. `/Volumes/workspace/eclipse` is a second Eclipse workspace,
  holding the pre-rename project names, and its `ndm.ndss` project holds
  `resources/lib/com.ndmsystems.ndss`.
  There `NDSS.js:39-40` builds the same `AuthVfs` provider, and `:42-60` check-creates fifteen groups
  with `admin` first. Its `wapi/NdssWebShare.js` is 581 lines and sets `authenticationProvider` to
  `NDSS` at `:25-27`. That tree is git-tracked against `ssh://ndl.ru/var/git/cloud/ndss` at `a86b37e`
  (2015-08-21), so these are the module's own files and not a decompiled copy. Its line numbers are not
  his. The file on his host is named `NdssWapiWebShare`, which occurs in none of the 106 roots searched,
  where the control `NdssWebShare.js` gives one. That count of 106 includes `/Volumes/workspace/eclipse`
  as historical evidence only, since no place name resolves it.
- The provider is constructed once, at
  `ae3.web.server/resources/lib/ru.myx.ae3.web.iface/AE3.js:3-4`, over
  `storage/data/ae3/clients`. That project, not this one, is where the web gate consumes it.

The `messageContent` property on a thrown error carries the raw thrown value, not the dump. `BaseError.java:70`
declares `Object getMessageContent()` beside `String getMessage()`, both `@ReflectionExplicit`.
`BaseAbstractException.java:73-76` returns `getMessage()`; `ExecErrorDefault.java:78-81` returns
`this.message` while `getLocalizedMessage()` at `:61-65` returns `this.info`, the dump text.
`ExecErrorWrapped.java:184-187` returns `this.prototype`, the wrapped throwable, so a wrapped error is
short too.

The property is on the prototype, and the receiver is the error itself.
`BaseError.PROTOTYPE` is built at `BaseError.java:17` by `Reflect.classToBasePrototype`, and manual
reflection admits only `@ReflectionExplicit` members (`ValueReflectionImpl.java:1052`). A `getX` method
then becomes the property `x` (`:788-796`). `BaseNativeError.java:66-78` answers own-property lookups
for `"message"` alone, so `messageContent` resolves through the prototype rather than the instance.
The object the read started from, not the prototype that held the property, is what reaches the
getter: `BaseObject.java:2062` passes `this` into `propertyGetCtxResult`
(`HandlerForGetterAndSetter.java:88-97`), and `FMIVoid.java:60-67` resolves that object with
`ReflectReference.getInstanceForNative`. That returns the candidate
when `expected.isInstance(candidate)` holds (`ReflectReference.java:59-83`), and `expected` is the
declaring class, `BaseError`, which every AE3 error satisfies.

## One compile dependency the project.inf omits

`CryptoApiHelper` needs a third source project, and `project.inf` names two.

- `project.inf:3-5` declares `ae3.api` and `ae3.sdk` alone.
- `.classpath:11` adds `/clean-java.crypto` as a third source project.
- `java/ru/myx/ae3/internal/crypto/CryptoApiHelper.java:13-15` is the only file that needs it, importing
  `ru.myx.crypto.EllipticCurveSecp256r1`, `ru.myx.crypto.SignatureECDSA` and `ru.myx.util.Base58`.
  `Base58` lives in `clean-java.util`, which reaches the build through `ae3.api`'s exported entries
  (`ae3.api/.classpath:10`), so only `clean-java.crypto` is a project-level omission.

A build driven from `project.inf` rather than from the Eclipse `.classpath` misses that third project.
Eclipse resolves it, so the omission stays invisible there. Read the `.classpath` before moving the file
or rebuilding this project outside Eclipse. Measured 2026-11-13.

## The eight `Auth*.js` questions, each answered from the source

Every figure below was read in the `eclipse-ae3` place on 2026-11-17, and none is a run. The section "Auth is the abstract half and AuthVfs the only provider here, and admin means one file per account" carries the deeper record of the same files. These eight are the questions a reader of that record's two open points must answer first.

- **Where the three provider files live.** They sit in `/Volumes/workspace/myx/ae3.sys.pkg.base/ae3-packages/ae3.base/resources/lib/ae3.util/`. `Auth.js` is 624 lines, md5 `ca53e83c61a5491a2693524b0d6c9717`. `AuthVfs.js` is 412 lines, md5 `2ab45088b93ba85fa858419f1f290e1c`, and `AuthMap.js` is 393 lines, md5 `879d9e2d3d655d395f1341d159bef467`. `/Volumes/workspace/myx/ae3.sys.pkg.i3.web` consumes them and holds none of them.
- **`Auth` is the abstract half and `AuthVfs` its provider.** `Auth.js:392-396` creates `Auth` with `undefined` as parent. `AuthVfs.js:39-43` creates `AuthVfs` inheriting `Auth`.
- **`AuthVfs` covers all 21 methods `Auth` leaves open.** `Auth.js` holds 21 `throw new Error("Not Implemented")` bodies. `AuthVfs.js` and `AuthMap.js` hold 0 each. So a `Not Implemented` thrown from a live instance means a third subclass is in play.
- **`admin` at the check is one file's existence, and two spellings pass the two-name rules.** `AuthVfs.js:359` tests `<root>/accounts/<user>/membership/<group>`. `vfsAccounts` is built at `AuthVfs.js:57-58`. `Auth.js:36`, `:585` and `:597` accept `admin` or `def.supervisor`.
- **The live AE3 web share takes its provider from `Ae3WebService`, over `storage/data/ae3/clients/accounts`.** `ae3.web/Share.js:43-44` declares `authenticationProvider` as `undefined`, so a share overrides it. `/Volumes/workspace/myx/ae3.sys.pkg.i3.web/ae3-packages/ae3.web.server/resources/lib/ru.myx.ae3.web.iface/Ae3WebService.js:51-53` returns `./AE3`. That module builds its provider over `storage/data/ae3/clients` at `AE3.js:2-4`.
- **`AuthMap` is a third concrete subclass, and `admin` cannot resolve through it here.** It is built over a literal map, in the logout route alone. The three build sites are `Share.js:113`, `AbstractWebPage.js:89` and `IndexPage.js:210`. All three pass `AUTH_LOGOUT_MAP`, defined at `AbstractWebPage.js:185-203`. That map's `groups` is empty and its only account is `guest`.
- **A census over the three projects these questions touch returns twelve files.** This project holds three. `/Volumes/workspace/myx/ae3.sys.pkg.i3.web` holds five: `AuthAccountsPage.js`, `AuthPersonalPage.js` and three under `wapi/developer/`. `/Volumes/workspace/myx/ae3.sys.pkg.l2.tgt.xml` holds four reply builders under `ae3.l2.xml/helper/`, and those are not providers.
- **The admin gate at `Share.js:213` reads a provider method, and its `try` yields the narrow answer.** `Share.js:213` reads `share.authenticationProvider?.authCheckMembership(context.client, 'admin')` inside a `try` whose `catch` leaves the flag false. `Auth.js:563-566` makes `authCheckMembership` a pure delegate, so the answer is the subclass's own. Which provider a host mounts is a settings fact, and no source read settles it.
