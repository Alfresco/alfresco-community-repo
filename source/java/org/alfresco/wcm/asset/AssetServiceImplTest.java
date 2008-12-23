/*
 * Copyright (C) 2005-2008 Alfresco Software Limited.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.

 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.

 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.

 * As a special exception to the terms and conditions of version 2.0 of 
 * the GPL, you may redistribute this Program in connection with Free/Libre 
 * and Open Source Software ("FLOSS") applications as described in Alfresco's 
 * FLOSS exception.  You should have recieved a copy of the text describing 
 * the FLOSS exception, and it is also available here: 
 * http://www.alfresco.com/legal/licensing"
 */
package org.alfresco.wcm.asset;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import junit.framework.TestCase;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.content.MimetypeMap;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.service.cmr.avm.AVMNotFoundException;
import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.service.cmr.repository.ContentWriter;
import org.alfresco.service.cmr.security.AuthenticationService;
import org.alfresco.service.cmr.security.PersonService;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.ApplicationContextHelper;
import org.alfresco.util.PropertyMap;
import org.alfresco.wcm.sandbox.SandboxInfo;
import org.alfresco.wcm.sandbox.SandboxService;
import org.alfresco.wcm.util.WCMUtil;
import org.alfresco.wcm.webproject.WebProjectInfo;
import org.alfresco.wcm.webproject.WebProjectService;
import org.springframework.context.ApplicationContext;

/**
 * Asset Service implementation unit test
 * 
 * @author janv
 */
public class AssetServiceImplTest extends TestCase 
{
    private static final ApplicationContext ctx = ApplicationContextHelper.getApplicationContext();
    
    //
    // test data
    //
    
    private static final String TEST_RUN = ""+System.currentTimeMillis();
    private static final boolean CLEAN = true; // cleanup during teardown
    
    // base web project
    private static final String TEST_WEBPROJ_DNS  = "testAsset-"+TEST_RUN;
    private static final String TEST_WEBPROJ_NAME = "testAsset Web Project Display Name - "+TEST_RUN;
    private static final String TEST_WEBPROJ_TITLE = "This is my title";
    private static final String TEST_WEBPROJ_DESCRIPTION = "This is my description";
    private static final String TEST_WEBPROJ_DEFAULT_WEBAPP = WCMUtil.DIR_ROOT;
    private static final boolean TEST_WEBPROJ_DONT_USE_AS_TEMPLATE = false;
    
    private static final String USER_ADMIN = "admin";
    
    private static final String TEST_USER = "testAssetUser-"+TEST_RUN;
    
    private static final String USER_ONE = TEST_USER+"-One";
    private static final String USER_TWO = TEST_USER+"-Two";
    private static final String USER_THREE = TEST_USER+"-Three";
    
    //
    // services
    //
    
    private WebProjectService wpService;
    private SandboxService sbService;
    private AssetService assetService;
    
    private AuthenticationService authenticationService;
    private PersonService personService;
    
    @Override
    protected void setUp() throws Exception
    {
        // Get the required services
        wpService = (WebProjectService)ctx.getBean("WebProjectService");
        sbService = (SandboxService)ctx.getBean("SandboxService");
        assetService = (AssetService)ctx.getBean("AssetService");
        
        authenticationService = (AuthenticationService)ctx.getBean("AuthenticationService");
        personService = (PersonService)ctx.getBean("PersonService");
        
        // By default run as Admin
        AuthenticationUtil.setFullyAuthenticatedUser(USER_ADMIN);
        
        createUser(USER_ONE);
        createUser(USER_TWO);
        createUser(USER_THREE);
    }
    
    @Override
    protected void tearDown() throws Exception
    {
        if (CLEAN)
        {
            // Switch back to Admin
            AuthenticationUtil.setFullyAuthenticatedUser(USER_ADMIN);
            
            List<WebProjectInfo> webProjects = wpService.listWebProjects();
            for (WebProjectInfo wpInfo : webProjects)
            {
                if (wpInfo.getStoreId().startsWith(TEST_WEBPROJ_DNS))
                {
                    wpService.deleteWebProject(wpInfo.getNodeRef());
                }
            }

            deleteUser(USER_ONE);
            deleteUser(USER_TWO);
            deleteUser(USER_THREE);
        }
        
        AuthenticationUtil.clearCurrentSecurityContext();
        super.tearDown();
    }
    
    private void createUser(String userName)
    {
        if (authenticationService.authenticationExists(userName) == false)
        {
            authenticationService.createAuthentication(userName, "PWD".toCharArray());
            
            PropertyMap ppOne = new PropertyMap(4);
            ppOne.put(ContentModel.PROP_USERNAME, userName);
            ppOne.put(ContentModel.PROP_FIRSTNAME, "firstName");
            ppOne.put(ContentModel.PROP_LASTNAME, "lastName");
            ppOne.put(ContentModel.PROP_EMAIL, "email@email.com");
            ppOne.put(ContentModel.PROP_JOBTITLE, "jobTitle");
            
            personService.createPerson(ppOne);
        }
    }
    
    private void deleteUser(String userName)
    {
        if (authenticationService.authenticationExists(userName) == true)
        {
            personService.deletePerson(userName);
            authenticationService.deleteAuthentication(userName);
        }
    }
    
    private void checkAssetInfo(AssetInfo assetInfo, String expectedName, String expectedPath, String expectedCreator, boolean expectedIsFile, boolean expectedIsFolder, boolean expectedIsDeleted, boolean expectedIsLocked, String expectedLockOwner)
    {
        assertNotNull(assetInfo);
        
        assertEquals(expectedName, assetInfo.getName());
        assertEquals(expectedPath, assetInfo.getPath());
        assertEquals(expectedCreator, assetInfo.getCreator());
        
        assertEquals(expectedIsFile, assetInfo.isFile());
        assertEquals(expectedIsFolder, assetInfo.isFolder());
        assertEquals(expectedIsDeleted, assetInfo.isDeleted());
        
        assertNotNull(assetInfo.getCreatedDate());
        
        assertEquals(expectedIsLocked, assetInfo.isLocked());
        assertEquals(expectedLockOwner, assetInfo.getLockOwner());
    }
    
    public void testSimple()
    {
        // create web project (also creates staging sandbox and admin's author sandbox)
        WebProjectInfo wpInfo = wpService.createWebProject(TEST_WEBPROJ_DNS+"-simple", TEST_WEBPROJ_NAME+"-simple", TEST_WEBPROJ_TITLE, TEST_WEBPROJ_DESCRIPTION, TEST_WEBPROJ_DEFAULT_WEBAPP, TEST_WEBPROJ_DONT_USE_AS_TEMPLATE, null);
        
        // get admin's author sandbox
        SandboxInfo sbInfo = sbService.getAuthorSandbox(wpInfo.getStoreId());
        String sbStoreId = sbInfo.getSandboxId();
        
        String path = sbInfo.getSandboxRootPath() + "/" + wpInfo.getDefaultWebApp();
        
        // create folder
        assetService.createFolder(sbStoreId, path, "myFolder1", null);
        
        // create (empty) file
        assetService.createFile(sbStoreId, path+"/myFolder1", "myFile1", null);
        
        // get assets
        
        AssetInfo myFolder1Asset = assetService.getAsset(sbStoreId, path+"/myFolder1");
        checkAssetInfo(myFolder1Asset, "myFolder1", path+"/myFolder1", USER_ADMIN, false, true, false, false, null);
        
        AssetInfo myFile1Asset = assetService.getAsset(sbStoreId, path+"/myFolder1/myFile1");
        checkAssetInfo(myFile1Asset, "myFile1", path+"/myFolder1/myFile1", USER_ADMIN, true, false, false, true, USER_ADMIN);
        
        // delete folder
        assetService.deleteAsset(myFolder1Asset); // also deletes myFile1
        
        // try to get assets (including deleted)
        
        myFolder1Asset = assetService.getAsset(sbStoreId, -1, path+"/myFolder1", true);
        assertNull(myFolder1Asset);
        
        myFile1Asset = assetService.getAsset(sbStoreId, -1, path+"/myFolder1/myFile1", true);
        assertNull(myFile1Asset);
    }
    
    /**
     * Test CRUD - create, retrieve (get, list), update and delete
     */
    public void testCRUD() throws IOException
    {
        // create web project (also creates staging sandbox and admin's author sandbox)
        WebProjectInfo wpInfo = wpService.createWebProject(TEST_WEBPROJ_DNS+"-crud", TEST_WEBPROJ_NAME+"-crud", TEST_WEBPROJ_TITLE, TEST_WEBPROJ_DESCRIPTION, TEST_WEBPROJ_DEFAULT_WEBAPP, TEST_WEBPROJ_DONT_USE_AS_TEMPLATE, null);
        String defaultWebApp = wpInfo.getDefaultWebApp();
        
        // invite web user and auto-create their (author) sandbox
        wpService.inviteWebUser(wpInfo.getStoreId(), USER_ONE, WCMUtil.ROLE_CONTENT_MANAGER, true);
        
        // switch to user
        AuthenticationUtil.setFullyAuthenticatedUser(USER_ONE);
        
        // get user's author sandbox
        SandboxInfo sbInfo = sbService.getAuthorSandbox(wpInfo.getStoreId());
        String sbStoreId = sbInfo.getSandboxId();
        
        String path = sbInfo.getSandboxRootPath() + "/" + defaultWebApp;
        
        // get non-existent assets
        assertNull(assetService.getAsset(sbStoreId, path+"/myFolder1"));
        assertNull(assetService.getAsset(sbStoreId, path+"/myFile1"));
        assertNull(assetService.getAsset(sbStoreId, path+"/myFolder1/myFile2"));
        
        assertEquals(0, assetService.listAssets(sbStoreId, path, false).size());
        
        // create folder
        assetService.createFolder(sbStoreId, path, "myFolder1", null);
        
        assertEquals(1, assetService.listAssets(sbStoreId, path, false).size());
        assertEquals(0, assetService.listAssets(sbStoreId, path+"/myFolder1", false).size());
        
        // create file (and add content)
        final String MYFILE1 = "This is myFile1";
        ContentWriter writer = assetService.createFile(sbStoreId, path, "myFile1", null);
        writer.setMimetype(MimetypeMap.MIMETYPE_TEXT_PLAIN);
        writer.setEncoding("UTF-8");
        writer.putContent(MYFILE1);
        
        assertEquals(2, assetService.listAssets(sbStoreId, path, false).size());
        
        final String MYFILE2 = "This is myFile2";
        writer = assetService.createFile(sbStoreId, path+"/myFolder1", "myFile2", null);
        writer.setMimetype(MimetypeMap.MIMETYPE_TEXT_PLAIN);
        writer.setEncoding("UTF-8");
        writer.putContent(MYFILE2);
        
        assertEquals(1, assetService.listAssets(sbStoreId, path+"/myFolder1", false).size());
        
        // get assets (not including deleted)
        
        AssetInfo myFolder1Asset = assetService.getAsset(sbStoreId, path+"/myFolder1");
        checkAssetInfo(myFolder1Asset, "myFolder1", path+"/myFolder1", USER_ONE, false, true, false, false, null);
        
        AssetInfo myFile1Asset = assetService.getAsset(sbStoreId, path+"/myFile1");
        checkAssetInfo(myFile1Asset, "myFile1", path+"/myFile1", USER_ONE, true, false, false, true, USER_ONE);
        
        // get content
        
        ContentReader reader = assetService.getContentReader(myFile1Asset);
        InputStream in = reader.getContentInputStream();
        byte[] buff = new byte[1024];
        in.read(buff);
        in.close();
        assertEquals(MYFILE1, new String(buff, 0, MYFILE1.length())); // assumes 1byte=1char
        
        AssetInfo myFile2Asset = assetService.getAsset(sbStoreId, path+"/myFolder1/myFile2");
        checkAssetInfo(myFile2Asset, "myFile2", path+"/myFolder1/myFile2", USER_ONE, true, false, false, true, USER_ONE);
        
        reader = assetService.getContentReader(myFile2Asset);
        in = reader.getContentInputStream();
        buff = new byte[1024];
        in.read(buff);
        in.close();
        assertEquals(MYFILE2, new String(buff, 0, MYFILE2.length())); // assumes 1byte=1char
        
        // update content
        
        final String MYFILE2_MODIFIED = "This is myFile2 ... modified";
        writer = assetService.getContentWriter(myFile2Asset);
        writer.setMimetype(MimetypeMap.MIMETYPE_TEXT_PLAIN);
        writer.setEncoding("UTF-8");
        writer.putContent(MYFILE2_MODIFIED);
        
        // get updated content
        
        reader = assetService.getContentReader(myFile2Asset);
        in = reader.getContentInputStream();
        buff = new byte[1024];
        in.read(buff);
        in.close();
        assertEquals(MYFILE2_MODIFIED, new String(buff, 0, MYFILE2_MODIFIED.length())); // assumes 1byte=1char
        
        // delete folders and files
        assetService.deleteAsset(myFile1Asset);
        assetService.deleteAsset(myFolder1Asset); // also deletes myFile2
        
        // try to get assets (including deleted)
        
        myFolder1Asset = assetService.getAsset(sbStoreId, -1, path+"/myFolder1", true);
        checkAssetInfo(myFolder1Asset, "myFolder1", path+"/myFolder1", USER_ONE, false, true, true, false, null); // TODO - unlike admin (testSimple)
        
        myFile1Asset = assetService.getAsset(sbStoreId, -1, path+"/myFile1", true);
        assertNull(myFile1Asset);
        
        myFile2Asset = assetService.getAsset(sbStoreId, -1, path+"/myFolder1/myFile2", true);
        assertNull(myFile2Asset);
        
        assertEquals(0, assetService.listAssets(sbStoreId, path, false).size());
        
        try
        {
            // -ve test
            assertEquals(0, assetService.listAssets(sbStoreId, path+"/myFolder1", false).size());
            fail("Cannot list assets within non-existant folder");
        }
        catch (AVMNotFoundException nfe)
        {
            // expected
        }
    }
    
    /**
     * Test CRUD in a webApp - create, retrieve (get, list), update and delete
     */
    public void testCRUDinWebApp() throws IOException
    {
        // create web project (also creates staging sandbox and admin's author sandbox)
        WebProjectInfo wpInfo = wpService.createWebProject(TEST_WEBPROJ_DNS+"-crudwebapp", TEST_WEBPROJ_NAME+"-crudwebapp", TEST_WEBPROJ_TITLE, TEST_WEBPROJ_DESCRIPTION, TEST_WEBPROJ_DEFAULT_WEBAPP, TEST_WEBPROJ_DONT_USE_AS_TEMPLATE, null);
        
        String myWebApp1 = "myWebApp1";
        wpService.createWebApp(wpInfo.getStoreId(), myWebApp1, null);
        
        // invite web user and auto-create their (author) sandbox
        wpService.inviteWebUser(wpInfo.getStoreId(), USER_ONE, WCMUtil.ROLE_CONTENT_MANAGER, true);
        
        // switch to user
        AuthenticationUtil.setFullyAuthenticatedUser(USER_ONE);
        
        // get user's author sandbox
        SandboxInfo sbInfo = sbService.getAuthorSandbox(wpInfo.getStoreId());
        String sbStoreId = sbInfo.getSandboxId();
        
        // get non-existent assets
        assertNull(assetService.getAssetWebApp(sbStoreId, myWebApp1, "/myFolder1"));
        assertNull(assetService.getAssetWebApp(sbStoreId, myWebApp1, "/myFile1"));
        assertNull(assetService.getAssetWebApp(sbStoreId, myWebApp1, "/myFolder1/myFile2"));
        
        assertEquals(0, assetService.listAssetsWebApp(sbStoreId, myWebApp1, "/", false).size());
        
        // create folder
        assetService.createFolderWebApp(sbStoreId, myWebApp1, "/", "myFolder1");
        
        assertEquals(1, assetService.listAssetsWebApp(sbStoreId, myWebApp1, "/", false).size());
        assertEquals(0, assetService.listAssetsWebApp(sbStoreId, myWebApp1, "/myFolder1", false).size());
        
        // create file (and add content)
        final String MYFILE1 = "This is myFile1";
        ContentWriter writer = assetService.createFileWebApp(sbStoreId, myWebApp1, "/", "myFile1");
        writer.setMimetype(MimetypeMap.MIMETYPE_TEXT_PLAIN);
        writer.setEncoding("UTF-8");
        writer.putContent(MYFILE1);
        
        assertEquals(2, assetService.listAssetsWebApp(sbStoreId, myWebApp1, "/", false).size());
        
        final String MYFILE2 = "This is myFile2";
        writer = assetService.createFileWebApp(sbStoreId, myWebApp1, "/myFolder1", "myFile2");
        writer.setMimetype(MimetypeMap.MIMETYPE_TEXT_PLAIN);
        writer.setEncoding("UTF-8");
        writer.putContent(MYFILE2);
        
        assertEquals(1, assetService.listAssetsWebApp(sbStoreId, myWebApp1, "/myFolder1", false).size());
        
        // get assets (not including deleted)
        String path = sbInfo.getSandboxRootPath() + "/" + myWebApp1;
        AssetInfo myFolder1Asset = assetService.getAssetWebApp(sbStoreId, myWebApp1, "/myFolder1");
        checkAssetInfo(myFolder1Asset, "myFolder1", path+"/myFolder1", USER_ONE, false, true, false, false, null);
        
        AssetInfo myFile1Asset = assetService.getAssetWebApp(sbStoreId, myWebApp1, "/myFile1");
        checkAssetInfo(myFile1Asset, "myFile1", path+"/myFile1", USER_ONE, true, false, false, true, USER_ONE);
        
        // get content
        
        ContentReader reader = assetService.getContentReader(myFile1Asset);
        InputStream in = reader.getContentInputStream();
        byte[] buff = new byte[1024];
        in.read(buff);
        in.close();
        assertEquals(MYFILE1, new String(buff, 0, MYFILE1.length())); // assumes 1byte=1char
        
        AssetInfo myFile2Asset = assetService.getAssetWebApp(sbStoreId, myWebApp1, "/myFolder1/myFile2");
        checkAssetInfo(myFile2Asset, "myFile2", path+"/myFolder1/myFile2", USER_ONE, true, false, false, true, USER_ONE);
        
        reader = assetService.getContentReader(myFile2Asset);
        in = reader.getContentInputStream();
        buff = new byte[1024];
        in.read(buff);
        in.close();
        assertEquals(MYFILE2, new String(buff, 0, MYFILE2.length())); // assumes 1byte=1char
        
        // update content
        
        final String MYFILE2_MODIFIED = "This is myFile2 ... modified";
        writer = assetService.getContentWriter(myFile2Asset);
        writer.setMimetype(MimetypeMap.MIMETYPE_TEXT_PLAIN);
        writer.setEncoding("UTF-8");
        writer.putContent(MYFILE2_MODIFIED);
        
        // get updated content
        
        reader = assetService.getContentReader(myFile2Asset);
        in = reader.getContentInputStream();
        buff = new byte[1024];
        in.read(buff);
        in.close();
        assertEquals(MYFILE2_MODIFIED, new String(buff, 0, MYFILE2_MODIFIED.length())); // assumes 1byte=1char
        
        // delete folders and files
        assetService.deleteAsset(myFile1Asset);
        assetService.deleteAsset(myFolder1Asset); // also deletes myFile2
        
        // try to get assets (including deleted)
        
        myFolder1Asset = assetService.getAssetWebApp(sbStoreId, myWebApp1, "/myFolder1", true);
        checkAssetInfo(myFolder1Asset, "myFolder1", path+"/myFolder1", USER_ONE, false, true, true, false, null); // TODO - unlike admin (testSimple)
        
        myFile1Asset = assetService.getAssetWebApp(sbStoreId, myWebApp1, "/myFile1", true);
        assertNull(myFile1Asset);
        
        myFile2Asset = assetService.getAssetWebApp(sbStoreId, myWebApp1, "/myFolder1/myFile2", true);
        assertNull(myFile2Asset);
        
        assertEquals(0, assetService.listAssetsWebApp(sbStoreId, myWebApp1, "/", false).size());
        
        try
        {
            // -ve test
            assertEquals(0, assetService.listAssetsWebApp(sbStoreId, myWebApp1, "/myFolder1", false).size());
            fail("Cannot list assets within non-existant folder");
        }
        catch (AVMNotFoundException nfe)
        {
            // expected
        }
    }
    
    public void testRenameFile()
    {
        // create web project (also creates staging sandbox and admin's author sandbox)
        WebProjectInfo wpInfo = wpService.createWebProject(TEST_WEBPROJ_DNS+"-renamefile", TEST_WEBPROJ_NAME+"-renamefile", TEST_WEBPROJ_TITLE, TEST_WEBPROJ_DESCRIPTION, TEST_WEBPROJ_DEFAULT_WEBAPP, TEST_WEBPROJ_DONT_USE_AS_TEMPLATE, null);
        String defaultWebApp = wpInfo.getDefaultWebApp();
        
        // invite web user and auto-create their (author) sandbox
        wpService.inviteWebUser(wpInfo.getStoreId(), USER_ONE, WCMUtil.ROLE_CONTENT_MANAGER, true);
        
        // switch to user
        AuthenticationUtil.setFullyAuthenticatedUser(USER_ONE);
        
        // get user's author sandbox
        SandboxInfo sbInfo = sbService.getAuthorSandbox(wpInfo.getStoreId());
        String sbStoreId = sbInfo.getSandboxId();
        
        String path = sbInfo.getSandboxRootPath() + "/" + defaultWebApp;
        
        // create folders
        assetService.createFolder(sbStoreId, path, "myFolder1", null);
        assetService.createFolder(sbStoreId, path+"/myFolder1", "myFolder2", null);
        
        // create file
        assetService.createFile(sbStoreId, path+"/myFolder1/myFolder2", "myFile1", null);
        
        // rename file
        AssetInfo myFile1Asset = assetService.getAsset(sbStoreId, path+"/myFolder1/myFolder2/myFile1");
        checkAssetInfo(myFile1Asset, "myFile1", path+"/myFolder1/myFolder2/myFile1", USER_ONE, true, false, false, true, USER_ONE);
        
        myFile1Asset = assetService.renameAsset(myFile1Asset, "myFile1Renamed");
        checkAssetInfo(myFile1Asset, "myFile1Renamed", path+"/myFolder1/myFolder2/myFile1Renamed", USER_ONE, true, false, false, true, USER_ONE);
    }
    
    /*
        // TODO lock issue ...
        // org.alfresco.service.cmr.avm.AVMNotFoundException: Lock not found for testAsset-1228476617644-rename:/www/avm_webapps/ROOT/myFolder1
        // at org.alfresco.repo.avm.locking.AVMLockingServiceImpl.modifyLock(AVMLockingServiceImpl.java:490)
        // at org.alfresco.repo.avm.AVMLockingAwareService.rename(AVMLockingAwareService.java:712)

    public void testRenameFolder()
    {
        // create web project (also creates staging sandbox and admin's author sandbox)
        WebProjectInfo wpInfo = wpService.createWebProject(TEST_WEBPROJ_DNS+"-renamefolder", TEST_WEBPROJ_NAME+"-renamefolder", TEST_WEBPROJ_TITLE, TEST_WEBPROJ_DESCRIPTION, TEST_WEBPROJ_DEFAULT_WEBAPP, TEST_WEBPROJ_DONT_USE_AS_TEMPLATE, null);
        String defaultWebApp = wpInfo.getDefaultWebApp();
        
        // invite web user and auto-create their (author) sandbox
        wpService.inviteWebUser(wpInfo.getStoreId(), USER_ONE, WCMUtil.ROLE_CONTENT_MANAGER, true);
        
        // switch to user
        AuthenticationUtil.setFullyAuthenticatedUser(USER_ONE);
        
        // get user's author sandbox
        SandboxInfo sbInfo = sbService.getAuthorSandbox(wpInfo.getStoreId());
        String sbStoreId = sbInfo.getSandboxId();
        
        String path = sbInfo.getSandboxRootPath() + "/" + defaultWebApp;
        
        // create folders
        assetService.createFolder(sbStoreId, path, "myFolder1", null);
        assetService.createFolder(sbStoreId, path+"/myFolder1", "myFolder2", null);
        
        AssetInfo myFolder1Asset = assetService.getAsset(sbStoreId, path+"/myFolder1");
        checkAssetInfo(myFolder1Asset, "myFolder1", path+"/myFolder1", USER_ONE, false, true, false, false, null);
        
        AssetInfo myFolder2Asset = assetService.getAsset(sbStoreId, path+"/myFolder1/myFolder2");
        checkAssetInfo(myFolder2Asset, "myFolder2", path+"/myFolder1/myFolder2", USER_ONE, false, true, false, false, null);
        
        // TODO lock issue ...
        // org.alfresco.service.cmr.avm.AVMNotFoundException: Lock not found for testAsset-1228476617644-rename:/www/avm_webapps/ROOT/myFolder1
        // at org.alfresco.repo.avm.locking.AVMLockingServiceImpl.modifyLock(AVMLockingServiceImpl.java:490)
        //at org.alfresco.repo.avm.AVMLockingAwareService.rename(AVMLockingAwareService.java:712)

        // rename folder
        myFolder1Asset = assetService.renameAsset(myFolder1Asset, "myFolder1Renamed");
        checkAssetInfo(myFolder1Asset, "myFolder1Renamed", path+"/myFolder1Renamed", USER_ONE, false, true, false, false, null);
        
        // rename folder
        myFolder2Asset = assetService.renameAsset(myFolder2Asset, "myFolder2Renamed");
        checkAssetInfo(myFolder2Asset, "myFolder2Renamed", path+"/myFolder1/myFolder2Renamed", USER_ONE, false, true, false, false, null);
    }
    */
    
    public void testCopyFile()
    {
        // create web project (also creates staging sandbox and admin's author sandbox)
        WebProjectInfo wpInfo = wpService.createWebProject(TEST_WEBPROJ_DNS+"-copyfile", TEST_WEBPROJ_NAME+"-copyfile", TEST_WEBPROJ_TITLE, TEST_WEBPROJ_DESCRIPTION, TEST_WEBPROJ_DEFAULT_WEBAPP, TEST_WEBPROJ_DONT_USE_AS_TEMPLATE, null);
        String defaultWebApp = wpInfo.getDefaultWebApp();
        
        // invite web user and auto-create their (author) sandbox
        wpService.inviteWebUser(wpInfo.getStoreId(), USER_ONE, WCMUtil.ROLE_CONTENT_MANAGER, true);
        
        // switch to user
        AuthenticationUtil.setFullyAuthenticatedUser(USER_ONE);
        
        // get user's author sandbox
        SandboxInfo sbInfo = sbService.getAuthorSandbox(wpInfo.getStoreId());
        String sbStoreId = sbInfo.getSandboxId();
        
        String path = sbInfo.getSandboxRootPath() + "/" + defaultWebApp;
        
        // create folders
        assetService.createFolder(sbStoreId, path, "myFolder1", null);
        assetService.createFolder(sbStoreId, path+"/myFolder1", "myFolder2", null);
        
        // create (nn-empty) file
        final String MYFILE1 = "This is myFile1";
        ContentWriter writer = assetService.createFile(sbStoreId, path+"/myFolder1/myFolder2", "myFile1", null);
        writer.setMimetype(MimetypeMap.MIMETYPE_TEXT_PLAIN);
        writer.setEncoding("UTF-8");
        writer.putContent(MYFILE1);
        
        // copy file - note: must have content
        AssetInfo myFile1Asset = assetService.getAsset(sbStoreId, path+"/myFolder1/myFolder2/myFile1");
        checkAssetInfo(myFile1Asset, "myFile1", path+"/myFolder1/myFolder2/myFile1", USER_ONE, true, false, false, true, USER_ONE);
        
        myFile1Asset = assetService.copyAsset(myFile1Asset, path+"/myFolder1");
        
        // TODO review - copied files are not locked ?
        //checkAssetInfo(myFile1Asset, "myFile1", path+"/myFolder1/myFile1", USER_ONE, true, false, false, true, USER_ONE);
        checkAssetInfo(myFile1Asset, "myFile1", path+"/myFolder1/myFile1", USER_ONE, true, false, false, false, null);
    }
    
    public void testCopyFolder()
    {
        // create web project (also creates staging sandbox and admin's author sandbox)
        WebProjectInfo wpInfo = wpService.createWebProject(TEST_WEBPROJ_DNS+"-copyfolder", TEST_WEBPROJ_NAME+"-copyfolder", TEST_WEBPROJ_TITLE, TEST_WEBPROJ_DESCRIPTION, TEST_WEBPROJ_DEFAULT_WEBAPP, TEST_WEBPROJ_DONT_USE_AS_TEMPLATE, null);
        String defaultWebApp = wpInfo.getDefaultWebApp();
        
        // invite web user and auto-create their (author) sandbox
        wpService.inviteWebUser(wpInfo.getStoreId(), USER_ONE, WCMUtil.ROLE_CONTENT_MANAGER, true);
        
        // switch to user
        AuthenticationUtil.setFullyAuthenticatedUser(USER_ONE);
        
        // get user's author sandbox
        SandboxInfo sbInfo = sbService.getAuthorSandbox(wpInfo.getStoreId());
        String sbStoreId = sbInfo.getSandboxId();
        
        String path = sbInfo.getSandboxRootPath() + "/" + defaultWebApp;
        
        // create folders
        assetService.createFolder(sbStoreId, path, "myFolder1", null);
        assetService.createFolder(sbStoreId, path+"/myFolder1", "myFolder2", null);
        
        // create (non-empty) file
        final String MYFILE1 = "This is myFile1";
        ContentWriter writer = assetService.createFile(sbStoreId, path+"/myFolder1/myFolder2", "myFile1", null);
        writer.setMimetype(MimetypeMap.MIMETYPE_TEXT_PLAIN);
        writer.setEncoding("UTF-8");
        writer.putContent(MYFILE1);
        
        AssetInfo myFile1Asset = assetService.getAsset(sbStoreId, path+"/myFolder1/myFolder2/myFile1");
        checkAssetInfo(myFile1Asset, "myFile1", path+"/myFolder1/myFolder2/myFile1", USER_ONE, true, false, false, true, USER_ONE);
        
        AssetInfo myFolder2Asset = assetService.getAsset(sbStoreId, path+"/myFolder1/myFolder2");
        checkAssetInfo(myFolder2Asset, "myFolder2", path+"/myFolder1/myFolder2", USER_ONE, false, true, false, false, null);
        
        // recursively copy folder
        myFolder2Asset = assetService.copyAsset(myFolder2Asset, path);
        
        checkAssetInfo(myFolder2Asset, "myFolder2", path+"/myFolder2", USER_ONE, false, true, false, false, null);
        
        AssetInfo myCopiedFolder2Asset = assetService.getAsset(sbStoreId, path+"/myFolder2");
        checkAssetInfo(myCopiedFolder2Asset, "myFolder2", path+"/myFolder2", USER_ONE, false, true, false, false, null);
        
        AssetInfo myCopiedFile1Asset = assetService.getAsset(sbStoreId, path+"/myFolder2/myFile1");
        
        // TODO review - copied files are not locked ?
        //checkAssetInfo(myCopiedFile1Asset, "myFile1", path+"/myFolder2/myFile1", USER_ONE, true, false, false, true, USER_ONE);
        checkAssetInfo(myCopiedFile1Asset, "myFile1", path+"/myFolder2/myFile1", USER_ONE, true, false, false, false, null);
    }
    
    public void testMoveFile()
    {
        // create web project (also creates staging sandbox and admin's author sandbox)
        WebProjectInfo wpInfo = wpService.createWebProject(TEST_WEBPROJ_DNS+"-movefile", TEST_WEBPROJ_NAME+"-movefile", TEST_WEBPROJ_TITLE, TEST_WEBPROJ_DESCRIPTION, TEST_WEBPROJ_DEFAULT_WEBAPP, TEST_WEBPROJ_DONT_USE_AS_TEMPLATE, null);
        String defaultWebApp = wpInfo.getDefaultWebApp();
        
        // invite web user and auto-create their (author) sandbox
        wpService.inviteWebUser(wpInfo.getStoreId(), USER_ONE, WCMUtil.ROLE_CONTENT_MANAGER, true);
        
        // switch to user
        AuthenticationUtil.setFullyAuthenticatedUser(USER_ONE);
        
        // get user's author sandbox
        SandboxInfo sbInfo = sbService.getAuthorSandbox(wpInfo.getStoreId());
        String sbStoreId = sbInfo.getSandboxId();
        
        String path = sbInfo.getSandboxRootPath() + "/" + defaultWebApp;
        
        // create folders
        assetService.createFolder(sbStoreId, path, "myFolder1", null);
        assetService.createFolder(sbStoreId, path+"/myFolder1", "myFolder2", null);
        
        // create (empty) file
        assetService.createFile(sbStoreId, path+"/myFolder1/myFolder2", "myFile1", null);
        
        // move file
        AssetInfo myFile1Asset = assetService.getAsset(sbStoreId, path+"/myFolder1/myFolder2/myFile1");
        checkAssetInfo(myFile1Asset, "myFile1", path+"/myFolder1/myFolder2/myFile1", USER_ONE, true, false, false, true, USER_ONE);
        
        myFile1Asset = assetService.moveAsset(myFile1Asset, path+"/myFolder1");
        
        // TODO review - moved files are not locked ?
        checkAssetInfo(myFile1Asset, "myFile1", path+"/myFolder1/myFile1", USER_ONE, true, false, false, true, USER_ONE);
        //checkAssetInfo(myFile1Asset, "myFile1", path+"/myFolder1/myFile1", USER_ONE, true, false, false, false, null);
    }
    
    /*
        // TODO lock issue ...
        // org.alfresco.service.cmr.avm.AVMNotFoundException: Lock not found for testAsset-1228830920248-movefolder:/www/avm_webapps/ROOT/myFolder1/myFolder2
        // at org.alfresco.repo.avm.locking.AVMLockingServiceImpl.modifyLock(AVMLockingServiceImpl.java:490)
        // at org.alfresco.repo.avm.AVMLockingAwareService.rename(AVMLockingAwareService.java:712)

    public void testMoveFolder()
    {
        // create web project (also creates staging sandbox and admin's author sandbox)
        WebProjectInfo wpInfo = wpService.createWebProject(TEST_WEBPROJ_DNS+"-movefolder", TEST_WEBPROJ_NAME+"-movefolder", TEST_WEBPROJ_TITLE, TEST_WEBPROJ_DESCRIPTION, TEST_WEBPROJ_DEFAULT_WEBAPP, TEST_WEBPROJ_DONT_USE_AS_TEMPLATE, null);
        String defaultWebApp = wpInfo.getDefaultWebApp();
        
        // invite web user and auto-create their (author) sandbox
        wpService.inviteWebUser(wpInfo.getStoreId(), USER_ONE, WCMUtil.ROLE_CONTENT_MANAGER, true);
        
        // switch to user
        AuthenticationUtil.setFullyAuthenticatedUser(USER_ONE);
        
        // get user's author sandbox
        SandboxInfo sbInfo = sbService.getAuthorSandbox(wpInfo.getStoreId());
        String sbStoreId = sbInfo.getSandboxId();
        
        String path = sbInfo.getSandboxRootPath() + "/" + defaultWebApp;
        
        // create folders
        assetService.createFolder(sbStoreId, path, "myFolder1", null);
        assetService.createFolder(sbStoreId, path+"/myFolder1", "myFolder2", null);
        
        // create (non-empty) file
        //final String MYFILE1 = "This is myFile1";
        ContentWriter writer = assetService.createFile(sbStoreId, path+"/myFolder1/myFolder2", "myFile1", null);
        //writer.setMimetype(MimetypeMap.MIMETYPE_TEXT_PLAIN);
        //writer.setEncoding("UTF-8");
        //writer.putContent(MYFILE1);
        
        AssetInfo myFile1Asset = assetService.getAsset(sbStoreId, path+"/myFolder1/myFolder2/myFile1");
        checkAssetInfo(myFile1Asset, "myFile1", path+"/myFolder1/myFolder2/myFile1", USER_ONE, true, false, false, true, USER_ONE);
        
        AssetInfo myFolder2Asset = assetService.getAsset(sbStoreId, path+"/myFolder1/myFolder2");
        checkAssetInfo(myFolder2Asset, "myFolder2", path+"/myFolder1/myFolder2", USER_ONE, false, true, false, false, null);
        
        // recursively move folder
        myFolder2Asset = assetService.moveAsset(myFolder2Asset, path);
        
        checkAssetInfo(myFolder2Asset, "myFolder2", path+"/myFolder2", USER_ONE, false, true, false, false, null);
        
        AssetInfo myMovedFolder2Asset = assetService.getAsset(sbStoreId, path+"/myFolder2");
        checkAssetInfo(myMovedFolder2Asset, "myFolder2", path+"/myFolder2", USER_ONE, false, true, false, false, null);
        
        AssetInfo myMovedFile1Asset = assetService.getAsset(sbStoreId, path+"/myFolder2/myFile1");
        
        // TODO review - moved files are not locked ?
        checkAssetInfo(myMovedFile1Asset, "myFile1", path+"/myFolder2/myFile1", USER_ONE, true, false, false, true, USER_ONE);
        //checkAssetInfo(myMovedFile1Asset, "myFile1", path+"/myFolder2/myFile1", USER_ONE, true, false, false, false, null);
    }
    */
    
    public void testProperties()
    {
        // create web project (also creates staging sandbox and admin's author sandbox)
        WebProjectInfo wpInfo = wpService.createWebProject(TEST_WEBPROJ_DNS+"-properties", TEST_WEBPROJ_NAME+"-properties", TEST_WEBPROJ_TITLE, TEST_WEBPROJ_DESCRIPTION, TEST_WEBPROJ_DEFAULT_WEBAPP, TEST_WEBPROJ_DONT_USE_AS_TEMPLATE, null);
        String defaultWebApp = wpInfo.getDefaultWebApp();
        
        // invite web user and auto-create their (author) sandbox
        wpService.inviteWebUser(wpInfo.getStoreId(), USER_ONE, WCMUtil.ROLE_CONTENT_CONTRIBUTOR, true);
        
        // switch to user
        AuthenticationUtil.setFullyAuthenticatedUser(USER_ONE);
        
        // get user's author sandbox
        SandboxInfo sbInfo = sbService.getAuthorSandbox(wpInfo.getStoreId());
        String sbStoreId = sbInfo.getSandboxId();
        
        String path = sbInfo.getSandboxRootPath() + "/" + defaultWebApp;
        
        // create folder
        assetService.createFolderWebApp(sbStoreId, defaultWebApp, "/", "myFolder1");
        AssetInfo myFolder1Asset = assetService.getAssetWebApp(sbStoreId, defaultWebApp, "/myFolder1");
        checkAssetInfo(myFolder1Asset, "myFolder1", path+"/myFolder1", USER_ONE, false, true, false, false, null);
        
        Map<QName, Serializable> props = assetService.getAssetProperties(myFolder1Asset);
        assertNotNull(props);
        int countFolderInbuiltProps = props.size();
        
        // create file
        assetService.createFileWebApp(sbStoreId, defaultWebApp, "/myFolder1", "myFile1");
        AssetInfo myFile1Asset = assetService.getAssetWebApp(sbStoreId, defaultWebApp, "/myFolder1/myFile1");
        checkAssetInfo(myFile1Asset, "myFile1", path+"/myFolder1/myFile1", USER_ONE, true, false, false, true, USER_ONE);
        
        props = assetService.getAssetProperties(myFile1Asset);
        assertNotNull(props);
        int countFileInbuiltProps = props.size();
        
        assertEquals(USER_ONE, assetService.getLockOwner(myFile1Asset));
        
        sbService.submitWebApp(sbStoreId, defaultWebApp, "submit1 label", "submit1 comment");
        
        assertNull(assetService.getLockOwner(myFile1Asset));
        
        // update (or set, if not already set) specific properties - eg. title and description
        
        Map<QName, Serializable> newProps = new HashMap<QName, Serializable>(2);
        newProps.put(ContentModel.PROP_TITLE, "folder title");
        newProps.put(ContentModel.PROP_DESCRIPTION, "folder description");
        
        assetService.updateAssetProperties(myFolder1Asset, newProps);
        props = assetService.getAssetProperties(myFolder1Asset);
        assertEquals((countFolderInbuiltProps+2), props.size());
        
        assertEquals("folder title", props.get(ContentModel.PROP_TITLE));
        assertEquals("folder description", props.get(ContentModel.PROP_DESCRIPTION));
        
        // set all (or replace existing) properties - eg. just title
        
        newProps = new HashMap<QName, Serializable>(1);
        newProps.put(ContentModel.PROP_TITLE, "folder title2");
        
        assetService.setAssetProperties(myFolder1Asset, newProps);
        props = assetService.getAssetProperties(myFolder1Asset);
        assertEquals((countFolderInbuiltProps+1), props.size());
        
        assertEquals("folder title2", props.get(ContentModel.PROP_TITLE));
        assertNull(props.get(ContentModel.PROP_DESCRIPTION));
        
        // set all (or replace existing) properties - eg. title and description
        
        newProps = new HashMap<QName, Serializable>(2);
        newProps.put(ContentModel.PROP_TITLE, "file title");
        newProps.put(ContentModel.PROP_DESCRIPTION, "file description");
        
        assetService.setAssetProperties(myFile1Asset, newProps);
        props = assetService.getAssetProperties(myFile1Asset);
        assertEquals((countFileInbuiltProps+2), props.size());
        
        assertEquals("file title", props.get(ContentModel.PROP_TITLE));
        assertEquals("file description", props.get(ContentModel.PROP_DESCRIPTION));
        
        assertEquals(USER_ONE, assetService.getLockOwner(myFile1Asset));
    }
    
    public void testSimpleLockFile()
    {
        // create web project (also creates staging sandbox and admin's author sandbox)
        WebProjectInfo wpInfo = wpService.createWebProject(TEST_WEBPROJ_DNS+"-simpleLock", TEST_WEBPROJ_NAME+"-simpleLock", TEST_WEBPROJ_TITLE, TEST_WEBPROJ_DESCRIPTION, TEST_WEBPROJ_DEFAULT_WEBAPP, TEST_WEBPROJ_DONT_USE_AS_TEMPLATE, null);
        String defaultWebApp = wpInfo.getDefaultWebApp();
        
        // invite web users and auto-create their (author) sandboxs
        wpService.inviteWebUser(wpInfo.getStoreId(), USER_ONE, WCMUtil.ROLE_CONTENT_CONTRIBUTOR, true);
        wpService.inviteWebUser(wpInfo.getStoreId(), USER_TWO, WCMUtil.ROLE_CONTENT_CONTRIBUTOR, true);
        wpService.inviteWebUser(wpInfo.getStoreId(), USER_THREE, WCMUtil.ROLE_CONTENT_MANAGER, true);
        
        // switch to user one
        AuthenticationUtil.setFullyAuthenticatedUser(USER_ONE);
        
        // get user's author sandbox
        SandboxInfo sbInfo = sbService.getAuthorSandbox(wpInfo.getStoreId());
        String sbStoreId = sbInfo.getSandboxId();
        
        String path = sbInfo.getSandboxRootPath() + "/" + defaultWebApp;
        
        // create file
        assetService.createFileWebApp(sbStoreId, defaultWebApp, "/", "myFile1");
        AssetInfo myFile1Asset = assetService.getAssetWebApp(sbStoreId, defaultWebApp, "myFile1");
        checkAssetInfo(myFile1Asset, "myFile1", path+"/myFile1", USER_ONE, true, false, false, true, USER_ONE);
        
        assertEquals(USER_ONE, assetService.getLockOwner(myFile1Asset));
        assertTrue(assetService.hasLockAccess(myFile1Asset));
        
        // switch to user two
        AuthenticationUtil.setFullyAuthenticatedUser(USER_TWO);
        
        assertEquals(USER_ONE, assetService.getLockOwner(myFile1Asset));
        assertFalse(assetService.hasLockAccess(myFile1Asset));
        
        // switch to user three
        AuthenticationUtil.setFullyAuthenticatedUser(USER_THREE);
        
        assertEquals(USER_ONE, assetService.getLockOwner(myFile1Asset));
        assertTrue(assetService.hasLockAccess(myFile1Asset)); // content manager
        
        // switch to user one
        AuthenticationUtil.setFullyAuthenticatedUser(USER_ONE);
        
        sbService.submitWebApp(sbStoreId, defaultWebApp, "submit1 label", "submit1 comment");
        
        assertNull(assetService.getLockOwner(myFile1Asset));
        assertTrue(assetService.hasLockAccess(myFile1Asset));
        
        // switch to user two
        AuthenticationUtil.setFullyAuthenticatedUser(USER_TWO);
        
        assertNull(assetService.getLockOwner(myFile1Asset));
        assertTrue(assetService.hasLockAccess(myFile1Asset));
    }
    
    public void testSimpleImport()
    {
        // create web project (also creates staging sandbox and admin's author sandbox)
        WebProjectInfo wpInfo = wpService.createWebProject(TEST_WEBPROJ_DNS+"-simpleImport", TEST_WEBPROJ_NAME+"-simpleImport", TEST_WEBPROJ_TITLE, TEST_WEBPROJ_DESCRIPTION, TEST_WEBPROJ_DEFAULT_WEBAPP, TEST_WEBPROJ_DONT_USE_AS_TEMPLATE, null);
        String defaultWebApp = wpInfo.getDefaultWebApp();
        
        // invite web user and auto-create their (author) sandbox
        wpService.inviteWebUser(wpInfo.getStoreId(), USER_ONE, WCMUtil.ROLE_CONTENT_CONTRIBUTOR, true);
        
        // switch to user
        AuthenticationUtil.setFullyAuthenticatedUser(USER_ONE);
        
        // get user's author sandbox
        SandboxInfo sbInfo = sbService.getAuthorSandbox(wpInfo.getStoreId());
        String sbStoreId = sbInfo.getSandboxId();
        
        String path = sbInfo.getSandboxRootPath() + "/" + defaultWebApp;
        
        // create folder
        assetService.createFolder(sbStoreId, path, "myFolder1", null);
        AssetInfo myFolder1Asset = assetService.getAsset(sbStoreId, path+"/myFolder1");
        
        // bulk import
        String testFile = System.getProperty("user.dir") + "/source/test-resources/module/test.war";
        
        File zipFile = new File(testFile);
        assetService.bulkImport(sbStoreId, myFolder1Asset.getPath(), zipFile, false);
    }
}
