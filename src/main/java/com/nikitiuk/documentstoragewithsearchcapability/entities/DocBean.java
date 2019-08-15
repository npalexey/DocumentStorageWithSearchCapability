package com.nikitiuk.documentstoragewithsearchcapability.entities;

import org.hibernate.annotations.GenericGenerator;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "Documents")
public class DocBean {

    @Id
    @GeneratedValue(generator = "native")
    @GenericGenerator(name = "native", strategy = "native")
    @Column(name = "document_id", unique = true, updatable = false, nullable = false)
    private int id;

    @Column(name = "document_name")
    private String name;

    @Column(name = "document_path")
    private String path;

    public DocBean(String name, String path) {
        this.name = name;
        this.path = path;
    }

    public DocBean(){

    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
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

    public Boolean equals(DocBean otherDocBean){
        return this.getName().equals(otherDocBean.getName()) && this.getPath().equals(otherDocBean.getPath());
    }
}
