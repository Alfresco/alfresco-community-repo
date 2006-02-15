/*
 * Copyright (C) 2005 Alfresco, Inc.
 *
 * Licensed under the Mozilla Public License version 1.1 
 * with a permitted attribution clause. You may obtain a
 * copy of the License at
 *
 *   http://www.alfresco.org/legal/license.txt
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific
 * language governing permissions and limitations under the
 * License.
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
 * Is sub class evaluator test
 * 
 * @author Roy Wetherall
 */
public class SetPropertyValueActionExecuterTest extends BaseSpringTest
{
    private NodeService nodeService;
    private StoreRef testStoreRef;
    private NodeRef rootNodeRef;
    private NodeRef nodeRef;
    private SetPropertyValueActionExecuter executer;
    
    private final static String ID = GUID.generate();
    
    private final static String TEST_VALUE = "TestValue";

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
        this.executer = (SetPropertyValueActionExecuter)this.applicationContext.getBean(SetPropertyValueActionExecuter.NAME);
    }
    
    /**
     * Test execution
     */
    public void testExecution()
    {
        // Check that the property is empty
        assertNull(this.nodeService.getProperty(this.nodeRef, ContentModel.PROP_NAME));
        
        // Execute the action
        ActionImpl action = new ActionImpl(ID, SetPropertyValueActionExecuter.NAME, null);
        action.setParameterValue(SetPropertyValueActionExecuter.PARAM_PROPERTY, ContentModel.PROP_NAME);
        action.setParameterValue(SetPropertyValueActionExecuter.PARAM_VALUE, TEST_VALUE);
        this.executer.execute(action, this.nodeRef);
        
        // Check that the property value has been set
        assertEquals(TEST_VALUE, this.nodeService.getProperty(this.nodeRef, ContentModel.PROP_NAME));
        
        // Check what happens when a bad property name is set
        action.setParameterValue(SetPropertyValueActionExecuter.PARAM_PROPERTY, QName.createQName("{test}badProperty"));
        
        try
        {
            this.executer.execute(action, this.nodeRef);
            fail("We would expect and exception to be thrown since the property name is invalid.");
        }
        catch (Throwable exception)
        {
            // Good .. we where expecting this
        }
    }
}
