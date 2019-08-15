package com.nikitiuk.documentstoragewithsearchcapability.entities;

import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "Permission_groups")
public class GroupBean {

    @Id
    @GeneratedValue(generator = "native")
    @GenericGenerator(name = "native", strategy = "native")
    @Column(name = "group_id", unique = true, updatable = false, nullable = false)
    private int id;

    @Column(name = "group_name")
    private String name;

    @Column(name = "group_permissions")
    private String permissions;

    @ManyToMany(cascade = { CascadeType.PERSIST, CascadeType.MERGE }, fetch= FetchType.LAZY)
    @Cascade(org.hibernate.annotations.CascadeType.SAVE_UPDATE)
    @JoinTable(
            name = "User_groups_binding",
            joinColumns = { @JoinColumn(name = "group_id") },
            inverseJoinColumns = { @JoinColumn(name = "user_id") }
    )
    Set<UserBean> users = new HashSet<>();

    public GroupBean(String name, String permissions) {
        this.name = name;
        this.permissions = permissions;
    }

    public GroupBean(){

    }

    public Set<UserBean> getUsers() {
        return users;
    }

    public void setUsers(Set<UserBean> users) {
        this.users = users;
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
        return "Group [gourp_id=" + id + ", group_name=" + name + ", group_permissions=" + permissions + "]";
    }

    public Boolean equals(GroupBean otherGroupBean){
        return this.getName().equals(otherGroupBean.getName());
    }
}
