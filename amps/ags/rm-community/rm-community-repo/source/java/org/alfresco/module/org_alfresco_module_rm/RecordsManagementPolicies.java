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

package org.alfresco.module.org_alfresco_module_rm;

import java.io.Serializable;
import java.util.Map;

import org.alfresco.repo.policy.ClassPolicy;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;

/**
 * Interface containing records management policies
 *
 * @author Roy Wetherall
 */
public interface RecordsManagementPolicies
{
    /** Policy names */
    QName BEFORE_RM_ACTION_EXECUTION = QName.createQName(NamespaceService.ALFRESCO_URI, "beforeRMActionExecution");
    QName ON_RM_ACTION_EXECUTION = QName.createQName(NamespaceService.ALFRESCO_URI, "onRMActionExecution");
    QName BEFORE_CREATE_REFERENCE = QName.createQName(NamespaceService.ALFRESCO_URI, "beforeCreateReference");
    QName ON_CREATE_REFERENCE = QName.createQName(NamespaceService.ALFRESCO_URI, "onCreateReference");
    QName BEFORE_REMOVE_REFERENCE = QName.createQName(NamespaceService.ALFRESCO_URI, "beforeRemoveReference");
    QName ON_REMOVE_REFERENCE = QName.createQName(NamespaceService.ALFRESCO_URI, "onRemoveReference");
    QName BEFORE_RECORD_DECLARATION = QName.createQName(NamespaceService.ALFRESCO_URI, "beforeRecordDeclaration");
    QName ON_RECORD_DECLARATION = QName.createQName(NamespaceService.ALFRESCO_URI, "onRecordDeclaration");
    QName BEFORE_RECORD_REJECTION = QName.createQName(NamespaceService.ALFRESCO_URI, "beforeRecordRejection");
    QName ON_RECORD_REJECTION = QName.createQName(NamespaceService.ALFRESCO_URI, "onRecordRejection");

    /** Before records management action execution */
    interface BeforeRMActionExecution extends ClassPolicy
    {
        void beforeRMActionExecution(NodeRef nodeRef, String name, Map<String, Serializable> parameters);
    }

    /** On records management action execution */
    interface OnRMActionExecution extends ClassPolicy
    {
        void onRMActionExecution(NodeRef nodeRef, String name, Map<String, Serializable> parameters);
    }

    /** Before creation of reference */
    interface BeforeCreateReference extends ClassPolicy
    {
        void beforeCreateReference(NodeRef fromNodeRef, NodeRef toNodeRef, QName reference);
    }

    /** On creation of reference */
    interface OnCreateReference extends ClassPolicy
    {
        void onCreateReference(NodeRef fromNodeRef, NodeRef toNodeRef, QName reference);
    }

    /** Before removal of reference */
    interface BeforeRemoveReference extends ClassPolicy
    {
        void beforeRemoveReference(NodeRef fromNodeRef, NodeRef toNodeRef, QName reference);
    }

    /**
     * On removal of reference
     *
     * @since 1.0
     */
    interface OnRemoveReference extends ClassPolicy
    {
        /**
         * @param fromNodeRef   from node reference
         * @param toNodeRef     to node reference
         * @param reference     name of reference
         */
        void onRemoveReference(NodeRef fromNodeRef, NodeRef toNodeRef, QName reference);
    }

    /**
     * Before record file policy
     *
     * @since 2.2
     */
    interface BeforeFileRecord extends ClassPolicy
    {
        /** policy name */
        QName QNAME = QName.createQName(NamespaceService.ALFRESCO_URI, "beforeRecordFile");

        /**
         * @param nodeRef   node reference
         */
        void beforeFileRecord(NodeRef nodeRef);
    }

    /**
     * On record file policy
     *
     * @since 2.2
     */
    interface OnFileRecord extends ClassPolicy
    {
        /** policy name */
        QName QNAME = QName.createQName(NamespaceService.ALFRESCO_URI, "onRecordFile");

        /**
         * @param nodeRef   node reference
         */
        void onFileRecord(NodeRef nodeRef);
    }

    /**
     * Before record declaration
     * @since 2.5
     */
    interface BeforeRecordDeclaration extends ClassPolicy
    {
        void beforeRecordDeclaration(NodeRef nodeRef);
    }

    /**
     * On record declaration
     * @since 2.5
     */
    interface OnRecordDeclaration extends ClassPolicy
    {
        void onRecordDeclaration(NodeRef nodeRef);
    }

    /**
     * Before record rejection
     * @since 2.5
     */
    interface BeforeRecordRejection extends ClassPolicy
    {
        void beforeRecordRejection(NodeRef nodeRef);
    }

    /**
     * On record rejection
     * @since 2.5
     */
    interface OnRecordRejection extends ClassPolicy
    {
        void onRecordRejection(NodeRef nodeRef);
    }
}
