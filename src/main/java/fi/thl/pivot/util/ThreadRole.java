package fi.thl.pivot.util;

import fi.thl.pivot.web.Role;

public class ThreadRole {

    private static final ThreadLocal<Role> pwd = new ThreadLocal<>();
    private static final ThreadLocal<String> language = new ThreadLocal<>();

    public static void setRole(Role role) {
        pwd.set(role);
    }

    public static Role getRole() {
        return pwd.get();
    }

    public static boolean isAuthenticated() {
        return pwd.get() != null;
    }

    public static void setLanguage(String string) {
        language.set(string);
    }

    public static String getLanguage() {
        String l = language.get();
        return l == null ? "fi" : l;
    }

}
