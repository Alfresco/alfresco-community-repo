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
package org.alfresco.rest.rm.community.files;

import static org.alfresco.rest.rm.community.model.fileplancomponents.FilePlanComponentAlias.UNFILED_RECORDS_CONTAINER_ALIAS;
import static org.alfresco.rest.rm.community.model.fileplancomponents.FilePlanComponentType.NON_ELECTRONIC_RECORD_TYPE;
import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.http.HttpStatus.FORBIDDEN;
import static org.springframework.http.HttpStatus.UNPROCESSABLE_ENTITY;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;

import java.io.InputStream;
import java.util.List;
import java.util.stream.Collectors;

import org.alfresco.dataprep.CMISUtil;
import org.alfresco.rest.rm.community.base.BaseRMRestTest;
import org.alfresco.rest.rm.community.model.fileplancomponents.FilePlanComponent;
import org.alfresco.rest.rm.community.model.fileplancomponents.FilePlanComponentEntry;
import org.alfresco.rest.rm.community.model.fileplancomponents.FilePlanComponentProperties;
import org.alfresco.rest.rm.community.requests.igCoreAPI.FilePlanComponentAPI;
import org.alfresco.test.AlfrescoTest;
import org.alfresco.utility.constants.UserRole;
import org.alfresco.utility.data.DataUser;
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
    @Autowired
    private DataUser dataUser;

    private UserModel testUser, testUserReadOnly;
    private SiteModel testSite;
    private FolderModel testFolder;


    @BeforeClass(alwaysRun=true)
    public void declareDocumentAsRecordSetup() throws Exception
    {
        // create test user and test collaboration site to store documents in
        testUser = dataUser.createRandomTestUser();
        testUserReadOnly = dataUser.createRandomTestUser();
        
        testSite = dataSite.usingAdmin().createPublicRandomSite();
        
        dataUser.addUserToSite(testUser, testSite, UserRole.SiteContributor);
        dataUser.addUserToSite(testUserReadOnly, testSite, UserRole.SiteConsumer);
        
        testFolder = dataContent.usingSite(testSite).usingUser(testUser).createFolder();
    }
    
    /**
     * <pre>
     * Given a document that is not a record
     * And I have write permissions on the record
     * When I declare the document as a record
     * Then it successfully moved into the unfiled record container
     * And it is renamed to reflect the record identifier
     * And it is now a record
     * And it remains a secondary child of the starting location where I can still view it
     * <pre>
     * @throws Exception 
     */
    @Test(description = "User with correct permissions can declare document as a record")
    @AlfrescoTest(jira = "RM-4429")
    public void userWithPrivilegesCanDeclareDocumentAsRecord() throws Exception
    {        
        // create document in a folder in a collaboration site       
        FileModel document = dataContent.usingSite(testSite)
            .usingUser(testUser)
            .usingResource(testFolder)
            .createContent(CMISUtil.DocumentType.TEXT_PLAIN);
    
        // declare document as record
        FilePlanComponent record = getRestAPIFactory().getFilesAPI(testUser).declareAsRecord(document.getNodeRefWithoutVersion());
        assertStatusCode(CREATED);
       
        // verify the declared record is in Unfiled Records folder
        FilePlanComponentAPI filePlanComponentAPI = getRestAPIFactory().getFilePlanComponentsAPI();
        List<FilePlanComponentEntry> matchingRecords = filePlanComponentAPI.listChildComponents(UNFILED_RECORDS_CONTAINER_ALIAS)
            .getEntries()
            .stream()
            .filter(e -> e.getFilePlanComponentModel().getId().equals(document.getNodeRefWithoutVersion()))
            .collect(Collectors.toList());
        // there should be only one matching record corresponding this document
        assertEquals(matchingRecords.size(), 1, "More than one record matching document name");

        // verify the original file in collaboration site has been renamed to reflect the record identifier
        // FIXME: this call uses the FilePlanComponentAPI due to no TAS support for Node API in TAS restapi v 5.2.0-0. See RM-4585 for details.
        List<FilePlanComponentEntry> filesAfterRename = filePlanComponentAPI.listChildComponents(testFolder.getNodeRefWithoutVersion())
            .getEntries()
            .stream()
            .filter(f -> f.getFilePlanComponentModel().getId().equals(document.getNodeRefWithoutVersion()))
            .collect(Collectors.toList());
       assertEquals(filesAfterRename.size(), 1, "There should be only one file in folder " + testFolder.getName());
       
       // verify the new name has the form of "<original name> (<record Id>).<original extension>"
       assertEquals(filesAfterRename.get(0).getFilePlanComponentModel().getName(), 
           document.getName().replace(".", String.format(" (%s).", record.getProperties().getRecordId())));
       
       // verify the document in collaboration site is now a record, note the file is now renamed hence folder + doc. name concatenation
       // this also verifies the document is still in the initial folder
       Document documentPostFiling = dataContent.usingSite(testSite)
           .usingUser(testUser)
           .getCMISDocument(testFolder.getCmisLocation() + "/" + filesAfterRename.get(0).getFilePlanComponentModel().getName());
 
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
           InputStream documentInputStream = documentPostFiling.getContentStream().getStream();
       )
       {
           assertEquals(DigestUtils.sha1(recordInputStream), DigestUtils.sha1(documentInputStream));
       }
    }
    
    /**
     * <pre>
     * Given a document that is not a record
     * And I have read permissions on the record
     * When I declare the document as a record
     * Then I get a permission denied exception
     * </pre>
     * @throws Exception 
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
        // FIXME: this call uses the FilePlanComponentAPI due to no TAS support for Node API in TAS restapi v 5.2.0-0. See RM-4585 for details.
        List<FilePlanComponentEntry> filesAfterRename = getRestAPIFactory().getFilePlanComponentsAPI()
            .listChildComponents(testFolder.getNodeRefWithoutVersion())
            .getEntries()
            .stream()
            .filter(f -> f.getFilePlanComponentModel().getId().equals(document.getNodeRefWithoutVersion()))
            .collect(Collectors.toList());
       assertEquals(filesAfterRename.size(), 1, "Declare as record failed but original document is missing");
    }

    /**
     * <pre>
     * Given a record
     * When I declare the record as a record
     * Then I get a invalid operation exception
     * </pre>
     * @throws Exception
     */
    @Test(description = "Record can't be declared a record")
    @AlfrescoTest(jira = "RM-4429")
    public void recordCantBeDeclaredARecord() throws Exception
    {
        // create a non-electronic record in a random folder
        FilePlanComponent nonelectronicRecord = FilePlanComponent.builder()
            .properties(FilePlanComponentProperties.builder()
                .description("Description")
                .title("Title")
                .build())
            .name("Non-Electronic Record")
            .nodeType(NON_ELECTRONIC_RECORD_TYPE)
            .build();
        FilePlanComponent record = getRestAPIFactory().getFilePlanComponentsAPI()
            .createFilePlanComponent(nonelectronicRecord, createCategoryFolderInFilePlan().getId());
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
     * @throws Exception 
     */
    @Test(description = "Node that is not a document can't be declared a record")
    @AlfrescoTest(jira = "RM-4429")
    public void nonDocumentCantBeDeclaredARecord() throws Exception
    {
        FolderModel otherTestFolder = dataContent.usingSite(testSite).usingUser(testUser).createFolder();
        
        // try to declare otherTestFolder as a record
        getRestAPIFactory().getFilesAPI().declareAsRecord(otherTestFolder.getNodeRefWithoutVersion());
        assertStatusCode(UNPROCESSABLE_ENTITY);
    }
}
