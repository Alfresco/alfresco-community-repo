package org.alfresco.repo.search.impl.lucene;

import java.io.Serializable;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.transaction.Status;
import javax.transaction.UserTransaction;

import junit.framework.TestCase;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.management.subsystems.ChildApplicationContextFactory;
import org.alfresco.repo.security.authentication.AuthenticationComponent;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.service.cmr.model.FileFolderService;
import org.alfresco.service.cmr.model.FileInfo;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.CopyService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.search.ResultSet;
import org.alfresco.service.cmr.search.ResultSetRow;
import org.alfresco.service.cmr.search.SearchService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.service.transaction.TransactionService;
import org.alfresco.util.ApplicationContextHelper;
import org.springframework.context.ApplicationContext;

public class ALF947Test extends TestCase
{
    private static ApplicationContext ctx = ApplicationContextHelper.getApplicationContext();
    
    private NodeService nodeService;
    private LuceneIndexerAndSearcher indexerAndSearcher;
    private TransactionService transactionService;
    private AuthenticationComponent authenticationComponent;
    private CopyService copyService;
    private FileFolderService fileFolderService;
    
    private UserTransaction testTX;

    private StoreRef storeRef = null;
    
    public ALF947Test()
    {
        super();
    }

    public void setUp() throws Exception
    {
        this.nodeService = (NodeService) ctx.getBean("dbNodeService");
        ChildApplicationContextFactory luceneSubSystem = (ChildApplicationContextFactory) ctx.getBean("lucene");
        this.indexerAndSearcher = (LuceneIndexerAndSearcher) luceneSubSystem.getApplicationContext().getBean("search.admLuceneIndexerAndSearcherFactory");
        this.authenticationComponent = (AuthenticationComponent) ctx.getBean("authenticationComponent");
        this.transactionService = (TransactionService) ctx.getBean("transactionComponent");
        this.copyService = (CopyService) ctx.getBean("CopyService");
        this.fileFolderService = (FileFolderService)ctx.getBean("FileFolderService");
    }

    @Override
    protected void tearDown() throws Exception
    {
        if (testTX.getStatus() == Status.STATUS_ACTIVE)
        {
            testTX.rollback();
        }
        AuthenticationUtil.clearCurrentSecurityContext();
        super.tearDown();
    }
    
    private NodeRef folder1;
    private NodeRef folder2;
    private NodeRef folder3;
    private NodeRef folder4;    
    private NodeRef folder5;
    private NodeRef folder6;
    private NodeRef folder7;
    
    private NodeRef file1;
    private NodeRef file2;
    private NodeRef file3;
    private NodeRef file4;
    
    private void buildBigTree(NodeRef rootNodeRef)
    {
        Map<QName, Serializable> properties = new HashMap<QName, Serializable>();

        // create node hierarchy
        properties.put(ContentModel.PROP_NAME, "folder1");
        QName assoc1Name = QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, "root_p_f1");
        folder1 = nodeService.createNode(rootNodeRef, ContentModel.ASSOC_CHILDREN, assoc1Name, ContentModel.TYPE_FOLDER, properties).getChildRef();

        properties.put(ContentModel.PROP_NAME, "folder2");
        QName assoc2Name = QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, "root_p_f2");
        folder2 = nodeService.createNode(rootNodeRef, ContentModel.ASSOC_CHILDREN, assoc2Name, ContentModel.TYPE_FOLDER, properties).getChildRef();

        properties.put(ContentModel.PROP_NAME, "folder3");
        QName assoc3Name = QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, "root_p_f3");
        folder3 = nodeService.createNode(rootNodeRef, ContentModel.ASSOC_CHILDREN, assoc3Name, ContentModel.TYPE_FOLDER, properties).getChildRef();

        properties.put(ContentModel.PROP_NAME, "folder4");
        QName assoc4Name = QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, "f1_p_f4");
        folder4 = nodeService.createNode(folder1, ContentModel.ASSOC_CONTAINS, assoc4Name, ContentModel.TYPE_FOLDER, properties).getChildRef();

        properties.put(ContentModel.PROP_NAME, "folder5");
        QName assoc5Name = QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, "f4_p_f5");
        folder5 = nodeService.createNode(folder4, ContentModel.ASSOC_CONTAINS, assoc5Name, ContentModel.TYPE_FOLDER, properties).getChildRef();

        properties.put(ContentModel.PROP_NAME, "folder6");
        QName assoc6Name = QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, "f5_p_f6");
        folder6 = nodeService.createNode(folder5, ContentModel.ASSOC_CONTAINS, assoc6Name, ContentModel.TYPE_FOLDER, properties).getChildRef();

        //properties.put(ContentModel.PROP_NAME, "folder7");
        //QName assoc7Name = QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, "f6_p_f7");
        //folder7 = nodeService.createNode(folder6, ContentModel.ASSOC_CONTAINS, assoc6Name, ContentModel.TYPE_FOLDER, properties).getChildRef();
        
        properties.put(ContentModel.PROP_NAME, "file1");
        QName assoc8Name = QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, "f1_p_file1");
        file1 = nodeService.createNode(folder1, ContentModel.ASSOC_CONTAINS, assoc8Name, ContentModel.TYPE_CONTENT, properties).getChildRef();

        properties.put(ContentModel.PROP_NAME, "file2");
        QName assoc9Name = QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, "f4_p_file2");
        file2 = nodeService.createNode(folder4, ContentModel.ASSOC_CONTAINS, assoc8Name, ContentModel.TYPE_CONTENT, properties).getChildRef();
        
        properties.put(ContentModel.PROP_NAME, "file3");
        QName assoc10Name = QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, "f5_p_file3");
        file3 = nodeService.createNode(folder5, ContentModel.ASSOC_CONTAINS, assoc9Name, ContentModel.TYPE_CONTENT, properties).getChildRef();        

        properties.put(ContentModel.PROP_NAME, "file4");
        QName assoc11Name = QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, "f3_p_file4");
        file4 = nodeService.createNode(folder3, ContentModel.ASSOC_CONTAINS, assoc10Name, ContentModel.TYPE_CONTENT, properties).getChildRef();

        nodeService.addChild(Arrays.asList(folder1, folder4, folder5, folder6), file4, ContentModel.ASSOC_CONTAINS,
                QName.createQName("stuff"));
    }

    /**
     * This moves a node under a different parent and then checks that a search does
     * not return duplicates
     * 
     * @throws Exception
     */
    public void testALF947_1() throws Exception
    {
        StoreRef storeRef = null;
        NodeRef rootNodeRef = null;
        String queryString = null;
        SearchService searchService = null;
        ResultSet results = null;

        try {
            testTX = transactionService.getUserTransaction();
            testTX.begin();
            this.authenticationComponent.setSystemUserAsCurrentUser();
            
            storeRef = nodeService.createStore(StoreRef.PROTOCOL_WORKSPACE, "Test_" + System.currentTimeMillis());
            rootNodeRef = nodeService.getRootNode(storeRef);
            
            buildBigTree(rootNodeRef);

            // perform a search, which should flush the index delta
            // this is key to getting the duplicates
            searchService = indexerAndSearcher.getSearcher(storeRef, true);
            queryString = "@cm\\:name:n4";
            results = searchService.query(storeRef, SearchService.LANGUAGE_LUCENE, queryString); // should call flushPending
            results.close();

            // move folder1 under folder2
            nodeService.moveNode(folder1, folder2, ContentModel.ASSOC_CONTAINS, QName.createQName("{namespace}test"));

            // search for all nodes in the store
            queryString = "PATH:\"//*\"";
            results = searchService.query(storeRef, SearchService.LANGUAGE_LUCENE, queryString);

            for (ResultSetRow row : results)
            {
               NodeRef nodeRef = row.getNodeRef();
               System.out.println("Node ref = " + nodeRef);
               Map<QName, Serializable> nodeProperties = nodeService.getProperties(nodeRef);
               for(QName propName : nodeProperties.keySet())
               {
                   System.out.println("Property " + propName + "=" + nodeProperties.get(propName));
               }
            }
            System.out.println("===================");

            results.close();

            assertEquals(10, results.length()); // expect 10 nodes from the search i.e. no duplicates
            List<ChildAssociationRef> parents = nodeService.getParentAssocs(file4);
            assertEquals(5, parents.size()); // expect 5 parents (including primary parent)
        }
        finally
        {
            if(null != storeRef)
            {
                //nodeService.deleteStore(storeRef);
            }
            if(null != testTX)
            {
                testTX.commit();
            }
        }
    }

    /**
     * The same as testALF942_1 but the search is performed in a different txn
     * @throws Exception
     */
    public void testALF947_2() throws Exception
    {
        StoreRef storeRef = null;
        NodeRef rootNodeRef = null;
        String queryString = null;
        SearchService searchService = null;
        ResultSet results = null;

        try {
            testTX = transactionService.getUserTransaction();
            testTX.begin();
            this.authenticationComponent.setSystemUserAsCurrentUser();
            
            storeRef = nodeService.createStore(StoreRef.PROTOCOL_WORKSPACE, "Test_" + System.currentTimeMillis());
            rootNodeRef = nodeService.getRootNode(storeRef);

            buildBigTree(rootNodeRef);

            // perform a search, which should flush the delta index
            // this is key to getting the duplicates
            searchService = indexerAndSearcher.getSearcher(storeRef, true);
            queryString = "@cm\\:name:n4";
            results = searchService.query(storeRef, SearchService.LANGUAGE_LUCENE, queryString); // should call flushPending
            results.close();

            // move n1 under n2
            nodeService.moveNode(folder1, folder2, ContentModel.ASSOC_CONTAINS, QName.createQName(NamespaceService.CONTENT_MODEL_PREFIX, "n2_p_n1")/*QName.createQName("{namespace}test")*/);
        }
        finally
        {
            if(null != storeRef)
            {
                ////nodeService.deleteStore(storeRef);
            }
            if(null != testTX)
            {
                testTX.commit();
            }
        }
            
        try {
            // start another transaction to perform a fresh search
            testTX = transactionService.getUserTransaction();
            testTX.begin();
            this.authenticationComponent.setSystemUserAsCurrentUser();
            
            // perform a search, which should flush the index delta
            queryString = "PATH:\"//*\"";
            searchService = indexerAndSearcher.getSearcher(storeRef, true);
            results = searchService.query(storeRef, SearchService.LANGUAGE_LUCENE, queryString);

            for (ResultSetRow row : results)
            {
               NodeRef nodeRef = row.getNodeRef();
               System.out.println("Node ref = " + nodeRef);
               Map<QName, Serializable> nodeProperties = nodeService.getProperties(nodeRef);
               for(QName propName : nodeProperties.keySet())
               {
                   System.out.println("Property " + propName + "=" + nodeProperties.get(propName));
               }
            }
            System.out.println("===================");

            results.close();

            assertEquals(10, results.length()); // expect 5 nodes from the search i.e. no duplicates

            List<ChildAssociationRef> parents = nodeService.getParentAssocs(file4);
            assertEquals(5, parents.size()); // expect 5 parents (including primary parent)
        }
        finally
        {
            if(null != storeRef)
            {
                //nodeService.deleteStore(storeRef);
            }
            if(null != testTX)
            {
                testTX.commit();
            }
        }
    }
    
    /**
     * This is duplicating what is happening in the bug report but without rules.
     * It results in a rename of the copied node which subsequently results in
     * a move node operation (with the parent staying the same).
     * 
     * A search is performed in a different txn to mimic the bug report
     * 
     * @throws Exception
     */
    public void testALF947_3() throws Exception
    {
        NodeRef rootNodeRef = null;
        String queryString = null;
        SearchService searchService = null;
        ResultSet results = null;

        try {
            testTX = transactionService.getUserTransaction();
            testTX.begin();
            this.authenticationComponent.setSystemUserAsCurrentUser();
            
            storeRef = nodeService.createStore(StoreRef.PROTOCOL_WORKSPACE, "Test_" + System.currentTimeMillis());
            rootNodeRef = nodeService.getRootNode(storeRef);

            buildBigTree(rootNodeRef);

            // copy folder1 node hierarchy under folder2
            NodeRef newNode = copyService.copyAndRename(folder1, folder2, ContentModel.ASSOC_CONTAINS, null, true);

            // perform a search, which should flush the index delta
            // this is key to getting the duplicates
            searchService = indexerAndSearcher.getSearcher(storeRef, true);
            queryString = "PATH:\"//*\"";
            results = searchService.query(storeRef, SearchService.LANGUAGE_LUCENE, queryString); // should call flushPending
            results.close();

            // rename the copied node
            FileInfo fileInfo = fileFolderService.rename(newNode, "folder7");     
        }
        finally
        {
            if(null != storeRef)
            {
                ////nodeService.deleteStore(storeRef);
            }
            if(null != testTX)
            {
                testTX.commit();
            }
        }
            
        try {
            // start another transaction to perform a fresh search
            testTX = transactionService.getUserTransaction();
            testTX.begin();
            this.authenticationComponent.setSystemUserAsCurrentUser();
            
            queryString = "PATH:\"//*\"";
            searchService = indexerAndSearcher.getSearcher(storeRef, true);
            results = searchService.query(storeRef, SearchService.LANGUAGE_LUCENE, queryString);

            for (ResultSetRow row : results)
            {
               NodeRef nodeRef = row.getNodeRef();
               System.out.println("Node ref = " + nodeRef);
               Map<QName, Serializable> nodeProperties = nodeService.getProperties(nodeRef);
               for(QName propName : nodeProperties.keySet())
               {
                   System.out.println("Property " + propName + "=" + nodeProperties.get(propName));
               }
            }
            System.out.println("===================");       
            
            results.close();

            // expect 9 nodes from the search i.e. no duplicates
            // n1, n2, n3, n4, n5, copies of n1 (renamed to n6), n3, n4, n5
            assertEquals(17, results.length());

            List<ChildAssociationRef> parents = nodeService.getParentAssocs(file4);
            // expect 9 parents (including primary parent) i.e. 4 parents from each copy of the subtree
            assertEquals(9, parents.size());
        }
        finally
        {
            if(null != storeRef)
            {
                ////nodeService.deleteStore(storeRef);
            }
            if(null != testTX)
            {
                testTX.commit();
            }
        }
    }
}
