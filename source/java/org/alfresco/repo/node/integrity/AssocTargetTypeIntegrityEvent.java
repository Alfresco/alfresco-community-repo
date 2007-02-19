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
import java.util.Set;

import org.alfresco.service.cmr.dictionary.AspectDefinition;
import org.alfresco.service.cmr.dictionary.AssociationDefinition;
import org.alfresco.service.cmr.dictionary.ClassDefinition;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.dictionary.TypeDefinition;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.QName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Event to check the target type of an association
 * <p>
 * Checks are ignored if the target node has been deleted.
 * 
 * @author Derek Hulley
 */
public class AssocTargetTypeIntegrityEvent extends AbstractIntegrityEvent
{
    private static Log logger = LogFactory.getLog(AssocTargetTypeIntegrityEvent.class);
    
    public AssocTargetTypeIntegrityEvent(
            NodeService nodeService,
            DictionaryService dictionaryService,
            NodeRef targetNodeRef,
            QName assocTypeQName)
    {
        super(nodeService, dictionaryService, targetNodeRef, assocTypeQName, null);
    }
    
    public void checkIntegrity(List<IntegrityRecord> eventResults)
    {
        QName assocTypeQName = getTypeQName();
        NodeRef targetNodeRef = getNodeRef();
        // if the node is gone then the check is irrelevant
        QName targetNodeTypeQName = getNodeType(targetNodeRef);
        if (targetNodeTypeQName == null)
        {
            // target or source is missing
            if (logger.isDebugEnabled())
            {
                logger.debug("Ignoring integrity check - node gone: \n" +
                        "   event: " + this);
            }
            return;
        }
        
        // get the association def
        AssociationDefinition assocDef = getAssocDef(eventResults, assocTypeQName);
        // the association definition must exist
        if (assocDef == null)
        {
            IntegrityRecord result = new IntegrityRecord(
                    "Association type does not exist: \n" +
                    "   Target Node Type: " + targetNodeTypeQName + "\n" +
                    "   Association Type: " + assocTypeQName);
            eventResults.add(result);
            return;
        }
        
        // perform required checks
        checkTargetType(eventResults, assocDef, targetNodeRef, targetNodeTypeQName);
    }
    
    /**
     * Checks that the target node type is valid for the association. 
     */
    protected void checkTargetType(
            List<IntegrityRecord> eventResults,
            AssociationDefinition assocDef,
            NodeRef targetNodeRef,
            QName targetNodeTypeQName)
    {
        // check the association target type
        ClassDefinition targetDef = assocDef.getTargetClass();
        if (targetDef instanceof TypeDefinition)
        {
            // the node type must be a match
            if (!dictionaryService.isSubClass(targetNodeTypeQName, targetDef.getName()))
            {
                IntegrityRecord result = new IntegrityRecord(
                        "The association target type is incorrect: \n" +
                        "   Association: " + assocDef + "\n" +
                        "   Required Target Type: " + targetDef.getName() + "\n" +
                        "   Actual Target Type: " + targetNodeTypeQName);
                eventResults.add(result);
            }
        }
        else if (targetDef instanceof AspectDefinition)
        {
            // the target must have a relevant aspect
            Set<QName> targetAspects = nodeService.getAspects(targetNodeRef);
            boolean found = false;
            for (QName targetAspectTypeQName : targetAspects)
            {
                if (dictionaryService.isSubClass(targetAspectTypeQName, targetDef.getName()))
                {
                    found = true;
                    break;
                }
            }
            if (!found)
            {
                IntegrityRecord result = new IntegrityRecord(
                        "The association target is missing the aspect required for this association: \n" +
                        "   Association: " + assocDef + "\n" +
                        "   Required Target Aspect: " + targetDef.getName() + "\n" +
                        "   Actual Target Aspects: " + targetAspects);
                eventResults.add(result);
            }
        }
        else
        {
            IntegrityRecord result = new IntegrityRecord(
                    "Unknown ClassDefinition subclass on the target definition: \n" +
                    "   Association: " + assocDef + "\n" +
                    "   Source Definition: " + targetDef.getName());
            eventResults.add(result);
        }
    }
}
