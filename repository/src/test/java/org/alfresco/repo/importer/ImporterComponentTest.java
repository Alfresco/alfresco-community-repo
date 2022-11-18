/*
 * #%L
 * Alfresco Repository
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
package org.alfresco.repo.importer;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.security.authentication.AuthenticationComponent;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.repository.datatype.DefaultTypeConverter;
import org.alfresco.service.cmr.version.VersionHistory;
import org.alfresco.service.cmr.version.VersionService;
import org.alfresco.service.cmr.view.ImporterBinding.UUID_BINDING;
import org.alfresco.service.cmr.view.ImporterBinding;
import org.alfresco.service.cmr.view.ImporterContentCache;
import org.alfresco.service.cmr.view.ImporterService;
import org.alfresco.service.cmr.view.Location;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.service.namespace.RegexQNamePattern;
import org.alfresco.test_category.BaseSpringTestsCategory;
import org.alfresco.util.BaseSpringTest;
import org.alfresco.util.debug.NodeStoreInspector;
import org.joda.time.DateTimeZone;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.springframework.extensions.surf.util.ISO8601DateFormat;
import org.springframework.transaction.annotation.Transactional;

@Category(BaseSpringTestsCategory.class)
@Transactional
public class ImporterComponentTest extends BaseSpringTest
{
    private ImporterService importerService;
    private ImporterBootstrap importerBootstrap;
    private VersionService versionService;
    private NodeService nodeService;
    private StoreRef storeRef;
    private AuthenticationComponent authenticationComponent;


    @Before
    public void before() throws Exception
    {
        nodeService = (NodeService)applicationContext.getBean(ServiceRegistry.NODE_SERVICE.getLocalName());
        importerService = (ImporterService)applicationContext.getBean(ServiceRegistry.IMPORTER_SERVICE.getLocalName());
        
        importerBootstrap = (ImporterBootstrap)applicationContext.getBean("spacesBootstrap");
        
        this.authenticationComponent = (AuthenticationComponent)this.applicationContext.getBean("authenticationComponent");
        
        this.authenticationComponent.setSystemUserAsCurrentUser();
        
        this.versionService = (VersionService)this.applicationContext.getBean("VersionService");
        
        // Create the store
        this.storeRef = nodeService.createStore(StoreRef.PROTOCOL_WORKSPACE, "Test_" + System.currentTimeMillis());
        
        TimeZone tz = TimeZone.getTimeZone("GMT");
        TimeZone.setDefault(tz);
        // Joda time has already grabbed the JVM zone so re-set it here
        DateTimeZone.setDefault(DateTimeZone.forTimeZone(tz));
    }
    
    @After
    public void after()
    {
        authenticationComponent.clearCurrentSecurityContext();
    }
    
    
    @Test
    public void testImport() throws Exception
    {
        InputStream test = getClass().getClassLoader().getResourceAsStream("org/alfresco/repo/importer/importercomponent_test.xml");
        InputStreamReader testReader = new InputStreamReader(test, "UTF-8");
        Location location = new Location(storeRef);
        importerService.importView(testReader, location, null, new ImportTimerProgress());
        System.out.println(NodeStoreInspector.dumpNodeStore(nodeService, storeRef));
    }

    @Test
    public void testImportNotIndexedSubfolder() throws Exception
    {
        InputStream test = getClass().getClassLoader().getResourceAsStream(
            "org/alfresco/repo/importer/import_not_indexed_subfolder.xml");

        try (InputStreamReader testReader = new InputStreamReader(test, "UTF-8"))
        {
            Location location = new Location(storeRef);
            importerService.importView(testReader, location, null, new ImportTimerProgress());
            NodeRef rootNodeRef = nodeService.getRootNode(storeRef);
            NodeRef testParentFolderRef = nodeService.getChildAssocs(rootNodeRef).get(0).getChildRef();
            NodeRef testSubfolderRef = nodeService.getChildAssocs(testParentFolderRef).get(0).getChildRef();

            assertFalse("The node's isIndexed property should be false.",
                DefaultTypeConverter.INSTANCE.convert(Boolean.class,
                    nodeService.getProperty(testSubfolderRef, ContentModel.PROP_IS_INDEXED)));
            assertFalse("The node's isContentIndexed property should be false.",
                DefaultTypeConverter.INSTANCE.convert(Boolean.class,
                    nodeService.getProperty(testSubfolderRef, ContentModel.PROP_IS_CONTENT_INDEXED)));
            assertTrue("The node should be marked with the indexControl aspect.",
                nodeService.getAspects(testSubfolderRef).contains(ContentModel.ASPECT_INDEX_CONTROL));
        }
    }
    
    @Test
    public void testImportWithAuditableProperties() throws Exception
    {
        InputStream test = getClass().getClassLoader().getResourceAsStream("org/alfresco/repo/importer/importercomponent_test.xml");
        InputStreamReader testReader = new InputStreamReader(test, "UTF-8");
        Location location = new Location(storeRef);
        try
        {
            importerService.importView(
                    testReader,
                    location,
                    null,
                    new ImportTimerProgress());
        }
        finally
        {
            testReader.close();
        }
        
        NodeRef rootNodeRef = nodeService.getRootNode(storeRef);
        List<ChildAssociationRef> childAssocs = nodeService.getChildAssocs(
                rootNodeRef,
                RegexQNamePattern.MATCH_ALL,
                new RegexQNamePattern(NamespaceService.CONTENT_MODEL_1_0_URI, "SpaceWith.*"));
//                QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, "SpaceWithAuditable"));
        assertEquals("'SpaceWith*' path not found", 2, childAssocs.size());
        
        NodeRef nodeRef = childAssocs.get(0).getChildRef();
        Map<QName, Serializable> nodeProps = nodeService.getProperties(nodeRef);
        String createdDate = DefaultTypeConverter.INSTANCE.convert(String.class, nodeProps.get(ContentModel.PROP_CREATED));
        String creator = DefaultTypeConverter.INSTANCE.convert(String.class, nodeProps.get(ContentModel.PROP_CREATOR));
        String modifiedDate = DefaultTypeConverter.INSTANCE.convert(String.class, nodeProps.get(ContentModel.PROP_MODIFIED));
        String modifier = DefaultTypeConverter.INSTANCE.convert(String.class, nodeProps.get(ContentModel.PROP_MODIFIER));
        // Check that the cm:auditable properties are correct
        assertEquals("cm:created not preserved during import", ISO8601DateFormat.format(ISO8601DateFormat.parse("2009-04-30T23:00:00.000Z")), createdDate);
        assertEquals("cm:creator not preserved during import", "Import Creator", creator);
        assertEquals("cm:modified not preserved during import", ISO8601DateFormat.format(ISO8601DateFormat.parse("2009-05-01T23:00:00.000Z")), modifiedDate);
        assertEquals("cm:modifier not preserved during import", "Import Modifier", modifier);
        
        nodeRef = childAssocs.get(1).getChildRef();
        nodeProps = nodeService.getProperties(nodeRef);
        createdDate = DefaultTypeConverter.INSTANCE.convert(String.class, nodeProps.get(ContentModel.PROP_CREATED));
        creator = DefaultTypeConverter.INSTANCE.convert(String.class, nodeProps.get(ContentModel.PROP_CREATOR));
        modifiedDate = DefaultTypeConverter.INSTANCE.convert(String.class, nodeProps.get(ContentModel.PROP_MODIFIED));
        modifier = DefaultTypeConverter.INSTANCE.convert(String.class, nodeProps.get(ContentModel.PROP_MODIFIER));
        // Check that the cm:auditable properties are correct
        assertEquals("cm:created not preserved during import", ISO8601DateFormat.format(ISO8601DateFormat.parse("2009-04-30T23:00:00.000Z")), createdDate);
        assertEquals("cm:creator not preserved during import", "Import Creator", creator);
        assertEquals("cm:modifier not preserved during import", AuthenticationUtil.getSystemUserName(), modifier);
    }
    
    @Test
    public void testImportWithVersioning() throws Exception
    {
        InputStream test = getClass().getClassLoader().getResourceAsStream("org/alfresco/repo/importer/importercomponent_test.xml");
        InputStreamReader testReader = new InputStreamReader(test, "UTF-8");
        Location location = new Location(storeRef);
        try
        {
            importerService.importView(
                    testReader,
                    location,
                    null,
                    new ImportTimerProgress());
        }
        finally
        {
            testReader.close();
        }
        
        NodeRef rootNodeRef = nodeService.getRootNode(storeRef);
        List<ChildAssociationRef> childAssocs = nodeService.getChildAssocs(
                rootNodeRef,
                RegexQNamePattern.MATCH_ALL,
                new RegexQNamePattern(NamespaceService.CONTENT_MODEL_1_0_URI, "Version Containing Folder"));
        assertEquals("'Version Folder' path not found", 1, childAssocs.size());
        NodeRef versionFolder = childAssocs.get(0).getChildRef();
        
        childAssocs = nodeService.getChildAssocs(
                versionFolder,
                RegexQNamePattern.MATCH_ALL,
                new RegexQNamePattern(NamespaceService.CONTENT_MODEL_1_0_URI, "Versioned Node"));
        assertEquals("'Versioned Node' path not found", 1, childAssocs.size());
        NodeRef versionedNode = childAssocs.get(0).getChildRef();
        
        // Check the version label isn't 1.0, but the 1.15 from the ACP
        assertEquals("1.15", nodeService.getProperty(versionedNode, ContentModel.PROP_VERSION_LABEL));
        
        // Check that there's no history on the (un-versioned) folder
        assertEquals(null, versionService.getVersionHistory(versionFolder));
        
        // Check that there's a single version history entry for the node
        VersionHistory vh = versionService.getVersionHistory(versionedNode);
        assertNotNull(vh);
        assertEquals(1, vh.getAllVersions().size());
    }
    
    @Test
    public void testImportWithUuidBinding() throws Exception
    {
        Location location = new Location(storeRef);

        // First pass must succeed
        InputStream test1 = getClass().getClassLoader().getResourceAsStream("org/alfresco/repo/importer/importercomponent_test.xml");
        InputStreamReader testReader1 = new InputStreamReader(test1, "UTF-8");
        try
        {
            importerService.importView(testReader1, location, null, new ImportTimerProgress());
        }
        finally
        {
            testReader1.close();
        }
        // Second pass must succeed (defaults to CREATE_NEW)
        InputStream test2 = getClass().getClassLoader().getResourceAsStream("org/alfresco/repo/importer/importercomponent_test.xml");
        InputStreamReader testReader2 = new InputStreamReader(test2, "UTF-8");
        try
        {
            importerBootstrap.setUuidBinding(UUID_BINDING.CREATE_NEW_WITH_UUID);
            importerService.importView(testReader2, location, null, new ImportTimerProgress());
        }
        finally
        {
            testReader2.close();
        }
        // Set the UUID binding to guarantee a failure
        InputStream test3 = getClass().getClassLoader().getResourceAsStream("org/alfresco/repo/importer/importercomponent_test.xml");
        InputStreamReader testReader3 = new InputStreamReader(test3, "UTF-8");
        try
        {
            importerBootstrap.setUuidBinding(UUID_BINDING.THROW_ON_COLLISION);
            importerService.importView(testReader3, location, null, new ImportTimerProgress());
            fail("Failed to detected collision of UUID on import with THROW_ON_COLLISION");
        }
        catch (Throwable e)
        {
            // Expected
        }
        finally
        {
            importerBootstrap.setUuidBinding(UUID_BINDING.CREATE_NEW_WITH_UUID);
            testReader3.close();
        }
        System.out.println(NodeStoreInspector.dumpNodeStore(nodeService, storeRef));
    }

    @Test
    public void testBootstrap()
    {
        StoreRef bootstrapStoreRef = new StoreRef(StoreRef.PROTOCOL_WORKSPACE, "Test_" + System.currentTimeMillis());
        importerBootstrap.setStoreUrl(bootstrapStoreRef.toString());
        importerBootstrap.bootstrap();
        authenticationComponent.setSystemUserAsCurrentUser();
        System.out.println(NodeStoreInspector.dumpNodeStore(nodeService, bootstrapStoreRef));
    }
    
    @Test
    public void testImportFoldersUuidBindingNullUuidNullLocationPath() throws Exception
    {
        Location location = new Location(storeRef);
         
        InputStream test1 = getClass().getClassLoader().getResourceAsStream("org/alfresco/repo/importer/import_folders.xml");
        InputStreamReader testReader1 = new InputStreamReader(test1, "UTF-8");
        try
        {
            importerService.importView(testReader1, location, new ImporterBinding()
            {
                
                @Override
                public String getValue(String key)
                {
                    return null;
                }
                
                @Override
                public UUID_BINDING getUUIDBinding()
                {
                    return UUID_BINDING.UPDATE_EXISTING;
                }
                
                @Override
                public ImporterContentCache getImportConentCache()
                {
                    return null;
                }
                
                @Override
                public QName[] getExcludedClasses()
                {
                    return null;
                }
                
                @Override
                public boolean allowReferenceWithinTransaction()
                {
                    return true;
                }
            }, new ImportTimerProgress());
        }
        finally
        {
            testReader1.close();
        }
        
        //  - root        
        //      - Main folder  
        //          - sub folder 
        //              - sub folder 1
        //              - sub folder 2
        //      - Archive folder
        
        NodeRef rootNodeRef = nodeService.getRootNode(storeRef);
        List<ChildAssociationRef> childAssocs = nodeService.getChildAssocs(
                rootNodeRef,
                RegexQNamePattern.MATCH_ALL,
                new RegexQNamePattern(NamespaceService.CONTENT_MODEL_1_0_URI, "main folder"));
        assertEquals("'main folder' path not found", 1, childAssocs.size());
        
        NodeRef mainFolderNode = childAssocs.get(0).getChildRef();
        childAssocs = nodeService.getChildAssocs(mainFolderNode);
        assertEquals("'sub folder' path not found", 1, childAssocs.size());
        
        NodeRef subFolderNode = childAssocs.get(0).getChildRef();
        childAssocs = nodeService.getChildAssocs(subFolderNode);                
        assertEquals("'subsub folder' path not found", 2, childAssocs.size());
        
        childAssocs = nodeService.getChildAssocs(
                rootNodeRef,
                RegexQNamePattern.MATCH_ALL,
                new RegexQNamePattern(NamespaceService.CONTENT_MODEL_1_0_URI, "ArchiveFolder"));
        assertEquals("'ArchiveFolder' path not found", 1, childAssocs.size());
    }
}

