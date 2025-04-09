/*
 * #%L
 * Alfresco Repository
 * %%
 * Copyright (C) 2005 - 2016 Alfresco Software Limited
 * %%
 * This file is part of the Alfresco software. 
 * If the software was purchased under a paid Alfresco license, the terms of 
 * the paid license agreement will prevail.  Otherwise, the software is 
 * provided under the following open source license terms:
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
 * #L%
 */
package org.alfresco.repo.node;

import org.alfresco.repo.policy.ClassPolicy;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;

/**
 * Node archive service policies
 * 
 * @author Viachaslau Tsikhanovich
 */
public interface NodeArchiveServicePolicies
{
    public interface BeforePurgeNodePolicy extends ClassPolicy
    {
        public static final QName QNAME = QName.createQName(NamespaceService.ALFRESCO_URI, "beforePurgeNode");

        /**
         * Called before a node is purged (deleted from archive).
         * 
         * @param nodeRef
         *            the node reference
         */
        public void beforePurgeNode(NodeRef nodeRef);
    }

    interface BeforeRestoreArchivedNodePolicy extends ClassPolicy
    {
        QName QNAME = QName.createQName(NamespaceService.ALFRESCO_URI, "beforeRestoreArchivedNode");

        /**
         * Called before an archived node is restored.
         *
         * @param nodeRef
         *            the node reference
         */
        void beforeRestoreArchivedNode(NodeRef nodeRef);
    }

    interface OnRestoreArchivedNodePolicy extends ClassPolicy
    {
        QName QNAME = QName.createQName(NamespaceService.ALFRESCO_URI, "onRestoreArchivedNode");

        /**
         * Called after an archived node is restored.
         *
         * @param nodeRef
         *            the node reference
         */
        void onRestoreArchivedNode(NodeRef nodeRef);
    }

}
