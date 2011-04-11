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
 */
package org.alfresco.repo.action;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.action.evaluator.ComparePropertyValueEvaluator;
import org.alfresco.repo.action.evaluator.InCategoryEvaluator;
import org.alfresco.repo.action.evaluator.NoConditionEvaluator;
import org.alfresco.repo.action.evaluator.compare.ComparePropertyValueOperation;
import org.alfresco.repo.action.executer.ActionExecuter;
import org.alfresco.repo.action.executer.ActionExecuterAbstractBase;
import org.alfresco.repo.action.executer.AddFeaturesActionExecuter;
import org.alfresco.repo.action.executer.CheckInActionExecuter;
import org.alfresco.repo.action.executer.CheckOutActionExecuter;
import org.alfresco.repo.action.executer.CompositeActionExecuter;
import org.alfresco.repo.action.executer.MoveActionExecuter;
import org.alfresco.repo.action.executer.ScriptActionExecuter;
import org.alfresco.repo.content.MimetypeMap;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.transaction.RetryingTransactionHelper;
import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;
import org.alfresco.service.cmr.action.Action;
import org.alfresco.service.cmr.action.ActionCondition;
import org.alfresco.service.cmr.action.ActionConditionDefinition;
import org.alfresco.service.cmr.action.ActionDefinition;
import org.alfresco.service.cmr.action.ActionService;
import org.alfresco.service.cmr.action.ActionStatus;
import org.alfresco.service.cmr.action.ActionTrackingService;
import org.alfresco.service.cmr.action.CancellableAction;
import org.alfresco.service.cmr.action.CompositeAction;
import org.alfresco.service.cmr.action.CompositeActionCondition;
import org.alfresco.service.cmr.action.ParameterDefinition;
import org.alfresco.service.cmr.coci.CheckOutCheckInService;
import org.alfresco.service.cmr.repository.ContentData;
import org.alfresco.service.cmr.repository.ContentWriter;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.security.PermissionService;
import org.alfresco.service.cmr.security.PersonService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.service.transaction.TransactionService;
import org.alfresco.util.BaseAlfrescoSpringTest;
import org.alfresco.util.GUID;
import org.alfresco.util.PropertyMap;
import org.springframework.context.ConfigurableApplicationContext;

/**
 * Action service test
 * 
 * @author Roy Wetherall
 */
public class ActionServiceImplTest extends BaseAlfrescoSpringTest
{
    private static final String BAD_NAME = "badName";
    
    private NodeRef nodeRef;
    private NodeRef folder;
    private RetryingTransactionHelper transactionHelper;
    
//    @Override
//    protected String[] getConfigLocations()
//    {
//        String[] existingConfigLocations = ApplicationContextHelper.CONFIG_LOCATIONS;
//
//        List<String> locations = Arrays.asList(existingConfigLocations);
//		List<String> mutableLocationsList = new ArrayList<String>(locations);
//    	mutableLocationsList.add("classpath:org/alfresco/repo/action/test-action-services-context.xml");
//    	
//    	String[] result = mutableLocationsList.toArray(new String[mutableLocationsList.size()]);
//		return (String[]) result;
//    }
    
    @Override
    protected void onSetUpInTransaction() throws Exception
    {
        super.onSetUpInTransaction();

        this.transactionHelper = (RetryingTransactionHelper)this.applicationContext.getBean("retryingTransactionHelper");

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
        this.folder = this.nodeService.createNode(
                this.rootNodeRef,
                ContentModel.ASSOC_CHILDREN,
                QName.createQName("{test}testFolder"),
                ContentModel.TYPE_FOLDER).getChildRef();
        
        // Register the test executor, if needed
        SleepActionExecuter.registerIfNeeded(applicationContext);
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
        int totalCount = defintions.size();
        
        for (ActionDefinition definition : defintions)
        {
            System.out.println(definition.getTitle());
        }
        
        // Get the action defintions for a folder type (there should be less than the total available)
        List<ActionDefinition> definitions = this.actionService.getActionDefinitions(this.folder);
        assertNotNull(definitions);
        assertTrue(totalCount > definitions.size());        
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
    * Test createCompositeAction
    */
   public void testCreateCompositeActionCondition()
   {
      CompositeActionCondition action = this.actionService.createCompositeActionCondition();
      assertNotNull(action);
      assertEquals(CompositeActionCondition.COMPOSITE_CONDITION, action.getActionConditionDefinitionName());
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
    * Test evaluate action condition
    */
   public void testEvaluateCompositeActionConditionWith1SubCondition()
   {
      CompositeActionCondition compositeCondition = this.actionService.createCompositeActionCondition();
      
      ActionCondition condition = this.actionService.createActionCondition(ComparePropertyValueEvaluator.NAME);
      condition.setParameterValue(ComparePropertyValueEvaluator.PARAM_VALUE, "*.doc");

      compositeCondition.addActionCondition(condition);
      
      this.nodeService.setProperty(this.nodeRef, ContentModel.PROP_NAME, "myDocument.doc");
      assertTrue(this.actionService.evaluateActionCondition(compositeCondition, this.nodeRef));
        
      // Check that inverting the condition has the correct effect
      compositeCondition.setInvertCondition(true);
      assertFalse(this.actionService.evaluateActionCondition(compositeCondition, this.nodeRef));
   }

   /**
    * Test evaluate action condition
    */
   public void testEvaluateCompositeActionConditionWith2SubConditions()
   {
      CompositeActionCondition compositeCondition = this.actionService.createCompositeActionCondition();
      
      ActionCondition condition = this.actionService.createActionCondition(ComparePropertyValueEvaluator.NAME);
      condition.setParameterValue(ComparePropertyValueEvaluator.PARAM_VALUE, "*.doc");

      compositeCondition.addActionCondition(condition);
      
      this.nodeService.setProperty(this.nodeRef, ContentModel.PROP_NAME, "myDocument.doc");
      assertTrue(this.actionService.evaluateActionCondition(compositeCondition, this.nodeRef));

      ActionCondition conditionTwo = this.actionService.createActionCondition("compare-text-property");
      conditionTwo.setParameterValue(ComparePropertyValueEvaluator.PARAM_VALUE, "Doc");
      conditionTwo.setParameterValue(ComparePropertyValueEvaluator.PARAM_PROPERTY, QName.createQName(null, "name"));
      conditionTwo.setParameterValue(ComparePropertyValueEvaluator.PARAM_OPERATION, ComparePropertyValueOperation.CONTAINS.toString());

      compositeCondition.addActionCondition(conditionTwo);
      assertFalse(this.actionService.evaluateActionCondition(compositeCondition, this.nodeRef));
      
      compositeCondition.removeAllActionConditions();
      assertFalse(compositeCondition.hasActionConditions());
      
      compositeCondition.addActionCondition(condition);
      conditionTwo.setParameterValue(ComparePropertyValueEvaluator.PARAM_VALUE, "NotFound");
      assertFalse(this.actionService.evaluateActionCondition(conditionTwo, this.nodeRef)); 
      
      compositeCondition.addActionCondition(conditionTwo);
      compositeCondition.setORCondition(true);
      assertTrue(this.actionService.evaluateActionCondition(compositeCondition, this.nodeRef));

      compositeCondition.setORCondition(false);
      assertFalse(this.actionService.evaluateActionCondition(compositeCondition, this.nodeRef));
      
      // Check that inverting the condition has the correct effect
      compositeCondition.setInvertCondition(true);
      assertTrue(this.actionService.evaluateActionCondition(compositeCondition, this.nodeRef));
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
        action.setParameterValue(AddFeaturesActionExecuter.PARAM_ASPECT_NAME, ContentModel.ASPECT_AUDITABLE);
        
        // Set the compensating action
        Action compensatingAction = this.actionService.createAction(AddFeaturesActionExecuter.NAME);
        compensatingAction.setParameterValue(AddFeaturesActionExecuter.PARAM_ASPECT_NAME, ContentModel.ASPECT_VERSIONABLE);
        action.setCompensatingAction(compensatingAction);
        
        this.actionService.saveAction(this.nodeRef, action);
        Action savedAction2 = this.actionService.getAction(this.nodeRef, actionId);
        
        // Check the updated properties
        assertEquals(1, savedAction2.getParameterValues().size());
        assertEquals(ContentModel.ASPECT_AUDITABLE, savedAction2.getParameterValue(AddFeaturesActionExecuter.PARAM_ASPECT_NAME));
        
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
        //assertNull(action.getOwningNodeRef());
                
        // Save the action
        this.actionService.saveAction(this.nodeRef, action);
        
        // Get the action
        this.actionService.getAction(this.nodeRef, actionId);        
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
    
    /**
     * Test the action result parameter
     */
    public void testActionResult()
    {
        // Create the script node reference
        NodeRef script = this.nodeService.createNode(
                this.folder,
                ContentModel.ASSOC_CONTAINS,
                QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, "testScript.js"),
                ContentModel.TYPE_CONTENT).getChildRef();
        this.nodeService.setProperty(script, ContentModel.PROP_NAME, "testScript.js");
        ContentWriter contentWriter = this.contentService.getWriter(script, ContentModel.PROP_CONTENT, true);
        contentWriter.setMimetype("text/plain");
        contentWriter.setEncoding("UTF-8");
        contentWriter.putContent("\"VALUE\";");
        
        // Create the action
        Action action1 = this.actionService.createAction(ScriptActionExecuter.NAME);
        action1.setParameterValue(ScriptActionExecuter.PARAM_SCRIPTREF, script);
        
        // Execute the action
        this.actionService.executeAction(action1, this.nodeRef);
        
        // Get the result
        String result = (String)action1.getParameterValue(ActionExecuter.PARAM_RESULT);
        assertNotNull(result);
        assertEquals("VALUE", result);                
    }
    
    /** ===================================================================================
     *  Test asynchronous actions
     */
    

    /**
     * This test checks that a series of "equivalent" actions submitted for asynchronous execution
     * will be correctly filtered so that no 2 equivalent actions are executed at the same time.
     */
    public void offtestAsyncLongRunningActionsFilter()
    {
    	setComplete();
    	endTransaction();

    	final SleepActionExecuter sleepAction = (SleepActionExecuter)applicationContext.getBean("sleep-action");
		assertNotNull(sleepAction);
		sleepAction.setSleepMs(10);
		
		final int actionSubmissonCount = 4; // Rather arbitrary count.
		for (int i = 0; i < actionSubmissonCount; i ++)
		{
	        transactionHelper.doInTransaction(new RetryingTransactionHelper.RetryingTransactionCallback<Void>()
	                {
	                    public Void execute() throws Throwable
	                    {
	                    	Action action = actionService.createAction(SleepActionExecuter.NAME);
	                    	action.setExecuteAsynchronously(true);
	                    	
	                    	actionService.executeAction(action, nodeRef);

	                        return null;
	                    }          
	                });        

		}

        // Wait long enough for previous action(s) to have executed and then submit another
        try
        {
	  		Thread.sleep(sleepAction.getSleepMs() * actionSubmissonCount + 1000); // Enough time for all actions and an extra second for luck.
  		}
        catch (InterruptedException ignored)
		{
			// intentionally empty
		}
        transactionHelper.doInTransaction(new RetryingTransactionHelper.RetryingTransactionCallback<Void>()
                {
                    public Void execute() throws Throwable
                    {
                    	Action action = actionService.createAction(SleepActionExecuter.NAME);
                    	action.setExecuteAsynchronously(true);
                    	
                    	actionService.executeAction(action, nodeRef);

                        return null;
                    }          
                });        
        try
        {
	  		Thread.sleep(sleepAction.getSleepMs() + 2000); // Enough time for latest action and an extra 2 seconds for luck.
  		}
        catch (InterruptedException ignored)
		{
			// intentionally empty
		}

        
        int sleepTime = 0; // Do not sleep during execution as the Action itself sleeps.
		int maxTries = 1;
		postAsyncActionTest(
                this.transactionService,
                sleepTime, 
                maxTries, 
                new AsyncTest()
                {
                    public String executeTest()
                    {
                    	final int expectedResult = 2;
                    	int actualResult = sleepAction.getTimesExecuted();
                    	return actualResult == expectedResult ? null : "Expected timesExecuted " + expectedResult + " was " + actualResult;
                    };
                });
    }

    /**
     * Test asynchronous execute action
     */
    public void testAsyncExecuteAction()
    {
        assertFalse(this.nodeService.hasAspect(this.nodeRef, ContentModel.ASPECT_VERSIONABLE));
        
        Action action = this.actionService.createAction(AddFeaturesActionExecuter.NAME);
        action.setParameterValue(AddFeaturesActionExecuter.PARAM_ASPECT_NAME, ContentModel.ASPECT_CLASSIFIABLE);
        action.setExecuteAsynchronously(true);
        
        this.actionService.executeAction(action, this.nodeRef);
        
        setComplete();
        endTransaction();
        
        final NodeService finalNodeService = this.nodeService;
        final NodeRef finalNodeRef = this.nodeRef;
        
        postAsyncActionTest(
                this.transactionService,
                1000l, 
                10, 
                new AsyncTest()
                {
                    public String executeTest() 
                    {
                    	boolean result = finalNodeService.hasAspect(finalNodeRef, ContentModel.ASPECT_CLASSIFIABLE);
                    	return result ? null : "Expected aspect Classifiable";
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
                    public String executeTest() 
                    {
                    	boolean result = finalNodeService.hasAspect(finalNodeRef, ContentModel.ASPECT_VERSIONABLE) &&
                        finalNodeService.hasAspect(finalNodeRef, ContentModel.ASPECT_LOCKABLE);
                    	return result ? null : "Expected aspects Versionable & Lockable";
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
            String errorMsg = null;
            while (errorMsg == null && tries < maxTries)
            {
                try
                {
                    // Increment the tries counter
                    tries++;
                    
                    // Sleep for a bit
                    Thread.sleep(sleepTime);
                    
                    errorMsg = (transactionService.getRetryingTransactionHelper().doInTransaction(
                                new RetryingTransactionCallback<String>()
                                {
                                    public String execute()
                                    {    
                                        // See if the action has been performed
                                        String done = test.executeTest();
                                        return done;
                                    }                    
                                }));
                } 
                catch (InterruptedException e)
                {
                    // Do nothing
                    e.printStackTrace();
                }
            }
            
            if (errorMsg != null)
            {
                throw new RuntimeException("Asynchronous action was not executed. " + errorMsg);
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
    	/**
    	 * 
    	 * @return <code>null</code> if the test succeeded, else an error message for use in JUnit report.
    	 */
        String executeTest();        
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
        Action action = createFailingMoveAction();
        
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
        final Action action = createFailingMoveAction();
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
                    public String executeTest() 
                    {
                    	boolean result = ActionServiceImplTest.this.nodeService.hasAspect(nodeRef, ContentModel.ASPECT_CLASSIFIABLE);
                    	return result == true ? null : "Expected aspect Classifiable";
                    };
                });
        
        // Modify the compensating action so that it will also fail
        compensatingAction.setParameterValue(AddFeaturesActionExecuter.PARAM_ASPECT_NAME, QName.createQName("{test}badAspect"));
        
        this.transactionService.getRetryingTransactionHelper().doInTransaction(
                new RetryingTransactionCallback<Object>()
                {
                    public Object execute()
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
    
    private void createUser(String userName)
    {
        if (this.authenticationService.authenticationExists(userName) == false)
        {
            this.authenticationService.createAuthentication(userName, "PWD".toCharArray());
            
            PropertyMap ppOne = new PropertyMap(4);
            ppOne.put(ContentModel.PROP_USERNAME, userName);
            ppOne.put(ContentModel.PROP_FIRSTNAME, "firstName");
            ppOne.put(ContentModel.PROP_LASTNAME, "lastName");
            ppOne.put(ContentModel.PROP_EMAIL, "email@email.com");
            ppOne.put(ContentModel.PROP_JOBTITLE, "jobTitle");
            
            PersonService personService = (PersonService)applicationContext.getBean("personService");
            personService.createPerson(ppOne);
        }        
    }
    
    /**
     * http://issues.alfresco.com/jira/browse/ALF-5027
     */
    public void testALF5027() throws Exception
    {
    	String userName = "bob" + GUID.generate();
    	createUser(userName);
    	PermissionService permissionService = (PermissionService)applicationContext.getBean("PermissionService");
    	permissionService.setPermission(rootNodeRef, userName, PermissionService.COORDINATOR, true);
    	
    	AuthenticationUtil.setRunAsUser(userName);
    	
    	NodeRef myNodeRef = nodeService.createNode(
                this.rootNodeRef,
                ContentModel.ASSOC_CHILDREN,
                QName.createQName("{test}myTestNode" + GUID.generate()),
                ContentModel.TYPE_CONTENT).getChildRef();
    	
    	CheckOutCheckInService coci = (CheckOutCheckInService)applicationContext.getBean("CheckoutCheckinService");
    	NodeRef workingcopy = coci.checkout(myNodeRef);
    	assertNotNull(workingcopy);
    	
    	assertFalse(nodeService.hasAspect(myNodeRef, ContentModel.ASPECT_DUBLINCORE));
    	
    	Action action1 = this.actionService.createAction(AddFeaturesActionExecuter.NAME);
        action1.setParameterValue(AddFeaturesActionExecuter.PARAM_ASPECT_NAME, ContentModel.ASPECT_DUBLINCORE);        
        actionService.executeAction(action1, myNodeRef);
        
        // The action should have been ignored since the node is locked
        assertFalse(nodeService.hasAspect(myNodeRef, ContentModel.ASPECT_DUBLINCORE));
        
        coci.checkin(workingcopy, null);
        actionService.executeAction(action1, myNodeRef);
        
        assertTrue(nodeService.hasAspect(myNodeRef, ContentModel.ASPECT_DUBLINCORE));
    }
    
    /**
     * Tests that we can read, save, load etc the various
     *  execution related details such as started at,
     *  ended at, status and exception
     */
    public void testExecutionTrackingDetails() {
       Action action = this.actionService.createAction(AddFeaturesActionExecuter.NAME);
       String actionId = action.getId();
       
       assertNull(action.getExecutionStartDate());
       assertNull(action.getExecutionEndDate());
       assertNull(action.getExecutionFailureMessage());
       assertEquals(ActionStatus.New, action.getExecutionStatus());

       
       // Save and load, details shouldn't have changed
       this.actionService.saveAction(this.nodeRef, action);
       action = (Action)this.actionService.getAction(this.nodeRef, actionId);
       
       assertNull(action.getExecutionStartDate());
       assertNull(action.getExecutionEndDate());
       assertNull(action.getExecutionFailureMessage());
       assertEquals(ActionStatus.New, action.getExecutionStatus());
       
       
       // Set some details, ensure they survive a save/load
       ((ActionImpl)action).setExecutionStatus(ActionStatus.Running);
       ((ActionImpl)action).setExecutionStartDate(new Date(12345));
       
       this.actionService.saveAction(this.nodeRef, action);
       action = (Action)this.actionService.getAction(this.nodeRef, actionId);
       
       assertEquals(ActionStatus.Running, action.getExecutionStatus());
       assertEquals(12345, action.getExecutionStartDate().getTime());
       assertNull(action.getExecutionEndDate());
       assertNull(action.getExecutionFailureMessage());
       
       
       // Set the rest, and change some, ensure they survive a save/load
       ((ActionImpl)action).setExecutionStatus(ActionStatus.Failed);
       ((ActionImpl)action).setExecutionStartDate(new Date(123450));
       ((ActionImpl)action).setExecutionEndDate(new Date(123455));
       ((ActionImpl)action).setExecutionFailureMessage("Testing");
       
       this.actionService.saveAction(this.nodeRef, action);
       action = (Action)this.actionService.getAction(this.nodeRef, actionId);
       
       assertEquals(ActionStatus.Failed, action.getExecutionStatus());
       assertEquals(123450, action.getExecutionStartDate().getTime());
       assertEquals(123455, action.getExecutionEndDate().getTime());
       assertEquals("Testing", action.getExecutionFailureMessage());
       
       
       // Unset a few, ensure they survive a save/load
       ((ActionImpl)action).setExecutionStatus(null);
       ((ActionImpl)action).setExecutionStartDate(new Date(123450));
       ((ActionImpl)action).setExecutionFailureMessage(null);
       
       this.actionService.saveAction(this.nodeRef, action);
       action = (Action)this.actionService.getAction(this.nodeRef, actionId);
       
       assertEquals(ActionStatus.New, action.getExecutionStatus()); // Default
       assertEquals(123450, action.getExecutionStartDate().getTime());
       assertEquals(123455, action.getExecutionEndDate().getTime());
       assertEquals(null, action.getExecutionFailureMessage());
    }
    
    protected Action createFailingMoveAction() {
       Action failingAction = this.actionService.createAction(MoveActionExecuter.NAME);
       failingAction.setParameterValue(MoveActionExecuter.PARAM_ASSOC_TYPE_QNAME, ContentModel.ASSOC_CHILDREN);
       failingAction.setParameterValue(MoveActionExecuter.PARAM_ASSOC_QNAME, ContentModel.ASSOC_CHILDREN);
       // Create a bad node ref
       NodeRef badNodeRef = new NodeRef(this.storeRef, "123123");
       failingAction.setParameterValue(MoveActionExecuter.PARAM_DESTINATION_FOLDER, badNodeRef);
       
       return failingAction;
    }
    
    protected Action createFailingSleepAction(String id) throws Exception {
       return createFailingSleepAction(id, this.actionService);
    }
    protected static Action createFailingSleepAction(String id, ActionService actionService) throws Exception {
       Action failingAction = createWorkingSleepAction(id, actionService);
       failingAction.setParameterValue(SleepActionExecuter.GO_BANG, Boolean.TRUE);
       return failingAction;
    }
    
    protected Action createWorkingSleepAction() throws Exception
    {
       return createWorkingSleepAction(null);
    }
    protected Action createWorkingSleepAction(String id) throws Exception
    {
       return createWorkingSleepAction(id, this.actionService);
    }
    protected static Action createWorkingSleepAction(String id, ActionService actionService) throws Exception
    {
       Action workingAction = new CancellableSleepAction(actionService.createAction(SleepActionExecuter.NAME));
       workingAction.setTrackStatus(Boolean.TRUE);
       if(id != null)
       {
          Field idF = ParameterizedItemImpl.class.getDeclaredField("id");
          idF.setAccessible(true);
          idF.set(workingAction, id);
       }
       return workingAction;
    }
    
    /**
     * This class is only used during JUnit testing.
     * 
     * @author Neil Mc Erlean
     */
    public static class SleepActionFilter extends AbstractAsynchronousActionFilter
    {
    	public int compare(OngoingAsyncAction sae1, OngoingAsyncAction sae2)
    	{
    		// Sleep actions are always equivalent.
    		return 0;
    	}
    }
    
    /**
     * This class is only intended for use in JUnit tests.
     * 
     * @author Neil McErlean.
     */
    public static class SleepActionExecuter extends ActionExecuterAbstractBase
    {
       public static final String NAME = "sleep-action";
       public static final String GO_BANG = "GoBang";
       private int sleepMs;
       
       private Thread executingThread;
      
       private int timesExecuted = 0;
       private void incrementTimesExecutedCount() {timesExecuted++;}
       public int getTimesExecuted() {return timesExecuted;}
       public void resetTimesExecuted() {timesExecuted=0;}
       
       private ActionTrackingService actionTrackingService;
              
       /**
        * Loads this executor into the ApplicationContext, if it isn't already there
        */
        public static void registerIfNeeded(ConfigurableApplicationContext ctx)
        {
            if (!ctx.containsBean(SleepActionExecuter.NAME))
            {
                // Create, and do dependencies
                SleepActionExecuter executor = new SleepActionExecuter();
                executor.setTrackStatus(true);
                executor.actionTrackingService = (ActionTrackingService) ctx.getBean("actionTrackingService");
                // Register
                ctx.getBeanFactory().registerSingleton(SleepActionExecuter.NAME, executor);
            }
        }
       
       public Thread getExecutingThread()
       {
          return executingThread;
       }
      
       public int getSleepMs()
       {
          return sleepMs;
       }
      
       public void setSleepMs(int sleepMs)
       {
          this.sleepMs = sleepMs;
       }
      
       /**
        * Add parameter definitions
        */
       @Override
       protected void addParameterDefinitions(List<ParameterDefinition> paramList) 
       {
          // Intentionally empty
       }

       @Override
       protected void executeImpl(Action action, NodeRef actionedUponNodeRef) {
          executingThread = Thread.currentThread();
          //System.err.println("Sleeping for " + sleepMs + " for " + action);
         
          try
          {
             Thread.sleep(sleepMs);
          }
          catch (InterruptedException ignored)
          {
             // Intentionally empty
          }
          finally
          {
             incrementTimesExecutedCount();
          }
         
          Boolean fail = (Boolean)action.getParameterValue(GO_BANG);
          if(fail != null && fail) 
          {
             throw new RuntimeException("Bang!");
          }
          
          if(action instanceof CancellableSleepAction)
          {
             CancellableSleepAction ca = (CancellableSleepAction)action;
             boolean cancelled = actionTrackingService.isCancellationRequested(ca);
             if(cancelled)
                throw new ActionCancelledException(ca);
          }
       }
    }
    protected static class CancellableSleepAction extends ActionImpl implements CancellableAction
    {
        public CancellableSleepAction(Action action)
        {
            super(action);
            this.setTrackStatus(Boolean.TRUE);
        }
    }
    
    public static void assertBefore(Date before, Date after)
    {
       assertTrue(
             before.toString() + " not before " + after.toString(),
             before.getTime() <= after.getTime()
       );
    }
}