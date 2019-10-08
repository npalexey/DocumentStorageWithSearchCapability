package com.nikitiuk.documentstoragewithsearchcapability.security;

import com.nikitiuk.documentstoragewithsearchcapability.entities.GroupBean;

import javax.ws.rs.core.SecurityContext;

public class SecurityContextImplementation implements SecurityContext {

    private UserPrincipal user;
    private String scheme;

    public SecurityContextImplementation(UserPrincipal user, String scheme) {
        this.user = user;
        this.scheme = scheme;
    }

    @Override
    public UserPrincipal getUserPrincipal() {
        return this.user;
    }

    @Override
    public boolean isUserInRole(String s) {
        if (user.getGroups() != null) {
            for (GroupBean groupBean : user.getGroups()) {
                if (groupBean.getName().equals(s)) {
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public boolean isSecure() {
        return "https".equals(this.scheme);
    }

    @Override
    public String getAuthenticationScheme() {
        return SecurityContext.BASIC_AUTH;
    }
}