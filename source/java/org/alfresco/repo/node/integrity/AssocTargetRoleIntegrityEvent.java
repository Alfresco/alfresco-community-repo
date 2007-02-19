/*
 * Copyright (C) 2005-2007 Alfresco Software Limited.
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
package org.alfresco.repo.node.integrity;

import java.util.List;

import org.alfresco.service.cmr.dictionary.AssociationDefinition;
import org.alfresco.service.cmr.dictionary.ChildAssociationDefinition;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.QName;
import org.alfresco.service.namespace.RegexQNamePattern;

/**
 * Event to check the association target role name
 * 
 * @author Derek Hulley
 */
public class AssocTargetRoleIntegrityEvent extends AbstractIntegrityEvent
{
    public AssocTargetRoleIntegrityEvent(
            NodeService nodeService,
            DictionaryService dictionaryService,
            NodeRef sourceNodeRef,
            QName assocTypeQName,
            QName assocName)
    {
        super(nodeService, dictionaryService, sourceNodeRef, assocTypeQName, assocName);
    }
    
    public void checkIntegrity(List<IntegrityRecord> eventResults)
    {
        QName assocTypeQName = getTypeQName();
        QName assocQName = getQName();
        
        // get the association def
        AssociationDefinition assocDef = getAssocDef(eventResults, assocTypeQName);
        // the association definition must exist
        if (assocDef == null)
        {
            IntegrityRecord result = new IntegrityRecord(
                    "Association type does not exist: \n" +
                    "   Association Type: " + assocTypeQName);
            eventResults.add(result);
            return;
        }
        
        // check that we are dealing with child associations
        if (assocQName == null)
        {
            throw new IllegalArgumentException("The association qualified name must be supplied");
        }
        if (!assocDef.isChild())
        {
            throw new UnsupportedOperationException("This operation is only relevant to child associations");
        }
        ChildAssociationDefinition childAssocDef = (ChildAssociationDefinition) assocDef;
        
        // perform required checks
        checkAssocQNameRegex(eventResults, childAssocDef, assocQName);
    }

    /**
     * Checks that the association name matches the constraints imposed by the model.
     */
    protected void checkAssocQNameRegex(
            List<IntegrityRecord> eventResults,
            ChildAssociationDefinition assocDef,
            QName assocQName)
    {
        // check the association name
        QName assocRoleQName = assocDef.getTargetRoleName();
        if (assocRoleQName != null)
        {
            // the assoc defines a role name - check it
            RegexQNamePattern rolePattern = new RegexQNamePattern(assocRoleQName.toString());
            if (!rolePattern.isMatch(assocQName))
            {
                IntegrityRecord result = new IntegrityRecord(
                        "The association name does not match the allowed role names: \n" +
                        "   Association: " + assocDef + "\n" +
                        "   Allowed roles: " + rolePattern + "\n" +
                        "   Name assigned: " + assocRoleQName);
                eventResults.add(result);
            }
        }
    }
}
