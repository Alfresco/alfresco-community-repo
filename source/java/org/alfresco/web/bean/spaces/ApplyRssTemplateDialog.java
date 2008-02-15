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
import org.alfresco.web.ui.common.Utils;

public class ApplyRssTemplateDialog extends BaseDialogBean
{

    private static final String DIALOG_CLOSE = "dialog:close";

    private static final String MSG_APPLY_RSS_FEED = "apply_rss_feed";
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

    public boolean getFinishButtonDisabled()
    {
        return false;
    }

    public String getContainerTitle()
    {
        return Application.getMessage(FacesContext.getCurrentInstance(), MSG_APPLY_RSS_FEED) + " '" + getNode().getName() + "'";
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
