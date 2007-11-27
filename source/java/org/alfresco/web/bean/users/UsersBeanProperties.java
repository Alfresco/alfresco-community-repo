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
package org.alfresco.web.bean.users;

import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.search.SearchService;
import org.alfresco.service.cmr.security.AuthenticationService;
import org.alfresco.service.cmr.security.PersonService;
import org.alfresco.service.cmr.usage.ContentUsageService;
import org.alfresco.web.bean.repository.Node;
import org.alfresco.web.ui.common.component.data.UIRichList;

public class UsersBeanProperties
{
    /** NodeService bean reference */
    private NodeService nodeService;

    /** SearchService bean reference */
    private SearchService searchService;
    
    /** AuthenticationService bean reference */
    private AuthenticationService authenticationService;

    /** PersonService bean reference */
    private PersonService personService;
    
    /** ContentUsageService bean reference */
    private ContentUsageService contentUsageService;
    
    
    /** Component reference for Users RichList control */
    private UIRichList usersRichList;

    /** action context */
    private Node person = null;
    
    private String password = null;
    private String oldPassword = null;
    private String confirm = null;
    private String searchCriteria = null;
    private String userName = null;
    
    // ------------------------------------------------------------------------------
    // Bean property getters and setters

    /**
     * @return the nodeService
     */
    public NodeService getNodeService()
    {
        return nodeService;
    }

    /**
     * @return the searchService
     */
    public SearchService getSearchService()
    {
        return searchService;
    }

    /**
     * @return the authenticationService
     */
    public AuthenticationService getAuthenticationService()
    {
        return authenticationService;
    }

    /**
     * @return the personService
     */
    public PersonService getPersonService()
    {
        return personService;
    }

    /**
     * @param nodeService        The NodeService to set.
     */
    public void setNodeService(NodeService nodeService)
    {
       this.nodeService = nodeService;
    }

    /**
     * @param searchService      the search service
     */
    public void setSearchService(SearchService searchService)
    {
       this.searchService = searchService;
    }

    /**
     * @param authenticationService  The AuthenticationService to set.
     */
    public void setAuthenticationService(AuthenticationService authenticationService)
    {
       this.authenticationService = authenticationService;
    }

    /**
     * @param personService  The PersonService to set.
     */
    public void setPersonService(PersonService personService)
    {
       this.personService = personService;
    }
    
    /**
     * @param contentUsageService  The ContentUsageService to set.
     */
    public void setContentUsageService(ContentUsageService contentUsageService)
    {
        this.contentUsageService = contentUsageService;
    }

    /**
     * @return Returns the usersRichList.
     */
    public UIRichList getUsersRichList()
    {
       return this.usersRichList;
    }

    /**
     * @param usersRichList  The usersRichList to set.
     */
    public void setUsersRichList(UIRichList usersRichList)
    {
       this.usersRichList = usersRichList;
    }

    /**
     * @return Returns the search criteria
     */
    public String getSearchCriteria()
    {
       return searchCriteria;
    }

    /**
     * @param searchCriteria The search criteria to select
     */
    public void setSearchCriteria(String searchCriteria)
    {
       this.searchCriteria = searchCriteria;
    }

    /**
     * @return Returns the confirm password.
     */
    public String getConfirm()
    {
       return this.confirm;
    }

    /**
     * @param confirm The confirm password to set.
     */
    public void setConfirm(String confirm)
    {
       this.confirm = confirm;
    }

    /**
     * @return Returns the password.
     */
    public String getPassword()
    {
       return this.password;
    }

    /**
     * @param password The password to set.
     */
    public void setPassword(String password)
    {
       this.password = password;
    }
    
    /**
     * @return Returns the old password.
     */
    public String getOldPassword()
    {
       return this.oldPassword;
    }

    /**
     * @param oldPassword The old password to set.
     */
    public void setOldPassword(String oldPassword)
    {
       this.oldPassword = oldPassword;
    }

    /**
     * @return Returns the person context.
     */
    public Node getPerson()
    {
       return this.person;
    }

    /**
     * @param person     The person context to set.
     */
    public void setPerson(Node person)
    {
       this.person = person;
       this.userName = (String)person.getProperties().get(ContentModel.PROP_USERNAME);
    }
    
    public Long getUserUsage(String userName)
    {
       long usage = this.contentUsageService.getUserUsage(userName);
       return (usage == -1 ? null : usage);
    }
    
    public Long getUserUsage()
    {
       long usage = this.contentUsageService.getUserUsage(this.userName);
       return (usage == -1 ? null : usage);
    }
  
    public Long getUserQuota()
    {
       long quota = this.contentUsageService.getUserQuota(this.userName);
       return (quota == -1 ? null : quota);
    }
     
    public boolean getUsagesEnabled()
    {
       return this.contentUsageService.getEnabled();
    }
}
