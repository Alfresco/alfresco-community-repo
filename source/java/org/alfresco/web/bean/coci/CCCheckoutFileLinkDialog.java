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

import org.alfresco.web.app.AlfrescoNavigationHandler;
import org.alfresco.web.app.Application;
import org.alfresco.web.bean.repository.Node;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class CCCheckoutFileLinkDialog extends CheckinCheckoutDialog
{
    public static final String MSG_CHECKOUT_OF = "check_out_of";
    public static final String LBL_UNDO_CHECKOUT = "undo_checkout";

    private static Log logger = LogFactory.getLog(CCCheckoutFileLinkDialog.class);

    @Override
    protected String finishImpl(FacesContext context, String outcome) throws Exception
    {
        return checkoutFileOK(context, outcome);
    }

    @Override
    public boolean getFinishButtonDisabled()
    {
        return false;
    }

    @Override
    public String getContainerTitle()
    {
        return Application.getMessage(FacesContext.getCurrentInstance(), MSG_CHECKOUT_OF) + " '" + property.getDocument().getName() + "'";
    }

    @Override
    public String getCancelButtonLabel()
    {
        return Application.getMessage(FacesContext.getCurrentInstance(), LBL_UNDO_CHECKOUT);
    }
    
    public String getFinishButtonLabel()
    {
       return Application.getMessage(FacesContext.getCurrentInstance(), "ok");
    }    

    @Override
    public String cancel()
    {
        undoCheckout();
        resetState();
        super.cancel();
        return "browse";

    }

    /**
     * Action called upon completion of the Check Out file Link download page
     */
    public String checkoutFileOK(FacesContext context, String outcome)
    {
        Node node = property.getWorkingDocument();
        if (node != null)
        {
            // reset the underlying node
            if (this.browseBean.getDocument() != null)
            {
                this.browseBean.getDocument().reset();
            }

            // clean up and clear action context
            resetState();
            property.setDocument(null);
            property.setWorkingDocument(null);
            // currentAction = Action.NONE;
            outcome = AlfrescoNavigationHandler.CLOSE_DIALOG_OUTCOME + ":browse";
        }
        else
        {
            logger.warn("WARNING: checkoutFileOK called without a current WorkingDocument!");
        }
        return outcome;
    }
}
