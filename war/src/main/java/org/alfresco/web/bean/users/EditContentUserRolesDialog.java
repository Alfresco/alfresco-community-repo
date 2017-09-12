/*
 * #%L
 * Alfresco Repository WAR Community
 * %%
 * Copyright (C) 2005 - 2016 Alfresco Software Limited
 * %%
 * This file is part of the Alfresco software. 
 * If the software was purchased under a paid Alfresco license, the terms of 
 * the paid license agreement will prevail.  Otherwise, the software is 
 * provided under the following open source license terms:
 * 
 * Alfresco is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * Alfresco is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with Alfresco. If not, see <http://www.gnu.org/licenses/>.
 * #L%
 */
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
