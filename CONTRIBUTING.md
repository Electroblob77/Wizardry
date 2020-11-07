# Guide for contributing to this repository

## Submitting an issue

When submitting an issue, I would be grateful if you could follow these few guidelines:

- First and foremost, please check your issue hasn't already been posted. The issue tracker is searchable, so try typing in a few keywords and see if anything comes up.

- Please stick to the issue template, it's there to help you provide the right information so I can fix the problem more easily. If you're not sure what to put for anything, that's ok, just leave it as it is.

- One issue per issue please! Make a separate issue for each bug/suggestion. If you think multiple different issues may be related, reference between them using # followed by the issue number, and github will automagically link them.

- Please give me as much information about the issue as you can, but keep it relevant. "This is really annoying me and my friend" does not help me fix the problem!

- Make sure your wizardry and Forge versions are both up-to-date for the game version you are using.

- There's no need to put 'tags' in square brackets in the issue title, I'll put proper github issue tags on it which will also help organise it. You can use them to search for issues too!

- Don't close an issue unless it has actually been fixed or is no longer relevant - otherwise I might not end up fixing it at all! Usually I'll take care of closing issues anyway, so you don't have to worry about it.

- Please don't revive old issues, _especially_ if they have been fixed, because I don't routinely check them. Make a new issue and link the old one to it if you think it's related.

- Most importantly, use gist or pastebin to post crash reports. Do not paste the entire crash report inline, not even in a code block!
> If you've got a git account (which you do, if you're posting an issue) you can access gist, and pastebin doesn't require an account at all, so there's no excuse!

## Submitting a PR

I like my code to be tidy, so it saves me time if yours is too. Here are a few general guidelines for how to submit a good PR:

- Use proper indentation and try to space your code out nicely. This really goes without saying - select all your code and hit ctrl-i before you submit it, and try to use my spacing if you can (for instance, don't do what the Minecraft source does and put a { on a separate line, it's a waste of space).

- Commented code is dead code, UNLESS it has an accompanying comment detailing an appropriate reason for it being commented. (something like "DO NOT REMOVE, commented because it will be useful in 1.12" or "DO NOT REMOVE, commented for debugging purposes, uncomment when fixed")

- Please comment your methods/fields/classes! Preferably using Javadoc, but for small/private methods one line comments are OK. I find it hard enough to remember what I wrote a method for if it isn't commented, let alone trying to work out what yours are for. That said, you should try to name the method/field/class so it's obvious what it is anyway.

- Please use @Override when overriding any methods.

- Follow naming conventions. BLOCK_CAPITALS for constants, CapitalisedWords for classes, camelCase for almost everything else.

- This isn't code related, but when you submit a PR, please tell me what it does! You don't need much, just something like "Makes item X do X" or "Fixes #162". If you're resolving an issue, put "Fixes", "Closes" or "Resolves" and then the issue number and Github will close it automatically.

## Guide for translators

Pull requests aren't just for code - if you would like to help translate Wizardry, you can do that here too! Simply fork the repository, add your translations and submit a PR when you're done. Wizardry has TWO files you'll need to translate: the usual .lang file and the handbook text file, which can be found in resources/assets/wizardry/texts.

- Firstly, the .lang file: if you're familiar with translating mods, this is all pretty standard - it holds the names of blocks, items, mobs and so on. Ignore the %X$s bits, they're placeholders for stuff that gets put in by Wizardry when the game is running.

- Secondly, the handbook JSON file: the US english version is named handbook_en_us.json, and (rather obviously) other languages' text files replace 'en_us' with the name of their corresponding .lang file. Make sure you only change the _values_ (after each `:`), not the identifiers - for a summary of JSON syntax, see the [JSON webpage](https://www.json.org/json-en.html). If you're familiar with advancements, loot tables and similar files, this won't be new to you. The handbook file also applies its own form of markup to the main text, with the following features:

    - `#image tag` adds an image to the text with the given tag. Image tags are defined in the `"images": {...}` block at the start of the file. Each named entry in this block defines a single image with that name as its tag. Each entry is a JSON object containing a file location and optional caption/dimensions.
    - `#recipe tag` adds a crafting recipe to the text with the given tag. Similar to image tags, recipe tags are defined in the `"recipes": {...}` block at the start of the file. Each named entry in this block defines a recipe with that name as its tag. Each entry contains a list of one or more locations or recipes to display (if more than one is specified, the display will cycle through the different recipes).
    - Anything else with a `#` directly before it (with no space) will be treated as a format argument if possible. This means everything between the `#` and the next space will be replaced with something else at runtime, similar to the `%X$s` bits in a normal .lang file. The current available format arguments can be found [here](https://github.com/Electroblob77/Wizardry/blob/1.12.2/src/main/java/electroblob/wizardry/client/gui/handbook/GuiWizardHandbook.java#L177-L203).
    - `@section@` adds a hyperlink to the given section, which will display as the name of that section. `@section alt text@` is the same, except alt text is displayed instead of the section name (alt text may contain spaces). It is also possible to hyperlink to a webpage, simply replace the section name with the url (e.g. `@https://example.com/@` or `@https://example.com/ alt text@`).
    
If all of that confused you, the simple rule is: _Don't translate anything directly after an `@` symbol, directly after a `#`, or the first word after `#image` or `#recipe`_. However, you should translate the `"caption"` entries in the `images` block.

**Please ensure all translation files are encoded in UTF-8 format, otherwise they might crash the game!**
