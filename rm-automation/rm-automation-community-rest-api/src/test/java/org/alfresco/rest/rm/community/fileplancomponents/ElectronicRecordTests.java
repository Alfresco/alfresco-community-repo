/*
 * #%L
 * Alfresco Records Management Module
 * %%
 * Copyright (C) 2005 - 2016 Alfresco Software Limited
 * %%
 * License rights for this program may be obtained from Alfresco Software, Ltd.
 * pursuant to a written agreement and any use of this program without such an
 * agreement is prohibited.
 * #L%
 */
package org.alfresco.rest.rm.community.fileplancomponents;

import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import static org.alfresco.rest.rm.community.model.fileplancomponents.FilePlanComponentAlias.FILE_PLAN_ALIAS;
import static org.alfresco.rest.rm.community.model.fileplancomponents.FilePlanComponentAlias.UNFILED_RECORDS_CONTAINER_ALIAS;
import static org.alfresco.rest.rm.community.model.fileplancomponents.FilePlanComponentType.NON_ELECTRONIC_RECORD_TYPE;
import static org.alfresco.rest.rm.community.model.fileplancomponents.FilePlanComponentType.RECORD_FOLDER_TYPE;
import static org.alfresco.rest.rm.community.model.fileplancomponents.FilePlanComponentType.CONTENT_TYPE;
import static org.alfresco.rest.rm.community.util.PojoUtility.toJson;
import static org.alfresco.utility.data.RandomData.getRandomAlphanumeric;
import static org.springframework.http.HttpStatus.CREATED;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

import java.util.Random;

import org.alfresco.rest.rm.community.base.BaseRestTest;
import org.alfresco.rest.rm.community.model.fileplancomponents.FilePlanComponent;
import org.alfresco.rest.rm.community.model.fileplancomponents.FilePlanComponentProperties;
import org.alfresco.rest.rm.community.requests.FilePlanComponentAPI;
import org.alfresco.rest.rm.community.requests.RMSiteAPI;
import org.alfresco.utility.data.DataUser;
import org.springframework.beans.factory.annotation.Autowired;

/**
 *
 * @author Kristijan Conkas
 * @since
 */
public class ElectronicRecordTests extends BaseRestTest
{
    @Autowired
    private FilePlanComponentAPI filePlanComponentAPI;

    @Autowired
    private DataUser dataUser;
    
    @Autowired
    private RMSiteAPI rmSiteAPI;
    
    /** image resource file to be used for records body */
    private static final String IMAGE_FILE = "money.JPG";
    
    /** Valid root containers where non-electronic records can be created */
    @DataProvider(name = "validContainers")
    public Object[][] rootContainers() throws Exception {
        return new Object[][] {
            // an arbitrary record folder
            { createFolderInFilePlan(dataUser.getAdminUser(), FILE_PLAN_ALIAS.toString()) },
            // unfiled records root
            { getFilePlanComponentAsUser(dataUser.getAdminUser(), UNFILED_RECORDS_CONTAINER_ALIAS.toString()) },
            // an arbitrary unfiled records folder
            { createUnfiledRecordsFolder(UNFILED_RECORDS_CONTAINER_ALIAS.toString(), "Unfiled Folder " + getRandomAlphanumeric()) }
        };
    }
    
    /**
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
        dataProvider = "validContainers",
        description = "Electronic records can be created in unfiled record folder or unfiled record root"
    )
    public void canCreateElectronicRecordsInValidContainers(FilePlanComponent container) throws Exception
    {
        filePlanComponentAPI.usingRestWrapper().authenticateUser(dataUser.getAdminUser());

        FilePlanComponent record = new FilePlanComponent("Record " + getRandomAlphanumeric(), CONTENT_TYPE.toString(), 
            new FilePlanComponentProperties());
        String newRecordId = filePlanComponentAPI.createElectronicRecord(record, IMAGE_FILE, container.getId()).getId();
        
        // verify the create request status code
        filePlanComponentAPI.usingRestWrapper().assertStatusCodeIs(CREATED);
        
        // get newly created electonic record and verify its properties
        FilePlanComponent electronicRecord = filePlanComponentAPI.getFilePlanComponent(newRecordId);
        // record will have record identifier inserted in its name but will for sure start with file name
        // and end with its extension
        assertTrue(electronicRecord.getName().startsWith(IMAGE_FILE.substring(0, IMAGE_FILE.indexOf("."))));
    }
}
