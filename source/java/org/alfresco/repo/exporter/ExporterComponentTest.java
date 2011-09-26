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
 */package org.alfresco.repo.exporter;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.importer.ACPImportPackageHandler;
import org.alfresco.repo.security.authentication.AuthenticationComponent;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.model.FileFolderService;
import org.alfresco.service.cmr.model.FileInfo;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.ContentData;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.search.CategoryService;
import org.alfresco.service.cmr.security.AccessPermission;
import org.alfresco.service.cmr.view.Exporter;
import org.alfresco.service.cmr.view.ExporterContext;
import org.alfresco.service.cmr.view.ExporterCrawlerParameters;
import org.alfresco.service.cmr.view.ExporterService;
import org.alfresco.service.cmr.view.ImportPackageHandler;
import org.alfresco.service.cmr.view.ImporterService;
import org.alfresco.service.cmr.view.Location;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.BaseSpringTest;
import org.alfresco.util.TempFileProvider;
import org.alfresco.util.debug.NodeStoreInspector;
import org.springframework.extensions.surf.util.I18NUtil;


public class ExporterComponentTest extends BaseSpringTest
{

    private NodeService nodeService;
    private ExporterService exporterService;
    private ImporterService importerService;
    private FileFolderService fileFolderService;
    private CategoryService categoryService;
    private StoreRef storeRef;
    private AuthenticationComponent authenticationComponent;

    
    @Override
    protected void onSetUpInTransaction() throws Exception
    {
        nodeService = (NodeService)applicationContext.getBean(ServiceRegistry.NODE_SERVICE.getLocalName());
        exporterService = (ExporterService)applicationContext.getBean("exporterComponent");
        importerService = (ImporterService)applicationContext.getBean("importerComponent");
        fileFolderService = (FileFolderService) applicationContext.getBean("fileFolderService");
        categoryService = (CategoryService) applicationContext.getBean("categoryService");     
        
        this.authenticationComponent = (AuthenticationComponent)this.applicationContext.getBean("authenticationComponent");
        this.authenticationComponent.setSystemUserAsCurrentUser();
        this.storeRef = nodeService.createStore(StoreRef.PROTOCOL_WORKSPACE, "Test_" + System.currentTimeMillis());
    }

    @Override
    protected void onTearDownInTransaction() throws Exception
    {
        authenticationComponent.clearCurrentSecurityContext();
        super.onTearDownInTransaction();
    }
    
    public void testExport()
        throws Exception
    {
        TestProgress testProgress = new TestProgress();
        Location location = new Location(storeRef);

        // import
        InputStream test = getClass().getClassLoader().getResourceAsStream("org/alfresco/repo/importer/importercomponent_test.xml");
        InputStreamReader testReader = new InputStreamReader(test, "UTF-8");
        importerService.importView(testReader, location, null, null);        
        
        dumpNodeStore(Locale.ENGLISH);
        dumpNodeStore(Locale.FRENCH);
        dumpNodeStore(Locale.GERMAN);
        
        // now export
        location.setPath("/system");
        File tempFile = TempFileProvider.createTempFile("xmlexporttest", ".xml");
        OutputStream output = new FileOutputStream(tempFile);
        ExporterCrawlerParameters parameters = new ExporterCrawlerParameters();
        parameters.setExportFrom(location);
//        parameters.setExcludeAspects(new QName[] { ContentModel.ASPECT_AUDITABLE });
//        parameters.setExcludeChildAssocs(new QName[] { ContentModel.ASSOC_CONTAINS });
        
        File acpFile = TempFileProvider.createTempFile("alf", ACPExportPackageHandler.ACP_EXTENSION);
        File dataFile = new File("test");
        File contentDir = new File("test");
        ACPExportPackageHandler acpHandler = new ACPExportPackageHandler(new FileOutputStream(acpFile), dataFile, contentDir, null);
        acpHandler.setNodeService(nodeService);
        acpHandler.setExportAsFolders(true);
        exporterService.exportView(acpHandler, parameters, testProgress);
        output.close();
    }

    /**
     * Round-trip of export then import will result in the imported content having the same categories
     * assigned to it as for the exported content -- provided the source and destination stores are the same.
     */
    @SuppressWarnings("unchecked")
    public void testRoundTripKeepsCategoriesWhenWithinSameStore() throws Exception
    {   
        // Use a store ref that has the bootstrapped categories
        StoreRef storeRef = StoreRef.STORE_REF_WORKSPACE_SPACESSTORE;
        NodeRef rootNode = nodeService.getRootNode(storeRef);
        
        ChildAssociationRef contentChildAssocRef = createContentWithCategories(storeRef, rootNode);
        
        // Export/import
        File acpFile = exportContent(contentChildAssocRef);
        FileInfo importFolderFileInfo = importContent(acpFile, rootNode);
        
        // Check categories
        NodeRef importedFileNode = fileFolderService.searchSimple(importFolderFileInfo.getNodeRef(), "test.txt");
        assertNotNull("Couldn't find imported file: test.txt", importedFileNode);
        assertTrue(nodeService.hasAspect(importedFileNode, ContentModel.ASPECT_GEN_CLASSIFIABLE));
        List<NodeRef> importedFileCategories = (List<NodeRef>)
            nodeService.getProperty(importedFileNode, ContentModel.PROP_CATEGORIES);
        assertCategoriesEqual(importedFileCategories,
                    "Regions",
                    "Software Document Classification");
    }
    
    /**
     * If the source and destination stores are not the same, then a round-trip of export then import
     * will result in the imported content not having the categories assigned to it that were present
     * on the exported content.
     */
    @SuppressWarnings("unchecked")
    public void testRoundTripLosesCategoriesImportingToDifferentStore() throws Exception
    {   
        // Use a store ref that has the bootstrapped categories
        StoreRef storeRef = StoreRef.STORE_REF_WORKSPACE_SPACESSTORE;
        NodeRef rootNode = nodeService.getRootNode(storeRef);
        
        ChildAssociationRef contentChildAssocRef = createContentWithCategories(storeRef, rootNode);
        
        // Export
        File acpFile = exportContent(contentChildAssocRef);
        // Import - destination store is different from export store.
        NodeRef destRootNode = nodeService.getRootNode(this.storeRef);
        FileInfo importFolderFileInfo = importContent(acpFile, destRootNode);
        
        // Check categories
        NodeRef importedFileNode = fileFolderService.searchSimple(importFolderFileInfo.getNodeRef(), "test.txt");
        assertNotNull("Couldn't find imported file: test.txt", importedFileNode);
        assertTrue(nodeService.hasAspect(importedFileNode, ContentModel.ASPECT_GEN_CLASSIFIABLE));
        List<NodeRef> importedFileCategories = (List<NodeRef>)
            nodeService.getProperty(importedFileNode, ContentModel.PROP_CATEGORIES);
        assertEquals("No categories should have been imported for the content", 0, importedFileCategories.size());
    }

    /**
     * @param contentChildAssocRef
     * @return
     * @throws FileNotFoundException
     * @throws IOException
     */
    private File exportContent(ChildAssociationRef contentChildAssocRef)
                throws FileNotFoundException, IOException
    {
        TestProgress testProgress = new TestProgress();
        Location location = new Location(contentChildAssocRef.getParentRef());
        ExporterCrawlerParameters parameters = new ExporterCrawlerParameters();
        parameters.setExportFrom(location);
        File acpFile = TempFileProvider.createTempFile("category-export-test", ACPExportPackageHandler.ACP_EXTENSION);
        System.out.println("Exporting to file: " + acpFile.getAbsolutePath());        
        File dataFile = new File("test-data-file");
        File contentDir = new File("test-content-dir");
        OutputStream fos = new FileOutputStream(acpFile);
        ACPExportPackageHandler acpHandler = new ACPExportPackageHandler(fos, dataFile, contentDir, null);
        acpHandler.setNodeService(nodeService);
        acpHandler.setExportAsFolders(true);
        exporterService.exportView(acpHandler, parameters, testProgress);
        fos.close();
        return acpFile;
    }

    /**
     * @param storeRef
     * @param rootNode
     * @return
     */
    private ChildAssociationRef createContentWithCategories(StoreRef storeRef, NodeRef rootNode)
    {   
        Collection<ChildAssociationRef> assocRefs = categoryService.
            getRootCategories(storeRef, ContentModel.ASPECT_GEN_CLASSIFIABLE);
        assertTrue("Pre-condition failure: not enough categories", assocRefs.size() >= 2);
        Iterator<ChildAssociationRef> it = assocRefs.iterator();
        NodeRef softwareDocCategoryNode = it.next().getChildRef();
        it.next(); // skip one
        NodeRef regionsCategoryNode = it.next().getChildRef();        
        
        
        // Create a content node to categorise
        FileInfo exportFileInfo = fileFolderService.create(rootNode, "Export Folder", ContentModel.TYPE_FOLDER);
        Map<QName, Serializable> properties = Collections.singletonMap(ContentModel.PROP_NAME, (Serializable)"test.txt");
        ChildAssociationRef contentChildAssocRef = nodeService.createNode(
                    exportFileInfo.getNodeRef(),
                    ContentModel.ASSOC_CHILDREN,
                    ContentModel.ASSOC_CHILDREN,
                    ContentModel.TYPE_CONTENT,
                    properties);
        
        NodeRef contentNodeRef = contentChildAssocRef.getChildRef();

        // Attach categories
        ArrayList<NodeRef> categories = new ArrayList<NodeRef>(2);
        categories.add(softwareDocCategoryNode);
        categories.add(regionsCategoryNode);
        if(!nodeService.hasAspect(contentNodeRef, ContentModel.ASPECT_GEN_CLASSIFIABLE))
        {
            HashMap<QName, Serializable> props = new HashMap<QName, Serializable>();
            props.put(ContentModel.PROP_CATEGORIES, categories);
            nodeService.addAspect(contentNodeRef, ContentModel.ASPECT_GEN_CLASSIFIABLE, props);
        }
        else
        {
            nodeService.setProperty(contentNodeRef, ContentModel.PROP_CATEGORIES, categories);
        }
        return contentChildAssocRef;
    }
    
    /**
     * @param acpFile
     * @param destRootNode
     * @return
     */
    private FileInfo importContent(File acpFile, NodeRef destRootNode)
    {
        FileInfo importFolderFileInfo = fileFolderService.create(destRootNode, "Import Folder", ContentModel.TYPE_FOLDER);
        ImportPackageHandler importHandler = new ACPImportPackageHandler(acpFile, ACPImportPackageHandler.DEFAULT_ENCODING);
        importerService.importView(importHandler, new Location(importFolderFileInfo.getNodeRef()), null, null);
        return importFolderFileInfo;
    }

    private void assertCategoriesEqual(List<NodeRef> categories, String... expectedCategoryNames)
    {
        assertEquals("Number of categories is not as expected.", expectedCategoryNames.length, categories.size());
        
        List<String> categoryNames = new ArrayList<String>(10);
        for (NodeRef nodeRef : categories)
        {
            String name = (String) nodeService.getProperty(nodeRef, ContentModel.PROP_NAME);
            categoryNames.add(name);
        }
        // Sort, to give deterministic test results
        Collections.sort(categoryNames);
        
        for (int i = 0; i < expectedCategoryNames.length; i++)
        {
            assertEquals(expectedCategoryNames[i], categoryNames.get(i));
        }
    }
    
    
    private void dumpNodeStore(Locale locale)
    {
     
        System.out.println(locale.getDisplayLanguage() + " LOCALE: ");
        I18NUtil.setLocale(locale);
        System.out.println(NodeStoreInspector.dumpNodeStore((NodeService) applicationContext.getBean("NodeService"), storeRef));
    }
    
    private static class TestProgress
        implements Exporter
    {

        public void start(ExporterContext exportNodeRef)
        {
            System.out.println("TestProgress: start");
        }

        public void startNamespace(String prefix, String uri)
        {
//            System.out.println("TestProgress: start namespace prefix = " + prefix + " uri = " + uri);
        }

        public void endNamespace(String prefix)
        {
//            System.out.println("TestProgress: end namespace prefix = " + prefix);
        }

        public void startNode(NodeRef nodeRef)
        {
            System.out.println("TestProgress: start node " + nodeRef);
        }

        public void endNode(NodeRef nodeRef)
        {
//            System.out.println("TestProgress: end node " + nodeRef);
        }

        public void startAspect(NodeRef nodeRef, QName aspect)
        {
//            System.out.println("TestProgress: start aspect " + aspect);
        }

        public void endAspect(NodeRef nodeRef, QName aspect)
        {
//            System.out.println("TestProgress: end aspect " + aspect);
        }

        public void startProperty(NodeRef nodeRef, QName property)
        {
//            System.out.println("TestProgress: start property " + property);
        }

        public void endProperty(NodeRef nodeRef, QName property)
        {
//            System.out.println("TestProgress: end property " + property);
        }

        public void startValueCollection(NodeRef nodeRef, QName property)
        {
//          System.out.println("TestProgress: start value collection: node " + nodeRef + " , property " + property);
        }

        public void endValueCollection(NodeRef nodeRef, QName property)
        {
//          System.out.println("TestProgress: end value collection: node " + nodeRef + " , property " + property);
        }
        
        public void value(NodeRef nodeRef, QName property, Object value, int index)
        {
//            System.out.println("TestProgress: single value " + value);
        }

        public void content(NodeRef nodeRef, QName property, InputStream content, ContentData contentData, int index)
        {
//            System.out.println("TestProgress: content stream ");
        }

        public void startAssoc(NodeRef nodeRef, QName assoc)
        {
//            System.out.println("TestProgress: start association " + assocDef.getName());
        }

        public void endAssoc(NodeRef nodeRef, QName assoc)
        {
//            System.out.println("TestProgress: end association " + assocDef.getName());
        }

        public void warning(String warning)
        {
            System.out.println("TestProgress: warning " + warning);   
        }

        public void end()
        {
            System.out.println("TestProgress: end");
        }

        public void startProperties(NodeRef nodeRef)
        {
//            System.out.println("TestProgress: startProperties: " + nodeRef);
        }

        public void endProperties(NodeRef nodeRef)
        {
//            System.out.println("TestProgress: endProperties: " + nodeRef);
        }

        public void startAspects(NodeRef nodeRef)
        {
//          System.out.println("TestProgress: startAspects: " + nodeRef);
        }

        public void endAspects(NodeRef nodeRef)
        {
//          System.out.println("TestProgress: endAspects: " + nodeRef);
        }

        public void startAssocs(NodeRef nodeRef)
        {
//          System.out.println("TestProgress: startAssocs: " + nodeRef);
        }

        public void endAssocs(NodeRef nodeRef)
        {
//          System.out.println("TestProgress: endAssocs: " + nodeRef);
        }

        public void startACL(NodeRef nodeRef)
        {
//          System.out.println("TestProgress: startACL: " + nodeRef);
        }

        public void permission(NodeRef nodeRef, AccessPermission permission)
        {
//          System.out.println("TestProgress: permission: " + permission);
        }

        public void endACL(NodeRef nodeRef)
        {
//          System.out.println("TestProgress: endACL: " + nodeRef);
        }

        public void startReference(NodeRef nodeRef, QName childName)
        {
//          System.out.println("TestProgress: startReference: " + nodeRef);
        }

        public void endReference(NodeRef nodeRef)
        {
//          System.out.println("TestProgress: endReference: " + nodeRef);
        }

        public void endValueMLText(NodeRef nodeRef)
        {
//             System.out.println("TestProgress: end MLValue.");            
        }

        public void startValueMLText(NodeRef nodeRef, Locale locale)
        {
//             System.out.println("TestProgress: start MLValue for locale: " + locale);            
        }

    }
    
}
