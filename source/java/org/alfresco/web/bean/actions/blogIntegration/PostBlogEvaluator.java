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
package org.alfresco.web.bean.actions.blogIntegration;

import javax.faces.context.FacesContext;

import org.alfresco.model.BlogIntegrationModel;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.blogIntegration.BlogIntegrationService;
import org.alfresco.repo.blogIntegration.BlogIntegrationServiceImpl;
import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.service.cmr.repository.ContentService;
import org.alfresco.web.action.evaluator.BaseActionEvaluator;
import org.alfresco.web.bean.repository.Node;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.jsf.FacesContextUtils;


/**
 * Post blog evaluator
 * 
 * @author Roy Wetherall
 */
public class PostBlogEvaluator extends BaseActionEvaluator implements BlogIntegrationModel
{
    /**
     * @see org.alfresco.web.action.ActionEvaluator#evaluate(org.alfresco.web.bean.repository.Node)
     */
    public boolean evaluate(Node node)
    {
        boolean result = false;
        
        // Get the conten service and the blog integration service
        WebApplicationContext applicationContext = FacesContextUtils.getRequiredWebApplicationContext(FacesContext.getCurrentInstance());
        ContentService contentService = (ContentService)applicationContext.getBean("ContentService");
        BlogIntegrationService blogIntegrationService = (BlogIntegrationService)applicationContext.getBean("BlogIntegrationService");
                
        // Check the mimetype of the content 
        ContentReader contentReader = contentService.getReader(node.getNodeRef(), ContentModel.PROP_CONTENT);
        if (contentReader != null)
        {
            String mimetype = contentReader.getMimetype();
            if (node.hasAspect(ASPECT_BLOG_POST) == false &&
                BlogIntegrationServiceImpl.supportedMimetypes.contains(mimetype) == true &&
                blogIntegrationService.getBlogDetails(node.getNodeRef()).size() != 0)
            {
                result = true;
            }
        }
        return result;
    }
}
