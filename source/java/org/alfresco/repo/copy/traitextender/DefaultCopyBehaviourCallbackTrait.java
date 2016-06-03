package org.alfresco.repo.copy.traitextender;

import org.alfresco.repo.copy.CopyDetails;
import org.alfresco.service.namespace.QName;
import org.alfresco.traitextender.Trait;

public interface DefaultCopyBehaviourCallbackTrait extends Trait
{
    boolean getMustCopy(QName classQName, CopyDetails copyDetails);
}
