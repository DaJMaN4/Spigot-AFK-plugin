package me.afk;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.*;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;

public final class Afk extends JavaPlugin implements Listener {

    public Map<Player, Integer> afktime = new HashMap<Player, Integer>();
    public Map<ItemStack, Integer> items = new HashMap<ItemStack, Integer>();
    public List<Player> plays = new ArrayList<Player>();
    public List<String> w = this.getConfig().getStringList("worlds where kick will not work");
    public List<String> o = this.getConfig().getStringList("afk permissions");
    public String m = this.getConfig().getString("messages.player get kick because afk screen");
    public String h = this.getConfig().getString("messages.before kick");
    public Integer k = this.getConfig().getInt("time before kick");
    public Integer l = this.getConfig().getInt("reminder before kick");


    @Override
    public void onEnable() {
        System.out.println(k);
        System.out.println("Andan");
        // Plugin startup logic
        this.saveDefaultConfig();
        this.getServer().getPluginManager().registerEvents(this, this);
        kick();
    }


    public boolean isPlayerInGroup(Player player) {
        for (int i = 0; i < o.size(); i++) {
            if (player.hasPermission("group." + o.get(i)))
                return true;
        }
        return false;
    }


    public String text(Player player) {
        for (int i = 0; i < o.size(); i++) {
            if (this.getConfig().getStringList("opiekunowie").contains(player.getDisplayName()))
                return ChatColor.translateAlternateColorCodes('&',  "&6&lOpiekun&r " + player.getDisplayName() + " ");
            if (player.hasPermission("group.root"))
                return ChatColor.translateAlternateColorCodes('&', "&4&lRoot&r " +  player.getDisplayName() + " ");
            if (player.hasPermission("group.helper"))
                return ChatColor.translateAlternateColorCodes('&', "&b&lHelper&r " + player.getDisplayName() + " ");
            if (player.hasPermission("group.mod"))
                return ChatColor.translateAlternateColorCodes('&', "&2&lMod&r " + player.getDisplayName() + " ");
            if (player.hasPermission("group.dev"))
                return ChatColor.translateAlternateColorCodes('&', "&1&lDev&r " + player.getDisplayName() + " ");
            if (player.hasPermission("group.admin"))
                return ChatColor.translateAlternateColorCodes('&', "&c&lAdmin&r " + player.getDisplayName() + " ");
        }
        return "error";
    }


    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }


    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (label.equalsIgnoreCase("afk")) {
            if (!(sender instanceof Player)) {
                System.out.println("Command not compatible with console");
                return true;
            }
            Player p = (Player) sender;
            if (plays.contains(p)) {
                return false;
            }
            if (isPlayerInGroup(p)) { //"afk.on";
                plays.add(p);
                getServer().broadcastMessage(text(p) + ChatColor.translateAlternateColorCodes('&',
                        this.getConfig().getString("messages.admin afk on all players")));
                return true;
            }
            else
                sender.sendMessage(ChatColor.translateAlternateColorCodes('&',
                        this.getConfig().getString("messages.player without permission uses /afk")));
        }
        return false;
    }


    @EventHandler
    public void playerjoins(PlayerJoinEvent event) {
        if (!isPlayerInGroup(event.getPlayer())) //"afk.*"
            afktime.put(event.getPlayer(), 0);
    }


    @EventHandler
    public void playerleave(PlayerQuitEvent event) {
        afktime.remove(event.getPlayer());
        plays.remove(event.getPlayer());
    }


    @EventHandler
    public void dontmove(PlayerMoveEvent event) {
        if (this.getConfig().getConfigurationSection("worlds where kick will not work") != null)
            if (Objects.requireNonNull(this.getConfig().getConfigurationSection("worlds where kick will not work")).contains(event.getPlayer().getWorld().getName()))
                return;

        if (plays.contains(event.getPlayer())) { //event.setCancelled(true)
            Player p = event.getPlayer();
            plays.remove(p);
            getServer().broadcastMessage(text(p) + ChatColor.translateAlternateColorCodes('&',
                    this.getConfig().getString("messages.admin afk off all players")));
            return;
        }
        if (!isPlayerInGroup(event.getPlayer()))
            afktime.put(event.getPlayer(), 0);
    }


    @EventHandler
    public void write(PlayerChatEvent event) {
        if (plays.contains(event.getPlayer())) {
            Player p = event.getPlayer();
            plays.remove(p);
            getServer().broadcastMessage(text(p) + ChatColor.translateAlternateColorCodes('&',
                        this.getConfig().getString("messages.admin afk off all players")));
        }
    }

    public void Alagadda() {
        this.getConfig().getConfigurationSection("blocks").getKeys(false).forEach(key -> {
            int position = 0;
            Random r = new Random();
            for (String i : this.getConfig().getStringList("blocks." + key)) {
                ItemStack item;
                try {
                    item = new ItemStack(Material.matchMaterial(i), r.nextInt(16) + 1);  // itemStack first argument is the block and next amount
                } catch (Exception e) {
                    item = new ItemStack(Material.matchMaterial(key));
                }
                items.put(item, 69);
                position++;
            }
        });
    }


    public void kick() {
        new BukkitRunnable() {
            public void run() {
                for (int i = 0; i <= getServer().getWorlds().size() - 1; i++) {
                    if (!w.contains(getServer().getWorlds().get(i).getName())) {
                        getServer().getWorlds().get(i).getPlayers().forEach(key -> {
                            if (afktime.get(key) != null) {
                                if (afktime.get(key) > l)
                                    key.sendMessage(ChatColor.translateAlternateColorCodes('&', h));
                                if (afktime.get(key) > k)
                                    key.kickPlayer(m);
                            }
                            if (afktime.containsKey(key)) {
                                afktime.put(key, afktime.get(key) + 1);
                            }
                        });
                    }
                }
            }
        }.runTaskTimer(this, 0, 20);
    }
}