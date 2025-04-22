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
package org.alfresco.repo.version;

import java.io.Serializable;
import java.util.Map;

import org.alfresco.api.AlfrescoPublicApi;
import org.alfresco.repo.policy.ClassPolicy;
import org.alfresco.repo.policy.PolicyScope;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.version.Version;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;

/**
 * Version service policy interfaces
 * 
 * @author Roy Wetherall
 */
public interface VersionServicePolicies
{
    /**
     * Before create version policy interface.
     */
    @AlfrescoPublicApi
    public interface BeforeCreateVersionPolicy extends ClassPolicy
    {
        public static final QName QNAME = QName.createQName(NamespaceService.ALFRESCO_URI, "beforeCreateVersion");

        /**
         * Called before a new version is created for a version
         * 
         * @param versionableNode
         *            reference to the node about to be versioned
         */
        public void beforeCreateVersion(NodeRef versionableNode);

    }

    /**
     * After create version policy interface
     *
     */
    @AlfrescoPublicApi
    public interface AfterCreateVersionPolicy extends ClassPolicy
    {
        public static final QName QNAME = QName.createQName(NamespaceService.ALFRESCO_URI, "afterCreateVersion");

        /**
         * Called after the version has been created
         * 
         * @param versionableNode
         *            the node that has been versioned
         * @param version
         *            the created version
         */
        public void afterCreateVersion(NodeRef versionableNode, Version version);
    }

    /**
     * On create version policy interface
     */
    @AlfrescoPublicApi
    public interface OnCreateVersionPolicy extends ClassPolicy
    {
        public static final QName QNAME = QName.createQName(NamespaceService.ALFRESCO_URI, "onCreateVersion");

        /**
         * Called during the creation of the version to determine what the versioning policy for a perticular type may be. WARNING: implementing behaviour for this policy effects the versioning behaviour of the type the behaviour is registered against.
         * 
         * @param classRef
         *            the class reference
         * @param versionableNode
         *            the versionable node reference
         * @param versionProperties
         *            the version properties
         * @param nodeDetails
         *            the details of the node to be versioned
         */
        public void onCreateVersion(
                QName classRef,
                NodeRef versionableNode,
                Map<String, Serializable> versionProperties,
                PolicyScope nodeDetails);
    }

    @AlfrescoPublicApi
    public interface OnRevertVersionPolicy extends ClassPolicy
    {
        public static final QName QNAME = QName.createQName(NamespaceService.ALFRESCO_URI, "getRevertVersionCallback");

        /**
         *
         * Called for all types and aspects before reverting a node.
         *
         * @param classRef
         *            the type or aspect qualified name
         * @param copyDetails
         *            the details of the impending revert
         * @return Return the callback that will be used to modify the revert behaviour for this type or aspect. Return <tt>null</tt> to assume the default.
         *
         *
         * @since V4.2
         */
        VersionRevertCallback getRevertVersionCallback(QName classRef, VersionRevertDetails copyDetails);
    }

    /**
     * Calculate version lable policy interface
     */
    @AlfrescoPublicApi
    public interface CalculateVersionLabelPolicy extends ClassPolicy
    {
        public String calculateVersionLabel(
                QName classRef,
                Version preceedingVersion,
                int versionNumber,
                Map<String, Serializable> verisonProperties);
    }

    /**
     * After revert version policy interface
     *
     */
    @AlfrescoPublicApi
    public interface AfterVersionRevertPolicy extends ClassPolicy
    {
        public static final QName QNAME = QName.createQName(NamespaceService.ALFRESCO_URI, "afterVersionRevert");

        /**
         * Called after the version has been reverted
         *
         * @param nodeRef
         *            the node that has been reverted
         * @param version
         *            the reverted version
         */
        public void afterVersionRevert(NodeRef nodeRef, Version version);
    }
}
