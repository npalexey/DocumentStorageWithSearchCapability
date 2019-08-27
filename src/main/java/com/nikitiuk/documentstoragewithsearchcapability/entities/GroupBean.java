package com.nikitiuk.documentstoragewithsearchcapability.entities;

import com.nikitiuk.documentstoragewithsearchcapability.entities.helpers.Permissions;
import org.hibernate.Hibernate;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.*;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.*;
import java.io.Serializable;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Objects;
import java.util.Set;

@Entity
@Table(name = "Permission_groups")
@NaturalIdCache
@Cache(
        usage = CacheConcurrencyStrategy.READ_WRITE
)
public class GroupBean implements Serializable {

    @ManyToMany(mappedBy = "groups", cascade = {CascadeType.PERSIST, CascadeType.MERGE})//, fetch = FetchType.LAZY)
    //@OrderBy("name ASC")
    @OnDelete(action = OnDeleteAction.CASCADE)
    Set<UserBean> users = new HashSet<>();

    @OneToMany(
            mappedBy = "group",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    private Set<DocGroupPermissions> documentsPermissions = new HashSet<>();

    @Id
    @GeneratedValue(generator = "native")
    @GenericGenerator(name = "native", strategy = "native")
    @Column(name = "id", unique = true, updatable = false, nullable = false)
    private Long id;

    @NaturalId
    @Column(name = "group_name", unique = true, nullable = false)
    private String name;

    public GroupBean(String name) {
        this.name = name;
    }

    public GroupBean() {

    }

    public Set<UserBean> getUsers() {
        return users;
    }

    public void setUsers(Set<UserBean> users) {
        this.users = users;
        for (UserBean user : users){
            user.getGroups().add(this);
        }
    }

    public Set<DocGroupPermissions> getDocumentsPermissions() {
        return documentsPermissions;
    }

    public void setDocumentsPermissions(Set<DocGroupPermissions> documentsPermissions) {
        this.documentsPermissions = documentsPermissions;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void addDocument(DocBean document, Permissions permissions) {
        if (checkIfGroupHasDocument(document)) {
            return;
        }
        DocGroupPermissions docGroupPermissions = new DocGroupPermissions(this, document);
        docGroupPermissions.setPermissions(permissions);
        documentsPermissions.add(docGroupPermissions);
    }

    public void updateDocument(DocBean document, Permissions permissions) {
        for (DocGroupPermissions docGroupPermissions : documentsPermissions) {
            if (docGroupPermissions.getGroup().equals(this) &&
                    docGroupPermissions.getDocument().equals(document)) {
                docGroupPermissions.setPermissions(permissions);
            }
        }
    }

    public void removeDocument(DocBean document) {
        for (Iterator<DocGroupPermissions> iterator = documentsPermissions.iterator();
             iterator.hasNext(); ) {
            DocGroupPermissions docGroupPermissions = iterator.next();
            if (docGroupPermissions.getGroup().equals(this) &&
                    docGroupPermissions.getDocument().equals(document)) {
                iterator.remove();
                docGroupPermissions.setGroup(null);
                docGroupPermissions.setDocument(null);
            }
        }
    }

    public void addUser(UserBean user) {
        this.users.add(user);
        user.getGroups().add(this);
    }

    public void removeUser(UserBean user) {
        this.users.remove(user);
        user.getGroups().remove(this);
    }

    public boolean checkIfGroupHasDocument(DocBean document) {
        for (DocGroupPermissions docGroupPermissions : documentsPermissions) {
            if (docGroupPermissions.getGroup().equals(this) &&
                    docGroupPermissions.getDocument().equals(document)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public String toString() {
        return "Group [gourp_id=" + id + ", group_name=" + name + ", group_permissions=" + documentsPermissions.toString() + "]";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass())
            return false;
        GroupBean groupBean = (GroupBean) o;
        return Objects.equals(name, groupBean.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name);
    }
}