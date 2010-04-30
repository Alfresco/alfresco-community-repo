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
package org.alfresco.repo.googledocs;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;

import javax.transaction.UserTransaction;

import junit.framework.TestCase;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.content.MimetypeMap;
import org.alfresco.repo.management.subsystems.ApplicationContextFactory;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.site.SiteServiceImpl;
import org.alfresco.service.cmr.coci.CheckOutCheckInService;
import org.alfresco.service.cmr.model.FileFolderService;
import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.service.cmr.repository.ContentService;
import org.alfresco.service.cmr.repository.ContentWriter;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.security.MutableAuthenticationService;
import org.alfresco.service.cmr.security.PersonService;
import org.alfresco.service.cmr.site.SiteService;
import org.alfresco.service.cmr.site.SiteVisibility;
import org.alfresco.service.transaction.TransactionService;
import org.alfresco.util.ApplicationContextHelper;
import org.alfresco.util.GUID;
import org.alfresco.util.PropertyMap;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;

import com.google.gdata.util.ServiceException;

public class GoogleDocumentServiceTest extends TestCase implements GoogleDocsModel
{
    private NodeService nodeService;
    private GoogleDocsService googleDocsService;
    private SiteService siteService;
    private TransactionService transactionService;
    private FileFolderService fileFolderService;
    private ContentService contentService;
    private CheckOutCheckInService checkOutCheckInService;
    private MutableAuthenticationService authenticationService;
    private PersonService personService;
    private ApplicationContextFactory subsystem;
    
    private static final String USER_ONE = "GoogleDocUserOne";
    private static final String USER_TWO = "GoogleDocUserTwo";
    private static final String USER_THREE = "GoogleDocUserThree";
    private static final String USER_FOUR = "GoogleDocUserFour";
    //private static final String EMAIL_DOMAIN = "@alfresco.com";
    
    private NodeRef folder = null;
    private NodeRef nodeRefDoc = null;
    private NodeRef nodeRefSpread = null;
    private NodeRef nodeRefPres = null;
    private NodeRef nodeRefPdf = null;
    private NodeRef nodeRef2 = null;
    private UserTransaction userTransaction = null;
    
    @Override
    protected void setUp() throws Exception
    {
        ApplicationContext appContext = ApplicationContextHelper.getApplicationContext();
        
        nodeService = (NodeService)appContext.getBean("nodeService");
        siteService = (SiteService)appContext.getBean("siteService");
        transactionService = (TransactionService)appContext.getBean("transactionService");
        fileFolderService = (FileFolderService)appContext.getBean("fileFolderService");
        contentService = (ContentService)appContext.getBean("contentService");
        checkOutCheckInService = (CheckOutCheckInService)appContext.getBean("checkOutCheckInService");
        authenticationService = (MutableAuthenticationService)appContext.getBean("authenticationService");
        personService = (PersonService)appContext.getBean("personService");
        
        // Start the user transaction
        userTransaction = transactionService.getUserTransaction();
        userTransaction.begin();
        
        // Get the sub-system and make sure the googleeditable feature is turned on
        subsystem = (ApplicationContextFactory)appContext.getBean("googledocs");      
        if (subsystem.getProperty("googledocs.googleeditable.enabled").equals("false") == true)
        {
            subsystem.stop();
            subsystem.setProperty("googledocs.googleeditable.enabled", "true");
            subsystem.start();
        }
        
        // Get the google docs service
        ConfigurableApplicationContext childContext = (ConfigurableApplicationContext)subsystem.getApplicationContext();
        googleDocsService = (GoogleDocsService)childContext.getBean("googleDocsService");   
        
        // Create the test users
        createUser(USER_ONE, "rwetherall@alfresco.com");
        createUser(USER_TWO, "rwetherall@activiti.com");
        createUser(USER_THREE, "roy.wetherall@alfresco.com");
        createUser(USER_FOUR, "roy.wetherall@activiti.com");
        
        // Authenticate as user one
        AuthenticationUtil.setFullyAuthenticatedUser(USER_ONE);
        
        String id = GUID.generate();
        
        // Create a site to use as holder for our test google documents
        siteService.createSite("sitePreset", id, "My Title", "My Description", SiteVisibility.PUBLIC);
        NodeRef container = siteService.createContainer(id, "testComponent", null, null);
        
        // Add some memberships to the site
        siteService.setMembership(id, USER_TWO, SiteServiceImpl.SITE_COLLABORATOR);
        siteService.setMembership(id, USER_THREE, SiteServiceImpl.SITE_CONTRIBUTOR);
        siteService.setMembership(id, USER_FOUR, SiteServiceImpl.SITE_CONSUMER);
        
        // Create a folder in our site container
        folder = fileFolderService.create(container, "myfolder", ContentModel.TYPE_FOLDER).getNodeRef();  
        
        // Create test documents
        nodeRefDoc = createTestDocument("mydoc.docx", "alfresco/subsystems/googledocs/default/test.docx", MimetypeMap.MIMETYPE_WORD);
        nodeRefSpread = createTestDocument("mydoc.xlsx", "alfresco/subsystems/googledocs/default/test.xlsx", MimetypeMap.MIMETYPE_EXCEL);
        
        // Create an empty content node (simulate creation of a new google doc in UI)
        nodeRef2 = fileFolderService.create(folder, "mygoogledoc.doc", ContentModel.TYPE_CONTENT).getNodeRef();
        nodeService.addAspect(nodeRef2, ASPECT_GOOGLEEDITABLE, null);
    }
    
    private NodeRef createTestDocument(String name, String contentPath, String mimetype)
    {
        NodeRef nodeRef = fileFolderService.create(folder, name, ContentModel.TYPE_CONTENT).getNodeRef();
        ContentWriter writer = contentService.getWriter(nodeRef, ContentModel.PROP_CONTENT, true);        
        writer.setEncoding("UTF-8");
        writer.setMimetype(mimetype);
        InputStream is = getClass().getClassLoader().getResourceAsStream(contentPath);
        writer.putContent(is);    
        return nodeRef;
    }
    
    private void createUser(String userName, String email)
    {
        if (authenticationService.authenticationExists(userName) == false)
        {
            authenticationService.createAuthentication(userName, "PWD".toCharArray());

            PropertyMap ppOne = new PropertyMap(4);
            ppOne.put(ContentModel.PROP_USERNAME, userName);
            ppOne.put(ContentModel.PROP_FIRSTNAME, "firstName");
            ppOne.put(ContentModel.PROP_LASTNAME, "lastName");
            ppOne.put(ContentModel.PROP_EMAIL, email);
            ppOne.put(ContentModel.PROP_JOBTITLE, "jobTitle");
            
            personService.createPerson(ppOne);
        }        
    }
    
    @Override
    protected void tearDown() throws Exception
    {
        if (userTransaction != null)
        {
            userTransaction.rollback();
        }
    }
    
    private boolean isGoogleServiceAvailable()
    {
    	boolean result = true;
    	try
    	{    	
    		googleDocsService.initialise();
    	}
    	catch (GoogleDocsServiceInitException e)
    	{
    		result = false;
    	}
    	return result;
    }
    
    public void testGoogleDocUploadDownload() throws Exception
    {
    	if (isGoogleServiceAvailable() == true)
    	{
	        googleDocsService.createGoogleDoc(nodeRefDoc, GoogleDocsPermissionContext.SHARE_WRITE);
	        
	        assertTrue(nodeService.hasAspect(nodeRefDoc, ASPECT_GOOGLERESOURCE));
	        assertNotNull(nodeService.getProperty(nodeRefDoc, PROP_URL));
	        assertNotNull(nodeService.getProperty(nodeRefDoc, PROP_RESOURCE_ID));
	        assertNotNull(nodeService.getProperty(nodeRefDoc, PROP_RESOURCE_TYPE));
	        
	        System.out.println("Google doc URL: " + nodeService.getProperty(nodeRefDoc, PROP_URL));
	        System.out.println("Google doc type: " + nodeService.getProperty(nodeRefDoc, PROP_RESOURCE_TYPE));
	        System.out.println("Google doc id: " + nodeService.getProperty(nodeRefDoc, PROP_RESOURCE_ID));                
	        String downloadFile = downloadFile(googleDocsService.getGoogleDocContent(nodeRefDoc), ".doc");
	        System.out.println("Download file: " + downloadFile);
	        
	//        googleDocsService.upload(nodeRefSpread, GoogleDocsPermissionContext.SHARE_WRITE);
	//        
	//        assertTrue(nodeService.hasAspect(nodeRefSpread, ASPECT_GOOGLERESOURCE));
	//        assertNotNull(nodeService.getProperty(nodeRefSpread, PROP_URL));
	//        assertNotNull(nodeService.getProperty(nodeRefSpread, PROP_RESOURCE_ID));
	//        assertNotNull(nodeService.getProperty(nodeRefSpread, PROP_RESOURCE_TYPE));
	//        
	//        System.out.println("Google doc URL: " + nodeService.getProperty(nodeRefSpread, PROP_URL));
	//        System.out.println("Google doc type: " + nodeService.getProperty(nodeRefSpread, PROP_RESOURCE_TYPE));
	//        System.out.println("Google doc id: " + nodeService.getProperty(nodeRefSpread, PROP_RESOURCE_ID));                
	//        downloadFile = downloadFile(googleDocsService.download(nodeRefSpread), ".xls");
	//        System.out.println("Download file: " + downloadFile);
    	}
        
    }
    
    public void testCheckOutCheckIn() throws Exception
    {
    	if (isGoogleServiceAvailable() == true)
    	{
	        ContentReader contentReader = contentService.getReader(nodeRef2, ContentModel.PROP_CONTENT);
	        assertNull(contentReader);
	        
	        // Check out the empty google document
	        NodeRef workingCopy = checkOutCheckInService.checkout(nodeRef2);
	        
	        assertTrue(nodeService.hasAspect(workingCopy, ASPECT_GOOGLERESOURCE));
	        assertNotNull(nodeService.getProperty(workingCopy, PROP_URL));
	        assertNotNull(nodeService.getProperty(workingCopy, PROP_RESOURCE_ID));
	        assertNotNull(nodeService.getProperty(workingCopy, PROP_RESOURCE_TYPE));
	
	        System.out.println("Google doc URL: " + nodeService.getProperty(workingCopy, PROP_URL));
	        System.out.println("Google doc type: " + nodeService.getProperty(workingCopy, PROP_RESOURCE_TYPE));
	        System.out.println("Google doc id: " + nodeService.getProperty(workingCopy, PROP_RESOURCE_ID));    
	        
	        checkOutCheckInService.checkin(workingCopy, null);
	        
	        assertFalse(nodeService.hasAspect(nodeRefDoc, ASPECT_GOOGLERESOURCE));
	        contentReader = contentService.getReader(nodeRef2, ContentModel.PROP_CONTENT);
	        assertNotNull(contentReader);        
    	}
    }    
    
    /**
     * Utility method to download input stream to a file for inspection
     * 
     * @param inStream
     * @param ext
     * @return
     * @throws IOException
     * @throws MalformedURLException
     * @throws ServiceException
     */
    private String downloadFile(InputStream inStream, String ext) throws IOException, MalformedURLException, ServiceException 
    {
        File file = File.createTempFile("googleDocTest", ext);
        String filePath = file.getAbsolutePath();
        FileOutputStream outStream = null;        
        try 
        {
            outStream = new FileOutputStream(filePath);
        
            int c;
            while ((c = inStream.read()) != -1) 
            {
              outStream.write(c);
            }
        } 
        finally 
        {
            if (inStream != null) 
            {
              inStream.close();
            }
            if (outStream != null) 
            {
              outStream.flush();
              outStream.close();
            }
        }
        
        return filePath;
    }
}
