package com.nikitiuk.documentstoragewithsearchcapability.entities;

import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "Users")
public class UserBean {

    @Id
    @GeneratedValue(generator = "native")
    @GenericGenerator(name = "native", strategy = "native")
    @Column(name = "user_id", unique = true, updatable = false, nullable = false)
    private int id;

    @Column(name = "user_name")
    private String name;

    /*@Column(name = "user_group")
    private String group;*/

    @ManyToMany(cascade = {CascadeType.PERSIST, CascadeType.MERGE}, mappedBy = "users")
    private Set<GroupBean> groups = new HashSet<>();

    public UserBean(String name) {
        this.name = name;
    }

    public UserBean() {

    }

    public Set<GroupBean> getGroups() {
        return groups;
    }

    public void setGroups(Set<GroupBean> groups) {
        this.groups = groups;
    }

    /*public String getGroup() {
        return group;
    }

    public void setGroup(String group) {
        this.group = group;
    }*/

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
        return "User [user_id=" + id + ", user_name=" + name + ", user_groups=" + groups.toString() + "]";
    }

    public Boolean equals(UserBean otherUserBean) {
        return this.getName().equals(otherUserBean.getName()) && this.getGroups().equals(otherUserBean.getGroups());
    }
}
