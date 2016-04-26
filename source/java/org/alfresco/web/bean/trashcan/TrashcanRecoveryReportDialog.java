package org.alfresco.web.bean.trashcan;

import javax.faces.context.FacesContext;

import org.alfresco.web.app.Application;

public class TrashcanRecoveryReportDialog extends TrashcanDialog
{

    private static final long serialVersionUID = -3381444990908748991L;
    
    private final static String MSG_CLOSE = "close";

    @Override
    protected String finishImpl(FacesContext context, String outcome) throws Exception
    {
        return outcome;
    }

    @Override
    public String getCancelButtonLabel()
    {
        return Application.getMessage(FacesContext.getCurrentInstance(), MSG_CLOSE);
    }

    @Override
    protected String getDefaultCancelOutcome()
    {
        return "dialog:close[2]";
    }

}
