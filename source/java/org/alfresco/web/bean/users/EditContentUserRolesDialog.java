package org.alfresco.web.bean.users;

import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;
import javax.faces.model.DataModel;

import org.alfresco.web.bean.dialog.BaseDialogBean;

public class EditContentUserRolesDialog extends BaseDialogBean
{
    private static final long serialVersionUID = -1690749440382024258L;
  
    ContentUsersBean contentUsersBean;
    @Override
    public boolean getFinishButtonDisabled()
    {
        return false;
    }
    
    public ContentUsersBean getContentUsersBean()
    {
        return contentUsersBean;
    }

    public void setContentUsersBean(ContentUsersBean contentUsersBean)
    {
        this.contentUsersBean = contentUsersBean;
    }

    @Override
    protected String finishImpl(FacesContext context, String outcome) throws Exception
    {
        contentUsersBean.finishOK();
        return outcome;
    }
    
    public void addRole(ActionEvent event)
    {
        contentUsersBean.addRole(event);
    }
    
    public void setupUserAction(ActionEvent event)
    {
        contentUsersBean.setupUserAction(event);
    }
    
    public void removeRole(ActionEvent event)
    {
        contentUsersBean.removeRole(event);
    }
    
    public DataModel getPersonRolesDataModel()
    {
        return contentUsersBean.getPersonRolesDataModel();
    }

}
