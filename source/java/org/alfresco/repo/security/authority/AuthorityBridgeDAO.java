package org.alfresco.repo.security.authority;

import java.util.List;

import org.alfresco.service.cmr.repository.NodeRef;

/**
 * @author Andy
 *
 */
public interface AuthorityBridgeDAO
{
    List<AuthorityBridgeLink> getAuthorityBridgeLinks();
    
    List<AuthorityBridgeLink> getDirectAuthoritiesForUser(NodeRef authRef);
}
