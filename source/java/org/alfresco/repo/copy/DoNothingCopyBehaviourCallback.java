/*
 * Copyright (C) 2005-2009 Alfresco Software Limited.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.

 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.

 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.

 * As a special exception to the terms and conditions of version 2.0 of 
 * the GPL, you may redistribute this Program in connection with Free/Libre 
 * and Open Source Software ("FLOSS") applications as described in Alfresco's 
 * FLOSS exception.  You should have recieved a copy of the text describing 
 * the FLOSS exception, and it is also available here: 
 * http://www.alfresco.com/legal/licensing"
 */
package org.alfresco.repo.copy;

import java.io.Serializable;
import java.util.Collections;
import java.util.Map;

import org.alfresco.service.namespace.QName;

/**
 * Simple <i>copy behaviour</i> to prevent any copying.
 * <p>
 * This implementation is {@link #getInstance() stateless} and therefore thread-safe.
 * 
 * @author Derek Hulley
 * @since 3.2
 */
public class DoNothingCopyBehaviourCallback extends AbstractCopyBehaviourCallback
{
    private static CopyBehaviourCallback instance = new DoNothingCopyBehaviourCallback();
    
    /**
     * @return          Returns a stateless singleton
     */
    public static CopyBehaviourCallback getInstance()
    {
        return instance;
    }
    
    /**
     * @return          Returns <tt>false</tt> always
     */
    public boolean getMustCopy(QName classQName, CopyDetails copyDetails)
    {
        return false;
    }

    /**
     * @return          Returns <tt>false</tt> always
     */
    public ChildAssocCopyAction getChildAssociationCopyAction(
            QName classQName,
            CopyDetails copyDetails,
            CopyChildAssociationDetails childAssocCopyDetails)
    {
        return ChildAssocCopyAction.IGNORE;
    }

    /**
     * @return          Returns an empty map always
     */
    public Map<QName, Serializable> getCopyProperties(
            QName classQName,
            CopyDetails copyDetails,
            Map<QName, Serializable> properties)
    {
        return Collections.emptyMap();
    }
}
