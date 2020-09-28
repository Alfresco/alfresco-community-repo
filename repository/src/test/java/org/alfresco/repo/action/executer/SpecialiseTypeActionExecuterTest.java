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
package org.alfresco.repo.action.executer;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.action.ActionImpl;
import org.alfresco.repo.security.authentication.AuthenticationComponent;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.service.cmr.dictionary.InvalidTypeException;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.security.PermissionService;
import org.alfresco.service.namespace.QName;
import org.alfresco.test_category.BaseSpringTestsCategory;
import org.alfresco.util.BaseAlfrescoSpringTest;
import org.alfresco.util.GUID;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.springframework.transaction.annotation.Transactional;

/**
 * Specialise type action execution test
 *
 * @author Roy Wetherall
 */
@Category(BaseSpringTestsCategory.class)
@Transactional
public class SpecialiseTypeActionExecuterTest extends BaseAlfrescoSpringTest
{
    /**
     * Id used to identify the test action created
     */
    private final static String ID = GUID.generate();
    /**
     * The specialise action executer
     */
    private SpecialiseTypeActionExecuter executer;
    /**
     * The test node reference
     */
    private NodeRef nodeRef1;
    private NodeRef nodeRef2;
    private NodeRef nodeRef3;

    /**
     * Called at the beginning of all tests
     */
    @Before public void before() throws Exception
    {
        super.before();

        // Create the node used for tests
        this.nodeRef1 = this.nodeService
            .createNode(this.rootNodeRef, ContentModel.ASSOC_CHILDREN, QName.createQName("{test}testnode"), ContentModel.TYPE_CONTENT).getChildRef();

        // Create the node used for tests
        this.nodeRef2 = this.nodeService
            .createNode(this.rootNodeRef, ContentModel.ASSOC_CHILDREN, QName.createQName("{test}testnode"), ContentModel.TYPE_CONTENT).getChildRef();

        // Create the node used for tests
        this.nodeRef3 = this.nodeService
            .createNode(this.rootNodeRef, ContentModel.ASSOC_CHILDREN, QName.createQName("{test}testnode"), ContentModel.TYPE_CONTENT).getChildRef();

        // Get the executer instance
        this.executer = (SpecialiseTypeActionExecuter) this.applicationContext.getBean(SpecialiseTypeActionExecuter.NAME);
    }

    /**
     * Test execution
     */
    @Test public void testExecution()
    {
        // check with "Admin" user
        {
            AuthenticationUtil.setFullyAuthenticatedUser(AuthenticationUtil.getAdminUserName());

            // Check the type of the node
            ActionImpl action = checkActionToChangeNodeType(nodeRef1);

            // Execute the action again
            checkActionAgainAndExpectTypeToChange(action, nodeRef1);
        }

        // check with "System user"
        {
            AuthenticationUtil.setFullyAuthenticatedUser(AuthenticationUtil.getSystemUserName());

            // Check the type of the node
            ActionImpl action = checkActionToChangeNodeType(nodeRef2);

            // Execute the action again
            checkActionAgainAndExpectTypeToChange(action, nodeRef2);
        }

        //Check with normal user
        {
            //Create user as coordinator without administrator writes
            String userName = "bob" + GUID.generate();
            createUser(userName);
            PermissionService permissionService = (PermissionService) applicationContext.getBean("PermissionService");
            permissionService.setPermission(nodeRef3, userName, PermissionService.COORDINATOR, true);

            AuthenticationUtil.setFullyAuthenticatedUser(userName);

            // Check the type of the node
            ActionImpl action = checkActionToChangeNodeType(nodeRef3);

            try
            {
                // Execute the action again
                action.setParameterValue(SpecialiseTypeActionExecuter.PARAM_TYPE_NAME, ContentModel.TYPE_DICTIONARY_MODEL);
                this.executer.execute(action, this.nodeRef3);

                fail("The creation should NOT succeed because the code is not executed but Admin or System user/role");
            }
            catch (InvalidTypeException ex)
            {
            }
        }
    }

    private void checkActionAgainAndExpectTypeToChange(ActionImpl action, NodeRef nodeRef)
    {
        // Execute the action again
        action.setParameterValue(SpecialiseTypeActionExecuter.PARAM_TYPE_NAME, ContentModel.TYPE_DICTIONARY_MODEL);
        this.executer.execute(action, nodeRef);

        // Check that the node's type has now been changed
        assertEquals(ContentModel.TYPE_DICTIONARY_MODEL, this.nodeService.getType(nodeRef));
    }

    private ActionImpl checkActionToChangeNodeType(NodeRef nodeRef)
    {
        // Check the type of the node
        assertEquals(ContentModel.TYPE_CONTENT, this.nodeService.getType(nodeRef));

        // Execute the action
        ActionImpl action = new ActionImpl(null, ID, SpecialiseTypeActionExecuter.NAME, null);
        action.setParameterValue(SpecialiseTypeActionExecuter.PARAM_TYPE_NAME, ContentModel.TYPE_FOLDER);
        this.executer.execute(action, nodeRef);

        // Check that the node's type has not been changed since it would not be a specialisation
        assertEquals(ContentModel.TYPE_CONTENT, this.nodeService.getType(nodeRef));
        return action;
    }
}
