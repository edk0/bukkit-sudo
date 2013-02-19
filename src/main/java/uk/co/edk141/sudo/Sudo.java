package uk.co.edk141.sudo;

import java.util.Arrays;

import org.apache.commons.lang.StringUtils;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandException;
import org.bukkit.command.CommandSender;
import org.bukkit.event.Listener;
import org.bukkit.event.server.ServerCommandEvent;
import org.bukkit.plugin.java.JavaPlugin;

public class Sudo extends JavaPlugin implements Listener {
    @Override
    public void onDisable() {
    }
    
    @Override
    public void onEnable() {
        getServer().getPluginManager().registerEvents(this, this);
    }
    
    public String preprocessCommand(CommandSender sender, String command) {
        ServerCommandEvent event = new ServerCommandEvent(sender, command);
        Bukkit.getPluginManager().callEvent(event);
        return event.getCommand();
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        // deny chained sudo calls
        if (sender instanceof SudoCommandSender) {
            sender.sendMessage(ChatColor.RED + "sudo sudo sudo sudo sudo");
            return true;
        }
        
        // don't handle anything except /sudo
        if (!cmd.getName().equalsIgnoreCase("sudo")) {
            return false;
        }
        
        // what mode are we in?
        boolean use_pe = false;
        boolean silent = false;
        boolean verbose = false;
        String user = null;
        boolean need_user = false;
        int opts_end = 0;
        for (String s : args) {
            ++opts_end;
            if (need_user) {
                user = s;
                need_user = false;
                continue;
            }
            if (!s.startsWith("-")) {
                --opts_end;
                break;
            }
            if (s.equals("--")) {
                break;
            }
            char[] cs = s.substring(1).toCharArray();
            for (char c : cs) {
                switch (c) {
                    case 'p':
                        use_pe = true;
                        break;
                    case 's':
                        silent = true;
                        break;
                    case 'u':
                        if (need_user || user != null) {
                            sender.sendMessage("User must only be specified once");
                            return true;
                        }
                        need_user = true;
                        break;
                    case 'v':
                        verbose = true;
                        break;
                    default:
                        sender.sendMessage("Invalid option " + c);
                        return true;
                }
            }
        }
        
        // is there anything left to run?
        if (args.length - opts_end <= 0) {
            return false;
        }
        
        // lol
        if (verbose && silent) {
            sender.sendMessage(ChatColor.RED + "" + ChatColor.BOLD + "ATTEMPTING TO ENABLE SILENT MODE");
            sender.sendMessage(ChatColor.RED + "" + ChatColor.BOLD + "TRYING...");
            sender.sendMessage(ChatColor.RED + "" + ChatColor.BOLD + "TRYING HARDER...");
            sender.sendMessage(ChatColor.RED + "" + ChatColor.BOLD + "Failed: java.lang." + ChatColor.MAGIC +
                    "-------" + ChatColor.RESET + "" + ChatColor.RED + "" + ChatColor.BOLD + "Exception: make up your mind");
            sender.sendMessage(ChatColor.GRAY + "[sudo] (I can't be verbose and silent at the same time)");
            return true;
        }
        
        // check permissions
        if (user != null) {
            if (!sender.hasPermission("sudo.name")) {
                sender.sendMessage("You don't have permission for -u");
                return true;
            }
        } else {
            user = sender.getName();
        }
        if (use_pe && !sender.hasPermission("sudo.permission")) {
            sender.sendMessage("You don't have permission for -p");
            return true;
        }
        
        // do the actual thing
        String[] sudoCommand = Arrays.copyOfRange(args, opts_end, args.length);
        String cmdline = StringUtils.join(sudoCommand, " ");
        
        // verbose debug output for pre-execute
        if (verbose) {
            String as_str = "";
            if (use_pe) as_str += " with extended permissions";
            if (!user.equals(sender.getName())) as_str += " as " + ChatColor.GREEN + user + ChatColor.RESET;
            sender.sendMessage(ChatColor.GRAY + "[sudo] Executing " + ChatColor.RESET + cmdline + ChatColor.GRAY + as_str);
        }
        
        // create sender
        SudoCommandSender spoofed = new SudoCommandSender(user, sender, use_pe, silent);

        // give things like CommandHelper a chance to intercept the command
        cmdline = preprocessCommand(spoofed, cmdline);
        
        // I don't know of a way to make Bukkit give you back its error string, so catch CommandExceptions
        try {
            Bukkit.dispatchCommand(spoofed, cmdline);
        } catch (CommandException ex) {
            String ex_str = "";
            if (verbose) ex_str += ": " + ChatColor.RED + ex.getCause().toString() + ChatColor.RESET;
            sender.sendMessage(ChatColor.GRAY + "[sudo] Error executing " + ChatColor.RESET + sudoCommand[0] + ChatColor.GRAY + ex_str);
        }
        
        spoofed.clearPermissions();
        
        return true;
    }
}

