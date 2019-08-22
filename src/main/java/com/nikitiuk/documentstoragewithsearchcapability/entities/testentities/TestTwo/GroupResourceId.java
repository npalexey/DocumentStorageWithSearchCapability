package com.nikitiuk.documentstoragewithsearchcapability.entities.testentities.TestTwo;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import java.io.Serializable;
import java.util.Objects;

@Embeddable
public class GroupResourceId implements Serializable {

    @Column(name = "group_id")
    private long groupId;

    @Column(name = "resource_id")
    private long resourceId;

    public GroupResourceId() {
    }

    public GroupResourceId(long groupId, long resourceId) {
        this.groupId = groupId;
        this.resourceId = resourceId;
    }

    public long getGroupId() {
        return groupId;
    }

    public void setGroupId(long groupId) {
        this.groupId = groupId;
    }

    public long getResourceId() {
        return resourceId;
    }

    public void setResourceId(long resourceId) {
        this.resourceId = resourceId;
    }

    @Override
    public int hashCode() {
        return Objects.hash(groupId, resourceId);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;

        if (obj == null || getClass() != obj.getClass())
            return false;

        GroupResourceId that = (GroupResourceId) obj;
        return Objects.equals(groupId, that.groupId) &&
                Objects.equals(resourceId, that.resourceId);
    }
}