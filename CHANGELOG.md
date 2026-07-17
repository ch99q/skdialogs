# Changelog

User-visible changes per release, in [Keep a Changelog](https://keepachangelog.com) style. New
entries go under Unreleased; a release retitles that section, and CI publishes it as the release
notes on GitHub and Modrinth.

## [Unreleased]

## [1.1.0] - 2026-07-17

### Added

- Widths on bodies, buttons, and inputs (`with width 200`), and item body sizing (`sized 24 by 24`).
- Item body options: a description next to the item, `without tooltip`, and `without decorations`
  to hide the count and durability bar.
- `set external title to`, the label shown where a client lists the dialog outside itself.
- Toggle submit values (`with values "64" and "1"`), what a `$(key)` command placeholder becomes
  for each state.
- Slider label formats (`with format "{value} blocks"`).
- Multiline line limits (`multiline up to 5 lines`).
- The server links dialog type (`add server links to the list`).
- Server link management: `add server link labeled ... to ...`, typed links (`add website server
  link to ...`), and `clear the server links`.
- All dialog text now parses Skript's chat format, the same as `send`: hex colours and tags like
  `<translate:item.minecraft.filled_map>`, which each client renders in its own language.

### Changed

- A dialog with one button and an exit button now renders as a vanilla yes/no confirmation;
  setting `columns` keeps the multi-action layout.
- A slider without a default starts at the low end of its range instead of vanilla's midpoint,
  which could sit between steps.

## [1.0.0] - 2026-07-16

### Added

- The `create dialog` section: titles, bodies, item bodies, and button columns.
- Inputs: text (with defaults, max length, multiline), toggle, slider, and dropdown, including the
  option section form with per-choice labels.
- Buttons with actions: run a command (with `$(input)` templates), open a url, copy text, suggest
  a command, open another dialog, and the exit button.
- Dialog lists: menus whose entries open other dialogs.
- The `on dialog click` event with the clicked button, the dialog id, and typed input values.
- `show dialog` and `close dialog` effects.
