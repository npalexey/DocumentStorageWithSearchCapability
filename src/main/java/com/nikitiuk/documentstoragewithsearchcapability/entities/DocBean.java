package com.nikitiuk.documentstoragewithsearchcapability.entities;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "document")
public class DocBean {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", unique = true)
    private int id;

    @Column(name = "name")
    private String name;

    @Column(name = "path")
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
        return "Document [id=" + id + ", Name=" + name + ", Path=" + path + "]";
    }

    public Boolean equals(DocBean otherDocBean){
        return this.getName().equals(otherDocBean.getName()) && this.getPath().equals(otherDocBean.getPath());
    }
}
