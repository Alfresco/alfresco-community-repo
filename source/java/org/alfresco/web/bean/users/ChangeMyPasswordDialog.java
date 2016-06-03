package org.alfresco.web.bean.users;

import javax.faces.context.FacesContext;

import org.alfresco.model.ContentModel;
import org.alfresco.web.app.Application;
import org.alfresco.web.ui.common.ReportedException;
import org.alfresco.web.ui.common.Utils;

public class ChangeMyPasswordDialog extends UsersDialog
{

    private static final long serialVersionUID = 1965846039555088108L;

    private static final String MSG_FINISH_BUTTON = "finish_button";
    private static final String MSG_ERROR_INCORRECT_OLD_PASSWORD = "error_incorrect_old_password";

    @Override
    protected String finishImpl(FacesContext context, String outcome) throws Exception
    {
        String result = changeMyPasswordOK(outcome, context);
        if (result == null)
        {
            isFinished = false;
        }
        return result;
    }

    public String getFinishButtonLabel()
    {
        return Application.getMessage(FacesContext.getCurrentInstance(), MSG_FINISH_BUTTON);
    }

    public boolean getFinishButtonDisabled()
    {
        return false;
    }

    /**
     * Action handler called for OK button press on Change My Password screen For this screen the user is required to enter their old password - effectively login.
     */
    public String changeMyPasswordOK(String newOutcome, FacesContext newContext)
    {
        String outcome = newOutcome;

        if (properties.getPassword() != null && properties.getConfirm() != null && properties.getPassword().equals(properties.getConfirm()))
        {
            try
            {
                String userName = (String) properties.getPerson().getProperties().get(ContentModel.PROP_USERNAME);
                properties.getAuthenticationService().updateAuthentication(userName, properties.getOldPassword().toCharArray(), properties.getPassword().toCharArray());
            }
            catch (Exception e)
            {
                outcome = null;
                Utils.addErrorMessage(Application.getMessage(FacesContext.getCurrentInstance(), 
                         MSG_ERROR_INCORRECT_OLD_PASSWORD));
                ReportedException.throwIfNecessary(e);
            }
        }
        else
        {
            outcome = null;
            Utils.addErrorMessage(Application.getMessage(newContext, ERROR_PASSWORD_MATCH));
        }

        return outcome;
    }

}
