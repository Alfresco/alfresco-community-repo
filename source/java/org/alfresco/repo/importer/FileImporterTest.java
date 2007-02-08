/*
 * Copyright (C) 2005 Alfresco, Inc.
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
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package org.alfresco.repo.importer;

import java.io.File;
import java.io.FileFilter;
import java.net.URL;
import java.util.List;

import javax.transaction.HeuristicMixedException;
import javax.transaction.HeuristicRollbackException;
import javax.transaction.NotSupportedException;
import javax.transaction.RollbackException;
import javax.transaction.SystemException;
import javax.transaction.UserTransaction;

import junit.framework.TestCase;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.repo.content.transform.AbstractContentTransformerTest;
import org.alfresco.repo.security.authentication.AuthenticationComponent;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.repository.ContentService;
import org.alfresco.service.cmr.repository.MimetypeService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.search.SearchService;
import org.alfresco.service.cmr.security.AuthenticationService;
import org.alfresco.service.cmr.security.PermissionService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.transaction.TransactionService;
import org.alfresco.util.ApplicationContextHelper;
import org.alfresco.util.TestWithUserUtils;
import org.springframework.context.ApplicationContext;

public class FileImporterTest extends TestCase
{
    static ApplicationContext ctx = ApplicationContextHelper.getApplicationContext();
    private NodeService nodeService;
    private SearchService searchService;
    private DictionaryService dictionaryService;
    private ContentService contentService;
    private AuthenticationService authenticationService;
    private AuthenticationComponent authenticationComponent;
    private PermissionService permissionService;
    private MimetypeService mimetypeService;
    private NamespaceService namespaceService;
    private TransactionService transactionService;

    private ServiceRegistry serviceRegistry;
    private NodeRef rootNodeRef;

    public FileImporterTest()
    {
        super();
    }

    public FileImporterTest(String arg0)
    {
        super(arg0);
    }

    public void setUp()
    {
        serviceRegistry = (ServiceRegistry) ctx.getBean(ServiceRegistry.SERVICE_REGISTRY);

        nodeService = serviceRegistry.getNodeService();
        searchService = serviceRegistry.getSearchService();
        dictionaryService = serviceRegistry.getDictionaryService();
        contentService = serviceRegistry.getContentService();
        authenticationService = (AuthenticationService) ctx.getBean("authenticationService");
        authenticationComponent = (AuthenticationComponent) ctx.getBean("authenticationComponent");
        permissionService = serviceRegistry.getPermissionService();
        mimetypeService = serviceRegistry.getMimetypeService();
        namespaceService = serviceRegistry.getNamespaceService();
        transactionService = serviceRegistry.getTransactionService();

        authenticationComponent.setSystemUserAsCurrentUser();
        StoreRef storeRef = nodeService.createStore(StoreRef.PROTOCOL_WORKSPACE, "Test_" + System.currentTimeMillis());
        rootNodeRef = nodeService.getRootNode(storeRef);
    }

    private FileImporter createFileImporter(boolean txnPerFile)
    {
        FileImporterImpl fileImporter = new FileImporterImpl();
        fileImporter.setAuthenticationService(authenticationService);
        fileImporter.setContentService(contentService);
        fileImporter.setMimetypeService(mimetypeService);
        fileImporter.setNodeService(nodeService);
        fileImporter.setDictionaryService(dictionaryService);
        fileImporter.setTransactionService(transactionService);
        fileImporter.setTxnPerFile(txnPerFile);
        return fileImporter;
    }

    public void testCreateFile() throws Exception
    {
        FileImporter fileImporter = createFileImporter(false);
        File file = AbstractContentTransformerTest.loadQuickTestFile("xml");
        fileImporter.loadFile(rootNodeRef, file);
    }

    public void testLoadRootNonRecursive1()
    {
        FileImporter fileImporter = createFileImporter(false);
        URL url = this.getClass().getClassLoader().getResource("");
        File rootFile = new File(url.getFile());
        int count = fileImporter.loadFile(rootNodeRef, rootFile);
        assertEquals("Expected to load a single file", 1, count);
    }

    public void testLoadRootNonRecursive2()
    {
        FileImporter fileImporter = createFileImporter(false);
        URL url = this.getClass().getClassLoader().getResource("");
        File root = new File(url.getFile());
        int count = fileImporter.loadFile(rootNodeRef, root, null, false);
        assertEquals("Expected to load a single file", 1, count);
    }

    public void testLoadXMLFiles()
    {
        FileImporter fileImporter = createFileImporter(false);
        URL url = this.getClass().getClassLoader().getResource("");
        FileFilter filter = new XMLFileFilter();
        fileImporter.loadFile(rootNodeRef, new File(url.getFile()), filter, true);
    }

    public void testLoadSourceTestResources()
    {
        FileImporter fileImporter = createFileImporter(false);
        URL url = this.getClass().getClassLoader().getResource("quick");
        FileFilter filter = new QuickFileFilter();
        fileImporter.loadFile(rootNodeRef, new File(url.getFile()), filter, true);
    }

    private static class XMLFileFilter implements FileFilter
    {
        public boolean accept(File file)
        {
            return file.getName().endsWith(".xml");
        }
    }

    private static class QuickFileFilter implements FileFilter
    {
        public boolean accept(File file)
        {
            return file.getName().startsWith("quick");
        }
    }

    /**
     * @param args
     *            <ol>
     *            <li>StoreRef: The store to load the files into
     *            <li>String: The path within the store into which to load the files (e.g. /app:company_home)
     *            <li>String: Directory to use as source (e.g. c:/temp)
     *            <li>String: New name to give the source.  It may have a suffix added (e.g. upload_xxx)
     *            <li>Integer: Number of times to repeat the load.
     *            <li>Boolean: (optional - default 'false') Create each file/folder in a new transaction
     *            <li>String: (optional) user to authenticate as
     *            <li>String: (optional) password for authentication
     *            </ol>
     * @throws SystemException
     * @throws NotSupportedException
     * @throws HeuristicRollbackException
     * @throws HeuristicMixedException
     * @throws RollbackException
     * @throws IllegalStateException
     * @throws SecurityException
     */
    public static final void main(String[] args) throws Exception
    {
        int grandTotal = 0;
        int count = 0;
        File sourceFile = new File(args[2]);
        String baseName = args[3];
        int target = Integer.parseInt(args[4]);
        Boolean txnPerFile = args.length > 5 ? Boolean.parseBoolean(args[5]) : false;
        String userName = args.length > 6 ? args[6] : null;
        String userPwd = args.length > 7 ? args[7] : "";
        while (count < target)
        {
            count++;
            FileImporterTest test = new FileImporterTest();
            test.setUp();
            
            test.authenticationComponent.setSystemUserAsCurrentUser();
            TransactionService transactionService = test.serviceRegistry.getTransactionService();
            UserTransaction tx = transactionService.getUserTransaction(); 
            tx.begin();

            try
            {
                StoreRef spacesStore = new StoreRef(args[0]);
                if (!test.nodeService.exists(spacesStore))
                {
                    test.nodeService.createStore(spacesStore.getProtocol(), spacesStore.getIdentifier());
                }

                NodeRef storeRoot = test.nodeService.getRootNode(spacesStore);
                List<NodeRef> importLocations = test.searchService.selectNodes(
                        storeRoot,
                        args[1],
                        null,
                        test.namespaceService,
                        false);
                if (importLocations.size() == 0)
                {
                    throw new AlfrescoRuntimeException(
                            "Root node not found, " +
                            args[1] +
                            " not found in store, " +
                            storeRoot);
                }
                NodeRef importLocation = importLocations.get(0);
                
                // optionally authenticate as a specific user
                if (userName != null)
                {
                    // give the user all necessary permissions on the root
                    test.permissionService.setPermission(importLocation, userName, PermissionService.ALL_PERMISSIONS, true);
                    // authenticate as the designated user
                    TestWithUserUtils.authenticateUser(
                            userName,
                            userPwd,
                            test.authenticationService,
                            test.authenticationComponent);
                }
                tx.commit();

                // only begin if we are doing it all in one transaction
                if (!txnPerFile)
                {
                    tx = transactionService.getUserTransaction();
                    tx.begin();
                }
                
                long start = System.nanoTime();
                FileImporter importer = test.createFileImporter(txnPerFile);
                int importCount = importer.loadNamedFile(
                        importLocation,
                        sourceFile,
                        true,
                        String.format("%s-%05d-%s", baseName, count, System.currentTimeMillis()));
                grandTotal += importCount;
                long end = System.nanoTime();
                long first = end-start;
                System.out.println("Created in: " + ((end - start) / 1000000.0) + "ms");
                start = System.nanoTime();

                if (!txnPerFile)
                {
                    tx.commit();
                }
                end = System.nanoTime();
                long second = end-start;
                System.out.println("Committed in: " + ((end - start) / 1000000.0) + "ms");
                double total = ((first+second)/1000000.0);
                System.out.println("Grand Total: "+ grandTotal);
                System.out.println("Imported: " + importCount + " files or directories");
                System.out.println("Average: " + (importCount / (total / 1000.0)) + " files per second");
            }
            catch (Throwable e)
            {
                tx.rollback();
                e.printStackTrace();
            }
        }
        System.exit(0);
    }
}
