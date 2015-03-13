When I was looking for a IDEA plugin which uses Eclipse Code Formatter, I was out of luck. I stumbled upon an interesting plugin called [changelistaction](http://code.google.com/p/changelistaction/) which allows you to invoke any external command on selected changelist. But out of the box, I can't use it with Eclipse code formatter as there was no way of passing entire list of files as a command line param to external command.

I then wrote my own plugin inspired from this and added few more features.

This can be executed against any VCS change list.

Instructions:

1. Go to "Changes" tool window.

2. Right click on a change list and select "Eclipse Code Formatter".

OR

In Commit Changes (Ctrl+K) window, click on "Eclipse Code Formatter" button under Commit action and it will execute formatter.

Please note that you will have to launch the Commit Changes window again to perform commit.

OR

You can select "Run Eclipse Code Formatter" Before Commit action and it does formatting and would commit code as well.


After writing this plugin, I found another plugin [eclipse-code-formatter-intellij-plugin](http://code.google.com/p/eclipse-code-formatter-intellij-plugin/) which does the same thing. Interestingly, this was created just couple of days back and if I was looking for a Eclipse Code Formatter I would use this.