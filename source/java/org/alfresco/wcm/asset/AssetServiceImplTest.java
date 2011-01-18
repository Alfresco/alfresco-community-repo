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
package org.alfresco.wcm.asset;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.content.MimetypeMap;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.security.permissions.AccessDeniedException;
import org.alfresco.service.cmr.avm.AVMNotFoundException;
import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.service.cmr.repository.ContentWriter;
import org.alfresco.service.namespace.QName;
import org.alfresco.wcm.AbstractWCMServiceImplTest;
import org.alfresco.wcm.sandbox.SandboxInfo;
import org.alfresco.wcm.util.WCMUtil;
import org.alfresco.wcm.webproject.WebProjectInfo;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Asset Service implementation unit test
 * 
 * @author janv
 */
public class AssetServiceImplTest extends AbstractWCMServiceImplTest
{
    private static Log logger = LogFactory.getLog(AssetServiceImplTest.class);
    
    // test data
    private static final String PREFIX = "created-by-admin-";
    private static final String FILE = "This is file1 - admin";
    
    @Override
    protected void setUp() throws Exception
    {
        super.setUp();
    }
    
    @Override
    protected void tearDown() throws Exception
    {
        super.tearDown();
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
        WebProjectInfo wpInfo = wpService.createWebProject(TEST_WEBPROJ_DNS+"-assetSimple", TEST_WEBPROJ_NAME+"-assetSimple", TEST_WEBPROJ_TITLE, TEST_WEBPROJ_DESCRIPTION, TEST_WEBPROJ_DEFAULT_WEBAPP, TEST_WEBPROJ_DONT_USE_AS_TEMPLATE, null);
        
        // get admin's author sandbox
        SandboxInfo sbInfo = sbService.getAuthorSandbox(wpInfo.getStoreId());
        String sbStoreId = sbInfo.getSandboxId();
        
        String path = sbInfo.getSandboxRootPath() + "/" + wpInfo.getDefaultWebApp();
        
        // create folder
        assetService.createFolder(sbStoreId, path, "myFolder1", null);
        
        // create (empty) file
        assetService.createFile(sbStoreId, path+"/myFolder1", "myFile1", null); // ignore return
        
        // get assets
        
        AssetInfo myFolder1Asset = assetService.getAsset(sbStoreId, path+"/myFolder1");
        checkAssetInfo(myFolder1Asset, "myFolder1", path+"/myFolder1", AuthenticationUtil.getAdminUserName(), false, true, false, false, null);
        
        AssetInfo myFile1Asset = assetService.getAsset(sbStoreId, path+"/myFolder1/myFile1");
        checkAssetInfo(myFile1Asset, "myFile1", path+"/myFolder1/myFile1", AuthenticationUtil.getAdminUserName(), true, false, false, true, AuthenticationUtil.getAdminUserName());
        
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
        assertNull(myFolder1Asset);
        
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
        assertNull(myFolder1Asset);
        
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
    
    /**
     * Test CRUD - create, retrieve (get, list), update and delete for each role
     */
    public void testCRUDforRoles() throws IOException, InterruptedException
    {
        // create web project (also creates staging sandbox and admin's author sandbox)
        WebProjectInfo wpInfo = wpService.createWebProject(TEST_WEBPROJ_DNS+"-crudroles", TEST_WEBPROJ_NAME+"-crudroles", TEST_WEBPROJ_TITLE, TEST_WEBPROJ_DESCRIPTION, TEST_WEBPROJ_DEFAULT_WEBAPP, TEST_WEBPROJ_DONT_USE_AS_TEMPLATE, null);
        
        String wpStoreId = wpInfo.getStoreId();
        String defaultWebApp = wpInfo.getDefaultWebApp();
        String stagingStoreId = wpInfo.getStagingStoreName();
        
        // get admin sandbox
        SandboxInfo sbInfo = sbService.getAuthorSandbox(wpStoreId);
        String sbStoreId = sbInfo.getSandboxId();
        
        // create some existing folders and files
        String[] users = new String[]{USER_ONE, USER_TWO, USER_THREE, USER_FOUR};
        for (String user : users)
        {
            assetService.createFolderWebApp(sbStoreId, defaultWebApp, "/", PREFIX+user);
            
            // create file (and add content)
            ContentWriter writer = assetService.createFileWebApp(sbStoreId, defaultWebApp, "/"+PREFIX+user, "fileA");
            writer.setMimetype(MimetypeMap.MIMETYPE_TEXT_PLAIN);
            writer.setEncoding("UTF-8");
            writer.putContent(FILE);
        }
        
        sbService.submitWebApp(sbStoreId, defaultWebApp, "some existing folders and files", "some existing folders and files");
        
        pollForSnapshotCount(stagingStoreId, 1);
        
        runCRUDforRoles(USER_ONE, WCMUtil.ROLE_CONTENT_MANAGER, wpStoreId, defaultWebApp, true, true, true);
        
        // TODO - pending ETHREEOH-1314 (see below) if updating folder properties
        runCRUDforRoles(USER_TWO, WCMUtil.ROLE_CONTENT_PUBLISHER, wpStoreId, defaultWebApp, true, true, false);
        runCRUDforRoles(USER_THREE, WCMUtil.ROLE_CONTENT_REVIEWER, wpStoreId, defaultWebApp, false, true, false);
        
        runCRUDforRoles(USER_FOUR, WCMUtil.ROLE_CONTENT_CONTRIBUTOR, wpStoreId, defaultWebApp, true, false, false);
    }
    
    private void runCRUDforRoles(String user, String role, final String wpStoreId, String defaultWebApp, boolean canCreate, boolean canUpdateExisting, boolean canDeleteExisting) throws IOException, InterruptedException
    {
        // switch to user - content manager
        AuthenticationUtil.setFullyAuthenticatedUser(AuthenticationUtil.getAdminUserName());
        
        // invite web user and auto-create their (author) sandbox
        wpService.inviteWebUser(wpStoreId, user, role, true);
        
        // switch to user
        AuthenticationUtil.setFullyAuthenticatedUser(user);
        
        // get staging sandbox
        String stagingStoreId = sbService.getStagingSandbox(wpStoreId).getSandboxId();
            
        // get user's author sandbox
        SandboxInfo sbInfo = sbService.getAuthorSandbox(wpStoreId);
        String sbStoreId = sbInfo.getSandboxId();
        String path = sbInfo.getSandboxRootPath() + "/" + defaultWebApp; // for checks only
        
        if (canCreate)
        {
            // create folder
            assetService.createFolderWebApp(sbStoreId, defaultWebApp, "/", user);
            
            // create file (and add content)
            final String MYFILE1 = "This is myFile1 - "+user;
            ContentWriter writer = assetService.createFileWebApp(sbStoreId, defaultWebApp, "/"+user, "fileA");
            writer.setMimetype(MimetypeMap.MIMETYPE_TEXT_PLAIN);
            writer.setEncoding("UTF-8");
            writer.putContent(MYFILE1);
            
            // list assets
            assertEquals(1, assetService.listAssetsWebApp(sbStoreId, defaultWebApp, "/"+user, false).size());
            
            // get assets
            AssetInfo myFolder1Asset = assetService.getAssetWebApp(sbStoreId, defaultWebApp, "/"+user);
            checkAssetInfo(myFolder1Asset, user, path+"/"+user, user, false, true, false, false, null);
            
            AssetInfo myFile1Asset = assetService.getAssetWebApp(sbStoreId, defaultWebApp, "/"+user+"/fileA");
            checkAssetInfo(myFile1Asset, "fileA", path+"/"+user+"/fileA", user, true, false, false, true, user);
            
            // get content
            
            ContentReader reader = assetService.getContentReader(myFile1Asset);
            InputStream in = reader.getContentInputStream();
            byte[] buff = new byte[1024];
            in.read(buff);
            in.close();
            assertEquals(MYFILE1, new String(buff, 0, MYFILE1.length())); // assumes 1byte=1char
            
            // update content
            
            final String MYFILE1_MODIFIED = "This is myFile1 ... modified";
            writer = assetService.getContentWriter(myFile1Asset);
            writer.setMimetype(MimetypeMap.MIMETYPE_TEXT_PLAIN);
            writer.setEncoding("UTF-8");
            writer.putContent(MYFILE1_MODIFIED);
            
            // get updated content
            
            reader = assetService.getContentReader(myFile1Asset);
            in = reader.getContentInputStream();
            buff = new byte[1024];
            in.read(buff);
            in.close();
            assertEquals(MYFILE1_MODIFIED, new String(buff, 0, MYFILE1_MODIFIED.length())); // assumes 1byte=1char
            
            // update folder properties - eg. title and description
            
            Map<QName, Serializable> newProps = new HashMap<QName, Serializable>(2);
            newProps.put(ContentModel.PROP_TITLE, "folder title");
            newProps.put(ContentModel.PROP_DESCRIPTION, "folder description");
            
            assetService.updateAssetProperties(myFolder1Asset, newProps);
            Map<QName, Serializable> props = assetService.getAssetProperties(myFolder1Asset);
            assertEquals("folder title", props.get(ContentModel.PROP_TITLE));
            assertEquals("folder description", props.get(ContentModel.PROP_DESCRIPTION));
            
            // Delete created file and folder
            assetService.deleteAsset(myFile1Asset);
            assetService.deleteAsset(myFolder1Asset);
        }
        else
        {
            try
            {
                // try to create folder (-ve test)
                assetService.createFolderWebApp(sbStoreId, defaultWebApp, "/", user);
                fail("User "+user+" with role "+role+" should not be able to create folder");
            }
            catch (AccessDeniedException ade)
            {
                // expected
            }
            
            try
            {
                // try to create file (-ve test)
                assetService.createFileWebApp(sbStoreId, defaultWebApp, "/", "file-"+user);
                fail("User "+user+" with role "+role+" should not be able to create file");
            }
            catch (AccessDeniedException ade)
            {
                // expected
            }
        }
         
        // list existing assets
        assertEquals(1, assetService.listAssetsWebApp(sbStoreId, defaultWebApp, "/"+PREFIX+user, false).size());
        
        // get existing assets
        AssetInfo existingFolder1Asset = assetService.getAssetWebApp(sbStoreId, defaultWebApp, "/"+PREFIX+user);
        checkAssetInfo(existingFolder1Asset, PREFIX+user, path+"/"+PREFIX+user, AuthenticationUtil.getAdminUserName(), false, true, false, false, null);
        
        AssetInfo existingFile1Asset = assetService.getAssetWebApp(sbStoreId, defaultWebApp, "/"+PREFIX+user+"/fileA");
        checkAssetInfo(existingFile1Asset, "fileA", path+"/"+PREFIX+user+"/fileA", AuthenticationUtil.getAdminUserName(), true, false, false, false, null);
        
        // get existing content
        
        ContentReader reader = assetService.getContentReader(existingFile1Asset);
        InputStream in = reader.getContentInputStream();
        byte[] buff = new byte[1024];
        in.read(buff);
        in.close();
        assertEquals(FILE, new String(buff, 0, FILE.length())); // assumes 1byte=1char
        
        if (canUpdateExisting)
        {
            // update content
            
            final String MYFILE1_MODIFIED = "This is myFile1 ... modified";
            ContentWriter writer = assetService.getContentWriter(existingFile1Asset);
            writer.setMimetype(MimetypeMap.MIMETYPE_TEXT_PLAIN);
            writer.setEncoding("UTF-8");
            writer.putContent(MYFILE1_MODIFIED);
            
            // get updated content
            
            reader = assetService.getContentReader(existingFile1Asset);
            in = reader.getContentInputStream();
            buff = new byte[1024];
            in.read(buff);
            in.close();
            assertEquals(MYFILE1_MODIFIED, new String(buff, 0, MYFILE1_MODIFIED.length())); // assumes 1byte=1char
            
            // update file properties - eg. title and description
            
            Map<QName, Serializable> newProps = new HashMap<QName, Serializable>(2);
            newProps.put(ContentModel.PROP_TITLE, "file title");
            newProps.put(ContentModel.PROP_DESCRIPTION, "file description");
            
            assetService.updateAssetProperties(existingFile1Asset, newProps);
            Map<QName, Serializable> props = assetService.getAssetProperties(existingFile1Asset);
            assertEquals("file title", props.get(ContentModel.PROP_TITLE));
            assertEquals("file description", props.get(ContentModel.PROP_DESCRIPTION));
            
            /* TODO - pending ETHREEOH-1314 - fails for content contributor / content publisher during submit if updating folder properties
            */
            // update folder properties - eg. title and description
            
            newProps = new HashMap<QName, Serializable>(2);
            newProps.put(ContentModel.PROP_TITLE, "folder title");
            newProps.put(ContentModel.PROP_DESCRIPTION, "folder description");
            
            assetService.updateAssetProperties(existingFolder1Asset, newProps);
            props = assetService.getAssetProperties(existingFolder1Asset);
            assertEquals("folder title", props.get(ContentModel.PROP_TITLE));
            assertEquals("folder description", props.get(ContentModel.PROP_DESCRIPTION));
            
            
        }
        else
        {
            try
            {
                // try to update file (-ve test)
                assetService.getContentWriter(existingFile1Asset);
                fail("User "+user+" with role "+role+" should not be able to update existing file");
            }
            catch (AccessDeniedException ade)
            {
                // expected
            }
            
            try
            {
                // try to update file properties (-ve test)
                Map<QName, Serializable> newProps = new HashMap<QName, Serializable>(2);
                newProps.put(ContentModel.PROP_TITLE, "file title");
                newProps.put(ContentModel.PROP_DESCRIPTION, "file description");
                
                assetService.updateAssetProperties(existingFile1Asset, newProps);
                fail("User "+user+" with role "+role+" should not be able to update existing file properties");
            }
            catch (AccessDeniedException ade)
            {
                // expected
            }
            
            try
            {
                // try to update folder properties (-ve test)
                Map<QName, Serializable> newProps = new HashMap<QName, Serializable>(2);
                newProps.put(ContentModel.PROP_TITLE, "folder title");
                newProps.put(ContentModel.PROP_DESCRIPTION, "folder description");
                
                assetService.updateAssetProperties(existingFolder1Asset, newProps);
                fail("User "+user+" with role "+role+" should not be able to update existing folder properties");
            }
            catch (AccessDeniedException ade)
            {
                // expected
            }
        }
        
        if (canDeleteExisting)
        {
            // Delete existing file and folder
            assetService.deleteAsset(existingFile1Asset);
            assetService.deleteAsset(existingFolder1Asset);
        }
        else
        {
            try
            {
                // try to delete file (-ve test)
                assetService.deleteAsset(existingFile1Asset);
                fail("User "+user+" with role "+role+" should not be able to delete existing file");
            }
            catch (AVMNotFoundException nfe)
            {
                // expected
            }
            
            try
            {
                // try to delete folder (-ve test)
                assetService.deleteAsset(existingFolder1Asset);
                fail("User "+user+" with role "+role+" should not be able to delete existing folder");
            }
            catch (AVMNotFoundException ade)
            {
                // expected
            }
        }
        
        // switch to admin (content manager)
        AuthenticationUtil.setFullyAuthenticatedUser(AuthenticationUtil.getAdminUserName());
        
        int snapCnt = sbService.listSnapshots(wpStoreId, false).size();
        
        // switch to user
        AuthenticationUtil.setFullyAuthenticatedUser(user);
        
        List<AssetInfo> changedAssets = sbService.listChangedWebApp(sbStoreId, defaultWebApp, true);
        
        if (changedAssets.size() > 0)
        {
            // submit the changes
            sbService.submitWebApp(sbStoreId, defaultWebApp, "some updates by "+user, "some updates by "+user);
            
            snapCnt += (canUpdateExisting || canDeleteExisting) ? (1):(0);
            pollForSnapshotCount(stagingStoreId, snapCnt);
            
            assertEquals(0, sbService.listChangedWebApp(sbStoreId, defaultWebApp, true).size());
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
        
        // rename folder 1
        myFolder1Asset = assetService.renameAsset(myFolder1Asset, "myFolder1Renamed");
        checkAssetInfo(myFolder1Asset, "myFolder1Renamed", path+"/myFolder1Renamed", USER_ONE, false, true, false, false, null);
        
        // rename folder 2
        myFolder2Asset = assetService.getAsset(sbStoreId, path+"/myFolder1Renamed/myFolder2");
        checkAssetInfo(myFolder2Asset, "myFolder2", path+"/myFolder1Renamed/myFolder2", USER_ONE, false, true, false, false, null);
        
        myFolder2Asset = assetService.renameAsset(myFolder2Asset, "myFolder2Renamed");
        checkAssetInfo(myFolder2Asset, "myFolder2Renamed", path+"/myFolder1Renamed/myFolder2Renamed", USER_ONE, false, true, false, false, null);
    }
    
    
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
        
        // create (non-empty) file
        final String MYFILE1 = "This is myFile1";
        ContentWriter writer = assetService.createFile(sbStoreId, path+"/myFolder1/myFolder2", "myFile1", null);
        writer.setMimetype(MimetypeMap.MIMETYPE_TEXT_PLAIN);
        writer.setEncoding("UTF-8");
        writer.putContent(MYFILE1);
        
        // copy file - note: must have content
        AssetInfo myFile1Asset = assetService.getAsset(sbStoreId, path+"/myFolder1/myFolder2/myFile1");
        checkAssetInfo(myFile1Asset, "myFile1", path+"/myFolder1/myFolder2/myFile1", USER_ONE, true, false, false, true, USER_ONE);
        
        // TODO review - copied files are not locked ?
        myFile1Asset = assetService.copyAsset(myFile1Asset, path+"/myFolder1");
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
        assetService.createFile(sbStoreId, path+"/myFolder1/myFolder2", "myFile1", null); // ignore return
        
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
    */
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
        
        // create (empty) file
        assetService.createFile(sbStoreId, path+"/myFolder1/myFolder2", "myFile1", null); // ignore return
        
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
        //checkAssetInfo(myMovedFile1Asset, "myFile1", path+"/myFolder2/myFile1", USER_ONE, true, false, false, true, USER_ONE);
        checkAssetInfo(myMovedFile1Asset, "myFile1", path+"/myFolder2/myFile1", USER_ONE, true, false, false, false, null);
    }
    
    public void testProperties() throws InterruptedException
    {
        // create web project (also creates staging sandbox and admin's author sandbox)
        WebProjectInfo wpInfo = wpService.createWebProject(TEST_WEBPROJ_DNS+"-properties", TEST_WEBPROJ_NAME+"-properties", TEST_WEBPROJ_TITLE, TEST_WEBPROJ_DESCRIPTION, TEST_WEBPROJ_DEFAULT_WEBAPP, TEST_WEBPROJ_DONT_USE_AS_TEMPLATE, null);
        String defaultWebApp = wpInfo.getDefaultWebApp();
        
        // get staging sandbox id
        String stagingStoreId = sbService.getStagingSandbox(wpInfo.getStoreId()).getSandboxId();
        
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
        
        pollForSnapshotCount(stagingStoreId, 1);
        
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
    
    public void testAspects()
    {
        // create web project (also creates staging sandbox and admin's author sandbox)
        WebProjectInfo wpInfo = wpService.createWebProject(TEST_WEBPROJ_DNS+"-aspects", TEST_WEBPROJ_NAME+"-aspects", TEST_WEBPROJ_TITLE, TEST_WEBPROJ_DESCRIPTION, TEST_WEBPROJ_DEFAULT_WEBAPP, TEST_WEBPROJ_DONT_USE_AS_TEMPLATE, null);
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
        
        int folderAspectCnt = assetService.getAspects(myFolder1Asset).size();
        
        // create file
        assetService.createFileWebApp(sbStoreId, defaultWebApp, "/myFolder1", "myFile1");
        AssetInfo myFile1Asset = assetService.getAssetWebApp(sbStoreId, defaultWebApp, "/myFolder1/myFile1");
        checkAssetInfo(myFile1Asset, "myFile1", path+"/myFolder1/myFile1", USER_ONE, true, false, false, true, USER_ONE);
        
        int fileAspectCnt = assetService.getAspects(myFile1Asset).size();
        
        // add/remove aspect to/from folder
        
        assertFalse(assetService.hasAspect(myFolder1Asset, ContentModel.ASPECT_TITLED));
        
        Map<QName, Serializable> newProps = new HashMap<QName, Serializable>(2);
        newProps.put(ContentModel.PROP_TITLE, "folder title");
        newProps.put(ContentModel.PROP_DESCRIPTION, "folder description");
        
        assetService.addAspect(myFolder1Asset, ContentModel.ASPECT_TITLED, newProps);
        
        assertEquals(folderAspectCnt+1, assetService.getAspects(myFolder1Asset).size());
        assertTrue(assetService.hasAspect(myFolder1Asset, ContentModel.ASPECT_TITLED));
        
        assetService.removeAspect(myFolder1Asset, ContentModel.ASPECT_TITLED);
        
        assertEquals(folderAspectCnt, assetService.getAspects(myFolder1Asset).size());
        assertFalse(assetService.hasAspect(myFolder1Asset, ContentModel.ASPECT_TITLED));
        
        // add/remove aspect to/from file
        
        assertFalse(assetService.hasAspect(myFile1Asset, ContentModel.ASPECT_TITLED));
        
        newProps = new HashMap<QName, Serializable>(2);
        newProps.put(ContentModel.PROP_TITLE, "file title");
        newProps.put(ContentModel.PROP_DESCRIPTION, "file description");
        
        assetService.addAspect(myFile1Asset, ContentModel.ASPECT_TITLED, newProps);
        
        assertEquals(fileAspectCnt+1, assetService.getAspects(myFile1Asset).size());
        assertTrue(assetService.hasAspect(myFile1Asset, ContentModel.ASPECT_TITLED));
        
        assetService.removeAspect(myFile1Asset, ContentModel.ASPECT_TITLED);
        
        assertEquals(fileAspectCnt, assetService.getAspects(myFile1Asset).size());
        assertFalse(assetService.hasAspect(myFile1Asset, ContentModel.ASPECT_TITLED));        
    }
    
    
    public void testSimpleLockFile() throws InterruptedException
    {
        // create web project (also creates staging sandbox and admin's author sandbox)
        WebProjectInfo wpInfo = wpService.createWebProject(TEST_WEBPROJ_DNS+"-simpleLock", TEST_WEBPROJ_NAME+"-simpleLock", TEST_WEBPROJ_TITLE, TEST_WEBPROJ_DESCRIPTION, TEST_WEBPROJ_DEFAULT_WEBAPP, TEST_WEBPROJ_DONT_USE_AS_TEMPLATE, null);
        String defaultWebApp = wpInfo.getDefaultWebApp();
        
        // get staging sandbox id
        String stagingStoreId = sbService.getStagingSandbox(wpInfo.getStoreId()).getSandboxId();
        
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
        
        pollForSnapshotCount(stagingStoreId, 1);
        
        assertNull(assetService.getLockOwner(myFile1Asset));
        assertTrue(assetService.hasLockAccess(myFile1Asset));
        
        // switch to user two
        AuthenticationUtil.setFullyAuthenticatedUser(USER_TWO);
        
        assertNull(assetService.getLockOwner(myFile1Asset));
        assertTrue(assetService.hasLockAccess(myFile1Asset));
    }
    
    public void testSimpleLockFile2() throws InterruptedException
    {
        AuthenticationUtil.setFullyAuthenticatedUser(USER_ADMIN);
        
        // create web project (also creates staging sandbox and admin's author sandbox)
        WebProjectInfo wpInfo = wpService.createWebProject(TEST_WEBPROJ_DNS+"-partialSubmitWithNewFolder", TEST_WEBPROJ_NAME+"-partialSubmitWithNewFolder", TEST_WEBPROJ_TITLE, TEST_WEBPROJ_DESCRIPTION, TEST_WEBPROJ_DEFAULT_WEBAPP, TEST_WEBPROJ_DONT_USE_AS_TEMPLATE, null);
        String defaultWebApp = wpInfo.getDefaultWebApp();
        
        // get staging sandbox id
        String stagingStoreId = sbService.getStagingSandbox(wpInfo.getStoreId()).getSandboxId();
        
        // get admin's sandbox
        SandboxInfo sbInfo = sbService.getAuthorSandbox(wpInfo.getStoreId());
        String sbStoreId = sbInfo.getSandboxId();
        
        String path = sbInfo.getSandboxRootPath() + "/" + defaultWebApp;
        
        // create folder
        assetService.createFolderWebApp(sbStoreId, defaultWebApp, "/", "myDir1");
        AssetInfo myDir1Asset = assetService.getAssetWebApp(sbStoreId, defaultWebApp, "myDir1");
        checkAssetInfo(myDir1Asset, "myDir1", path+"/myDir1", USER_ADMIN, false, true, false, false, null);
        
        // note: folders do not get locked
        assertNull(assetService.getLockOwner(myDir1Asset));
        assertTrue(assetService.hasLockAccess(myDir1Asset));
        
        // create two files
        assetService.createFileWebApp(sbStoreId, defaultWebApp, "/myDir1", "myFile1");
        assetService.createFileWebApp(sbStoreId, defaultWebApp, "/myDir1", "myFile2");
        
        AssetInfo myFile1Asset = assetService.getAssetWebApp(sbStoreId, defaultWebApp, "myDir1/myFile1");
        checkAssetInfo(myFile1Asset, "myFile1", path+"/myDir1/myFile1", USER_ADMIN, true, false, false, true, USER_ADMIN);
        
        assertEquals(USER_ADMIN, assetService.getLockOwner(myFile1Asset));
        assertTrue(assetService.hasLockAccess(myFile1Asset));
        
        AssetInfo myFile2Asset = assetService.getAssetWebApp(sbStoreId, defaultWebApp, "myDir1/myFile2");
        checkAssetInfo(myFile2Asset, "myFile2", path+"/myDir1/myFile2", USER_ADMIN, true, false, false, true, USER_ADMIN);
        
        assertEquals(USER_ADMIN, assetService.getLockOwner(myFile2Asset));
        assertTrue(assetService.hasLockAccess(myFile2Asset));
        
        List<AssetInfo> changedAssets = sbService.listChangedWebApp(sbStoreId, defaultWebApp, false);
        assertEquals(1, changedAssets.size());
        myDir1Asset = changedAssets.get(0);
        checkAssetInfo(myDir1Asset, "myDir1", path+"/myDir1", USER_ADMIN, false, true, false, false, null);
        
        List<AssetInfo> selectedAssetsToSubmit = new ArrayList<AssetInfo>(1);
        selectedAssetsToSubmit.add(myFile1Asset);
        
        // partial submit with new folder
        sbService.submitListAssets(sbStoreId, selectedAssetsToSubmit, "submit1 label", "submit1 comment");
        
        pollForSnapshotCount(stagingStoreId, 1);
        
        changedAssets = sbService.listChangedWebApp(sbStoreId, defaultWebApp, false);
        assertEquals(1, changedAssets.size());
        myFile2Asset = changedAssets.get(0);
        
        // ETWOTWO-1265
        checkAssetInfo(myFile2Asset, "myFile2", path+"/myDir1/myFile2", USER_ADMIN, true, false, false, true, USER_ADMIN);
        assertEquals(USER_ADMIN, assetService.getLockOwner(myFile2Asset));
        assertTrue(assetService.hasLockAccess(myFile2Asset));
    }
    
    // bulk import and submit all
    public void testImportAndSubmit1() throws InterruptedException
    {
        // create web project (also creates staging sandbox and admin's author sandbox)
        WebProjectInfo wpInfo = wpService.createWebProject(TEST_WEBPROJ_DNS+"-simpleImport", TEST_WEBPROJ_NAME+"-simpleImport", TEST_WEBPROJ_TITLE, TEST_WEBPROJ_DESCRIPTION, TEST_WEBPROJ_DEFAULT_WEBAPP, TEST_WEBPROJ_DONT_USE_AS_TEMPLATE, null);
        String defaultWebApp = wpInfo.getDefaultWebApp();
        
        // get staging sandbox
        SandboxInfo stagingInfo = sbService.getStagingSandbox(wpInfo.getStoreId());
        String stagingStoreId = stagingInfo.getSandboxId();
        
        // invite web user and auto-create their (author) sandbox
        wpService.inviteWebUser(wpInfo.getStoreId(), USER_ONE, WCMUtil.ROLE_CONTENT_CONTRIBUTOR, true);
        
        assertEquals(0, sbService.listSnapshots(stagingStoreId, false).size());
        
        // switch to user
        AuthenticationUtil.setFullyAuthenticatedUser(USER_ONE);
        
        // get user's author sandbox
        SandboxInfo sbInfo = sbService.getAuthorSandbox(wpInfo.getStoreId());
        String sbStoreId = sbInfo.getSandboxId();
        
        String path = sbInfo.getSandboxRootPath() + "/" + defaultWebApp;
        
        assertEquals(0, assetService.listAssets(stagingStoreId, path, false).size());
        assertEquals(0, assetService.listAssets(sbStoreId, path, false).size());
        
        assertEquals(0, sbService.listChanged(sbStoreId, path, false).size());
        
        // create folder
        assetService.createFolder(sbStoreId, path, "myFolder1", null);
        AssetInfo myFolder1Asset = assetService.getAsset(sbStoreId, path+"/myFolder1");
        
        assertEquals(1, sbService.listChanged(sbStoreId, path, false).size());
        
        assertEquals(0, assetService.listAssets(stagingStoreId, path, false).size());
        assertEquals(1, assetService.listAssets(sbStoreId, path, false).size());
        assertEquals(0, assetService.listAssets(sbStoreId, path+"/myFolder1", false).size());
        
        // bulk import
        String testFile = System.getProperty("user.dir") + "/source/test-resources/module/test.war";
        
        File zipFile = new File(testFile);
        assetService.bulkImport(sbStoreId, myFolder1Asset.getPath(), zipFile, false);
        
        assertEquals(0, assetService.listAssets(stagingStoreId, path, false).size());
        assertEquals(1, assetService.listAssets(sbStoreId, path, false).size());
        assertEquals(9, assetService.listAssets(sbStoreId, path+"/myFolder1", false).size());
        
        assertEquals(1, sbService.listChanged(sbStoreId, path, false).size());
        
        sbService.submitWebApp(sbStoreId, defaultWebApp, "s1", "s1");
        
        pollForSnapshotCount(stagingStoreId, 1);
        
        assertEquals(1, assetService.listAssets(stagingStoreId, path, false).size());
        assertEquals(9, assetService.listAssets(stagingStoreId, path+"/myFolder1", false).size());
        assertEquals(1, assetService.listAssets(sbStoreId, path, false).size());
        assertEquals(9, assetService.listAssets(sbStoreId, path+"/myFolder1", false).size());
        
        assertEquals(0, sbService.listChanged(sbStoreId, path, false).size());
        
        // switch to admin
        AuthenticationUtil.setFullyAuthenticatedUser(AuthenticationUtil.getAdminUserName());
        
        assertEquals(1, sbService.listSnapshots(stagingStoreId, false).size());
    }
    
    // bulk import and submit 1-by-1
    public void testImportAndSubmit2() throws InterruptedException
    {
        long start = System.currentTimeMillis();
        
        // create web project (also creates staging sandbox and admin's author sandbox)
        WebProjectInfo wpInfo = wpService.createWebProject(TEST_WEBPROJ_DNS+"-import", TEST_WEBPROJ_NAME+"-import", TEST_WEBPROJ_TITLE, TEST_WEBPROJ_DESCRIPTION, TEST_WEBPROJ_DEFAULT_WEBAPP, TEST_WEBPROJ_DONT_USE_AS_TEMPLATE, null);
        
        logger.debug("create web project in "+(System.currentTimeMillis()-start)+" msecs");
        
        String defaultWebApp = wpInfo.getDefaultWebApp();
        
        // get staging sandbox
        SandboxInfo stagingInfo = sbService.getStagingSandbox(wpInfo.getStoreId());
        String stagingStoreId = stagingInfo.getSandboxId();
        
        // get admin user's author sandbox
        SandboxInfo sbInfo = sbService.getAuthorSandbox(wpInfo.getStoreId());
        String sbStoreId = sbInfo.getSandboxId();
        
        String path = sbInfo.getSandboxRootPath() + "/" + defaultWebApp;
        
        assertEquals(0, assetService.listAssets(stagingStoreId, path, false).size());
        assertEquals(0, assetService.listAssets(sbStoreId, path, false).size());
        
        assertEquals(0, sbService.listSnapshots(stagingStoreId, false).size());
        
        // bulk import
        //String testFile = System.getProperty("user.dir") + "/source/test-resources/wcm/1001_files.zip";
        String testFile = System.getProperty("user.dir") + "/source/test-resources/module/test.war";
        
        start = System.currentTimeMillis();
        
        File zipFile = new File(testFile);
        assetService.bulkImport(sbStoreId, path, zipFile, false);
        
        logger.debug("bulk import in "+(System.currentTimeMillis()-start)+" msecs");
        
        int totalCnt = assetService.listAssets(sbStoreId, path, false).size();
        int expectedChangeCnt = totalCnt;
        int expectedSnapCnt = 0;
        int expectedStageCnt = 0;
        
        for (int i = 1; i <= totalCnt; i++)
        {
            assertEquals(expectedStageCnt, assetService.listAssets(stagingStoreId, path, false).size());
            assertEquals(totalCnt, assetService.listAssets(sbStoreId, path, false).size());
            
            assertEquals(expectedSnapCnt, sbService.listSnapshots(stagingStoreId, false).size());
            
            List<AssetInfo> assets = sbService.listChanged(sbStoreId, path, false);
            assertEquals(expectedChangeCnt, assets.size());
            
            List<AssetInfo> submitAssets = new ArrayList<AssetInfo>(1);
            submitAssets.add(assets.get(0));
            
            start = System.currentTimeMillis();
            
            sbService.submitListAssets(sbStoreId, submitAssets, "s1", "s1");
            
            logger.debug("initiated submit of item "+i+" in "+(System.currentTimeMillis()-start)+" msecs");
            
            start = System.currentTimeMillis();
            
            expectedSnapCnt++;
            
            pollForSnapshotCount(stagingStoreId, expectedSnapCnt);
            
            expectedChangeCnt--;
            expectedStageCnt++;
        }
    }
    
    // ALF-1948
    public void testDeleteFile() throws Exception
    {
        WebProjectInfo wpInfo = wpService.createWebProject(TEST_WEBPROJ_DNS + "-import", TEST_WEBPROJ_NAME + "-import", TEST_WEBPROJ_TITLE, TEST_WEBPROJ_DESCRIPTION,
                TEST_WEBPROJ_DEFAULT_WEBAPP, TEST_WEBPROJ_DONT_USE_AS_TEMPLATE, null);
        
        String defaultWebApp = wpInfo.getDefaultWebApp();
        
        SandboxInfo stagingInfo = sbService.getStagingSandbox(wpInfo.getStoreId());
        String stagingStoreId = stagingInfo.getSandboxId();
        
        SandboxInfo sbInfo = sbService.getAuthorSandbox(wpInfo.getStoreId());
        String sbStoreId = sbInfo.getSandboxId();
        
        String path = sbInfo.getSandboxRootPath() + "/" + defaultWebApp;
        
        assetService.createFile(sbStoreId, path, "testfile.txt", null);
        assetService.createFolder(sbStoreId, path, "testfolder", null);
        
        List<AssetInfo> listAssets = assetService.listAssets(stagingStoreId, path, true);
        assertEquals(0, listAssets.size());
        
        listAssets = assetService.listAssets(sbStoreId, path, true);
        assertEquals(2, listAssets.size());
        
        int validAmount = listAssets.size() - 1;
        for (AssetInfo asset : listAssets)
        {
            assetService.deleteAsset(asset);
            List<AssetInfo> tempAssetsList = assetService.listAssets(sbStoreId, path, true);
            assertNotNull(tempAssetsList);
            assertEquals(validAmount--, tempAssetsList.size());
        }
        
        List<AssetInfo> listChanged = sbService.listChanged(sbStoreId, path, true);
        assertEquals(0, listChanged.size());
    }
}
