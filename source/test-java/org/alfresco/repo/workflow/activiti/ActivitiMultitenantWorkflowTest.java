/*
 * Copyright (C) 2005-2010 Alfresco Software Limited.
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

package org.alfresco.repo.workflow.activiti;

import java.io.InputStream;
import java.io.Serializable;
import java.util.HashMap;

import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.workflow.AbstractMultitenantWorkflowTest;
import org.alfresco.service.cmr.workflow.WorkflowDefinition;
import org.alfresco.service.cmr.workflow.WorkflowDeployment;
import org.alfresco.service.namespace.QName;
import org.alfresco.test_category.OwnJVMTestsCategory;
import org.junit.experimental.categories.Category;


/**
 * @author Nick Smith
 * @since 4.0
 *
 */
@Category(OwnJVMTestsCategory.class)
public class ActivitiMultitenantWorkflowTest extends AbstractMultitenantWorkflowTest
{
	private static final String CALLACTIVITY_SUBPROCESS_LOCATION = "activiti/test-callactivity-subprocess.bpmn20.xml";
    private static final String CALLACTIVITY_MAINPROCESS_LOCATION = "activiti/test-callactivity-main.bpmn20.xml";
    
    
    @Override
    protected String getEngine()
    {
        return ActivitiConstants.ENGINE_ID;
    }

    @Override
    protected String getTestDefinitionPath()
    {
        return "activiti/testTransaction.bpmn20.xml";
    }
    
    @Override
    protected String getTestDefinitionKey()
    {
        return "activiti$testTask";
    }

    @Override
    protected String getAdhocDefinitionKey()
    {
        return "activiti$activitiAdhoc";
    }

    
    /**
     * ALF-15939: Call-activity should be multi-tenant aware.
     */
    public void testSubProcessCallActivity() throws Exception
    {
    	// Run as User1 so tenant domain 1
        AuthenticationUtil.setFullyAuthenticatedUser(user1);
        
        // Deploy called sub-process on tenant domain 1
        InputStream input = getInputStream(CALLACTIVITY_SUBPROCESS_LOCATION);
        WorkflowDeployment deployment = workflowService.deployDefinition(getEngine(), input, XML);
        
        // Deploy called main-process on tenant domain 1
        input = getInputStream(CALLACTIVITY_MAINPROCESS_LOCATION);
        deployment = workflowService.deployDefinition(getEngine(), input, XML);
        WorkflowDefinition mainProcessDefinition = deployment.getDefinition();
        
        // Start a process, which immediately tries to call the sub-process before returning control to thread
        try {
        	workflowService.startWorkflow(mainProcessDefinition.getId(), new HashMap<QName, Serializable>());
        } catch(Exception e) {
        	e.printStackTrace();
        	fail("No exception was expected while running process, but got: " + e.toString());
        }
    }
}
