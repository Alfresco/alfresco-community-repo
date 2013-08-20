/*
 * Copyright (C) 2005-2012 Alfresco Software Limited.
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
package org.alfresco.cmis.mapping;

import org.alfresco.cmis.CMISAllowedActionEnum;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.lock.LockService;
import org.alfresco.service.cmr.lock.LockType;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.apache.chemistry.abdera.ext.CMISAllowableActions;

/**
 * This evaluator determines an action availability in accordance with {@link LockType} lock parameter and in accordance with rules of checking application of the lock. The rules
 * are:<br />
 * - is it expected that object had been locked
 * <br />
 * This evaluator is generic, because it is used in the scope of {@link CompositeActionEvaluator}
 * 
 * @author Dmitry Velichkevich
 */
public class ObjectLockedActionEvaluator<ObjectType> extends AbstractActionEvaluator<ObjectType>
{
    private NodeService nodeService;

    private LockService lockService;

    private LockType lockType;

    private boolean lockExpected;

    /**
     * Constructor
     * 
     * @param lockType - {@link LockType} enumeration value, which determines type of the lock, which should be validated
     * @param lockExpected - {@link Boolean} value, which determines: <code>true</code> - object should be locked and lock should satisfy to the <code>lockType</code>,
     *        <code>false</code> - object should not have the <code>lockType</code> lock
     * @param serviceRegistry - {@link ServiceRegistry} instance
     * @param action - {@link CMISAllowableActions} enumeration value, which determines the action to check
     */
    protected ObjectLockedActionEvaluator(LockType lockType, boolean lockExpected, ServiceRegistry serviceRegistry, CMISAllowedActionEnum action)
    {
        super(serviceRegistry, action);
        this.lockType = lockType;
        this.lockExpected = lockExpected;

        nodeService = serviceRegistry.getNodeService();
        lockService = serviceRegistry.getLockService();
    }

    @Override
    public boolean isAllowed(ObjectType object)
    {
        NodeRef nodeRef = (object instanceof NodeRef) ? ((NodeRef) object) : (null);

        if ((null != lockType) && nodeService.exists(nodeRef))
        {
            boolean locked = lockType == lockService.getLockType(nodeRef);
            return (lockExpected && locked) || (!lockExpected && !locked);
        }

        return false;
    }
}
