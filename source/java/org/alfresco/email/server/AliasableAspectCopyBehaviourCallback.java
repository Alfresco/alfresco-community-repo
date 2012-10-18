/*
 * Copyright (C) 2012-2012 Alfresco Software Limited.
 *
 * This file is part of Alfresco
 *
 * Alfresco is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Alfresco is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Alfresco. If not, see <http://www.gnu.org/licenses/>.
 */
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
     * Disallows copying of the {@link ASPECT_ALIASABLE aspect.
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
     * Prevents copying off the {@link ContentModel#PROP_NAME <b>cm:name</b>} property.
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


