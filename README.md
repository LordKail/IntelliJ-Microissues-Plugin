# IntelliJ-Microissues-Plugin
A plugin for embedding and viewing microissues in IntelliJ.

Microissues are represented in the code as comments in a similar style to JavaDoc. A sample ticket in source code would, for example, look like:
```java
/*
@tckt Sample Summary
@type BUG
@priority 6
*/
```

The type and priority of the tickets are optional with the only required tag for a ticket being ```@tckt```.

The plugin scans for these types of comments and displays the tickets in a tool window in a tree structure with the corresponding ticket information to its right. 

The plugin can be found and installed on the IntelliJ IDEA Plugin Repository:
https://plugins.jetbrains.com/plugin/9576-microissues

YouTrack Link for Issue Management:
https://microissues-plugin.myjetbrains.com/youtrack/issues/MIJP
