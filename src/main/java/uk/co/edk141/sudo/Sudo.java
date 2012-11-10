package uk.co.edk141.sudo;

import java.util.Arrays;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;
import org.apache.commons.lang.StringUtils;

public class Sudo extends JavaPlugin implements Listener {
    @Override
    public void onDisable() {
    }
    
    @Override
    public void onEnable() {
        getServer().getPluginManager().registerEvents(this, this);
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
        String cmdline = StringUtils.join(Arrays.copyOfRange(args, opts_end, args.length), " ");
        Bukkit.dispatchCommand(new SudoCommandSender(user, sender, use_pe, silent), cmdline);
        
        return true;
    }
}

