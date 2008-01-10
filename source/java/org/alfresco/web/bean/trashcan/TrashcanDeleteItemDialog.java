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
package org.alfresco.web.bean.trashcan;

import java.text.MessageFormat;

import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;

import org.alfresco.web.app.Application;
import org.alfresco.web.bean.repository.Node;
import org.alfresco.web.bean.repository.Repository;
import org.alfresco.web.ui.common.Utils;

public class TrashcanDeleteItemDialog extends TrashcanDialog
{
    private static final String RICHLIST_ID = "trashcan-list";
    private static final String RICHLIST_MSG_ID = "trashcan" + ':' + RICHLIST_ID;
    private static final String MSG_YES = "yes";
    private static final String MSG_NO = "no";
    private static final String MSG_DELETE_ITEM = "delete_item";

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
            }
        }
        return newOutcome;
    }

    @Override
    protected String finishImpl(FacesContext context, String outcome) throws Exception
    {
        deleteItem(context, outcome);
        return "dialog:close";

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

        return Application.getMessage(FacesContext.getCurrentInstance(), MSG_DELETE_ITEM) + " '" + property.getItem().getName() + "'";
    }

}
