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
package org.alfresco.web.bean.content;

import java.io.Serializable;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Map;

import javax.faces.context.FacesContext;

import org.alfresco.model.ApplicationModel;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;
import org.alfresco.web.app.Application;
import org.alfresco.web.bean.actions.handlers.SimpleWorkflowHandler;
import org.alfresco.web.bean.dialog.BaseDialogBean;
import org.alfresco.web.bean.repository.Node;
import org.alfresco.web.ui.common.ReportedException;
import org.alfresco.web.ui.common.Utils;

public class EditSimpleWorkflowDialog extends BaseDialogBean
{

    private static final long serialVersionUID = 7203447561571625990L;

    private static final String MSG_ERROR_UPDATE_SIMPLEWORKFLOW = "error_update_simpleworkflow";

    protected Map<String, Serializable> workflowProperties;

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

    public Node getNode()
    {
        return this.browseBean.getDocument();
    }
    
    public Node getDocument()
    {
        return this.getNode();
    }
    
    public String saveWorkflow()
    {
        String outcome = "cancel";

        try
        {
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
            getNode().reset();
        }
        catch (Throwable e)
        {
            Utils.addErrorMessage(MessageFormat.format(Application.getMessage(FacesContext.getCurrentInstance(), MSG_ERROR_UPDATE_SIMPLEWORKFLOW), e.getMessage()), e);
            ReportedException.throwIfNecessary(e);
        }

        return outcome;
    }

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
}
