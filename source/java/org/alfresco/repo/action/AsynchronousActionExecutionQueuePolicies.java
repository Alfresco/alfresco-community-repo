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

import org.alfresco.repo.policy.ClassPolicy;
import org.alfresco.service.cmr.action.Action;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;

/**
 * Asynchronous action execution queue policies
 * 
 * @author Roy Wetherall
 */
public interface AsynchronousActionExecutionQueuePolicies
{
    /**
     * Policy invoked when an async action has completed execution
     */
    public interface OnAsyncActionExecute extends ClassPolicy
    {
        /** QName of the policy */
        public static final QName QNAME = QName.createQName(NamespaceService.ALFRESCO_URI, "onAsyncActionExecute");
        
        /**
         * @param action                    action
         * @param actionedUponNodeRef       actioned upon node reference
         */
        public void onAsyncActionExecute(Action action, NodeRef actionedUponNodeRef);
    }
	
}
