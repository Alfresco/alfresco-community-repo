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
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package org.alfresco.repo.action.executer;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.action.ActionImpl;
import org.alfresco.repo.security.authentication.AuthenticationComponent;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.BaseSpringTest;
import org.alfresco.util.GUID;

/**
 * Add features action execution test
 * 
 * @author Roy Wetherall
 */
public class AddFeaturesActionExecuterTest extends BaseSpringTest
{
    /**
     * The node service
     */
    private NodeService nodeService;
    
    /**
     * The store reference
     */
    private StoreRef testStoreRef;
    
    /**
     * The root node reference
     */
    private NodeRef rootNodeRef;
    
    /**
     * The test node reference
     */
    private NodeRef nodeRef;
    
    /**
     * The add features action executer
     */
    private AddFeaturesActionExecuter executer;
    
    /**
     * Id used to identify the test action created
     */
    private final static String ID = GUID.generate();
    
    /**
     * Called at the begining of all tests
     */
    @Override
    protected void onSetUpInTransaction() throws Exception
    {
        this.nodeService = (NodeService)this.applicationContext.getBean("nodeService");
        
        AuthenticationComponent authenticationComponent = (AuthenticationComponent)applicationContext.getBean("authenticationComponent");
        authenticationComponent.setCurrentUser(authenticationComponent.getSystemUserName());
        
        // Create the store and get the root node
        this.testStoreRef = this.nodeService.createStore(
                StoreRef.PROTOCOL_WORKSPACE, "Test_"
                        + System.currentTimeMillis());
        this.rootNodeRef = this.nodeService.getRootNode(this.testStoreRef);

        // Create the node used for tests
        this.nodeRef = this.nodeService.createNode(
                this.rootNodeRef,
                ContentModel.ASSOC_CHILDREN,
                QName.createQName("{test}testnode"),
                ContentModel.TYPE_CONTENT).getChildRef();
        
        // Get the executer instance 
        this.executer = (AddFeaturesActionExecuter)this.applicationContext.getBean(AddFeaturesActionExecuter.NAME);
    }
    
    /**
     * Test execution
     */
    public void testExecution()
    {
        // Check that the node does not have the classifiable aspect
        assertFalse(this.nodeService.hasAspect(this.nodeRef, ContentModel.ASPECT_CLASSIFIABLE));
        
        // Execute the action
        ActionImpl action = new ActionImpl(null, ID, AddFeaturesActionExecuter.NAME, null);
        action.setParameterValue(AddFeaturesActionExecuter.PARAM_ASPECT_NAME, ContentModel.ASPECT_CLASSIFIABLE);
        this.executer.execute(action, this.nodeRef);
        
        // Check that the node now has the classifiable aspect applied
        assertTrue(this.nodeService.hasAspect(this.nodeRef, ContentModel.ASPECT_CLASSIFIABLE));
    }
}
