/*
 * #%L
 * Alfresco Repository
 * %%
 * Copyright (C) 2005 - 2024 Alfresco Software Limited
 * %%
 * This file is part of the Alfresco software.
 * If the software was purchased under a paid Alfresco license, the terms of
 * the paid license agreement will prevail.  Otherwise, the software is
 * provided under the following open source license terms:
 *
 * Alfresco is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Alfresco is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Alfresco. If not, see <http://www.gnu.org/licenses/>.
 * #L%
 */
package org.alfresco.util;

import java.util.Objects;

/**
 * Represents a predicate (boolean-valued function) of three arguments. This is the three-arity specialization of Predicate.
 * This is a functional interface whose functional method is test(Object, Object, Object).
 *
 * @param <T> - type of the first argument to the predicate
 * @param <U> - type of the second argument the predicate
 * @param <V> - type of the third argument the predicate
 */
@FunctionalInterface
public interface TriPredicate<T, U, V>
{

    /**
     * Evaluates this predicate on the given arguments.
     *
     * @param t - first input argument
     * @param u - second input argument
     * @param v - third input argument
     * @return true if the input arguments match the predicate, otherwise false
     */
    boolean test(T t, U u, V v);

    /**
     * Creates a composed predicate that represents a logical AND of this predicate and another.
     *
     * @param other - predicate that will be logically-ANDed with this predicate
     * @return composed predicate
     */
    default TriPredicate<T, U, V> and(TriPredicate<? super T, ? super U, ? super V> other)
    {
        Objects.requireNonNull(other);
        return (T t, U u, V v) -> test(t, u, v) && other.test(t, u, v);
    }

    /**
     * @return a predicate that represents the logical negation of this predicate
     */
    default TriPredicate<T, U, V> negate()
    {
        return (T t, U u, V v) -> !test(t, u, v);
    }

    /**
     * Creates a composed predicate that represents a logical OR of this predicate and another.
     *
     * @param other - predicate that will be logically-ORed with this predicate
     * @return composed predicate
     */
    default TriPredicate<T, U, V> or(TriPredicate<? super T, ? super U, ? super V> other)
    {
        Objects.requireNonNull(other);
        return (T t, U u, V v) -> test(t, u, v) || other.test(t, u, v);
    }
}
