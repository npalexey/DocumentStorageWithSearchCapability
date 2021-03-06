package com.nikitiuk.documentstoragewithsearchcapability.security;

import com.nikitiuk.documentstoragewithsearchcapability.entities.GroupBean;

import java.security.Principal;
import java.util.Set;

public class UserPrincipal implements Principal {

    private Long id;
    private String name;
    private Set<GroupBean> groups;

    public UserPrincipal(String name) {
        this.name = name;
    }

    public UserPrincipal() {

    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Set<GroupBean> getGroups() {
        return groups;
    }

    public void setGroups(Set<GroupBean> groups) {
        this.groups = groups;
    }

    @Override
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}