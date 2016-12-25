/*
 * #%L
 * Alfresco Records Management Module
 * %%
 * Copyright (C) 2005 - 2016 Alfresco Software Limited
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

import static org.alfresco.rest.rm.community.model.fileplancomponents.FilePlanComponentAlias.FILE_PLAN_ALIAS;
import static org.alfresco.rest.rm.community.model.fileplancomponents.FilePlanComponentAlias.HOLDS_ALIAS;
import static org.alfresco.rest.rm.community.model.fileplancomponents.FilePlanComponentAlias.TRANSFERS_ALIAS;
import static org.alfresco.rest.rm.community.model.fileplancomponents.FilePlanComponentType.CONTENT_TYPE;
import static org.alfresco.rest.rm.community.model.fileplancomponents.FilePlanComponentType.RECORD_FOLDER_TYPE;
import static org.alfresco.rest.rm.community.util.PojoUtility.toJson;
import static org.alfresco.rest.rm.community.utils.FilePlanComponentsUtil.IMAGE_FILE;
import static org.alfresco.rest.rm.community.utils.FilePlanComponentsUtil.createElectronicRecordModel;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.http.HttpStatus.UNPROCESSABLE_ENTITY;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import org.alfresco.rest.rm.community.base.BaseRestTest;
import org.alfresco.rest.rm.community.model.fileplancomponents.FilePlanComponentModel;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

/**
 * Create/File electronic records tests
 * <br>
 * These tests only test the creation and filing of electronic records, update at
 * present isn't implemented in the API under test.
 * <p>
 * @author Kristijan Conkas
 * @since 2.6
 */
public class ElectronicRecordTests extends BaseRestTest
{
    /** Valid root containers where electronic records can be created */
    @DataProvider(name = "invalidParentContainers")
    public Object[][] invalidContainers() throws Exception
    {
        return new Object[][]
        {
            // record category
            { getFilePlanComponent(createCategoryFolderInFilePlan().getParentId()) },
            // file plan root
            { getFilePlanComponent(FILE_PLAN_ALIAS) },
            // transfers
            { getFilePlanComponent(TRANSFERS_ALIAS) },
            // holds
            { getFilePlanComponent(HOLDS_ALIAS) },
        };
    }

    /**
     * <pre>
     * Given a parent container that is NOT a record folder or an unfiled record folder
     * When I try to create an electronic record within the parent container
     * Then nothing happens
     * And an error is reported
     * </pre>
     * @param container
     * @throws Exception
     */
    @Test
    (
        dataProvider = "invalidParentContainers",
        description = "Electronic records can't be created in invalid parent containers"
    )
    public void cantCreateElectronicRecordsInInvalidContainers(FilePlanComponentModel container) throws Exception
    {
        // Build object the filePlan
        getFilePlanComponentsAPI().createElectronicRecord(createElectronicRecordModel(), IMAGE_FILE, container.getId());

        // verify the create request status code
        assertStatusCode(UNPROCESSABLE_ENTITY);
    }

    /**
     * <pre>
     * Given a parent container that is a record folder
     * And the record folder is closed
     * When I try to create an electronic record within the parent container
     * Then nothing happens
     * And an error is reported
     * </pre>
     * @throws Exception
     */
    @Test(description = "Electronic record can't be created in closed record folder")
    public void cantCreateElectronicRecordInClosedFolder() throws Exception
    {
        FilePlanComponentModel recordFolder = createCategoryFolderInFilePlan();

        // the folder should be open
        assertFalse(recordFolder.getProperties().getIsClosed());

        // close the folder
        closeFolder(recordFolder.getId());

        // try to create it, this should fail
        getFilePlanComponentsAPI().createElectronicRecord(createElectronicRecordModel(), IMAGE_FILE, recordFolder.getId());

        // verify the status code
        assertStatusCode(UNPROCESSABLE_ENTITY);
    }

    /**
     * <pre>
     * Given a parent container that is a record folder
     * And the record folder is open
     * When I try to create an electronic record within the parent container
     * And I do not provide all the required mandatory property values
     * Then nothing happens
     * And an error is reported
     * </pre>
     * and
     * <pre>
     * Given a parent container that is an unfiled record folder or the root unfiled record container
     * When I try to create an electronic record within the parent container
     * And I do not provide all the required mandatory property values
     * Then nothing happens
     * And an error is reported
     * </pre>
     * @param container
     * @throws Exception
     */
    @Test
    (
        dataProvider = "validRootContainers",
        description = "Electronic record can only be created if all mandatory properties are given"
    )
    public void canCreateElectronicRecordOnlyWithMandatoryProperties(FilePlanComponentModel container) throws Exception
    {
        logger.info("Root container:\n" + toJson(container));

        if (container.getNodeType().equals(RECORD_FOLDER_TYPE))
        {
            // only record folders can be open or closed
            assertFalse(container.getProperties().getIsClosed());
        }

        // component without name
        FilePlanComponentModel record = FilePlanComponentModel.builder()
                                                    .nodeType(CONTENT_TYPE)
                                                    .build();

        // try to create it
        getFilePlanComponentsAPI().createFilePlanComponent(record, container.getId());

        // verify the status code is BAD_REQUEST
        assertStatusCode(BAD_REQUEST);
    }

    /**
     * <pre>
     * Given a parent container that is a record folder
     * And the record folder is open
     * When I try to create an electronic record within the parent container
     * Then the electronic record is created
     * And the details of the new record are returned
     * </pre>
     * and
     * <pre>
     * Given a parent container that is an unfiled record folder or the root unfiled record container
     * When I try to create an electronic record within the parent container
     * Then the electronic record is created
     * And the details of the new record are returned
     * </pre>
     * @throws Exception
     */
    @Test
    (
        dataProvider = "validRootContainers",
        description = "Electronic records can be created in unfiled record folder or unfiled record root"
    )
    public void canCreateElectronicRecordsInValidContainers(FilePlanComponentModel container) throws Exception
    {
        FilePlanComponentModel record = createElectronicRecordModel();
        String newRecordId = getFilePlanComponentsAPI().createElectronicRecord(record, IMAGE_FILE, container.getId()).getId();

        // verify the create request status code
        assertStatusCode(CREATED);

        // get newly created electronic record and verify its properties
        FilePlanComponentModel electronicRecord = getFilePlanComponentsAPI().getFilePlanComponent(newRecordId);
        // created record will have record identifier inserted in its name but will be prefixed with
        // the name it was created as
        assertTrue(electronicRecord.getName().startsWith(record.getName()));
    }

    /**
     * This test verified that in the test client implementation if record name isn't specified it
     * defaults to filed file name.
     * @param container valid record container
     * @throws Exception if record creation failed
     */
    @Test
    (
        dataProvider = "validRootContainers",
        description = "Electronic records can be created in unfiled record folder or unfiled record root"
    )
    public void recordNameDerivedFromFileName(FilePlanComponentModel container) throws Exception
    {
        // record object without name set
        FilePlanComponentModel record = FilePlanComponentModel.builder()
                .nodeType(CONTENT_TYPE)
                .build();

        String newRecordId = getFilePlanComponentsAPI().createElectronicRecord(record, IMAGE_FILE, container.getId()).getId();

        // verify the create request status code
        assertStatusCode(CREATED);

        // get newly created electonic record and verify its properties
        FilePlanComponentModel electronicRecord = getFilePlanComponentsAPI().getFilePlanComponent(newRecordId);
        // record will have record identifier inserted in its name but will for sure start with file name
        // and end with its extension
        assertTrue(electronicRecord.getName().startsWith(IMAGE_FILE.substring(0, IMAGE_FILE.indexOf("."))));
    }
}
