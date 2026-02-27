/*
 * #%L
 * Alfresco Repository
 * %%
 * Copyright (C) 2005 - 2025 Alfresco Software Limited
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
package org.alfresco.repo.search.impl.elasticsearch.query.language.afts;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import org.opensearch.client.opensearch._types.query_dsl.Query;
import org.opensearch.client.opensearch._types.query_dsl.QueryBase;
import org.opensearch.client.opensearch._types.query_dsl.QueryVariant;
import org.opensearch.client.util.ObjectBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.ReflectionUtils;

import org.alfresco.error.AlfrescoRuntimeException;

/**
 * Helper class to add a boost to a query.
 */
public class QueryBooster extends QueryBase
{
    private static final Logger LOGGER = LoggerFactory.getLogger(QueryBooster.class);
    private static final Map<Class<?>, Method> QUERY_TO_BUILDER_CACHE = new ConcurrentHashMap<>();

    private QueryBooster()
    {
        super(null);
        throw new AlfrescoRuntimeException("Do not instantiate this class.");
    }

    public static Query boost(Query query, float boost)
    {
        return getQueryBuilder(query)
                .map(queryBuilder -> boost((ObjectBuilder<? extends QueryBase>) queryBuilder, boost))
                .or(() -> findBoostField(query).map(boostField -> boost(boostField, query, boost)))
                .orElse(query);
    }

    private static Optional<Object> getQueryBuilder(Query query)
    {
        Object originalQuery = query._get();
        return Optional.ofNullable(originalQuery)
                .filter(QueryVariant.class::isInstance)
                .filter(QueryBase.class::isInstance)
                .map(Object::getClass)
                .filter(QueryBase.class::isAssignableFrom)
                .flatMap(QueryBooster::findToBuilderMethod)
                .map(method -> invokeToBuilder(method, originalQuery))
                .filter(ObjectBuilder.class::isInstance);
    }

    private static Object invokeToBuilder(Method method, Object originalQuery)
    {
        try
        {
            return ReflectionUtils.invokeMethod(method, originalQuery);
        }
        catch (Exception e)
        {
            LOGGER.debug("Failed to invoke toBuilder() on existing query to set boost field value.", e);
            return null;
        }
    }

    private static Query boost(ObjectBuilder<? extends QueryBase> originalBuilder, float boost)
    {
        ((QueryBase.AbstractBuilder<?>) originalBuilder).boost(boost);
        return ((QueryVariant) originalBuilder.build()).toQuery();
    }

    private static Query boost(Field boostField, Query query, float boost)
    {
        try
        {
            ReflectionUtils.setField(boostField, query, boost);
        }
        catch (Exception e)
        {
            LOGGER.debug("Failed to set boost field on existing query.", e);
        }

        return query;
    }

    private static Optional<Method> findToBuilderMethod(Class<?> queryClass)
    {
        return Optional.ofNullable(
                QUERY_TO_BUILDER_CACHE.computeIfAbsent(queryClass, clazz -> ReflectionUtils.findMethod(clazz, "toBuilder")));
    }

    private static Optional<Field> findBoostField(Query query)
    {
        return Optional.ofNullable(ReflectionUtils.findField(query.getClass(), "boost"));
    }
}
