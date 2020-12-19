/*
 * #%L
 * Alfresco Repository
 * %%
 * Copyright (C) 2005 - 2020 Alfresco Software Limited
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

package org.alfresco.repo.search.impl.solr;

import org.alfresco.repo.management.subsystems.SwitchableApplicationContextFactory;
import org.alfresco.repo.solr.SOLRTrackingComponentImpl;
import org.alfresco.util.BaseSpringTest;
import org.junit.Before;
import org.junit.Test;
import org.springframework.context.ApplicationContext;

public class SolrSubsystemTest extends BaseSpringTest
{

    private SOLRTrackingComponentImpl solr6TrackingBean;

    @Before
    public void buildElasticsearchClient() throws Exception
    {
        SwitchableApplicationContextFactory subsystemManager = ((SwitchableApplicationContextFactory) applicationContext
                    .getBean("Search"));
        ApplicationContext solrContext = subsystemManager.getApplicationContext();
        solr6TrackingBean = (SOLRTrackingComponentImpl) solrContext.getBean("search.trackingComponent");
        assertNotNull(solr6TrackingBean);
    }

    /**
     * Solr Tracking Bean doesn't include Shard Registry reference for Community
     */
    @Test
    public void shouldNotShardRegistryAttachedToSolrBean()
    {
        assertNull(solr6TrackingBean.getShardRegistry());
    }

}