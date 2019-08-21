package com.nikitiuk.documentstoragewithsearchcapability.entities;

import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.NaturalId;
import org.hibernate.annotations.NaturalIdCache;
import org.hibernate.annotations.Cache;

import javax.persistence.*;
import java.util.Objects;

@Entity
@Table(name = "Documents")
@NaturalIdCache
@Cache(
        usage = CacheConcurrencyStrategy.READ_WRITE
)
public class DocBean {

    @Id
    @GeneratedValue(generator = "native")
    @GenericGenerator(name = "native", strategy = "native")
    @Column(name = "id", unique = true, updatable = false, nullable = false)
    private Long id;

    @Column(name = "document_name", nullable = false)
    private String name;

    @NaturalId
    @Column(name = "document_path", unique = true, nullable = false)
    private String path;

    public DocBean(String name, String path) {
        this.name = name;
        this.path = path;
    }

    public DocBean() {

    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    @Override
    public String toString() {
        return "Document [document_id=" + id + ", document_name=" + name + ", document_path=" + path + "]";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass())
            return false;
        DocBean docBean = (DocBean) o;
        return Objects.equals(path, docBean.path);
        //return this.getName().equals(otherDocBean.getName()) && this.getPath().equals(otherDocBean.getPath());
    }

    @Override
    public int hashCode() {
        return Objects.hash(path);
    }
}
