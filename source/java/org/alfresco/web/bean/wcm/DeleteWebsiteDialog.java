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
package org.alfresco.web.bean.wcm;

import javax.faces.context.FacesContext;

import org.alfresco.wcm.webproject.WebProjectService;
import org.alfresco.web.bean.repository.Node;
import org.alfresco.web.bean.repository.Repository;
import org.alfresco.web.bean.spaces.DeleteSpaceDialog;

/**
 * Bean implementation for the "Delete Website" dialog. Removes all user stores and the main staging and preview stores.
 * 
 * @author kevinr
 */
public class DeleteWebsiteDialog extends DeleteSpaceDialog
{
    private static final long serialVersionUID = -3598950865168230942L;

    transient private WebProjectService wpService;

    // ------------------------------------------------------------------------------
    // Bean property getters and setters

    public void setWebProjectService(WebProjectService wpService)
    {
       this.wpService = wpService;
    }

    protected WebProjectService getWebProjectService()
    {
       if (wpService == null)
       {
          wpService = Repository.getServiceRegistry(FacesContext.getCurrentInstance()).getWebProjectService();
       }
       return wpService;
    }

    // ------------------------------------------------------------------------------
    // Dialog implementation

    /**
     * @see org.alfresco.web.bean.dialog.BaseDialogBean#finishImpl(javax.faces.context.FacesContext, java.lang.String)
     */
    @Override
    protected String finishImpl(FacesContext context, String outcome) throws Exception
    {
       Node websiteNode = this.browseBean.getActionSpace();

       if (websiteNode != null)
       {
          getWebProjectService().deleteWebProject(websiteNode.getNodeRef());
       }
        
       return super.finishImpl(context, outcome);
    }

    /**
     * Returns the message bundle id of the confirmation message to display to the user before deleting the website.
     * 
     * @return The message bundle id
     */
    @Override
    protected String getConfirmMessageId()
    {
        return "delete_website_confirm";
    }
}
