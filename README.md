bukkit-sudo
===========

A Bukkit plugin for executing commands with other usernames.

Usage
-----

    /sudo [-psv] [--] <command>
    /sudo [-psv] -u <user> [--] <command>

I'm well aware that the first form is all but useless - I just wanted the
syntax to match that of sudo.

Without `-p` set, /sudo gives you op permissions plus any permissions held by
the calling user. With `-p` it gives you every permission. Some plugins might
have "bad permissions" that make `-p` a bad option but in the absence of these
(and I don't know of any important ones) it's probably a good idea.

*Note that the `-u` option has no effect on the permission set /sudo gives
you.* `-u` simply runs the command from the username given instead of your own
name.

By default /sudo prints [sudo] in front of everything the command tries to
send back. `-s` (silent) disables this behaviour.

Conversely, if you feel /sudo does not spam you enough, you can specify `-v`
to get some extra output.

If `<command>` contains words beginning with a `-` character the `--` is
required or they could under certain circumstances be parsed as options.

Permissions
-----------

* `sudo.*` - everything
* `sudo.name` - /sudo -u <user>
* `sudo.op` - /sudo
* `sudo.permission` - /sudo -p
