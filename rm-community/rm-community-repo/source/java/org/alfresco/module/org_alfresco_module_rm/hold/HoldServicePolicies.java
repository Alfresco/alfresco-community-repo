/*
 * #%L
 * Alfresco Records Management Module
 * %%
 * Copyright (C) 2005 - 2019 Alfresco Software Limited
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

package org.alfresco.module.org_alfresco_module_rm.hold;

import org.alfresco.repo.policy.ClassPolicy;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;

/**
 * Hold Service Policies
 *
 * @author Ramona Popa
 * @since 3.3
 */

public interface HoldServicePolicies
{

    /**
     * Policy names
     */
    public static final QName BEFORE_CREATE_HOLD = QName.createQName(NamespaceService.ALFRESCO_URI, "beforeCreateHold");
    public static final QName ON_CREATE_HOLD = QName.createQName(NamespaceService.ALFRESCO_URI, "onCreateHold");
    public static final QName BEFORE_DELETE_HOLD = QName.createQName(NamespaceService.ALFRESCO_URI, "beforeDeleteHold");
    public static final QName ON_DELETE_HOLD = QName.createQName(NamespaceService.ALFRESCO_URI, "onDeleteHold");

    interface BeforeCreateHoldPolicy extends ClassPolicy
    {
        /**
         * Called before a hold is created.
         *
         * @param name   name of the hold to be created
         * @param reason reason for the hold to be created
         */
        void beforeCreateHold(String name, String reason);
    }

    interface OnCreateHoldPolicy extends ClassPolicy
    {
        /**
         * Called when a hold is created.
         *
         * @param hold node reference
         */
        void onCreateHold(NodeRef hold);
    }

    interface BeforeDeleteHoldPolicy extends ClassPolicy
    {
        /**
         * Called before a hold is created.
         *
         * @param hold node reference
         */
        void beforeDeleteHold(NodeRef hold);
    }

    interface OnDeleteHoldPolicy extends ClassPolicy
    {
        /**
         * Called when a hold is deleted.
         *
         * @param hold node reference
         */
        void onDeleteHold(NodeRef hold);
    }
}
