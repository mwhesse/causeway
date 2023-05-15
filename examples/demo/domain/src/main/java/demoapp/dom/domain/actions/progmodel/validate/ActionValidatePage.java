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
package demoapp.dom.domain.actions.progmodel.validate;

import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import javax.inject.Inject;
import javax.inject.Named;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;

import org.apache.causeway.applib.annotation.Collection;
import org.apache.causeway.applib.annotation.CollectionLayout;
import org.apache.causeway.applib.annotation.DomainObject;
import org.apache.causeway.applib.annotation.Editing;
import org.apache.causeway.applib.annotation.LabelPosition;
import org.apache.causeway.applib.annotation.Nature;
import org.apache.causeway.applib.annotation.ObjectSupport;
import org.apache.causeway.applib.annotation.Optionality;
import org.apache.causeway.applib.annotation.Property;
import org.apache.causeway.applib.annotation.PropertyLayout;
import org.apache.causeway.valuetypes.asciidoc.applib.value.AsciiDoc;

import lombok.Getter;
import lombok.Setter;

import demoapp.dom._infra.asciidocdesc.HasAsciiDocDescription;
import demoapp.dom._infra.resources.AsciiDocReaderService;
import demoapp.dom.domain.actions.progmodel.TvCharacter;
import demoapp.dom.domain.actions.progmodel.TvShow;

@XmlRootElement(name = "Demo")
@XmlType
@XmlAccessorType(XmlAccessType.FIELD)
@Named("demo.ActionValidate")
@DomainObject(nature=Nature.VIEW_MODEL, editing=Editing.ENABLED)
public class ActionValidatePage implements HasAsciiDocDescription {

    @ObjectSupport public String title() {
        return "Action Validates";
    }


    @Property
    @PropertyLayout(labelPosition=LabelPosition.NONE)
    public AsciiDoc getSingleValidateDescription() {
        return asciiDocReaderService.readFor(this, "singleValidateDescription");
    }
    @Property(editing = Editing.ENABLED, optionality = Optionality.OPTIONAL)
    @Getter @Setter
    private TvShow selectedTvShow;

    @Property
    @PropertyLayout(labelPosition=LabelPosition.NONE)
    public AsciiDoc getParameterMatchingDescription() {
        return asciiDocReaderService.readFor(this, "parameterMatchingDescription");
    }
    @Property(editing = Editing.ENABLED, optionality = Optionality.OPTIONAL)
    @Getter @Setter
    private TvShow preselectTvShow2;
    @Property(editing = Editing.ENABLED, optionality = Optionality.OPTIONAL)
    @Getter @Setter
    private TvCharacter.Sex preselectCharacterSex2;

    @Property
    @PropertyLayout(labelPosition=LabelPosition.NONE)
    public AsciiDoc getAllArgsValidationDescription() {
        return asciiDocReaderService.readFor(this, "allArgsValidationDescription");
    }


    @Collection
    @CollectionLayout
    @Getter
    private final Set<TvCharacter> tvCharacters = new LinkedHashSet<>();

    @Collection
    @CollectionLayout
    @Getter
    private final Set<TvCharacter> selectedTvCharacters = new LinkedHashSet<>();

    @Inject @XmlTransient AsciiDocReaderService asciiDocReaderService;

}

