---
name: About Java
---
## Java requirements

The randomizer requires **Java 8 (or higher), 64-bit**. If you do not have 64-bit Java, you will not be able to start the launcher; without using the launcher, you will not be able to randomize 3DS games.

You can check your Java version by opening a terminal window and typing `java -version`:

![java -version output]({{ site.baseurl }}/assets/images/wikipages/about_java/java_version.png)

If you run this command and it does _not_ say that you have Java version 1.8 and a 64-bit VM, you will need to upgrade Java.

## Java Downloads

["Recommended" Java download](https://java.com/en/download/) (if this page does not explicitly state that it is the 64-bit version of Java, use the link below instead)

[All Java downloads](https://java.com/en/download/manual.jsp) (make sure to pick 64-bit version and correct operating system)

## Troubleshooting

### When I try to launch the launcher, nothing happens, and there is no output in the terminal window.

While this is not guaranteed to fix your issue, try seeing if your antivirus is interfering with the launcher. Either turn it off entirely or just make sure it's disabled for the launcher scripts and try again.

If that *still* doesn't work, then try moving the entire randomizer folder to a different directory on your computer; Windows and macOS can sometimes prevent applications from writing files to certain locations.

### When I try to randomize the game, it fails and I see something like "Picked up _JAVA_OPTIONS: -Xmx512M" in the terminal window.

This error indicates that you have an environment variable that overrides the amount of memory available to Java; you'll need at least 4 GB (4096 MB) to use the randomizer. To remove this environment variable on Windows, follow these steps:
1. Hit the start button, then type "Edit the system environment variables" and select the option that looks like this:
![]({{ site.baseurl }}/assets/images/wikipages/about_java/edit_env1.png)
2. In the window that pops up, click on the "Environment Variables" button:
![]({{ site.baseurl }}/assets/images/wikipages/about_java/edit_env2.png)
3. In either the user or system variables, you should find _JAVA_OPTIONS. You'll need to find and delete it. Just click on it and press "Delete".