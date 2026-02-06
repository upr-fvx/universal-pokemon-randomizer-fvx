---
name: Building the Universal Pokémon Randomizer FVX
---
This page is to help aspiring developers learn how to build the randomizer; if you're not interested in development, please download an official release from our [release page](https://github.com/upr-fvx/universal-pokemon-randomizer-fvx/releases) instead. Versions of the randomizer built via the steps on this page will **not** be officially supported!

## Setting up the project IntelliJ IDEA

The main developers of this randomizer use IntelliJ IDEA to develop, build, and test the randomizer. Other Java IDEs may or may not work (they will probably have difficulties building the GUI), but IDEA is the only one that works for sure. The following steps assume you're using IDEA:

1. Download and install [IntelliJ IDEA](https://www.jetbrains.com/idea/). The free Community version is fine.
2. Clone the randomizer's source code to your machine like so:
```
git clone https://github.com/upr-fvx/universal-pokemon-randomizer-fvx.git
```
3. Run IntelliJ IDEA. [On the initial window, select "Open or Import,"]({{ site.baseurl }}/assets/images/wikipages/building/import.png) then select the universal-pokemon-randomizer-fvx folder that you cloned in the previous step.
4. Under File -> Project Structure -> Project, [ensure that the "Project SDK" is set to 1.8]({{ site.baseurl }}/assets/images/wikipages/building/set_sdk.png). If you do not have any version of 1.8 available, hit the "Edit" button next to the "Project SDK" dropdown to navigate to the SDK page. [Click the plus button near the top of the window to expose the "Download JDK..." option]({{ site.baseurl }}/assets/images/wikipages/building/download_jdk.png), then select any 1.8 version.
5. Under Run -> Edit Configurations, hit the plus button near the top of the window to add a new Application configuration. Set your "Main class" to `com.dabomstew.pkrandom.gui.RandomizerGUI`, set your "Program arguments" to `please-use-the-launcher`, and set your JRE to 1.8 if it isn't already that. The end result should look [something like this]({{ site.baseurl }}/assets/images/wikipages/building/program_arguments.png). The "please-use-the-launcher" program argument allows you to use all features of the randomizer without going through the launcher. You may also want to add `-Xmx4096M` to the VM options to ensure that you will be able to randomize 3DS games.

## Building and running the randomizer
Assuming that you followed all steps in the previous section, you should now see something like Run -> Run 'Randomizer' (or whatever you named your Application configuration). Clicking that will build all code and run the randomizer if the build is successful. Congrats, you've now built the randomizer!