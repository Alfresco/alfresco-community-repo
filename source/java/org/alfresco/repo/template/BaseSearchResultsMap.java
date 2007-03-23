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
package org.alfresco.repo.template;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.search.ResultSet;
import org.alfresco.service.cmr.search.ResultSetRow;
import org.alfresco.service.cmr.search.SearchService;

/**
 * Class providing the base Search Query services to execute a search returning a list of TemplateNode objects from a Lucene search string.
 * 
 * @author Kevin Roast
 */
public abstract class BaseSearchResultsMap extends BaseTemplateMap
{
    /**
     * Constructor
     * 
     * @param parent
     *            The parent TemplateNode to execute searches from
     * @param services
     *            The ServiceRegistry to use
     */
    public BaseSearchResultsMap(TemplateNode parent, ServiceRegistry services)
    {
        super(parent, services);
    }

    /**
     * Perform a SearchService query with the given Lucene search string
     */
    protected List<TemplateNode> query(String search)
    {
        List<TemplateNode> nodes = null;
        HashSet<NodeRef> nodeRefs = new HashSet<NodeRef>();

        // check if a full Lucene search string has been supplied or extracted from XML
        if (search != null && search.length() != 0)
        {
            // perform the search against the repo
            ResultSet results = null;
            try
            {
                results = this.services.getSearchService().query(this.parent.getNodeRef().getStoreRef(),
                        SearchService.LANGUAGE_LUCENE, search);

                if (results.length() != 0)
                {
                    nodes = new ArrayList<TemplateNode>(results.length());
                    for (ResultSetRow row : results)
                    {
                        NodeRef nodeRef = row.getNodeRef();
                        if (!nodeRefs.contains(nodeRef))
                        {
                            nodes.add(new TemplateNode(nodeRef, services, this.parent.getImageResolver()));
                            nodeRefs.add(nodeRef);
                        }
                    }
                }
            }
            catch (Throwable err)
            {
                throw new AlfrescoRuntimeException("Failed to execute search: " + search, err);
            }
            finally
            {
                if (results != null)
                {
                    results.close();
                }
            }
        }

        return nodes != null ? nodes : (List) Collections.emptyList();
    }
}
