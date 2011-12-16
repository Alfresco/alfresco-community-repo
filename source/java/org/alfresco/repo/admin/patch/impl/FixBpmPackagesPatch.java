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
package org.alfresco.repo.admin.patch.impl;

import java.util.List;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.admin.patch.AbstractPatch;
import org.alfresco.repo.importer.ImporterBootstrap;
import org.alfresco.repo.workflow.WorkflowModel;
import org.alfresco.service.cmr.admin.PatchException;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.service.namespace.RegexQNamePattern;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.extensions.surf.util.I18NUtil;

/**
 * Patch that updates workflow package type and package items associations
 * 
 * @see https://issues.alfresco.com/jira/browse/ETHREEOH-3613
 * @see https://issues.alfresco.com/jira/browse/ALF-11499
 * @author Arseny Kovalchuk
 * @since 3.4.7
 */
public class FixBpmPackagesPatch extends AbstractPatch
{
    private static final String MSG_SUCCESS = "patch.fixBpmPackages.result";

    private static final String ERR_MSG_INVALID_BOOTSTRAP_STORE = "patch.fixBpmPackages.invalidBootsrapStore";
    private static final String ERR_MSG_EMPTY_CONTAINER = "patch.fixBpmPackages.emptyContainer";

    private static final Log logger = LogFactory.getLog(FixBpmPackagesPatch.class);

    private ImporterBootstrap importerBootstrap;

    public void setImporterBootstrap(ImporterBootstrap importerBootstrap)
    {
        this.importerBootstrap = importerBootstrap;
    }

    @Override
    protected String applyInternal() throws Exception
    {
        // Package counter for report
        int packagesCount = 0;
        StoreRef store = importerBootstrap.getStoreRef();
        if (store == null)
        {
            throw new PatchException(ERR_MSG_INVALID_BOOTSTRAP_STORE);
        }

        // Get root node for store
        NodeRef rootRef = nodeService.getRootNode(store);

        if (logger.isDebugEnabled())
            logger.debug("StoreRef:" + store + " RootNodeRef: " + rootRef);

        // Get /sys:system container path, if it doesn't exist there is something wrong with the repo
        String sysContainer = importerBootstrap.getConfiguration().getProperty("system.system_container.childname");
        QName sysContainerQName = QName.createQName(sysContainer, namespaceService);

        List<ChildAssociationRef> refs = nodeService.getChildAssocs(rootRef, ContentModel.ASSOC_CHILDREN, sysContainerQName);

        if (refs == null || refs.size() == 0)
            throw new PatchException(ERR_MSG_EMPTY_CONTAINER, sysContainer);

        NodeRef sysNodeRef = refs.get(0).getChildRef();

        // Get /sys:system/sys:workflow container, if it doesn't exist there is something wrong with the repo
        String sysWorkflowContainer = importerBootstrap.getConfiguration().getProperty("system.workflow_container.childname");
        QName sysWorkflowQName = QName.createQName(sysWorkflowContainer, namespaceService);

        refs = nodeService.getChildAssocs(sysNodeRef, ContentModel.ASSOC_CHILDREN, sysWorkflowQName);

        if (refs == null || refs.size() == 0)
            throw new PatchException(ERR_MSG_EMPTY_CONTAINER, sysWorkflowContainer);

        NodeRef workflowContainerRef = refs.get(0).getChildRef();

        // Try to get /sys:system/sys:workflow/cm:packages, if there is no such node, then it wasn't created yet,
        // so there is nothing to convert
        refs = nodeService.getChildAssocs(workflowContainerRef, ContentModel.ASSOC_CHILDREN, RegexQNamePattern.MATCH_ALL);

        if (refs == null || refs.size() == 0)
        {
            if (logger.isDebugEnabled())
                logger.debug("There are no any packages in the container " + sysWorkflowContainer);
            return I18NUtil.getMessage(MSG_SUCCESS, packagesCount);
        }
        // Get /sys:system/sys:workflow/cm:packages container NodeRef
        NodeRef packagesContainerRef = refs.get(0).getChildRef();
        // Get workflow packages to be converted
        refs = nodeService.getChildAssocs(packagesContainerRef, ContentModel.ASSOC_CONTAINS, RegexQNamePattern.MATCH_ALL);

        if (logger.isDebugEnabled())
            logger.debug("Found " + refs.size() + " packages to convert");

        NodeRef packageRef = null;
        // For each package we get package items and convert their type from cm:systemfolder to bpm:package
        // Also we convert associations between packages and their items from cm:contains to bpm:packageContains
        for (ChildAssociationRef assocRef : refs)
        {
            packageRef = assocRef.getChildRef();
            QName typeQname = nodeService.getType(packageRef);
            String name = (String) nodeService.getProperty(packageRef, ContentModel.PROP_NAME);
            if (logger.isDebugEnabled())
                logger.debug("Package " + name + " type " + typeQname);
            // New type of the package is bpm:package
            nodeService.setType(packageRef, WorkflowModel.TYPE_PACKAGE);
            // Get all package items
            List<ChildAssociationRef> packageItemsAssocs = nodeService.getChildAssocs(packageRef, ContentModel.ASSOC_CONTAINS, RegexQNamePattern.MATCH_ALL);

            for (ChildAssociationRef itemAssoc : packageItemsAssocs)
            {
                NodeRef parentRef = itemAssoc.getParentRef();
                NodeRef childRef = itemAssoc.getChildRef();
                String itemName = (String) nodeService.getProperty(childRef, ContentModel.PROP_NAME);
                // To avoid unnecessary deletion of the child item, we check if the association is not primary
                // For the package item it should be not primary association.
                if (itemAssoc.isPrimary())
                {
                    logger.error("Association between package: " + name + " and item: " + itemName + " is primary association, so removing this assiciation will result in child node deletion");
                    continue;
                }
                boolean assocRemoved = nodeService.removeChildAssociation(itemAssoc);
                if (assocRemoved)
                {
                    if (logger.isDebugEnabled())
                        logger.debug("Association between package: " + name + " and item: " + itemName + " was removed");
                }
                else
                {
                    if (logger.isErrorEnabled())
                        logger.error("Association between package: " + name + " and item: " + itemName + " doesn't exist");
                    // If there is no association we won't create a new one
                    continue;
                }
                // Recreate new association between package and particular item as bpm:packageContains
                /* ChildAssociationRef newChildAssoc = */nodeService.addChild(parentRef, childRef, WorkflowModel.ASSOC_PACKAGE_CONTAINS,
                        QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, QName.createValidLocalName(itemName)));
                if (logger.isDebugEnabled())
                {
                    logger.debug("New association has been created between package: " + name + " and item: " + itemName);
                }
            }
            packagesCount++;

        }
        return I18NUtil.getMessage(MSG_SUCCESS, packagesCount);
    }

}
