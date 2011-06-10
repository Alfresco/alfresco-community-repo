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

import static org.alfresco.repo.publishing.PublishingModel.TYPE_DELIVERY_CHANNEL;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import javax.annotation.Resource;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.model.FileFolderService;
import org.alfresco.service.cmr.publishing.channels.ChannelType;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.site.SiteService;
import org.alfresco.service.cmr.site.SiteVisibility;
import org.alfresco.util.GUID;
import org.junit.After;
import org.junit.Before;
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
public abstract class AbstractPublishingIntegrationTest
{
    protected static final String environmentName = "live";

    protected static final String channelTypeId = "MockChannelType";
    
    @Resource(name="publishingObjectFactory")
    protected PublishingObjectFactory factory;
    
    @Resource(name="ServiceRegistry")
    protected ServiceRegistry serviceRegistry;
    
    protected SiteService siteService;
    protected FileFolderService fileFolderService;
    protected NodeService nodeService;
    
    protected String siteId;
    protected PublishingQueueImpl queue;
    protected EnvironmentImpl environment;
    protected NodeRef docLib;

    @Before
    public void setUp() throws Exception
    {
        AuthenticationUtil.setFullyAuthenticatedUser(AuthenticationUtil.getAdminUserName());
        this.siteService = serviceRegistry.getSiteService();
        this.fileFolderService = serviceRegistry.getFileFolderService();
        this.nodeService = serviceRegistry.getNodeService();
        
        this.siteId = GUID.generate();
        siteService.createSite("test", siteId,
                "Test site created by ChannelServiceImplIntegratedTest",
                "Test site created by ChannelServiceImplIntegratedTest",
                SiteVisibility.PUBLIC);
        this.docLib = siteService.createContainer(siteId, SiteService.DOCUMENT_LIBRARY, ContentModel.TYPE_FOLDER, null);

        this.environment = (EnvironmentImpl) factory.createEnvironmentObject(siteId, environmentName);
        this.queue = (PublishingQueueImpl) environment.getPublishingQueue();
    }
    
    @After
    public void tearDown()
    {
        siteService.deleteSite(siteId);
    }

    protected ChannelType mockChannelType()
    {
        ChannelType channelType = mock(ChannelType.class);
        when(channelType.getId()).thenReturn(channelTypeId);
        when(channelType.getChannelNodeType()).thenReturn(TYPE_DELIVERY_CHANNEL);
        when(channelType.getContentRootNodeType()).thenReturn(ContentModel.TYPE_FOLDER);
        return channelType;
    }

}
