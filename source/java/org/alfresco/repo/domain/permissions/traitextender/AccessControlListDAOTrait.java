package org.alfresco.repo.domain.permissions.traitextender;

import org.alfresco.repo.domain.permissions.Acl;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.traitextender.Trait;

public interface AccessControlListDAOTrait extends Trait
{
    public Acl getAccessControlList(NodeRef nodeRef);
}
