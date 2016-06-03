package org.alfresco.web.bean.trashcan;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

import javax.faces.context.FacesContext;

import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.web.app.Application;
import org.alfresco.web.bean.repository.Node;
import org.alfresco.web.bean.repository.Repository;
import org.alfresco.web.ui.common.ReportedException;
import org.alfresco.web.ui.common.Utils;

public class TrashcanDeleteListedItemsDialog extends TrashcanDialog
{
    private static final long serialVersionUID = 5576836588148974609L;
    
    private static final String MSG_YES = "yes";
    private static final String MSG_NO = "no";

    private String deleteListedItems(FacesContext context, String outcome)
    {
        if (property.isInProgress())
            return null;

        property.setInProgress(true);

        try
        {
            List<NodeRef> nodeRefs = new ArrayList<NodeRef>(property.getListedItems().size());
            for (Node node : property.getListedItems())
            {
                nodeRefs.add(node.getNodeRef());
            }
            property.getNodeArchiveService().purgeArchivedNodes(nodeRefs);
        }
        catch (Throwable err)
        {
            Utils.addErrorMessage(MessageFormat.format(Application.getMessage(context, Repository.ERROR_GENERIC), err.getMessage()), err);
            ReportedException.throwIfNecessary(err);
        }
        finally
        {
            property.setInProgress(false);
        }

        return outcome;
    }

    @Override
    protected String finishImpl(FacesContext context, String outcome) throws Exception
    {
        return deleteListedItems(context, outcome);

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
