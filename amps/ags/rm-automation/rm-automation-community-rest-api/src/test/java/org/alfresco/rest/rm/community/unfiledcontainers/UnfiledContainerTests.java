/*
 * #%L
 * Alfresco Records Management Module
 * %%
 * Copyright (C) 2005 - 2022 Alfresco Software Limited
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
package org.alfresco.rest.rm.community.unfiledcontainers;

import static java.time.LocalDateTime.now;

import static org.alfresco.rest.rm.community.model.fileplancomponents.FilePlanComponentAlias.FILE_PLAN_ALIAS;
import static org.alfresco.rest.rm.community.model.fileplancomponents.FilePlanComponentAlias.UNFILED_RECORDS_CONTAINER_ALIAS;
import static org.alfresco.rest.rm.community.model.fileplancomponents.FilePlanComponentType.CONTENT_TYPE;
import static org.alfresco.rest.rm.community.model.fileplancomponents.FilePlanComponentType.FOLDER_TYPE;
import static org.alfresco.rest.rm.community.model.fileplancomponents.FilePlanComponentType.NON_ELECTRONIC_RECORD_TYPE;
import static org.alfresco.rest.rm.community.model.fileplancomponents.FilePlanComponentType.UNFILED_CONTAINER_TYPE;
import static org.alfresco.rest.rm.community.model.fileplancomponents.FilePlanComponentType.UNFILED_RECORD_FOLDER_TYPE;
import static org.alfresco.rest.rm.community.utils.FilePlanComponentsUtil.createUnfiledContainerChildModel;
import static org.alfresco.utility.data.RandomData.getRandomAlphanumeric;
import static org.alfresco.utility.data.RandomData.getRandomName;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.CONFLICT;
import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.http.HttpStatus.OK;
import static org.springframework.http.HttpStatus.UNPROCESSABLE_ENTITY;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNotEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.NoSuchElementException;

import org.alfresco.rest.rm.community.base.BaseRMRestTest;
import org.alfresco.rest.rm.community.base.DataProviderClass;
import org.alfresco.rest.rm.community.model.fileplan.FilePlan;
import org.alfresco.rest.rm.community.model.unfiledcontainer.UnfiledContainer;
import org.alfresco.rest.rm.community.model.unfiledcontainer.UnfiledContainerChild;
import org.alfresco.rest.rm.community.model.unfiledcontainer.UnfiledContainerChildCollection;
import org.alfresco.rest.rm.community.model.unfiledcontainer.UnfiledContainerChildProperties;
import org.alfresco.rest.rm.community.model.unfiledcontainer.UnfiledRecordFolder;
import org.alfresco.rest.rm.community.utils.FilePlanComponentsUtil;
import org.alfresco.utility.report.Bug;
import org.springframework.http.HttpStatus;
import org.testng.annotations.AfterClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

/**
 * Unfiled container related API tests
 *
 * @author Ana Bozianu
 * @since 2.6
 */
public class UnfiledContainerTests extends BaseRMRestTest
{
    /** Number of children (for children creation test) */
    private static final int NUMBER_OF_CHILDREN = 10;
    private final List<UnfiledContainerChild> unfiledChildren = new ArrayList<>();

    /**
     * Data Provider with:
     * with the object types  for creating a Unfiled Record Folder
     *
     * @return file plan component alias
     */
    @DataProvider (name = "unfiledFolderTypes")
    public static Object[][] unfiledFolderTypes()
    {
        return new String[][] {
                { UNFILED_RECORD_FOLDER_TYPE },
                { FOLDER_TYPE }
        };
    }


    /**
     * <pre>
     * Given the RM site exists
     * When I retrieve the unfiled record conteiner by placeholder
     * Then the details of the unfiled record container is returned
     * </pre>
     */
    @Test
    (
        description = "Get the unfiled records container"
    )
    public void getUnfiledRecordsContainer()
    {
        // Get the unfiled records container
        UnfiledContainer container = getRestAPIFactory().getUnfiledContainersAPI().getUnfiledContainer(UNFILED_RECORDS_CONTAINER_ALIAS);

        // Check the response code
        assertStatusCode(OK);

        // Check the response contains the right node type
        assertEquals(container.getNodeType(), UNFILED_CONTAINER_TYPE);
    }

    /**
     * <pre>
     * Given that an unfiled container exists
     * When I ask the API to update the details of the unfiled container
     * Then the details of the unfiled container are updated
     * </pre>
     */
    @Test
    (
        description = "Rename unfiled container"
    )
    public void renameUnfiledContainer()
    {
        String newContainerName = "RenamedUnfiledContainer (" + getRandomAlphanumeric() + ")";

        // Build the properties which will be updated
        UnfiledContainer unfiledContainerUpdate = UnfiledContainer.builder().name(newContainerName).build();

        // Update the unfiled records container
        UnfiledContainer renamedUnfiledContainer = getRestAPIFactory().getUnfiledContainersAPI().updateUnfiledContainer(unfiledContainerUpdate, UNFILED_RECORDS_CONTAINER_ALIAS);

        // Verify the status code
        assertStatusCode(OK);

        // Verify the returned unfiled records container
        assertEquals(renamedUnfiledContainer.getName(), newContainerName);

        // Get actual FILE_PLAN_ALIAS id
        FilePlan filePlan = getRestAPIFactory().getFilePlansAPI().getFilePlan(FILE_PLAN_ALIAS);

        // verify renamed component still has this parent
        assertEquals(renamedUnfiledContainer.getParentId(), filePlan.getId());
    }

    /**
     * <pre>
     * Given that an unfiled records container exists
     * When I ask the API to create a child unfiled record folder
     * Then it is created within the unfiled records container
     * </pre>
     */
    @Test
    (
        description = "Create unfiled record folder child in unfiled root container",
        dataProvider = "unfiledFolderTypes"
    )
    public void createUnfiledRecordFolderChild(String folderType)
    {
        String unfiledRecordFolderName = "UnfiledRecordFolder-" + getRandomAlphanumeric();
        UnfiledContainerChild unfiledRecordFolderChild = createUnfiledContainerChild(UNFILED_RECORDS_CONTAINER_ALIAS, unfiledRecordFolderName, folderType);
        unfiledChildren.add(unfiledRecordFolderChild);

        assertNotNull(unfiledRecordFolderChild.getId());

        // Verify the returned file plan component
        assertFalse(unfiledRecordFolderChild.getIsRecord());
        assertTrue(unfiledRecordFolderChild.getIsUnfiledRecordFolder()); // it is not a _normal_ record folder!

        assertEquals(unfiledRecordFolderChild.getName(), unfiledRecordFolderName);
        assertEquals(unfiledRecordFolderChild.getNodeType(), UNFILED_RECORD_FOLDER_TYPE);

        assertEquals(unfiledRecordFolderChild.getCreatedByUser().getId(), getAdminUser().getUsername());

        UnfiledRecordFolder unfiledRecordFolder = getRestAPIFactory().getUnfiledRecordFoldersAPI().getUnfiledRecordFolder(unfiledRecordFolderChild.getId());
        // Verify the returned file plan component properties
        UnfiledContainerChildProperties unfiledRecordFolderChildProperties = unfiledRecordFolder.getProperties();
        assertEquals(unfiledRecordFolderChildProperties.getTitle(), FilePlanComponentsUtil.TITLE_PREFIX + unfiledRecordFolderName);
        assertNotNull(unfiledRecordFolderChildProperties.getIdentifier());
        assertEquals(unfiledRecordFolder.getParentId(),
                getRestAPIFactory().getUnfiledContainersAPI().getUnfiledContainer(UNFILED_RECORDS_CONTAINER_ALIAS).getId());
    }

    @Test
    (   description = "Create duplicate unfiled folder child",
        dataProvider = "unfiledFolderTypes"
    )
    @Bug(id ="RM-5116, RM-5148")
    public void createDuplicateUnfiledFolderChild(String folderType)
    {
        String unfiledRecordFolderName = "UnfiledRecordFolder-" + getRandomAlphanumeric();
        UnfiledContainerChild unfiledRecordFolderChild = createUnfiledContainerChild(UNFILED_RECORDS_CONTAINER_ALIAS,
                    unfiledRecordFolderName, folderType);

        // Verify the status code
        assertStatusCode(CREATED);
        unfiledChildren.add(unfiledRecordFolderChild);
        assertEquals(unfiledRecordFolderChild.getName(), unfiledRecordFolderName);

		// create the same unfiled folder
        UnfiledContainerChild unfiledRecordFolderDuplicate = getRestAPIFactory().getUnfiledContainersAPI()
                .createUnfiledContainerChild(createUnfiledContainerChildModel(unfiledRecordFolderName, folderType),
                        UNFILED_RECORDS_CONTAINER_ALIAS);

        // Verify the status code
        assertStatusCode(CONFLICT);

        // create the same unfiled folder with the autoRename parameter on true
        unfiledRecordFolderDuplicate = getRestAPIFactory().getUnfiledContainersAPI()
                .createUnfiledContainerChild(createUnfiledContainerChildModel(unfiledRecordFolderName, folderType),
                        UNFILED_RECORDS_CONTAINER_ALIAS, "autoRename=true");

        //verify the response status code
		assertStatusCode(CREATED);
        unfiledChildren.add(unfiledRecordFolderDuplicate);
        assertNotEquals(unfiledRecordFolderDuplicate.getName(), unfiledRecordFolderName);
        assertTrue(unfiledRecordFolderDuplicate.getName().startsWith(unfiledRecordFolderName));
    }

    /**
     * <pre>
     * Given that an unfiled records container exists
     * When I ask the API to create a child unfiled record folder with relative path
     * Then it is not supported
     * </pre>
     */
    @Test
    (
        description = "Create unfiled record folder child in unfiled root container"
    )
    public void createUnfiledRecordFolderChildWithRelativePathNotSuported()
    {
        // relativePath specify the container structure to create relative to
        // the record folder to be created
        String relativePath = now().getYear() + "/" + now().getMonth() + "/" + now().getDayOfMonth();
        UnfiledContainerChild unfiledFolderModel = UnfiledContainerChild.builder()
                                                                        .name(getRandomName("UnfiledRecordFolder"))
                                                                        .nodeType(UNFILED_RECORD_FOLDER_TYPE)
                                                                        .relativePath(relativePath)
                                                                        .build();
        getRestAPIFactory().getUnfiledContainersAPI()
                           .createUnfiledContainerChild(unfiledFolderModel, UNFILED_RECORDS_CONTAINER_ALIAS);

        // Check the API response code
        assertStatusCode(BAD_REQUEST);
    }

    /**
     * <pre>
     * Given that an unfiled records container exists
     * When I ask the API to create a child record
     * Then it is created within the unfiled records container
     * </pre>
     */
    @Test
    (
        description = "Create non-electronic record child in unfiled root container"
    )
    public void createNonElectronicRecordChild()
    {
        String recordName = "NERecord-" + getRandomAlphanumeric();
        UnfiledContainerChild unfiledRecord = createUnfiledContainerChild(UNFILED_RECORDS_CONTAINER_ALIAS, recordName, NON_ELECTRONIC_RECORD_TYPE);
        unfiledChildren.add(unfiledRecord);

        assertNotNull(unfiledRecord.getId());
        assertTrue(unfiledRecord.getIsRecord());
        assertEquals(unfiledRecord.getNodeType(), NON_ELECTRONIC_RECORD_TYPE);
        // check it was created in the unfiled root container
        UnfiledContainer container = getRestAPIFactory().getUnfiledContainersAPI().getUnfiledContainer(UNFILED_RECORDS_CONTAINER_ALIAS);
        assertEquals(unfiledRecord.getParentId(), container.getId());
        // check the name contains the identifier
        String identifier = unfiledRecord.getProperties().getIdentifier();
        assertNotNull(identifier);
        assertEquals(unfiledRecord.getName(), recordName + " (" + identifier + ")");
    }

    /**
     * <pre>
     * Given that an unfiled records container exists
     * When I ask the API to create a child record
     * Then it is created within the unfiled records container
     * </pre>
     */
    @Test
    (
        description = "Create electronic record child in unfiled root container"
    )
    public void createElectronicRecordChild()
    {
        String recordName = "ERecord-" + getRandomAlphanumeric();
        UnfiledContainerChild unfiledRecord = createUnfiledContainerChild(UNFILED_RECORDS_CONTAINER_ALIAS, recordName, CONTENT_TYPE);
        unfiledChildren.add(unfiledRecord);

        assertNotNull(unfiledRecord.getId());
        assertTrue(unfiledRecord.getIsRecord());
        assertEquals(unfiledRecord.getNodeType(), CONTENT_TYPE);
        // check it was created in the unfiled root container
        UnfiledContainer container = getRestAPIFactory().getUnfiledContainersAPI().getUnfiledContainer(UNFILED_RECORDS_CONTAINER_ALIAS);
        assertEquals(unfiledRecord.getParentId(), container.getId());
        // check the name contains the identifier
        String identifier = unfiledRecord.getProperties().getIdentifier();
        assertNotNull(identifier);
        assertEquals(unfiledRecord.getName(), recordName + " (" + identifier + ")");
    }

    /**
     * <pre>
     * Given the RM site is created
     * And contains a number of records and unfiled record folders
     * When I ask the API to get me the children of the unfiled root container
     * Then I am returned the contained record and unfiled record folders
     * </pre>
     */
    @Test
    (
        description = "Get children of the root unfiled root container"
    )
    public void getUnfiledRootContainerChildren()
    {
        // Add unfiled root container children
        List<UnfiledContainerChild> createdChildren = new LinkedList<>();
        for (int i = 0; i < NUMBER_OF_CHILDREN; i++)
        {
            String childType;
            if (i % 3 == 0)
            {
                childType = CONTENT_TYPE;
            }
            else if (i % 3 == 1)
            {
                childType = NON_ELECTRONIC_RECORD_TYPE;
            }
            else
            {
                childType = UNFILED_RECORD_FOLDER_TYPE;
            }
            UnfiledContainerChild child = createUnfiledContainerChild(UNFILED_RECORDS_CONTAINER_ALIAS, getRandomAlphanumeric(), childType);
            assertNotNull(child.getId());
            createdChildren.add(child);
        }

        // Get children from API
        UnfiledContainerChildCollection listedChildren = getRestAPIFactory().getUnfiledContainersAPI().getUnfiledContainerChildren(UNFILED_RECORDS_CONTAINER_ALIAS,"include=properties");

        // Check status code
        assertStatusCode(OK);

        // Check listed children contains created list
        UnfiledContainer unfiledContainer = getRestAPIFactory().getUnfiledContainersAPI().getUnfiledContainer(UNFILED_RECORDS_CONTAINER_ALIAS);
        List<UnfiledContainerChild> verifiedChildren = new LinkedList<>();
        listedChildren.getEntries().forEach(c ->
        {
            UnfiledContainerChild containerChild = c.getEntry();
            String childId = containerChild.getId();

            assertNotNull(childId);
            logger.info("Checking child " + childId);

            try
            {
                // Get the element from the created children list
                UnfiledContainerChild createdComponent = createdChildren.stream()
                                                                        .filter(child -> child.getId().equals(childId))
                                                                        .findFirst().orElseThrow();

                // Created by
                assertEquals(containerChild.getCreatedByUser().getId(), getAdminUser().getUsername());

                // Is parent id set correctly?
                assertEquals(containerChild.getParentId(), unfiledContainer.getId());

                // Boolean properties related to node type
                if (containerChild.getNodeType().equals(UNFILED_RECORD_FOLDER_TYPE))
                {
                    assertFalse(containerChild.getIsRecord());
                }
                else
                {
                    assertTrue(containerChild.getIsRecord());
                    assertTrue(containerChild.getName().contains(containerChild.getProperties().getIdentifier()),
                            "Records don't have in name the identifier");
                }

                // Does returned object have the same contents as the created one?
                assertEquals(createdComponent.getName(), containerChild.getName());
                assertEquals(createdComponent.getNodeType(), containerChild.getNodeType());

                // check rm identifier
                assertNotNull(createdComponent.getProperties().getIdentifier());

                // add the element to the matched children list
                verifiedChildren.add(createdComponent);
            }
            catch (NoSuchElementException e)
            {
                // the element was not created in this test, continue
            }
        });

        // check all the created elements have been returned
        assertTrue(verifiedChildren.containsAll(createdChildren));
        assertTrue(createdChildren.containsAll(verifiedChildren));
        unfiledChildren.addAll(createdChildren);
    }

    /**
     * Negative test to check that invalid types cannot be created at unfiled container root level
     * Only unfiled record folders and records can be created into unfiled container
     */
    @Test
    (
        dataProvider = "invalidRootTypes",
        dataProviderClass = DataProviderClass.class,
        description = "Only unfiled records folders and records  can be created as children for unfiled container root"
    )
    public void createInvalidUnfiledChildren(String filePlanComponentType)
    {
        String unfiledRecordFolderName = "UnfiledRecordFolder-" + getRandomAlphanumeric();

        logger.info("creating " + filePlanComponentType);

        // Build unfiled records folder properties
        UnfiledContainerChild unfiledFolderModel = createUnfiledContainerChildModel(unfiledRecordFolderName, filePlanComponentType);

        getRestAPIFactory().getUnfiledContainersAPI().createUnfiledContainerChild(unfiledFolderModel, UNFILED_RECORDS_CONTAINER_ALIAS);
        // Verify the status code
        assertStatusCode(UNPROCESSABLE_ENTITY);
    }

    @Test(description = "Create a record with custom record identifier in unfiled container")
    public void createRecordWithCustomIdentifier()
    {
        String recordName = "customIdRecord-" + getRandomAlphanumeric();
        String customIdentifier = "customId";
        UnfiledContainerChildProperties propertiesModel = UnfiledContainerChildProperties.builder().identifier(customIdentifier).build();

        UnfiledContainerChild childModel = UnfiledContainerChild.builder()
                                                                .name(recordName)
                                                                .nodeType(CONTENT_TYPE)
                                                                .properties(propertiesModel)
                                                                .build();

        UnfiledContainerChild child = getRestAPIFactory().getUnfiledContainersAPI().createUnfiledContainerChild(childModel, UNFILED_RECORDS_CONTAINER_ALIAS);

        assertStatusCode(HttpStatus.CREATED);
        unfiledChildren.add(child);
        assertEquals(child.getProperties().getIdentifier(), customIdentifier);
        assertEquals(child.getName(), recordName + " (" + customIdentifier + ")");
    }

    @AfterClass (alwaysRun = true)
    public void tearDown()
    {
        unfiledChildren.forEach(unfiledChild ->
        {
            if (unfiledChild.getIsRecord())
            {
                getRestAPIFactory().getRecordsAPI().deleteRecord(unfiledChild.getId());
            }
            else
            {
                getRestAPIFactory().getUnfiledRecordFoldersAPI().deleteUnfiledRecordFolder(unfiledChild.getId());
            }
        });
    }
}
