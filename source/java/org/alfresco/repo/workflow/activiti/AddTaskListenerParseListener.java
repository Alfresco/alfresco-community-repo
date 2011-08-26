/*
 * Copyright (C) 2005-2011 Alfresco Software Limited.
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
package org.alfresco.repo.workflow.activiti;

import org.activiti.engine.delegate.ExecutionListener;
import org.activiti.engine.delegate.TaskListener;
import org.activiti.engine.impl.bpmn.behavior.UserTaskActivityBehavior;
import org.activiti.engine.impl.bpmn.parser.BpmnParseListener;
import org.activiti.engine.impl.persistence.entity.ProcessDefinitionEntity;
import org.activiti.engine.impl.pvm.delegate.ActivityBehavior;
import org.activiti.engine.impl.pvm.process.ActivityImpl;
import org.activiti.engine.impl.pvm.process.ScopeImpl;
import org.activiti.engine.impl.pvm.process.TransitionImpl;
import org.activiti.engine.impl.util.xml.Element;
import org.activiti.engine.impl.variable.VariableDeclaration;

/**
 * A {@link BpmnParseListener} that adds a start- and endTaskListener to
 * all parsed userTasks.
 * 
 * This is used to wire in custom logic when task is created and completed.
 *
 * @author Frederik Heremans
 * @since 3.4.e
 */
public class AddTaskListenerParseListener implements BpmnParseListener
{
    private TaskListener completeTaskListener;
    private TaskListener createTaskListener;
    private ExecutionListener processCreateListener;
    
    @Override
    public void parseUserTask(Element userTaskElement, ScopeImpl scope, ActivityImpl activity)
    {
         ActivityBehavior activitybehaviour = activity.getActivityBehavior();
         if(activitybehaviour instanceof UserTaskActivityBehavior)
         {
        	 UserTaskActivityBehavior userTaskActivity = (UserTaskActivityBehavior) activitybehaviour;
             if(createTaskListener != null)
             {
                 userTaskActivity.getTaskDefinition().addTaskListener(TaskListener.EVENTNAME_CREATE, createTaskListener);                 
             }
             if(completeTaskListener != null)
             {
                 userTaskActivity.getTaskDefinition().addTaskListener(TaskListener.EVENTNAME_COMPLETE, completeTaskListener);                 
             }
         }
    }
    
    @Override
    public void parseProcess(Element processElement, ProcessDefinitionEntity processDefinition)
    {
        processDefinition.addExecutionListener(ExecutionListener.EVENTNAME_START, processCreateListener);
    }

    @Override
    public void parseStartEvent(Element startEventElement, ScopeImpl scope,
                ActivityImpl startEventActivity)
    {
      // Nothing to do here
    }

    @Override
    public void parseExclusiveGateway(Element exclusiveGwElement, ScopeImpl scope,
                ActivityImpl activity)
    {
      // Nothing to do here
    }

    @Override
    public void parseParallelGateway(Element parallelGwElement, ScopeImpl scope,
                ActivityImpl activity)
    {
      // Nothing to do here
    }

    @Override
    public void parseScriptTask(Element scriptTaskElement, ScopeImpl scope, ActivityImpl activity)
    {
      // Nothing to do here
    }

    @Override
    public void parseServiceTask(Element serviceTaskElement, ScopeImpl scope, ActivityImpl activity)
    {
      // Nothing to do here
    }

    @Override
    public void parseTask(Element taskElement, ScopeImpl scope, ActivityImpl activity)
    {
      // Nothing to do here
    }

    @Override
    public void parseManualTask(Element manualTaskElement, ScopeImpl scope, ActivityImpl activity)
    {
      // Nothing to do here
    }

    @Override
    public void parseEndEvent(Element endEventElement, ScopeImpl scope, ActivityImpl activity)
    {
      // Nothing to do here
    }

    @Override
    public void parseBoundaryTimerEventDefinition(Element timerEventDefinition,
                boolean interrupting, ActivityImpl timerActivity)
    {
     // Nothing to do here
    }

    @Override
    public void parseSubProcess(Element subProcessElement, ScopeImpl scope, ActivityImpl activity)
    {
     // Nothing to do here
    }

    @Override
    public void parseCallActivity(Element callActivityElement, ScopeImpl scope,
                ActivityImpl activity)
    {
     // Nothing to do here
    }

    @Override
    public void parseProperty(Element propertyElement, VariableDeclaration variableDeclaration,
                ActivityImpl activity)
    {
     // Nothing to do here
    }

    @Override
    public void parseSequenceFlow(Element sequenceFlowElement, ScopeImpl scopeElement,
                TransitionImpl transition)
    {
     // Nothing to do here
    }

    @Override
    public void parseSendTask(Element sendTaskElement, ScopeImpl scope, ActivityImpl activity)
    {
        // Nothing to do here
    }
    
    @Override
    public void parseBusinessRuleTask(Element businessRuleTaskElement, ScopeImpl scope,
                ActivityImpl activity)
    {
        // Nothing to do here
    }
    
    @Override
	public void parseBoundaryErrorEventDefinition(Element errorEventDefinition,
			boolean interrupting, ActivityImpl activity,
			ActivityImpl nestedErrorEventActivity) 
    {
		// Nothing to do here
	}
    
    @Override
	public void parseIntermediateTimerEventDefinition(
			Element timerEventDefinition, ActivityImpl timerActivity) 
    {
    	// Nothing to do here
	}
    
    @Override
    public void parseMultiInstanceLoopCharacteristics(Element activityElement,
    		Element multiInstanceLoopCharacteristicsElement,
    		ActivityImpl activity) {
    	// Nothing to do here
    }
    
    public void setCompleteTaskListener(TaskListener completeTaskListener)
    {
        this.completeTaskListener = completeTaskListener;
    }

    public void setCreateTaskListener(TaskListener createTaskListener)
    {
        this.createTaskListener = createTaskListener;
    }
    
    public void setProcessCreateListener(ExecutionListener processCreateListener)
    {
        this.processCreateListener = processCreateListener;
    }

}
