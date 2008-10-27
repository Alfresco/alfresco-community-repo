/*
 * Copyright (C) 2005-2008 Alfresco Software Limited.
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
