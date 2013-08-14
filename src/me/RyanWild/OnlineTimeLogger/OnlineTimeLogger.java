package me.RyanWild.OnlineTimeLogger;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.java.JavaPlugin;

public final class OnlineTimeLogger extends JavaPlugin
        implements Listener
{

    public static OnlineTimeLogger instance;

    public static boolean isMYSQL;

    private static Connection mysql;

    public void onEnable()
    {
        instance = this;
        getLogger().info(getDescription().getVersion() + " has been enabled.");
        getServer().getPluginManager().registerEvents(this, this);
        reloadConfig();
        saveConfig();
        getConfig().options().copyDefaults(true);
        saveConfig();

        if (!getConfig().contains("backup-after-seconds"))
        {
            getConfig().set("backup-after-seconds", Integer.valueOf(60));
            saveConfig();
        }

        if (!getConfig().contains("show-messages"))
        {
            getConfig().set("show-messages", Boolean.valueOf(true));
            saveConfig();
        }

        isMYSQL = getConfig().getBoolean("mysql.enabled", false);

        if (isMYSQL)
        {
            if (ConnectDatabase())
            {
                DoUpdate("CREATE TABLE `%t%` (`id` int(10) NOT NULL AUTO_INCREMENT,`name` varchar(32) NOT NULL,`ontime` int(20) NOT NULL,`startdate` datetime NOT NULL,`enddate` datetime NOT NULL,PRIMARY KEY (`id`)) ENGINE=InnoDB DEFAULT CHARSET=latin1", "logs");
            }
            else
            {
                isMYSQL = false;
            }
        }

        File bfolder = new File(getDataFolder() + "/backup");
        if (!bfolder.exists())
        {
            bfolder.mkdir();
        }

        Player[] players = getServer().getOnlinePlayers();

        for (int i = 0; i < players.length; i++)
        {
            prepareUser(players[i].getName());
        }
        getServer().getScheduler().scheduleAsyncRepeatingTask(this, new Runnable()
        {
            public void run()
            {
                for (User u : UserManager.getUsers())
                {
                    Player p = OnlineTimeLogger.instance.getServer().getPlayerExact(u.getUsername());
                    if ((p != null) && (p.isOnline()))
                    {
                        u.setOnTime(u.getOnTime() + 1L);
                        u.setOnTimeFromCountingFrom(u.getOnTimeFromCountingFrom() + 1L);
                    }
                }

                UserManager.saveUsers();
                UserManager.backUpIfNeeded();
                OnlineTimeLogger.instance.saveConfig();
            }

        }, 0L, 20L);
    }

    public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args)
    {
        if ((cmd.getName().equalsIgnoreCase("otl")) && (sender.hasPermission("onlinetimelogger.admin")))
        {
            if ((args.length == 0) || ((args.length == 1) && (args[0].equalsIgnoreCase("help"))))
            {
                sender.sendMessage("OnlineTimeLogger");
                sender.sendMessage("/otl stats <name> >> Show the player online time stats.");
                sender.sendMessage("/otl help >> Shows this.");
            }
            else if ((args.length == 2) && (args[0].equalsIgnoreCase("stats")))
            {
                Player p = getServer().getPlayerExact(args[1]);
                if ((p == null) || (!p.isOnline()))
                {
                    sender.sendMessage("Player not found.");
                }
                else
                {
                    long sc = UserManager.getUser(p.getName()).getOnTime();
                    int hours = (int) sc / 3600;
                    sc -= hours * 3600;
                    int minutes = (int) sc / 60;
                    sc -= minutes * 60;
                    int seconds = (int) sc;
                    sender.sendMessage(p.getName() + " was online for " + hours + " hours " + minutes + " minutes " + seconds + " seconds on this server.");
                }
            }
            else if ((args.length == 1) && (args[0].equalsIgnoreCase("reload")))
            {
                sender.sendMessage("Config reloaded.");
                reloadConfig();
                saveConfig();
                UserManager.saveUsers();
                UserManager.backUpIfNeeded();
            }
        }

        return true;
    }

    public void onDisable()
    {
        getLogger().info(getDescription().getVersion() + " has been disabled.");
    }

    public void prepareUser(String name)
    {
        if (!UserManager.containsUser(name))
        {
            UserManager.addUser(name, System.currentTimeMillis());
        }
        else
        {
            UserManager.getUser(name).setLoggedInTime(System.currentTimeMillis());
        }
        if (UserManager.getUser(name).getCountingFrom() == -1L)
        {
            UserManager.getUser(name).setCountingFrom(System.currentTimeMillis());
        }
    }

    @EventHandler
    public void playerConnect(PlayerJoinEvent e)
    {
        prepareUser(e.getPlayer().getName());
    }

    private static boolean ConnectDatabase()
    {
        String server = instance.getConfig().getString("mysql.server");
        String user = instance.getConfig().getString("mysql.user");
        String password = instance.getConfig().getString("mysql.password");
        String port = instance.getConfig().getString("mysql.port");
        String db = instance.getConfig().getString("mysql.db");
        String url = "jdbc:mysql://" + server + ":" + port + "/" + db;
        instance.getLogger().info("Connecting to database...");
        try
        {
            mysql = DriverManager.getConnection(url, user, password);
            instance.getLogger().info("Connected to the database.");
            return true;
        }
        catch (SQLException ex)
        {
            instance.getLogger().info("Failed connecting to database.");
        }
        return false;
    }

    public static ResultSet DoQuery(String q, String table)
    {
        ResultSet rs = null;
        try
        {
            Statement st = mysql.createStatement();
            rs = st.executeQuery(q.replace("%t%", instance.getConfig().getString("mysql.prefix") + table));
        }
        catch (SQLException ex)
        {
            ConnectDatabase();
            instance.getLogger().info(ex.getMessage());
        }

        return rs;
    }

    public static void DoUpdate(String q, String table)
    {
        try
        {
            Statement st = mysql.createStatement();
            st.executeUpdate(q.replace("%t%", instance.getConfig().getString("mysql.prefix") + table));
            st.close();
        }
        catch (SQLException ex)
        {
            ConnectDatabase();
            instance.getLogger().info(ex.getMessage());
        }
    }

}

/* Location:           C:\Users\Ryan\Downloads\OnlineTimeLogger.jar
 * Qualified Name:     me.newboy.OnlineTimeLogger.OnlineTimeLogger
 * JD-Core Version:    0.6.2
 */