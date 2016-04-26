package org.alfresco.web.bean.trashcan;

import java.text.MessageFormat;

import javax.faces.context.FacesContext;

import org.alfresco.web.app.Application;
import org.alfresco.web.bean.repository.Repository;
import org.alfresco.web.ui.common.ReportedException;
import org.alfresco.web.ui.common.Utils;

public class TrashcanDeleteAllItemsDialog extends TrashcanDialog
{
    private static final long serialVersionUID = 2537803727179629546L;
    
    private static final String MSG_YES = "yes";
    private static final String MSG_NO = "no";

    private String deleteAllItems(FacesContext context, String outcome)
    {
        if (property.isInProgress())
            return null;

        property.setInProgress(true);

        try
        {
            property.getNodeArchiveService().purgeAllArchivedNodes(Repository.getStoreRef());
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
        return deleteAllItems(context, outcome);

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
