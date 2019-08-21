package com.nikitiuk.documentstoragewithsearchcapability.entities.helpers;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import java.io.Serializable;
import java.util.Objects;

@Embeddable
public class DocGroupPermissionsId implements Serializable {

    //@Column(name = "comb_group_id")
    private Long groupId;

    //@Column(name = "comb_document_id")
    private Long documentId;

    public DocGroupPermissionsId() {
    }

    public DocGroupPermissionsId(
            Long groupId,
            Long documentId) {
        this.groupId = groupId;
        this.documentId = documentId;
    }

    public Long getGroupId() {
        return groupId;
    }

    public void setGroupId(Long groupId) {
        this.groupId = groupId;
    }

    public Long getDocumentId() {
        return documentId;
    }

    public void setDocumentId(Long documentId) {
        this.documentId = documentId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass())
            return false;
        DocGroupPermissionsId that = (DocGroupPermissionsId) o;
        return Objects.equals(groupId, that.groupId) && Objects.equals(documentId, that.documentId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(groupId, documentId);
    }
}
