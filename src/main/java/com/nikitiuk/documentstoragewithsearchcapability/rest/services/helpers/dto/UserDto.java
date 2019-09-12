package com.nikitiuk.documentstoragewithsearchcapability.rest.services.helpers.dto;

import com.nikitiuk.documentstoragewithsearchcapability.entities.GroupBean;

import java.util.List;

public class UserDto {

    private String name;

    private String password;

    private List<GroupBean> groups;
}