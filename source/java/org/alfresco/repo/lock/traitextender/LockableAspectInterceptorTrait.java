
package org.alfresco.repo.lock.traitextender;

import org.alfresco.repo.lock.mem.LockState;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.traitextender.Trait;

public interface LockableAspectInterceptorTrait extends Trait
{
    LockState traitImplOf_getLockState(NodeRef nodeRef);
}
