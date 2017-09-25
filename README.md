# AutoSwitch #

AutoSwitch is a very advanced mod that automatically switches your tools and weapons.

See [http://is.gd/ThebombzensMods#AutoSwitch](http://is.gd/ThebombzensMods#AutoSwitch) for details.

Note: If you want to contribute, feel free! Just send me a pull request and I'll review it before adding it.

## Reporting Bugs ##
You can report bugs on the bug tracker at Github.

If your bug report is about incorrect behavior or a crash, remember to provide a link to a debug log. Do this by enabling debug logging in the config screen. Then, trigger the bug (preferably in creative mode, set "Use in Creative" to ON). Then send me the file .minecraft → mods → AutoSwitch → DEBUG.txt by putting it on a paste site like [https://gist.github.com/](https://gist.github.com/), [http://pastebin.com/](http://pastebin.com/), or [https://0x0.st/](https://0x0.st/). Remember to disable debug when you're done.

## Compiling ##

First, you need to clone [ThebombzenAPI](https://github.com/thebombzen/ThebombzenAPI) to the same directory that you cloned AutoSwitch. i.e. you should see ThebombzenAPI/ and AutoSwitch/ in the same directory.

Then navigate to AutoSwitch and run:

	$ ./build.sh

This will create the directory "build" which and all build supplies inside of it, and should create a finished AutoSwitch jar file upon completion.

On Windows? Sorry, you're on your own. I don't know how to write CMD Batch files. 

## Eclipse ##

Once you've run the buildscript at least once, you can go to Eclipse and select File -> Import -> Existing Projects to Workspace, and select AutoSwitch as the root project directory. If you have the Git plugin it should recognize AutoSwitch as a git respository.

## Releases ##

The releases in the upper-right contain intermediary releases that don't bump the version number. This is to publish hotfixes without reminding everyone to update.


