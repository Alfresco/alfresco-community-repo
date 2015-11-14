package org.alfresco.repo.domain.permissions.traitextender;

import org.alfresco.repo.domain.permissions.Acl;
import org.alfresco.service.cmr.repository.NodeRef;

public interface AccessControlListDAOExtension
{

    public Acl getAccessControlList(NodeRef nodeRef);
}
