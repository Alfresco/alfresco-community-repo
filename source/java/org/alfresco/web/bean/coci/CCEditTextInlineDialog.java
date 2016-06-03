package org.alfresco.web.bean.coci;

import javax.faces.context.FacesContext;

import org.alfresco.web.app.Application;

public class CCEditTextInlineDialog extends CheckinCheckoutDialog
{
    private static final long serialVersionUID = 4657371875928010937L;
    
    public static final String LBL_SAVE = "save";

    @Override
    protected String finishImpl(FacesContext context, String outcome) throws Exception
    {
        return editInline(context, outcome);

    }

    @Override
    public boolean getFinishButtonDisabled()
    {
        return false;
    }

    @Override
    public String getFinishButtonLabel()
    {

        return Application.getMessage(FacesContext.getCurrentInstance(), LBL_SAVE);
    }

    @Override
    public String getContainerTitle()
    {
        return property.getDocument().getName();
    }

}
