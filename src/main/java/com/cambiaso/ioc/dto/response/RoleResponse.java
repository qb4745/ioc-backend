package com.cambiaso.ioc.dto.response;

import java.util.List;

public class RoleResponse {
    private int id;
    private String name;
    private String description;
    private int usersCount; // optional; default 0
    private List<String> permissions; // optional

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public int getUsersCount() { return usersCount; }
    public void setUsersCount(int usersCount) { this.usersCount = usersCount; }

    public List<String> getPermissions() { return permissions; }
    public void setPermissions(List<String> permissions) { this.permissions = permissions; }
}

