package model;

public class Role {
    private int roleId;
    private String roleType;

    public Role(int roleId, String roleType) {
        this.roleId = roleId;
        this.roleType = roleType;
    }
    public Role() {}

    public int getRoleId() {
        return roleId;
    }

    public String getRoleType() {
        return roleType;
    }
}