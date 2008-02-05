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
package org.alfresco.web.bean.coci;

import javax.faces.context.FacesContext;

import org.alfresco.model.ContentModel;
import org.alfresco.web.app.Application;
import org.alfresco.web.bean.repository.Node;
import org.alfresco.web.ui.common.Utils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class CancelEditingDialog extends CheckinCheckoutDialog
{
    public static final String LBL_CANCEL_EDITING = "cancel_editing";
    public static final String MSG_CANCEL_EDITING_FOR = "cancel_editing_for";

    private static Log logger = LogFactory.getLog(CheckinCheckoutDialog.class);

    @Override
    protected String finishImpl(FacesContext context, String outcome) throws Exception
    {
        return undoCheckoutFile(context, outcome);
    }

    @Override
    public String getContainerTitle()
    {

        return Application.getMessage(FacesContext.getCurrentInstance(), MSG_CANCEL_EDITING_FOR) + " '" + property.getDocument().getName() + "'";
    }

    @Override
    public boolean getFinishButtonDisabled()
    {
        return false;
    }

    @Override
    public String getFinishButtonLabel()
    {
        return Application.getMessage(FacesContext.getCurrentInstance(), LBL_CANCEL_EDITING);

    }

    /**
     * Action to undo the checkout of a locked document. This document may
     * either by the original copy or the working copy node. Therefore calculate
     * which it is, if the working copy is found then we simply cancel checkout
     * on that document. If the original copy is found then we need to find the
     * appropriate working copy and perform the action on that node.
     */
    public String undoCheckoutFile(FacesContext context, String outcome)
    {
        Node node = property.getDocument();
        if (node != null)
        {
            try
            {
                if (node.hasAspect(ContentModel.ASPECT_WORKING_COPY))
                {
                    this.property.getVersionOperationsService().cancelCheckout(node.getNodeRef());
                }
                else if (node.hasAspect(ContentModel.ASPECT_LOCKABLE))
                {
                    // TODO: find the working copy for this document and cancel
                    // the checkout on it
                    // is this possible? as currently only the workingcopy
                    // aspect has the copyReference
                    // attribute - this means we cannot find out where the copy
                    // is to cancel it!
                    // can we construct an XPath node lookup?
                    throw new RuntimeException("NOT IMPLEMENTED");
                }
                else
                {
                    throw new IllegalStateException("Node supplied for undo checkout has neither Working Copy or Locked aspect!");
                }

                resetState();
            }
            catch (Throwable err)
            {
                Utils.addErrorMessage(Application.getMessage(FacesContext.getCurrentInstance(), MSG_ERROR_CANCELCHECKOUT) + err.getMessage(), err);
            }
        }
        else
        {
            logger.warn("WARNING: undoCheckout called without a current WorkingDocument!");
        }

        return outcome;
    }

}
