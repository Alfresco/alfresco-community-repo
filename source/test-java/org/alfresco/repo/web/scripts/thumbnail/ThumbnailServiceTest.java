/*
 * Copyright (C) 2005-2010 Alfresco Software Limited.
 *
 * This file is part of Alfresco
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
 */
package org.alfresco.repo.web.scripts.thumbnail;

import java.io.InputStream;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.content.MimetypeMap;
import org.alfresco.repo.model.Repository;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.transaction.TransactionServiceImpl;
import org.alfresco.repo.web.scripts.BaseWebScriptTest;
import org.alfresco.service.cmr.model.FileFolderService;
import org.alfresco.service.cmr.model.FileInfo;
import org.alfresco.service.cmr.repository.ContentService;
import org.alfresco.service.cmr.repository.ContentWriter;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.security.MutableAuthenticationService;
import org.alfresco.service.cmr.security.PersonService;
import org.alfresco.test_category.OwnJVMTestsCategory;
import org.alfresco.util.GUID;
import org.alfresco.util.PropertyMap;
import org.junit.experimental.categories.Category;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.extensions.webscripts.TestWebScriptServer.GetRequest;
import org.springframework.extensions.webscripts.TestWebScriptServer.PostRequest;
import org.springframework.extensions.webscripts.TestWebScriptServer.Response;

/**
 * Unit test to test thumbnail web script API
 * 
 * @author Roy Wetherall
 */
@Category(OwnJVMTestsCategory.class)
public class ThumbnailServiceTest extends BaseWebScriptTest
{    
    private NodeRef testRoot;
    private NodeRef pdfNode;
    private NodeRef jpgNode;
    
    private FileFolderService fileFolderService;
    private ContentService contentService;
    private Repository repositoryHelper;
    private TransactionServiceImpl transactionService;
    private MutableAuthenticationService authenticationService;
    private PersonService personService;
    
    private static final String USER_ALFRESCO = "AlfrescoUser";
    
    @Override
    protected void setUp() throws Exception
    {
        super.setUp();       
 
        this.fileFolderService = (FileFolderService)getServer().getApplicationContext().getBean("FileFolderService");
        this.contentService = (ContentService)getServer().getApplicationContext().getBean("ContentService");
        this.repositoryHelper = (Repository)getServer().getApplicationContext().getBean("repositoryHelper");
        this.authenticationService = (MutableAuthenticationService)getServer().getApplicationContext().getBean("AuthenticationService");
        this.personService = (PersonService)getServer().getApplicationContext().getBean("PersonService");
        try
        {
            this.transactionService = (TransactionServiceImpl) getServer().getApplicationContext().getBean("transactionComponent");
        }
        catch (ClassCastException e)
        {
            throw new AlfrescoRuntimeException("The ThumbnailServiceTest needs direct access to the TransactionServiceImpl");
        }
        
        AuthenticationUtil.setFullyAuthenticatedUser(AuthenticationUtil.getSystemUserName());
        
        this.testRoot = this.repositoryHelper.getCompanyHome();
        
        // Get test content
        InputStream pdfStream = ThumbnailServiceTest.class.getClassLoader().getResourceAsStream("org/alfresco/repo/web/scripts/thumbnail/test_doc.pdf");        
        assertNotNull(pdfStream);
        InputStream jpgStream = ThumbnailServiceTest.class.getClassLoader().getResourceAsStream("org/alfresco/repo/web/scripts/thumbnail/test_image.jpg");
        assertNotNull(jpgStream);
        
        String guid = GUID.generate();
        
        // Create new nodes and set test content
        FileInfo fileInfoPdf = this.fileFolderService.create(this.testRoot, "test_doc" + guid + ".pdf", ContentModel.TYPE_CONTENT);
        this.pdfNode = fileInfoPdf.getNodeRef();
        ContentWriter contentWriter = this.contentService.getWriter(fileInfoPdf.getNodeRef(), ContentModel.PROP_CONTENT, true);
        contentWriter.setEncoding("UTF-8");
        contentWriter.setMimetype(MimetypeMap.MIMETYPE_PDF);
        contentWriter.putContent(pdfStream);
        
        FileInfo fileInfoJpg = this.fileFolderService.create(this.testRoot, "test_image" + guid + ".jpg", ContentModel.TYPE_CONTENT);
        this.jpgNode = fileInfoJpg.getNodeRef();
        contentWriter = this.contentService.getWriter(fileInfoJpg.getNodeRef(), ContentModel.PROP_CONTENT, true);
        contentWriter.setEncoding("UTF-8");
        contentWriter.setMimetype(MimetypeMap.MIMETYPE_IMAGE_JPEG);
        contentWriter.putContent(jpgStream);        
    }    
    
    @Override
    protected void tearDown() throws Exception
    {
        super.tearDown();
        transactionService.setAllowWrite(true);

        AuthenticationUtil.clearCurrentSecurityContext();
    }
    
    public void testCreateThumbnail() throws Exception
    {
        // Check for pdfToSWF transformation before doing test
        if (this.contentService.getTransformer(MimetypeMap.MIMETYPE_PDF, MimetypeMap.MIMETYPE_FLASH) != null)
        {
            String url = "/api/node/" + pdfNode.getStoreRef().getProtocol() + "/" + pdfNode.getStoreRef().getIdentifier() + "/" + pdfNode.getId() + "/content/thumbnails";
            
            JSONObject tn = new JSONObject();
            tn.put("thumbnailName", "webpreview");
            
            Response response = sendRequest(new PostRequest(url, tn.toString(), "application/json"), 200);
            
            System.out.println(response.getContentAsString());
        }
        
        // Check getAll whilst we are here 
        Response getAllResp = sendRequest(new GetRequest(getThumbnailsURL(jpgNode)), 200);
        JSONArray getArr = new JSONArray(getAllResp.getContentAsString());
        assertNotNull(getArr);
        assertEquals(0, getArr.length());
        
        // Do a image transformation (medium)
        String url = "/api/node/" + jpgNode.getStoreRef().getProtocol() + "/" + jpgNode.getStoreRef().getIdentifier() + "/" + jpgNode.getId() + "/content/thumbnails";
        JSONObject tn = new JSONObject();
        tn.put("thumbnailName", "medium");
        Response response = sendRequest(new PostRequest(url, tn.toString(), "application/json"), 200);
        System.out.println(response.getContentAsString());
        JSONObject result = new JSONObject(response.getContentAsString());
        String thumbnailUrl = result.getString("url").substring(17);
        
        System.out.println(thumbnailUrl);
        response = sendRequest(new GetRequest(thumbnailUrl), 200);
        
        // Check getAll whilst we are here 
        getAllResp = sendRequest(new GetRequest(getThumbnailsURL(jpgNode)), 200);
        getArr = new JSONArray(getAllResp.getContentAsString());
        assertNotNull(getArr);
        assertEquals(1, getArr.length());
        assertEquals("medium", getArr.getJSONObject(0).get("thumbnailName"));
        
    }
    
    private void createUser(String userName)
    {
        // if user with given user name doesn't already exist then create user
        if (this.authenticationService.authenticationExists(userName) == false)
        {
            // create user
            this.authenticationService.createAuthentication(userName, "password".toCharArray());
            
            // create person properties
            PropertyMap personProps = new PropertyMap();
            personProps.put(ContentModel.PROP_USERNAME, userName);
            personProps.put(ContentModel.PROP_FIRSTNAME, "First");
            personProps.put(ContentModel.PROP_LASTNAME, "Last");
            personProps.put(ContentModel.PROP_EMAIL, "FirstName123.LastName123@email.com");
            personProps.put(ContentModel.PROP_JOBTITLE, "JobTitle123");
            personProps.put(ContentModel.PROP_JOBTITLE, "Organisation123");
            
            // create person node for user
            this.personService.createPerson(personProps);
        }
    }
    
    public void testCreateThumbnailInReadonlyMode() throws Exception
    {
        createUser(USER_ALFRESCO);
        AuthenticationUtil.setFullyAuthenticatedUser(USER_ALFRESCO);
        this.transactionService.setAllowWrite(false);
        
        // do pdfToSWF transformation in read-only
        if (this.contentService.getTransformer(MimetypeMap.MIMETYPE_PDF, MimetypeMap.MIMETYPE_FLASH) != null)
        {
            // in share creation of thumbnail for webpreview is forced
            String url = "/api/node/" + pdfNode.getStoreRef().getProtocol() + "/" + pdfNode.getStoreRef().getIdentifier() + "/" + pdfNode.getId() + "/content/thumbnails/webpreview?c=force";
            
            JSONObject tn = new JSONObject();
            tn.put("thumbnailName", "webpreview");
            
            sendRequest(new GetRequest(url), 200, USER_ALFRESCO);
        }
        
        this.transactionService.setAllowWrite(true);
        
        // Check getAll whilst we are here 
        Response getAllResp = sendRequest(new GetRequest(getThumbnailsURL(jpgNode)), 200);
        JSONArray getArr = new JSONArray(getAllResp.getContentAsString());
        assertNotNull(getArr);
        assertEquals(0, getArr.length());
    }
    
    public void testThumbnailDefinitions() throws Exception
    {
        // Check for pdfToSWF transformation before doing test
        if (this.contentService.getTransformer(MimetypeMap.MIMETYPE_PDF, MimetypeMap.MIMETYPE_FLASH) != null)
        {
            String url = "/api/node/" + pdfNode.getStoreRef().getProtocol() + "/" + pdfNode.getStoreRef().getIdentifier() + "/" + pdfNode.getId() + "/content/thumbnaildefinitions";
            Response response = sendRequest(new GetRequest(url), 200);
            
            JSONArray array = new JSONArray(response.getContentAsString());
            assertNotNull(array);
            assertFalse(array.length() == 0);
            boolean hasMedium = false;
            boolean hasWebPreview = false;
            for (int i = 0; i < array.length(); i++)
            {
                if (array.getString(i).equals("medium") == true)
                {
                    hasMedium = true;
                }
                else if (array.getString(i).equals("webpreview") == true)
                {
                    hasWebPreview = true;
                }
            }
            assertTrue(hasMedium);
            assertTrue(hasWebPreview);
        }
        
        String url = "/api/node/" + jpgNode.getStoreRef().getProtocol() + "/" + jpgNode.getStoreRef().getIdentifier() + "/" + jpgNode.getId() + "/content/thumbnaildefinitions";
        Response response = sendRequest(new GetRequest(url), 200);
        
        JSONArray array = new JSONArray(response.getContentAsString());
        assertNotNull(array);
        assertFalse(array.length() == 0);
        boolean hasMedium = false;
        boolean hasWebPreview = false;
        for (int i = 0; i < array.length(); i++)
        {
            if (array.getString(i).equals("medium") == true)
            {
                hasMedium = true;
            }
            else if (array.getString(i).equals("webpreview") == true)
            {
                hasWebPreview = true;
            }
        }
        assertTrue(hasMedium);
        assertFalse(hasWebPreview);
    }
    
    public void testCreateAsyncThumbnail() throws Exception
    {
        // Check for pdfToSWF transformation before doing test
        if (this.contentService.getTransformer(MimetypeMap.MIMETYPE_PDF, MimetypeMap.MIMETYPE_FLASH) != null)
        {
            String url = "/api/node/" + pdfNode.getStoreRef().getProtocol() + "/" + pdfNode.getStoreRef().getIdentifier() + "/" + pdfNode.getId() + "/content/thumbnails?as=true";
            
            JSONObject tn = new JSONObject();
            tn.put("thumbnailName", "webpreview");
            
            Response response = sendRequest(new PostRequest(url, tn.toString(), "application/json"), 200);
            assertEquals("", response.getContentAsString().trim());
            getWait(pdfNode, "webpreview");            
        }
        
        // Do a image transformation (medium)
        String url = "/api/node/" + jpgNode.getStoreRef().getProtocol() + "/" + jpgNode.getStoreRef().getIdentifier() + "/" + jpgNode.getId() + "/content/thumbnails?as=true";
        JSONObject tn = new JSONObject();
        tn.put("thumbnailName", "medium");
        Response response = sendRequest(new PostRequest(url, tn.toString(), "application/json"), 200);

        assertEquals("", response.getContentAsString().trim());
        getWait(jpgNode, "medium");        
    }
    
    private void getWait(NodeRef node, String thumbnailName)
        throws Exception
    {
        String url = "/api/node/" + node.getStoreRef().getProtocol() + "/" + node.getStoreRef().getIdentifier() + "/" + node.getId() + "/content/thumbnails/" + thumbnailName;
       
        int retries = 50;
        int tries = 0;
        while (true)
        {
            if (tries >= retries)
            {
                fail("Thumbnail never gets created " + thumbnailName);
            }
            
            Response response = sendRequest(new GetRequest(url), 0);
            if (response.getStatus() == 200)
            {
                break;
            }
            else if (response.getStatus() == 500)
            {
                System.out.println("Error during getWait: " + response.getContentAsString());
                fail("A 500 status was found whilst waiting for the thumbnail to be processed");
            }
            else
            {
                Thread.sleep(100);
            }
            
            tries++;
        }        
    } 
    
    public void testPlaceHolder()
        throws Exception
    {
        if (this.contentService.getTransformer(MimetypeMap.MIMETYPE_PDF, MimetypeMap.MIMETYPE_FLASH) != null)
        {
            // Check that there is no place holder set for webpreview
            sendRequest(new GetRequest(getThumbnailsURL(pdfNode) + "/webpreview"), 404);
            sendRequest(new GetRequest(getThumbnailsURL(pdfNode) + "/webpreview?ph=true"), 404);
        }
        
        // Check that here is a place holder for medium
        sendRequest(new GetRequest(getThumbnailsURL(jpgNode) + "/medium"), 404);
        sendRequest(new GetRequest(getThumbnailsURL(jpgNode) + "/medium?ph=true"), 200);
        
        System.out.println(getThumbnailsURL(jpgNode) + "/medium?ph=true");
    }
    
    private String getThumbnailsURL(NodeRef nodeRef)
    {
        return "/api/node/" + nodeRef.getStoreRef().getProtocol() + "/" + nodeRef.getStoreRef().getIdentifier() + "/" + nodeRef.getId() + "/content/thumbnails";
    }
}
