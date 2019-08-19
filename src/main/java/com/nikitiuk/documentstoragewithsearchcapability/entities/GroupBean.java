package com.nikitiuk.documentstoragewithsearchcapability.entities;

import org.hibernate.annotations.*;

import javax.persistence.*;
import javax.persistence.Entity;
import javax.persistence.Table;
import java.io.Serializable;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

@Entity
@Table(name = "Permission_groups")
@org.hibernate.annotations.Cache(
        usage = CacheConcurrencyStrategy.READ_WRITE
)
@NaturalIdCache
public class GroupBean implements Serializable {

    @Id
    @GeneratedValue(generator = "native")
    @GenericGenerator(name = "native", strategy = "native")
    @Column(name = "group_id", unique = true, updatable = false, nullable = false)
    private int id;

    @NaturalId
    @Column(name = "group_name", unique = true, nullable = false)
    private String name;

    @Column(name = "group_permissions")
    private String permissions;

    @ManyToMany(mappedBy = "groups")//, fetch = FetchType.LAZY)
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

    @Override
    public boolean equals(Object o){
        if (this == o) return true;
        if (o == null || getClass() != o.getClass())
            return false;
        GroupBean groupBean = (GroupBean) o;
        return Objects.equals(name, groupBean.name);
        //return this.getName().equals(o.getName());
    }

    @Override
    public int hashCode() {
        return Objects.hash(name);
    }
}
