/*
 * #%L
 * Alfresco Remote API
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
package org.alfresco.repo.web.scripts.upload;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.Serializable;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;
import org.alfresco.repo.version.VersionModel;
import org.alfresco.repo.version.VersionableAspectTest;
import org.alfresco.repo.web.scripts.BaseWebScriptTest;
import org.alfresco.service.cmr.coci.CheckOutCheckInService;
import org.alfresco.service.cmr.repository.ContentService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.security.AuthenticationService;
import org.alfresco.service.cmr.security.MutableAuthenticationService;
import org.alfresco.service.cmr.site.SiteInfo;
import org.alfresco.service.cmr.site.SiteService;
import org.alfresco.service.cmr.site.SiteVisibility;
import org.alfresco.service.cmr.version.VersionType;
import org.alfresco.service.namespace.QName;
import org.alfresco.service.transaction.TransactionService;
import org.alfresco.util.GUID;
import org.apache.commons.httpclient.methods.multipart.FilePart;
import org.apache.commons.httpclient.methods.multipart.MultipartRequestEntity;
import org.apache.commons.httpclient.methods.multipart.Part;
import org.apache.commons.httpclient.methods.multipart.StringPart;
import org.apache.commons.httpclient.params.HttpMethodParams;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.springframework.extensions.webscripts.TestWebScriptServer.PostRequest;
import org.springframework.extensions.webscripts.TestWebScriptServer.Response;
import org.springframework.util.ResourceUtils;

public class UploadWebScriptTest extends BaseWebScriptTest
{

    public static final String AUTO_VERSION_KEY = "version.store.enableAutoVersioning";
    public static final String AUTO_VERSION_PROPS_KEY = "version.store.enableAutoVersionOnUpdateProps";
    private static final String ADMIN_CREDENTIAL = "admin";

    private AuthenticationService authenticationService;
    private Properties globalProperties;
    private TransactionService transactionService;
    private SiteService siteService;
    private NodeService nodeService;
    private ContentService contentService;
    private CheckOutCheckInService checkOutCheckInService;
    private static final String DOCUMENT_SITE_NAME = "docSiteWithVersionLabel-.txt";
    private static final String UPLOAD_URL = "api/upload";
    private static final String RESOURCE_PREFIX = "publicapi/upload/";
    private static final String TEST_SITE_PRESET = "testSitePreset";
    private static final String TEST_TITLE = "TitleTest This is my title";
    private static final String TEST_DESCRIPTION = "DescriptionTest This is my description";

    private NodeRef documentSite;
    private String fileName;
    private File file;

    @Override
    protected void setUp() throws Exception
    {
        super.setUp();

        this.authenticationService = (MutableAuthenticationService) getServer().getApplicationContext().getBean("AuthenticationService");
        this.globalProperties = (Properties) getServer().getApplicationContext().getBean("global-properties");
        this.transactionService = (TransactionService) getServer().getApplicationContext().getBean("transactionService");
        this.siteService = (SiteService) getServer().getApplicationContext().getBean("SiteService");
        this.nodeService = (NodeService) getServer().getApplicationContext().getBean("nodeService");
        this.contentService = (ContentService) getServer().getApplicationContext().getBean("contentService");
        this.checkOutCheckInService = (CheckOutCheckInService) getServer().getApplicationContext().getBean("checkOutCheckInService");

        globalProperties.setProperty(AUTO_VERSION_PROPS_KEY, "true");

        transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<Void>()
        {
            @Override
            public Void execute() throws Throwable
            {
                authenticationService.authenticate(ADMIN_CREDENTIAL, ADMIN_CREDENTIAL.toCharArray());
                fileName = "quick-2.pdf";
                file = getResourceFile(fileName);

                // Create a Public site
                SiteInfo siteInfo = createSite("site" + GUID.generate(), "doclib", SiteVisibility.PUBLIC);
                
                //Upload file in a site using webScript
                Response response = uploadFileWs(file, fileName, siteInfo.getShortName(), "doclib");
                assertNotNull("content of file", response.getContentAsString());
                JSONObject jsonRsp = (JSONObject) JSONValue.parse(response.getContentAsString());
                final String ssdNodeRefString = (String) jsonRsp.get("nodeRef");
                assertNotNull("nodeRef", ssdNodeRefString);
                documentSite = new NodeRef(ssdNodeRefString);

                return null;
            }
        });
    }

    public void testChangeMetadataOnSite()
    {

        final String name11 = GUID.generate() + "02";
        final String name21 = GUID.generate() + "2.1";

        transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<Void>()
        {
            @Override
            public Void execute() throws Throwable
            {
                Map<QName, Serializable> properties = getAndAssertProperties(documentSite, "1.0");

                Serializable autoVersionProps = properties.get(ContentModel.PROP_AUTO_VERSION_PROPS);
                assertNotNull(("Autoversion property is NULL! NodeRef = '" + documentSite.toString() + "'"), autoVersionProps);
                assertTrue(("Autoversion must be TRUE! NodeRef = '" + documentSite.toString() + "'"), (Boolean) autoVersionProps);

                //change name minor version increment
                nodeService.setProperty(documentSite, ContentModel.PROP_NAME, name11);
                return null;
            }
        });
        
        Map<QName, Serializable> properties = getAndAssertProperties(documentSite, "1.1");
        assertEquals(name11, properties.get(ContentModel.PROP_NAME));

        transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<Void>()
        {
            @Override
            public Void execute() throws Throwable
            {
                NodeRef workingCopyDocSite = checkOutCheckInService.checkout(documentSite);
                contentService.getWriter(workingCopyDocSite, ContentModel.PROP_CONTENT, true).putContent("content new");
                Map<String, Serializable> versionProperties = new HashMap<String, Serializable>();
                versionProperties.put(VersionModel.PROP_VERSION_TYPE, VersionType.MAJOR);
                //checkIn major version increment
                documentSite = checkOutCheckInService.checkin(workingCopyDocSite, versionProperties);
                return null;
            }
        });

        getAndAssertProperties(documentSite, "2.0");

        transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<Void>()
        {
            @Override
            public Void execute() throws Throwable
            {
                //change name minor version increment
                nodeService.setProperty(documentSite, ContentModel.PROP_NAME, name21);
                return null;
            }
        });

        getAndAssertProperties(documentSite, "2.1");

    }

    private Response uploadFileWs(File file, String filename, String siteId, String containerId) throws IOException
    {
        PostRequest postRequest = buildMultipartPostRequest(file, filename, siteId, containerId);
        return sendRequest(postRequest, 200);
    }

    public PostRequest buildMultipartPostRequest(File file, String filename, String siteId, String containerId) throws IOException
    {
        Part[] parts = { new FilePart("filedata", file.getName(), file, "text/plain", null), new StringPart("filename", filename),
                new StringPart("description", "description"), new StringPart("siteid", siteId), new StringPart("containerid", containerId) };

        MultipartRequestEntity multipartRequestEntity = new MultipartRequestEntity(parts, new HttpMethodParams());

        ByteArrayOutputStream os = new ByteArrayOutputStream();
        multipartRequestEntity.writeRequest(os);

        PostRequest postReq = new PostRequest(UPLOAD_URL, os.toByteArray(), multipartRequestEntity.getContentType());
        return postReq;
    }

    private File getResourceFile(String fileName) throws FileNotFoundException
    {
        URL url = VersionableAspectTest.class.getClassLoader().getResource(RESOURCE_PREFIX + fileName);
        if (url == null)
        {
            fail("Cannot get the resource: " + fileName);
        }
        return ResourceUtils.getFile(url);
    }

    private SiteInfo createSite(String siteShortName, String componentId, SiteVisibility visibility)
    {
        // Create a public site
        SiteInfo siteInfo = this.siteService.createSite(TEST_SITE_PRESET, siteShortName, TEST_TITLE, TEST_DESCRIPTION, visibility);
        this.siteService.createContainer(siteShortName, componentId, ContentModel.TYPE_FOLDER, null);
        return siteInfo;
    }

    private Map<QName, Serializable> getAndAssertProperties(NodeRef nodeRef, String versionLabel)
    {
        assertNotNull("NodeRef of document is NULL!", nodeRef);

        Map<QName, Serializable> properties = nodeService.getProperties(nodeRef);

        assertNotNull(("Properties must not be NULL! NodeRef = '" + nodeRef.toString() + "'"), properties);
        assertFalse(("Version specific properties can't be found! NodeRef = '" + nodeRef.toString() + "'"), properties.isEmpty());
        assertEquals(versionLabel, properties.get(ContentModel.PROP_VERSION_LABEL));

        return properties;
    }

}
