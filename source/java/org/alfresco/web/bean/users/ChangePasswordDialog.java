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
import org.alfresco.web.bean.repository.Repository;
import org.alfresco.web.ui.common.ReportedException;
import org.alfresco.web.ui.common.Utils;

public class ChangePasswordDialog extends UsersDialog
{

    private static final long serialVersionUID = -1570967895811499123L;

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
