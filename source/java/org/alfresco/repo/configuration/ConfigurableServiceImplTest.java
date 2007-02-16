/*
 * Copyright (C) 2005 Alfresco, Inc.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.

 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.

 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.

 * As a special exception to the terms and conditions of version 2.0 of 
 * the GPL, you may redistribute this Program in connection with Free/Libre 
 * and Open Source Software ("FLOSS") applications as described in Alfresco's 
 * FLOSS exception.  You should have recieved a copy of the text describing 
 * the FLOSS exception, and it is also available here: 
 * http://www.alfresco.com/legal/licensing"
 */
package org.alfresco.repo.configuration;

import java.util.List;

import org.alfresco.model.ApplicationModel;
import org.alfresco.model.ContentModel;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.namespace.RegexQNamePattern;
import org.alfresco.util.BaseSpringTest;

/**
 * Configurable service implementation test
 * 
 * @author Roy Wetherall
 */
@SuppressWarnings("unused")
public class ConfigurableServiceImplTest extends BaseSpringTest
{
	public NodeService nodeService;
    private ServiceRegistry serviceRegistry;
	private ConfigurableService configurableService;
	private StoreRef testStoreRef;
	private NodeRef rootNodeRef;
	private NodeRef nodeRef;
	
	/**
	 * onSetUpInTransaction
	 */
	@Override
	protected void onSetUpInTransaction() throws Exception
	{
		this.nodeService = (NodeService)this.applicationContext.getBean("nodeService");
		this.serviceRegistry = (ServiceRegistry)this.applicationContext.getBean(ServiceRegistry.SERVICE_REGISTRY);
		this.configurableService = (ConfigurableService)this.applicationContext.getBean("configurableService");
		
		this.testStoreRef = this.nodeService.createStore(StoreRef.PROTOCOL_WORKSPACE, "Test_" + System.currentTimeMillis());
        this.rootNodeRef = this.nodeService.getRootNode(this.testStoreRef);
        
        // Create the node used for tests
        this.nodeRef = this.nodeService.createNode(
                this.rootNodeRef,
				ContentModel.ASSOC_CHILDREN,
                ContentModel.ASSOC_CHILDREN,
                ContentModel.TYPE_CONTAINER).getChildRef();
	}

	/**
	 * Test isConfigurable
	 */
	public void testIsConfigurable()
	{
		assertFalse(this.configurableService.isConfigurable(this.nodeRef));
		this.configurableService.makeConfigurable(this.nodeRef);
		assertTrue(this.configurableService.isConfigurable(this.nodeRef));
	}
	
	/**
	 * Test make configurable
	 */
	public void testMakeConfigurable()
	{
		this.configurableService.makeConfigurable(this.nodeRef);
		assertTrue(this.nodeService.hasAspect(this.nodeRef, ApplicationModel.ASPECT_CONFIGURABLE));
		List<ChildAssociationRef> assocs = this.nodeService.getChildAssocs(
                this.nodeRef,
                RegexQNamePattern.MATCH_ALL,
                ApplicationModel.ASSOC_CONFIGURATIONS);
		assertNotNull(assocs);
		assertEquals(1, assocs.size());
	}
	
	/**
	 * Test getConfigurationFolder
	 */
	public void testGetConfigurationFolder()
	{
		assertNull(this.configurableService.getConfigurationFolder(this.nodeRef));
		this.configurableService.makeConfigurable(nodeRef);
		NodeRef configFolder = this.configurableService.getConfigurationFolder(this.nodeRef);
		assertNotNull(configFolder);
		NodeRef parentNodeRef = this.nodeService.getPrimaryParent(configFolder).getParentRef();
		assertNotNull(parentNodeRef);
		assertEquals(nodeRef, parentNodeRef);
	}
	
}
