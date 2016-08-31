/*
 * Copyright (C) 2005-2011 Alfresco Software Limited.
 *
 * This file is part of Alfresco
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
 */
package org.alfresco.query;

import org.alfresco.util.GUID;
import org.alfresco.util.PropertyCheck;
import org.alfresco.util.registry.NamedObjectRegistry;
import org.springframework.beans.factory.BeanNameAware;
import org.springframework.beans.factory.InitializingBean;

/**
 * Basic services for {@link CannedQueryFactory} implementations.
 * 
 * @author Derek Hulley
 * @since 4.0
 */
public abstract class AbstractCannedQueryFactory<R> implements CannedQueryFactory<R>, InitializingBean, BeanNameAware
{
    private String name;
    @SuppressWarnings("rawtypes")
    private NamedObjectRegistry<CannedQueryFactory> registry;

    /**
     * Set the name with which to {@link #setRegistry(NamedObjectRegistry) register}
     * @param name          the name of the bean
     */
    public void setBeanName(String name)
    {
        this.name = name;
    }

    /**
     * Set the registry with which to register
     */
    @SuppressWarnings("rawtypes")
    public void setRegistry(NamedObjectRegistry<CannedQueryFactory> registry)
    {
        this.registry = registry;
    }

    /**
     * Registers the instance
     */
    public void afterPropertiesSet() throws Exception
    {
        PropertyCheck.mandatory(this, "name", name);
        PropertyCheck.mandatory(this, "registry", registry);

        registry.register(name, this);
    }
    
    /**
     * Helper method to construct a unique query execution ID based on the
     * instance of the factory and the parameters provided.
     * 
     * @param parameters                the query parameters
     * @return                          a unique query instance ID
     */
    protected String getQueryExecutionId(CannedQueryParameters parameters)
    {
        // Create a GUID
        String uuid = name + "-" + GUID.generate();
        return uuid;
    }
    
    /** 
     * {@inheritDoc}
     */
    @Override
    public CannedQuery<R> getCannedQuery(Object parameterBean, int skipResults, int pageSize, String queryExecutionId)
    {
        return getCannedQuery(new CannedQueryParameters(parameterBean, skipResults, pageSize, queryExecutionId));
    }
}
