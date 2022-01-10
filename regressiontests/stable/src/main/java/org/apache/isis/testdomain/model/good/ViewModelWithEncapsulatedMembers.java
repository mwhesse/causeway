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
package org.apache.isis.testdomain.model.good;

import java.io.Serializable;
import java.util.List;

import org.apache.isis.applib.annotations.Action;
import org.apache.isis.applib.annotations.Collection;
import org.apache.isis.applib.annotations.DomainObject;
import org.apache.isis.applib.annotations.Introspection;
import org.apache.isis.applib.annotations.MemberSupport;
import org.apache.isis.applib.annotations.Nature;
import org.apache.isis.applib.annotations.Property;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

@DomainObject(
        nature = Nature.VIEW_MODEL,
        introspection = Introspection.ENCAPSULATION_ENABLED)
public class ViewModelWithEncapsulatedMembers
implements Serializable {

    private static final long serialVersionUID = 1L;

    // allowed to be private since 2.0.0-M7
    @Action
    private String myAction() {
        return "Hallo World!";
    }

    // allowed to be private since 2.0.0-M7
    @MemberSupport private String disableMyAction() {
        return "action disabled for testing purposes";
    }

    // -- PROPERTY WITH PRIVATE GETTER AND SETTER

    @Property
    // allowed to be private since 2.0.0-M7
    @Getter(AccessLevel.PRIVATE)
    @Setter(AccessLevel.PRIVATE)
    private String propWithPrivateAccessors = "Foo";

    // allowed to be private since 2.0.0-M7
    @MemberSupport private String disablePropWithPrivateAccessors() {
        return "property disabled for testing purposes";
    }

    // -- COLLECTION WITH PRIVATE GETTER AND SETTER

    @Collection
    // allowed to be private since 2.0.0-M7
    @Getter(AccessLevel.PRIVATE)
    @Setter(AccessLevel.PRIVATE)
    private List<String> collWithPrivateAccessors = List.of("Foo");

    // allowed to be private since 2.0.0-M7
    @MemberSupport private String disableCollWithPrivateAccessors() {
        return "collection disabled for testing purposes";
    }


}