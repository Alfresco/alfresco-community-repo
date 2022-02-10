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
package org.alfresco.rest.rm.community.files;

import static org.alfresco.rest.rm.community.model.fileplancomponents.FilePlanComponentAlias.UNFILED_RECORDS_CONTAINER_ALIAS;
import static org.alfresco.rest.rm.community.model.fileplancomponents.FilePlanComponentType.NON_ELECTRONIC_RECORD_TYPE;
import static org.alfresco.utility.report.log.Step.STEP;
import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.http.HttpStatus.FORBIDDEN;
import static org.springframework.http.HttpStatus.UNPROCESSABLE_ENTITY;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;

import java.io.InputStream;
import java.util.List;
import java.util.stream.Collectors;

import org.alfresco.dataprep.CMISUtil;
import org.alfresco.rest.model.RestNodeModel;
import org.alfresco.rest.rm.community.base.BaseRMRestTest;
import org.alfresco.rest.rm.community.model.record.Record;
import org.alfresco.rest.rm.community.model.record.RecordProperties;
import org.alfresco.rest.rm.community.model.unfiledcontainer.UnfiledContainerChildEntry;
import org.alfresco.rest.rm.community.requests.gscore.api.UnfiledContainerAPI;
import org.alfresco.rest.v0.RecordsAPI;
import org.alfresco.test.AlfrescoTest;
import org.alfresco.utility.constants.UserRole;
import org.alfresco.utility.model.FileModel;
import org.alfresco.utility.model.FolderModel;
import org.alfresco.utility.model.SiteModel;
import org.alfresco.utility.model.UserModel;
import org.apache.chemistry.opencmis.client.api.Document;
import org.apache.chemistry.opencmis.client.api.SecondaryType;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * Tests for Declare Documents as Records Action API
 *
 * @author Kristijan Conkas
 * @since 2.6
 */
public class DeclareDocumentAsRecordTests extends BaseRMRestTest
{
    private UserModel testUser, testUserReadOnly;
    private SiteModel testSite;
    private FolderModel testFolder;

    @Autowired
    RecordsAPI recordsAPI;

    @BeforeClass(alwaysRun=true)
    public void declareDocumentAsRecordSetup()
    {
        // create test user and test collaboration site to store documents in
        testUser = getDataUser().createRandomTestUser();
        testUserReadOnly = getDataUser().createRandomTestUser();

        testSite = dataSite.usingAdmin().createPublicRandomSite();

        getDataUser().addUserToSite(testUser, testSite, UserRole.SiteContributor);
        getDataUser().addUserToSite(testUserReadOnly, testSite, UserRole.SiteConsumer);

        testFolder = dataContent.usingSite(testSite).usingUser(testUser).createFolder();
    }

    /**
     * <pre>
     * Given a document that is not a record
     * And I have write permissions on the document
     * When I declare the document as a record
     * Then it is successfully moved into the unfiled record container
     * And it is renamed to reflect the record identifier
     * And it is now a record
     * And it remains a secondary child of the starting location where I can still view it
     * <pre>
     *
     * RM-6779
     * Given I declare a record using the v1 API
     * When I do not provide a location parameter
     * Then the record is declared in the unfiled folder
     *
     * @throws Exception for malformed JSON API response
     */
    @Test(description = "User with correct permissions can declare document as a record")
    @AlfrescoTest(jira = "RM-4429, RM-6779")
    public void userWithPrivilegesCanDeclareDocumentAsRecord() throws Exception
    {
        // create document in a folder in a collaboration site
        FileModel document = dataContent.usingSite(testSite)
            .usingUser(testUser)
            .usingResource(testFolder)
            .createContent(CMISUtil.DocumentType.TEXT_PLAIN);

        // declare document as record
        Record record = getRestAPIFactory().getFilesAPI(testUser).declareAsRecord(document.getNodeRefWithoutVersion());
        assertStatusCode(CREATED);

        // verify the declared record is in Unfiled Records folder
        UnfiledContainerAPI unfiledContainersAPI = getRestAPIFactory().getUnfiledContainersAPI();
        List<UnfiledContainerChildEntry> matchingRecords = unfiledContainersAPI.getUnfiledContainerChildren(UNFILED_RECORDS_CONTAINER_ALIAS)
            .getEntries()
            .stream()
            .filter(e -> e.getEntry().getId().equals(document.getNodeRefWithoutVersion()))
            .collect(Collectors.toList());
        // there should be only one matching record corresponding this document
        assertEquals(matchingRecords.size(), 1, "More than one record matching document name");

        // verify the original file in collaboration site has been renamed to reflect the record identifier
        List<RestNodeModel> filesAfterRename = getRestAPIFactory().getNodeAPI(testFolder)
                .listChildren()
                .getEntries()
                .stream()
                .filter(f -> f.onModel().getId().equals(document.getNodeRefWithoutVersion()))
                .collect(Collectors.toList());
        assertEquals(filesAfterRename.size(), 1, "There should be only one file in folder " + testFolder.getName());

        // verify the new name has the form of "<original name> (<record Id>).<original extension>"
        String recordName = filesAfterRename.get(0).onModel().getName();
        assertEquals(recordName, document.getName().replace(".", String.format(" (%s).", record.getProperties().getIdentifier())));

        // verify the document in collaboration site is now a record, note the file is now renamed hence folder + doc. name concatenation
        // this also verifies the document is still in the initial folder
        Document documentPostFiling = dataContent.usingSite(testSite)
            .usingUser(testUser)
            .getCMISDocument(testFolder.getCmisLocation() + "/" + recordName);

        // a document is a record if "Record" is one of its secondary types
        List<SecondaryType> documentSecondary = documentPostFiling.getSecondaryTypes()
            .stream()
            .filter(t -> t.getDisplayName().equals("Record"))
            .collect(Collectors.toList());
        assertFalse(documentSecondary.isEmpty(), "Document is not a record");

        // verify the document is readable and has same content as corresponding record
        try
        (
            InputStream recordInputStream = getRestAPIFactory().getRecordsAPI().getRecordContent(record.getId()).asInputStream();
            InputStream documentInputStream = documentPostFiling.getContentStream().getStream()
        )
        {
            assertEquals(DigestUtils.sha1(recordInputStream), DigestUtils.sha1(documentInputStream));
        }
    }

    /**
     * <pre>
     * Given a document that is not a record
     * And I have read permissions on the document
     * When I declare the document as a record
     * Then I get a permission denied exception
     * </pre>
     * @throws Exception for malformed JSON API response
     */
    @Test(description = "User with read-only permissions can't declare document a record")
    @AlfrescoTest(jira = "RM-4429")
    public void userWithReadPermissionsCantDeclare() throws Exception
    {
        // create document in a folder in a collaboration site
        FileModel document = dataContent.usingSite(testSite)
            .usingUser(testUser)
            .usingResource(testFolder)
            .createContent(CMISUtil.DocumentType.TEXT_PLAIN);

        // declare document as record as testUserReadOnly
        getRestAPIFactory().getFilesAPI(testUserReadOnly).declareAsRecord(document.getNodeRefWithoutVersion());
        assertStatusCode(FORBIDDEN);

        // verify the document is still in the original folder
        List<RestNodeModel> filesAfterRename = getRestAPIFactory().getNodeAPI(testFolder)
                .listChildren()
                .getEntries()
                .stream()
                .filter(f -> f.onModel().getId().equals(document.getNodeRefWithoutVersion()))
                .collect(Collectors.toList());
       assertEquals(filesAfterRename.size(), 1, "Declare as record failed but original document is missing");
    }

    /**
     * <pre>
     * Given a record
     * When I declare the record as a record
     * Then I get a invalid operation exception
     * </pre>
     */
    @Test(description = "Record can't be declared a record")
    @AlfrescoTest(jira = "RM-4429")
    public void recordCantBeDeclaredARecord()
    {
        // create a non-electronic record in a random folder
        Record nonelectronicRecord = Record.builder()
            .properties(RecordProperties.builder()
                .description("Description")
                .title("Title")
                .build())
            .name("Non-Electronic Record")
            .nodeType(NON_ELECTRONIC_RECORD_TYPE)
            .build();
        Record record = getRestAPIFactory().getRecordFolderAPI()
            .createRecord(nonelectronicRecord, createCategoryFolderInFilePlan().getId());
        assertStatusCode(CREATED);

        // try to declare it as a record
        getRestAPIFactory().getFilesAPI().declareAsRecord(record.getId());
        assertStatusCode(UNPROCESSABLE_ENTITY);
    }

    /**
     * <pre>
     * Given a node that is NOT a document
     * When I declare the node as a record
     * Then I get a invalid operation exception
     * </pre>
     */
    @Test(description = "Node that is not a document can't be declared a record")
    @AlfrescoTest(jira = "RM-4429")
    public void nonDocumentCantBeDeclaredARecord()
    {
        FolderModel otherTestFolder = dataContent.usingSite(testSite).usingUser(testUser).createFolder();

        // try to declare otherTestFolder as a record
        getRestAPIFactory().getFilesAPI().declareAsRecord(otherTestFolder.getNodeRefWithoutVersion());
        assertStatusCode(UNPROCESSABLE_ENTITY);
    }

    /**
     * Given a file that has version declared as record
     * When the file is declared as record
     * Then the action is successful
     */
    @Test (description = "Declaring as record a file that already has its version declared as record is successful")
    @AlfrescoTest (jira = "RM-6786")
    public void declareAsRecordAFileWithARecordVersion()
    {
        STEP("Create a file.");
        FileModel testFile = dataContent.usingAdmin().usingSite(testSite).createContent(CMISUtil.DocumentType.TEXT_PLAIN);

        STEP("Declare file version as record and check that record is successfully created.");
        recordsAPI.declareDocumentVersionAsRecord(getAdminUser().getUsername(), getAdminUser().getPassword(), testSite.getId(),
                testFile.getName());

        STEP("Declare file as record and check that record is successfully created.");
        getRestAPIFactory().getFilesAPI().declareAsRecord(testFile.getNodeRefWithoutVersion());
        assertStatusCode(CREATED);
    }

//    @Test(description = "Create 500 documents and declare them ass records concurently.")
//    public void declare500DocumentsAsRecordsConcurrently() throws Exception
//    {
//        FolderModel testFolder1 = dataContent.usingSite(testSite).usingUser(testUser).createFolder();
//        // create 500 documents in a folder in a collaboration site
//        List<FileModel> listOfDocuments = new ArrayList<FileModel>();
//        for(int i = 0; i < 500; i++)
//        {
//            FileModel document = dataContent.usingSite(testSite)
//                        .usingUser(testUser)
//                        .usingResource(testFolder1)
//                        .createContent(CMISUtil.DocumentType.TEXT_PLAIN);
//            listOfDocuments.add(document);
//        }
//
//        UnfiledContainerAPI unfiledContainersAPI = getRestAPIFactory().getUnfiledContainersAPI();
//        String unfiledContainerId = unfiledContainersAPI.getUnfiledContainer(UNFILED_RECORDS_CONTAINER_ALIAS).getId();
//        Counter.initSuccessCount(0);
//        Counter.initFailCount(0);
//        ExecutorService pool = Executors.newFixedThreadPool(16);
//        for (FileModel document : listOfDocuments)
//        {
//            pool.submit(new Task(document, unfiledContainerId));
//        }
//        pool.shutdown();
//        pool.awaitTermination(120L, TimeUnit.SECONDS);
//
//        assertEquals(Counter.getSuccessCount(), 500 - Counter.getFailCount());
//    }
//
//    class Task implements Runnable
//    {
//        private FileModel document;
//        private String unfiledContainerId;
//        public Task(FileModel document, String unfiledContainerId)
//        {
//            this.document = document;
//            this.unfiledContainerId = unfiledContainerId;
//        }
//
//        @Override
//        public void run()
//        {
//            String parentId = "";
//            try
//            {
//                Record record = getRestAPIFactory().getFilesAPI(testUser).declareAsRecord(document.getNodeRefWithoutVersion());
//                assertStatusCode(CREATED);
//
//                parentId = record.getParentId();
//            }
//            catch (Exception e)
//            {
//                Counter.incrementFailCount();
//                fail("Should not be here");
//            }
//
//            assertEquals(parentId, unfiledContainerId, "Declare as record was unsuccessful.");
//            Counter.incrementSuccessCount();
//        }
//    }
//
//    static class Counter
//    {
//        private static int successCount;
//        private static int failCount;
//
//        public static void initSuccessCount(int initialCount)
//        {
//            successCount = initialCount;
//        }
//
//        public static void initFailCount(int initialCount)
//        {
//            failCount = initialCount;
//        }
//
//        public static synchronized void incrementSuccessCount()
//        {
//            successCount++;
//        }
//
//        public static int getSuccessCount()
//        {
//            return successCount;
//        }
//
//        public static synchronized void incrementFailCount()
//        {
//            failCount++;
//        }
//
//        public static int getFailCount()
//        {
//            return failCount;
//        }
//    }
}
