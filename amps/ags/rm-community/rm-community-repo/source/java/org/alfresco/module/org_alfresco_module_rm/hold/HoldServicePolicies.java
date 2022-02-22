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

package org.alfresco.module.org_alfresco_module_rm.hold;

import org.alfresco.repo.policy.ClassPolicy;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;

/**
 * Hold Service Policies
 *
 * @author Ramona Popa
 * @author Roxana Lucanu
 * @since 3.3
 */

public interface HoldServicePolicies
{
    interface BeforeCreateHoldPolicy extends ClassPolicy
    {
        public static final QName QNAME = QName.createQName(NamespaceService.ALFRESCO_URI, "beforeCreateHold");
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
        public static final QName QNAME = QName.createQName(NamespaceService.ALFRESCO_URI, "onCreateHold");
        /**
         * Called when a hold is created.
         *
         * @param hold node reference
         */
        void onCreateHold(NodeRef hold);
    }

    interface BeforeDeleteHoldPolicy extends ClassPolicy
    {
        public static final QName QNAME = QName.createQName(NamespaceService.ALFRESCO_URI, "beforeDeleteHold");
        /**
         * Called before a hold is deleted.
         *
         * @param hold node reference
         */
        void beforeDeleteHold(NodeRef hold);
    }

    interface OnDeleteHoldPolicy extends ClassPolicy
    {
        public static final QName QNAME = QName.createQName(NamespaceService.ALFRESCO_URI, "onDeleteHold");

        /**
         * Called when a hold is deleted.
         *
         * @param holdname name of the deleted hold
         */
        void onDeleteHold(String holdname);
    }

    interface BeforeAddToHoldPolicy extends ClassPolicy
    {
        QName QNAME = QName.createQName(NamespaceService.ALFRESCO_URI, "beforeAddToHold");

        /**
         * Called before adding content to hold.
         *
         * @param hold           the hold to be added into
         * @param contentNodeRef the item to be added to hold
         */
        void beforeAddToHold(NodeRef hold, NodeRef contentNodeRef);
    }

    interface OnAddToHoldPolicy extends ClassPolicy
    {
        QName QNAME = QName.createQName(NamespaceService.ALFRESCO_URI, "onAddToHold");

        /**
         * Called when content is added to hold.
         *
         * @param hold           the hold to be added into
         * @param contentNodeRef the item to be added to hold
         */
        void onAddToHold(NodeRef hold, NodeRef contentNodeRef);
    }

    interface BeforeRemoveFromHoldPolicy extends ClassPolicy
    {
        QName QNAME = QName.createQName(NamespaceService.ALFRESCO_URI, "beforeRemoveFromHold");

        /**
         * Called before removing content from hold.
         *
         * @param hold           the hold to be removed from
         * @param contentNodeRef the item to be removed from hold
         */
        void beforeRemoveFromHold(NodeRef hold, NodeRef contentNodeRef);
    }

    interface OnRemoveFromHoldPolicy extends ClassPolicy
    {
        QName QNAME = QName.createQName(NamespaceService.ALFRESCO_URI, "onRemoveFromHold");

        /**
         * Called when removing content from hold.
         *
         * @param hold           the hold to be removed from
         * @param contentNodeRef the item to be removed from hold
         */
        void onRemoveFromHold(NodeRef hold, NodeRef contentNodeRef);
    }

}
