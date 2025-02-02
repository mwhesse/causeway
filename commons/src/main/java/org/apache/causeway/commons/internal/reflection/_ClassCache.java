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
package org.apache.causeway.commons.internal.reflection;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Stream;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.xml.bind.annotation.XmlRootElement;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.Nullable;
import org.springframework.util.ReflectionUtils;

import org.apache.causeway.commons.collections.Can;
import org.apache.causeway.commons.internal._Constants;
import org.apache.causeway.commons.internal.base._Casts;
import org.apache.causeway.commons.internal.base._Strings;
import org.apache.causeway.commons.internal.collections._Arrays;
import org.apache.causeway.commons.internal.context._Context;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.val;

/**
 * <h1>- internal use only -</h1>
 * <p>
 * <b>WARNING</b>: Do <b>NOT</b> use any of the classes provided by this package! <br/>
 * These may be changed or removed without notice!
 * </p>
 * <p>
 * Motivation: JDK reflection API has no Class.getMethod(name, ...) variant that does not produce an expensive
 * stack-trace, when no such method exists.
 * </p>
 * @apiNote
 * thread-save, implements AutoCloseable so we can put it on the _Context, which then automatically
 * takes care of the lifecycle
 * @since 2.0
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class _ClassCache implements AutoCloseable {

    public static _ClassCache getInstance() {
        return _Context.computeIfAbsent(_ClassCache.class, _ClassCache::new);
    }

    /**
     * JUnit support.
     */
    public static void invalidate() {
        _Context.put(_ClassCache.class, new _ClassCache(), true);
    }

    public void add(final Class<?> type) {
        inspectType(type);
    }

    // -- TYPE SPECIFIC SEMANTICS

    public boolean hasJaxbRootElementSemantics(final Class<?> type) {
        return inspectType(type).hasJaxbRootElementSemantics;
    }

    // -- CONSTRUCTOR SEMANTICS

    public <T> Stream<Constructor<T>> streamPublicConstructors(final Class<T> type) {
        return _Casts.uncheckedCast(inspectType(type).publicConstructorsByKey.values().stream());
    }

    public <T> Stream<Constructor<T>> streamPublicConstructorsWithInjectSemantics(final Class<T> type) {
        return _Casts.uncheckedCast(inspectType(type).constructorsWithInjectSemanticsByKey.values().stream());
    }

    public Optional<Constructor<?>> lookupPublicConstructor(final Class<?> type, final Class<?>[] paramTypes) {
        return Optional.ofNullable(lookupConstructor(false, type, paramTypes));
    }

    // -- POST CONSTRUCT SEMANTICS

    public Stream<Method> streamPostConstructMethods(final Class<?> type) {
        return inspectType(type).postConstructMethodsByKey.values().stream();
    }

    // -- METHOD SEMANTICS

    /**
     * A drop-in replacement for {@link Class#getMethod(String, Class...)} that only looks up
     * public methods and does not throw {@link NoSuchMethodException}s.
     */
    public Method lookupPublicMethod(final Class<?> type, final String name, final Class<?>[] paramTypes) {
        return lookupMethod(false, type, name, paramTypes);
    }

    /**
     * Variant of {@link #lookupPublicMethod(Class, String, Class[])}
     * that in addition looks up declared methods. (including non-public,
     * but not including inherited non-public ones)
     */
    public Method lookupPublicOrDeclaredMethod(final Class<?> type, final String name, final Class<?>[] paramTypes) {
        return lookupMethod(true, type, name, paramTypes);
    }

    public Stream<Method> streamPublicMethods(final Class<?> type) {
        return inspectType(type).publicMethodsByKey.values().stream();
    }

    public Stream<Method> streamPublicOrDeclaredMethods(final Class<?> type) {
        val classModel = inspectType(type);
        return Stream.concat(
                classModel.publicMethodsByKey.values().stream(),
                classModel.nonPublicDeclaredMethodsByKey.values().stream());
    }

    public Stream<Method> streamDeclaredMethods(final Class<?> type) {
        return inspectType(type).declaredMethods.stream();
    }

    public Method findMethodUniquelyByNameOrFail(final Class<?> type, final String methodName) {
        val matchingMethods = streamPublicOrDeclaredMethods(type)
                .filter(method->method.getName().equals(methodName))
                .collect(Can.toCan());
        return matchingMethods.isCardinalityMultiple()
                ? _Reflect.mostSpecificMethodOf(matchingMethods).orElseThrow()
                : matchingMethods.getSingletonOrFail();
    }

    // -- FIELD SEMANTICS

    public Stream<Field> streamDeclaredFields(final Class<?> type) {
        return inspectType(type).declaredFields.stream();
    }

    // -- FIELD vs GETTER

    public Optional<Method> getterForField(final Class<?> type, final Field field) {
        val capitalizedFieldName = _Strings.capitalize(field.getName());
        return Stream.of("get", "is")
        .map(prefix->prefix + capitalizedFieldName)
        .map(methodName->lookupPublicOrDeclaredMethod(type, methodName, _Constants.emptyClasses))
        .filter(_Reflect.Filter.isGetter())
        .findFirst();
    }

    public Optional<Field> fieldForGetter(final Class<?> type, final Method getterCandidate) {
        return Optional.ofNullable(findFieldForGetter(getterCandidate));
    }

    // -- METHOD STREAMS

//    private Stream<Method> streamAllMethods(final Class<?> type) {
//        val classModel = inspectType(type);
//        return classModel.declaredMethods.stream();
//    }

    /**
     * Returns a Stream of declared Methods, that pass the given {@code filter},
     * while as an optimization, memoizing the result under given
     * {@code attributeName}.
     * @param type
     * @param attributeName
     * @param filter
     */
    public Stream<Method> streamDeclaredMethodsHaving(
            final Class<?> type,
            final String attributeName,
            final Predicate<Method> filter) {

        val classModel = inspectType(type);

        synchronized(classModel.declaredMethodsByAttribute) {
            return classModel.declaredMethodsByAttribute
            .computeIfAbsent(attributeName, key->classModel.declaredMethods.filter(filter))
            .stream();
        }
    }


    // -- IMPLEMENATION DETAILS

    @RequiredArgsConstructor
    private static class ClassModel {
        private final Can<Field> declaredFields;
        private final Can<Method> declaredMethods;
        private final Map<ConstructorKey, Constructor<?>> publicConstructorsByKey = new HashMap<>();
        private final Map<ConstructorKey, Constructor<?>> constructorsWithInjectSemanticsByKey = new HashMap<>();
        //private final Map<ConstructorKey, Constructor<?>> nonPublicDeclaredConstructorsByKey = new HashMap<>();
        private final Map<MethodKey, Method> publicMethodsByKey = new HashMap<>();
        private final Map<MethodKey, Method> postConstructMethodsByKey = new HashMap<>();
        private final Map<MethodKey, Method> nonPublicDeclaredMethodsByKey = new HashMap<>();
        private final Map<String, Can<Method>> declaredMethodsByAttribute = new HashMap<>();
        private final boolean hasJaxbRootElementSemantics;
    }

    private final Map<Class<?>, ClassModel> inspectedTypes = new HashMap<>();

    @AllArgsConstructor(staticName = "of") @EqualsAndHashCode
    private static final class ConstructorKey {
        private final Class<?> type; // constructors's declaring class
        private final @Nullable Class<?>[] paramTypes;

        public static ConstructorKey of(final Class<?> type, final Constructor<?> constructor) {
            return ConstructorKey.of(type, _Arrays.emptyToNull(constructor.getParameterTypes()));
        }
    }

    @AllArgsConstructor(staticName = "of") @EqualsAndHashCode
    private static final class MethodKey {
        private final Class<?> type; // method's declaring class
        private final String name; // method name
        private final @Nullable Class<?>[] paramTypes;

        public static MethodKey of(final Class<?> type, final Method method) {
            return MethodKey.of(type, method.getName(), _Arrays.emptyToNull(method.getParameterTypes()));
        }
    }

    @Override
    public void close() throws Exception {
        synchronized(inspectedTypes) {
            inspectedTypes.clear();
        }
    }

    // -- UTILITY

    public static boolean methodExcludeFilter(final Method method) {
        return method.isBridge()
                || Modifier.isStatic(method.getModifiers())
                || method.getDeclaringClass().equals(Object.class)
                || (_Reflect.isNonFinalObjectMethod(method)
                        // keep overwritten toString() methods, see TitleFacetFromToStringMethod
                        && !_Reflect.isOverwrittenToString(method))
                || _Reflect.hasGenericBounds(method);
    }

    public static boolean methodIncludeFilter(final Method method) {
        return !methodExcludeFilter(method);
    }

    // -- HELPER

    private ClassModel inspectType(final Class<?> type) {
        synchronized(inspectedTypes) {

            return inspectedTypes.computeIfAbsent(type, __->{

              //TODO[CAUSEWAY-3556] optimization candidate (needs inspectedTypes to be a ConcurrentHashMap)
//                val declaredMethods = _Reflect.streamTypeHierarchy(type, InterfacePolicy.INCLUDE)
//                    .filter(cls->cls.equals(Object.class))
//                    .flatMap(cls->{
//                        return cls.equals(type)
//                            ? _NullSafe.stream(cls.getDeclaredMethods())
//                                .filter(_ClassCache::methodIncludeFilter)
//                            : inspectType(cls).declaredMethods.stream();
//                    })
//                    .collect(Can.toCan());

                val declaredFields = Can.ofArray(type.getDeclaredFields());
                val declaredMethods = //type.getDeclaredMethods(); ... cannot detect non overridden inherited methods
                        Can.ofStream(_Reflect.streamAllMethods(type, true)
                                .filter(_ClassCache::methodIncludeFilter));

                val model = new ClassModel(
                        declaredFields,
                        declaredMethods,
                        _Annotations.isPresent(type, XmlRootElement.class));

                val publicConstr = type.getConstructors();
                for(val constr : publicConstr) {
                    val key = ConstructorKey.of(type, constr);
                    // collect public constructors
                    model.publicConstructorsByKey.put(key, constr);
                    // collect public constructors with inject semantics
                    if(isInjectSemantics(constr)) {
                        model.constructorsWithInjectSemanticsByKey.put(key, constr);
                    }
                }

                // process all public and non-public
                for(val method : declaredMethods) {

                    val key = MethodKey.of(type, method);
                    // add all now, remove public ones later
                    val methodToKeep =
                            putIntoMapHonoringOverridingRelation(model.nonPublicDeclaredMethodsByKey, key, method);

                    // collect post-construct methods
                    if(isPostConstruct(methodToKeep)) {
                        model.postConstructMethodsByKey.put(key, methodToKeep);
                    }
                }

                // process public only
                val publicMethods = Can.ofArray(type.getMethods()) //XXX[CAUSEWAY-3327] order of methods returned might differ among JVM instances
                        .filter(_ClassCache::methodIncludeFilter);
                for(val method : publicMethods) {
                    val key = MethodKey.of(type, method);
                    putIntoMapHonoringOverridingRelation(model.publicMethodsByKey, key, method);
                    model.nonPublicDeclaredMethodsByKey.remove(key);
                }

                return model;

            });
        }
    }

    /**
     * Handles the case well, when a method is already in the map and is about to be overwritten.
     * We keep or put that one that overrides the other (in a Java language sense) and return the winner so to speak.
     */
    private static Method putIntoMapHonoringOverridingRelation(
            final Map<MethodKey, Method> map, final MethodKey key, final Method method) {
        val methodWithSameKey = map.get(key); // in case the map is already populated
        val methodToKeep = methodWithSameKey==null
            ? method
            /* key-clash originating from one method overriding the other
             * we need to keep or put the one which is the overriding one (not the overwritten one) */
            : _Reflect.methodsWhichIsOverridingTheOther(methodWithSameKey, method);
        map.put(key, methodToKeep);
        return methodToKeep;
    }

    /**
     * signature: any
     * access: public and non-public
     */
    private boolean isInjectSemantics(final Constructor<?> con) {
        return _Annotations.synthesize(con, Inject.class).isPresent()
                || _Annotations.synthesize(con, Autowired.class).map(annot->annot.required()).orElse(false);
    }

    /**
     * return-type: void
     * signature: no args
     * access: public and non-public
     */
    private boolean isPostConstruct(final Method method) {
        return void.class.equals(method.getReturnType())
                && method.getParameterCount()==0
                ? _Annotations.synthesize(method, PostConstruct.class).isPresent()
                : false;
    }

    private Constructor<?> lookupConstructor(
            final boolean includeDeclaredConstructors,
            final Class<?> type,
            final Class<?>[] paramTypes) {

        val model = inspectType(type);
        val key = ConstructorKey.of(type, _Arrays.emptyToNull(paramTypes));

        val publicConstructor = model.publicConstructorsByKey.get(key);
        return publicConstructor;
    }

    private Method lookupMethod(
            final boolean includeDeclaredMethods,
            final Class<?> type,
            final String name,
            final Class<?>[] paramTypes) {

        val model = inspectType(type);

        val key = MethodKey.of(type, name, _Arrays.emptyToNull(paramTypes));

        val publicMethod = model.publicMethodsByKey.get(key);
        if(publicMethod!=null) {
            return publicMethod;
        }
        if(includeDeclaredMethods) {
            return model.nonPublicDeclaredMethodsByKey.get(key);
        }
        return null;
    }

    // -- HELPER - FIELD FOR GETTER

    private static Field findFieldForGetter(final Method getterCandidate) {
        if(ReflectionUtils.isObjectMethod(getterCandidate)) {
            return null;
        }
        val fieldNameCandidate = fieldNameForGetter(getterCandidate);
        if(fieldNameCandidate==null) {
            return null;
        }
        val declaringClass = getterCandidate.getDeclaringClass();
        return ReflectionUtils.findField(declaringClass, fieldNameCandidate);
    }

    private static String fieldNameForGetter(final Method getter) {
        if(getter.getParameterCount()>0) {
            return null;
        }
        if(getter.getReturnType()==void.class) {
            return null;
        }
        val methodName = getter.getName();
        String fieldName = null;
        if(methodName.startsWith("is") &&  methodName.length() > 2) {
            fieldName = methodName.substring(2);
        } else if(methodName.startsWith("get") &&  methodName.length() > 3) {
            fieldName = methodName.substring(3);
        } else {
            return null;
        }
        return _Strings.decapitalize(fieldName);
    }
}
