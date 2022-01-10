/*
 * #%L
 * Alfresco Records Management Module
 * %%
 * Copyright (C) 2005 - 2022 Alfresco Software Limited
 * %%
 * This file is part of the Alfresco software.
 * -
 * If the software was purchased under a paid Alfresco license, the terms of
 * the paid license agreement will prevail.  Otherwise, the software is
 * provided under the following open source license terms:
 * -
 * Alfresco is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * -
 * Alfresco is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * -
 * You should have received a copy of the GNU Lesser General Public License
 * along with Alfresco. If not, see <http://www.gnu.org/licenses/>.
 * #L%
 */

package org.alfresco.module.org_alfresco_module_rm.capability;

import org.alfresco.module.org_alfresco_module_rm.model.RecordsManagementModel;
import org.alfresco.module.org_alfresco_module_rm.security.RMMethodSecurityInterceptor;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.security.AccessStatus;
import org.apache.commons.lang3.StringUtils;
import org.springframework.extensions.surf.util.I18NUtil;

import net.sf.acegisecurity.vote.AccessDecisionVoter;

/**
 * Abstract capability implementation.
 *
 * @author Andy Hind
 * @author Roy Wetherall
 */
public abstract class AbstractCapability extends RMSecurityCommon
                                         implements Capability, RecordsManagementModel, RMPermissionModel
{
    /** Capability service */
    protected CapabilityService capabilityService;

    /** Capability name */
    protected String name;

    /** Capability title and description */
    protected String title;
    protected String description;

    /** Capability group */
    protected Group group;

    /** Capability index */
    protected int index;

    /** Indicates whether this is a private capability or not */
    protected boolean isPrivate = false;

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
     * @see org.alfresco.module.org_alfresco_module_rm.capability.Capability#getTitle()
     */
    @Override
    public String getTitle()
    {
        String title = this.title;
        if (StringUtils.isBlank(title))
        {
            title = I18NUtil.getMessage("capability." + getName() + ".title");
            if (StringUtils.isBlank(title))
            {
                title = getName();
            }

        }
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
        if (result == NOSET_VALUE)
        {
            if (checkRmRead(nodeRef) == AccessDecisionVoter.ACCESS_DENIED)
            {
                result = AccessDecisionVoter.ACCESS_DENIED;
            }
            else
            {
                result = hasPermissionImpl(nodeRef);
            }
    
            result = setTransactionCache(prefix, nodeRef, result);
        }
        
        // Log information about evaluated capability
        RMMethodSecurityInterceptor.reportCapabilityStatus(getName(), result);
        
        return result;
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
     * @see org.alfresco.module.org_alfresco_module_rm.capability.Capability#getGroup()
     */
    public Group getGroup()
    {
        return this.group;
    }

    public void setGroup(Group group)
    {
        this.group = group;
    }

    /**
     * @see org.alfresco.module.org_alfresco_module_rm.capability.Capability#getIndex()
     */
    public int getIndex()
    {
        return this.index;
    }

    public void setIndex(int index)
    {
        this.index = index;
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
        {
            return true;
        }
        if (obj == null)
        {
            return false;
        }
        if (getClass() != obj.getClass())
        {
            return false;
        }
        final AbstractCapability other = (AbstractCapability) obj;
        if (getName() == null)
        {
            if (other.getName() != null)
            {
                return false;
            }
        }
        else if (!getName().equals(other.getName()))
        {
            return false;
        }
        return true;
    }

}
