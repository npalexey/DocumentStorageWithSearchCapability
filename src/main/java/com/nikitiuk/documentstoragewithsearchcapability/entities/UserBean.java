package com.nikitiuk.documentstoragewithsearchcapability.entities;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "users")
public class UserBean {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", unique = true)
    private int id;

    @Column(name = "name")
    private String name;

    @Column(name = "usergroup")
    private String group;

    public UserBean(String name, String group) {
        this.name = name;
        this.group = group;
    }

    public UserBean() {

    }

    public String getGroup() {
        return group;
    }

    public void setGroup(String group) {
        this.group = group;
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
        return "User [id=" + id + ", Name=" + name + ", Group=" + "]";
    }

    public Boolean equals(UserBean otherUserBean) {
        return this.getName().equals(otherUserBean.getName()) && this.getGroup().equals(otherUserBean.getGroup());
    }
}
