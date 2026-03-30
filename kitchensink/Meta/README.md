# Meta

Adds commands for manipulating a held item.  Uses the
[MiniMessage format](https://docs.papermc.io/adventure/minimessage/format/)
where possible, i.e. in names, lore lines, and other display text.

## General

General lore manipulation.

| Subcommand | Function                              | Usage                                               |
|------------|---------------------------------------|-----------------------------------------------------|
| `name`     | Set an item's display name.           | `/meta name <name>`                                 |
| `reset`    | Reset an item's metadata to defaults. | `/meta reset`<br/>`/meta reset <DataComponentType>` |

## Lore

Used to manipulate item lore.  
Standalone subcommand of `/meta` accessible by `/lore` or `/meta lore`.

> [!NOTE]
> Position arguments are 1-indexed. For example, to overwrite the first line of lore, try
> `/lore set 1 <italic:false><white>Hello, world!`.

| Subcommand | Function                                       | Usage                            |
|------------|------------------------------------------------|----------------------------------|
| `add`      | Add a line of lore.                            | `/lore add <content>`            |
| `set`      | Replaces an existing line of lore.             | `/lore set <index> <content>`    |
| `insert`   | Adds a line of lore at a specific position.    | `/lore insert <index> <content>` |
| `delete`   | Deletes a line of lore at a specific position. | `/lore delete <index>`           |

## Book

Used to manipulate written books.  
Subcommand of `/meta`.

| Subcommand | Function             | Usage                      |
|------------|----------------------|----------------------------|
| `author`   | Set a book's author. | `/meta book author <name>` |
| `title`    | Set a book's title.  | `/meta book title <title>` |


## Head

Used to manipulate player heads.  
Subcommand of `/meta`.

| Subcommand | Function                        | Usage                                                                                       |
|------------|---------------------------------|---------------------------------------------------------------------------------------------|
| `owner`    | Change a player head's owner.   | `/meta head owner <name>`                                                                   |
| `texture`  | Change a player head's texture. | `/meta head texture <base64 texture>`<br/>`/meta head texture <textures.minecraft.net URL>` |
