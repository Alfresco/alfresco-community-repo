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
package org.alfresco.repo.node.archive;

import java.util.List;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.search.ResultSet;
import org.alfresco.service.cmr.search.ResultSetRow;
import org.alfresco.service.cmr.search.SearchParameters;
import org.alfresco.service.cmr.search.SearchService;
import org.alfresco.util.AbstractLifecycleBean;
import org.springframework.context.ApplicationEvent;

/**
 * Bootstrap component that component that ensures that any nodes tagged with the
 * <b>sys:deleted</b> aspects are removed as the archival process was probably interrupted.
 * 
 * @since 2.1
 * @author Derek Hulley
 */
public class DeletedTagBootstrap extends AbstractLifecycleBean
{
    private static final String LUCENE_QUERY =
        "+ASPECT:\"" + ContentModel.ASPECT_DELETED_NODE + "\"";

    private NodeService nodeService;
    private SearchService searchService;

    public void setNodeService(NodeService nodeService)
    {
        this.nodeService = nodeService;
    }

    public void setSearchService(SearchService searchService)
    {
        this.searchService = searchService;
    }

    @Override
    protected void onBootstrap(ApplicationEvent event)
    {
        AuthenticationUtil.setSystemUserAsCurrentUser();
        removeAspects();
    }
    
    private void removeAspects()
    {
        // Get all stores
        List<StoreRef> storeRefs = nodeService.getStores();
        for (StoreRef storeRef : storeRefs)
        {
            SearchParameters params = new SearchParameters();
            params.setLanguage(SearchService.LANGUAGE_LUCENE);
            params.addStore(storeRef);
            params.setQuery(LUCENE_QUERY);
            // Search
            ResultSet rs = searchService.query(params);
            try
            {
                for (ResultSetRow row : rs)
                {
                    NodeRef nodeRef = row.getNodeRef();
                    // Delete it
                    nodeService.deleteNode(nodeRef);
                }
            }
            finally
            {
                rs.close();
            }
        }
    }

    @Override
    protected void onShutdown(ApplicationEvent event)
    {
    }
}
