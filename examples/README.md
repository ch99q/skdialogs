# Examples

Each file is a complete, working script. To try one, copy it into `plugins/Skript/scripts/` on a
server running this addon, run `/sk reload all`, and run the command named at the top of the file.

Start with `confirm.sk` if dialogs are new to you; it is the smallest complete flow.

| File | Command | Shows |
|---|---|---|
| [confirm.sk](confirm.sk) | `/cleartrash` | A yes/no confirmation before a destructive action. |
| [rules.sk](rules.sk) | join the server | Rules shown on join that must be accepted before playing. |
| [welcome.sk](welcome.sk) | `/welcome` | A form with text, toggle, and dropdown inputs, read back in one click handler. |
| [settings.sk](settings.sk) | `/settings` | Every input type in one dialog, saved to per-player variables. |
| [wizard.sk](wizard.sk) | `/wizard` | A multi-page flow: each page is a dialog, and each click opens the next. |
| [list.sk](list.sk) | `/hub` | A dialog list: a menu whose entries open other dialogs, with Back buttons that loop. |
| [actions.sk](actions.sk) | `/menu`, `/rename`, `/class` | Action buttons, command templates with `$(input)`, labelled dropdown options, and keep-open dialogs. |
| [showcase.sk](showcase.sk) | `/showcase` | Every component at once; press a button and all values print to chat. |
