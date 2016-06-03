package org.alfresco.web.bean.coci;

import javax.faces.context.FacesContext;

import org.alfresco.web.app.AlfrescoNavigationHandler;
import org.alfresco.web.app.Application;

public class CCWorkingCopyMissingDialog extends CheckinCheckoutDialog
{
    private static final long serialVersionUID = 8067485292477557683L;
    
    public static final String MSG_WORKING_COPY_FOR = "working_copy_for";
    private final static String MSG_LEFT_QUOTE = "left_qoute";
    private final static String MSG_RIGHT_QUOTE = "right_quote";
    public static final String LBL_CLOSE = "close";

    @Override
    protected String finishImpl(FacesContext context, String outcome) throws Exception
    {
        FacesContext.getCurrentInstance().getExternalContext().getSessionMap().remove(AlfrescoNavigationHandler.EXTERNAL_CONTAINER_SESSION);

        return getDefaultCancelOutcome();
    }

    @Override
    public String cancel()
    {
        FacesContext.getCurrentInstance().getExternalContext().getSessionMap().remove(AlfrescoNavigationHandler.EXTERNAL_CONTAINER_SESSION);
        
        return AlfrescoNavigationHandler.CLOSE_DIALOG_OUTCOME + ":browse";
    }

    @Override
    public String getContainerTitle()
    {
        FacesContext fc = FacesContext.getCurrentInstance();
        return Application.getMessage(fc, MSG_WORKING_COPY_FOR) + " " + Application.getMessage(fc, MSG_LEFT_QUOTE)
            + property.getDocument().getName() + Application.getMessage(fc, MSG_RIGHT_QUOTE);
    }

    @Override
    public String getCancelButtonLabel()
    {
        return Application.getMessage(FacesContext.getCurrentInstance(), LBL_CLOSE);
    }
}
