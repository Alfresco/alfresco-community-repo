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
package org.alfresco.repo.webservice;

import java.util.List;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.content.MimetypeMap;
import org.alfresco.repo.security.authentication.AuthenticationComponent;
import org.alfresco.repo.webservice.repository.UpdateResult;
import org.alfresco.repo.webservice.types.CML;
import org.alfresco.repo.webservice.types.CMLAddAspect;
import org.alfresco.repo.webservice.types.CMLAddChild;
import org.alfresco.repo.webservice.types.CMLCopy;
import org.alfresco.repo.webservice.types.CMLCreate;
import org.alfresco.repo.webservice.types.CMLCreateAssociation;
import org.alfresco.repo.webservice.types.CMLDelete;
import org.alfresco.repo.webservice.types.CMLMove;
import org.alfresco.repo.webservice.types.CMLRemoveAspect;
import org.alfresco.repo.webservice.types.CMLRemoveAssociation;
import org.alfresco.repo.webservice.types.CMLRemoveChild;
import org.alfresco.repo.webservice.types.CMLUpdate;
import org.alfresco.repo.webservice.types.CMLWriteContent;
import org.alfresco.repo.webservice.types.ContentFormat;
import org.alfresco.repo.webservice.types.NamedValue;
import org.alfresco.repo.webservice.types.ParentReference;
import org.alfresco.repo.webservice.types.Predicate;
import org.alfresco.repo.webservice.types.Reference;
import org.alfresco.service.cmr.repository.AssociationRef;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.ContentData;
import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.service.cmr.repository.ContentService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.search.SearchService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.BaseSpringTest;
import org.alfresco.util.PropertyMap;

/**
 * @author Roy Wetherall
 */
public class CMLUtilTest extends BaseSpringTest
{
    private static final ContentData CONTENT_DATA_TEXT_UTF8 = new ContentData(null, MimetypeMap.MIMETYPE_TEXT_PLAIN, 0L, "UTF-8");
    private static final ContentData CONTENT_DATA_HTML_UTF16 = new ContentData(null, MimetypeMap.MIMETYPE_HTML, 0L, "UTF-16");
    private static final String TEST_CONTENT = "This is some test content";
    
    private CMLUtil cmlUtil;
    private NodeService nodeService;
    private StoreRef testStoreRef;
    private NodeRef rootNodeRef;
    private NodeRef nodeRef;
    private NamespaceService namespaceService;
    private SearchService searchService;
    private NodeRef folderNodeRef;
    private AuthenticationComponent authenticationComponent;
    private ContentService contentService;

    @Override
    protected String[] getConfigLocations()
    {
        return new String[]{"classpath:org/alfresco/repo/webservice/cml-test.xml"};
    }
    
    @Override
    protected void onSetUpInTransaction() throws Exception
    {
        this.cmlUtil = (CMLUtil)this.applicationContext.getBean("CMLUtil");
        this.nodeService = (NodeService)this.applicationContext.getBean("nodeService");
        this.searchService = (SearchService)this.applicationContext.getBean("searchService");
        this.namespaceService = (NamespaceService)this.applicationContext.getBean("namespaceService");
        this.authenticationComponent = (AuthenticationComponent) this.applicationContext.getBean("authenticationComponent");
        this.contentService = (ContentService)this.applicationContext.getBean("contentService");
        
        this.authenticationComponent.setSystemUserAsCurrentUser();
        
        // Create the store and get the root node
        this.testStoreRef = this.nodeService.createStore(
                StoreRef.PROTOCOL_WORKSPACE, "Test_"
                        + System.currentTimeMillis());
        this.rootNodeRef = this.nodeService.getRootNode(this.testStoreRef);
        
        // Create the node used for tests
        PropertyMap contentProps = new PropertyMap();
        contentProps.put(ContentModel.PROP_CONTENT, CONTENT_DATA_TEXT_UTF8);
        this.nodeRef = this.nodeService.createNode(
                this.rootNodeRef,
                ContentModel.ASSOC_CHILDREN,
                ContentModel.ASSOC_CHILDREN,
                ContentModel.TYPE_CONTENT,
                contentProps).getChildRef();
        
        this.folderNodeRef = this.nodeService.createNode(
                this.rootNodeRef,
                ContentModel.ASSOC_CHILDREN,
                ContentModel.ASSOC_CHILDREN,
                ContentModel.TYPE_FOLDER).getChildRef();
    }
    
    @Override
    protected void onTearDownInTransaction() throws Exception
    {
        authenticationComponent.clearCurrentSecurityContext();
        super.onTearDownInTransaction();
    }
    
    public void testMoreThanOneStatement()
    {
        
    }
    
    public void testCreate()
    {        
        CMLCreate[] creates = new CMLCreate[]{createCMLCreate(ContentModel.TYPE_CONTENT, "id1")};
        
        CML cml = new CML();
        cml.setCreate(creates);
        
        UpdateResult[] result = this.cmlUtil.executeCML(cml);
        assertNotNull(result);
        assertEquals(1, result.length);
        
        UpdateResult updateResult = result[0];
        assertEquals("create", updateResult.getStatement());
        assertNull(updateResult.getSource());
        assertNotNull(updateResult.getDestination());
        NodeRef createdNodeRef =  Utils.convertToNodeRef(updateResult.getDestination(), this.nodeService, this.searchService, this.namespaceService);
        assertNotNull(createdNodeRef);
        
        assertEquals(ContentModel.TYPE_CONTENT, this.nodeService.getType(createdNodeRef));
        assertEquals("name", this.nodeService.getProperty(createdNodeRef, ContentModel.PROP_NAME));
        
        //System.out.println(NodeStoreInspector.dumpNodeStore(this.nodeService, this.testStoreRef));
    }
    
    public void testAddRemoveAspect()
    {
        CMLAddAspect addAspect = new CMLAddAspect();
        addAspect.setAspect(ContentModel.ASPECT_VERSIONABLE.toString());
        addAspect.setWhere(createPredicate(this.nodeRef));
        
        CML cml = new CML();
        cml.setAddAspect(new CMLAddAspect[]{addAspect});
        
        UpdateResult[] result = this.cmlUtil.executeCML(cml);
        assertNotNull(result);
        assertEquals(1, result.length);
        
        UpdateResult updateResult = result[0];
        assertEquals("addAspect", updateResult.getStatement());
        assertNotNull(updateResult.getSource());
        assertNotNull(updateResult.getDestination());
        
        assertTrue(this.nodeService.hasAspect(this.nodeRef, ContentModel.ASPECT_VERSIONABLE));
        
        // TODO should test with properties set as well
        
        CMLRemoveAspect removeAspect = new CMLRemoveAspect();
        removeAspect.setAspect(ContentModel.ASPECT_VERSIONABLE.toString());
        removeAspect.setWhere(createPredicate(this.nodeRef));
        
        CML cml2 = new CML();
        cml2.setRemoveAspect(new CMLRemoveAspect[]{removeAspect});
        
        UpdateResult[] results2 = this.cmlUtil.executeCML(cml2);
        assertNotNull(results2);
        assertEquals(1, results2.length);
        
        UpdateResult result2 = results2[0];
        assertEquals("removeAspect", result2.getStatement());
        assertNotNull(result2.getDestination());
        assertNotNull(result2.getSource());
        
        assertFalse(this.nodeService.hasAspect(this.nodeRef, ContentModel.ASPECT_VERSIONABLE));
        
    }
    
    public void testUpdate()
    {
        CMLUpdate update = new CMLUpdate();
        update.setWhere(createPredicate(this.nodeRef));
        update.setProperty(new NamedValue[]
        {
                new NamedValue(ContentModel.PROP_NAME.toString(), false, "updatedName", null),
                new NamedValue(ContentModel.PROP_CONTENT.toString(), false, CONTENT_DATA_HTML_UTF16.toString(), null)
        });
        
        CML cml = new CML();
        cml.setUpdate(new CMLUpdate[]{update});
        
        UpdateResult[] result = this.cmlUtil.executeCML(cml);
        assertNotNull(result);
        assertEquals(1, result.length);
        
        UpdateResult updateResult = result[0];
        assertEquals("update", updateResult.getStatement());
        assertNotNull(updateResult.getSource());
        assertNotNull(updateResult.getDestination());
        
        assertEquals("updatedName", this.nodeService.getProperty(this.nodeRef, ContentModel.PROP_NAME));
        assertEquals(CONTENT_DATA_HTML_UTF16, this.nodeService.getProperty(this.nodeRef, ContentModel.PROP_CONTENT));
    }
    
    public void testWriteContent()
    {
        CMLWriteContent write = new CMLWriteContent();
        write.setWhere(createPredicate(this.nodeRef));
        write.setProperty(ContentModel.PROP_CONTENT.toString());
        ContentFormat format = new ContentFormat(MimetypeMap.MIMETYPE_TEXT_PLAIN, "UTF-8");
        write.setFormat(format);
        write.setContent(TEST_CONTENT.getBytes());
        
        CML cml = new CML();
        cml.setWriteContent(new CMLWriteContent[]{write});
        
        UpdateResult[] result = this.cmlUtil.executeCML(cml);
        assertNotNull(result);
        assertEquals(1, result.length);
        
        UpdateResult updateResult = result[0];
        assertEquals("writeContent", updateResult.getStatement());
        assertNotNull(updateResult.getSource());
        assertNotNull(updateResult.getDestination());
        
        ContentReader reader = this.contentService.getReader(this.nodeRef, ContentModel.PROP_CONTENT);
        assertNotNull(reader);
        assertEquals(reader.getContentString(), TEST_CONTENT);
    }
    
    public void testDelete()
    {
        CMLDelete delete = new CMLDelete();
        delete.setWhere(createPredicate(this.nodeRef));
        
        CML cml = new CML();
        cml.setDelete(new CMLDelete[]{delete});
        
        UpdateResult[] result = this.cmlUtil.executeCML(cml);
        assertNotNull(result);
        assertEquals(1, result.length);
        
        UpdateResult updateResult = result[0];
        assertEquals("delete", updateResult.getStatement());
        assertNotNull(updateResult.getSource());
        assertNull(updateResult.getDestination());
        
        // Check that the node no longer exists
        assertFalse(this.nodeService.exists(this.nodeRef));
    }
    
    public void testMove()
    {
        CMLMove move = new CMLMove();       
        move.setTo(createParentReference(this.folderNodeRef, ContentModel.ASSOC_CONTAINS, ContentModel.ASSOC_CONTAINS));
        move.setWhere(createPredicate(this.nodeRef));
        
        CML cml = new CML();
        cml.setMove(new CMLMove[]{move});
        
        UpdateResult[] result = this.cmlUtil.executeCML(cml);
        assertNotNull(result);
        assertEquals(1, result.length);
        
        UpdateResult updateResult = result[0];
        assertEquals("move", updateResult.getStatement());
        assertNotNull(updateResult.getSource());
        assertNotNull(updateResult.getDestination());
        
        List<ChildAssociationRef> assocs = this.nodeService.getChildAssocs(this.folderNodeRef);
        assertNotNull(assocs);
        assertEquals(1, assocs.size());
        ChildAssociationRef assoc = assocs.get(0);
        assertEquals(assoc.getChildRef(), Utils.convertToNodeRef(
                                                updateResult.getDestination(),
                                                this.nodeService,
                                                this.searchService,
                                                this.namespaceService));
    }
    
    public void testCopy()
    {
        CMLCopy copy = new CMLCopy();       
        copy.setTo(createParentReference(this.folderNodeRef, ContentModel.ASSOC_CONTAINS, ContentModel.ASSOC_CONTAINS));
        copy.setWhere(createPredicate(this.nodeRef));
        
        CML cml = new CML();
        cml.setCopy(new CMLCopy[]{copy});
        
        UpdateResult[] result = this.cmlUtil.executeCML(cml);
        assertNotNull(result);
        assertEquals(1, result.length);
        
        UpdateResult updateResult = result[0];
        assertEquals("copy", updateResult.getStatement());
        assertNotNull(updateResult.getSource());
        assertNotNull(updateResult.getDestination());
        
        List<ChildAssociationRef> assocs = this.nodeService.getChildAssocs(this.folderNodeRef);
        assertNotNull(assocs);
        assertEquals(1, assocs.size());
        ChildAssociationRef assoc = assocs.get(0);
        assertEquals(assoc.getChildRef(), Utils.convertToNodeRef(
                                                updateResult.getDestination(),
                                                this.nodeService,
                                                this.searchService,
                                                this.namespaceService));
    }
    
    public void testAddChild()
    {
        CMLAddChild addChild = new CMLAddChild();
        addChild.setTo(createParentReference(this.folderNodeRef, ContentModel.ASSOC_CONTAINS, ContentModel.ASSOC_CONTAINS));
        addChild.setWhere(createPredicate(this.nodeRef));
        
        CML cml = new CML();
        cml.setAddChild(new CMLAddChild[]{addChild});
        
        UpdateResult[] result = this.cmlUtil.executeCML(cml);
        assertNotNull(result);
        assertEquals(1, result.length);
        
        UpdateResult updateResult = result[0];
        assertEquals("addChild", updateResult.getStatement());
        assertNotNull(updateResult.getSource());
        assertNotNull(updateResult.getDestination());
        
        List<ChildAssociationRef> assocs = this.nodeService.getChildAssocs(this.folderNodeRef);
        assertNotNull(assocs);
        assertEquals(1, assocs.size());
        ChildAssociationRef assoc = assocs.get(0);
        assertEquals(assoc.getChildRef(), Utils.convertToNodeRef(
                                                updateResult.getDestination(),
                                                this.nodeService,
                                                this.searchService,
                                                this.namespaceService));
    }
    
    public void testRemoveChild()
    {
        // Add the node as a child of the folder
        this.nodeService.addChild(this.folderNodeRef, this.nodeRef, ContentModel.ASSOC_CONTAINS, ContentModel.ASSOC_CONTAINS);
        
        CMLRemoveChild removeChild = new CMLRemoveChild();
        removeChild.setFrom(Utils.convertToReference(this.nodeService, this.namespaceService, this.folderNodeRef));
        removeChild.setWhere(createPredicate(this.nodeRef));
        
        CML cml = new CML();
        cml.setRemoveChild(new CMLRemoveChild[]{removeChild});
        
        UpdateResult[] result = this.cmlUtil.executeCML(cml);
        assertNotNull(result);
        assertEquals(1, result.length);
        
        UpdateResult updateResult = result[0];
        assertEquals("removeChild", updateResult.getStatement());
        assertNotNull(updateResult.getSource());
        assertNull(updateResult.getDestination());
        
        List<ChildAssociationRef> assocs = this.nodeService.getChildAssocs(this.folderNodeRef);
        assertEquals(0, assocs.size());
    }
    
    public void testCreateAssociation()
    {
        CMLCreateAssociation createAssoc = new CMLCreateAssociation();
        createAssoc.setAssociation(ContentModel.ASSOC_CONTAINS.toString());
        createAssoc.setFrom(createPredicate(this.folderNodeRef));
        createAssoc.setTo(createPredicate(this.nodeRef));
        
        CML cml = new CML();
        cml.setCreateAssociation(new CMLCreateAssociation[]{createAssoc});
        
        UpdateResult[] result = this.cmlUtil.executeCML(cml);
        assertNotNull(result);
        assertEquals(1, result.length);
        
        UpdateResult updateResult = result[0];
        assertEquals("createAssociation", updateResult.getStatement());
        assertNotNull(updateResult.getSource());
        assertNotNull(updateResult.getDestination());
        
        List<AssociationRef> assocs = this.nodeService.getTargetAssocs(this.folderNodeRef, ContentModel.ASSOC_CONTAINS);
        assertNotNull(assocs);
        assertEquals(1, assocs.size());
        AssociationRef assoc = assocs.get(0);
        assertEquals(assoc.getTargetRef(), Utils.convertToNodeRef(
                                                updateResult.getDestination(),
                                                this.nodeService,
                                                this.searchService,
                                                this.namespaceService));
    }
    
    public void testRemoveAssociation()
    {
        this.nodeService.createAssociation(this.folderNodeRef, this.nodeRef, ContentModel.ASSOC_CONTAINS);
        
        CMLRemoveAssociation removeAssociation = new CMLRemoveAssociation();
        removeAssociation.setAssociation(ContentModel.ASSOC_CONTAINS.toString());
        removeAssociation.setFrom(createPredicate(this.folderNodeRef));
        removeAssociation.setTo(createPredicate(this.nodeRef));
        
        CML cml = new CML();
        cml.setRemoveAssociation(new CMLRemoveAssociation[]{removeAssociation});
        
        UpdateResult[] result = this.cmlUtil.executeCML(cml);
        assertNotNull(result);
        assertEquals(1, result.length);
        
        UpdateResult updateResult = result[0];
        assertEquals("removeAssociation", updateResult.getStatement());
        assertNotNull(updateResult.getSource());
        assertNotNull(updateResult.getDestination());
        
        List<AssociationRef> assocs = this.nodeService.getTargetAssocs(this.folderNodeRef, ContentModel.ASSOC_CONTAINS);
        assertNotNull(assocs);
        assertEquals(0, assocs.size());
    }
    
    private ParentReference createParentReference(NodeRef nodeRef, QName assocType, QName assocName)
    {
        ParentReference parentReference = new ParentReference();
        parentReference.setAssociationType(assocType.toString());
        parentReference.setChildName(assocName.toString());
        parentReference.setStore(Utils.convertToStore(nodeRef.getStoreRef()));
        parentReference.setUuid(nodeRef.getId());
        return parentReference;
    }
    
    private Predicate createPredicate(NodeRef nodeRef)
    {
        Predicate predicate = new Predicate();
        predicate.setStore(Utils.convertToStore(nodeRef.getStoreRef()));
        predicate.setNodes(new Reference[]{Utils.convertToReference(this.nodeService, this.namespaceService, nodeRef)});
        return predicate;
    }

    private CMLCreate createCMLCreate(QName type, String id)
    {
        CMLCreate create = new CMLCreate();
        create.setId("id1");
        create.setType(ContentModel.TYPE_CONTENT.toString());
        
        ParentReference parentReference = new ParentReference();
        parentReference.setAssociationType(ContentModel.ASSOC_CHILDREN.toString());
        parentReference.setChildName(ContentModel.ASSOC_CHILDREN.toString());
        parentReference.setStore(Utils.convertToStore(this.testStoreRef));
        parentReference.setUuid(this.rootNodeRef.getId());
        
        create.setParent(parentReference);
        create.setProperty(getContentNamedValues());
        
        return create;
    }
    
    private NamedValue[] getContentNamedValues()
    {
        return new NamedValue[]
          {
                new NamedValue(ContentModel.PROP_NAME.toString(), false, "name", null),
                new NamedValue(ContentModel.PROP_CONTENT.toString(), false, CONTENT_DATA_TEXT_UTF8.toString(), null)
          };
    }
}
