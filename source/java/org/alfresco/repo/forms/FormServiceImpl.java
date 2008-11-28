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
package org.alfresco.repo.forms;

import org.alfresco.service.cmr.model.FileFolderService;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.search.SearchService;
import org.alfresco.service.cmr.security.PermissionService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Form Service Implementation.
 * 
 * @author Gavin Cornwell
 */
public class FormServiceImpl implements FormService
{
    /** Logger */
    private static Log logger = LogFactory.getLog(FormServiceImpl.class);
    
    /** Services */
    private NodeService nodeService;
    private FileFolderService fileFolderService;
    private SearchService searchService;
    private PermissionService permissionService;
    
    /**
     * Set node service
     * 
     * @param nodeService   node service
     */
    public void setNodeService(NodeService nodeService)
    {
        this.nodeService = nodeService;
    }
    
    /**
     * Set file folder service
     * 
     * @param fileFolderService     file folder service
     */
    public void setFileFolderService(FileFolderService fileFolderService)
    {
        this.fileFolderService = fileFolderService;
    }
    
    /**
     * Set search service
     * 
     * @param searchService     search service
     */
    public void setSearchService(SearchService searchService)
    {
        this.searchService = searchService;
    }
    
    /**
     * Set permission service
     * 
     * @param permissionService     permission service
     */
    public void setPermissionService(PermissionService permissionService)
    {
        this.permissionService = permissionService;
    }
    
    /**
     * @see org.alfresco.repo.forms.FormService#getForm(java.lang.String)
     */
    public Form getForm(String id)
    {
        if (logger.isDebugEnabled())
            logger.debug("Retrieving form for item with id: " + id);
        
        return null;
    }

    /*
     * @see org.alfresco.repo.forms.FormService#saveForm(java.lang.String, org.alfresco.repo.forms.Form)
     */
    public void saveForm(String id, Form form)
    {
        if (logger.isDebugEnabled())
            logger.debug("Saving form for item with id '" + id + "': " + form);
    }
}
