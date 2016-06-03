package org.alfresco.web.bean.users;

import java.text.MessageFormat;

import javax.faces.context.FacesContext;

import org.alfresco.model.ContentModel;
import org.alfresco.web.app.Application;
import org.alfresco.web.bean.dialog.BaseDialogBean;

public class UserProfileDialog extends BaseDialogBean
{
    private static final String MSG_CLOSE = "close";
    private static final String MSG_USER_PROFILE = "user_profile_for";
    
    private UsersBeanProperties properties;
    

    /**
     * @param properties the properties to set
     */
    public void setProperties(UsersBeanProperties properties)
    {
        this.properties = properties;
    }

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

    @Override
    public String getContainerDescription()
    {
        // display description of user profile (full name etc.)
        return MessageFormat.format(
                Application.getMessage(FacesContext.getCurrentInstance(), MSG_USER_PROFILE),
                this.properties.getPerson().getProperties().get(ContentModel.PROP_USERNAME));
    }
}