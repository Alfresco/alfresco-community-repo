/*
 * Copyright (C) 2005-2010 Alfresco Software Limited.
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
package org.alfresco.repo.copy;

import java.io.Serializable;
import java.util.Map;

import org.alfresco.service.namespace.QName;
import org.alfresco.util.Pair;


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
     * Default behaviour:<br/>
     * * {@link AssocCopySourceAction#COPY_REMOVE_EXISTING}<br/>
     * * {@link AssocCopyTargetAction#USE_COPIED_OTHERWISE_ORIGINAL_TARGET}
     */
    @Override
    public Pair<AssocCopySourceAction, AssocCopyTargetAction> getAssociationCopyAction(
            QName classQName,
            CopyDetails copyDetails,
            CopyAssociationDetails assocCopyDetails)
    {
        return new Pair<AssocCopySourceAction, AssocCopyTargetAction>(
                AssocCopySourceAction.COPY_REMOVE_EXISTING,
                AssocCopyTargetAction.USE_COPIED_OTHERWISE_ORIGINAL_TARGET);
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
