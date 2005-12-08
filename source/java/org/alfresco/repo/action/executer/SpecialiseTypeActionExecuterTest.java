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
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.BaseAlfrescoSpringTest;
import org.alfresco.util.GUID;

/**
 * Specialise type action execution test
 * 
 * @author Roy Wetherall
 */
public class SpecialiseTypeActionExecuterTest extends BaseAlfrescoSpringTest
{    
    /**
     * The test node reference
     */
    private NodeRef nodeRef;
    
    /**
     * The specialise action executer
     */
    private SpecialiseTypeActionExecuter executer;
    
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
        super.onSetUpInTransaction();
        
        // Create the node used for tests
        this.nodeRef = this.nodeService.createNode(
                this.rootNodeRef,
                ContentModel.ASSOC_CHILDREN,
                QName.createQName("{test}testnode"),
                ContentModel.TYPE_CONTENT).getChildRef();
        
        // Get the executer instance 
        this.executer = (SpecialiseTypeActionExecuter)this.applicationContext.getBean(SpecialiseTypeActionExecuter.NAME);
    }
    
    /**
     * Test execution
     */
    public void testExecution()
    {
        // Check the type of the node
        assertEquals(ContentModel.TYPE_CONTENT, this.nodeService.getType(this.nodeRef));
        
        // Execute the action
        ActionImpl action = new ActionImpl(ID, SpecialiseTypeActionExecuter.NAME, null);
        action.setParameterValue(SpecialiseTypeActionExecuter.PARAM_TYPE_NAME, ContentModel.TYPE_FOLDER);
        this.executer.execute(action, this.nodeRef);
        
        // Check that the node's type has not been changed since it would not be a specialisation
        assertEquals(ContentModel.TYPE_CONTENT, this.nodeService.getType(this.nodeRef));
        
        // Execute the action agian
        action.setParameterValue(SpecialiseTypeActionExecuter.PARAM_TYPE_NAME, ContentModel.TYPE_DICTIONARY_MODEL);
        this.executer.execute(action, this.nodeRef);
        
        // Check that the node's type has now been changed
        assertEquals(ContentModel.TYPE_DICTIONARY_MODEL, this.nodeService.getType(this.nodeRef));
    }
}
