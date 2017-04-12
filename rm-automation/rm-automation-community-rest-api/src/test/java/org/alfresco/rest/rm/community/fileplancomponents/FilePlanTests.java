/*
 * #%L
 * Alfresco Records Management Module
 * %%
 * Copyright (C) 2005 - 2017 Alfresco Software Limited
 * %%
 * This file is part of the Alfresco software.
 * -
 * If the software was purchased under a paid Alfresco license, the terms of
 * the paid license agreement will prevail.  Otherwise, the software is
 * provided under the following open source license terms:
 * -
 * Alfresco is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * -
 * Alfresco is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * -
 * You should have received a copy of the GNU Lesser General Public License
 * along with Alfresco. If not, see <http://www.gnu.org/licenses/>.
 * #L%
 */
package org.alfresco.rest.rm.community.fileplancomponents;

import static java.util.Arrays.asList;
import static org.alfresco.rest.rm.community.base.AllowableOperations.CREATE;
import static org.alfresco.rest.rm.community.base.AllowableOperations.DELETE;
import static org.alfresco.rest.rm.community.base.AllowableOperations.UPDATE;
import static org.alfresco.rest.rm.community.model.fileplancomponents.FilePlanComponentAlias.FILE_PLAN_ALIAS;
import static org.alfresco.rest.rm.community.model.fileplancomponents.FilePlanComponentAlias.TRANSFERS_ALIAS;
import static org.alfresco.rest.rm.community.model.fileplancomponents.FilePlanComponentFields.ALLOWABLE_OPERATIONS;
import static org.springframework.http.HttpStatus.FORBIDDEN;
import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.http.HttpStatus.OK;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import org.alfresco.rest.rm.community.base.BaseRMRestTest;
import org.alfresco.rest.rm.community.base.TestData;
import org.alfresco.rest.rm.community.model.fileplan.FilePlan;
import org.alfresco.rest.rm.community.model.transfercontainer.TransferContainer;
import org.alfresco.rest.rm.community.model.unfiledcontainer.UnfiledContainer;
import org.alfresco.rest.rm.community.requests.gscore.api.RMSiteAPI;
import org.alfresco.utility.model.UserModel;
import org.testng.annotations.Test;

/**
 * This class contains the tests for the File Plan CRUD API
 *
 * @author Rodica Sutu
 * @since 2.6
 */
public class FilePlanTests extends BaseRMRestTest
{
    /**
     * <pre>
     * Given that the RM site doesn't exist
     * When I use the API to get the File Plan/Holds/Unfiled Records Container/Transfers
     * Then I get the 404 response code
     * </pre>
     */
    @Test
    (
        description = "Check the GET response for the special contianers when the RM site doesn't exist",
        dataProviderClass = TestData.class,
        dataProvider = "getContainers"
    )
    public void getContainersWhenRMIsNotCreated(String containerAlias) throws Exception
    {
        RMSiteAPI rmSiteAPI = getRestAPIFactory().getRMSiteAPI();

        // Check RM Site Exist
        if (rmSiteAPI.existsRMSite())
        {
            // Delete RM Site
            rmSiteAPI.deleteRMSite();
        }

        if (FILE_PLAN_ALIAS.equalsIgnoreCase(containerAlias))
        {
            getRestAPIFactory().getFilePlansAPI().getFilePlan(containerAlias);
        }
        else if(TRANSFERS_ALIAS.equalsIgnoreCase(containerAlias))
        {
            getRestAPIFactory().getTransferContainerAPI().getTransferContainer(containerAlias);
        }
        else
        {
            getRestAPIFactory().getUnfiledContainersAPI().getUnfiledContainer(containerAlias);
        }

        // Check the response code is NOT_FOUND
        assertStatusCode(NOT_FOUND);
    }

    /**
     * <pre>
     * Given that a file plan exists
     * When I ask the API for the details of the file plan
     * Then I am given the details of the file plan
     * </pre>
     */
    @Test
    (
        description = "Check the GET response for the special containers when the RM site exit",
        dataProviderClass = TestData.class,
        dataProvider = "getContainersAndTypes"
    )
    public void getContainersWhenRMIsCreated(String containerAlias, String containerType) throws Exception
    {
        // Create RM Site if doesn't exist
        createRMSiteIfNotExists();

        // Get the file plan special container
        FilePlan filePlan = null;
        TransferContainer transferContainer = null;
        UnfiledContainer unfiledContainer = null;

        if (FILE_PLAN_ALIAS.equalsIgnoreCase(containerAlias))
        {
            filePlan = getRestAPIFactory().getFilePlansAPI().getFilePlan(containerAlias);
        }
        else if(TRANSFERS_ALIAS.equalsIgnoreCase(containerAlias))
        {
            transferContainer = getRestAPIFactory().getTransferContainerAPI().getTransferContainer(containerAlias);
        }
        else
        {
            unfiledContainer = getRestAPIFactory().getUnfiledContainersAPI().getUnfiledContainer(containerAlias);
        }

        // Check the response code
        assertStatusCode(OK);

        // Check the response contains the right node type
        if (FILE_PLAN_ALIAS.equalsIgnoreCase(containerAlias))
        {
            assertEquals(filePlan.getNodeType(), containerType);
        }
        else if(TRANSFERS_ALIAS.equalsIgnoreCase(containerAlias))
        {
            assertEquals(transferContainer.getNodeType(), containerType);
        }
        else
        {
            assertEquals(unfiledContainer.getNodeType(), containerType);
        }
    }

    /**
     * <pre>
     * Given that a file plan exists
     * When I ask the API for the details of the file plan to include the allowableOperations property
     * Then I am given the allowableOperations property with the update and create operations.
     * </pre>
     */
    @Test
    (
        description = "Check the allowableOperations list returned",
        dataProviderClass = TestData.class,
        dataProvider = "getContainers"
    )
    public void includeAllowableOperations(String containerAlias) throws Exception
    {
        // Create RM Site if doesn't exist
        createRMSiteIfNotExists();

        // Get the file plan special containers with the optional parameter allowableOperations
        FilePlan filePlan = null;
        TransferContainer transferContainer = null;
        UnfiledContainer unfiledContainer = null;

        if (FILE_PLAN_ALIAS.equalsIgnoreCase(containerAlias))
        {
            // Check the list of allowableOperations returned
            filePlan = getRestAPIFactory().getFilePlansAPI().getFilePlan(containerAlias, "include=" + ALLOWABLE_OPERATIONS);

            assertTrue(filePlan.getAllowableOperations().containsAll(asList(UPDATE, CREATE)),
                    "Wrong list of the allowable operations is return" + filePlan.getAllowableOperations().toString());

            // Check the list of allowableOperations doesn't contain DELETE operation
            assertFalse(filePlan.getAllowableOperations().contains(DELETE),
                    "The list of allowable operations contains delete option" + filePlan.getAllowableOperations().toString());
        }
        else if (TRANSFERS_ALIAS.equalsIgnoreCase(containerAlias))
        {
            // Check the list of allowableOperations returned
            transferContainer = getRestAPIFactory().getTransferContainerAPI().getTransferContainer(containerAlias, "include=" + ALLOWABLE_OPERATIONS);

            assertTrue(transferContainer.getAllowableOperations().containsAll(asList(UPDATE)),
                    "Wrong list of the allowable operations is return" + transferContainer.getAllowableOperations().toString());

            // Check the list of allowableOperations doesn't contain DELETE operation
            assertFalse(transferContainer.getAllowableOperations().contains(DELETE),
                    "The list of allowable operations contains delete option" + transferContainer.getAllowableOperations().toString());

            // Check the list of allowableOperations doesn't contain DELETE operation
            assertFalse(transferContainer.getAllowableOperations().contains(CREATE),
                    "The list of allowable operations contains delete option" + transferContainer.getAllowableOperations().toString());
        }
        else
        {
            unfiledContainer = getRestAPIFactory().getUnfiledContainersAPI().getUnfiledContainer(containerAlias, "include=" + ALLOWABLE_OPERATIONS);

            // Check the list of allowableOperations returned
            assertTrue(unfiledContainer.getAllowableOperations().containsAll(asList(UPDATE, CREATE)),
                        "Wrong list of the allowable operations is return" + unfiledContainer.getAllowableOperations().toString());

            // Check the list of allowableOperations doesn't contain DELETE operation
            assertFalse(unfiledContainer.getAllowableOperations().contains(DELETE),
                    "The list of allowable operations contains delete option" + unfiledContainer.getAllowableOperations().toString());
        }
    }

    /**
     * <pre>
     * Given that RM site exists
     * When a non-RM user asks the API for the details of the file plan
     * Then the status code 403 (Permission denied) is return
     * </pre>
     */
    @Test
    (
        description = "Check the response code when the RM site containers are get with non-RM users",
        dataProviderClass = TestData.class,
        dataProvider = "getContainers"
    )
    public void getContainersWithNonRMuser(String containerAlias) throws Exception
    {
        // Create RM Site if doesn't exist
        createRMSiteIfNotExists();

        // Create a random user
        UserModel nonRMuser = getDataUser().createRandomTestUser("testUser");

        // Get the special file plan components
        if (FILE_PLAN_ALIAS.equalsIgnoreCase(containerAlias))
        {
            getRestAPIFactory().getFilePlansAPI(nonRMuser).getFilePlan(containerAlias);
        }
        else if(TRANSFERS_ALIAS.equalsIgnoreCase(containerAlias))
        {
            getRestAPIFactory().getTransferContainerAPI(nonRMuser).getTransferContainer(containerAlias);
        }
        else
        {
            getRestAPIFactory().getUnfiledContainersAPI(nonRMuser).getUnfiledContainer(containerAlias);
        }

        // Check the response status code is FORBIDDEN
        assertStatusCode(FORBIDDEN);
    }
}
