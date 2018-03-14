import os, json, sys

modid = "ebwizardry"

here = os.path.dirname(os.path.realpath(__file__))
source = os.path.join(here, "src", "main", "java", "electroblob", "wizardry", "registry", "WizardryAchievements.java")
dest = os.path.join(here, "src", "main", "resources", "assets", modid, "advancements")

if not os.path.isfile(source):
    print("Can't find input file: ", source)
    sys.exit(1)

if not os.path.isdir(dest):
    os.makedirs(dest)

with open(source, "r") as src:
    cheev = ""
    for line in src:
        if "public static final Achievement " in line:
            cheev = ""
        cheev = cheev.strip() + line.strip()
        if "registerStat" in line:
            acname, acdetails = cheev.split("=", 1)
            acname = acname.strip().split(" ")[-1]
            special = True if "setSpecial" in acdetails else False
            acdetails = acdetails.split("(", 1)[1].split(")", 1)[0].split(",")
            mod, icon = acdetails[4].strip().split(".")
            mod = modid if mod == "WizardryItems" else "minecraft"
            icon = mod + ":" + icon.lower()
            adv = {
                "display": { "icon": { "item": icon } },
                "title": { "translate": "achievement." + acname },
                "description": { "translate": "achievement." + acname + ".desc" },
                "parent": modid + ":" + acdetails[5].strip() if acdetails[5] != "null" else modid + ":root",
                "criteria": {
                    "criteria_0": {
                        "trigger": modid + ":trigger_" + acname
                    }
                }
            }
            with open(os.path.join(dest, acname + ".json"), "w") as out:
                json.dump(adv, out, indent=4)


with open(os.path.join(dest, "root.json"), "w") as root:
    content = {
        "display": { "icon": { "item": modid + ":wizard_handbook" } },
        "title": { "translate": "itemGroup.wizardry" },
        "show_toast": False,
        "announce_to_chat": False,
        "criteria": {
            "criteria_0": {
                "trigger": modid + ":trigger_root"
            }
        }
    }
    json.dump(content, root, indent=4)
