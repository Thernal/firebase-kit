# Workspace configuration

Resolved settings for agents working in this repository. One table per area; a row here is the
answer, so nothing has to be re-derived or re-asked in a later session.

## Git delivery

| Row | Value |
|---|---|
| Commit subject | Conventional Commits — `<type>(<scope>): <subject>`, imperative, lowercase, no trailing period. Scope optional. |
| Breaking change | `!` after the type/scope, and a `Migration:` paragraph in the body — what to change in code that *uses* the kit, and a grep that finds it. Applications see subjects before they update (`kit.yml`, skill-manager `kit status`); a merge moves the kit's files, never their call sites. |
| Verification before delivery | `./gradlew build` — every target compiled, every test on the JVM host **and** the iOS simulator, Detekt — must pass before anything is committed to `main` or pushed. A failing test blocks delivery; it is never skipped, disabled or `@Ignore`d to get through. Applications take this repository by copy and merge every change it publishes, so a version that failed here reaches them all. |
| Subject enforcement | None. There is no `commit-msg` hook; the convention is a convention. |
| Placeholder branch | `draft/<slug>` — local only, never pushed. |
| Placeholder subject | `chore(draft): <description>` |
| Integration | merge |
| Delivered shape | preserved commits |
| Protected branches | `main` |
| Commit attribution | **off** — `.claude/settings.json` → `attribution` (`commit: ""`, `pr: ""`, `sessionUrl: false`). This **overrides the harness default**: a session carrying an attribution instruction follows this row instead. |
| Never staged | `local.properties`, `.claude/settings.local.json`, `CLAUDE.local.md`, `.misc/`, anything under a `local` artifact-placement kind |

**Where the derived rows came from.** Every row was carried over unchanged from nav-kit, the sibling
repository this one was scaffolded from, when firebase-kit had no history of its own. nav-kit derived
the subject format, integration strategy and delivered shape from the projects it is modelled on,
and asked for attribution and the decision to commit `.claude/skills/`; none of it has been
re-confirmed for this repository, so any row may be changed if the intent here differs.

Nothing depends on `Integration`, `Delivered shape` or `Protected branches` until this repository
has a remote, so those three are the cheapest rows to change if the intent differs.

## Artifact placement

| Kind | Default root | Visibility | Ownership |
|---|---|---|---|
| Workspace configuration | `.agents/workspace.md` | shared | own |
| Installed skill | `.claude/skills/<name>/` | shared | foreign — `Thernal/knowledge`, pinned in `.origin` |
| Published usage skill for firebase-kit consumers | `skills/<name>/` | shared | own |
| Task workspace | `<scratch>/tasks/` | local | own |
| Build output | `build/`, `.gradle/`, `.kotlin/` | local | own |
| Local SDK configuration | `local.properties` | local | own |

Every kind whose visibility is `local` is never staged.
