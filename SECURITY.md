# Security Policy

## Supported Versions

Generally speaking, DimensionPause will be built on the current LTS version of Java *or* the minimum supported LTS version of Java for MC. Currently, DimensionPause is built on **Java 17**.

The latest minor release and all revisions of the plugin will get feature and security updates, the previous minor release and all revisions will get security updates, and anything older than that will not receive updates unless A) that version is still used on a substantial amount of servers (See [bstats](https://bstats.org/plugin/bukkit/DimensionPause/19032)) and B) is vulnerable to a critical flaw (See [Log4j](https://en.wikipedia.org/wiki/Log4Shell)).

An example is below. Please note that DimensionPause is only on version 1.0.x. 

| Version       | Supported          | Support Given              |
|---------------|--------------------|----------------------------|
| 2.6.x         | :white_check_mark: | Feature & Security Updates |
| 2.5.x - 2.6.x | :white_check_mark: | Security Updates only      |
| < 2.5.x       | :x:                | No updates, update now!    |

## Reporting a Vulnerability

Reporting a vulnerability is extremely simple. [Click this link](https://github.com/TerrorByteTW/DimensionPause/security/advisories/new) to open a new security advisory. This will be privately sent to the developers of DimensionPause and will be reviewed as soon as received. If the flaw is deemed applicable, a fix will be released as soon as possible.

**DO NOT OPEN AN ISSUE FOR SECURITY VULNERABILITIES!** Not only will these be immediately closed and removed, but releasing information on security vulnerabilities without first attempting to notify the proper channels can be extremely detrimental to people who use this software. (See [SlickWraps Data Breach](https://web.archive.org/web/20200221151606/https://medium.com/@lynx0x00/i-hacked-slickwraps-this-is-how-8b0806358fbb). The person who discovered the breach "sent a subtle tweet, anticipating that the “Security Researcher, White Hat Hacker” designation in \[their\] Twitter bio would be sufficient enough to spark a line of communication" instead of properly disclosing it, causing others to attempt to breach SlickWraps as well).

Proper disclosure is crucial in these situations. I will do my absolute best to ensure security vulnerabilities are fixed, but doing so properly gives me the chance to fix it *before* it's abused.
