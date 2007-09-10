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
package org.alfresco.repo.webservice.administration;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.webservice.AbstractQuery;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.security.PersonService;

/**
 * A query to retrieve normal node associations.
 * 
 * @author Derek Hulley
 * @since 2.1
 */
public class UserQuery extends AbstractQuery<UserQueryResults>
{
    private static final long serialVersionUID = -672399618512462040L;

    private UserFilter userFilter;

    /**
     * @param userFilter
     *            The user filter
     */
    public UserQuery(UserFilter userFilter)
    {
        this.userFilter = userFilter;
    }

    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder(128);
        sb.append("AssociationQuery")
          .append("[ userFilter=").append(userFilter.getUserName())
          .append("]");
        return sb.toString();
    }

    /**
     * {@inheritDoc}
     */
    public UserQueryResults execute(ServiceRegistry serviceRegistry)
    {
        PersonService personService = serviceRegistry.getPersonService();
        NodeService nodeService = serviceRegistry.getNodeService();

        Set<NodeRef> nodeRefs = personService.getAllPeople();
        
        // Filter the results
        List<NodeRef> filteredNodeRefs = null;
        if (userFilter != null && userFilter.getUserName() != null && userFilter.getUserName().length() != 0)
        {
            String userNameFilter = userFilter.getUserName();
            
            filteredNodeRefs = new ArrayList<NodeRef>(nodeRefs.size());
            for (NodeRef nodeRef : nodeRefs)
            {
                String userName = (String) nodeService.getProperty(nodeRef, ContentModel.PROP_USERNAME);
                if (userName.matches(userNameFilter) == true)
                {
                    filteredNodeRefs.add(nodeRef);
                }
            }
        }
        else
        {
            filteredNodeRefs = new ArrayList<NodeRef>(nodeRefs);
        }
        
        UserDetails[] results = new UserDetails[filteredNodeRefs.size()];
        int index = 0;
        for (NodeRef nodeRef : filteredNodeRefs)
        {
            String userName = (String) nodeService.getProperty(nodeRef, ContentModel.PROP_USERNAME);
            results[index] = AdministrationWebService.createUserDetails(nodeService, userName, nodeRef);
            index++;
        }

        UserQueryResults queryResults = new UserQueryResults(null, results);
        
        // Done
        return queryResults;
    }
}