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
package org.alfresco.repo.workflow;

import java.util.Date;

import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.springframework.transaction.annotation.Transactional;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.action.ActionImpl;
import org.alfresco.repo.security.authentication.AuthenticationComponent;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.security.PersonService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.test_category.OwnJVMTestsCategory;
import org.alfresco.util.BaseSpringTest;
import org.alfresco.util.GUID;

/**
 * Start Advanced Workflow action execution test
 * 
 * @author David Caruana
 */
@Category(OwnJVMTestsCategory.class)
@Transactional
public class StartWorkflowActionExecuterTest extends BaseSpringTest
{
    private NodeService nodeService;
    private NamespaceService namespaceService;
    private PersonService personService;
    private NodeRef rootNodeRef;
    private NodeRef nodeRef;
    private StartWorkflowActionExecuter executer;

    @Before
    public void before() throws Exception
    {
        this.nodeService = (NodeService) this.applicationContext.getBean("nodeService");
        this.namespaceService = (NamespaceService) this.applicationContext.getBean("namespaceService");
        this.personService = (PersonService) this.applicationContext.getBean("personService");

        AuthenticationComponent authenticationComponent = (AuthenticationComponent) applicationContext.getBean("authenticationComponent");
        authenticationComponent.setCurrentUser(authenticationComponent.getSystemUserName());

        // Create the store and get the root node
        rootNodeRef = nodeService.getRootNode(new StoreRef(StoreRef.PROTOCOL_WORKSPACE, "SpacesStore"));
        this.nodeRef = this.nodeService.createNode(
                this.rootNodeRef,
                ContentModel.ASSOC_CHILDREN,
                QName.createQName("{test}testnode"),
                ContentModel.TYPE_CONTENT).getChildRef();

        // Get the executer instance
        this.executer = (StartWorkflowActionExecuter) this.applicationContext.getBean(StartWorkflowActionExecuter.NAME);
    }

    /**
     * Test execution
     */
    @Test
    public void testExecution()
    {
        // Execute the action
        ActionImpl action = new ActionImpl(null, GUID.generate(), StartWorkflowActionExecuter.NAME, null);
        action.setParameterValue(StartWorkflowActionExecuter.PARAM_WORKFLOW_NAME, "activiti$activitiReview");
        action.setParameterValue(WorkflowModel.PROP_WORKFLOW_DUE_DATE.toPrefixString(namespaceService), new Date());
        NodeRef reviewer = personService.getPerson(AuthenticationUtil.getAdminUserName());
        action.setParameterValue(WorkflowModel.ASSOC_ASSIGNEE.toPrefixString(namespaceService), reviewer);
        executer.execute(action, this.nodeRef);
    }

}
