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

package org.alfresco.repo.dictionary;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.transaction.RetryingTransactionHelper;
import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;
import org.alfresco.service.cmr.dictionary.CustomModelDefinition;
import org.alfresco.service.cmr.dictionary.CustomModelService;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.ApplicationContextHelper;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.context.ApplicationContext;

/**
 * Tests custom model expected behaviour after the repo server restarts.
 *
 * @author Jamal Kaabi-Mofrad
 */
public class CustomModelRepoRestartTest
{
    private CustomModelService customModelService;
    private RetryingTransactionHelper transactionHelper;
    private String modelName;

    @Before
    public void setUp() throws Exception
    {
        getCtxAndSetBeans();
        modelName = System.currentTimeMillis() + "testCustomModel";
    }

    @After
    public void tearDown() throws Exception
    {
        transactionHelper.doInTransaction(new RetryingTransactionCallback<Void>()
        {
            public Void execute() throws Exception
            {
                try
                {
                    // Deactivate the model
                    customModelService.deactivateCustomModel(modelName);
                }
                catch (Exception ex)
                {
                    // Ignore
                }
                return null;
            }
        });

        transactionHelper.doInTransaction(new RetryingTransactionCallback<Void>()
        {
            public Void execute() throws Exception
            {
                try
                {
                    // Delete the model
                    customModelService.deleteCustomModel(modelName);
                }
                catch (Exception ex)
                {
                    // we did our best, so ignore
                }
                return null;
            }
        });

        AuthenticationUtil.clearCurrentSecurityContext();
    }

    private void getCtxAndSetBeans()
    {
        ApplicationContext ctx = ApplicationContextHelper.getApplicationContext();
        this.customModelService = ctx.getBean("customModelService", CustomModelService.class);
        this.transactionHelper = ctx.getBean("retryingTransactionHelper", RetryingTransactionHelper.class);
        AuthenticationUtil.setAdminUserAsFullyAuthenticatedUser();
    }

    @Test
    public void testActivateModelAndRestartRepo() throws Exception
    {
        final long timeMillis = System.currentTimeMillis();
        final String uri = "http://www.alfresco.org/model/testcmmrestartnamespace" + timeMillis;
        final String prefix = "testcmmrestart" + timeMillis;

        final M2Model model = M2Model.createModel(prefix + QName.NAMESPACE_PREFIX + modelName);
        model.createNamespace(uri, prefix);

        // Create the model
        CustomModelDefinition modelDefinition = transactionHelper.doInTransaction(new RetryingTransactionCallback<CustomModelDefinition>()
        {
            public CustomModelDefinition execute() throws Exception
            {
                return customModelService.createCustomModel(model, false);
            }
        });

        assertNotNull(modelDefinition);
        assertEquals(modelName, modelDefinition.getName().getLocalName());
        assertFalse(modelDefinition.isActive());

        // Activate the model
        transactionHelper.doInTransaction(new RetryingTransactionCallback<Void>()
        {
            public Void execute() throws Exception
            {
                customModelService.activateCustomModel(modelName);
                return null;
            }
        });

        // Retrieve the model
        modelDefinition = getModel(modelName);
        assertNotNull(modelDefinition);
        assertEquals(modelName, modelDefinition.getName().getLocalName());
        assertTrue(modelDefinition.isActive());

        // Close the application context
        ApplicationContextHelper.closeApplicationContext();
        // Get the application context and set the beans
        getCtxAndSetBeans();

        // Retrieve the model after the server restart
        modelDefinition = getModel(modelName);
        assertNotNull(modelDefinition);
        assertEquals(modelName, modelDefinition.getName().getLocalName());
        assertTrue(modelDefinition.isActive());
    }

    private CustomModelDefinition getModel(final String modelName)
    {
        return transactionHelper.doInTransaction(new RetryingTransactionCallback<CustomModelDefinition>()
        {
            public CustomModelDefinition execute() throws Exception
            {
                return customModelService.getCustomModel(modelName);
            }
        });
    }
}
