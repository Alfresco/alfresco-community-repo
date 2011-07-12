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

import javax.transaction.HeuristicMixedException;
import javax.transaction.HeuristicRollbackException;
import javax.transaction.RollbackException;
import javax.transaction.Status;
import javax.transaction.SystemException;
import javax.transaction.UserTransaction;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.model.FileFolderService;
import org.alfresco.service.cmr.publishing.channels.ChannelType;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.site.SiteService;
import org.alfresco.service.cmr.site.SiteVisibility;
import org.alfresco.util.BaseSpringTest;
import org.alfresco.util.GUID;
import org.junit.After;
import org.junit.Before;
import org.mockito.Mockito;

/**
 * @author Nick Smith
 * @since 4.0
 *
 */
public abstract class AbstractPublishingIntegrationTest extends BaseSpringTest
{
    protected static final String channelTypeId = "MockChannelType";
    
    protected PublishingObjectFactory factory;
    
    protected ServiceRegistry serviceRegistry;
    
    protected SiteService siteService;
    protected FileFolderService fileFolderService;
    protected NodeService nodeService;
    
    protected String siteId;
    protected PublishingQueueImpl queue;
    protected EnvironmentImpl environment;
    protected NodeRef docLib;

    protected UserTransaction transaction;

    @Before
    public void onSetUp() throws Exception
    {
        factory = (PublishingObjectFactory) getApplicationContext().getBean("publishingObjectFactory");
        serviceRegistry = (ServiceRegistry) getApplicationContext().getBean("ServiceRegistry");
        AuthenticationUtil.setFullyAuthenticatedUser(AuthenticationUtil.getAdminUserName());
        this.siteService = serviceRegistry.getSiteService();
        this.fileFolderService = serviceRegistry.getFileFolderService();
        this.nodeService = serviceRegistry.getNodeService();
        
        transaction = serviceRegistry.getTransactionService().getUserTransaction();
        transaction.begin();
        transaction.setRollbackOnly();
        
        this.siteId = GUID.generate();
        siteService.createSite("test", siteId,
                "Site created by publishing test",
                "Site created by publishing test",
                SiteVisibility.PUBLIC);
        this.docLib = siteService.createContainer(siteId, SiteService.DOCUMENT_LIBRARY, ContentModel.TYPE_FOLDER, null);

        this.environment = (EnvironmentImpl) factory.createEnvironmentObject(siteId);
        this.queue = (PublishingQueueImpl) environment.getPublishingQueue();
    }
    
    @After
    public void onTearDown()
    {
        siteService.deleteSite(siteId);
        try
        {
            if (transaction.getStatus() == Status.STATUS_MARKED_ROLLBACK)
            {
                transaction.rollback();
            }
            else
            {
                transaction.commit();
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    protected ChannelType mockChannelType()
    {
        ChannelType channelType = mock(ChannelType.class);
        mockChannelTypeBehaviour(channelType);
        return channelType;
    }

    protected void mockChannelTypeBehaviour(ChannelType channelType)
    {
        when(channelType.getId()).thenReturn(channelTypeId);
        when(channelType.getChannelNodeType()).thenReturn(TYPE_DELIVERY_CHANNEL);
    }

}
