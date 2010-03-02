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
package org.alfresco.web.bean.coci;

import javax.faces.context.FacesContext;

import org.alfresco.web.app.Application;

/**
 * Dialog supporting the Upload New Version action on a working copy node.
 */
public class UploadNewVersionDialog extends DoneEditingDialog
{
    private final static String MSG_UPLOAD_NEW_VERSION = "upload_new_version";
    private final static String MSG_LEFT_QUOTE = "left_qoute";
    private final static String MSG_RIGHT_QUOTE = "right_quote";
    private final static String MSG_OF = "of";

    private boolean finishedEditing = false;

    public void setFinishedEditing(boolean finished)
    {
        this.finishedEditing = finished;
    }

    public boolean isFinishedEditing()
    {
        return finishedEditing;
    }

    @Override
    public boolean getFinishButtonDisabled()
    {
        return property.getFile() == null;
    }

    @Override
    public String getContainerTitle()
    {
        FacesContext fc = FacesContext.getCurrentInstance();
        return Application.getMessage(fc, MSG_UPLOAD_NEW_VERSION) + " " + Application.getMessage(fc, MSG_OF) + " " +
                Application.getMessage(fc, MSG_LEFT_QUOTE)
                + property.getDocument().getName() + Application.getMessage(fc, MSG_RIGHT_QUOTE);
    }

    @Override
    protected String finishImpl(FacesContext context, String outcome) throws Exception
    {
        if (finishedEditing)
        {
            property.setKeepCheckedOut(false);
            return checkinFileOK(context, outcome);
        }
        else
        {
            return updateFileOK(context, outcome);
        }
    }
    
    @Override
    public void resetState()
    {
        super.resetState();
        finishedEditing = false;
    }
}
