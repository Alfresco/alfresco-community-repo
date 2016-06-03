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
package org.alfresco.repo.action.executer;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import org.alfresco.model.ApplicationModel;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.action.ActionImpl;
import org.alfresco.service.cmr.model.FileFolderService;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;
import org.alfresco.test_category.BaseSpringTestsCategory;
import org.alfresco.util.BaseAlfrescoSpringTest;
import org.alfresco.util.GUID;
import org.junit.experimental.categories.Category;

/**
 * Transition simple workflow action executer unit test
 * 
 * @author Roy Wetherall
 */
@Category(BaseSpringTestsCategory.class)
public class TransitionSimpleWorkflowActionExecuterTest extends BaseAlfrescoSpringTest
{    
    private FileFolderService fileFolderService;
    
    /**
     * The test node reference
     */
    private NodeRef sourceFolder;
    private NodeRef destinationFolder;
    private NodeRef node;
    
    /**
     * The action executer
     */
    private TransitionSimpleWorkflowActionExecuter acceptExecuter;
    private TransitionSimpleWorkflowActionExecuter rejectExecuter;
    
    /**
     * Id used to identify the test action created
     */
    private final static String ID = GUID.generate();
    
    /**
     * Called at the begining of all tests
     */
    @SuppressWarnings("deprecation")
    @Override
    protected void onSetUpInTransaction() throws Exception
    {
        super.onSetUpInTransaction();
        
        fileFolderService = (FileFolderService)this.applicationContext.getBean("fileFolderService");
        
        // Create the node used for tests
        NodeRef container = this.nodeService.createNode(
                this.rootNodeRef,
                ContentModel.ASSOC_CHILDREN,
                QName.createQName("{test}testnode"),
                ContentModel.TYPE_CONTENT).getChildRef();
        
        sourceFolder = fileFolderService.create(container, "my source folder", ContentModel.TYPE_FOLDER).getNodeRef();
        destinationFolder = fileFolderService.create(container, "my destination folder", ContentModel.TYPE_FOLDER).getNodeRef();
        node = fileFolderService.create(sourceFolder, "my node.txt", ContentModel.TYPE_CONTENT).getNodeRef();
        
        // Get the executer instance 
        this.acceptExecuter = (TransitionSimpleWorkflowActionExecuter)this.applicationContext.getBean("accept-simpleworkflow");
        this.rejectExecuter = (TransitionSimpleWorkflowActionExecuter)this.applicationContext.getBean("reject-simpleworkflow");       
    }

    public void testExecutionApprove()
    {
        addWorkflowAspect(node, destinationFolder, Boolean.TRUE, Boolean.FALSE);
        
        assertTrue(nodeService.hasAspect(node, ApplicationModel.ASPECT_SIMPLE_WORKFLOW));
        NodeRef pParent = nodeService.getPrimaryParent(node).getParentRef();
        assertEquals(sourceFolder, pParent);
        
        ActionImpl action = new ActionImpl(null, ID, "accept-simpleworkflow", null);
        acceptExecuter.execute(action, node);
        
        assertFalse(nodeService.hasAspect(node, ApplicationModel.ASPECT_SIMPLE_WORKFLOW));
        pParent = nodeService.getPrimaryParent(node).getParentRef();
        assertEquals(destinationFolder, pParent);
    }
    
    public void testExecutionReject()
    {
        addWorkflowAspect(node, destinationFolder, Boolean.TRUE, Boolean.FALSE);
        
        assertTrue(nodeService.hasAspect(node, ApplicationModel.ASPECT_SIMPLE_WORKFLOW));
        NodeRef pParent = nodeService.getPrimaryParent(node).getParentRef();
        assertEquals(sourceFolder, pParent);
        assertEquals(0, nodeService.getChildAssocs(destinationFolder).size());
        
        ActionImpl action = new ActionImpl(null, ID, "reject-simpleworkflow", null);
        rejectExecuter.execute(action, node);
        
        assertFalse(nodeService.hasAspect(node, ApplicationModel.ASPECT_SIMPLE_WORKFLOW));
        pParent = nodeService.getPrimaryParent(node).getParentRef();
        assertEquals(sourceFolder, pParent);        
        assertEquals(1, nodeService.getChildAssocs(destinationFolder).size());
    }
    
    /** Test for MNT-14730*/
    public void testExecutionApproveWhenDestinationSameAsSource()
    {
        addWorkflowAspect(node, sourceFolder, Boolean.FALSE, Boolean.FALSE);
        
        assertTrue(nodeService.hasAspect(node, ApplicationModel.ASPECT_SIMPLE_WORKFLOW));
        NodeRef pParent = nodeService.getPrimaryParent(node).getParentRef();
        assertEquals(sourceFolder, pParent);
        
        ActionImpl action = new ActionImpl(null, ID, "accept-simpleworkflow", null);
        acceptExecuter.execute(action, node);
        
        String copyName = QName.createValidLocalName("Copy of my node.txt");
        NodeRef nodeRef = nodeService.getChildByName(sourceFolder, ContentModel.ASSOC_CONTAINS, copyName);
        assertNotNull(nodeRef);
    }
    
    /** Test for MNT-14730*/
    public void testExecutionRejectWhenDestinationSameAsSource()
    {
        addWorkflowAspect(node, sourceFolder, Boolean.FALSE, Boolean.FALSE);
        
        assertTrue(nodeService.hasAspect(node, ApplicationModel.ASPECT_SIMPLE_WORKFLOW));
        NodeRef pParent = nodeService.getPrimaryParent(node).getParentRef();
        assertEquals(sourceFolder, pParent);
        assertEquals(0, nodeService.getChildAssocs(destinationFolder).size());
        
        ActionImpl action = new ActionImpl(null, ID, "reject-simpleworkflow", null);
        rejectExecuter.execute(action, node);
        
        assertFalse(nodeService.hasAspect(node, ApplicationModel.ASPECT_SIMPLE_WORKFLOW));
        pParent = nodeService.getPrimaryParent(node).getParentRef();
        assertEquals(sourceFolder, pParent);        
        assertEquals(0, nodeService.getChildAssocs(destinationFolder).size());
        
        String copyName = QName.createValidLocalName("Copy of my node.txt");
        NodeRef nodeRef = nodeService.getChildByName(sourceFolder, ContentModel.ASSOC_CONTAINS, copyName);
        assertNotNull(nodeRef);
    }

    private void addWorkflowAspect(NodeRef node, NodeRef destinationFolder, Boolean moveOnApprove, Boolean moveOnReject)
    {
        // Set up workflow details on the node
        Map<QName, Serializable> propertyValues = createWorkflowProperties(destinationFolder, moveOnApprove, moveOnReject);

        // Apply the simple workflow aspect to the node
        this.nodeService.addAspect(node, ApplicationModel.ASPECT_SIMPLE_WORKFLOW, propertyValues);
    }

    private Map<QName, Serializable> createWorkflowProperties(NodeRef destinationFolder, Boolean moveOnApprove, Boolean moveOnReject)
    {
        Map<QName, Serializable> propertyValues = new HashMap<QName, Serializable>();
        propertyValues.put(ApplicationModel.PROP_APPROVE_STEP, "Approve");
        propertyValues.put(ApplicationModel.PROP_APPROVE_FOLDER, destinationFolder);
        propertyValues.put(ApplicationModel.PROP_APPROVE_MOVE, moveOnApprove);
        propertyValues.put(ApplicationModel.PROP_REJECT_STEP, "Reject");
        propertyValues.put(ApplicationModel.PROP_REJECT_FOLDER, destinationFolder);
        propertyValues.put(ApplicationModel.PROP_REJECT_MOVE, moveOnReject);
        return propertyValues;
    }
}
