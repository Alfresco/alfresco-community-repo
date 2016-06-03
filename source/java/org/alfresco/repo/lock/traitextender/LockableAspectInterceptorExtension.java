
package org.alfresco.repo.lock.traitextender;

import org.alfresco.repo.lock.mem.LockState;
import org.alfresco.service.cmr.repository.NodeRef;

public interface LockableAspectInterceptorExtension
{
    LockState getLockState(NodeRef nodeRef);
}
