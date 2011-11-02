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

import org.alfresco.model.ContentModel;
import org.alfresco.web.app.AlfrescoNavigationHandler;
import org.alfresco.web.app.Application;
import org.alfresco.web.bean.repository.Node;
import org.alfresco.web.ui.common.ReportedException;
import org.alfresco.web.ui.common.Utils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class CancelEditingDialog extends CheckinCheckoutDialog
{
    public static final String MSG_CANCEL_EDITING = "cancel_editing";
    public static final String MSG_CANCEL_EDITING_FOR = "cancel_editing_for";
    private final static String MSG_LEFT_QUOTE = "left_qoute";
    private final static String MSG_RIGHT_QUOTE = "right_quote";

    private static Log logger = LogFactory.getLog(CheckinCheckoutDialog.class);

    @Override
    protected String finishImpl(FacesContext context, String outcome) throws Exception
    {
        undoCheckoutFile(context, outcome);
        return outcome;
    }
    
    @Override 
    protected String getDefaultCancelOutcome() 
    { 
       return AlfrescoNavigationHandler.CLOSE_DIALOG_OUTCOME + 
              AlfrescoNavigationHandler.OUTCOME_SEPARATOR + 
              AlfrescoNavigationHandler.OUTCOME_BROWSE; 
    } 

    @Override 
    protected String getDefaultFinishOutcome() 
    { 
        return AlfrescoNavigationHandler.CLOSE_DIALOG_OUTCOME + 
               AlfrescoNavigationHandler.OUTCOME_SEPARATOR + 
               AlfrescoNavigationHandler.OUTCOME_BROWSE; 
    } 

    @Override
    public String getContainerTitle()
    {
        FacesContext fc = FacesContext.getCurrentInstance();
        return Application.getMessage(fc, MSG_CANCEL_EDITING_FOR) + " " + Application.getMessage(fc, MSG_LEFT_QUOTE) 
            + property.getDocument().getName() + Application.getMessage(fc, MSG_RIGHT_QUOTE);
    }

    @Override
    public boolean getFinishButtonDisabled()
    {
        return false;
    }

    @Override
    public String getFinishButtonLabel()
    {
        return Application.getMessage(FacesContext.getCurrentInstance(), MSG_CANCEL_EDITING);
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
                ReportedException.throwIfNecessary(err);
            }
        }
        else
        {
            logger.warn("WARNING: undoCheckout called without a current WorkingDocument!");
        }

        return outcome;
    }

}
