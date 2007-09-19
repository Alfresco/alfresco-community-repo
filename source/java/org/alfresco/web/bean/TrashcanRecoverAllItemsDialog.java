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
package org.alfresco.web.bean;

import java.util.List;

import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;

import org.alfresco.repo.node.archive.RestoreNodeReport;
import org.alfresco.web.app.Application;
import org.alfresco.web.bean.repository.Repository;

public class TrashcanRecoverAllItemsDialog extends TrashcanDialog
{
    private static final String OUTCOME_RECOVERY_REPORT = "dialog:recoveryReport";
    private static final String MSG_NO = "no";
    private static final String MSG_YES = "yes";

    private String recoverAllItems(FacesContext context, String outcome)
    {
        if (property.isInProgress())
            return null;

        property.setInProgress(true);

        try
        {

            // restore all nodes - the user may have requested a restore to a
            // different parent
            List<RestoreNodeReport> reports;
            if (property.getDestination() == null)
            {
                reports = property.getNodeArchiveService().restoreAllArchivedNodes(Repository.getStoreRef());
            }
            else
            {
                reports = property.getNodeArchiveService().restoreAllArchivedNodes(Repository.getStoreRef(), property.getDestination(), null, null);
            }

            saveReportDetail(reports);

        }
        finally
        {
            property.setInProgress(false);
        }

        return OUTCOME_RECOVERY_REPORT;
    }

    @Override
    protected String finishImpl(FacesContext context, String outcome) throws Exception
    {

        return recoverAllItems(context, outcome);

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
    public void setupListAction(ActionEvent event)
    {

        super.setupListAction(event);
        clearSearch(event);
        getItems();
    }
}
