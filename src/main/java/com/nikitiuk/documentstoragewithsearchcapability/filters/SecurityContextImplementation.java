package com.nikitiuk.documentstoragewithsearchcapability.filters;

import com.nikitiuk.documentstoragewithsearchcapability.entities.GroupBean;
import com.nikitiuk.documentstoragewithsearchcapability.entities.UserBean;
import org.hibernate.Hibernate;

import javax.ws.rs.core.SecurityContext;
import java.security.Principal;

public class SecurityContextImplementation implements SecurityContext {
    private UserBean user;
    private String scheme;

    public SecurityContextImplementation(UserBean user, String scheme) {
        this.user = user;
        this.scheme = scheme;
    }

    public UserBean getUser(){
        Hibernate.initialize(user);
        Hibernate.initialize(user.getGroups());
        return user;
    }

    @Override
    public Principal getUserPrincipal() {return this.user;}

    @Override
    public boolean isUserInRole(String s) {
        Hibernate.initialize(user.getGroups());
        if (user.getGroups() != null) {
            for(GroupBean groupBean : user.getGroups()){
                if(groupBean.getName().equals(s)){
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public boolean isSecure() {return "https".equals(this.scheme);}

    @Override
    public String getAuthenticationScheme() {
        return SecurityContext.BASIC_AUTH;
    }
}