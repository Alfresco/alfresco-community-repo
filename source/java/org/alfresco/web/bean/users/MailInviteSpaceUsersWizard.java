package org.alfresco.web.bean.users;

import java.util.Set;

import org.alfresco.web.bean.spaces.InviteSpaceUsersWizard;

/**
 * MailInviteSpaceUsersWizard JSF managed bean.
 * Overrides the InviteSpaceUsersWizard bean to return a list of Groups without EVERYONE.
 */
public class MailInviteSpaceUsersWizard extends InviteSpaceUsersWizard
{
    private static final long serialVersionUID = -68947308160920434L;

    @Override
    protected Set<String> getGroups(String search)
    {
        // get the groups without the EVERYONE group
        return super.getGroups(search, false);
    }
}