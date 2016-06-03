package org.alfresco.web.bean.trashcan;

import java.util.ArrayList;
import java.util.List;

import javax.faces.context.FacesContext;

import org.alfresco.repo.node.archive.RestoreNodeReport;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.web.app.Application;
import org.alfresco.web.bean.repository.Node;

public class TrashcanRecoverListedItemsDialog extends TrashcanDialog
{
    private static final long serialVersionUID = 5500454626559426051L;
    
    private static final String OUTCOME_RECOVERY_REPORT = "dialog:recoveryReport";
    private static final String MSG_NO = "no";
    private static final String MSG_YES = "yes";

    private String recoverListedItems(FacesContext context, String outcome)
    {
        if (property.isInProgress())
            return null;

        property.setInProgress(true);

        try
        {

            // restore the nodes - the user may have requested a restore to a
            // different parent
            List<NodeRef> nodeRefs = new ArrayList<NodeRef>(property.getListedItems().size());
            for (Node node : property.getListedItems())
            {
                nodeRefs.add(node.getNodeRef());
            }
            List<RestoreNodeReport> reports;
            if (property.getDestination() == null)
            {
                reports = property.getNodeArchiveService().restoreArchivedNodes(nodeRefs);
            }
            else
            {
                reports = property.getNodeArchiveService().restoreArchivedNodes(nodeRefs, property.getDestination(), null, null);
            }

            saveReportDetail(reports);

        }
        finally
        {
            property.setInProgress(false);
        }

        return OUTCOME_RECOVERY_REPORT;
    }

    @Override
    protected String finishImpl(FacesContext context, String outcome) throws Exception
    {

        return recoverListedItems(context, outcome);

    }

    @Override
    public String getCancelButtonLabel()
    {
        return Application.getMessage(FacesContext.getCurrentInstance(), MSG_NO);
    }

    @Override
    public boolean getFinishButtonDisabled()
    {
        return false;
    }

    @Override
    public String getFinishButtonLabel()
    {
        return Application.getMessage(FacesContext.getCurrentInstance(), MSG_YES);
    }

}
