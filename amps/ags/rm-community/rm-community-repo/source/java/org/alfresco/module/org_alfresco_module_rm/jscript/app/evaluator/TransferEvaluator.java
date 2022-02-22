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

package org.alfresco.module.org_alfresco_module_rm.jscript.app.evaluator;

import java.util.List;

import org.alfresco.module.org_alfresco_module_rm.jscript.app.BaseEvaluator;
import org.alfresco.module.org_alfresco_module_rm.model.RecordsManagementModel;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.security.permissions.AccessDeniedException;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.RegexQNamePattern;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Evaluates whether the node in question is transferring is either a transfer or accession.
 *
 * @author Roy Wetherall
 */
public class TransferEvaluator extends BaseEvaluator
{
    /** Logger */
    private static Log logger = LogFactory.getLog(TransferEvaluator.class);

    /** indicates whether we are looking for accessions or transfers */
    private boolean transferAccessionIndicator = false;

    /**
     * @param transferAccessionIndicator    true if accession, false otherwise
     */
    public void setTransferAccessionIndicator(boolean transferAccessionIndicator)
    {
        this.transferAccessionIndicator = transferAccessionIndicator;
    }

    /**
     * @see org.alfresco.module.org_alfresco_module_rm.jscript.app.BaseEvaluator#evaluateImpl(org.alfresco.service.cmr.repository.NodeRef)
     */
    @Override
    protected boolean evaluateImpl(NodeRef nodeRef)
    {
        boolean result = false;

        NodeRef transfer = getTransferNodeRef(nodeRef);
        if (transfer != null)
        {
            try
            {
                boolean actual = ((Boolean)nodeService.getProperty(transfer, RecordsManagementModel.PROP_TRANSFER_ACCESSION_INDICATOR)).booleanValue();
                result = (actual == transferAccessionIndicator);
            }
            catch (AccessDeniedException ade)
            {
                logger.info("The user '"
                        + AuthenticationUtil.getFullyAuthenticatedUser()
                        + "' does not have permissions on the node '"
                        + transfer + "'.");
            }
        }

        return result;
    }

    /**
     * Helper method to get the transfer node reference.
     * <p>
     * Takes into account records in tranferred record folders.
     *
     * @param nodeRef               node reference
     * @return {@link NodeRef}      transfer node
     */
    private NodeRef getTransferNodeRef(NodeRef nodeRef)
    {
        NodeRef result = null;

        List<ChildAssociationRef> parents = nodeService.getParentAssocs(nodeRef, RecordsManagementModel.ASSOC_TRANSFERRED, RegexQNamePattern.MATCH_ALL);
        if (parents.size() == 1)
        {
            result = parents.get(0).getParentRef();
        }
        else
        {
            if (recordService.isRecord(nodeRef))
            {
                for (NodeRef recordFolder : recordFolderService.getRecordFolders(nodeRef))
                {
                    result = getTransferNodeRef(recordFolder);
                    if (result != null)
                    {
                        break;
                    }
                }
            }
        }

        return result;
    }
}
