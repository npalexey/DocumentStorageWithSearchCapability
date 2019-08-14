package com.nikitiuk.documentstoragewithsearchcapability.entities;

import javax.persistence.*;

@Entity
@Table(name = "permissiongroups")
public class GroupBean {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", unique = true)
    private int id;

    @Column(name = "name")
    private String name;

    @Column(name = "permissions")
    private String permissions;

    public GroupBean(String name, String permissions) {
        this.name = name;
        this.permissions = permissions;
    }

    public GroupBean(){

    }

    public String getPermissions() {
        return permissions;
    }

    public void setPermissions(String permissions) {
        this.permissions = permissions;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return "User [id=" + id + ", GroupName=" + name + ", Permissions=" +"]";
    }

    public Boolean equals(GroupBean otherGroupBean){
        return this.getName().equals(otherGroupBean.getName());
    }
}
