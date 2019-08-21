package com.nikitiuk.documentstoragewithsearchcapability.entities.TestEntities.TestTwo;

import javax.persistence.*;
import java.util.Objects;

@Entity
@Table(name = "ResourcePermission")
public class ResourcePermission {

    @EmbeddedId
    private GroupResourceId id;

    @Column(name = "permissionType")
    @Enumerated(EnumType.STRING)
    private PermissionType permissionType;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("groupId")
    private Group group;

    @ManyToOne(fetch = FetchType.EAGER)
    @MapsId("resourceId")
    private Resource resource;

    public ResourcePermission() {
    }

    public ResourcePermission(Group group, Resource resource, PermissionType permissionType) {
        this.group = group;
        this.resource = resource;
        this.permissionType = permissionType;
        this.id = new GroupResourceId(group.getId(), resource.getId());
    }

    public GroupResourceId getId() {
        return id;
    }

    public void setId(GroupResourceId id) {
        this.id = id;
    }

    public Group getGroup() {
        return group;
    }

    public void setGroup(Group group) {
        this.group = group;
    }

    public Resource getResource() {
        return resource;
    }

    public void setResource(Resource resource) {
        this.resource = resource;
    }

    public PermissionType getPermissionType() {
        return permissionType;
    }

    public void setPermissionType(PermissionType permissionType) {
        this.permissionType = permissionType;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;

        if (obj == null || getClass() != obj.getClass())
            return false;

        ResourcePermission that = (ResourcePermission) obj;
        return Objects.equals(group, that.group) &&
                Objects.equals(resource, that.resource);
    }

    @Override
    public int hashCode() {
        return Objects.hash(group, resource);
    }
}
