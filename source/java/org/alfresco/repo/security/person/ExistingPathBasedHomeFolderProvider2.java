package org.alfresco.repo.security.person;

import org.alfresco.service.cmr.repository.NodeRef;

/**
 * HomeFolderProvider that simply uses the root path for the home folder.
 * Generally it is a better idea to give each user their own home folder.
 * 
 * @author Alan Davis
 */
public class ExistingPathBasedHomeFolderProvider2 extends AbstractHomeFolderProvider2
{
    @Override
    public HomeSpaceNodeRef getHomeFolder(NodeRef person)
    {
        return getHomeFolderManager().getHomeFolder(this, person, true);
    }
}
