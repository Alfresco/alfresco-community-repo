/*
 * #%L
 * Alfresco Records Management Module
 * %%
 * Copyright (C) 2005 - 2016 Alfresco Software Limited
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

package org.alfresco.module.org_alfresco_module_rm.model;

import java.security.InvalidParameterException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.module.org_alfresco_module_rm.util.ServiceBaseImpl;
import org.alfresco.repo.policy.BehaviourFilter;
import org.alfresco.repo.policy.annotation.BehaviourRegistry;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.google.common.collect.Sets;

/**
 * Convenient base class for behaviour beans.
 *
 * @author Roy Wetherall
 * @since 2.2
 */
public abstract class BaseBehaviourBean extends ServiceBaseImpl
                                        implements RecordsManagementModel,
                                                   BehaviourRegistry
{
    /** Logger */
    protected static final Log LOGGER = LogFactory.getLog(BaseBehaviourBean.class);

    /** behaviour filter */
    protected BehaviourFilter behaviourFilter;

    /** behaviour map */
    protected Map<String, org.alfresco.repo.policy.Behaviour> behaviours = new HashMap<String, org.alfresco.repo.policy.Behaviour>(7);

    /**
     * @param behaviourFilter   behaviour filter
     */
    public void setBehaviourFilter(BehaviourFilter behaviourFilter)
    {
        this.behaviourFilter = behaviourFilter;
    }

    /**
     * @see org.alfresco.repo.policy.annotation.BehaviourRegistry#registerBehaviour(java.lang.String, org.alfresco.repo.policy.Behaviour)
     */
    @Override
    public void registerBehaviour(String name, org.alfresco.repo.policy.Behaviour behaviour)
    {
        if (behaviours.containsKey(name))
        {
            throw new AlfrescoRuntimeException("Can not register behaviour, because name " + name + "has already been used.");
        }

        behaviours.put(name, behaviour);
    }

    /**
     * @see org.alfresco.repo.policy.annotation.BehaviourRegistry#getBehaviour(java.lang.String)
     */
    @Override
    public org.alfresco.repo.policy.Behaviour getBehaviour(String name)
    {
        return behaviours.get(name);
    }

    /**
     * Helper method that checks if the newly created child association complies with the RM rules
     * @param parent the parent node
     * @param childType the child node
     * @param acceptedUniqueChildType a list of node types that are accepted as children of the provided parent only once
     * @param acceptedMultipleChildType a list of node types that are accepted as children of the provided parent multiple times
     * @throws InvalidParameterException if the child association doesn't comply with the RM rules
     */
    protected void validateNewChildAssociation(NodeRef parent, NodeRef child, List<QName> acceptedUniqueChildType, List<QName> acceptedMultipleChildType) throws InvalidParameterException
    {
        QName childType = getInternalNodeService().getType(child);
        if(acceptedUniqueChildType.contains(childType))
        {
            // check the user is not trying to create multiple children of a type that is only accepted once
            if(nodeService.getChildAssocs(parent, Sets.newHashSet(childType)).size() > 1)
            {
                throw new InvalidParameterException("Operation failed. Multiple children of this type are not allowed.");
            }
        }
        else if(!acceptedMultipleChildType.contains(childType))
        {
            throw new InvalidParameterException("Operation failed. Children of type " + childType + " are not allowed");
        }
    }

    /**
     * Helper method that checks if the newly created child association is between the sub-types of accepted types.
     * @param childType the child node
     * @param acceptedMultipleChildType a list of node types that are accepted as children of the provided parent multiple times
     * @throws InvalidParameterException if the child association isn't between the sub-types of accepted types
     */
    protected void validateNewChildAssociationSubTypesIncluded(NodeRef child, List<QName> acceptedMultipleChildType) throws InvalidParameterException
    {
        QName childType = getInternalNodeService().getType(child);
        for(QName type :  acceptedMultipleChildType)
        {
            if(instanceOf(childType, type))
            {
                return;
            }
        }
        //no match was found in sub-types of permitted types list
        throw new InvalidParameterException("Operation failed. Children of type " + childType + " are not allowed");
    }
}
