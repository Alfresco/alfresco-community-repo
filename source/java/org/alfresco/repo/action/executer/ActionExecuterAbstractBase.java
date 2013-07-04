/*
 * Copyright (C) 2005-2013 Alfresco Software Limited.
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
package org.alfresco.repo.action.executer;

import java.util.HashSet;
import java.util.Set;

import org.alfresco.repo.action.ActionDefinitionImpl;
import org.alfresco.repo.action.ParameterizedItemAbstractBase;
import org.alfresco.repo.lock.LockUtils;
import org.alfresco.service.cmr.action.Action;
import org.alfresco.service.cmr.action.ActionDefinition;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.lock.LockService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.QName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Rule action executor abstract base.
 * 
 * @author Roy Wetherall
 */
public abstract class ActionExecuterAbstractBase extends ParameterizedItemAbstractBase implements ActionExecuter
{
    private static Log logger = LogFactory.getLog(ActionExecuterAbstractBase.class);
    
    protected ActionDefinition actionDefinition;
    private LockService lockService;
    private NodeService baseNodeService;
    private DictionaryService dictionaryService;
    private NodeService mlAwareNodeService;
    
    /** Indicate if the action status should be tracked or not (default <tt>false</tt>) */
    private boolean trackStatus = false;
    
    /** Indicated whether the action is public or internal (default <tt>true</tt>) */
    protected boolean publicAction = true;
    
    /** List of types and aspects for which this action is applicable */
    protected Set<QName> applicableTypes = new HashSet<QName>();
    
    /**  Default queue name */
    private String queueName = "";
    
    /** Indicates whether the action should be ignored if the actioned upon node is locked */
    private boolean ignoreLock = true;
    
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
    
    public void setMlAwareNodeService(NodeService mlAwareNodeService)
    {
		this.mlAwareNodeService = mlAwareNodeService;
	}

	public void setLockService(LockService lockService) 
    {
        this.lockService = lockService;
    }
    
    public void setBaseNodeService(NodeService nodeService)
    {
        this.baseNodeService = nodeService;
    }
    
    /**
     * Set the dictionary service
     * 
     * @param dictionaryService the dictionary service
     */
    public void setDictionaryService(DictionaryService dictionaryService)
    {
        this.dictionaryService = dictionaryService;
    }
    
    /**
     * Set whether the action is public or not.
     * 
     * @param publicAction    true if the action is public, false otherwise
     */
    public void setPublicAction(boolean publicAction)
    {
        this.publicAction = publicAction;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean getTrackStatus()
    {
        return trackStatus;
    }

    /**
     * Set whether the basic action definition requires status tracking.
     * This can be overridden on each action instance but if not, it falls back
     * to this definition.
     * <p/>
     * Setting this to <tt>true</tt> introduces performance problems for concurrently-executing
     * rules on V3.4: <a href="https://issues.alfresco.com/jira/browse/ALF-7341">ALF-7341</a>.
     * It should only be used for long, seldom-run actions.
     * 
     * @param trackStatus           <tt>true</tt> to track execution status otherwise <tt>false</tt>
     * 
     * @since 3.4.1
     */
    public void setTrackStatus(boolean trackStatus)
    {
        this.trackStatus = trackStatus;
    }

    /**
     * Set the list of types for which this action is applicable
     * 
     * @param applicableTypes   array of applicable types
     */
    public void setApplicableTypes(String[] applicableTypes)
    {
        for (String type : applicableTypes)
        {
            this.applicableTypes.add(QName.createQName(type));
        }
    }
    
    /**
     * @see org.alfresco.repo.action.executer.ActionExecuter#getIgnoreLock()
     */
    public boolean getIgnoreLock()
    {
        return this.ignoreLock;
    }
    
    /**
     * Set the ignore lock value.
     * @param ignoreLock    true if lock should be ignored on actioned upon node, false otherwise
     */
    public void setIgnoreLock(boolean ignoreLock)
    {
        this.ignoreLock = ignoreLock;
    }
    
    /**
     * Check if a node is a type or subtype of the of one of the applicable types
     * 
     * @param actionedUponNodeRef   the node to check
     * @return                      Returns <tt>true</tt> if the node is in the list of
     *              {@link #setApplicableTypes(String[]) applicable types} or one of the
     *              subtypes
     */
    protected boolean isApplicableType(NodeRef actionedUponNodeRef)
    {
        if (this.baseNodeService.exists(actionedUponNodeRef) == true)
        {
            QName nodeType = baseNodeService.getType(actionedUponNodeRef);
            // Quick check in the set
            if (applicableTypes.contains(nodeType))
            {
                return true;
            }
            else
            {
                // Have to do a long-winded check
                for (QName type : applicableTypes)
                {
                    if (this.dictionaryService.isSubClass(nodeType, type))
                    {
                        return true;
                    }
                    // Not a subtype; keep checking
                }
            }
        }
        return false;
    }
    
    /**
     * Get rule action definition
     * 
     * @return    the action definition object
     */
    public ActionDefinition getActionDefinition() 
    {
        if (this.actionDefinition == null)
        {
            this.actionDefinition = createActionDefinition(this.name);
            ((ActionDefinitionImpl)this.actionDefinition).setTitleKey(getTitleKey());
            ((ActionDefinitionImpl)this.actionDefinition).setDescriptionKey(getDescriptionKey());
            ((ActionDefinitionImpl)this.actionDefinition).setTrackStatus(getTrackStatus());
            ((ActionDefinitionImpl)this.actionDefinition).setAdhocPropertiesAllowed(getAdhocPropertiesAllowed());
            ((ActionDefinitionImpl)this.actionDefinition).setRuleActionExecutor(this.name);
            ((ActionDefinitionImpl)this.actionDefinition).setParameterDefinitions(getParameterDefintions());
            ((ActionDefinitionImpl)this.actionDefinition).setApplicableTypes(this.applicableTypes);
        }
        return this.actionDefinition;
    }
    
    /**
     * This method returns an instance of an ActionDefinition implementation class. By default
     * this will be an {@link ActionDefinitionImpl}, but this could be overridden.
     */
    protected ActionDefinition createActionDefinition(String name)
    {
        return new ActionDefinitionImpl(name);
    }
    
    /**
     * {@inheritDoc}
     */
    public void execute(Action action, NodeRef actionedUponNodeRef)
    {        
        // Check the mandatory properties
        checkMandatoryProperties(action, getActionDefinition());
        
        // Only execute the action if this action is read only or the actioned upon node reference doesn't
        // have a lock applied for this user.
        boolean nodeIsLockedForThisUser = false;
        
        // null nodeRefs can't be locked and some actions can be run against 'null' nodes.
        // non-existent nodes can't be locked.
        if (!ignoreLock &&
            actionedUponNodeRef != null &&
            mlAwareNodeService.exists(actionedUponNodeRef))
        {
            nodeIsLockedForThisUser = LockUtils.isLockedAndReadOnly(actionedUponNodeRef, lockService);
        }
        
        if ( !nodeIsLockedForThisUser)
        {
            // Execute the implementation
            executeImpl(action, actionedUponNodeRef);
        }
        else
        {
            if (logger.isWarnEnabled() == true)
            {
                logger.warn("Action (" + action.getActionDefinitionName() + 
                             ") ignored because actioned upon node (" + actionedUponNodeRef + 
                             ") is locked.");
            }
        }
    }
    
    /**
     * Execute the action implementation
     * 
     * @param action                the action
     * @param actionedUponNodeRef   the actioned upon node
     */
    protected abstract void executeImpl(Action action, NodeRef actionedUponNodeRef);
    
    /**
     * Set the queueName which will execute this action
     * if blank or null then the action will be executed on the "default" queue
     * @param the name of the execution queue which should execute this action.
     */ 
    public void setQueueName(String queueName) 
    {
        this.queueName = queueName;
    }

    public String getQueueName() {
        return queueName;
    }
}
