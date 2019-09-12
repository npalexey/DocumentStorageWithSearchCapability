package com.nikitiuk.documentstoragewithsearchcapability.entities.helpers;

import com.nikitiuk.documentstoragewithsearchcapability.entities.FolderBean;
import com.nikitiuk.documentstoragewithsearchcapability.entities.UserBean;
import com.nikitiuk.documentstoragewithsearchcapability.rest.services.helpers.dto.FolderDto;
import com.nikitiuk.documentstoragewithsearchcapability.security.UserPrincipal;

public class DtoDaoTransformer {

    public UserBean userPrincipalToUserBean(UserPrincipal userPrincipal) {
        UserBean userBean = new UserBean();
        userBean.setId(userPrincipal.getId());
        userBean.setName(userPrincipal.getName());
        userBean.setGroups(userPrincipal.getGroups());
        return userBean;
    }

    public FolderBean folderDtoToFolderBean(FolderDto folderDto) {
        FolderBean folderBean = new FolderBean();
        folderBean.setId(folderDto.getId());
        folderBean.setPath(folderDto.getPath());
        return folderBean;
    }
}
