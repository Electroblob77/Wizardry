name: 🐛 Bug Report
description: Something is not working as expected
title: "[Bug] <write the title here>"
labels: [ "bug", "Needs Triage" ]
body:
  - type: markdown
    attributes:
      value: |
        Please read the [guide for contributing](https://github.com/Electroblob77/Wizardry/blob/1.12.2/CONTRIBUTING.md) before posting. You may also find the [troubleshooting page](https://github.com/Electroblob77/Wizardry/wiki/Troubleshooting) helpful.
        Please try the latest version of the mod and check if the issue persist before submitting a report.
  - type: checkboxes
    attributes:
      label: Is there an existing issue for this?
      description: Please search to see if an issue already exists for the bug you encountered.
      options:
        - label: I have searched the existing issues
          required: true
  - type: textarea
    id: observed
    attributes:
      label: Observed behaviour
      description: Describe what happened and when this happened
    validations:
      required: true
  - type: textarea
    id: expected
    attributes:
      label: Expected behaviour
      description: Describe what you expected to happen
    validations:
      required: true
  - type: textarea
    id: reproduction
    attributes:
      label: Steps to reproduce
      description: Step by step instructions on how to reproduce the observed behaviour
      placeholder: |
        1. Join a server
        2. Cast the [spell]
        3. ...
    validations:
      required: true
  - type: input
    id: crashlog
    attributes:
      label: Crashlog
      description: If this was a crash, grab [minecraft instance]/logs/debug.log, upload it as a [gist](https://gist.github.com/) and paste the link here. Alternatively, give a link to the crash report on gist, pastebin if you already have one there.
      placeholder: https://gist.github.com/<your-user>/abcdef124
  - type: dropdown
    id: environment
    attributes:
      label: Environment (Singleplayer/Server, etc.)
      options:
        - Singleplayer
        - LAN game/Server
        - Issue present on both sides
        - Not sure
    validations:
      required: true
  - type: input
    id: mod-version
    attributes:
      label: Mod version
      placeholder: 1.0.0
    validations:
      required: true
  - type: input
    id: minecraft-version
    attributes:
      label: Minecraft version
      placeholder: 12.2.2
    validations:
      required: true
  - type: input
    id: forge-version
    attributes:
      label: Forge version
      placeholder: 1.12.2 - 14.23.5.2860
    validations:
      required: true
  - type: textarea
    id: other-mods
    attributes:
      label: Other mods
      description: List any other mods present when the bug was encountered, include the mod versions. You only have to fill this if no crash log was attached
