/*
 * Copyright (C) 2005-2007 Alfresco Software Limited.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.

 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.

 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.

 * As a special exception to the terms and conditions of version 2.0 of 
 * the GPL, you may redistribute this Program in connection with Free/Libre 
 * and Open Source Software ("FLOSS") applications as described in Alfresco's 
 * FLOSS exception.  You should have recieved a copy of the text describing 
 * the FLOSS exception, and it is also available here: 
 * http://www.alfresco.com/legal/licensing"
 */
package org.alfresco.web.bean.users;

import java.text.MessageFormat;

import javax.faces.context.FacesContext;

import org.alfresco.model.ContentModel;
import org.alfresco.web.app.Application;
import org.alfresco.web.bean.repository.Repository;
import org.alfresco.web.ui.common.Utils;

public class ChangePasswordDialog extends UsersDialog
{

    private static final String MSG_FINISH_BUTTON = "finish_button";

    @Override
    protected String finishImpl(FacesContext context, String outcome) throws Exception
    {
        String result = changePasswordOK(outcome, context);
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
     * Action handler called for OK button press on Change Password screen
     */
    public String changePasswordOK(String newOutcome, FacesContext newContext)
    {
        String outcome = newOutcome;

        if (properties.getPassword() != null && properties.getConfirm() != null && properties.getPassword().equals(properties.getConfirm()))
        {
            try
            {
                String userName = (String) properties.getPerson().getProperties().get(ContentModel.PROP_USERNAME);
                properties.getAuthenticationService().setAuthentication(userName, properties.getPassword().toCharArray());
            }
            catch (Exception e)
            {
                outcome = null;
                Utils.addErrorMessage(MessageFormat.format(Application.getMessage(newContext, Repository.ERROR_GENERIC), e.getMessage()), e);
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
