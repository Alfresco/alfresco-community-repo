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

import org.alfresco.repo.node.archive.RestoreNodeReport;
import org.alfresco.web.app.Application;
import org.alfresco.web.bean.repository.Node;
import org.alfresco.web.ui.common.ReportedException;

public class TrashcanRecoverItemDialog extends TrashcanDialog
{
    private static final long serialVersionUID = -8237457079397611071L;
    
    private static final String RICHLIST_ID = "trashcan-list";
    private static final String RICHLIST_MSG_ID = "trashcan" + ':' + RICHLIST_ID;
    private static final String MSG_RECOVERED_ITEM_SUCCESS = "recovered_item_success";
    private static final String MSG_RECOVERED_ITEM_INTEGRITY = "recovered_item_integrity";
    private static final String MSG_RECOVERED_ITEM_PERMISSION = "recovered_item_permission";
    private static final String MSG_RECOVERED_ITEM_PARENT = "recovered_item_parent";
    private static final String MSG_RECOVERED_ITEM_FAILURE = "recovered_item_failure";
    private final static String MSG_LEFT_QUOTE = "left_qoute";
    private final static String MSG_RIGHT_QUOTE = "right_quote";

    private static final String MSG_RECOVER_ITEM = "recover_item";
    private static final String MSG_NO = "no";
    private static final String MSG_YES = "yes";

    private String recoverItem(FacesContext context, String outcome)
    {
        Node item = property.getItem();
        if (item != null)
        {
            FacesContext fc = context;
            try
            {
                String msg;
                FacesMessage errorfacesMsg = null;

                // restore the node - the user may have requested a restore to a
                // different parent
                RestoreNodeReport report;
                if (property.getDestination() == null)
                {
                    report = property.getNodeArchiveService().restoreArchivedNode(item.getNodeRef());
                }
                else
                {
                    report = property.getNodeArchiveService().restoreArchivedNode(item.getNodeRef(), property.getDestination(), null, null);
                }
                switch (report.getStatus())
                {
                case SUCCESS:
                    msg = MessageFormat.format(Application.getMessage(fc, MSG_RECOVERED_ITEM_SUCCESS), item.getName());
                    FacesMessage facesMsg = new FacesMessage(FacesMessage.SEVERITY_INFO, msg, msg);
                    fc.addMessage(RICHLIST_MSG_ID, facesMsg);

                    break;

                case FAILURE_INVALID_PARENT:
                    msg = MessageFormat.format(Application.getMessage(fc, MSG_RECOVERED_ITEM_PARENT), item.getName());
                    errorfacesMsg = new FacesMessage(FacesMessage.SEVERITY_ERROR, msg, msg);
                    break;

                case FAILURE_PERMISSION:
                    msg = MessageFormat.format(Application.getMessage(fc, MSG_RECOVERED_ITEM_PERMISSION), item.getName());
                    errorfacesMsg = new FacesMessage(FacesMessage.SEVERITY_ERROR, msg, msg);
                    break;

                case FAILURE_INTEGRITY:
                    msg = MessageFormat.format(Application.getMessage(fc, MSG_RECOVERED_ITEM_INTEGRITY), item.getName());
                    errorfacesMsg = new FacesMessage(FacesMessage.SEVERITY_ERROR, msg, msg);
                    break;

                default:
                    String reason = report.getCause().getMessage();
                    msg = MessageFormat.format(Application.getMessage(fc, MSG_RECOVERED_ITEM_FAILURE), item.getName(), reason);
                    errorfacesMsg = new FacesMessage(FacesMessage.SEVERITY_ERROR, msg, msg);
                    break;
                }

                // report the failure if one occured we stay on the current
                // screen
                if (errorfacesMsg != null)
                {
                    fc.addMessage(null, errorfacesMsg);
                }
            }
            catch (Throwable err)
            {
                // most exceptions will be caught and returned as
                // RestoreNodeReport objects by the service
                String reason = err.getMessage();
                String msg = MessageFormat.format(Application.getMessage(fc, MSG_RECOVERED_ITEM_FAILURE), item.getName(), reason);
                FacesMessage facesMsg = new FacesMessage(FacesMessage.SEVERITY_ERROR, msg, msg);
                fc.addMessage(null, facesMsg);
                ReportedException.throwIfNecessary(err);
            }
        }

        return outcome;
    }
    
    @Override    
    protected String getDefaultFinishOutcome()
    {
       return "dialog:close[2]";
    }

    @Override
    protected String finishImpl(FacesContext context, String outcome) throws Exception
    {
        return recoverItem(context, outcome);
    }

    @Override
    public String getContainerTitle()
    {
        FacesContext fc = FacesContext.getCurrentInstance();
        return Application.getMessage(fc, MSG_RECOVER_ITEM) + " " + Application.getMessage(fc, MSG_LEFT_QUOTE) 
                + property.getItem().getName() + Application.getMessage(fc, MSG_RIGHT_QUOTE);
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

}
