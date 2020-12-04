/*
 * #%L
 * Alfresco Repository
 * %%
 * Copyright (C) 2005 - 2016 Alfresco Software Limited
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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import org.alfresco.repo.management.subsystems.SwitchableApplicationContextFactory;
import org.alfresco.util.BaseSpringTest;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.context.ApplicationContext;

public class SolrChildApplicationContextFactoryTest extends BaseSpringTest
{
    
    /**
     * Sample partial response from http://127.0.0.1:8983/solr/admin/cores?action=SUMMARY&wt=json
     */
    private static JSONObject json = new JSONObject(
                "{\n"
                + "   \"responseHeader\":{\n"
                + "      \"status\":0,\n"
                + "      \"QTime\":1221\n"
                + "   },\n"
                + "   \"Summary\":{\n"
                + "      \"alfresco\":{\n"
                + "         \"Total Searcher Cache (GB)\":0,\n"
                + "         \"On disk (GB)\":\"0.006584\",\n"
                + "         \"Active\":false,\n"
                + "         \"TX Lag\":\"0 s\",\n"
                + "         \"TX Duration\":\"P0Y\",\n"
                + "         \"Approx transactions remaining\":0,\n"
                + "         \"Approx transaction indexing time remaining\":\"0 Seconds\",\n"
                + "         \"Id for last TX in index\":16\n"
                + "      },\n"
                + "      \"archive\":{\n"
                + "         \"Total Searcher Cache (GB)\":1,\n"
                + "         \"On disk (GB)\":\"0.001554\",\n"
                + "         \"Active\":true,\n"
                + "         \"TX Lag\":\"1 s\",\n"
                + "         \"TX Duration\":\"P1Y\",\n"
                + "         \"Approx transactions remaining\":1,\n"
                + "         \"Approx transaction indexing time remaining\":\"30 Seconds\",\n"
                + "         \"Id for last TX in index\":15\n"
                + "      }\n"
                + "   }\n"
                + "}");

    private SolrChildApplicationContextFactory solrChildApplicationContextFactory;
    
    @Mock
    private SolrAdminHTTPClient adminClient;

    @Before
    public void before()
    {

        SwitchableApplicationContextFactory subsystemManager = ((SwitchableApplicationContextFactory) applicationContext
                    .getBean("Search"));
        ApplicationContext solrContext = subsystemManager.getApplicationContext();
        solrChildApplicationContextFactory = (SolrChildApplicationContextFactory) solrContext.getBean("solr6");
        
        adminClient = Mockito.mock(SolrAdminHTTPClient.class);
        when(adminClient.execute(any())).thenReturn(json);
        solrChildApplicationContextFactory.setAdminClient(adminClient);
        
    }

    @Test
    public void test()
    {
        assertEquals(solrChildApplicationContextFactory.getProperty(
                    SolrChildApplicationContextFactory.ALFRESCO_ACTIVE), "false");
        assertEquals(solrChildApplicationContextFactory.getProperty(
                    SolrChildApplicationContextFactory.ALFRESCO_LAG), "0 s");
        assertEquals(solrChildApplicationContextFactory.getProperty(
                    SolrChildApplicationContextFactory.ALFRESCO_LAG_DURATION), "P0Y");
        assertEquals(solrChildApplicationContextFactory.getProperty(
                    SolrChildApplicationContextFactory.ALFRESCO_LAST_INDEXED_TXN), "16");
        assertEquals(solrChildApplicationContextFactory.getProperty(
                    SolrChildApplicationContextFactory.ALFRESCO_APPROX_TXNS_REMAINING), "0");
        assertEquals(solrChildApplicationContextFactory.getProperty(
                    SolrChildApplicationContextFactory.ALFRESCO_APPROX_INDEXING_TIME_REMAINING), "0 Seconds");
        assertEquals(solrChildApplicationContextFactory.getProperty(
                    SolrChildApplicationContextFactory.ALFRESCO_DISK), "0.006584");
        assertEquals(solrChildApplicationContextFactory.getProperty(SolrChildApplicationContextFactory.ALFRESCO_MEMORY), "0");

        assertEquals(solrChildApplicationContextFactory.getProperty(
                    SolrChildApplicationContextFactory.ARCHIVE_ACTIVE), "true");
        assertEquals(solrChildApplicationContextFactory.getProperty(
                    SolrChildApplicationContextFactory.ARCHIVE_LAG), "1 s");
        assertEquals(solrChildApplicationContextFactory.getProperty(
                    SolrChildApplicationContextFactory.ARCHIVE_LAG_DURATION), "P1Y");
        assertEquals(solrChildApplicationContextFactory.getProperty(
                    SolrChildApplicationContextFactory.ARCHIVE_LAST_INDEXED_TXN), "15");
        assertEquals(solrChildApplicationContextFactory.getProperty(
                    SolrChildApplicationContextFactory.ARCHIVE_APPROX_TXNS_REMAINING), "1");
        assertEquals(solrChildApplicationContextFactory.getProperty(
                    SolrChildApplicationContextFactory.ARCHIVE_APPROX_INDEXING_TIME_REMAINING), "30 Seconds");
        assertEquals(solrChildApplicationContextFactory.getProperty(
                    SolrChildApplicationContextFactory.ARCHIVE_DISK), "0.001554");
        assertEquals(solrChildApplicationContextFactory.getProperty(
                    SolrChildApplicationContextFactory.ARCHIVE_MEMORY), "1");
    }

}
