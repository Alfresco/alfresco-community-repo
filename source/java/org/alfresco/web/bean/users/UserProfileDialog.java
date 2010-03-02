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