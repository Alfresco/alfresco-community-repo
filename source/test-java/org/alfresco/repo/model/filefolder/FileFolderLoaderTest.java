/*
 * Copyright (C) 2005-2015 Alfresco Software Limited.
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
package org.alfresco.repo.model.filefolder;

import java.util.List;
import java.util.Locale;

import junit.framework.TestCase;
import net.sf.acegisecurity.AuthenticationCredentialsNotFoundException;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.content.MimetypeMap;
import org.alfresco.repo.node.MLPropertyInterceptor;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.security.authentication.AuthenticationUtil.RunAsWork;
import org.alfresco.repo.security.permissions.AccessDeniedException;
import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;
import org.alfresco.service.cmr.model.FileFolderService;
import org.alfresco.service.cmr.model.FileInfo;
import org.alfresco.service.cmr.model.FileNotFoundException;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.service.cmr.repository.MLText;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.datatype.DefaultTypeConverter;
import org.alfresco.service.cmr.security.PermissionService;
import org.alfresco.service.transaction.TransactionService;
import org.alfresco.test_category.OwnJVMTestsCategory;
import org.alfresco.util.ApplicationContextHelper;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.springframework.context.ApplicationContext;

import static org.junit.Assert.assertNotEquals;

/**
 * @see org.alfresco.repo.model.filefolder.FileFolderLoader
 * @author Derek Hulley
 * @since 5.1
 */
@Category(OwnJVMTestsCategory.class)
public class FileFolderLoaderTest extends TestCase
{
    private static final ApplicationContext ctx = ApplicationContextHelper.getApplicationContext();

    private FileFolderLoader fileFolderLoader;
    private FileFolderService fileFolderService;
    private PermissionService permissionService;
    private TransactionService transactionService;
    private NodeService nodeService;
    private String sharedHomePath;
    private NodeRef hiddenFolderNodeRef;
    private String hiddenFolderPath;
    private NodeRef readOnlyFolderNodeRef;
    private String readOnlyFolderPath;
    private NodeRef writeFolderNodeRef;
    private String writeFolderPath;
    
    @Override
    public void setUp() throws Exception
    {
        // Make sure we don't get leaked threads from other tests
        AuthenticationUtil.clearCurrentSecurityContext();
        AuthenticationUtil.pushAuthentication();

        RunAsWork<Void> setUpWork = new RunAsWork<Void>()
        {
            @Override
            public Void doWork() throws Exception
            {
                fileFolderLoader = (FileFolderLoader) ctx.getBean("FileFolderLoader");
                fileFolderService = (FileFolderService) ctx.getBean("FileFolderService");
                permissionService = (PermissionService) ctx.getBean("PermissionService");
                transactionService = (TransactionService) ctx.getBean("TransactionService");
                nodeService = (NodeService) ctx.getBean("nodeService");
                NodeRef companyHomeNodeRef = fileFolderLoader.getRepository().getCompanyHome();
                NodeRef sharedHomeNodeRef = fileFolderLoader.getRepository().getSharedHome();
                List<FileInfo> sharedHomeFileInfos = fileFolderService.getNamePath(companyHomeNodeRef, sharedHomeNodeRef);
                sharedHomePath = "/" + sharedHomeFileInfos.get(0).getName();
                
                // Create a folder that will be invisible to all normal users
                FileInfo hiddenFolderInfo = fileFolderService.create(sharedHomeNodeRef, "HideThis", ContentModel.TYPE_FOLDER);
                hiddenFolderNodeRef = hiddenFolderInfo.getNodeRef();
                hiddenFolderPath = sharedHomePath + "/HideThis";
                permissionService.setInheritParentPermissions(hiddenFolderNodeRef, false);
                
                // Create a folder that will be read-only
                FileInfo readOnlyFolderInfo = fileFolderService.create(sharedHomeNodeRef, "ReadOnlyThis", ContentModel.TYPE_FOLDER);
                readOnlyFolderNodeRef = readOnlyFolderInfo.getNodeRef();
                readOnlyFolderPath = sharedHomePath + "/ReadOnlyThis";
                permissionService.setInheritParentPermissions(readOnlyFolderNodeRef, false);
                permissionService.setPermission(readOnlyFolderNodeRef, PermissionService.ALL_AUTHORITIES, PermissionService.READ, true);
                
                // Create a folder to write to
                FileInfo writeFolderInfo = fileFolderService.create(sharedHomeNodeRef, "WriteThis", ContentModel.TYPE_FOLDER);
                writeFolderNodeRef = writeFolderInfo.getNodeRef();
                writeFolderPath = sharedHomePath + "/WriteThis";
                
                // Done
                return null;
            }
        };
        AuthenticationUtil.runAsSystem(setUpWork);
    }
    
    @Override
    public void tearDown() throws Exception
    {
        RunAsWork<Void> tearDownWork = new RunAsWork<Void>()
        {
            @Override
            public Void doWork() throws Exception
            {
                fileFolderService.delete(hiddenFolderNodeRef);
                fileFolderService.delete(readOnlyFolderNodeRef);
                fileFolderService.delete(writeFolderNodeRef);
                // Done
                return null;
            }
        };
        AuthenticationUtil.runAsSystem(tearDownWork);

        AuthenticationUtil.popAuthentication();
    }
    
    @Test
    public void testBasic()
    {
        assertNotNull(fileFolderLoader);
        assertNotNull(sharedHomePath);
    }
    
    @Test
    public void testIllegalArgs_MinMax() throws Exception
    {
        try
        {
            fileFolderLoader.createFiles(
                    sharedHomePath,
                    1, 256, 100L, 10L, Long.MAX_VALUE, false,
                    10, 256);
            fail("Should detect min/max size issue.");
        }
        catch (IllegalArgumentException e)
        {
            // Expected
        }
    }
    
    @Test
    public void testIllegalArgs_DescriptionCount() throws Exception
    {
        try
        {
            fileFolderLoader.createFiles(
                    sharedHomePath,
                    1, 256, 1024L, 10L, Long.MAX_VALUE, false,
                    Integer.MAX_VALUE, 256);
            fail("Should detect description count issue.");
        }
        catch (IllegalArgumentException e)
        {
            // Expected
        }
    }
    
    @Test
    public void testIllegalArgs_DescriptionSize() throws Exception
    {
        try
        {
            fileFolderLoader.createFiles(
                    sharedHomePath,
                    1, 256, 1024L, 10L, Long.MAX_VALUE, false,
                    10, Long.MAX_VALUE);
            fail("Should detect description size issue.");
        }
        catch (IllegalArgumentException e)
        {
            // Expected
        }
    }
    
    @Test
    public void testNoPermissionsAtAll() throws Exception
    {
        try
        {
            fileFolderLoader.createFiles(
                    sharedHomePath,
                    0, 256, 1024L, 1024L, Long.MAX_VALUE, false,
                    10, 256L);
            fail("No permissions to see folder.");
        }
        catch (AuthenticationCredentialsNotFoundException e)
        {
            // Expected
        }
    }
    
    @Test
    public void testNoPermissionsToFindFolder() throws Exception
    {
        try
        {
            AuthenticationUtil.pushAuthentication();
            AuthenticationUtil.setFullyAuthenticatedUser("BOB-1");
            fileFolderLoader.createFiles(
                    hiddenFolderPath,
                    0, 256, 1024L, 1024L, Long.MAX_VALUE, false,
                    10, 256L);
            fail("No permissions to see folder.");
        }
        catch (AccessDeniedException e)
        {
            // Expected
        }
        finally
        {
            AuthenticationUtil.popAuthentication();
        }
    }
    
    @Test
    public void testFolderMissing() throws Exception
    {
        try
        {
            AuthenticationUtil.pushAuthentication();
            AuthenticationUtil.setAdminUserAsFullyAuthenticatedUser();
            fileFolderLoader.createFiles(
                    sharedHomePath + "/Missing",
                    0, 256, 1024L, 1024L, Long.MAX_VALUE, false,
                    10, 256L);
            fail("Folder does not exist");
        }
        catch (AlfrescoRuntimeException e)
        {
            // Expected
            assertTrue(e.getCause() instanceof FileNotFoundException);
        }
        finally
        {
            AuthenticationUtil.popAuthentication();
        }
    }
    
    @Test
    public void testNoPermissionsToWriteToFolder() throws Exception
    {
        try
        {
            AuthenticationUtil.pushAuthentication();
            AuthenticationUtil.setFullyAuthenticatedUser("BOB-1");
            fileFolderLoader.createFiles(
                    readOnlyFolderPath,
                    1, 256, 1024L, 1024L, Long.MAX_VALUE, false,
                    10, 256L);
            fail("Folder is read only.  Should not be able to write to it.");
        }
        catch (AccessDeniedException e)
        {
            // Expected
        }
        finally
        {
            AuthenticationUtil.popAuthentication();
        }
    }
    
    /**
     * Zero files
     */
    @Test
    public void testLoad_ZeroFiles() throws Exception
    {
        try
        {
            AuthenticationUtil.pushAuthentication();
            AuthenticationUtil.setAdminUserAsFullyAuthenticatedUser();
            int created = fileFolderLoader.createFiles(
                    writeFolderPath,
                    0, 256, 1024L, 1024L, Long.MAX_VALUE, false,
                    10, 256L);
            assertEquals("Incorrect number of files generated.", 0, created);
            // Count
            assertEquals(0, nodeService.countChildAssocs(writeFolderNodeRef, true));
        }
        finally
        {
            AuthenticationUtil.popAuthentication();
        }
    }
    
    /**
     * One file
     */
    @Test
    public void testLoad_OneFile() throws Exception
    {
        try
        {
            AuthenticationUtil.pushAuthentication();
            AuthenticationUtil.setAdminUserAsFullyAuthenticatedUser();
            int created = fileFolderLoader.createFiles(
                    writeFolderPath,
                    1, 256, 1024L, 1024L, Long.MAX_VALUE, false,
                    10, 256L);
            assertEquals("Incorrect number of files generated.", 1, created);
            // Check the descriptions
            RetryingTransactionCallback<Void> checkCallback = new RetryingTransactionCallback<Void>()
            {
                @Override
                public Void execute() throws Throwable
                {
                    MLPropertyInterceptor.setMLAware(true);
                    List<ChildAssociationRef> childAssocs = nodeService.getChildAssocs(writeFolderNodeRef);
                    // Count
                    assertEquals(1, childAssocs.size());
                    NodeRef fileNodeRef = childAssocs.get(0).getChildRef();
                    MLText descriptions = (MLText) nodeService.getProperty(fileNodeRef, ContentModel.PROP_DESCRIPTION);
                    assertNotNull("No descriptions added", descriptions);
                    assertEquals("Incorrect number of unique descriptions added: ", 10, descriptions.size());
                    assertTrue("Expect the default language to be present. ",
                            descriptions.containsKey(new Locale(Locale.getDefault().getLanguage())));
                    return null;
                }
            };
            transactionService.getRetryingTransactionHelper().doInTransaction(checkCallback, true);
        }
        finally
        {
            AuthenticationUtil.popAuthentication();
        }
    }
    
    /**
     * 100 files; 10 per txn
     */
    @Test
    public void testLoad_02() throws Exception
    {
        try
        {
            AuthenticationUtil.pushAuthentication();
            AuthenticationUtil.setAdminUserAsFullyAuthenticatedUser();
            int created = fileFolderLoader.createFiles(
                    writeFolderPath,
                    100, 10, 1024L, 1024L, Long.MAX_VALUE, false,
                    10, 256L);
            assertEquals("Incorrect number of files generated.", 100, created);
            // Count
            assertEquals(100, nodeService.countChildAssocs(writeFolderNodeRef, true));
        }
        finally
        {
            AuthenticationUtil.popAuthentication();
        }
    }
    
    /**
     * 15 files; 10 per txn; spoofed; different
     */
    @Test
    public void testLoad_03() throws Exception
    {
        try
        {
            AuthenticationUtil.pushAuthentication();
            AuthenticationUtil.setAdminUserAsFullyAuthenticatedUser();
            int created = fileFolderLoader.createFiles(
                    writeFolderPath,
                    15, 10, 1024L, 1024L, Long.MAX_VALUE, false,
                    10, 256L);
            assertEquals("Incorrect number of files generated.", 15, created);
            // Count
            assertEquals(15, nodeService.countChildAssocs(writeFolderNodeRef, true));
            // Check the files
            List<FileInfo> fileInfos = fileFolderService.listFiles(writeFolderNodeRef);
            String lastText = null;
            String lastDescr = null;
            String lastUrl = null;
            for (FileInfo fileInfo : fileInfos)
            {
                NodeRef fileNodeRef = fileInfo.getNodeRef();
                // The URLs must all be unique as we wrote the physical binaries
                ContentReader reader = fileFolderService.getReader(fileNodeRef);
                assertEquals("UTF-8", reader.getEncoding());
                assertEquals(MimetypeMap.MIMETYPE_TEXT_PLAIN, reader.getMimetype());
                assertEquals(1024L, reader.getSize());
                if (lastUrl == null)
                {
                    lastUrl = reader.getContentUrl();
                }
                else
                {
                    assertNotEquals("We expect different URLs: ", lastUrl, reader.getContentUrl());
                    lastUrl = reader.getContentUrl();
                }
                // Check content
                if (lastText == null)
                {
                    lastText = reader.getContentString();
                }
                else
                {
                    String currentStr = reader.getContentString();
                    assertNotEquals("All text must differ due to varying seed. ", lastText, currentStr);
                    lastText = currentStr;
                }
                // Check description
                if (lastDescr == null)
                {
                    lastDescr = DefaultTypeConverter.INSTANCE.convert(String.class, nodeService.getProperty(fileNodeRef, ContentModel.PROP_DESCRIPTION));
                    assertEquals("cm:description length is incorrect. ", 256, lastDescr.getBytes().length);
                }
                else
                {
                    String currentDescr = DefaultTypeConverter.INSTANCE.convert(String.class, nodeService.getProperty(fileNodeRef, ContentModel.PROP_DESCRIPTION));
                    assertNotEquals("All descriptions must differ due to varying seed. ", lastDescr, currentDescr);
                    lastDescr = currentDescr;
                }
            }
        }
        finally
        {
            AuthenticationUtil.popAuthentication();
        }
    }
    
    /**
     * 10 files; 10 per txn; force storage; identical
     */
    @Test
    public void testLoad_04() throws Exception
    {
        try
        {
            AuthenticationUtil.pushAuthentication();
            AuthenticationUtil.setAdminUserAsFullyAuthenticatedUser();
            int created = fileFolderLoader.createFiles(
                    writeFolderPath,
                    10, 10, 1024L, 1024L, 1L, true,
                    10, 256L);
            assertEquals("Incorrect number of files generated.", 10, created);
            // Count
            assertEquals(10, nodeService.countChildAssocs(writeFolderNodeRef, true));
            // Check the files
            List<FileInfo> fileInfos = fileFolderService.listFiles(writeFolderNodeRef);
            String lastText = null;
            String lastDescr = null;
            String lastUrl = null;
            for (FileInfo fileInfo : fileInfos)
            {
                NodeRef fileNodeRef = fileInfo.getNodeRef();
                // The URLs must all be unique as we wrote the physical binaries
                ContentReader reader = fileFolderService.getReader(fileNodeRef);
                assertEquals("UTF-8", reader.getEncoding());
                assertEquals(MimetypeMap.MIMETYPE_TEXT_PLAIN, reader.getMimetype());
                assertEquals(1024L, reader.getSize());
                if (lastUrl == null)
                {
                    lastUrl = reader.getContentUrl();
                }
                else
                {
                    assertNotEquals("We expect unique URLs: ", lastUrl, reader.getContentUrl());
                    lastUrl = reader.getContentUrl();
                }
                // Check content
                if (lastText == null)
                {
                    lastText = reader.getContentString();
                }
                else
                {
                    String currentStr = reader.getContentString();
                    assertEquals("All text must be identical due to same seed. ", lastText, currentStr);
                    lastText = currentStr;
                }
                // Check description
                if (lastDescr == null)
                {
                    lastDescr = DefaultTypeConverter.INSTANCE.convert(String.class, nodeService.getProperty(fileNodeRef, ContentModel.PROP_DESCRIPTION));
                    assertEquals("cm:description length is incorrect. ", 256, lastDescr.getBytes().length);
                }
                else
                {
                    String currentDescr = DefaultTypeConverter.INSTANCE.convert(String.class, nodeService.getProperty(fileNodeRef, ContentModel.PROP_DESCRIPTION));
                    assertEquals("All descriptions must be identical due to varying seed. ", lastDescr, currentDescr);
                    lastDescr = currentDescr;
                }
            }
        }
        finally
        {
            AuthenticationUtil.popAuthentication();
        }
    }
}
