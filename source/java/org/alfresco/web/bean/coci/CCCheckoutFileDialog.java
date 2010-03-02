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
import org.alfresco.repo.web.scripts.FileTypeImageUtils;
import org.alfresco.repo.workflow.WorkflowModel;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.workflow.WorkflowTask;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.web.app.Application;
import org.alfresco.web.app.servlet.DownloadContentServlet;
import org.alfresco.web.bean.repository.Node;
import org.alfresco.web.ui.common.ReportedException;
import org.alfresco.web.ui.common.Utils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class CCCheckoutFileDialog extends CheckinCheckoutDialog
{
    private static final long serialVersionUID = 1137163500648349730L;
    
    public static final String LBL_SAVE = "save";
    public static final String LBL_CHECKOUT = "check_out";
    private final static String MSG_LEFT_QUOTE = "left_qoute";
    private final static String MSG_RIGHT_QUOTE = "right_quote";
    
    private static Log logger = LogFactory.getLog(CCCheckoutFileDialog.class);
    
    @Override
    protected String finishImpl(FacesContext context, String outcome) throws Exception
    {
        return checkoutFile(context, outcome);

    }

    @Override
    public boolean getFinishButtonDisabled()
    {
        return false;
    }

    @Override
    public String getFinishButtonLabel()
    {

        return Application.getMessage(FacesContext.getCurrentInstance(), LBL_CHECKOUT);
    }

    @Override
    public String getContainerTitle()
    {
        final Node document = property.getDocument();
        if (document != null){
            FacesContext fc = FacesContext.getCurrentInstance();
            return Application.getMessage(fc, LBL_CHECKOUT) + " " + Application.getMessage(fc, MSG_LEFT_QUOTE)
                + document.getName() + Application.getMessage(fc, MSG_RIGHT_QUOTE);
        }
        return null;
    }
    
    /**
     * Action called upon completion of the Check Out file page
     */
    public String checkoutFile(FacesContext context, String outcome)
    {
        boolean checkoutSuccessful = false;
        final Node node = property.getDocument();
        if (node != null)
        {
            try
            {
                if (logger.isDebugEnabled())
                    logger.debug("Trying to checkout content node Id: " + node.getId());

                // checkout the node content to create a working copy
                if (logger.isDebugEnabled())
                {
                    logger.debug("Checkout copy location: " + property.getCopyLocation());
                    logger.debug("Selected Space Id: " + property.getSelectedSpaceId());
                }
                NodeRef workingCopyRef = null;
                if (property.getCopyLocation().equals(CCProperties.COPYLOCATION_OTHER) && property.getSelectedSpaceId() != null)
                {
                    // checkout to a arbituary parent Space
                    NodeRef destRef = property.getSelectedSpaceId();

                    ChildAssociationRef childAssocRef = getNodeService().getPrimaryParent(destRef);
                    workingCopyRef = property.getVersionOperationsService().checkout(node.getNodeRef(), destRef, ContentModel.ASSOC_CONTAINS, childAssocRef.getQName());
                }
                else
                {
                    // checkout the content to the current space
                    workingCopyRef = property.getVersionOperationsService().checkout(node.getNodeRef());

                    // if this is a workflow action and there is a task id
                    // present we need
                    // to also link the working copy to the workflow package so
                    // it appears
                    // in the resources panel in the manage task dialog
                    if (property.isWorkflowAction() && property.getWorkflowTaskId() != null && (property.getWorkflowTaskId().equals("null") == false))
                    {
                        WorkflowTask task = property.getWorkflowService().getTaskById(property.getWorkflowTaskId());
                        if (task != null)
                        {
                            NodeRef workflowPackage = (NodeRef) task.properties.get(WorkflowModel.ASSOC_PACKAGE);
                            if (workflowPackage != null)
                            {
                                getNodeService().addChild(workflowPackage, workingCopyRef, ContentModel.ASSOC_CONTAINS, QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, QName
                                        .createValidLocalName((String) getNodeService().getProperty(workingCopyRef, ContentModel.PROP_NAME))));

                                if (logger.isDebugEnabled())
                                    logger.debug("Added working copy to workflow package: " + workflowPackage);
                            }
                        }
                    }
                }

                // set the working copy Node instance
                Node workingCopy = new Node(workingCopyRef);
                property.setWorkingDocument(workingCopy);

                // create content URL to the content download servlet with ID
                // and expected filename
                // the myfile part will be ignored by the servlet but gives the
                // browser a hint
                String url = DownloadContentServlet.generateDownloadURL(workingCopyRef, workingCopy.getName());

                workingCopy.getProperties().put("url", url);
                workingCopy.getProperties().put("fileType32", FileTypeImageUtils.getFileTypeImage(workingCopy.getName(), false));

                // mark as successful
                checkoutSuccessful = true;
            }
            catch (Throwable err)
            {
                Utils.addErrorMessage(Application.getMessage(context, MSG_ERROR_CHECKOUT) + err.getMessage(), err);
                ReportedException.throwIfNecessary(err);
            }
        }
        else
        {
            logger.warn("WARNING: checkoutFile called without a current Document!");
        }

        // determine which page to show next if the checkout was successful.
        if (checkoutSuccessful)
        {
            // If a check-in rule is present in the space
            // the document was checked out to the working copy would have
            // already disappeared!
            if (getNodeService().exists(property.getWorkingDocument().getNodeRef()))
            {
                // go to the page that allows the user to download the content
                // for editing
                outcome = "dialog:checkoutFileLink"; // "checkoutFileLink";
                // //checkout-file-link.jsp
                // currentAction = Action.CHECKOUT_FILE_LINK;
            }
            else
            {
                // show a page telling the user that the content has already
                // been checked in
                outcome = "dialog:workingCopyMissing";// "workingCopyMissing";
                // //
                // working-copy-missing.jsp
                // currentAction = Action.WORKING_COPY_MISSING;
            }
        }
        return outcome;
    }

}
