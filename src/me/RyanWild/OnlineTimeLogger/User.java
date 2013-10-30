package me.RyanWild.OnlineTimeLogger;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;
import java.util.logging.Level;
import org.bukkit.entity.Player;

public class User
{

    private String username;

    private long onTime = 0L;

    private long onTimeFromCountingFrom = 0L;

    private long countingFrom = -1L;

    private long loggedInTime = -1L;

    public User(String name)
    {
        this.username = name;
        loadFromConfig();
    }

    public void loadFromConfig()
    {
        setOnTime(OnlineTimeLogger.instance.getConfig().getLong(this.username + ".onTime", 0L));
        setCountingFrom(OnlineTimeLogger.instance.getConfig().getLong(this.username + ".countingFrom", -1L));
        setOnTimeFromCountingFrom(OnlineTimeLogger.instance.getConfig().getLong(this.username + ".onTimeFromCountingFrom", 0L));
    }

    public void saveToConfig()
    {
        OnlineTimeLogger.instance.getConfig().set(this.username + ".onTime", Long.valueOf(getOnTime()));
        long sc = getOnTime();
        int hours = (int) sc / 3600;
        sc -= hours * 3600;
        int minutes = (int) sc / 60;
        sc -= minutes * 60;
        int seconds = (int) sc;
        OnlineTimeLogger.instance.getConfig().set(this.username + ".onTimeReadable", hours + ":" + minutes + ":" + seconds);
        OnlineTimeLogger.instance.getConfig().set(this.username + ".countingFrom", Long.valueOf(getCountingFrom()));
        OnlineTimeLogger.instance.getConfig().set(this.username + ".countingFromReadable", new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date(getCountingFrom())));
        OnlineTimeLogger.instance.getConfig().set(this.username + ".onTimeFromCountingFrom", Long.valueOf(getOnTimeFromCountingFrom()));
    }

    public void backUp() throws IOException
    {
        if (OnlineTimeLogger.instance.getConfig().getBoolean("show-messages"))
        {
            OnlineTimeLogger.instance.getLogger().log(Level.INFO, "Backing up {0}", this.username);
        }

        Date d1 = new Date(getCountingFrom());
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        SimpleDateFormat sdf2 = new SimpleDateFormat("HH:mm:ss");
        sdf2.setTimeZone(TimeZone.getTimeZone("UTC"));

        long sc = getOnTimeFromCountingFrom() - 1L;
        int hours = (int) sc / 3600;
        sc -= hours * 3600;
        int minutes = (int) sc / 60;
        sc -= minutes * 60;
        int seconds = (int) sc;
        String msg = this.username + " was on for " + hours + " hours " + minutes + " minutes " + seconds + " seconds. Recording from " + sdf.format(d1) + " to " + sdf.format(new Date(getCountingFrom() + OnlineTimeLogger.instance.getConfig().getLong("backup-after-seconds") * 1000L));

        if (!OnlineTimeLogger.isMYSQL)
        {
            File datafolder = OnlineTimeLogger.instance.getDataFolder();
            File backupFile = new File(datafolder.getAbsolutePath() + "/backup/" + getUsername() + ".txt");
            if (!backupFile.exists())
            {
                backupFile.createNewFile();
            }
            FileWriter fw = new FileWriter(backupFile, true);
            BufferedWriter bw = new BufferedWriter(fw);
            bw.write(msg);
            bw.newLine();
            bw.close();
        }
        else
        {
            OnlineTimeLogger.DoUpdate("INSERT INTO %t% SET name='" + this.username + "', ontime='" + (getOnTimeFromCountingFrom() - 1L) + "', startdate='" + sdf.format(d1) + "', enddate='" + sdf.format(new Date(getCountingFrom() + OnlineTimeLogger.instance.getConfig().getLong("backup-after-seconds") * 1000L)) + "'", "logs");
        }

        setCountingFrom(System.currentTimeMillis());
        setOnTimeFromCountingFrom(0L);
    }

    public boolean needBackUP()
    {
        if (!OnlineTimeLogger.instance.getConfig().getBoolean("backup-enabled"))
        {
            return false;
        }
        Player player = OnlineTimeLogger.instance.getServer().getPlayerExact(this.username);
        if (player != null)
        {
            if ((OnlineTimeLogger.instance.getConfig().getInt("backup-type") == 0) && (!OnlineTimeLogger.instance.getServer().getPlayerExact(this.username).hasPermission("onlinetimelogger.backup")))
            {
                return false;
            }
            if ((OnlineTimeLogger.instance.getConfig().getInt("backup-type") == 1) && (OnlineTimeLogger.instance.getServer().getPlayerExact(this.username).hasPermission("onlinetimelogger.backup")))
            {
                return false;
            }
        }
        return System.currentTimeMillis() - getCountingFrom() >= OnlineTimeLogger.instance.getConfig().getLong("backup-after-seconds") * 1000L;
    }

    public String getUsername()
    {
        return this.username;
    }

    public void setUsername(String username)
    {
        this.username = username;
    }

    public long getCountingFrom()
    {
        return this.countingFrom;
    }

    public void setCountingFrom(long countingFrom)
    {
        this.countingFrom = countingFrom;
    }

    public long getOnTimeFromCountingFrom()
    {
        return this.onTimeFromCountingFrom;
    }

    public void setOnTimeFromCountingFrom(long onTimeFromCountingFrom)
    {
        this.onTimeFromCountingFrom = onTimeFromCountingFrom;
    }

    public long getOnTime()
    {
        return this.onTime;
    }

    public void setOnTime(long onTime)
    {
        this.onTime = onTime;
    }

    public void setLoggedInTime(long l)
    {
        this.loggedInTime = l;
    }

    public long getLoggedInTime()
    {
        return this.loggedInTime;
    }

}

/* Location:           C:\Users\Ryan\Downloads\OnlineTimeLogger.jar
 * Qualified Name:     me.newboy.OnlineTimeLogger.User
 * JD-Core Version:    0.6.2
 */