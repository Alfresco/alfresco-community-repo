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
package org.alfresco.repo.action.executer;

import java.util.ArrayList;
import java.util.List;

import org.alfresco.repo.action.ActionDefinitionImpl;
import org.alfresco.repo.action.ParameterizedItemAbstractBase;
import org.alfresco.service.cmr.action.Action;
import org.alfresco.service.cmr.action.ActionDefinition;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;

/**
 * Rule action executor abstract base.
 * 
 * @author Roy Wetherall
 */
public abstract class ActionExecuterAbstractBase extends ParameterizedItemAbstractBase implements ActionExecuter
{
	/**
	 * Action definition
	 */
	protected ActionDefinition actionDefinition;
	
	/**
	 * Indicated whether the action is public or internal
	 */
	protected boolean publicAction = true;
    
    /** List of types and aspects for which this action is applicable */
    protected List<QName> applicableTypes = new ArrayList<QName>();
	
	/**
	 * Init method	 
	 */
	public void init()
	{
		if (this.publicAction == true)
		{
			this.runtimeActionService.registerActionExecuter(this);
		}
	}
	
	/**
	 * Set whether the action is public or not.
	 * 
	 * @param publicAction	true if the action is public, false otherwise
	 */
	public void setPublicAction(boolean publicAction)
	{
		this.publicAction = publicAction;
	}
    
    /**
     * Set the list of types for which this action is applicable
     * 
     * @param applicableTypes   arry of applicable types
     */
    public void setApplicableTypes(String[] applicableTypes)
    {
        for (String type : applicableTypes)
        {
            this.applicableTypes.add(QName.createQName(type));
        }
    }
	
	/**
	 * Get rule action definition
	 * 
	 * @return	the action definition object
	 */
	public ActionDefinition getActionDefinition() 
	{
		if (this.actionDefinition == null)
		{
			this.actionDefinition = new ActionDefinitionImpl(this.name);
			((ActionDefinitionImpl)this.actionDefinition).setTitleKey(getTitleKey());
			((ActionDefinitionImpl)this.actionDefinition).setDescriptionKey(getDescriptionKey());
			((ActionDefinitionImpl)this.actionDefinition).setAdhocPropertiesAllowed(getAdhocPropertiesAllowed());
			((ActionDefinitionImpl)this.actionDefinition).setRuleActionExecutor(this.name);
			((ActionDefinitionImpl)this.actionDefinition).setParameterDefinitions(getParameterDefintions());
            ((ActionDefinitionImpl)this.actionDefinition).setApplicableTypes(this.applicableTypes);
		}
		return this.actionDefinition;
	}
	
	/**
     * @see org.alfresco.repo.action.executer.ActionExecuter#execute(org.alfresco.service.cmr.repository.NodeRef, org.alfresco.service.cmr.repository.NodeRef)
     */
    public void execute(Action action, NodeRef actionedUponNodeRef)
    {        
        // Check the mandatory properties
        checkMandatoryProperties(action, getActionDefinition());
        
        // Execute the implementation
        executeImpl(action, actionedUponNodeRef);        
    }
	
    /**
     * Execute the action implementation
     * 
     * @param action				the action
     * @param actionedUponNodeRef   the actioned upon node
     */
	protected abstract void executeImpl(Action action, NodeRef actionedUponNodeRef);

     
}
