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
package org.alfresco.repo.search.impl.elasticsearch;

import org.alfresco.repo.management.subsystems.ChildApplicationContextFactory;
import org.alfresco.repo.search.impl.elasticsearch.admin.ElasticsearchStatsService;
import org.springframework.context.ApplicationContext;

import java.util.List;
import java.util.OptionalLong;
import java.util.Set;
import java.util.TreeSet;
import java.util.function.Supplier;

import static org.alfresco.repo.search.impl.elasticsearch.ElasticsearchContextProperties.*;

/**
 * Context Factory for Elasticsearch Search subsystem.
 *
 * This class includes some properties (like "Id for last Node in index" or "Approx. nodes remaining") that are NOT updateable but can be used for Administering purposes.
 *
 */
public class ElasticsearchChildApplicationContextFactory extends ChildApplicationContextFactory
{
    /**
     * List of administering properties
     */
    private static final List<String> ADM_PROPERTY_NAMES = List.of(NODES_COUNT, ELASTICSEARCH_DOCUMENT_COUNT, ES_CONTENT_INDEX_SUCCESS_COUNT,
            ES_CONTENT_INDEX_FAILURES_COUNT, ES_CONTENT_INDEX_NEW_PROGRESS_COUNT, ES_CONTENT_INDEX_UPDATE_PROGRESS_COUNT);

    private ElasticsearchStatsService elasticsearchStatsService;

    /**
     * Check updateable status for a property name
     *
     * @param name
     *            property name
     * @return true if the name of the property is not
     */
    @Override
    public boolean isUpdateable(String name)
    {
        return super.isUpdateable(name) && !ADM_PROPERTY_NAMES.contains(name);
    }

    /**
     * Sets the value of a property
     *
     * @param name
     *            property name
     * @param value
     *            property value
     */
    @Override
    public void setProperty(String name, String value)
    {
        if (!isUpdateable(name))
        {
            throw new IllegalStateException("Illegal write to property \"" + name + "\"");
        }
        super.setProperty(name, value);
    }

    /**
     * Gets the value of a property
     *
     * @param name
     *            property name
     * @return value of the property
     */
    @Override
    public String getProperty(String name)
    {
        if (!isUpdateable(name))
        {
            // to avoid useless computation, if the subsystem is not active, the return will be always UNAVAILABLE
            if (!isElasticsearchSubsystemActive())
            {
                return UNAVAILABLE;
            }
            switch (name)
            {
            case ELASTICSEARCH_DOCUMENT_COUNT:
                return retrieveCounter(getElasticsearchStatsService()::getCount);
            case ES_CONTENT_INDEX_SUCCESS_COUNT:
                return retrieveCounter(getElasticsearchStatsService()::getContentIndexingSuccessCount);
            case ES_CONTENT_INDEX_FAILURES_COUNT:
                return retrieveCounter(getElasticsearchStatsService()::getContentIndexingFailuresCount);
            case ES_CONTENT_INDEX_NEW_PROGRESS_COUNT:
                return retrieveCounter(getElasticsearchStatsService()::getNewContentIndexingInProgressCount);
            case ES_CONTENT_INDEX_UPDATE_PROGRESS_COUNT:
                return retrieveCounter(getElasticsearchStatsService()::getOutdatedContentIndexingInProgressCount);
            default:
                return null;
            }
        }
        else
        {
            return super.getProperty(name);
        }
    }

    /**
     * Gets all the property names, including administering properties
     *
     * @return Set of property names
     */
    @Override
    public Set<String> getPropertyNames()
    {
        Set<String> result = new TreeSet<>();
        result.addAll(ADM_PROPERTY_NAMES);
        result.addAll(super.getPropertyNames());
        return result;
    }

    /**
     * Get the ElasticsearchStatsService from the Spring context.
     *
     * @return The statistics service.
     */
    public ElasticsearchStatsService getElasticsearchStatsService()
    {
        if (elasticsearchStatsService == null)
        {
            ApplicationContext ctx = getApplicationContext();
            elasticsearchStatsService = (ElasticsearchStatsService) ctx.getBean("elasticsearchStatsService");
        }
        return elasticsearchStatsService;
    }

    /**
     *
     * @return true if the Elasticsearch Spring context is selected
     */
    public boolean isElasticsearchSubsystemActive()
    {
        return ((ApplicationContextState) getState(false)).getApplicationContext(false) != null;
    }

    private String retrieveCounter(Supplier<OptionalLong> countSupplier)
    {
        var countOptional = countSupplier.get();
        return countOptional.isPresent() ? String.valueOf(countOptional.getAsLong()) : UNAVAILABLE;
    }
}
