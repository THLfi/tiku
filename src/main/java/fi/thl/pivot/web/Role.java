package fi.thl.pivot.web;

import java.util.List;

public class Role {

    public static enum Type {
        Master, Regular
    }

    private Type type;
    private String pwd;

    public Role(Type type, String pwd) {
        this.type = type;
        this.pwd = pwd;
    }

    public boolean matches(List<String> passwords) {
        if (Type.Master.equals(type)) {
            return true;
        } else {
            return passwords.contains(pwd);
        }
    }

}
