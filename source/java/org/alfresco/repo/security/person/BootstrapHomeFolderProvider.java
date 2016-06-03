package org.alfresco.repo.security.person;

import org.alfresco.service.cmr.repository.NodeRef;

/**
 * Provider to use in the boostrap process - does nothing
 * 
 * Thought to be probably not required as behaviour/policies are
 * disabled during normal import, but is used for 'admin' and 'guest'
 * 
 * @author Andy Hind
 */
public class BootstrapHomeFolderProvider extends AbstractHomeFolderProvider2
{
    @Override
    public HomeSpaceNodeRef getHomeFolder(NodeRef person)
    {
        return new HomeSpaceNodeRef(null, HomeSpaceNodeRef.Status.VALID);
    }
}
