package me.RyanWild.OnlineTimeLogger;

import java.io.IOException;
import java.util.ArrayList;

public class UserManager
{
  private static ArrayList<User> users = new ArrayList();

  public static boolean containsUser(String username) {
    return getUser(username) != null;
  }

  public static User getUser(String username) {
    for (User u : getUsers())
      if (u.getUsername().equalsIgnoreCase(username)) return u;
    return null;
  }

  public static void saveUsers() {
    for (User u : getUsers()) {
      u.saveToConfig();
    }
    OnlineTimeLogger.instance.saveConfig();
  }

  public static void backUpIfNeeded() {
    for (User u : getUsers()) {
      if (u.needBackUP()) try {
          u.backUp();
        } catch (IOException e) {
          e.printStackTrace();
        }
    }

    saveUsers();
  }

  public static void addUser(String name, long currentTimeMillis) {
    User u = new User(name);
    u.setLoggedInTime(currentTimeMillis);

    getUsers().add(u);
  }

  public static ArrayList<User> getUsers() {
    return users;
  }
}

/* Location:           C:\Users\Ryan\Downloads\OnlineTimeLogger.jar
 * Qualified Name:     me.newboy.OnlineTimeLogger.UserManager
 * JD-Core Version:    0.6.2
 */