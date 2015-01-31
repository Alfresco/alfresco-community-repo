/*
 * Copyright (C) 2005-2014 Alfresco Software Limited.
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
package org.alfresco.repo.webservice.action;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.security.authentication.AuthenticationComponent;
import org.alfresco.repo.webservice.types.NamedValue;
import org.alfresco.repo.webservice.types.ParentReference;
import org.alfresco.repo.webservice.types.Predicate;
import org.alfresco.repo.webservice.types.Reference;
import org.alfresco.repo.webservice.types.Store;
import org.alfresco.util.BaseSpringTest;

/**
 * Test for ActionWebService class.
 * 
 * @author alex.mukha
 * @since 4.2.4
 */
public class ActionWebServiceTest extends BaseSpringTest
{
    private AuthenticationComponent authenticationComponent;
    private ActionWebService actionWebService;

    private static String PARAM_PATH = "param-path";
    private static String PARAM_CONTENT = "param-content";

    @Override
    protected String[] getConfigLocations()
    {
        return new String[] { "classpath:org/alfresco/repo/webservice/action/action-test.xml" };
    }

    @SuppressWarnings("deprecation")
    @Override
    protected void onSetUpInTransaction() throws Exception
    {
        this.actionWebService = (ActionWebService) this.applicationContext.getBean("actionWebService");
        this.authenticationComponent = (AuthenticationComponent) this.applicationContext.getBean("authenticationComponent");

        this.authenticationComponent.setSystemUserAsCurrentUser();
    }

    @SuppressWarnings("deprecation")
    @Override
    protected void onTearDownInTransaction() throws Exception
    {
        authenticationComponent.clearCurrentSecurityContext();
        super.onTearDownInTransaction();
    }

    /**
     * Test for MNT-9342
     */
    public void testActionMultipleProperties() throws Exception
    {
        Store storeRef = new Store("workspace", "SpacesStore");
        ParentReference companyHomeParent = new ParentReference(storeRef, null, "/app:company_home", ContentModel.ASSOC_CONTAINS.toString(), null);
        Predicate predicate = new Predicate(new Reference[] { companyHomeParent }, null, null);
        String[] paramPath = { "path1", "path2", "path4" };
        String[] paramContent = { "content1", "content2", "content3" };

        // Create the action to create the document
        NamedValue[] parameters = new NamedValue[] { new NamedValue(PARAM_PATH, true, null, paramPath), new NamedValue(PARAM_CONTENT, true, null, paramContent) };

        Action newAction1 = new Action();
        newAction1.setActionName("create-doc-action-test");
        newAction1.setTitle("Create Document");
        newAction1.setDescription("This will create the document an content");
        newAction1.setParameters(parameters);

        ActionExecutionResult[] results = null;
        // An exception will be thrown here if the number of parameters passed to action executer will not match
        results = actionWebService.executeActions(predicate, new Action[] { newAction1 });
        assertNotNull(results);
    }
}
