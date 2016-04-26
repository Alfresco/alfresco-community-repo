package org.alfresco.email.server;

import java.io.Serializable;
import java.util.Collections;
import java.util.Map;

import org.alfresco.repo.copy.CopyBehaviourCallback;
import org.alfresco.repo.copy.CopyDetails;
import org.alfresco.repo.copy.DefaultCopyBehaviourCallback;
import org.alfresco.service.namespace.QName;

public class AliasableAspectCopyBehaviourCallback extends DefaultCopyBehaviourCallback
{
    static final CopyBehaviourCallback INSTANCE = new AliasableAspectCopyBehaviourCallback();
    
    /**
     * Disallows copying of the {@link EmailServerModel#ASPECT_ALIASABLE} aspect.
     */
    @Override
    public boolean getMustCopy(QName classQName, CopyDetails copyDetails)
    {
        if (classQName.equals(EmailServerModel.ASPECT_ALIASABLE))
        {
            return false;
        }
        else
        {
            return true;
        }
    }

    /**
     * Prevents copying off the {@link org.alfresco.model.ContentModel#PROP_NAME <b>cm:name</b>} property.
     */
    @Override
    public Map<QName, Serializable> getCopyProperties(
            QName classQName,
            CopyDetails copyDetails,
            Map<QName, Serializable> properties)
    {
        if (classQName.equals(EmailServerModel.ASPECT_ALIASABLE))
        {
            return Collections.emptyMap();
        }
        return properties;    
    }
}


