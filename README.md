IntelliJ IDEA / PhpStorm Phalyfusion Plugin
-------------
[![Version](http://phpstorm.espend.de/badge/15198/version)](https://plugins.jetbrains.com/plugin/15198)
[![Downloads](https://img.shields.io/jetbrains/plugin/d/15198)](https://plugins.jetbrains.com/plugin/15198)
[![Downloads Last Month](http://phpstorm.espend.de/badge/15198/last-month)](https://plugins.jetbrains.com/plugin/15198)

Provides convenient IDE integrated interactive analysis report of [Phalyfusion](https://github.com/taptima/phalyfusion) tool.  
Phalyfusion is a tool for convenient and effective usage of multiple PHP static code analysers such as:

*   PHPStan
*   Psalm
*   Phan
*   PHPMD
*   PHP-CS-Fixer (with --dry-run option)

__Page at JetBrains marketplace:__ [Phalyfusion Plugin](https://plugins.jetbrains.com/plugin/15198-phalyfusion)
## Supported products

IntelliJ IDEA Ultimate __2021.1.1 — 2021.1.2__  
PhpStorm __2021.1.1 — 2021.1.2__

## Installation and usage

*   Install and configure [Phalyfusion](https://github.com/taptima/phalyfusion) and desired code analysers
*   Install the plugin by going to Preferences | Plugins and searching "Phalyfusion" 
*   Plugin will find the path to Phalyfusion executable automatically on first run.
    You can provide path to Phalyfusion executable by yourself if needed.
    See "Preferences | Languages & Frameworks | PHP | Quality Tools | Phalyfusion"
*   Enable inspections "Phalyfusion global validation" and "Phalyfusion on-fly validation" in "Preferences | Editor | Inspections | PHP | Quality tools"
    to be able to launch Phalyfusion via plugin.
*   Use toolbar button or "Run | Run Phalyfusion" option to configure an analysis scope and launch Phalyfusion
    ![Run Phalyfusion button](https://i.imgur.com/FPowVBg.png)
*   Enable "Launch Phalyfusion in on-fly mode" setting in "Preferences | Language & Frameworks | PHP | Quality Tools | Phalyfusion | Configuration"
    for instant analysis of the current file and error highlighting

## Features

*   Phalyfusion auto detection in current project
*   Combine reports of multiple PHP static code analysers
*   Provide file-by-file structured interactive analysis report
*   Navigation between errors found by Phalyfusion
*   Perform instant analysis of current file
*   Show found errors by highlighting them in the code and provide descriptions in tooltips
*   Auto generate Phalyfusion neon configuration on the first run with all detected in the current project code analysers.
    
    **Note**: You need to set up all configuration files for code analysers by yourself.

## Screenshots

![Code editor highlighting](https://plugins.jetbrains.com/files/15198/screenshot_23442.png)
![Inspection result](https://plugins.jetbrains.com/files/15198/screenshot_23443.png)
![Specify analysis scope](https://plugins.jetbrains.com/files/15198/screenshot_23444.png)
![Plugin configuration](https://i.imgur.com/P28DYI2.png[/img])
![Inspection result 2](https://i.imgur.com/Awq0fS2.png)
