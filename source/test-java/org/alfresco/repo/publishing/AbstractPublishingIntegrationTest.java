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

import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.security.person.TestPersonManager;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.model.FileFolderService;
import org.alfresco.service.cmr.publishing.PublishingService;
import org.alfresco.service.cmr.publishing.channels.ChannelService;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.security.MutableAuthenticationService;
import org.alfresco.service.cmr.security.PermissionService;
import org.alfresco.service.cmr.security.PersonService;
import org.alfresco.service.cmr.site.SiteService;
import org.alfresco.util.BaseSpringTest;
import org.alfresco.util.GUID;
import org.junit.After;
import org.junit.Before;

/**
 * @author Nick Smith
 * @since 4.0
 */
public abstract class AbstractPublishingIntegrationTest extends BaseSpringTest
{
    protected static final String channelTypeId = "MockChannelType";
    
    protected ServiceRegistry serviceRegistry;
    protected NodeService nodeService;
    protected PublishingTestHelper testHelper;
    protected TestPersonManager personManager;
    
    protected String username;
    
    @Override
    @Before
    public void onSetUp() throws Exception
    {
        super.onSetUp();
        serviceRegistry = (ServiceRegistry) getApplicationContext().getBean(ServiceRegistry.SERVICE_REGISTRY);
        AuthenticationUtil.setAdminUserAsFullyAuthenticatedUser();
        SiteService siteService = serviceRegistry.getSiteService();
        FileFolderService fileFolderService = serviceRegistry.getFileFolderService();
        PermissionService permissionService = serviceRegistry.getPermissionService();
        this.nodeService = serviceRegistry.getNodeService();
        ChannelService channelService = (ChannelService) getApplicationContext().getBean(ChannelServiceImpl.NAME);
        PublishingService publishingService = (PublishingService) getApplicationContext().getBean(PublishServiceImpl.NAME);
        MutableAuthenticationService authenticationService= (MutableAuthenticationService) getApplicationContext().getBean(ServiceRegistry.AUTHENTICATION_SERVICE.getLocalName());
        PersonService personService= (PersonService) getApplicationContext().getBean(ServiceRegistry.PERSON_SERVICE.getLocalName());
        
        this.personManager = new TestPersonManager(authenticationService, personService, nodeService);
        this.testHelper = new PublishingTestHelper(channelService, publishingService, siteService, fileFolderService, permissionService);
        
        this.username = GUID.generate();
        personManager.createPerson(username);
    }
    
    @After
    public void onTearDown() throws Exception
    {
        try
        {
            testHelper.tearDown();
        }
        finally
        {
            super.onTearDown();
        }
    }
}
