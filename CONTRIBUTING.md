# Contributing

Thanks for helping improve SkDialogs. Issues and pull requests are welcome.

## Dev setup

You need a JDK 25 (the version Paper 26.2 requires). The Gradle wrapper fetches everything else.

```sh
./gradlew build
```

The jar lands in `build/libs/skdialogs-<version>.jar`.

To test a change in game, set up a local Paper server matching `paperVersion` in
`gradle.properties`, install [Skript](https://github.com/SkriptLang/Skript), drop the built jar
into `plugins/`, and load a script from [`examples/`](examples/). `showcase.sk` exercises every
component the addon exposes.

## When the build breaks against a new Paper version

The addon compiles against a pinned Paper build on purpose: the dialog API is experimental, and a
compile error against a known version beats a silent failure at runtime. To move to a new Paper
build, bump `paperVersion` in `gradle.properties` and fix what no longer compiles. All contact with
the dialog API is in
[`PaperDialogs.java`](src/main/java/com/ch99q/skdialogs/paper/PaperDialogs.java), so the fixes stay
in one file.

The scheduled `paper-api` workflow compiles against the newest Paper build every week, so a break
usually shows up there first, as a failed run.

## Releasing

Retitle the Unreleased section of `CHANGELOG.md` to the new version and date, bump `version` in
`gradle.properties`, then push a `vX.Y.Z` tag. That builds the jar, attaches it to a GitHub
release, and publishes it to Modrinth, with that changelog section as the notes on both. Tagging a
version without a changelog section fails the release.

The Modrinth step needs one-time setup and is skipped until it is done:

1. Create a Modrinth project with the slug `skdialogs`.
2. Create a Modrinth personal access token allowed to create versions.
3. Add it to the GitHub repository as a secret named `MODRINTH_TOKEN`.

## Pull requests

- Keep a pull request small and focused on one change.
- A user-visible change adds a line to `CHANGELOG.md` under Unreleased.
- Follow [Conventional Commits](https://www.conventionalcommits.org/) for commit messages
  (`feat:`, `fix:`, `docs:`, `chore:`, `refactor:`, `test:`).
- New syntax needs three things: doc annotations on the element (`@Name`, `@Description`,
  `@Examples`, `@Since`), a section in the README, and an example or an addition to an existing one.
- CI must pass; it builds the jar on every push and pull request.
