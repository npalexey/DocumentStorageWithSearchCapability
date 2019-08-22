package com.nikitiuk.documentstoragewithsearchcapability.entities.testentities.TestOne;

import javax.persistence.Embeddable;
import java.io.Serializable;
import java.util.Objects;

@Embeddable
public class DependentId implements Serializable {

    private String name;

    private long empId;

    private DependentId() {
    }

    public DependentId(
            Long empId) {
        this.empId = empId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public long getEmpId() {
        return empId;
    }

    public void setEmpId(long empId) {
        this.empId = empId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass())
            return false;
        DependentId that = (DependentId) o;
        return Objects.equals(empId, that.empId) ;
    }

    @Override
    public int hashCode() {
        return Objects.hash(empId);
    }
}
