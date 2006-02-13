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
package org.alfresco.repo.action;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.action.evaluator.ComparePropertyValueEvaluator;
import org.alfresco.repo.action.evaluator.InCategoryEvaluator;
import org.alfresco.repo.action.evaluator.NoConditionEvaluator;
import org.alfresco.repo.action.evaluator.compare.ComparePropertyValueOperation;
import org.alfresco.repo.action.executer.AddFeaturesActionExecuter;
import org.alfresco.repo.action.executer.CheckInActionExecuter;
import org.alfresco.repo.action.executer.CheckOutActionExecuter;
import org.alfresco.repo.action.executer.CompositeActionExecuter;
import org.alfresco.repo.action.executer.MoveActionExecuter;
import org.alfresco.repo.content.MimetypeMap;
import org.alfresco.repo.transaction.TransactionUtil;
import org.alfresco.service.cmr.action.Action;
import org.alfresco.service.cmr.action.ActionCondition;
import org.alfresco.service.cmr.action.ActionConditionDefinition;
import org.alfresco.service.cmr.action.ActionDefinition;
import org.alfresco.service.cmr.action.ActionService;
import org.alfresco.service.cmr.action.CompositeAction;
import org.alfresco.service.cmr.repository.ContentData;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.namespace.QName;
import org.alfresco.service.transaction.TransactionService;
import org.alfresco.util.BaseAlfrescoSpringTest;
import org.alfresco.util.BaseSpringTest;

/**
 * Action service test
 * 
 * @author Roy Wetherall
 */
public class ActionServiceImplTest extends BaseAlfrescoSpringTest
{
	private static final String BAD_NAME = "badName";
    
	private NodeRef nodeRef;
    
	@Override
	protected void onSetUpInTransaction() throws Exception
	{
		super.onSetUpInTransaction();

        // Create the node used for tests
        this.nodeRef = this.nodeService.createNode(
        		this.rootNodeRef,
                ContentModel.ASSOC_CHILDREN,
                QName.createQName("{test}testnode"),
                ContentModel.TYPE_CONTENT).getChildRef();
        this.nodeService.setProperty(
                this.nodeRef,
                ContentModel.PROP_CONTENT,
                new ContentData(null, MimetypeMap.MIMETYPE_TEXT_PLAIN, 0L, null));
	}
	
	/**
	 * Test getActionDefinition
	 */
	public void testGetActionDefinition()
	{
		ActionDefinition action = actionService.getActionDefinition(AddFeaturesActionExecuter.NAME);
		assertNotNull(action);
		assertEquals(AddFeaturesActionExecuter.NAME, action.getName());
		
		ActionConditionDefinition nullCondition = this.actionService.getActionConditionDefinition(BAD_NAME);
		assertNull(nullCondition);		
	}
    
	/**
	 * Test getActionDefintions
	 */
	public void testGetActionDefinitions()
	{
		List<ActionDefinition> defintions = this.actionService.getActionDefinitions();
		assertNotNull(defintions);
		assertFalse(defintions.isEmpty());
        
        for (ActionDefinition definition : defintions)
        {
            System.out.println(definition.getTitle());
        }
	}	

	/**
	 * Test getActionConditionDefinition
	 */
	public void testGetActionConditionDefinition()
	{
		ActionConditionDefinition condition = this.actionService.getActionConditionDefinition(NoConditionEvaluator.NAME);
		assertNotNull(condition);
		assertEquals(NoConditionEvaluator.NAME, condition.getName());
		
		ActionConditionDefinition nullCondition = this.actionService.getActionConditionDefinition(BAD_NAME);
		assertNull(nullCondition);	
	}

	/**
	 * Test getActionConditionDefinitions
	 *
	 */
	public void testGetActionConditionDefinitions()
	{
		List<ActionConditionDefinition> defintions = this.actionService.getActionConditionDefinitions();
		assertNotNull(defintions);
		assertFalse(defintions.isEmpty());
        
        for (ActionConditionDefinition definition : defintions)
        {
            System.out.println(definition.getTitle());
        }
	}

	/**
	 * Test create action condition
	 */
	public void testCreateActionCondition()
	{
		ActionCondition condition = this.actionService.createActionCondition(NoConditionEvaluator.NAME);
		assertNotNull(condition);
		assertEquals(NoConditionEvaluator.NAME, condition.getActionConditionDefinitionName());
	}

	/**
	 * Test createAction
	 */
	public void testCreateAction()
	{
		Action action = this.actionService.createAction(AddFeaturesActionExecuter.NAME);
		assertNotNull(action);
		assertEquals(AddFeaturesActionExecuter.NAME, action.getActionDefinitionName());
	}

	/**
	 * Test createCompositeAction
	 */
	public void testCreateCompositeAction()
	{
		CompositeAction action = this.actionService.createCompositeAction();
		assertNotNull(action);
		assertEquals(CompositeActionExecuter.NAME, action.getActionDefinitionName());
	}

	/**
	 * Evaluate action
	 */
	public void testEvaluateAction()
	{
		Action action = this.actionService.createAction(AddFeaturesActionExecuter.NAME);
		assertTrue(this.actionService.evaluateAction(action, this.nodeRef));
		
		ActionCondition condition = this.actionService.createActionCondition(ComparePropertyValueEvaluator.NAME);
		condition.setParameterValue(ComparePropertyValueEvaluator.PARAM_VALUE, "*.doc");
		action.addActionCondition(condition);
		
		assertFalse(this.actionService.evaluateAction(action, this.nodeRef));
		this.nodeService.setProperty(this.nodeRef, ContentModel.PROP_NAME, "myDocument.doc");
		assertTrue(this.actionService.evaluateAction(action, this.nodeRef));
		
		ActionCondition condition2 = this.actionService.createActionCondition(ComparePropertyValueEvaluator.NAME);
		condition2.setParameterValue(ComparePropertyValueEvaluator.PARAM_VALUE, "my");
		action.addActionCondition(condition2);
		assertTrue(this.actionService.evaluateAction(action, this.nodeRef));
		
		this.nodeService.setProperty(this.nodeRef, ContentModel.PROP_NAME, "document.doc");
		assertFalse(this.actionService.evaluateAction(action, this.nodeRef));
	}
	
	/**
	 * Test evaluate action condition
	 */
	public void testEvaluateActionCondition()
	{
		ActionCondition condition = this.actionService.createActionCondition(ComparePropertyValueEvaluator.NAME);
		condition.setParameterValue(ComparePropertyValueEvaluator.PARAM_VALUE, "*.doc");
		
		assertFalse(this.actionService.evaluateActionCondition(condition, this.nodeRef));
		this.nodeService.setProperty(this.nodeRef, ContentModel.PROP_NAME, "myDocument.doc");
		assertTrue(this.actionService.evaluateActionCondition(condition, this.nodeRef));
        
        // Check that inverting the condition has the correct effect
        condition.setInvertCondition(true);
        assertFalse(this.actionService.evaluateActionCondition(condition, this.nodeRef));
	}
	
	/**
	 * Test execute action
	 */
	public void testExecuteAction()
	{
		assertFalse(this.nodeService.hasAspect(this.nodeRef, ContentModel.ASPECT_VERSIONABLE));
		
		Action action = this.actionService.createAction(AddFeaturesActionExecuter.NAME);
		action.setParameterValue(AddFeaturesActionExecuter.PARAM_ASPECT_NAME, ContentModel.ASPECT_VERSIONABLE);
		
		this.actionService.executeAction(action, this.nodeRef);
		assertTrue(this.nodeService.hasAspect(this.nodeRef, ContentModel.ASPECT_VERSIONABLE));
		
		this.nodeService.removeAspect(this.nodeRef, ContentModel.ASPECT_VERSIONABLE);
		assertFalse(this.nodeService.hasAspect(this.nodeRef, ContentModel.ASPECT_VERSIONABLE));
		
		ActionCondition condition = this.actionService.createActionCondition(ComparePropertyValueEvaluator.NAME);
		condition.setParameterValue(ComparePropertyValueEvaluator.PARAM_VALUE, "*.doc");
		action.addActionCondition(condition);
				
		this.actionService.executeAction(action, this.nodeRef);
		assertFalse(this.nodeService.hasAspect(this.nodeRef, ContentModel.ASPECT_VERSIONABLE));
		
		this.actionService.executeAction(action, this.nodeRef, true);
		assertFalse(this.nodeService.hasAspect(this.nodeRef, ContentModel.ASPECT_VERSIONABLE));
		
		this.actionService.executeAction(action, this.nodeRef, false);
		assertTrue(this.nodeService.hasAspect(this.nodeRef, ContentModel.ASPECT_VERSIONABLE));
		
		this.nodeService.removeAspect(this.nodeRef, ContentModel.ASPECT_VERSIONABLE);
		assertFalse(this.nodeService.hasAspect(this.nodeRef, ContentModel.ASPECT_VERSIONABLE));
		
		this.nodeService.setProperty(this.nodeRef, ContentModel.PROP_NAME, "myDocument.doc");
		this.actionService.executeAction(action, this.nodeRef);
		assertTrue(this.nodeService.hasAspect(this.nodeRef, ContentModel.ASPECT_VERSIONABLE));
		
		this.nodeService.removeAspect(this.nodeRef, ContentModel.ASPECT_VERSIONABLE);
		assertFalse(this.nodeService.hasAspect(this.nodeRef, ContentModel.ASPECT_VERSIONABLE));
		
		this.nodeService.removeAspect(this.nodeRef, ContentModel.ASPECT_VERSIONABLE);
		assertFalse(this.nodeService.hasAspect(this.nodeRef, ContentModel.ASPECT_VERSIONABLE));
		
		// Create the composite action
		Action action1 = this.actionService.createAction(AddFeaturesActionExecuter.NAME);
		action1.setParameterValue(AddFeaturesActionExecuter.PARAM_ASPECT_NAME, ContentModel.ASPECT_LOCKABLE);
		Action action2 = this.actionService.createAction(AddFeaturesActionExecuter.NAME);
		action2.setParameterValue(AddFeaturesActionExecuter.PARAM_ASPECT_NAME, ContentModel.ASPECT_VERSIONABLE);		
		CompositeAction compAction = this.actionService.createCompositeAction();
		compAction.setTitle("title");
		compAction.setDescription("description");
		compAction.addAction(action1);
		compAction.addAction(action2);
		
		// Execute the composite action
		this.actionService.executeAction(compAction, this.nodeRef);
		
		assertTrue(this.nodeService.hasAspect(this.nodeRef, ContentModel.ASPECT_LOCKABLE));
		assertTrue(this.nodeService.hasAspect(this.nodeRef, ContentModel.ASPECT_VERSIONABLE));
	}	
	
	public void testGetAndGetAllWithNoActions()
	{
		assertNull(this.actionService.getAction(this.nodeRef, AddFeaturesActionExecuter.NAME));
		List<Action> actions = this.actionService.getActions(this.nodeRef);
		assertNotNull(actions);
		assertEquals(0, actions.size());
	}
	
	/**
	 * Test saving an action with no conditions.  Includes testing storage and retrieval 
	 * of compensating actions.
	 */
	public void testSaveActionNoCondition()
	{
		// Create the action
		Action action = this.actionService.createAction(AddFeaturesActionExecuter.NAME);
		String actionId = action.getId();
		
		// Set the parameters of the action
		action.setParameterValue(AddFeaturesActionExecuter.PARAM_ASPECT_NAME, ContentModel.ASPECT_VERSIONABLE);
		
		// Set the title and description of the action
		action.setTitle("title");
		action.setDescription("description");
		action.setExecuteAsynchronously(true);
				
		// Save the action
		this.actionService.saveAction(this.nodeRef, action);
		
		// Get the action
		Action savedAction = this.actionService.getAction(this.nodeRef, actionId);
		
		// Check the action 
		assertEquals(action.getId(), savedAction.getId());
		assertEquals(action.getActionDefinitionName(), savedAction.getActionDefinitionName());
		
		// Check the properties
		assertEquals("title", savedAction.getTitle());
		assertEquals("description", savedAction.getDescription());
		assertTrue(savedAction.getExecuteAsychronously());
		
		// Check that the compensating action has not been set
		assertNull(savedAction.getCompensatingAction());
		
		// Check the properties
		assertEquals(1, savedAction.getParameterValues().size());
		assertEquals(ContentModel.ASPECT_VERSIONABLE, savedAction.getParameterValue(AddFeaturesActionExecuter.PARAM_ASPECT_NAME));
				
		// Check the conditions
		assertNotNull(savedAction.getActionConditions());
		assertEquals(0, savedAction.getActionConditions().size());
		
		// Edit the properties of the action		
		Map<QName, Serializable> properties = new HashMap<QName, Serializable>(1);
		properties.put(ContentModel.PROP_NAME, "testName");
		action.setParameterValue(AddFeaturesActionExecuter.PARAM_ASPECT_PROPERTIES, (Serializable)properties);
		action.setParameterValue(AddFeaturesActionExecuter.PARAM_ASPECT_NAME, ContentModel.ASPECT_AUDITABLE);
		
		// Set the compensating action
		Action compensatingAction = this.actionService.createAction(AddFeaturesActionExecuter.NAME);
		compensatingAction.setParameterValue(AddFeaturesActionExecuter.PARAM_ASPECT_NAME, ContentModel.ASPECT_VERSIONABLE);
		action.setCompensatingAction(compensatingAction);
		
		this.actionService.saveAction(this.nodeRef, action);
		Action savedAction2 = this.actionService.getAction(this.nodeRef, actionId);
		
		// Check the updated properties
		assertEquals(2, savedAction2.getParameterValues().size());
		assertEquals(ContentModel.ASPECT_AUDITABLE, savedAction2.getParameterValue(AddFeaturesActionExecuter.PARAM_ASPECT_NAME));
		Map<QName, Serializable> temp = (Map<QName, Serializable>)savedAction2.getParameterValue(AddFeaturesActionExecuter.PARAM_ASPECT_PROPERTIES);
		assertNotNull(temp);
		assertEquals(1, temp.size());
		assertEquals("testName", temp.get(ContentModel.PROP_NAME));
		
		// Check the compensating action
		Action savedCompensatingAction = savedAction2.getCompensatingAction();
		assertNotNull(savedCompensatingAction);
		assertEquals(compensatingAction, savedCompensatingAction);
		assertEquals(AddFeaturesActionExecuter.NAME, savedCompensatingAction.getActionDefinitionName());
		assertEquals(ContentModel.ASPECT_VERSIONABLE, savedCompensatingAction.getParameterValue(AddFeaturesActionExecuter.PARAM_ASPECT_NAME));
		
		// Change the details of the compensating action (edit and remove)
		compensatingAction.setParameterValue(AddFeaturesActionExecuter.PARAM_ASPECT_NAME, ContentModel.ASPECT_CLASSIFIABLE);
		this.actionService.saveAction(this.nodeRef, action);
		Action savedAction3 = this.actionService.getAction(this.nodeRef, actionId);
		Action savedCompensatingAction2 = savedAction3.getCompensatingAction();
		assertNotNull(savedCompensatingAction2);
		assertEquals(compensatingAction, savedCompensatingAction2);
		assertEquals(AddFeaturesActionExecuter.NAME, savedCompensatingAction2.getActionDefinitionName());
		assertEquals(ContentModel.ASPECT_CLASSIFIABLE, savedCompensatingAction2.getParameterValue(AddFeaturesActionExecuter.PARAM_ASPECT_NAME));
		action.setCompensatingAction(null);
		this.actionService.saveAction(this.nodeRef, action);
		Action savedAction4 = this.actionService.getAction(this.nodeRef, actionId);
		assertNull(savedAction4.getCompensatingAction());
		
		//System.out.println(NodeStoreInspector.dumpNodeStore(this.nodeService, this.testStoreRef));
	}

    public void testOwningNodeRef()
    {
        // Create the action
        Action action = this.actionService.createAction(AddFeaturesActionExecuter.NAME);
        String actionId = action.getId();
        
        // Set the parameters of the action
        action.setParameterValue(AddFeaturesActionExecuter.PARAM_ASPECT_NAME, ContentModel.ASPECT_VERSIONABLE);
        
        // Set the title and description of the action
        action.setTitle("title");
        action.setDescription("description");
        action.setExecuteAsynchronously(true);
        
        // Check the owning node ref
        assertNull(action.getOwningNodeRef());
                
        // Save the action
        this.actionService.saveAction(this.nodeRef, action);
        
        // Check the owning node ref
        assertEquals(this.nodeRef, action.getOwningNodeRef());
        
        // Get the action
        Action savedAction = this.actionService.getAction(this.nodeRef, actionId);
        
        // Check the owning node ref
        assertEquals(this.nodeRef, savedAction.getOwningNodeRef());;
    }

	/**
	 * Test saving an action with conditions
	 */
	public void testSaveActionWithConditions()
	{
		// Create the action
		Action action = this.actionService.createAction(AddFeaturesActionExecuter.NAME);
		String actionId = action.getId();
		
		// Set the parameters of the action
		action.setParameterValue(AddFeaturesActionExecuter.PARAM_ASPECT_NAME, ContentModel.ASPECT_VERSIONABLE);
		Map<QName, Serializable> properties = new HashMap<QName, Serializable>(1);
		properties.put(ContentModel.PROP_NAME, "testName");
		action.setParameterValue(AddFeaturesActionExecuter.PARAM_ASPECT_PROPERTIES, (Serializable)properties);
		
		// Set the conditions of the action
		ActionCondition actionCondition = this.actionService.createActionCondition(NoConditionEvaluator.NAME);
        actionCondition.setInvertCondition(true);
		ActionCondition actionCondition2 = this.actionService.createActionCondition(ComparePropertyValueEvaluator.NAME);
		actionCondition2.setParameterValue(ComparePropertyValueEvaluator.PARAM_VALUE, "*.doc");
		action.addActionCondition(actionCondition);
		action.addActionCondition(actionCondition2);
		
		// Save the action
		this.actionService.saveAction(this.nodeRef, action);
		
		// Get the action
		Action savedAction = this.actionService.getAction(this.nodeRef, actionId);
		
		// Check the action 
		assertEquals(action.getId(), savedAction.getId());
		assertEquals(action.getActionDefinitionName(), savedAction.getActionDefinitionName());
		
		// Check the properties
		assertEquals(action.getParameterValues().size(), savedAction.getParameterValues().size());
		assertEquals(ContentModel.ASPECT_VERSIONABLE, savedAction.getParameterValue(AddFeaturesActionExecuter.PARAM_ASPECT_NAME));
		Map<QName, Serializable> temp = (Map<QName, Serializable>)savedAction.getParameterValue(AddFeaturesActionExecuter.PARAM_ASPECT_PROPERTIES);
		assertNotNull(temp);
		assertEquals(1, temp.size());
		assertEquals("testName", temp.get(ContentModel.PROP_NAME));
		
		// Check the conditions
		assertNotNull(savedAction.getActionConditions());
		assertEquals(2, savedAction.getActionConditions().size());
		for (ActionCondition savedCondition : savedAction.getActionConditions())
		{
			if (savedCondition.getActionConditionDefinitionName().equals(NoConditionEvaluator.NAME) == true)
			{
				assertEquals(0, savedCondition.getParameterValues().size());
                assertTrue(savedCondition.getInvertCondition());
			}
			else if (savedCondition.getActionConditionDefinitionName().equals(ComparePropertyValueEvaluator.NAME) == true)
			{
				assertEquals(1, savedCondition.getParameterValues().size());
				assertEquals("*.doc", savedCondition.getParameterValue(ComparePropertyValueEvaluator.PARAM_VALUE));
                assertFalse(savedCondition.getInvertCondition());
			}
			else
			{
				fail("There is a condition here that we are not expecting.");
			}
		}
		
		// Modify the conditions of the action
		ActionCondition actionCondition3 = this.actionService.createActionCondition(InCategoryEvaluator.NAME);
		actionCondition3.setParameterValue(InCategoryEvaluator.PARAM_CATEGORY_ASPECT, ContentModel.ASPECT_OWNABLE);
		action.addActionCondition(actionCondition3);
		action.removeActionCondition(actionCondition);
		actionCondition2.setParameterValue(ComparePropertyValueEvaluator.PARAM_VALUE, "*.exe");
		actionCondition2.setParameterValue(ComparePropertyValueEvaluator.PARAM_OPERATION, ComparePropertyValueOperation.EQUALS);
		
		this.actionService.saveAction(this.nodeRef, action);
		Action savedAction2 = this.actionService.getAction(this.nodeRef, actionId);
		
		// Check that the conditions have been updated correctly
		assertNotNull(savedAction2.getActionConditions());
		assertEquals(2, savedAction2.getActionConditions().size());
		for (ActionCondition savedCondition : savedAction2.getActionConditions())
		{
			if (savedCondition.getActionConditionDefinitionName().equals(InCategoryEvaluator.NAME) == true)
			{
				assertEquals(1, savedCondition.getParameterValues().size());
				assertEquals(ContentModel.ASPECT_OWNABLE, savedCondition.getParameterValue(InCategoryEvaluator.PARAM_CATEGORY_ASPECT));
			}
			else if (savedCondition.getActionConditionDefinitionName().equals(ComparePropertyValueEvaluator.NAME) == true)
			{
				assertEquals(2, savedCondition.getParameterValues().size());
				assertEquals("*.exe", savedCondition.getParameterValue(ComparePropertyValueEvaluator.PARAM_VALUE));
				assertEquals(ComparePropertyValueOperation.EQUALS, savedCondition.getParameterValue(ComparePropertyValueEvaluator.PARAM_OPERATION));
			}
			else
			{
				fail("There is a condition here that we are not expecting.");
			}
		}
		
		//System.out.println(NodeStoreInspector.dumpNodeStore(this.nodeService, this.testStoreRef));
	}
	
	/**
	 * Test saving a composite action
	 */
	public void testSaveCompositeAction()
	{
		Action action1 = this.actionService.createAction(AddFeaturesActionExecuter.NAME);
		Action action2 = this.actionService.createAction(CheckInActionExecuter.NAME);
		
		CompositeAction compositeAction = this.actionService.createCompositeAction();
		String actionId = compositeAction.getId();
		compositeAction.addAction(action1);
		compositeAction.addAction(action2);
		
		this.actionService.saveAction(this.nodeRef, compositeAction);
		assertEquals(1, this.actionService.getActions(this.nodeRef).size());
		CompositeAction savedCompositeAction = (CompositeAction)this.actionService.getAction(this.nodeRef, actionId);
		
		// Check the saved composite action
		assertEquals(2, savedCompositeAction.getActions().size());
		for (Action action : savedCompositeAction.getActions())
		{
			if (action.getActionDefinitionName().equals(AddFeaturesActionExecuter.NAME) == true)
			{
				assertEquals(action, action1);
			}
			else if (action.getActionDefinitionName().equals(CheckInActionExecuter.NAME) == true)
			{
				assertEquals(action, action2);
			}
			else
			{
				fail("We have an action here we are not expecting.");
			}
		}
		
		// Change the actions and re-save
		compositeAction.removeAction(action1);
		Action action3 = this.actionService.createAction(CheckOutActionExecuter.NAME);
		compositeAction.addAction(action3);
		action2.setParameterValue(CheckInActionExecuter.PARAM_DESCRIPTION, "description");
		
		this.actionService.saveAction(this.nodeRef, compositeAction);
		assertEquals(1, this.actionService.getActions(this.nodeRef).size());
		CompositeAction savedCompositeAction2 = (CompositeAction)this.actionService.getAction(this.nodeRef, actionId);
		
		assertEquals(2, savedCompositeAction2.getActions().size());
		for (Action action : savedCompositeAction2.getActions())
		{
			if (action.getActionDefinitionName().equals(CheckOutActionExecuter.NAME) == true)
			{
				assertEquals(action, action3);
			}
			else if (action.getActionDefinitionName().equals(CheckInActionExecuter.NAME) == true)
			{
				assertEquals(action, action2);
				assertEquals("description", action2.getParameterValue(CheckInActionExecuter.PARAM_DESCRIPTION));
			}
			else
			{
				fail("We have an action here we are not expecting.");
			}
		}
	}
	
	/**
	 * Test remove action
	 */
	public void testRemove()
	{
		assertEquals(0, this.actionService.getActions(this.nodeRef).size());
		
		Action action1 = this.actionService.createAction(AddFeaturesActionExecuter.NAME);
		this.actionService.saveAction(this.nodeRef, action1);
		Action action2 = this.actionService.createAction(CheckInActionExecuter.NAME);
		this.actionService.saveAction(this.nodeRef, action2);		
		assertEquals(2, this.actionService.getActions(this.nodeRef).size());
		
		this.actionService.removeAction(this.nodeRef, action1);
		assertEquals(1, this.actionService.getActions(this.nodeRef).size());
		
		this.actionService.removeAllActions(this.nodeRef);
		assertEquals(0, this.actionService.getActions(this.nodeRef).size());		
	}
    
    public void testConditionOrder()
    {
        Action action = this.actionService.createAction(AddFeaturesActionExecuter.NAME);
        String actionId = action.getId();
        
        ActionCondition condition1 = this.actionService.createActionCondition(NoConditionEvaluator.NAME);
        ActionCondition condition2 = this.actionService.createActionCondition(NoConditionEvaluator.NAME);
        
        action.addActionCondition(condition1);
        action.addActionCondition(condition2);
        
        this.actionService.saveAction(this.nodeRef, action);
        Action savedAction = this.actionService.getAction(this.nodeRef, actionId);
        
        // Check that the conditions have been retrieved in the correct order
        assertNotNull(savedAction);
        assertEquals(condition1, savedAction.getActionCondition(0));
        assertEquals(condition2, savedAction.getActionCondition(1));
        
        ActionCondition condition3 = this.actionService.createActionCondition(NoConditionEvaluator.NAME);
        ActionCondition condition4 = this.actionService.createActionCondition(NoConditionEvaluator.NAME);
        
        // Update the conditions on the action
        savedAction.removeActionCondition(condition1);
        savedAction.addActionCondition(condition3);
        savedAction.addActionCondition(condition4);
        
        this.actionService.saveAction(this.nodeRef, savedAction);
        Action savedAction2 = this.actionService.getAction(this.nodeRef, actionId);
        
        // Check that the conditions are still in the correct order
        assertNotNull(savedAction2);
        assertEquals(condition2, savedAction2.getActionCondition(0));
        assertEquals(condition3, savedAction2.getActionCondition(1));
        assertEquals(condition4, savedAction2.getActionCondition(2));
    }
    
    public void testActionOrder()
    {
        CompositeAction action = this.actionService.createCompositeAction();
        String actionId = action.getId();
        
        Action action1 = this.actionService.createAction(AddFeaturesActionExecuter.NAME);
        Action action2 = this.actionService.createAction(AddFeaturesActionExecuter.NAME);
        
        action.addAction(action1);
        action.addAction(action2);
        
        this.actionService.saveAction(this.nodeRef, action);
        CompositeAction savedAction = (CompositeAction)this.actionService.getAction(this.nodeRef, actionId);
        
        // Check that the conditions have been retrieved in the correct order
        assertNotNull(savedAction);
        assertEquals(action1, savedAction.getAction(0));
        assertEquals(action2, savedAction.getAction(1));
        
        Action action3 = this.actionService.createAction(AddFeaturesActionExecuter.NAME);
        Action action4 = this.actionService.createAction(AddFeaturesActionExecuter.NAME);
        
        // Update the conditions on the action
        savedAction.removeAction(action1);
        savedAction.addAction(action3);
        savedAction.addAction(action4);
        
        this.actionService.saveAction(this.nodeRef, savedAction);
        CompositeAction savedAction2 = (CompositeAction)this.actionService.getAction(this.nodeRef, actionId);
        
        // Check that the conditions are still in the correct order
        assertNotNull(savedAction2);
        assertEquals(action2, savedAction2.getAction(0));
        assertEquals(action3, savedAction2.getAction(1));
        assertEquals(action4, savedAction2.getAction(2));
    }
	
	/** ===================================================================================
	 *  Test asynchronous actions
	 */
	
	/**
	 * Test asynchronous execute action
	 */
	public void testAsyncExecuteAction()
	{
		assertFalse(this.nodeService.hasAspect(this.nodeRef, ContentModel.ASPECT_VERSIONABLE));
		
		Action action = this.actionService.createAction(AddFeaturesActionExecuter.NAME);
		action.setParameterValue(AddFeaturesActionExecuter.PARAM_ASPECT_NAME, ContentModel.ASPECT_VERSIONABLE);
		action.setExecuteAsynchronously(true);
		
		this.actionService.executeAction(action, this.nodeRef);
		
		setComplete();
		endTransaction();
		
		final NodeService finalNodeService = this.nodeService;
		final NodeRef finalNodeRef = this.nodeRef;
		
        postAsyncActionTest(
                this.transactionService,
				1000, 
				10, 
				new AsyncTest()
				{
					public boolean executeTest() 
					{
						return (
							finalNodeService.hasAspect(finalNodeRef, ContentModel.ASPECT_VERSIONABLE));
					};
				});
	}	
	
	
	
	/**
	 * Test async composite action execution
	 */
	public void testAsyncCompositeActionExecute()
	{
		// Create the composite action
		Action action1 = this.actionService.createAction(AddFeaturesActionExecuter.NAME);
		action1.setParameterValue(AddFeaturesActionExecuter.PARAM_ASPECT_NAME, ContentModel.ASPECT_LOCKABLE);
		Action action2 = this.actionService.createAction(AddFeaturesActionExecuter.NAME);
		action2.setParameterValue(AddFeaturesActionExecuter.PARAM_ASPECT_NAME, ContentModel.ASPECT_VERSIONABLE);		
		CompositeAction compAction = this.actionService.createCompositeAction();
		compAction.setTitle("title");
		compAction.setDescription("description");
		compAction.addAction(action1);
		compAction.addAction(action2);
		compAction.setExecuteAsynchronously(true);
		
		// Execute the composite action
		this.actionService.executeAction(compAction, this.nodeRef);
		
		setComplete();
		endTransaction();
		
		final NodeService finalNodeService = this.nodeService;
		final NodeRef finalNodeRef = this.nodeRef;
		
		postAsyncActionTest(
                this.transactionService,
				1000, 
				10, 
				new AsyncTest()
				{
					public boolean executeTest() 
					{
						return (
							finalNodeService.hasAspect(finalNodeRef, ContentModel.ASPECT_VERSIONABLE) &&
							finalNodeService.hasAspect(finalNodeRef, ContentModel.ASPECT_LOCKABLE));
					};
				});
	}
	
	public void xtestAsyncLoadTest()
	{
		// TODO this is very weak .. how do we improve this ???
		
		Action action = this.actionService.createAction(AddFeaturesActionExecuter.NAME);
		action.setParameterValue(AddFeaturesActionExecuter.PARAM_ASPECT_NAME, ContentModel.ASPECT_VERSIONABLE);
		action.setExecuteAsynchronously(true);
		
		for (int i = 0; i < 1000; i++)
		{
			this.actionService.executeAction(action, this.nodeRef);
		}		
		
		setComplete();
		endTransaction();
		
		// TODO how do we assess whether the large number of actions stacked cause a problem ??
	}
	
	/**
	 * 
	 * @param sleepTime
	 * @param maxTries
	 * @param test
	 * @param context
	 */
	public static void postAsyncActionTest(
            TransactionService transactionService,
            final long sleepTime, 
            final int maxTries, 
            final AsyncTest test)
	{
		try
		{
			int tries = 0;
			boolean done = false;
			while (done == false && tries < maxTries)
			{
				try
				{
					// Increment the tries counter
					tries++;
					
					// Sleep for a bit
					Thread.sleep(sleepTime);
					
					done = (TransactionUtil.executeInUserTransaction(
								transactionService,
								new TransactionUtil.TransactionWork<Boolean>()
								{
									public Boolean doWork()
									{	
										// See if the action has been performed
                                        boolean done = test.executeTest();
                                        return done;
									}					
								})).booleanValue();
				} 
				catch (InterruptedException e)
				{
					// Do nothing
					e.printStackTrace();
				}
			}
			
			if (done == false)
			{
				throw new RuntimeException("Asynchronous action was not executed.");
			}
		}
		catch (Throwable exception)
		{
			exception.printStackTrace();
			fail("An exception was encountered whilst checking the async action was executed: " + exception.getMessage());
		}
	}
	
	/**
	 * Async test interface
	 */
	public interface AsyncTest
	{
		boolean executeTest();		
	}
		
	/** ===================================================================================
	 *  Test failure behaviour
	 */
	
	/**
	 * Test sync failure behaviour
	 */
	public void testSyncFailureBehaviour()
	{
		// Create an action that is going to fail
		Action action = this.actionService.createAction(MoveActionExecuter.NAME);
		action.setParameterValue(MoveActionExecuter.PARAM_ASSOC_TYPE_QNAME, ContentModel.ASSOC_CHILDREN);
		action.setParameterValue(MoveActionExecuter.PARAM_ASSOC_QNAME, ContentModel.ASSOC_CHILDREN);
		// Create a bad node ref
		NodeRef badNodeRef = new NodeRef(this.storeRef, "123123");
		action.setParameterValue(MoveActionExecuter.PARAM_DESTINATION_FOLDER, badNodeRef);
		
		try
		{
			this.actionService.executeAction(action, this.nodeRef);
			
			// Fail if we get there since the exception should have been raised
			fail("An exception should have been raised.");
		}
		catch (RuntimeException exception)
		{
			// Good!  The exception was raised correctly
		}
		
		// Test what happens when a element of a composite action fails (should raise and bubble up to parent bahviour)		
		// Create the composite action
		Action action1 = this.actionService.createAction(AddFeaturesActionExecuter.NAME);
		action1.setParameterValue(AddFeaturesActionExecuter.PARAM_ASPECT_NAME, ContentModel.ASPECT_LOCKABLE);
		Action action2 = this.actionService.createAction(AddFeaturesActionExecuter.NAME);
		action2.setParameterValue(AddFeaturesActionExecuter.PARAM_ASPECT_NAME, QName.createQName("{test}badDogAspect"));		
		CompositeAction compAction = this.actionService.createCompositeAction();
		compAction.setTitle("title");
		compAction.setDescription("description");
		compAction.addAction(action1);
		compAction.addAction(action2);
		
		try
		{
			// Execute the composite action
			this.actionService.executeAction(compAction, this.nodeRef);
			
			fail("An exception should have been raised here !!");
		}
		catch (RuntimeException runtimeException)
		{
			// Good! The exception was raised
		}		
	}
	
	/**
	 * Test the compensating action
	 */
	public void testCompensatingAction()
	{
		// Create an action that is going to fail
		final Action action = this.actionService.createAction(MoveActionExecuter.NAME);
		action.setParameterValue(MoveActionExecuter.PARAM_ASSOC_TYPE_QNAME, ContentModel.ASSOC_CHILDREN);
		action.setParameterValue(MoveActionExecuter.PARAM_ASSOC_QNAME, ContentModel.ASSOC_CHILDREN);
		// Create a bad node ref
		NodeRef badNodeRef = new NodeRef(this.storeRef, "123123");
		action.setParameterValue(MoveActionExecuter.PARAM_DESTINATION_FOLDER, badNodeRef);
		action.setTitle("title");
		
		// Create the compensating action
		Action compensatingAction = actionService.createAction(AddFeaturesActionExecuter.NAME);
		compensatingAction.setParameterValue(AddFeaturesActionExecuter.PARAM_ASPECT_NAME, ContentModel.ASPECT_CLASSIFIABLE);
		compensatingAction.setTitle("title");
		action.setCompensatingAction(compensatingAction);
		
		// Set the action to execute asynchronously
		action.setExecuteAsynchronously(true);
		
		this.actionService.executeAction(action, this.nodeRef);
		
		setComplete();
		endTransaction();
		
		postAsyncActionTest(
                this.transactionService,
				1000, 
				10, 
				new AsyncTest()
				{
					public boolean executeTest() 
					{
						return (
							ActionServiceImplTest.this.nodeService.hasAspect(nodeRef, ContentModel.ASPECT_CLASSIFIABLE));
					};
				});
		
		// Modify the compensating action so that it will also fail
		compensatingAction.setParameterValue(AddFeaturesActionExecuter.PARAM_ASPECT_NAME, QName.createQName("{test}badAspect"));
		
		TransactionUtil.executeInUserTransaction(
				this.transactionService,
				new TransactionUtil.TransactionWork<Object>()
				{
					public Object doWork()
					{						
						try
						{
							ActionServiceImplTest.this.actionService.executeAction(action, ActionServiceImplTest.this.nodeRef);
						}
						catch (RuntimeException exception)
						{
							// The exception should have been ignored and execution continued
							exception.printStackTrace();
							fail("An exception should not have been raised here.");
						}
						return null;
					}
					
				});
		
	}
}