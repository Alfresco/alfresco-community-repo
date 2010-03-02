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

import org.alfresco.web.app.Application;
import org.alfresco.web.bean.dialog.BaseDialogBean;

public class RemoveInvitedUserDialog extends BaseDialogBean
{

    private static final long serialVersionUID = -7457234588814115434L;

    private static final String BUTTON_NO = "no";

    private static final String BUTTON_YES = "yes";

    private static final String MSG_REMOVE_USER = "remove_user";
    private final static String MSG_LEFT_QUOTE = "left_qoute";
    private final static String MSG_RIGHT_QUOTE = "right_quote";

    private SpaceUsersBean spaceUsersBean;

    @Override
    public boolean getFinishButtonDisabled()
    {
        return false;
    }

    @Override
    protected String finishImpl(FacesContext context, String outcome) throws Exception
    {
        return spaceUsersBean.removeOK();
    }

    public void setupUserAction(ActionEvent event)
    {
        spaceUsersBean.setupUserAction(event);
    }

    public String getPersonName()
    {
        return spaceUsersBean.getPersonName();
    }

    public void setPersonName(String personName)
    {
        this.spaceUsersBean.setPersonName(personName);
    }

    public SpaceUsersBean getSpaceUsersBean()
    {
        return spaceUsersBean;
    }

    public void setSpaceUsersBean(SpaceUsersBean spaceUsersBean)
    {
        this.spaceUsersBean = spaceUsersBean;
    }

    @Override
    public String getCancelButtonLabel()
    {
        return Application.getMessage(FacesContext.getCurrentInstance(), BUTTON_NO);
    }

    @Override
    public String getFinishButtonLabel()
    {
        return Application.getMessage(FacesContext.getCurrentInstance(), BUTTON_YES);
    }

    @Override
    public String getContainerTitle() 
    {
        FacesContext fc = FacesContext.getCurrentInstance();
        return Application.getMessage(fc, MSG_REMOVE_USER) + " " + Application.getMessage(fc, MSG_LEFT_QUOTE) + spaceUsersBean.getPersonName()
                + Application.getMessage(fc, MSG_RIGHT_QUOTE);
    }
}
