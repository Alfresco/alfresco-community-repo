
package org.alfresco.repo.web.scripts.site;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.security.PersonService;
import org.alfresco.service.cmr.site.SiteInfo;

/**
 * A simple POJO class for the state of a site. For easier passing to the FTL model.
 * 
 * @author jkaabimofrad
 */
public class SiteState
{

    private SiteInfo siteInfo;
    private List<MemberState> members;
    private boolean currentUserSiteManager;

    private SiteState()
    {
    }

    public static SiteState create(SiteInfo siteInfo, Map<String, String> members, String currentUser,
                NodeService nodeService, PersonService personService)
    {
        SiteState result = new SiteState();
        result.members = new ArrayList<MemberState>(members.size());

        result.siteInfo = siteInfo;

        boolean found = false;
        Set<String> siteMembers = members.keySet();
        for (String userName : siteMembers)
        {
            NodeRef person = personService.getPersonOrNull(userName);
            if (person != null)
            {
                String firstName = (String) nodeService.getProperty(person, ContentModel.PROP_FIRSTNAME);
                String lastName = (String) nodeService.getProperty(person, ContentModel.PROP_LASTNAME);
                result.members.add(new MemberState(userName, firstName, lastName));
            }
            
            if (!found && userName.equals(currentUser))
            {
                found = true;
                result.currentUserSiteManager = true;
            }
        }

        return result;
    }

    public SiteInfo getSiteInfo()
    {
        return this.siteInfo;
    }

    public List<MemberState> getMembers()
    {
        return this.members;
    }

    public boolean isCurrentUserSiteManager()
    {
        return this.currentUserSiteManager;
    }

    public static class MemberState
    {
        private String userName;
        private String firstName;
        private String lastName;

        public MemberState(String userName, String firstName, String lastName)
        {
            this.userName = userName;
            this.firstName = firstName;
            this.lastName = lastName;
        }

        public String getUserName()
        {
            return this.userName;
        }

        public String getFirstName()
        {
            return this.firstName;
        }

        public String getLastName()
        {
            return this.lastName;
        }

    }
}
