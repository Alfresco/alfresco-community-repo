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
package org.alfresco.web.bean.spaces;

import java.io.Serializable;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Map;

import javax.faces.context.FacesContext;

import org.alfresco.model.ApplicationModel;
import org.alfresco.repo.transaction.RetryingTransactionHelper;
import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;
import org.alfresco.web.app.Application;
import org.alfresco.web.bean.actions.handlers.SimpleWorkflowHandler;
import org.alfresco.web.bean.dialog.BaseDialogBean;
import org.alfresco.web.bean.repository.Node;
import org.alfresco.web.bean.repository.Repository;
import org.alfresco.web.ui.common.ReportedException;
import org.alfresco.web.ui.common.Utils;

public class EditSimpleWorkflowDialog extends BaseDialogBean
{
    private static final long serialVersionUID = 5997327694341960824L;

    protected Map<String, Serializable> workflowProperties;

    private static final String MSG_ERROR_UPDATE_SIMPLEWORKFLOW = "error_update_simpleworkflow";

    @Override
    protected String finishImpl(FacesContext context, String outcome) throws Exception
    {
        saveWorkflow();
        return outcome;
    }

    public boolean getFinishButtonDisabled()
    {
        return false;
    }

    public String saveWorkflow()
    {
        String outcome = "cancel";

        try
        {
            RetryingTransactionHelper txnHelper = Repository.getRetryingTransactionHelper(FacesContext.getCurrentInstance());
            RetryingTransactionCallback<Object> callback = new RetryingTransactionCallback<Object>()
            {
                public Object execute() throws Throwable
                {
                    // firstly retrieve all the properties for the current node
                    Map<QName, Serializable> updateProps = getNodeService().getProperties(getNode().getNodeRef());

                    // update the simple workflow properties

                    // set the approve step name
                    updateProps.put(ApplicationModel.PROP_APPROVE_STEP, workflowProperties.get(SimpleWorkflowHandler.PROP_APPROVE_STEP_NAME));

                    // specify whether the approve step will copy or move the content
                    boolean approveMove = true;
                    String approveAction = (String) workflowProperties.get(SimpleWorkflowHandler.PROP_APPROVE_ACTION);
                    if (approveAction != null && approveAction.equals("copy"))
                    {
                        approveMove = false;
                    }
                    updateProps.put(ApplicationModel.PROP_APPROVE_MOVE, Boolean.valueOf(approveMove));

                    // create node ref representation of the destination folder
                    updateProps.put(ApplicationModel.PROP_APPROVE_FOLDER, workflowProperties.get(SimpleWorkflowHandler.PROP_APPROVE_FOLDER));

                    // determine whether there should be a reject step
                    boolean requireReject = true;
                    String rejectStepPresent = (String) workflowProperties.get(SimpleWorkflowHandler.PROP_REJECT_STEP_PRESENT);
                    if (rejectStepPresent != null && rejectStepPresent.equals("no"))
                    {
                        requireReject = false;
                    }

                    if (requireReject)
                    {
                        // set the reject step name
                        updateProps.put(ApplicationModel.PROP_REJECT_STEP, workflowProperties.get(SimpleWorkflowHandler.PROP_REJECT_STEP_NAME));

                        // specify whether the reject step will copy or move the content
                        boolean rejectMove = true;
                        String rejectAction = (String) workflowProperties.get(SimpleWorkflowHandler.PROP_REJECT_ACTION);
                        if (rejectAction != null && rejectAction.equals("copy"))
                        {
                            rejectMove = false;
                        }
                        updateProps.put(ApplicationModel.PROP_REJECT_MOVE, Boolean.valueOf(rejectMove));

                        // create node ref representation of the destination folder
                        updateProps.put(ApplicationModel.PROP_REJECT_FOLDER, workflowProperties.get(SimpleWorkflowHandler.PROP_REJECT_FOLDER));
                    }
                    else
                    {
                        // set all the reject properties to null to signify there should
                        // be no reject step
                        updateProps.put(ApplicationModel.PROP_REJECT_STEP, null);
                        updateProps.put(ApplicationModel.PROP_REJECT_MOVE, null);
                        updateProps.put(ApplicationModel.PROP_REJECT_FOLDER, null);
                    }

                    // set the properties on the node
                    getNodeService().setProperties(getNode().getNodeRef(), updateProps);
                    return null;
                }
            };
            txnHelper.doInTransaction(callback);

            // reset the state of the current node so it reflects the changes just made
            getNode().reset();

            outcome = "finish";
        }
        catch (Throwable e)
        {
            Utils.addErrorMessage(MessageFormat.format(Application.getMessage(FacesContext.getCurrentInstance(), MSG_ERROR_UPDATE_SIMPLEWORKFLOW), e.getMessage()), e);
            ReportedException.throwIfNecessary(e);
        }

        return outcome;
    }

    /**
     * Returns the properties for the attached workflow as a map
     * 
     * @return Properties of the attached workflow, null if there is no workflow
     */
    public Map<String, Serializable> getWorkflowProperties()
    {
        if (this.workflowProperties == null && getNode().hasAspect(ApplicationModel.ASPECT_SIMPLE_WORKFLOW))
        {
            // get the exisiting properties for the node
            Map<String, Object> props = getNode().getProperties();

            String approveStepName = (String) props.get(ApplicationModel.PROP_APPROVE_STEP.toString());
            String rejectStepName = (String) props.get(ApplicationModel.PROP_REJECT_STEP.toString());

            Boolean approveMove = (Boolean) props.get(ApplicationModel.PROP_APPROVE_MOVE.toString());
            Boolean rejectMove = (Boolean) props.get(ApplicationModel.PROP_REJECT_MOVE.toString());

            NodeRef approveFolder = (NodeRef) props.get(ApplicationModel.PROP_APPROVE_FOLDER.toString());
            NodeRef rejectFolder = (NodeRef) props.get(ApplicationModel.PROP_REJECT_FOLDER.toString());

            // put the workflow properties in a separate map for use by the JSP
            this.workflowProperties = new HashMap<String, Serializable>(7);
            this.workflowProperties.put(SimpleWorkflowHandler.PROP_APPROVE_STEP_NAME, approveStepName);
            this.workflowProperties.put(SimpleWorkflowHandler.PROP_APPROVE_ACTION, approveMove ? "move" : "copy");
            this.workflowProperties.put(SimpleWorkflowHandler.PROP_APPROVE_FOLDER, approveFolder);

            if (rejectStepName == null || rejectMove == null || rejectFolder == null)
            {
                this.workflowProperties.put(SimpleWorkflowHandler.PROP_REJECT_STEP_PRESENT, "no");
            }
            else
            {
                this.workflowProperties.put(SimpleWorkflowHandler.PROP_REJECT_STEP_PRESENT, "yes");
                this.workflowProperties.put(SimpleWorkflowHandler.PROP_REJECT_STEP_NAME, rejectStepName);
                this.workflowProperties.put(SimpleWorkflowHandler.PROP_REJECT_ACTION, rejectMove ? "move" : "copy");
                this.workflowProperties.put(SimpleWorkflowHandler.PROP_REJECT_FOLDER, rejectFolder);
            }
        }

        return this.workflowProperties;
    }

    public Node getNode()
    {
        return this.browseBean.getActionSpace();
    }
}
