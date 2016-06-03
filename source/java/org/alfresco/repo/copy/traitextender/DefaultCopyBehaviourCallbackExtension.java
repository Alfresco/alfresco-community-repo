package org.alfresco.repo.copy.traitextender;

import org.alfresco.repo.copy.CopyDetails;
import org.alfresco.service.namespace.QName;

public interface DefaultCopyBehaviourCallbackExtension
{
    boolean getMustCopy(QName classQName, CopyDetails copyDetails);
}
