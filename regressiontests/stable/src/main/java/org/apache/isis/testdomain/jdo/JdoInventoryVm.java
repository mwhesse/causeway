package org.apache.isis.testdomain.jdo;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import javax.inject.Inject;

import org.apache.isis.applib.annotation.Collection;
import org.apache.isis.applib.annotation.DomainObject;
import org.apache.isis.applib.annotation.Editing;
import org.apache.isis.applib.annotation.MemberSupport;
import org.apache.isis.applib.annotation.Nature;
import org.apache.isis.applib.annotation.ObjectSupport;
import org.apache.isis.applib.annotation.Optionality;
import org.apache.isis.applib.annotation.Property;
import org.apache.isis.applib.services.repository.RepositoryService;
import org.apache.isis.testdomain.jdo.entities.JdoInventory;
import org.apache.isis.testdomain.jdo.entities.JdoProduct;

import lombok.Getter;
import lombok.Setter;

@DomainObject(
        nature=Nature.VIEW_MODEL
        , logicalTypeName = "testdomain.jdo.JdoInventoryVm"
)
public class JdoInventoryVm {
	
    @ObjectSupport public String title() {
        return String.format("%s; %s",
                this.getClass().getSimpleName(), getName());
    }
    
    private String getName() {
    	return Optional.ofNullable(inventory).map(JdoInventory::getName).orElse("No inventory set");
    }

    @Inject
    private RepositoryService repository;

    @Getter @Setter
    @Property(editing = Editing.ENABLED, optionality = Optionality.OPTIONAL)
    private JdoInventory inventory;

    @MemberSupport public List<JdoInventory> choicesInventory() {
        return repository.allInstances(JdoInventory.class);
    }

    @Collection
    public java.util.Collection<JdoProduct> getProductsForTab1() {
        return Optional.ofNullable(inventory)
                .map(JdoInventory::getProducts)
                .orElseGet(Collections::emptySet);
    }

    @Collection
    public java.util.Collection<JdoProduct> getProductsForTab2() {
        return Optional.ofNullable(inventory)
                .map(JdoInventory::getProducts)
                .orElseGet(Collections::emptySet);
    }
}
