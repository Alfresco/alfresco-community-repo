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

import java.text.MessageFormat;

import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;

import org.alfresco.model.ApplicationModel;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.web.app.Application;
import org.alfresco.web.bean.TemplateSupportBean;
import org.alfresco.web.bean.dialog.BaseDialogBean;
import org.alfresco.web.bean.repository.Node;
import org.alfresco.web.bean.repository.Repository;
import org.alfresco.web.ui.common.ReportedException;
import org.alfresco.web.ui.common.Utils;

public class ApplyRssTemplateDialog extends BaseDialogBean
{
    private static final long serialVersionUID = 9207265799149337182L;
    
    private static final String DIALOG_CLOSE = "dialog:close";
    private static final String MSG_APPLY_RSS_FEED = "apply_rss_feed";
    private final static String MSG_LEFT_QUOTE = "left_qoute";
    private final static String MSG_RIGHT_QUOTE = "right_quote";
    
    private String rssTemplate;
    
    
    public void setRSSTemplate(String rssTemplate)
    {
        this.rssTemplate = rssTemplate;
    }

    /**
     * @return Returns the current RSS Template ID.
     */
    public String getRSSTemplate()
    {
        // return current template if it exists
        NodeRef ref = (NodeRef) getNode().getProperties().get(ApplicationModel.PROP_FEEDTEMPLATE);
        return ref != null ? ref.getId() : this.rssTemplate;
    }

    @Override
    protected String finishImpl(FacesContext context, String outcome) throws Exception
    {
        applyRSSTemplate(null);
        return DIALOG_CLOSE;
    }

    public String getContainerTitle()
    {
        FacesContext fc = FacesContext.getCurrentInstance();
        return Application.getMessage(fc, MSG_APPLY_RSS_FEED) + " " + Application.getMessage(fc, MSG_LEFT_QUOTE) + getNode().getName() + Application.getMessage(fc, MSG_RIGHT_QUOTE);
    }

    public void applyRSSTemplate(ActionEvent event)
    {
        if (this.rssTemplate != null && this.rssTemplate.equals(TemplateSupportBean.NO_SELECTION) == false)
        {
            try
            {
                // apply the feedsource aspect if required
                if (getNode().hasAspect(ApplicationModel.ASPECT_FEEDSOURCE) == false)
                {
                    this.getNodeService().addAspect(getNode().getNodeRef(), ApplicationModel.ASPECT_FEEDSOURCE, null);
                }

                // get the selected template Id from the Template Picker
                NodeRef templateRef = new NodeRef(Repository.getStoreRef(), this.rssTemplate);

                // set the template NodeRef into the templatable aspect property
                this.getNodeService().setProperty(getNode().getNodeRef(), ApplicationModel.PROP_FEEDTEMPLATE, templateRef);

                // reset node details for next refresh of details page
                getNode().reset();
            }
            catch (Exception e)
            {
                Utils.addErrorMessage(MessageFormat.format(Application.getMessage(FacesContext.getCurrentInstance(), Repository.ERROR_GENERIC), e.getMessage()), e);
                ReportedException.throwIfNecessary(e);
            }
        }
    }

    /**
     * Returns the Node this bean is currently representing
     * 
     * @return The Node
     */
    public Node getNode()
    {
        return this.browseBean.getActionSpace();
    }
}