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
package org.alfresco.module.org_alfresco_module_rm.capability.impl;

import java.io.Serializable;
import java.util.Map;

import org.alfresco.module.org_alfresco_module_rm.capability.declarative.CompositeCapability;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;

/**
 * Update capability implementation.
 * 
 * @author andyh
 */
public class UpdateCapability extends CompositeCapability
{
    /**
     * 
     * @param nodeRef
     * @param aspectQName
     * @param properties
     * @return
     */
    public int evaluate(NodeRef nodeRef, QName aspectQName, Map<QName, Serializable> properties)
    {
       // if ((aspectQName != null) && (voter.isProtectedAspect(nodeRef, aspectQName)))
       // {
       //     return AccessDecisionVoter.ACCESS_DENIED;
       // }
        
      //  if ((properties != null) && (voter.includesProtectedPropertyChange(nodeRef, properties)))
      //  {
      //      return AccessDecisionVoter.ACCESS_DENIED;
      //  }
        
        return evaluate(nodeRef);
    }
}
