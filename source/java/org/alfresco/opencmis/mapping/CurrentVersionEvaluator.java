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
package org.alfresco.opencmis.mapping;

import java.io.Serializable;

import org.alfresco.model.ContentModel;
import org.alfresco.opencmis.dictionary.CMISActionEvaluator;
import org.alfresco.repo.version.VersionBaseModel;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.version.Version;
import org.alfresco.service.cmr.version.VersionHistory;
import org.apache.chemistry.opencmis.commons.enums.Action;

public class CurrentVersionEvaluator extends AbstractActionEvaluator<NodeRef>
{
    private CMISActionEvaluator<NodeRef> currentVersionEvaluator;
    private boolean currentVersionValue;
    private boolean nonCurrentVersionValue;

    /**
     * Construct
     * 
     * @param serviceRegistry
     */
    protected CurrentVersionEvaluator(ServiceRegistry serviceRegistry, Action action, boolean currentVersionValue,
            boolean nonCurrentVersionValue)
    {
        super(serviceRegistry, action);
        this.currentVersionValue = currentVersionValue;
        this.nonCurrentVersionValue = nonCurrentVersionValue;
    }

    /**
     * Construct
     * 
     * @param serviceRegistry
     */
    protected CurrentVersionEvaluator(ServiceRegistry serviceRegistry,
            CMISActionEvaluator<NodeRef> currentVersionEvaluator, boolean nonCurrentVersionValue)
    {
        super(serviceRegistry, currentVersionEvaluator.getAction());
        this.currentVersionEvaluator = currentVersionEvaluator;
        this.nonCurrentVersionValue = nonCurrentVersionValue;
    }

    public boolean isAllowed(NodeRef nodeRef)
    {
        if (nodeRef.getStoreRef().getProtocol().equals(VersionBaseModel.STORE_PROTOCOL))
        {
            VersionHistory versionHistory = getServiceRegistry().getVersionService().getVersionHistory(nodeRef);
            if (versionHistory != null)
            {
                Version currentVersion = versionHistory.getHeadVersion();
                Serializable versionLabel = getServiceRegistry().getNodeService().getProperty(nodeRef,
                        ContentModel.PROP_VERSION_LABEL);

                if (!currentVersion.getVersionLabel().equals(versionLabel))
                {
                    return nonCurrentVersionValue;
                }
            }
        }

        return currentVersionEvaluator == null ? currentVersionValue : currentVersionEvaluator.isAllowed(nodeRef);
    }
}
