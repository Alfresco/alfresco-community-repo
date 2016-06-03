package org.alfresco.web.bean.coci;

import javax.faces.context.FacesContext;

import org.alfresco.web.app.Application;

public class CCEditFileDialog extends CheckinCheckoutDialog
{
    private static final long serialVersionUID = -1145049277343144264L;
    
    private final static String MSG_LEFT_QUOTE = "left_qoute";
    private final static String MSG_RIGHT_QUOTE = "right_quote";
    public static final String LBL_CLOSE = "close";

    @Override
    protected String finishImpl(FacesContext context, String outcome) throws Exception
    {

        return super.cancel();
    }

    @Override
    public String cancel()
    {
        property.setDocument(null);
        property.setWorkingDocument(null);
        resetState();
        return super.cancel();
    }

    @Override
    public String getCancelButtonLabel()
    {
        return Application.getMessage(FacesContext.getCurrentInstance(), LBL_CLOSE);
    }

    @Override
    public String getContainerTitle()
    {
        FacesContext fc = FacesContext.getCurrentInstance();
        return Application.getMessage(fc, MSG_LEFT_QUOTE) + property.getDocument().getName() + Application.getMessage(fc, MSG_RIGHT_QUOTE);
    }

}
