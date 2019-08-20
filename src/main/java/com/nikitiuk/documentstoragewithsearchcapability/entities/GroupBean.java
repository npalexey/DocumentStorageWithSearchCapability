package com.nikitiuk.documentstoragewithsearchcapability.entities;

import com.nikitiuk.documentstoragewithsearchcapability.filters.Permissions;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.NaturalId;
import org.hibernate.annotations.NaturalIdCache;

import javax.persistence.*;
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

    @ManyToMany(mappedBy = "groups")//, fetch = FetchType.LAZY)
            //@OrderBy("name ASC")
            Set<UserBean> users = new HashSet<>();

    @Id
    @GeneratedValue(generator = "native")
    @GenericGenerator(name = "native", strategy = "native")
    @Column(name = "group_id", unique = true, updatable = false, nullable = false)
    private long id;

    @NaturalId
    @Column(name = "group_name", unique = true, nullable = false)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(name = "group_permissions")
    private Permissions permissions;

    public GroupBean(String name, String permissions) {
        this.name = name;
        if (permissions != null && Permissions.contains(permissions)) {
            this.permissions = Permissions.valueOf(permissions);
        } else {
            this.permissions = null;
        }
    }

    public GroupBean(String name, Permissions permissions) {
        this.name = name;
        this.permissions = permissions;
    }

    public GroupBean() {

    }

    public Set<UserBean> getUsers() {
        return users;
    }

    public void setUsers(Set<UserBean> users) {
        this.users = users;
    }

    public Permissions getPermissions() {
        return permissions;
    }

    public void setPermissions(Permissions permissions) {
        this.permissions = permissions;
    }

    public void setPermissions(String permissions) {
        if (permissions != null && Permissions.contains(permissions)) {
            this.permissions = Permissions.valueOf(permissions);
        } else {
            this.permissions = null;
        }
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
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
    public boolean equals(Object o) {
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
