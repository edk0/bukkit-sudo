package uk.co.edk141.sudo;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Server;
import org.bukkit.command.CommandSender;
import org.bukkit.permissions.PermissibleBase;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.ServerOperator;

public class SudoCommandSender extends PermissibleBase implements CommandSender
{
    private CommandSender base;
    private String name;
    private boolean permissionExtend;
    private String prefix;
    
    private static class SudoServerOperator implements ServerOperator
    {        
        @Override
        public boolean isOp()
        {
            return true;
        }
        
        @Override
        public void setOp(boolean value) {}
    }
    
    public SudoCommandSender(String name, CommandSender base, boolean permissionExtend, boolean silent)
    {
        super(new SudoServerOperator());
        this.base = base;
        this.name = name;
        this.permissionExtend = permissionExtend;
        
        if (silent) {
            this.prefix = "";
        } else if (name.equals(base.getName())) {
            this.prefix = ChatColor.GRAY + "[sudo] " + ChatColor.RESET;
        } else {
            this.prefix = ChatColor.GRAY + "[sudo/" + name + "] " + ChatColor.RESET;
        }
    }
    
    @Override
    public String getName()
    {
        return name;
    }
    
    @Override
    public void sendMessage(String string)
    {
        base.sendMessage(prefix + string);
    }
    
    @Override
    public void sendMessage(String[] strings)
    {
        String[] strings2 = new String[strings.length];
        for (int i = 0; i < strings.length; ++i) {
            strings2[i] = prefix + strings[i];
        }
        base.sendMessage(strings2);
    }
    
    @Override
    public boolean hasPermission(String name)
    {
        return permissionExtend ? true : (super.hasPermission(name) || base.hasPermission(name));
    }
    
    @Override
    public boolean hasPermission(Permission perm)
    {
        return permissionExtend ? true : (super.hasPermission(perm) || base.hasPermission(perm));
    }
    
    @Override
    public Server getServer()
    {
        return Bukkit.getServer();
    }
}
