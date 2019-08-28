package com.nikitiuk.documentstoragewithsearchcapability.entities.helpers;

import com.nikitiuk.documentstoragewithsearchcapability.entities.GroupBean;

import javax.security.auth.Subject;
import java.security.Principal;
import java.util.Set;

public class UserPrincipal implements Principal {

    private String name;
    private Set<GroupBean> groups;

    /**
     * Returns the name of this principal.
     *
     * @return the name of this principal.
     */
    @Override
    public String getName() {
        return null;
    }
}
