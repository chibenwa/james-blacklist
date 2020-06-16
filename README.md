# Black lists for the Apache James server

Provides whitelist / blacklist management utilities for the Apache James server.

 - `blacklist-api` introduces the Backlist component interface
 - `blacklist-memory` introduces the Backlist memory implementation

This enables the following plugins to be used:

 - `blacklist-smtp` SMTP RCPT hook for blacklist
 - `blacklist-mailet` Matchers reading the blacklist
 - `blacklist-webadmin` WebAdmin REST endpoints to administer the blacklist
 
 Finally, `blacklist-integration` demonstrate how to write integration tests on top of a testing memory guice server.

`blacklist-memory`, `blacklist-smtp`, `blacklist-mailet`, `blacklist-webadmin` can be used as **extension-jars** for the Guice James servers. In order to resolve the BlackList interface to its implementation one needs to register `com.linagora.james.blacklist.memory.MemoryBlacklistModule` as a guice extension module. Once done, the extension routes, matchers & hooks can be used in other configurations. A full configuration example can be found here: https://github.com/chibenwa/james-blacklist/tree/master/blacklist-integration/src/test/resources


This project will serve as a basis of my ApacheCon Europe 2019 presentation `Gaining control over emails with Apache James`.

`#ACEU19: Benoit Tellier – Gaining control over emails with Apache James`

https://www.youtube.com/watch?v=zr8qpNkL6U4&list=PLU2OcwpQkYCxVGCGWtMxb9d27Z-pcoN9a&index=9&t=9s
