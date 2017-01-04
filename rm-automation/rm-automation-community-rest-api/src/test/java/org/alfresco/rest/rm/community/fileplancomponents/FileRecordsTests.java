package org.alfresco.rest.rm.community.fileplancomponents;

import static org.alfresco.rest.rm.community.base.TestData.ELECTRONIC_RECORD_NAME;
import static org.alfresco.rest.rm.community.base.TestData.NONELECTRONIC_RECORD_NAME;
import static org.alfresco.rest.rm.community.model.fileplancomponents.FilePlanComponentAlias.FILE_PLAN_ALIAS;
import static org.alfresco.rest.rm.community.model.fileplancomponents.FilePlanComponentAlias.HOLDS_ALIAS;
import static org.alfresco.rest.rm.community.model.fileplancomponents.FilePlanComponentAlias.TRANSFERS_ALIAS;
import static org.alfresco.rest.rm.community.model.fileplancomponents.FilePlanComponentAlias.UNFILED_RECORDS_CONTAINER_ALIAS;
import static org.alfresco.rest.rm.community.model.fileplancomponents.FilePlanComponentType.CONTENT_TYPE;
import static org.alfresco.rest.rm.community.model.fileplancomponents.FilePlanComponentType.NON_ELECTRONIC_RECORD_TYPE;
import static org.alfresco.rest.rm.community.utils.FilePlanComponentsUtil.createTempFile;
import static org.alfresco.utility.data.RandomData.getRandomAlphanumeric;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.http.HttpStatus.FORBIDDEN;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.AssertJUnit.assertTrue;

import org.alfresco.rest.rm.community.base.BaseRMRestTest;
import org.alfresco.rest.rm.community.model.fileplancomponents.FilePlanComponent;
import org.alfresco.rest.rm.community.model.fileplancomponents.FilePlanComponentContent;
import org.alfresco.rest.rm.community.model.fileplancomponents.FilePlanComponentProperties;
import org.alfresco.rest.rm.community.model.fileplancomponents.RecordBodyFile;
import org.alfresco.utility.report.Bug;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

/**
 * This class contains the tests for
 * File Unfiled Record Action REST API
 *
 * @author Rodica Sutu
 * @since 2.6
 */
public class FileRecordsTests extends BaseRMRestTest
{
    private FilePlanComponent electronicRecord = FilePlanComponent.builder()
                                                                  .name(ELECTRONIC_RECORD_NAME)
                                                                  .nodeType(CONTENT_TYPE.toString())
                                                                  .content(FilePlanComponentContent.builder().mimeType("text/plain").build())
                                                                  .build();

    private FilePlanComponent nonelectronicRecord = FilePlanComponent.builder()
                                                                     .properties(FilePlanComponentProperties.builder()
                                                                                                            .description(NONELECTRONIC_RECORD_NAME)
                                                                                                            .title("Title")
                                                                                                            .build())
                                                                     .name(NONELECTRONIC_RECORD_NAME)
                                                                     .nodeType(NON_ELECTRONIC_RECORD_TYPE.toString())
                                                                     .build();
    /**
     * Given an unfiled record in the root unfiled record container or a unfiled record folder
     * And an open record folder
     * When I file the unfiled record into the record folder
     * Then the record is filed
     */
    /**
     * Unfiled containers from where record can be filed
     */
    @DataProvider (name = "unfiledContainer")
    public Object[][] getUnfiledContainer() throws Exception
    {
        return new Object[][] {
            //unfiled container
            { getFilePlanComponent(UNFILED_RECORDS_CONTAINER_ALIAS).getId() },
            // an arbitrary unfiled records folder
            { createUnfiledRecordsFolder(UNFILED_RECORDS_CONTAINER_ALIAS.toString(), "Unfiled Folder " + getRandomAlphanumeric()).getId() }
        };
    }
    @Test
    (
        dataProvider = "unfiledContainer",
        description = "File record from unfiled containers "
    )
    public void fileRecordIntoExistingFolder(String unfiledContainerId) throws Exception
    {
        //create a record folder
        String folderId = createCategoryFolderInFilePlan().getId();

        //create records
        FilePlanComponent recordElectronic = getRestAPIFactory().getFilePlanComponentsAPI().createElectronicRecord(electronicRecord, createTempFile(ELECTRONIC_RECORD_NAME, ELECTRONIC_RECORD_NAME), unfiledContainerId);
        FilePlanComponent recordNonElectId = getRestAPIFactory().getFilePlanComponentsAPI().createFilePlanComponent(nonelectronicRecord, unfiledContainerId);

        //file the record into the folder created
        RecordBodyFile recordBodyFile = RecordBodyFile.builder().targetParentId(folderId).build();
        FilePlanComponent recordFiled =getRestAPIFactory().getRecordsAPI().fileRecord(recordBodyFile, recordElectronic.getId());
        //check the response status
        assertStatusCode(CREATED);
        //check the parent id for the record returned
        assertEquals(recordFiled.getParentId(),folderId);
        //check the record is filed into the record folder
        assertTrue(getRestAPIFactory().getFilePlanComponentsAPI().listChildComponents(folderId)
                                         .getEntries().stream()
                                         .anyMatch(c->c.getFilePlanComponentModel().getId()
                                                       .equals(recordElectronic.getId())
                                                  )
                  );
        //check the record doesn't exist into unfiled record container
        assertFalse(getRestAPIFactory().getFilePlanComponentsAPI().listChildComponents(unfiledContainerId)
                                      .getEntries().stream()
                                      .anyMatch(c -> c.getFilePlanComponentModel().getId()
                                                      .equals(recordElectronic.getId())
                                               )
                                      );
        //file the non-electronic record into the folder created
        FilePlanComponent nonElectRecordFiled = getRestAPIFactory().getRecordsAPI().fileRecord(recordBodyFile, recordNonElectId.getId());
        //check the response status code
        assertStatusCode(CREATED);
        //check the parent id for the record returned
        assertEquals(nonElectRecordFiled.getParentId(), folderId);
        //check the record is added into the record folder
        assertTrue(getRestAPIFactory().getFilePlanComponentsAPI().listChildComponents(folderId)
                                         .getEntries().stream()
                                         .anyMatch(c -> c.getFilePlanComponentModel().getId()
                                                         .equals(recordNonElectId.getId())
                                                  )
                  );
        //check the record doesn't exist into unfiled record container
        assertFalse(getRestAPIFactory().getFilePlanComponentsAPI().listChildComponents(unfiledContainerId)
                                      .getEntries().stream()
                                      .anyMatch(c -> c.getFilePlanComponentModel().getId()
                                                      .equals(recordNonElectId.getId())
                                               )
                  );
    }

    /**
     * Given an unfiled record in the root unfiled record container or a unfiled record folder
     * And a closed record folder
     * When I file the unfiled record into the record folder
     * Then I get an unsupported operation exception
     *
     */
    @Test
    (
        dataProvider = "unfiledContainer",
        description = "File record from unfiled containers into a closed folder "
    )
    public void fileRecordIntoCloseFolder(String unfiledContainerId) throws Exception
    {
        //create a record folder
        String folderId = createCategoryFolderInFilePlan().getId();
        closeFolder(folderId);
        //create records
        FilePlanComponent recordElectronic = getRestAPIFactory().getFilePlanComponentsAPI().createElectronicRecord(electronicRecord, createTempFile(ELECTRONIC_RECORD_NAME, ELECTRONIC_RECORD_NAME), unfiledContainerId);
        FilePlanComponent recordNonElectId = getRestAPIFactory().getFilePlanComponentsAPI().createFilePlanComponent(nonelectronicRecord, unfiledContainerId);

        //file the record into the folder created
        RecordBodyFile recordBodyFile = RecordBodyFile.builder().targetParentId(folderId).build();
        getRestAPIFactory().getRecordsAPI().fileRecord(recordBodyFile, recordElectronic.getId());
        //check the response status
        assertStatusCode(FORBIDDEN);

        //check the record is filed into the record folder
        assertFalse(getRestAPIFactory().getFilePlanComponentsAPI().listChildComponents(folderId)
                                      .getEntries().stream()
                                      .anyMatch(c -> c.getFilePlanComponentModel().getId()
                                                      .equals(recordElectronic.getId())
                                               )
                  );
        //check the record doesn't exist into unfiled record container
        assertTrue(getRestAPIFactory().getFilePlanComponentsAPI().listChildComponents(unfiledContainerId)
                                       .getEntries().stream()
                                       .anyMatch(c -> c.getFilePlanComponentModel().getId()
                                                       .equals(recordElectronic.getId())
                                                )
                   );
        //file the non-electronic record into the folder created
        FilePlanComponent nonElectRecordFiled = getRestAPIFactory().getRecordsAPI().fileRecord(recordBodyFile, recordNonElectId.getId());
        //check the response status code
        assertStatusCode(FORBIDDEN);
        //check the record is added into the record folder
        assertFalse(getRestAPIFactory().getFilePlanComponentsAPI().listChildComponents(folderId)
                                      .getEntries().stream()
                                      .anyMatch(c -> c.getFilePlanComponentModel().getId()
                                                      .equals(recordNonElectId.getId())
                                               )
                  );
        //check the record doesn't exist into unfiled record container
        assertTrue(getRestAPIFactory().getFilePlanComponentsAPI().listChildComponents(unfiledContainerId)
                                       .getEntries().stream()
                                       .anyMatch(c -> c.getFilePlanComponentModel().getId()
                                                       .equals(recordNonElectId.getId())
                                                )
                   );
    }

    /**
     * Given a filed record in a record folder
     * And an open record folder
     * When I file the filed record into the record folder
     * Then the record is filed in both locations
     */
    @Test
    @Bug(id="RM-4578")
    public void linkRecordInto() throws Exception
    {
        //create a record folder
        String parentFolderId = createCategoryFolderInFilePlan().getId();

        //create records
        FilePlanComponent recordElectronic = getRestAPIFactory().getFilePlanComponentsAPI().createElectronicRecord(electronicRecord, createTempFile(ELECTRONIC_RECORD_NAME, ELECTRONIC_RECORD_NAME), UNFILED_RECORDS_CONTAINER_ALIAS);
        FilePlanComponent recordNonElect = getRestAPIFactory().getFilePlanComponentsAPI().createFilePlanComponent(nonelectronicRecord, UNFILED_RECORDS_CONTAINER_ALIAS);

        //file the record into the folder created
        RecordBodyFile recordBodyFile = RecordBodyFile.builder().targetParentId(parentFolderId).build();
        FilePlanComponent recordFiled = getRestAPIFactory().getRecordsAPI().fileRecord(recordBodyFile, recordElectronic.getId());
        FilePlanComponent nonElectronicFiled = getRestAPIFactory().getRecordsAPI().fileRecord(recordBodyFile, recordNonElect.getId());
        //check the response status
        assertStatusCode(CREATED);

        //create the second folder
        String folderToLink = createCategoryFolderInFilePlan().getId();
        recordBodyFile = RecordBodyFile.builder().targetParentId(folderToLink).build();

        //check the response status
        assertStatusCode(CREATED);
        //link the electronic record
        FilePlanComponent recordLink = getRestAPIFactory().getRecordsAPI().fileRecord(recordBodyFile, recordElectronic.getId());
        assertTrue(recordLink.getParentId().equals(parentFolderId));
        //check the response status code
        assertStatusCode(CREATED);
        //link the nonelectronic record
        FilePlanComponent nonElectronicLink = getRestAPIFactory().getRecordsAPI().fileRecord(recordBodyFile, nonElectronicFiled.getId());
        assertStatusCode(CREATED);
        assertTrue(nonElectronicLink.getParentId().equals(parentFolderId));

        //check the record is added into the record folder
        assertTrue(getRestAPIFactory().getFilePlanComponentsAPI().listChildComponents(parentFolderId)
                                       .getEntries().stream()
                                       .anyMatch(c -> c.getFilePlanComponentModel().getId()
                                                       .equals(recordFiled.getId())&& c.getFilePlanComponentModel().getParentId().equals(parentFolderId)
                                                )
                   );
        //check the record doesn't exist into unfiled record container
        //TODO add a check after the issue will be fixed RM-4578
        assertTrue(getRestAPIFactory().getFilePlanComponentsAPI().listChildComponents(folderToLink)
                                      .getEntries().stream()
                                      .anyMatch(c -> c.getFilePlanComponentModel().getId()
                                                      .equals(recordFiled.getId()) //&& c.getFilePlanComponentModel().getParentId().equals(parentFolderId)
                                               )
                  );

        //check the record is added into the record folder
        assertTrue(getRestAPIFactory().getFilePlanComponentsAPI().listChildComponents(parentFolderId)
                                      .getEntries().stream()
                                      .anyMatch(c -> c.getFilePlanComponentModel().getId()
                                                      .equals(nonElectronicFiled.getId()) && c.getFilePlanComponentModel().getParentId().equals(parentFolderId)
                                               )
                  );
        //check the record doesn't exist into unfiled record container
        //TODO add a check after the issue will be fixed RM-4578
        assertTrue(getRestAPIFactory().getFilePlanComponentsAPI().listChildComponents(folderToLink)
                                      .getEntries().stream()
                                      .anyMatch(c -> c.getFilePlanComponentModel().getId()
                                                      .equals(nonElectronicFiled.getId()) //&& c.getFilePlanComponentModel().getParentId().equals(parentFolderId)
                                               )
                  );
    }

    /**
     * Given an unfiled or filed record
     * And a container that is NOT a record folder
     * When I file the unfiled or filed record to the container
     * Then I get an unsupported operation exception
     */
    /**
     * Invalid  containers where electronic and non-electronic records can be filed
     */
    @DataProvider (name = "invalidContainersForFile")
    public Object[][] getFolderContainers() throws Exception
    {
        return new Object[][] {
            { getFilePlanComponent(FILE_PLAN_ALIAS).getId()},
            { getFilePlanComponent(UNFILED_RECORDS_CONTAINER_ALIAS).getId()},
            { getFilePlanComponent(HOLDS_ALIAS).getId() },
            { getFilePlanComponent(TRANSFERS_ALIAS).getId() },
            // an arbitrary record category
            { createCategory(getAdminUser(), FILE_PLAN_ALIAS, "Category " + getRandomAlphanumeric()).getId()},
            // an arbitrary unfiled records folder
            { createUnfiledRecordsFolder(UNFILED_RECORDS_CONTAINER_ALIAS.toString(), "Unfiled Folder " + getRandomAlphanumeric()).getId() }
        };
    }
    @Test
    (
        dataProvider = "invalidContainersForFile",
        description = "File the unfiled record to the container that is not a record foldr"
    )
    public void invalidContainerToFile(String containerId) throws Exception
    {

        //create records
        FilePlanComponent recordElectronic = getRestAPIFactory().getFilePlanComponentsAPI().createElectronicRecord(electronicRecord, createTempFile(ELECTRONIC_RECORD_NAME, ELECTRONIC_RECORD_NAME), UNFILED_RECORDS_CONTAINER_ALIAS);
        FilePlanComponent recordNonElect = getRestAPIFactory().getFilePlanComponentsAPI().createFilePlanComponent(nonelectronicRecord, UNFILED_RECORDS_CONTAINER_ALIAS);

        //file the record into the folder created
        RecordBodyFile recordBodyFile = RecordBodyFile.builder().targetParentId(containerId).build();
        getRestAPIFactory().getRecordsAPI().fileRecord(recordBodyFile, recordElectronic.getId());
        assertStatusCode(BAD_REQUEST);

        getRestAPIFactory().getRecordsAPI().fileRecord(recordBodyFile, recordNonElect.getId());
        //check the response status
        assertStatusCode(BAD_REQUEST);
    }

    /**
     * Given an unfiled record in the root unfiled record container or a unfiled record folder
     * When I file the unfiled record into the record folder using the relativePath
     * Then the filePlan structure from relativePath is created and the record is filed into the specified path
     */
    @Test
        (
            dataProvider = "unfiledContainer",
            description = "File record from unfiled containers "
        )
    public void fileRecordIntoRelativePath(String unfiledContainerId) throws Exception
    {
        //create a record folder
        String RELATIVE_PATH = "CATEGORY" + getRandomAlphanumeric() + "/FOLDER";

        //create records
        FilePlanComponent recordElectronic = getRestAPIFactory().getFilePlanComponentsAPI().createElectronicRecord(electronicRecord, createTempFile(ELECTRONIC_RECORD_NAME, ELECTRONIC_RECORD_NAME), unfiledContainerId);
        FilePlanComponent recordNonElectId = getRestAPIFactory().getFilePlanComponentsAPI().createFilePlanComponent(nonelectronicRecord, unfiledContainerId);

        //file the record into the folder created
        RecordBodyFile recordBodyFile = RecordBodyFile.builder().relativePath(RELATIVE_PATH).build();
        FilePlanComponent recordFiled = getRestAPIFactory().getRecordsAPI().fileRecord(recordBodyFile, recordElectronic.getId());

        //check the response status
        assertStatusCode(CREATED);

        //Get the  folder ID created
        String folderId= getRestAPIFactory().getFilePlanComponentsAPI().getFilePlanComponent(FILE_PLAN_ALIAS, "relativePath="+RELATIVE_PATH).getId();
        //check the parent id for the record returned
        assertEquals(recordFiled.getParentId(), folderId);
        //check the record is filed into the record folder
        assertTrue(getRestAPIFactory().getFilePlanComponentsAPI().listChildComponents(folderId)
                                      .getEntries().stream()
                                      .anyMatch(c -> c.getFilePlanComponentModel().getId()
                                                      .equals(recordElectronic.getId())
                                               )
                  );
        //check the record doesn't exist into unfiled record container
        assertFalse(getRestAPIFactory().getFilePlanComponentsAPI().listChildComponents(unfiledContainerId)
                                       .getEntries().stream()
                                       .anyMatch(c -> c.getFilePlanComponentModel().getId()
                                                       .equals(recordElectronic.getId())
                                                )
                   );
        //file the non-electronic record into the folder created
        FilePlanComponent nonElectRecordFiled = getRestAPIFactory().getRecordsAPI().fileRecord(recordBodyFile, recordNonElectId.getId());
        //check the response status code
        assertStatusCode(CREATED);
        //check the parent id for the record returned
        assertEquals(nonElectRecordFiled.getParentId(), folderId);
        //check the record is added into the record folder
        assertTrue(getRestAPIFactory().getFilePlanComponentsAPI().listChildComponents(folderId)
                                      .getEntries().stream()
                                      .anyMatch(c -> c.getFilePlanComponentModel().getId()
                                                      .equals(recordNonElectId.getId())
                                               )
                  );
        //check the record doesn't exist into unfiled record container
        assertFalse(getRestAPIFactory().getFilePlanComponentsAPI().listChildComponents(unfiledContainerId)
                                       .getEntries().stream()
                                       .anyMatch(c -> c.getFilePlanComponentModel().getId()
                                                       .equals(recordNonElectId.getId())
                                                )
                   );
    }

}
