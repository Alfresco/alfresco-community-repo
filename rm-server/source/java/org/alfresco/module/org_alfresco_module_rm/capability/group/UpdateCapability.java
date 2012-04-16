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
package org.alfresco.module.org_alfresco_module_rm.capability.group;

import java.io.Serializable;
import java.util.Map;

import net.sf.acegisecurity.vote.AccessDecisionVoter;

import org.alfresco.module.org_alfresco_module_rm.capability.AbstractCapability;
import org.alfresco.module.org_alfresco_module_rm.capability.Capability;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;

/**
 * @author andyh
 */
public class UpdateCapability extends AbstractCapability
{
    /**
     * @see org.alfresco.module.org_alfresco_module_rm.capability.Capability#evaluate(org.alfresco.service.cmr.repository.NodeRef)
     */
    @Override
    public int evaluate(NodeRef nodeRef)
    {
        return evaluate(nodeRef, null, null);
    }

    /**
     * 
     * @param nodeRef
     * @param aspectQName
     * @param properties
     * @return
     */
    public int evaluate(NodeRef nodeRef, QName aspectQName, Map<QName, Serializable> properties)
    {
        if ((aspectQName != null) && (voter.isProtectedAspect(nodeRef, aspectQName)))
        {
            return AccessDecisionVoter.ACCESS_DENIED;
        }
        if ((properties != null) && (voter.includesProtectedPropertyChange(nodeRef, properties)))
        {
            return AccessDecisionVoter.ACCESS_DENIED;
        }
        
        Capability destFolder = capabilityService.getCapability(CREATE_MODIFY_DESTROY_FOLDERS);
        if (destFolder.evaluate(nodeRef) == AccessDecisionVoter.ACCESS_GRANTED)
        {
            return AccessDecisionVoter.ACCESS_GRANTED;
        }
        
        Capability fileplanMeta = capabilityService.getCapability(CREATE_MODIFY_DESTROY_FILEPLAN_METADATA);
        if (fileplanMeta.evaluate(nodeRef) == AccessDecisionVoter.ACCESS_GRANTED)
        {
            return AccessDecisionVoter.ACCESS_GRANTED;
        }
        
        Capability recordMeta = capabilityService.getCapability(EDIT_DECLARED_RECORD_METADATA);
        if (recordMeta.evaluate(nodeRef) == AccessDecisionVoter.ACCESS_GRANTED)
        {
            return AccessDecisionVoter.ACCESS_GRANTED;
        }
        
        Capability nonRecordMetadata = capabilityService.getCapability(EDIT_NON_RECORD_METADATA);
        if (nonRecordMetadata.evaluate(nodeRef) == AccessDecisionVoter.ACCESS_GRANTED)
        {
            return AccessDecisionVoter.ACCESS_GRANTED;
        }
        
        Capability editRecordMetadata = capabilityService.getCapability(EDIT_RECORD_METADATA);
        if (editRecordMetadata.evaluate(nodeRef) == AccessDecisionVoter.ACCESS_GRANTED)
        {
            return AccessDecisionVoter.ACCESS_GRANTED;
        }
        
        return AccessDecisionVoter.ACCESS_DENIED;
    }
}
