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

import static org.alfresco.rest.rm.community.model.fileplancomponents.FilePlanComponentAlias.FILE_PLAN_ALIAS;
import static org.alfresco.rest.rm.community.model.fileplancomponents.FilePlanComponentAlias.HOLDS_ALIAS;
import static org.alfresco.rest.rm.community.model.fileplancomponents.FilePlanComponentAlias.TRANSFERS_ALIAS;
import static org.alfresco.rest.rm.community.model.fileplancomponents.FilePlanComponentType.NON_ELECTRONIC_RECORD_TYPE;
import static org.alfresco.rest.rm.community.model.fileplancomponents.FilePlanComponentType.RECORD_CATEGORY_TYPE;
import static org.alfresco.rest.rm.community.model.fileplancomponents.FilePlanComponentType.RECORD_FOLDER_TYPE;
import static org.alfresco.rest.rm.community.util.PojoUtility.toJson;
import static org.alfresco.rest.rm.community.utils.FilePlanComponentsUtil.createNonElectronicRecordModel;
import static org.alfresco.utility.constants.UserRole.SiteManager;
import static org.alfresco.utility.data.RandomData.getRandomAlphanumeric;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.http.HttpStatus.FORBIDDEN;
import static org.springframework.http.HttpStatus.UNPROCESSABLE_ENTITY;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;

import java.util.Random;

import org.alfresco.rest.rm.community.base.BaseRMRestTest;
import org.alfresco.rest.rm.community.model.fileplancomponents.FilePlanComponent;
import org.alfresco.rest.rm.community.model.fileplancomponents.FilePlanComponentProperties;
import org.alfresco.rest.rm.community.requests.igCoreAPI.FilePlanComponentAPI;
import org.alfresco.utility.constants.UserRole;
import org.alfresco.utility.model.SiteModel;
import org.alfresco.utility.model.UserModel;
import org.testng.annotations.Test;

/**
 * Create/File Non-Electronic Record into Unfiled Record Container/Record Folder ReST API tests
 *
 * @author Kristijan Conkas
 * @since 2.6
 */
public class NonElectronicRecordTests extends BaseRMRestTest
{
    /**
     * <pre>
     * Given a parent container that is NOT a record folder or an unfiled record folder
     * When I try to create a non-electronic record within the parent container
     * Then nothing happens
     * And an error is reported
     * </pre>
     * @throws Exception if prerequisites can't be created
     */
    @Test(description = "Non-electronic record can't be created as a child of invalid parent Id")
    public void cantCreateForInvalidParentIds() throws Exception
    {
        // create record category, non-electronic records can't be its children
        FilePlanComponent recordCategoryModel = FilePlanComponent.builder()
                                                         .name("Category " + getRandomAlphanumeric())
                                                         .nodeType(RECORD_CATEGORY_TYPE)
                                                         .build();

        FilePlanComponentAPI filePlanComponentsAPI = getRestAPIFactory().getFilePlanComponentsAPI();
        FilePlanComponent recordCategory = filePlanComponentsAPI.createFilePlanComponent(recordCategoryModel, FILE_PLAN_ALIAS);

        // iterate through all invalid parent containers and try to create/file an electronic record
        asList(FILE_PLAN_ALIAS, TRANSFERS_ALIAS, HOLDS_ALIAS, recordCategory.getId())
            .stream()
            .forEach(id ->
            {
                try
                {
                    filePlanComponentsAPI.createFilePlanComponent(createNonElectronicRecordModel(), id);
                }
                catch (Exception error)
                {
                }

                // Verify the status code
                assertStatusCode(UNPROCESSABLE_ENTITY);
            });
    }

    /**
     * <pre>
     * Given a parent container that is a record folder
     * And the record folder is open
     * When I try to create a non-electronic record within the parent container
     * Then the non-electronic record is created
     * And the details of the new record are returned
     * <pre>
     * and
     * <pre>
     * Given a parent container that is an unfiled record folder or the root unfiled record container
     * When I try to create a non-electronic record within the parent container
     * Then the non-electronic record is created
     * And the details of the new record are returned
     * </pre>
     * @throws Exception if record can't be created
     */
    @Test
    (
        dataProvider = "validRootContainers",
        description = "Non-electronic records can be created in valid containers"
    )
    public void canCreateInValidContainers(FilePlanComponent container) throws Exception
    {
        logger.info("Root container:\n" + toJson(container));

        if (container.getNodeType().equals(RECORD_FOLDER_TYPE))
        {
            // only record folders can be open or closed
            assertFalse(container.getProperties().getIsClosed());
        }

        // use these properties for non-electronic record to be created
        String title = "Title " + getRandomAlphanumeric();
        String description = "Description " + getRandomAlphanumeric();
        String box = "Box "+ getRandomAlphanumeric();
        String file = "File " + getRandomAlphanumeric();
        String shelf = "Shelf " + getRandomAlphanumeric();
        String location = "Location " + getRandomAlphanumeric();

        Random random = new Random();
        Integer copies = random.nextInt(Integer.MAX_VALUE);
        Integer size = random.nextInt(Integer.MAX_VALUE);

        // set values of all available properties for the non electronic records
        FilePlanComponent filePlanComponent = FilePlanComponent.builder()
                                                           .name("Record " + getRandomAlphanumeric())
                                                           .nodeType(NON_ELECTRONIC_RECORD_TYPE)
                                                           .properties(FilePlanComponentProperties.builder()
                                                                                                  .title(title)
                                                                                                  .description(description)
                                                                                                  .box(box)
                                                                                                  .file(file)
                                                                                                  .shelf(shelf)
                                                                                                  .location(location)
                                                                                                  .numberOfCopies(copies)
                                                                                                  .physicalSize(size)
                                                                                                  .build())
                                                           .build();

        // create non-electronic record
        FilePlanComponentAPI filePlanComponentsAPI = getRestAPIFactory().getFilePlanComponentsAPI();
        String nonElectronicId = filePlanComponentsAPI.createFilePlanComponent(
            filePlanComponent,
            container.getId()).getId();

        // verify the create request status code
        assertStatusCode(CREATED);

        // get newly created non-electonic record and verify its properties
        FilePlanComponent nonElectronicRecord = filePlanComponentsAPI.getFilePlanComponent(nonElectronicId);

        assertEquals(title, nonElectronicRecord.getProperties().getTitle());
        assertEquals(description, nonElectronicRecord.getProperties().getDescription());
        assertEquals(box, nonElectronicRecord.getProperties().getBox());
        assertEquals(file, nonElectronicRecord.getProperties().getFile());
        assertEquals(shelf, nonElectronicRecord.getProperties().getShelf());
        assertEquals(location, nonElectronicRecord.getProperties().getLocation());
        assertEquals(copies, nonElectronicRecord.getProperties().getNumberOfCopies());
        assertEquals(size, nonElectronicRecord.getProperties().getPhysicalSize());
    }

    /**
     * <pre>
     * Given a parent container that is a record folder
     * And the record folder is closed
     * When I try to create a non-electronic record within the parent container
     * Then nothing happens
     * And an error is reported
     * </pre>
     * @throws Exception if prerequisites can't be created
     */
    @Test(description = "Non-electronic record can't be created in closed record folder")
    public void cantCreateInClosedFolder() throws Exception
    {
        FilePlanComponent recordFolder = createCategoryFolderInFilePlan();

        // the folder should be open
        assertFalse(recordFolder.getProperties().getIsClosed());

        // close the folder
        closeFolder(recordFolder.getId());

        // try to create it, this should fail and throw an exception
        getRestAPIFactory().getFilePlanComponentsAPI().createFilePlanComponent(createNonElectronicRecordModel(), recordFolder.getId());

        // verify the status code
        assertStatusCode(UNPROCESSABLE_ENTITY);
    }

    /**
     * <pre>
     * Given a parent container that is a record folder
     * And the record folder is open
     * When I try to create a non-electronic record within the parent container
     * And I do not provide all the required mandatory property values
     * Then nothing happens
     * And an error is reported
     * </pre>
     * and
     * <pre>
     * Given a parent container that is an unfiled record folder or the root unfiled record container
     * When I try to create a non-electronic record within the parent container
     * And I do not provide all the required mandatory property values
     * Then nothing happens
     * And an error is reported
     * </pre>
     * @throws Exception if prerequisites can't be created
     */
    @Test
    (
        dataProvider = "validRootContainers",
        description = "Non-electronic record can only be created if all mandatory properties are given"
    )
    public void allMandatoryPropertiesRequired(FilePlanComponent container) throws Exception
    {
        logger.info("Root container:\n" + toJson(container));
        if (container.getNodeType().equals(RECORD_FOLDER_TYPE))
        {
            // only record folders can be open or closed
            assertFalse(container.getProperties().getIsClosed());
        }

        // component without name and title
        FilePlanComponent noNameOrTitle = getDummyNonElectronicRecord();

        // component with title only
        FilePlanComponent titleOnly = getDummyNonElectronicRecord();
        FilePlanComponentProperties properties = FilePlanComponentProperties.builder()
                                                                            .title("Title " + getRandomAlphanumeric())
                                                                            .build();
        titleOnly.setProperties(properties);

        // try to create invalid components
        asList(noNameOrTitle, titleOnly).stream().forEach(c ->
        {
            try
            {
                logger.info("Creating non-electronic record with body:\n" + toJson(c));
            }
            catch (Exception error)
            {
            }

            // this should fail and throw an exception
            try
            {
                getRestAPIFactory().getFilePlanComponentsAPI().createFilePlanComponent(c, container.getId());
            }
            catch (Exception e)
            {
            }

            // verify the status code is BAD_REQUEST
            assertStatusCode(BAD_REQUEST);
        });
    }

    /**
     * <pre>
     * Given that I am a user without RM privileges
     * When I try to create a non-electronic record
     * Then nothing happens
     * And an error is reported
     * </pre>
     * @throws Exception
     */
    @Test
    (
        dataProvider = "validRootContainers",
        description = "Non-electronic record can't be created if user doesn't have RM privileges"
    )
    public void cantCreateIfNoRmPrivileges(FilePlanComponent container) throws Exception
    {
        UserModel user = createUserWithRole("zzzuser", SiteManager);

        // try to create a fileplan component
        FilePlanComponent record = FilePlanComponent.builder()
                                                  .properties(FilePlanComponentProperties.builder()
                                                                                         .description("Description")
                                                                                         .title("Title")
                                                                                         .build())
                                                  .name("Record Name")
                                                  .nodeType(NON_ELECTRONIC_RECORD_TYPE)
                                                  .build();


        // this should fail and throw an exception
        try
        {
            getRestAPIFactory().getFilePlanComponentsAPI(user).createFilePlanComponent(record, container.getId());
        }
        catch (Exception e)
        {
        }

        // user who isn't an RM site member can't access the container path
        assertStatusCode(FORBIDDEN);
    }

    /**
     * Helper function to return an empty FilePlanComponent for non-electronic record
     * @return
     */
    private FilePlanComponent getDummyNonElectronicRecord()
    {
        FilePlanComponent component = FilePlanComponent.builder()
                                            .nodeType(NON_ELECTRONIC_RECORD_TYPE)
                                            .build();
        return component;
    }

    /**
     * Create user with given role and add it to RM site
     * <br>
     * Checks whether the user exists in RM site and creates it if required, with password identical
     * to username. Note the role is a Core API role, not an RM role.
     * <br>
     * For already existing users, no site membership or role verification is performed.
     * <p>
     * @param userName username to add
     * @param userRole user's role
     * @throws Exception
     */
    private UserModel createUserWithRole(String userName, UserRole userRole) throws Exception
    {
        String siteId = getRestAPIFactory().getRMSiteAPI().getSite().getId();

        // check if user exists
        UserModel user = new UserModel();
        user.setUsername(userName);
        user.setPassword(userName);

        if (!getDataUser().isUserInRepo(userName))
        {
            // user doesn't exist, create it
            user = getDataUser().createUser(userName, userName);
            user.setUserRole(userRole);

            getDataUser().addUserToSite(user, new SiteModel(siteId), userRole);
        }

        return user;
    }
}
