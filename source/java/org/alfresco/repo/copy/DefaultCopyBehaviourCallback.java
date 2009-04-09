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
import java.util.Map;

import org.alfresco.service.namespace.QName;


/**
 * The default behaviour that a type of aspect implements if there is no associated
 * <{@link CopyBehaviourCallback behaviour}.
 * <p>
 * This implementation is {@link #getInstance() stateless} and therefore thread-safe.
 * <p>
 * The default behaviour is:
 * <ul>
 *    <li><b>Must Copy:</b>         YES</li>
 *    <li><b>Must Cascade:</b>      YES, if cascade is on</li>
 *    <li><b>Properties to Copy:</b>ALL</li>
 * </ul>
 * 
 * @author Derek Hulley
 * @since 3.2
 */
public class DefaultCopyBehaviourCallback extends AbstractCopyBehaviourCallback
{
    private static CopyBehaviourCallback instance = new DefaultCopyBehaviourCallback();
    
    /**
     * @return          Returns a stateless singleton
     */
    public static CopyBehaviourCallback getInstance()
    {
        return instance;
    }
    
    /**
     * Default behaviour: Always copy
     * 
     * @return          Returns <tt>true</tt> always
     */
    public boolean getMustCopy(QName classQName, CopyDetails copyDetails)
    {
        return true;
    }

    /**
     * Default behaviour: Cascade if we are copying children <b>AND</b> the association is primary
     * 
     * @return          Returns <tt>true</tt> if the association is primary and <code>copyChildren == true</code>
     */
    public ChildAssocCopyAction getChildAssociationCopyAction(
            QName classQName,
            CopyDetails copyDetails,
            CopyChildAssociationDetails childAssocCopyDetails)
    {
        if (!childAssocCopyDetails.isCopyChildren())
        {
            return ChildAssocCopyAction.IGNORE;
        }
        if (childAssocCopyDetails.getChildAssocRef().isPrimary())
        {
            return ChildAssocCopyAction.COPY_CHILD;
        }
        else
        {
            return ChildAssocCopyAction.COPY_ASSOC;
        }
    }

    /**
     * Default behaviour: Copy all associated properties
     * 
     * @return          Returns all the properties passes in
     */
    public Map<QName, Serializable> getCopyProperties(
            QName classQName,
            CopyDetails copyDetails,
            Map<QName, Serializable> properties)
    {
        return properties;
    }
}
