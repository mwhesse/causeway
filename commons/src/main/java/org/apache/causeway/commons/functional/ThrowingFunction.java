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
package org.apache.causeway.commons.functional;

import java.util.function.BiFunction;
import java.util.function.Function;

/**
 * A {@link Function} that allows invocation of code that throws a checked
 * {@link Exception}.
 *
 * @param <T> the type of the input to the function
 * @param <R> the type of the result of the function
 * @apiNote this is a clone from <i>Spring's</i>
 *     org.springframework.util.function.ThrowingFunction (as was introduced with <i>Spring Framework v6</i>);
 *     with version 3, the latter is used as a replacement and this interface is removed
 * @since 2.x {index}
 */
@FunctionalInterface
public interface ThrowingFunction<T, R> extends Function<T, R> {

    /**
     * Applies this function to the given argument, possibly throwing a checked
     * exception.
     * @param t the function argument
     * @return the function result
     * @throws Exception on error
     */
    R applyWithException(T t) throws Exception;

    /**
     * Default {@link Function#apply(Object)} that wraps any thrown checked
     * exceptions (by default in a {@link RuntimeException}).
     * @see java.util.function.Function#apply(java.lang.Object)
     */
    @Override
    default R apply(final T t) {
        return apply(t, RuntimeException::new);
    }

    /**
     * Applies this function to the given argument, wrapping any thrown checked
     * exceptions using the given {@code exceptionWrapper}.
     * @param exceptionWrapper {@link BiFunction} that wraps the given message
     * and checked exception into a runtime exception
     * @return a result
     */
    default R apply(final T t, final BiFunction<String, Exception, RuntimeException> exceptionWrapper) {
        try {
            return applyWithException(t);
        }
        catch (RuntimeException ex) {
            throw ex;
        }
        catch (Exception ex) {
            throw exceptionWrapper.apply(ex.getMessage(), ex);
        }
    }

    /**
     * Return a new {@link ThrowingFunction} where the {@link #apply(Object)}
     * method wraps any thrown checked exceptions using the given
     * {@code exceptionWrapper}.
     * @param exceptionWrapper {@link BiFunction} that wraps the given message
     * and checked exception into a runtime exception
     * @return the replacement {@link ThrowingFunction} instance
     */
    default ThrowingFunction<T, R> throwing(final BiFunction<String, Exception, RuntimeException> exceptionWrapper) {
        return new ThrowingFunction<>() {

            @Override
            public R applyWithException(final T t) throws Exception {
                return ThrowingFunction.this.applyWithException(t);
            }

            @Override
            public R apply(final T t) {
                return apply(t, exceptionWrapper);
            }

        };
    }

    /**
     * Lambda friendly convenience method that can be used to create a
     * {@link ThrowingFunction} where the {@link #apply(Object)} method wraps
     * any checked exception thrown by the supplied lambda expression or method
     * reference.
     * <p>This method can be especially useful when working with method references.
     * It allows you to easily convert a method that throws a checked exception
     * into an instance compatible with a regular {@link Function}.
     * <p>For example:
     * <pre class="code">
     * stream.map(ThrowingFunction.of(Example::methodThatCanThrowCheckedException));
     * </pre>
     * @param <T> the type of the input to the function
     * @param <R> the type of the result of the function
     * @param function the source function
     * @return a new {@link ThrowingFunction} instance
     */
    static <T, R> ThrowingFunction<T, R> of(final ThrowingFunction<T, R> function) {
        return function;
    }

    /**
     * Lambda friendly convenience method that can be used to create a
     * {@link ThrowingFunction} where the {@link #apply(Object)} method wraps
     * any thrown checked exceptions using the given {@code exceptionWrapper}.
     * <p>This method can be especially useful when working with method references.
     * It allows you to easily convert a method that throws a checked exception
     * into an instance compatible with a regular {@link Function}.
     * <p>For example:
     * <pre class="code">
     * stream.map(ThrowingFunction.of(Example::methodThatCanThrowCheckedException, IllegalStateException::new));
     * </pre>
     * @param <T> the type of the input to the function
     * @param <R> the type of the result of the function
     * @param function the source function
     * @param exceptionWrapper the exception wrapper to use
     * @return a new {@link ThrowingFunction} instance
     */
    static <T, R> ThrowingFunction<T, R> of(final ThrowingFunction<T, R> function,
            final BiFunction<String, Exception, RuntimeException> exceptionWrapper) {

        return function.throwing(exceptionWrapper);
    }

}
