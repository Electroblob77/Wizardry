# Guide for contributing to this repository

## Submitting an issue

When submitting an issue, please stick to the issue template. Please give me as much information about the issue as you can, but keep it relevant. "This is really annoying me and my friend" does not help me fix the problem! Most importantly, use gist or pastebin to post crash reports.

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

- Secondly, the handbook text file: the US english version is named handbook_en_US.txt, and (rather obviously) other languages' text files replace 'en_US' with the name of their corresponding .lang file. Anything wirtten in BLOCK CAPITALS should be left alone, because Wizardry replaces it with the appropriate text when the game is running.
