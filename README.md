# IntelliJ IDEA / PhpStorm Phalyfusion Plugin
## NOTE: This repository is the development history. See [taptima/idea-php-phalyfusion-plugin] for the current version.
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

IntelliJ IDEA Ultimate __2020.1 — 2020.2.3__  
PhpStorm __2020.1 — 2020.2.2__

## Installation and usage

*   Install and configure [Phalyfusion](https://github.com/taptima/phalyfusion) and desired code analysers
*   Install the plugin by going to Preferences | Plugins and searching "Phalyfusion" 
*   Provide path to Phalyfusion executable in "Preferences | Languages & Frameworks | PHP | Quality Tools | Phalyfusion"
*   Enable inspection in "Preferences | Editor | Inspections | PHP | Quality tools | Phalyfusion validation"
*   Instant analysis of current file highlights found errors in your code
*   You can manually run analysis via "Run inspection by name" choosing "Phalyfusion validation" and configuring analysis scope

## Features

*   Combining reports of multiple PHP static code analysers
*   Perform instant analysis of current file
*   Show found errors by highlighting them in code and provide descriptions in tooltips
*   Provide file-by-file structured interactive analysis report

## Screenshots

![Code editor highlighting](https://plugins.jetbrains.com/files/15198/screenshot_23442.png)
![Inspection result](https://plugins.jetbrains.com/files/15198/screenshot_23443.png)
![Specify analysis scope](https://plugins.jetbrains.com/files/15198/screenshot_23444.png)

[//]: # (These are reference links used in the body of this note and get stripped out when the markdown processor does its job. There is no need to format nicely because it shouldn't be seen. Thanks SO - http://stackoverflow.com/questions/4823468/store-comments-in-markdown-syntax)

[taptima/idea-php-phalyfusion-plugin]: https://github.com/taptima/idea-php-phalyfusion-plugin
