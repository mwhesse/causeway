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

package org.apache.isis.applib.id;

import java.util.Objects;
import java.util.stream.Collectors;

import org.apache.isis.applib.services.i18n.TranslationService;
import org.apache.isis.commons.collections.Can;
import org.apache.isis.commons.internal.base._Strings;

import lombok.Getter;

/**
 * @since 1.x revised for 2.0 {@index}
 */
public class Identifier implements Comparable<Identifier> {

    /**
     * What type of feature this identifies.
     */
    public static enum Type {
        CLASS, PROPERTY_OR_COLLECTION, ACTION;
        public boolean isAction() { return this == ACTION; }
        public boolean isPropertyOrCollection() { return this == PROPERTY_OR_COLLECTION; }
        public boolean isClass() { return this == CLASS; }
    }

    // -- FACTORY METHODS

    public static Identifier classIdentifier(final TypeIdentifier typeIdentifier) {
        return new Identifier(typeIdentifier, "", Can.empty(), Type.CLASS);
    }

    public static Identifier propertyOrCollectionIdentifier(
            final TypeIdentifier typeIdentifier,
            final String propertyOrCollectionName) {
        return new Identifier(typeIdentifier, propertyOrCollectionName, Can.empty(), Type.PROPERTY_OR_COLLECTION);
    }

    public static Identifier actionIdentifier(
            final TypeIdentifier typeIdentifier,
            final String actionName, 
            final Class<?>... parameterClasses) {
        return actionIdentifier(typeIdentifier, actionName, classNamesOf(parameterClasses));
    }

    public static Identifier actionIdentifier(
            final TypeIdentifier typeIdentifier,
            final String actionName, 
            final Can<String> parameterClassNames) {
        return new Identifier(typeIdentifier, actionName, parameterClassNames, Type.ACTION);
    }

    // -- INSTANCE FIELDS

    @Getter private final TypeIdentifier typeIdentifier;
    
    @Getter private final String className;
    
    @Getter private final String memberName;
    
    private final Can<String> parameterClassNames;
    
    @Getter private final Type type;
    
    /**
     * Fully qualified Identity String. (class-name + member-name + param-class-names)
     */
    @Getter private final String fullIdentityString;
    
    /**
     * Member Identity String (class omitted), including parameters if any.
     */
    @Getter private final String memberNameAndParameterClassNamesIdentityString;

    /**
     * Context to be used for i18n translation.
     * @see TranslationService
     */
    @Getter private final String translationContext;

    // -- CONSTRUCTOR

    private Identifier(
            final TypeIdentifier typeIdentifier,
            final String memberName, 
            final Can<String> parameterClassNames, 
            final Type type) {
        
        this.typeIdentifier = typeIdentifier;
        this.className = typeIdentifier.getClassName();
        this.memberName = memberName;
        this.parameterClassNames = parameterClassNames;
        this.type = type;
         
        this.memberNameAndParameterClassNamesIdentityString =
                memberName + (type.isAction() 
                        ? "(" + parameterClassNames.stream().collect(Collectors.joining(",")) + ")" 
                        : "");
        
        this.translationContext = 
                className + "#" + memberName + (type.isAction() ? "()" : "");

        this.fullIdentityString = _Strings.isEmpty(memberName) 
                ? className
                : className + "#" + memberNameAndParameterClassNamesIdentityString;
    }

    // -- NATURAL NAMES
    
    public String getClassNaturalName() {
        final String className = getClassName();
        final String isolatedName = className.substring(className.lastIndexOf('.') + 1);
        return naturalName(isolatedName);
    }

    public String getMemberNaturalName() {
        return naturalName(memberName);
    }

    public Can<String> getMemberParameterClassNames() {
        return parameterClassNames;
    }

    public Can<String> getMemberParameterNaturalNames() {
        return naturalNames(parameterClassNames);
    }

    // -- OBJECT CONTRACT

    @Override
    public int compareTo(final Identifier other) {
        return toString().compareTo(other.toString());
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj instanceof Identifier) {
            return isEqualTo((Identifier) obj);
        }
        return false;
    }

    public boolean isEqualTo(final Identifier other) {
        return Objects.equals(this.className, other.className) 
                && Objects.equals(this.memberName, other.memberName) 
                && this.parameterClassNames.equals(other.parameterClassNames);
    }

    @Override
    public int hashCode() {
        return toString().hashCode();
    }

    @Override
    public String toString() {
        return fullIdentityString;
    }

    // -- HELPER
    
    private static Can<String> classNamesOf(final Class<?>[] parameterClasses) {
        return Can.ofArray(parameterClasses)
        .map(Class::getName);
    }
    
    private static final char SPACE = ' ';
    
    /*
     * Returns a word spaced version of the specified name, so there are spaces
     * between the words, where each word starts with a capital letter. E.g.,
     * "NextAvailableDate" is returned as "Next Available Date".
     */
    private static String naturalName(final String name) {
        final int length = name.length();

        if (length <= 1) {
            return name.toUpperCase();// ensure first character is upper case
        }

        final StringBuffer naturalName = new StringBuffer(length);

        char previousCharacter;
        char character = Character.toUpperCase(name.charAt(0));// ensure first
        // character is
        // upper case
        naturalName.append(character);
        char nextCharacter = name.charAt(1);

        for (int pos = 2; pos < length; pos++) {
            previousCharacter = character;
            character = nextCharacter;
            nextCharacter = name.charAt(pos);

            if (previousCharacter != SPACE) {
                if (Character.isUpperCase(character) && !Character.isUpperCase(previousCharacter)) {
                    naturalName.append(SPACE);
                }
                if (Character.isUpperCase(character) && Character.isLowerCase(nextCharacter) && Character.isUpperCase(previousCharacter)) {
                    naturalName.append(SPACE);
                }
                if (Character.isDigit(character) && !Character.isDigit(previousCharacter)) {
                    naturalName.append(SPACE);
                }
            }
            naturalName.append(character);
        }
        naturalName.append(nextCharacter);
        return naturalName.toString();
    }
    
    private static Can<String> naturalNames(final Can<String> names) {
        return names.map(Identifier::naturalName);
    }
    
}
