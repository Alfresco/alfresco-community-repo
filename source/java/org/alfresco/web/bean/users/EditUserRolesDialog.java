/*
 * Copyright (C) 2005-2010 Alfresco Software Limited.
 *
 * This file is part of Alfresco
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
 */
package org.alfresco.web.bean.users;

import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;
import javax.faces.model.DataModel;

import org.alfresco.web.app.Application;
import org.alfresco.web.bean.dialog.BaseDialogBean;

public class EditUserRolesDialog extends BaseDialogBean {
    
    private static final long serialVersionUID = 614541023231055676L;

    private static final String MSG_MODIFY_USER_ROLE = "modify_user_roles";
    private final static String MSG_LEFT_QUOTE = "left_qoute";
    private final static String MSG_RIGHT_QUOTE = "right_quote";

    SpaceUsersBean spaceUsersBean;

    @Override
    public boolean getFinishButtonDisabled() {
        return false;
    }

    public SpaceUsersBean getSpaceUsersBean() {
        return spaceUsersBean;
    }

    public void setSpaceUsersBean(SpaceUsersBean spaceUsersBean) {
        this.spaceUsersBean = spaceUsersBean;
    }

    @Override
    protected String finishImpl(FacesContext context, String outcome) throws Exception {
        spaceUsersBean.finishOK();
        return outcome;
    }

    public void addRole(ActionEvent event) {
        spaceUsersBean.addRole(event);
    }

    public void setupUserAction(ActionEvent event) {
        spaceUsersBean.setupUserAction(event);
    }

    public void removeRole(ActionEvent event) {
        spaceUsersBean.removeRole(event);
    }

    public DataModel getPersonRolesDataModel() {
        return spaceUsersBean.getPersonRolesDataModel();
    }

    @Override
    public String getContainerTitle() 
    {
        FacesContext fc = FacesContext.getCurrentInstance();
        return Application.getMessage(fc, MSG_MODIFY_USER_ROLE) + " " + Application.getMessage(fc, MSG_LEFT_QUOTE)
                + spaceUsersBean.getPersonName() + Application.getMessage(fc, MSG_RIGHT_QUOTE);
    }
}
