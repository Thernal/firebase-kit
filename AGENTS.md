# firebase-kit — rules for agents

firebase-kit is a **kit**: reusable Compose Multiplatform code that applications copy — renamed into their own
package — with `skillctl.sh kit install firebase-kit` (the skill-manager skill, `Thernal/knowledge`), and later
merge changes from with `kit update`. What they copy is listed in `kit.yml`: the modules `firebase/bridge`, `firebase/crash/api`, `firebase/crash/impl`, `firebase/crash/wiring` … and files placed outside Gradle modules (`ios/Firebase`, `scripts/distribute-firebase.sh`, `fastlane/FirebaseFastfile`).
Everything committed to `main` reaches every app that takes the next update, so this file's first rule is
about delivery.

## Delivering a change

- **Nothing reaches `main` without a green `./gradlew build`** — every target, the tests on the JVM host
  and the iOS simulator, Detekt. Check Gradle's own exit code (`./gradlew build && git commit …`), never
  through a pipe: `./gradlew … | tail && git commit` checks `tail`. A failing test is fixed, never skipped.
- Git conventions, attribution (off here) and branches: `.agents/workspace.md`. A breaking change to what
  apps use is `feat!:`/`refactor!:` with a `Migration:` paragraph — apps merge the kit's files, never their
  own call sites.
- Detekt fails the build on any finding. `./gradlew build -PdetektAutoCorrect=true` fixes formatting;
  the rest is fixed by hand (named arguments, braces on every branch, `is`-prefixed booleans, block bodies).
  A `@Suppress` names the rule and says why, on the narrowest element.

## Writing code here

- **No `api(...)`** (epic D24): a module declares everything it uses; nothing is re-exported.
- **Rename-safe.** An app's copy is renamed textually (`kit.yml`: package, module, alias, framework). Write the
  package prefix whole, never split or as a regex fragment; `libs.plugins.<alias>.` literally; and keep the
  kit's name out of any string an install does not rename (resource names, authorities, cache paths).
- `api`/`impl` code lives in `data`, `domain` or `presentation` packages, and layers point inwards
  (the kit's own Detekt rules). `wiring` holds Metro binding containers only.
- Kotlin nests block comments: never write `/*` inside KDoc (`image/*`, `ios/*.swift`).
- Every module that changes what apps copy updates `kit.yml` in the same change: `code`, `surface`,
  `requires`.

## Documentation

| File | Holds | Update when |
|---|---|---|
| `README.md` | what the kit does, its layout, how it is built | anything a user of the kit sees changes |
| `firebase/README.md` | why each part has its shape | a decision or trade-off changes |
| `firebase/DESIGN.md` | why each part has its shape | a decision or trade-off changes |
| `docs/todos/` | open questions, one file each | a question opens or is decided (then delete it) |
| `skills/firebase-kit` | the same for an agent in an app that took the kit | the public surface changes — `kit status` flags a skill older than the surface (LAG) |
| `kit.yml` | what an app copies and what its build must provide | a module, file part or requirement changes |

Docs are read in place by apps (`knowledgectl.sh kit firebase-kit read <path>`), never copied. When `kit.yml`
or `README.md` changes, the knowledge card `kits/firebase-kit.md` in `Thernal/knowledge` needs its `card_sha`
bumped (`scripts/check-corpus.py --kits` says so).

## The sample

`sample/` shows every capability on Android and iOS and is never copied into apps. A sample that builds is
not one that starts: after changing it, build the iOS app with `xcodebuild` and launch it on a simulator
(`xcrun simctl launch --console-pty …`) — Compose Multiplatform refuses to start without
`CADisableMinimumFrameDurationOnPhone` in Info.plist, and a crash there shows nowhere else.
