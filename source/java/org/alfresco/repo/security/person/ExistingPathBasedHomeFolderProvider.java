package org.alfresco.repo.security.person;

import org.alfresco.service.cmr.repository.NodeRef;

/**
 * HomeFolderProvider that simply uses the root path for the home folder.
 *
 * @deprecated 
 * Depreciated since 4.0. {@link ExistingPathBasedHomeFolderProvider2} should now be used.
 * 
 * @author Andy Hind
 */
public class ExistingPathBasedHomeFolderProvider extends AbstractHomeFolderProvider
{
    protected HomeSpaceNodeRef getHomeFolder(NodeRef person)
    {
        return getHomeFolderManager().getHomeFolder(getV2Adaptor(), person, true);
    }
}
