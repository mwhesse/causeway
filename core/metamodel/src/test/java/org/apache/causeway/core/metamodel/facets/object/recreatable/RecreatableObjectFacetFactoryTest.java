/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package org.apache.causeway.core.metamodel.facets.object.recreatable;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.apache.causeway.applib.ViewModel;
import org.apache.causeway.core.metamodel.facetapi.Facet;
import org.apache.causeway.core.metamodel.facets.FacetFactoryTestAbstract;
import org.apache.causeway.core.metamodel.facets.object.viewmodel.ViewModelFacet;
import org.apache.causeway.core.metamodel.facets.object.viewmodel.ViewModelFacetFactory;
import org.apache.causeway.core.metamodel.facets.object.viewmodel.ViewModelFacetForViewModelInterface;

class RecreatableObjectFacetFactoryTest
extends FacetFactoryTestAbstract {

    private ViewModelFacetFactory facetFactory;

    @BeforeEach
    protected void setUp() {
        facetFactory = new ViewModelFacetFactory(getMetaModelContext());
    }

    @AfterEach
    protected void tearDown() {
        facetFactory = null;
    }

    static class Customer implements ViewModel {
        @Override
        public String viewModelMemento() { return null; }
        public Customer(final String memento) { }
    }

    @Test
    void viewModelInterfacePickedUpOnClassAndDefaultsToAlways() {

        objectScenario(Customer.class, (processClassContext, facetHolder) -> {
            //when
            facetFactory.process(processClassContext);
            //then
            final Facet facet = facetHolder.getFacet(ViewModelFacet.class);
            assertNotNull(facet);
            assertTrue(facet instanceof ViewModelFacetForViewModelInterface);

            assertNoMethodsRemoved();
        });
    }

}
