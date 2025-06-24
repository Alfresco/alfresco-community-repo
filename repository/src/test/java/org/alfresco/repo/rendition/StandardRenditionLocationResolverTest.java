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

package org.alfresco.repo.rendition;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.springframework.transaction.annotation.Transactional;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.model.Repository;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.rendition.RenditionDefinition;
import org.alfresco.service.cmr.rendition.RenditionService;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.test_category.BaseSpringTestsCategory;
import org.alfresco.util.BaseAlfrescoSpringTest;
import org.alfresco.util.GUID;

/**
 * @author Brian Remmington
 * @author Nick Smith
 *
 * @deprecated We are introducing the new async RenditionService2.
 */
@Deprecated
@Category(BaseSpringTestsCategory.class)
@Transactional
public class StandardRenditionLocationResolverTest extends BaseAlfrescoSpringTest
{
    private ServiceRegistry serviceRegistry;
    private NodeRef companyHome;
    private StandardRenditionLocationResolverImpl locationResolver;
    private RenditionService renditionService;
    private Repository repositoryHelper;

    @Before
    public void before() throws Exception
    {
        super.before();

        // Get the required services
        this.serviceRegistry = (ServiceRegistry) applicationContext.getBean("ServiceRegistry");
        this.nodeService = serviceRegistry.getNodeService();
        this.renditionService = (RenditionService) applicationContext.getBean("RenditionService");
        this.repositoryHelper = (Repository) applicationContext.getBean("repositoryHelper");
        this.companyHome = this.applicationContext.getBean("repositoryHelper", Repository.class).getCompanyHome();
        locationResolver = new StandardRenditionLocationResolverImpl();
        locationResolver.setServiceRegistry(serviceRegistry);
        locationResolver.setRepositoryHelper(repositoryHelper);
    }

    @Test
    public void testChildAssociationFinder()
    {
        QName renditionKind = QName.createQName(NamespaceService.APP_MODEL_1_0_URI, "test");
        NodeRef sourceNode = makeNode(companyHome, ContentModel.TYPE_CONTENT);
        NodeRef tempRenditionNode = makeNode(sourceNode, ContentModel.TYPE_CONTENT);

        RenditionDefinition renditionDef = renditionService.createRenditionDefinition(renditionKind,
                "brians_test_engine");

        // Test default behaviour, no destination path or NodeRef specified.
        RenditionLocation location = locationResolver.getRenditionLocation(sourceNode, renditionDef, tempRenditionNode);

        assertEquals(sourceNode, location.getParentRef());
        assertNull(location.getChildName());
        assertNull(location.getChildRef());

        // Test fully specified path.
        NodeRef targetFolder = makeNode(companyHome, ContentModel.TYPE_FOLDER);
        String companyHomeName = (String) nodeService.getProperty(companyHome, ContentModel.PROP_NAME);
        String targetFolderName = (String) nodeService.getProperty(targetFolder, ContentModel.PROP_NAME);
        String template = "/" + companyHomeName + "/" + targetFolderName + "/brian.xml";
        renditionDef.setParameterValue(RenditionService.PARAM_DESTINATION_PATH_TEMPLATE, template);
        location = locationResolver.getRenditionLocation(sourceNode, renditionDef, tempRenditionNode);

        assertEquals(targetFolder, location.getParentRef());
        assertEquals("brian.xml", location.getChildName());
        assertNull(location.getChildRef());

        // Test path with name substitution.
        template = targetFolderName + "/test-${sourceContentType}.xml";
        renditionDef.setParameterValue(RenditionService.PARAM_DESTINATION_PATH_TEMPLATE, template);
        location = locationResolver.getRenditionLocation(sourceNode, renditionDef, tempRenditionNode);

        assertEquals(targetFolder, location.getParentRef());
        assertEquals("test-" + ContentModel.PROP_CONTENT.getLocalName() + ".xml", location.getChildName());
        assertNull(location.getChildRef());

        // Test that when the template path specifies an existing node then that
        // node is set as the 'childRef' property on the RenditionLocation.
        NodeRef destinationNode = makeNode(targetFolder, ContentModel.TYPE_CONTENT);
        String destinationName = (String) nodeService.getProperty(destinationNode, ContentModel.PROP_NAME);
        template = targetFolderName + "/" + destinationName;
        renditionDef.setParameterValue(RenditionService.PARAM_DESTINATION_PATH_TEMPLATE, template);
        location = locationResolver.getRenditionLocation(sourceNode, renditionDef, tempRenditionNode);

        assertEquals(targetFolder, location.getParentRef());
        assertEquals(destinationName, location.getChildName());
        assertEquals(destinationNode, location.getChildRef());

        // Test that the 'destination node' param takes precedence over the 'template path' param.
        template = "/" + targetFolderName + "/brian.xml";
        renditionDef.setParameterValue(RenditionService.PARAM_DESTINATION_PATH_TEMPLATE, template);
        renditionDef.setParameterValue(RenditionService.PARAM_DESTINATION_NODE, destinationNode);
        location = locationResolver.getRenditionLocation(sourceNode, renditionDef, tempRenditionNode);

        assertEquals(targetFolder, location.getParentRef());
        assertEquals(destinationName, location.getChildName());
        assertEquals(destinationNode, location.getChildRef());
    }

    @Test
    public void testCreatesFoldersForTemplatedLocation() throws Exception
    {
        QName fooName = QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, "testFooFolder");
        QName barName = QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, "testBarFolder");
        String fooPath = "/testFooFolder";
        String barPath = fooPath + "/testBarFolder";

        List<ChildAssociationRef> childAssocs = nodeService.getChildAssocs(companyHome, ContentModel.ASSOC_CONTAINS,
                fooName);
        assertTrue("Folder " + fooPath + " should not exist!", childAssocs.isEmpty());

        QName renditionKind = QName.createQName(NamespaceService.APP_MODEL_1_0_URI, "test");
        NodeRef sourceFolder = makeNode(companyHome, ContentModel.TYPE_FOLDER);
        NodeRef sourceNode = makeNode(sourceFolder, ContentModel.TYPE_CONTENT);
        NodeRef tempRenditionNode = makeNode(companyHome, ContentModel.TYPE_CONTENT);

        RenditionDefinition renditionDef = renditionService.createRenditionDefinition(renditionKind,
                "nicks_test_engine");

        String pathTemplate = barPath + "${cwd}nick.xml";
        renditionDef.setParameterValue(RenditionService.PARAM_DESTINATION_PATH_TEMPLATE, pathTemplate);

        RenditionLocation location = locationResolver.getRenditionLocation(sourceNode, renditionDef, tempRenditionNode);

        NodeRef fooNode = checkFolder(fooName, companyHome, "Foo");
        NodeRef barNode = checkFolder(barName, fooNode, "Bar");
        NodeRef finalFolderNode = checkFolder(nodeService.getPrimaryParent(sourceFolder).getQName(), barNode, "Final Folder");

        assertEquals("Final folder is not the rendition parent!", finalFolderNode, location.getParentRef());
        assertEquals("nick.xml", location.getChildName());
    }

    // public void testOldRenditionIsOrphaned() throws Exception
    // {
    // QName renditionKind = QName.createQName(NamespaceService.APP_MODEL_1_0_URI, "test");
    // NodeRef sourceNode = makeNode(companyHome, ContentModel.TYPE_CONTENT);
    // NodeRef tempRenditionNode = makeNode(sourceNode, ContentModel.TYPE_CONTENT);
    // ChildAssociationRef tempRenditionLocation=nodeService.getPrimaryParent(tempRenditionNode);
    //
    // RenditionDefinition definition = renditionService.createRenditionDefinition(renditionKind,
    // "nicks_test_engine");
    // ChildAssociationRef oldRendition =
    // nodeService.createNode(sourceNode, RenditionModel.ASSOC_RENDITION, definition.getRenditionName(), ContentModel.TYPE_CONTENT);
    // definition.setParameterValue(RenditionService.PARAM_ORPHAN_EXISTING_RENDITION, true);
    //
    // //Test deletes old rendition if it's under source Node and new rendition is not.
    // locationResolver.getRenditionLocation(sourceNode, definition, tempRenditionLocation);
    //
    // List<ChildAssociationRef> sourceChildren = nodeService.getChildAssocs(sourceNode);
    // assertFalse("The old rendition association should ahve been removed!", sourceChildren.contains(oldRendition));
    // assertFalse("The old rendition should have been deleted!", nodeService.exists(oldRendition.getChildRef()) );
    // }

    private NodeRef checkFolder(QName folderName, NodeRef parentName, String folderMessageName)
    {
        List<ChildAssociationRef> folderAssocs = nodeService.getChildAssocs(parentName, ContentModel.ASSOC_CONTAINS, folderName);
        assertEquals("Folder " + folderMessageName + " should exist!", 1, folderAssocs.size());
        NodeRef folderNode = folderAssocs.get(0).getChildRef();
        assertEquals(folderMessageName + " node is wrong type!", ContentModel.TYPE_FOLDER, nodeService.getType(folderNode));
        assertEquals(folderMessageName + " node has wrong name!", folderName.getLocalName(),
                nodeService.getProperty(folderNode, ContentModel.PROP_NAME));
        return folderNode;
    }

    private NodeRef makeNode(NodeRef parent, QName nodeType)
    {
        String uuid = GUID.generate();
        Map<QName, Serializable> props = new HashMap<QName, Serializable>();
        props.put(ContentModel.PROP_NAME, uuid);
        ChildAssociationRef assoc = nodeService.createNode(parent, ContentModel.ASSOC_CONTAINS, QName.createQName(
                NamespaceService.CONTENT_MODEL_1_0_URI, uuid), nodeType, props);
        return assoc.getChildRef();
    }

}
