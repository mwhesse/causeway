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
package org.apache.isis.core.metamodel.object;

import java.util.Optional;
import java.util.function.Supplier;

import org.springframework.lang.Nullable;

import org.apache.isis.applib.services.bookmark.Bookmark;
import org.apache.isis.applib.services.repository.EntityState;
import org.apache.isis.commons.collections.Can;
import org.apache.isis.commons.internal.exceptions._Exceptions;
import org.apache.isis.core.metamodel.context.HasMetaModelContext;
import org.apache.isis.core.metamodel.facets.object.icon.ObjectIcon;
import org.apache.isis.core.metamodel.object.ManagedObject.Specialization.BookmarkPolicy;
import org.apache.isis.core.metamodel.spec.HasObjectSpecification;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.core.metamodel.specloader.SpecificationLoader;

import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.val;
import lombok.extern.log4j.Log4j2;

/**
 * Represents an instance of some element of the meta-model recognized by the framework,
 * that is <i>Spring</i> managed beans, persistence-stack provided entities, view-models,
 * mixins or instances of value types.
 *
 * @since 2.0 {@index}}
 *
 */
public interface ManagedObject
extends
    Bookmarkable,
    HasMetaModelContext,
    HasObjectSpecification {

    /**
     * ManagedObject specializations have varying contract/behavior.
     */
    @Getter
    @RequiredArgsConstructor
    @Log4j2
    enum Specialization {
        /**
         * <h1>Contract</h1><ul>
         * <li>Specification (null, immutable)</li>
         * <li>Bookmark (n/a)</li>
         * <li>Pojo (null, immutable)</li>
         * </ul>
         * @implNote realized by a singleton (static) {@link ManagedObject} instance;
         */
        UNSPECIFIED(TypePolicy.NO_TYPE, BookmarkPolicy.NO_BOOKMARK, PojoPolicy.NO_POJO),

        /**
         * <h1>Contract</h1><ul>
         * <li>Specification (immutable, allowed to correspond to abstract type)</li>
         * <li>Bookmark (n/a)</li>
         * <li>Pojo (null, immutable)</li>
         * </ul>
         */
        EMPTY(TypePolicy.ABSTRACT_TYPE_ALLOWED, BookmarkPolicy.NO_BOOKMARK, PojoPolicy.NO_POJO),

        /**
         * <h1>Contract</h1><ul>
         * <li>Specification (immutable, NOT allowed to correspond to abstract type)</li>
         * <li>Bookmark (immutable)</li>
         * <li>Pojo (immutable)</li>
         * </ul>
         */
        VALUE(TypePolicy.EXACT_TYPE_REQUIRED, BookmarkPolicy.IMMUTABLE, PojoPolicy.IMMUTABLE),

        /**
         * <h1>Contract</h1><ul>
         * <li>Specification (immutable, NOT allowed to correspond to abstract type)</li>
         * <li>Bookmark (immutable)</li>
         * <li>Pojo (immutable)</li>
         * </ul>
         */
        SERVICE(TypePolicy.EXACT_TYPE_REQUIRED, BookmarkPolicy.IMMUTABLE, PojoPolicy.IMMUTABLE),

        /**
         * <h1>Contract</h1><ul>
         * <li>Specification (immutable, NOT allowed to correspond to abstract type)</li>
         * <li>Bookmark (refreshable, as VM state changes manifest in change of ID)</li>
         * <li>Pojo (mutable, but immutable obj. ref.)</li>
         * </ul>
         */
        VIEWMODEL(TypePolicy.EXACT_TYPE_REQUIRED, BookmarkPolicy.REFRESHABLE, PojoPolicy.STATEFUL),

        /**
         * <h1>Contract</h1><ul>
         * <li>Specification (immutable, NOT allowed to correspond to abstract type)</li>
         * <li>Bookmark (immutable,  entity must be persistent, it must have an ID,  fail otherwise)</li>
         * <li>Pojo (refetchable)</li>
         * </ul>
         */
        ENTITY(TypePolicy.EXACT_TYPE_REQUIRED, BookmarkPolicy.IMMUTABLE, PojoPolicy.REFETCHABLE),

        /**
         * <h1>Contract</h1><ul>
         * <li>Element Specification (immutable, NOT allowed to correspond to abstract type)</li>
         * <li>Bookmark (n/a)</li>
         * <li>Pojo (allowed stateful, immutable obj. ref)</li>
         * </ul>
         */
        MIXIN(TypePolicy.EXACT_TYPE_REQUIRED, BookmarkPolicy.NO_BOOKMARK, PojoPolicy.STATEFUL),

        /**
         * <h1>Contract</h1><ul>
         * <li>Element Specification (immutable, NOT allowed to correspond to abstract type)</li>
         * <li>Bookmark (n/a)</li>
         * <li>Pojo (allowed stateful, immutable obj. ref)</li>
         * </ul>
         */
        OTHER(TypePolicy.EXACT_TYPE_REQUIRED, BookmarkPolicy.NO_BOOKMARK, PojoPolicy.STATEFUL),

        /**
         * <h1>Contract</h1><ul>
         * <li>Element Specification (immutable, NOT allowed to correspond to abstract type)</li>
         * <li>Bookmark (n/a)</li>
         * <li>Pojo (unmod. Collection of pojos)</li>
         * </ul>
         */
        PACKED(TypePolicy.ABSTRACT_TYPE_ALLOWED, BookmarkPolicy.NO_BOOKMARK, PojoPolicy.PACKED);

        static enum TypePolicy {
            /** has no type information */
            NO_TYPE,
            /** has type information, abstract types are allowed */
            ABSTRACT_TYPE_ALLOWED,
            /** has type information, exact types are required */
            EXACT_TYPE_REQUIRED;
            ////
            /** has no type information */
            public boolean isNoType() { return this == NO_TYPE; }
            /** has type information, abstract types are allowed */
            public boolean isAbstractTypeAllowed() { return this == ABSTRACT_TYPE_ALLOWED; }
            /** has type information, exact types are required */
            public boolean isExactTypeRequired() { return this == EXACT_TYPE_REQUIRED; }
            /** has type information */
            public boolean isTypeRequiredAny() { return !isNoType(); }
        }
        static enum BookmarkPolicy {
            /** has no {@link Bookmark} */
            NO_BOOKMARK,
            /** has an immutable {@link Bookmark} */
            IMMUTABLE,
            /** has an refreshable {@link Bookmark}, that is a mutable object reference */
            REFRESHABLE;
            ////
            /** has no {@link Bookmark} */
            public boolean isNoBookmark() { return this == NO_BOOKMARK; }
            /** has an immutable {@link Bookmark} */
            public boolean isImmutable() { return this == IMMUTABLE; }
            /** has an refreshable {@link Bookmark}, that is a mutable object reference */
            public boolean isRefreshable() { return this == REFRESHABLE; }
        }
        static enum PojoPolicy {
            /** has no pojo, immutable <code>null</code> */
            NO_POJO,
            /** has a non-null pojo, immutable, with immutable object reference */
            IMMUTABLE,
            /** has a stateful pojo, with immutable object reference */
            STATEFUL,
            /** has a stateful pojo, with mutable object reference */
            REFETCHABLE,
            /** creates an unmodifiable collection of pojos (lazily);
             * supports unpacking into a {@link Can} of {@link ManagedObject}s;*/
            PACKED;
            ////
            /** has no pojo, immutable <code>null</code> */
            public boolean isNoPojo() { return this == NO_POJO; }
            /** has a non-null pojo, immutable, with immutable object reference */
            public boolean isImmutable() { return this == IMMUTABLE; }
            /** has a stateful pojo, with immutable object reference */
            public boolean isStateful() { return this == STATEFUL; }
            /** has a stateful pojo, with mutable object reference */
            public boolean isRefetchable() { return this == REFETCHABLE; }
            /** creates an unmodifiable collection of pojos (lazily);
             * supports unpacking into a {@link Can} of {@link ManagedObject}s;*/
            public boolean isPacked() { return this == PACKED; }
        }

        private final TypePolicy typePolicy;
        private final BookmarkPolicy bookmarkPolicy;
        private final PojoPolicy pojoPolicy;

        /**
         * UNSPECIFIED
         * @see TypePolicy#NO_TYPE
         * @see BookmarkPolicy#NO_BOOKMARK
         * @see PojoPolicy#NO_POJO
         */
        public boolean isUnspecified() { return this == UNSPECIFIED; }
        public boolean isSpecified() { return this != UNSPECIFIED; }
        /**
         * EMPTY
         * @see TypePolicy#ABSTRACT_TYPE_ALLOWED
         * @see BookmarkPolicy#NO_BOOKMARK
         * @see PojoPolicy#NO_POJO
         */
        public boolean isEmpty() { return this == EMPTY; }
        /**
         * VALUE
         * @see TypePolicy#EXACT_TYPE_REQUIRED
         * @see BookmarkPolicy#IMMUTABLE
         * @see PojoPolicy#IMMUTABLE
         */
        public boolean isValue() { return this == VALUE; }
        /**
         * SERVICE
         * @see TypePolicy#EXACT_TYPE_REQUIRED
         * @see BookmarkPolicy#IMMUTABLE
         * @see PojoPolicy#IMMUTABLE
         */
        public boolean isService() { return this == SERVICE; }
        /**
         * VIEWMODEL
         * @see TypePolicy#EXACT_TYPE_REQUIRED
         * @see BookmarkPolicy#REFRESHABLE
         * @see PojoPolicy#STATEFUL
         */
        public boolean isViewmodel() { return this == VIEWMODEL; }
        /**
         * ENTITY
         * @see TypePolicy#EXACT_TYPE_REQUIRED
         * @see BookmarkPolicy#IMMUTABLE
         * @see PojoPolicy#REFETCHABLE
         */
        public boolean isEntity() { return this == ENTITY; }
        /**
         * MIXIN
         * @see TypePolicy#EXACT_TYPE_REQUIRED
         * @see BookmarkPolicy#NO_BOOKMARK
         * @see PojoPolicy#STATEFUL
         */
        public boolean isMixin() { return this == MIXIN; }
        /**
         * OTHER
         * @see TypePolicy#EXACT_TYPE_REQUIRED
         * @see BookmarkPolicy#NO_BOOKMARK
         * @see PojoPolicy#STATEFUL
         */
        public boolean isOther() { return this == OTHER; }
        /**
         * PACKED
         * @see TypePolicy#ABSTRACT_TYPE_ALLOWED
         * @see BookmarkPolicy#NO_BOOKMARK
         * @see PojoPolicy#PACKED
         */
        public boolean isPacked() { return this == PACKED; }

        public static Specialization inferFrom(
                final @Nullable ObjectSpecification spec,
                final @Nullable Object pojo) {
            if(spec==null) {
                return UNSPECIFIED;
            }
            if(spec.isNonScalar()) {
                return PACKED;
            }
            if(pojo==null) {
                return EMPTY;
            }
            if(spec.isValue()) {
                return VALUE;
            }
            if(spec.isInjectable()) {
                return SERVICE;
            }
            if(spec.isViewModel()) {
                return VIEWMODEL;
            }
            if(spec.isEntity()) {
                return ENTITY;
            }
            if(spec.isMixin()) {
                return MIXIN;
            }
            if(!spec.isAbstract()) {
                return OTHER;
            }
            log.warn("failed specialization attempt for {}", spec);
            return UNSPECIFIED;
        }

    }

    /**
     * Returns the specific {@link Specialization} this {@link ManagedObject} implements,
     * which governs this object's behavior.
     */
    Specialization getSpecialization();
    @Override default BookmarkPolicy getBookmarkPolicy() {
        return getSpecialization().getBookmarkPolicy();
    }

    /**
     * Returns the specification that details the structure (meta-model) of this object.
     */
    @Override
    ObjectSpecification getSpecification();

    /**
     * Returns the adapted domain object, the 'plain old java' object this managed object
     * represents with the framework.
     */
    Object getPojo();

    @NonNull
    default EntityState getEntityState() {
        return EntityState.NOT_PERSISTABLE;
    }

    /**
     * If the underlying domain object is a viewmodel, refreshes any referenced entities.
     * (Acts as a no-op otherwise.)
     * @apiNote usually should be sufficient to refresh once per interaction.
     */
    void refreshViewmodel(@Nullable Supplier<Bookmark> bookmarkSupplier);

    Supplier<ManagedObject> asSupplier();

    /**
     * Unary operator asserting that {@code pojo} and {@link #getSpecification()} are
     * compliant with the policies from {@link #getSpecialization()}.
     */
    <T> T assertCompliance(@NonNull T pojo);

    // -- TITLE

    /**
     * The (untranslated) title of the wrapped pojo.
     */
    String getTitle();

    // -- SHORTCUT - ICON

    /**
     * Returns the name of an icon to use if this object is to be displayed
     * graphically.
     * <p>
     * May return <code>null</code> if no icon is specified.
     */
    default String getIconName() {
        return getSpecification().getIconName(this);
    }

    default ObjectIcon getIcon() {
        return getSpecification().getIcon(this);
    }

    // -- FACTORIES

    /**
     * Factory for Specialization#UNSPECIFIED.
     * @see ManagedObject.Specialization.TypePolicy#NO_TYPE
     * @see ManagedObject.Specialization.BookmarkPolicy#NO_BOOKMARK
     * @see ManagedObject.Specialization.PojoPolicy#NO_POJO
     */
    static ManagedObject unspecified() {
        return _ManagedObjectUnspecified.INSTANCE;
    }
    /**
     * EMPTY
     * @param spec - required
     * @see ManagedObject.Specialization.TypePolicy#ABSTRACT_TYPE_ALLOWED
     * @see ManagedObject.Specialization.BookmarkPolicy#NO_BOOKMARK
     * @see ManagedObject.Specialization.PojoPolicy#NO_POJO
     */
    static ManagedObject empty(final @NonNull ObjectSpecification spec) {
        return new _ManagedObjectEmpty(spec);
    }
    /**
     * VALUE
     * @param spec - required
     * @param pojo - if <code>null</code> maps to {@link #empty(ObjectSpecification)}
     * @see ManagedObject.Specialization.TypePolicy#EXACT_TYPE_REQUIRED
     * @see ManagedObject.Specialization.BookmarkPolicy#IMMUTABLE
     * @see ManagedObject.Specialization.PojoPolicy#IMMUTABLE
     */
    static ManagedObject value(
            final @NonNull ObjectSpecification spec,
            final @Nullable Object pojo) {
        return pojo != null
                ? new _ManagedObjectValue(spec, pojo)
                : empty(spec);
    }
    /**
     * SERVICE
     * @param spec - required
     * @param pojo - required
     * @see ManagedObject.Specialization.TypePolicy#EXACT_TYPE_REQUIRED
     * @see ManagedObject.Specialization.BookmarkPolicy#IMMUTABLE
     * @see ManagedObject.Specialization.PojoPolicy#IMMUTABLE
     */
    static ManagedObject service(
            final @NonNull ObjectSpecification spec,
            final @NonNull Object pojo) {
        return new _ManagedObjectService(spec, pojo);
    }
    /**
     * VIEWMODEL
     * @param spec - required
     * @param pojo - if <code>null</code> maps to {@link #empty(ObjectSpecification)}
     * @see ManagedObject.Specialization.TypePolicy#EXACT_TYPE_REQUIRED
     * @see ManagedObject.Specialization.BookmarkPolicy#REFRESHABLE
     * @see ManagedObject.Specialization.PojoPolicy#STATEFUL
     */
    static ManagedObject viewmodel(
            final @NonNull ObjectSpecification spec,
            final @Nullable Object pojo,
            final Optional<Bookmark> bookmarkIfKnown) {
        return pojo != null
                ? new _ManagedObjectViewmodel(spec, pojo, bookmarkIfKnown)
                : empty(spec);
    }
    /**
     * ENTITY
     * @param spec - required
     * @param pojo - if <code>null</code> maps to {@link #empty(ObjectSpecification)}
     * @param bookmark
     * @see ManagedObject.Specialization.TypePolicy#EXACT_TYPE_REQUIRED
     * @see ManagedObject.Specialization.BookmarkPolicy#IMMUTABLE
     * @see ManagedObject.Specialization.PojoPolicy#REFETCHABLE
     */
    static ManagedObject entity(
            final @NonNull ObjectSpecification spec,
            final @Nullable Object pojo,
            final @NonNull Optional<Bookmark> bookmarkIfKnown) {
        if(pojo == null) {
            return empty(spec);
        }
        val bookmarkIfAny = bookmarkIfKnown
                .or(()->spec.entityFacetElseFail().bookmarkFor(pojo));
        if(bookmarkIfAny.isPresent()) {
            return entityAttached(spec, pojo, bookmarkIfAny);
        } else {
            return entityDetached(spec, pojo);
        }
    }
    //FIXME java-doc
    static ManagedObject entityAttached(
            final @NonNull ObjectSpecification spec,
            final @NonNull Object pojo,
            final @NonNull Optional<Bookmark> bookmarkIfKnown) {
        return new _ManagedObjectEntityHybrid(
                        new _ManagedObjectEntityAttached(spec, pojo, bookmarkIfKnown));
    }
    //FIXME java-doc
    static ManagedObject entityDetached(
            final @NonNull ObjectSpecification spec,
            final @Nullable Object pojo) {
        return pojo != null
                ? new _ManagedObjectEntityHybrid(
                        new _ManagedObjectEntityDetached(spec, pojo))
                : empty(spec);
    }
    /**
     * MIXIN
     * @param spec - required
     * @param pojo - required
     * @see ManagedObject.Specialization.TypePolicy#EXACT_TYPE_REQUIRED
     * @see ManagedObject.Specialization.BookmarkPolicy#NO_BOOKMARK
     * @see ManagedObject.Specialization.PojoPolicy#STATEFUL
     */
    static ManagedObject mixin(
            final @NonNull ObjectSpecification spec,
            final @NonNull Object pojo) {
        return new _ManagedObjectMixin(spec, pojo);
    }
    /**
     * OTHER
     * @param spec - required
     * @param pojo - if <code>null</code> maps to {@link #empty(ObjectSpecification)}
     * @see ManagedObject.Specialization.TypePolicy#EXACT_TYPE_REQUIRED
     * @see ManagedObject.Specialization.BookmarkPolicy#NO_BOOKMARK
     * @see ManagedObject.Specialization.PojoPolicy#STATEFUL
     */
    static ManagedObject other(
            final @NonNull ObjectSpecification spec,
            final @Nullable Object pojo) {
        return pojo != null
                ? new _ManagedObjectOther(spec, pojo)
                : empty(spec);
    }
    /**
     * PACKED
     * @param elementSpec - required
     * @param nonScalar - if <code>null</code> uses {@link Can#empty()} instead
     * @see ManagedObject.Specialization.TypePolicy#ABSTRACT_TYPE_ALLOWED
     * @see ManagedObject.Specialization.BookmarkPolicy#NO_BOOKMARK
     * @see ManagedObject.Specialization.PojoPolicy#PACKED
     */
    static PackedManagedObject packed(
            final @NonNull ObjectSpecification elementSpec,
            final @Nullable Can<ManagedObject> nonScalar) {
        return new _ManagedObjectPacked(elementSpec, nonScalar);
    }

    /**
     * For cases, when the pojo's specification is not available and needs to be looked up.
     * <p>
     * Fails if the pojo is non-scalar.
     * @param specLoader - required
     * @param pojo - required, required non-scalar
     */
    static ManagedObject adaptScalar(
            final @NonNull SpecificationLoader specLoader,
            final @NonNull Object pojo) {
        if(pojo instanceof ManagedObject) {
            return (ManagedObject)pojo;
        }
        val spec = specLoader.specForType(pojo.getClass()).orElse(null);
        return adaptScalarInternal(spec, pojo, Optional.empty());
    }

    static ManagedObject adaptScalar(
            final @NonNull ObjectSpecification guess,
            final @Nullable Object pojo) {
        if(pojo instanceof ManagedObject) {
            return (ManagedObject)pojo;
        }
        return adaptScalarInternal(guess, pojo, Optional.empty());
    }

    static ManagedObject identified(
            final @NonNull  ObjectSpecification spec,
            final @Nullable Object pojo,
            final @NonNull  Bookmark bookmark) {
        return adaptScalarInternal(spec, pojo, Optional.of(bookmark));
    }

    // -- HELPER

    /**
     * spec and pojo don't need to be strictly in sync, we adapt if required
     */
    private static ManagedObject adaptScalarInternal(
            final @Nullable ObjectSpecification guess,
            final @NonNull Object pojo,
            final @NonNull Optional<Bookmark> bookmarkIfAny) {

        MmAssertionUtil.assertPojoIsScalar(pojo);
        val spec = MmSpecUtil.quicklyResolveObjectSpecificationFor(guess, pojo.getClass());

        val specialization = spec!=null
                ? Specialization.inferFrom(spec, pojo)
                : Specialization.UNSPECIFIED;

        switch(specialization) {
        case UNSPECIFIED:
            return unspecified();
        case VALUE:
            return value(spec, pojo);
        case SERVICE:
            return service(spec, pojo);
        case VIEWMODEL:
            return viewmodel(spec, pojo, bookmarkIfAny);
        case ENTITY:
            return entity(spec, pojo, bookmarkIfAny);
        case MIXIN:
            return mixin(spec, pojo);
        case OTHER:
            return other(spec, pojo);
        // unreachable (in this context)
        case EMPTY:
        case PACKED:
            throw _Exceptions.unexpectedCodeReach();
        }
        throw _Exceptions.unmatchedCase(specialization);
    }

    // -- FACTORIES LEGACY

    /**
     * Optimized for cases, when the pojo's specification is already available.
     * If {@code pojo} is an entity, automatically memoizes its bookmark.
     * @param spec
     * @param pojo - might also be a collection of pojos (null-able)
     */
    @Deprecated
    static ManagedObject of(
            final @NonNull ObjectSpecification spec,
            final @Nullable Object pojo) {

        if(pojo instanceof ManagedObject) {
            return (ManagedObject)pojo;
        }

        //ISIS-2430 Cannot assume Action Param Spec to be correct when eagerly loaded
        //actual type in use (during runtime) might be a sub-class of the above, so re-adapt with hinting spec
        val adapter = spec.getMetaModelContext().getObjectManager().adapt(pojo, spec);
        return adapter;
    }

    /**
     * Optimized for cases, when the pojo's specification and bookmark are already available.
     */
    @Deprecated
    static ManagedObject bookmarked(
            final @NonNull ObjectSpecification spec,
            final @NonNull Object pojo,
            final @NonNull Bookmark bookmark) {

        MmAssertionUtil.assertPojoIsScalar(pojo);

        if(!spec.getCorrespondingClass().isAssignableFrom(pojo.getClass())) {
            throw _Exceptions.illegalArgument(
                    "Pojo not compatible with ObjectSpecification, " +
                    "objectSpec.correspondingClass = %s, " +
                    "pojo.getClass() = %s, " +
                    "pojo.toString() = %s",
                    spec.getCorrespondingClass(), pojo.getClass(), pojo.toString());
        }
        MmAssertionUtil.assertPojoNotWrapped(pojo);
        return ManagedObject.identified(spec, pojo, bookmark);
    }

}
