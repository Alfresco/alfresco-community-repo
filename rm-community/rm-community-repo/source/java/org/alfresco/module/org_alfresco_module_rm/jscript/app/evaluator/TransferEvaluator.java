 
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
