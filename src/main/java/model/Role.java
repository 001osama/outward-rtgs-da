package model;

public class Role {
    private int roleID;
    private String roleType;

    public Role(int roleID, String roleType) {
        this.roleID = roleID;
        this.roleType = roleType;
    }
    public Role() {}

    public int getRoleID() {
        return roleID;
    }

    public String getRoleType() {
        return roleType;
    }

}