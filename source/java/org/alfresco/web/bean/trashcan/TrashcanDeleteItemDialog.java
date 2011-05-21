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
package org.alfresco.web.bean.trashcan;

import java.text.MessageFormat;

import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;

import org.alfresco.web.app.Application;
import org.alfresco.web.bean.repository.Node;
import org.alfresco.web.bean.repository.Repository;
import org.alfresco.web.ui.common.ReportedException;
import org.alfresco.web.ui.common.Utils;

public class TrashcanDeleteItemDialog extends TrashcanDialog
{
    private static final long serialVersionUID = 519967126630923155L;
    
    private static final String RICHLIST_ID = "trashcan-list";
    private static final String RICHLIST_MSG_ID = "trashcan" + ':' + RICHLIST_ID;
    private static final String MSG_YES = "yes";
    private static final String MSG_NO = "no";
    private static final String MSG_DELETE_ITEM = "delete_item";
    private final static String MSG_LEFT_QUOTE = "left_qoute";
    private final static String MSG_RIGHT_QUOTE = "right_quote";

    private String deleteItem(FacesContext newContext, String newOutcome)
    {
        Node item = property.getItem();
        if (item != null)
        {
            try
            {
                property.getNodeArchiveService().purgeArchivedNode(item.getNodeRef());

                FacesContext fc = newContext;
                String msg = MessageFormat.format(Application.getMessage(fc, "delete_item_success"), item.getName());
                FacesMessage facesMsg = new FacesMessage(FacesMessage.SEVERITY_INFO, msg, msg);
                fc.addMessage(RICHLIST_MSG_ID, facesMsg);
            }
            catch (Throwable err)
            {
                Utils.addErrorMessage(MessageFormat.format(Application.getMessage(newContext, Repository.ERROR_GENERIC), err.getMessage()), err);
                ReportedException.throwIfNecessary(err);
            }
        }
        return newOutcome;
    }

    @Override
    protected String getDefaultFinishOutcome()
    {
       return "dialog:close[2]";
    }

    @Override
    protected String finishImpl(FacesContext context, String outcome) throws Exception
    {
        return deleteItem(context, outcome);
    }

    @Override
    public String getCancelButtonLabel()
    {
        return Application.getMessage(FacesContext.getCurrentInstance(), MSG_NO);
    }

    @Override
    public boolean getFinishButtonDisabled()
    {

        return false;
    }

    @Override
    public String getFinishButtonLabel()
    {
        return Application.getMessage(FacesContext.getCurrentInstance(), MSG_YES);
    }

    @Override
    public String getContainerTitle()
    {
        FacesContext fc = FacesContext.getCurrentInstance();
        return Application.getMessage(fc, MSG_DELETE_ITEM) + " " + Application.getMessage(fc, MSG_LEFT_QUOTE)
                + property.getItem().getName() + Application.getMessage(fc, MSG_RIGHT_QUOTE);
    }

}
