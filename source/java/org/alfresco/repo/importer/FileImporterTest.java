/*
 * Copyright (C) 2005 Alfresco, Inc.
 *
 * Licensed under the Mozilla Public License version 1.1 
 * with a permitted attribution clause. You may obtain a
 * copy of the License at
 *
 *   http://www.alfresco.org/legal/license.txt
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific
 * language governing permissions and limitations under the
 * License.
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
import org.alfresco.model.ContentModel;
import org.alfresco.repo.content.transform.AbstractContentTransformerTest;
import org.alfresco.repo.security.authentication.AuthenticationComponent;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.repository.ContentService;
import org.alfresco.service.cmr.repository.MimetypeService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.search.ResultSet;
import org.alfresco.service.cmr.search.SearchParameters;
import org.alfresco.service.cmr.search.SearchService;
import org.alfresco.service.cmr.security.AuthenticationService;
import org.alfresco.service.cmr.security.PermissionService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.transaction.TransactionService;
import org.alfresco.util.ApplicationContextHelper;
import org.alfresco.util.TempFileProvider;
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

        authenticationComponent.setSystemUserAsCurrentUser();
        StoreRef storeRef = nodeService.createStore(StoreRef.PROTOCOL_WORKSPACE, "Test_" + System.currentTimeMillis());
        rootNodeRef = nodeService.getRootNode(storeRef);
    }

    private FileImporter createFileImporter()
    {
        FileImporterImpl fileImporter = new FileImporterImpl();
        fileImporter.setAuthenticationService(authenticationService);
        fileImporter.setContentService(contentService);
        fileImporter.setMimetypeService(mimetypeService);
        fileImporter.setNodeService(nodeService);
        fileImporter.setDictionaryService(dictionaryService);
        return fileImporter;
    }

    public void testCreateFile() throws Exception
    {
        FileImporter fileImporter = createFileImporter();
        File file = AbstractContentTransformerTest.loadQuickTestFile("xml");
        fileImporter.loadFile(rootNodeRef, file);
    }

    public void testLoadRootNonRecursive1()
    {
        FileImporter fileImporter = createFileImporter();
        URL url = this.getClass().getClassLoader().getResource("");
        File rootFile = new File(url.getFile());
        int count = fileImporter.loadFile(rootNodeRef, rootFile);
        assertEquals("Expected to load a single file", 1, count);
    }

    public void testLoadRootNonRecursive2()
    {
        FileImporter fileImporter = createFileImporter();
        URL url = this.getClass().getClassLoader().getResource("");
        File root = new File(url.getFile());
        int count = fileImporter.loadFile(rootNodeRef, root, null, false);
        assertEquals("Expected to load a single file", 1, count);
    }

    public void testLoadXMLFiles()
    {
        FileImporter fileImporter = createFileImporter();
        URL url = this.getClass().getClassLoader().getResource("");
        FileFilter filter = new XMLFileFilter();
        fileImporter.loadFile(rootNodeRef, new File(url.getFile()), filter, true);
    }

    public void testLoadSourceTestResources()
    {
        FileImporter fileImporter = createFileImporter();
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
        String userName = args.length > 5 ? args[5] : null;
        String userPwd = args.length > 6 ? args[6] : "";
        while (count < target)
        {
            File directory = TempFileProvider.getTempDir();
            File[] files = directory.listFiles();
            System.out.println("Start temp file count = " + files.length);
            
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
                    tx.commit();
                    tx = transactionService.getUserTransaction();
                    tx.begin();
                }

                long start = System.nanoTime();
                int importCount = test.createFileImporter().loadNamedFile(
                        importLocation,
                        sourceFile,
                        true,
                        String.format("%s-%05d-%s", baseName, count, System.currentTimeMillis()));
                grandTotal += importCount;
                long end = System.nanoTime();
                long first = end-start;
                System.out.println("Created in: " + ((end - start) / 1000000.0) + "ms");
                start = System.nanoTime();

                tx.commit();
                end = System.nanoTime();
                long second = end-start;
                System.out.println("Committed in: " + ((end - start) / 1000000.0) + "ms");
                double total = ((first+second)/1000000.0);
                System.out.println("Grand Total: "+ grandTotal);
                System.out.println("Count: "+ count + "ms");
                System.out.println("Imported: " + importCount + " files or directories");
                System.out.println("Average: " + (importCount / (total / 1000.0)) + " files per second");
                
                directory = TempFileProvider.getTempDir();
                files = directory.listFiles();
                System.out.println("End temp file count = " + files.length);
                
                
                tx = transactionService.getUserTransaction(); 
                tx.begin();
                SearchParameters sp = new SearchParameters();
                sp.setLanguage("lucene");
                sp.setQuery("ISNODE:T");
                sp.addStore(spacesStore);
                start = System.nanoTime();
                ResultSet rs = test.searchService.query(sp);
                end = System.nanoTime();
                System.out.println("Find all in: " + ((end - start) / 1000000.0) + "ms");
                System.out.println("     = "+rs.length() +"\n\n");
                rs.close();
                
                sp = new SearchParameters();
                sp.setLanguage("lucene");
                sp.setQuery("TEXT:\"andy\"");
                sp.addStore(spacesStore);
                start = System.nanoTime();
                rs = test.searchService.query(sp);
                end = System.nanoTime();
                System.out.println("Find andy in: " + ((end - start) / 1000000.0) + "ms");
                System.out.println("     = "+rs.length() +"\n\n");
                rs.close();
                
                sp = new SearchParameters();
                sp.setLanguage("lucene");
                sp.setQuery("TYPE:\"" + ContentModel.TYPE_CONTENT.toString()  + "\"");
                sp.addStore(spacesStore);
                start = System.nanoTime();
                rs = test.searchService.query(sp);
                end = System.nanoTime();
                System.out.println("Find content in: " + ((end - start) / 1000000.0) + "ms");
                System.out.println("     = "+rs.length() +"\n\n");
                rs.close();
                
                sp = new SearchParameters();
                sp.setLanguage("lucene");
                sp.setQuery("PATH:\"/*/*/*\"");
                sp.addStore(spacesStore);
                start = System.nanoTime();
                rs = test.searchService.query(sp);
                end = System.nanoTime();
                System.out.println("Find /*/*/* in: " + ((end - start) / 1000000.0) + "ms");
                System.out.println("     = "+rs.length() +"\n\n");
                rs.close();
                
                tx.commit();
                
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
