package org.alfresco.web.bean.users;

import javax.faces.context.FacesContext;

import org.alfresco.web.app.Application;
import org.alfresco.web.bean.dialog.BaseDialogBean;

public class UserConsoleDialog extends BaseDialogBean
{
   private static final long serialVersionUID = -217662039833342921L;

    private static final String MSG_CLOSE = "close";

    @Override
    protected String finishImpl(FacesContext context, String outcome) throws Exception
    {
        return null;
    }

    @Override
    public String getCancelButtonLabel()
    {
        return Application.getMessage(FacesContext.getCurrentInstance(), MSG_CLOSE);
    }
}