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
package org.alfresco.module.org_alfresco_module_rm.capability;

import java.util.ArrayList;
import java.util.List;

import net.sf.acegisecurity.vote.AccessDecisionVoter;

import org.alfresco.module.org_alfresco_module_rm.action.RecordsManagementAction;
import org.alfresco.module.org_alfresco_module_rm.model.RecordsManagementModel;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.security.AccessStatus;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.extensions.surf.util.I18NUtil;

/**
 * Abstract capability implementation.
 * 
 * @author Andy Hind
 * @author Roy Wetherall
 */
public abstract class AbstractCapability extends RMSecurityCommon
                                         implements Capability, RecordsManagementModel, RMPermissionModel
{
    /** Logger */
    @SuppressWarnings("unused")
	private static Log logger = LogFactory.getLog(AbstractCapability.class);

    /** RM entry voter */
    protected RMEntryVoter voter;
    
    /** Capability service */
    protected CapabilityService capabilityService;
    
    /** Capability name */
    protected String name;
    
    /** Capability title and description */
    protected String title;
    protected String description;
    
    /** Indicates whether this is a private capability or not */
    protected boolean isPrivate = false;

    /** List of actions */
    protected List<RecordsManagementAction> actions = new ArrayList<RecordsManagementAction>(1);

    /** Action names */
    protected List<String> actionNames = new ArrayList<String>(1);

    /**
     * @param voter     RM entry voter 
     */
    public void setVoter(RMEntryVoter voter)
    {
        this.voter = voter;
    }
    
    /**
     * @param capabilityService capability service
     */
    public void setCapabilityService(CapabilityService capabilityService)
    {
        this.capabilityService = capabilityService;
    }
    
    /**
     * Init method
     */
    public void init()
    {
        capabilityService.registerCapability(this);
    }

    /**
     * Registers an action
     * 
     * @param action
     */
    public void registerAction(RecordsManagementAction action)
    {
        this.actions.add(action);
        this.actionNames.add(action.getName());
        voter.addProtectedAspects(action.getProtectedAspects());
        voter.addProtectedProperties(action.getProtectedProperties());
    }
    
    /**
     * @param name  capability name
     */
    public void setName(String name)
    {
        this.name = name;
    }    
    
    /**
     * @see org.alfresco.module.org_alfresco_module_rm.capability.Capability#getName()
     */
    @Override
    public String getName()
    {
        return name;
    }
    
    /**
     * @param   title   capability title
     */
    public void setTitle(String title)
    {
        this.title = title;
    }

    /**
     * @param titleId   message id
     */
    public void setTitleId(String titleId)
    {
        this.title = I18NUtil.getMessage(titleId);
    }
    
    /**
     * @see org.alfresco.module.org_alfresco_module_rm.capability.Capability#getTitle()
     */
    @Override
    public String getTitle()
    {
        return title;
    }
    
    /**
     * @param description   capability description
     */
    public void setDescription(String description)
    {
        this.description = description;
    }
    
    /**
     * @param descriptionId     message id
     */
    public void setDescriptionId(String descriptionId)
    {
        this.description = I18NUtil.getMessage(descriptionId);
    }
    
    /**
     * @see org.alfresco.module.org_alfresco_module_rm.capability.Capability#getDescription()
     */
    @Override
    public String getDescription()
    {
        return description;
    }

    /**
     * @see org.alfresco.module.org_alfresco_module_rm.capability.Capability#isPrivate()
     */
    public boolean isPrivate()
    {
        return isPrivate;
    }

    /**
     * @param isPrivate indicates whether the capability is private or not
     */
    public void setPrivate(boolean isPrivate)
    {
        this.isPrivate = isPrivate;
    }
    
    /**
     * Translates the vote to an AccessStatus
     * 
     * @param vote
     * @return
     */
    private AccessStatus translate(int vote)
    {
        switch (vote)
        {
        case AccessDecisionVoter.ACCESS_ABSTAIN:
            return AccessStatus.UNDETERMINED;
        case AccessDecisionVoter.ACCESS_GRANTED:
            return AccessStatus.ALLOWED;
        case AccessDecisionVoter.ACCESS_DENIED:
            return AccessStatus.DENIED;
        default:
            return AccessStatus.UNDETERMINED;
        }
    }

    /**
     * 
     * @param nodeRef
     * @return
     */
    public int checkActionConditionsIfPresent(NodeRef nodeRef)
    {
        String prefix = "checkActionConditionsIfPresent" + getName();
        int result = getTransactionCache(prefix, nodeRef);
        if (result != NOSET_VALUE)
        {
            return result;
        }
        
        if (actions.size() > 0)
        {
            for (RecordsManagementAction action : actions)
            {
                if (action.isExecutable(nodeRef, null))
                {
                    return setTransactionCache(prefix, nodeRef, AccessDecisionVoter.ACCESS_GRANTED);
                }
            }
            return setTransactionCache(prefix, nodeRef, AccessDecisionVoter.ACCESS_DENIED);
        }
        else
        {
            return setTransactionCache(prefix, nodeRef, AccessDecisionVoter.ACCESS_GRANTED);
        }
    }

    /**
     * @see org.alfresco.module.org_alfresco_module_rm.capability.Capability#hasPermission(org.alfresco.service.cmr.repository.NodeRef)
     */
    public AccessStatus hasPermission(NodeRef nodeRef)
    {
        return translate(hasPermissionRaw(nodeRef));
    }
    
    /**
     * Determines whether the current user has permission on this capability.
     * <p>
     * Returns the raw permission value.
     * 
     * @param   nodeRef node reference
     * @return  raw permission value
     */
    public int hasPermissionRaw(NodeRef nodeRef)
    {
        String prefix = "hasPermissionRaw" + getName();
        int result = getTransactionCache(prefix, nodeRef);
        if (result != NOSET_VALUE)
        {
            return result;
        }
        
        if (checkRmRead(nodeRef) == AccessDecisionVoter.ACCESS_DENIED)
        {
            result = AccessDecisionVoter.ACCESS_DENIED;
        }
        else if (checkActionConditionsIfPresent(nodeRef) == AccessDecisionVoter.ACCESS_DENIED)
        {
            result = AccessDecisionVoter.ACCESS_DENIED;
        }
        else
        {
            result = hasPermissionImpl(nodeRef);
        }
        
        return setTransactionCache(prefix, nodeRef, result);
    }
    
    /**
     * Default implementation.  Override if different behaviour required.
     * 
     * @param nodeRef
     * @return
     */
    protected int hasPermissionImpl(NodeRef nodeRef)
    {
        return evaluate(nodeRef);
    }
    
    /**
     * @see org.alfresco.module.org_alfresco_module_rm.capability.Capability#evaluate(org.alfresco.service.cmr.repository.NodeRef, org.alfresco.service.cmr.repository.NodeRef)
     */
    public int evaluate(NodeRef source, NodeRef target)
    {
        return AccessDecisionVoter.ACCESS_ABSTAIN;
    }    

    /**
     * @see org.alfresco.module.org_alfresco_module_rm.capability.Capability#getActionNames()
     */
    public List<String> getActionNames()
    {
        return actionNames;
    }

    /**
     * @see org.alfresco.module.org_alfresco_module_rm.capability.Capability#getActions()
     */
    public List<RecordsManagementAction> getActions()
    {
        return actions;
    }

    /**
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((getName() == null) ? 0 : getName().hashCode());
        return result;
    }

    /**
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj)
    {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        final AbstractCapability other = (AbstractCapability) obj;
        if (getName() == null)
        {
            if (other.getName() != null)
                return false;
        }
        else if (!getName().equals(other.getName()))
            return false;
        return true;
    }

}
