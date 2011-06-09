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

package org.alfresco.repo.publishing;

import javax.annotation.Resource;

import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.service.cmr.publishing.Environment;
import org.alfresco.service.cmr.publishing.PublishingQueue;
import org.alfresco.service.cmr.site.SiteService;
import org.alfresco.service.cmr.site.SiteVisibility;
import org.alfresco.util.GUID;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.transaction.TransactionConfiguration;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author Nick Smith
 * @since 4.0
 *
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath:alfresco/application-context.xml" })
@TransactionConfiguration(transactionManager = "transactionManager", defaultRollback = true)
@Transactional
public class PublishQueueImplTest
{
    private static final String environmentName = "live";
    
    @Resource(name="publishingObjectFactory")
    private PublishingObjectFactory factory;
    
    @Resource(name="SiteService")
    private SiteService siteService;
    
    private PublishingQueue queue;

    private String siteId;
    
    @Test
    public void testSchedulePublishingEvent() throws Exception
    {
        
    }
    
    @Before
    public void setUp()
    {
        AuthenticationUtil.setFullyAuthenticatedUser(AuthenticationUtil.getAdminUserName());
        

        this.siteId = GUID.generate();
        siteService.createSite("test", siteId,
                "Test site created by ChannelServiceImplIntegratedTest",
                "Test site created by ChannelServiceImplIntegratedTest",
                SiteVisibility.PUBLIC);

        Environment environment = factory.createEnvironmentObject(siteId, environmentName);
        this.queue = environment.getPublishingQueue();
    }
    
    @After
    public void tearDown()
    {
        siteService.deleteSite(siteId);
    }
}
