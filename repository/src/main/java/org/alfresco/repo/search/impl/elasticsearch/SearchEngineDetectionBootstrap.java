/*
 * #%L
 * Alfresco Repository
 * %%
 * Copyright (C) 2005 - 2026 Alfresco Software Limited
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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.context.ApplicationEvent;
import org.springframework.extensions.surf.util.AbstractLifecycleBean;

import org.alfresco.repo.management.subsystems.SwitchableApplicationContextFactory;

/**
 * Ensures the search engine is detected at repository start-up (ACS-12415).
 * <p>
 * The {@link SearchEngineInfoDetector} runs as an {@code init-method} inside the elasticsearch
 * search subsystem, so it only executes when that subsystem's child application context is started.
 * Because the {@code Search} subsystem is configured with {@code autoStart=false}, it is otherwise
 * started lazily on the first search request, which means detection would not happen "as soon as ACS
 * runs".
 * <p>
 * This bean lives in the parent (repository) application context and follows the standard Alfresco
 * start-up pattern ({@link AbstractLifecycleBean#onBootstrap(ApplicationEvent)}, as used by
 * {@code RepositoryStartBootstrapBean}, {@code SiteServiceBootstrap} and others). On bootstrap it
 * checks which search subsystem is configured via {@link SwitchableApplicationContextFactory#getCurrentSourceBeanName()}
 * (which does <em>not</em> start the subsystem) and, only when Elasticsearch/OpenSearch is the active
 * engine, forces the subsystem to start by calling {@link SwitchableApplicationContextFactory#getApplicationContext()}.
 * Starting the subsystem triggers {@link SearchEngineInfoDetector#detect()}.
 * <p>
 * When a different engine is configured (for example {@code solr6} or {@code noindex}) the
 * elasticsearch subsystem is not loaded, so this bean does nothing. Any failure to start the
 * subsystem is logged as a warning and never blocks repository start-up.
 */
public class SearchEngineDetectionBootstrap extends AbstractLifecycleBean
{
    private static final Log logger = LogFactory.getLog(SearchEngineDetectionBootstrap.class);

    private SwitchableApplicationContextFactory searchSubsystem;
    private String elasticsearchSubsystemName = "elasticsearch";

    public void setSearchSubsystem(SwitchableApplicationContextFactory searchSubsystem)
    {
        this.searchSubsystem = searchSubsystem;
    }

    /**
     * @param elasticsearchSubsystemName
     *            the source bean name that identifies the Elasticsearch/OpenSearch subsystem (i.e. the
     *            value of {@code index.subsystem.name} for which detection should run). Defaults to
     *            {@code elasticsearch}.
     */
    public void setElasticsearchSubsystemName(String elasticsearchSubsystemName)
    {
        this.elasticsearchSubsystemName = elasticsearchSubsystemName;
    }

    @Override
    protected void onBootstrap(ApplicationEvent event)
    {
        if (searchSubsystem == null)
        {
            return;
        }

        String activeSubsystem = searchSubsystem.getCurrentSourceBeanName();
        if (elasticsearchSubsystemName.equalsIgnoreCase(activeSubsystem))
        {
            if (logger.isDebugEnabled())
            {
                logger.debug("'" + activeSubsystem
                        + "' is the active search subsystem; starting it so the search engine can be detected.");
            }
            try
            {
                // Forces the subsystem's child context to start, which runs SearchEngineInfoDetector.detect().
                searchSubsystem.getApplicationContext();
            }
            catch (RuntimeException e)
            {
                logger.warn("Unable to start the elasticsearch search subsystem during bootstrap to detect the "
                        + "search engine; detection will be retried on first search. Cause: " + e.getMessage(), e);
            }
        }
        else if (logger.isDebugEnabled())
        {
            logger.debug("Active search subsystem is '" + activeSubsystem
                    + "', not '" + elasticsearchSubsystemName + "'; skipping search engine detection.");
        }
    }

    @Override
    protected void onShutdown(ApplicationEvent event)
    {
        // No-op
    }
}
