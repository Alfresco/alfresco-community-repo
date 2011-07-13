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
import org.alfresco.service.namespace.RegexQNamePattern;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Event to check the source type of an association
 * <p>
 * Checks are ignored if the source node has been deleted.
 * 
 * @author Derek Hulley
 */
public class AssocSourceTypeIntegrityEvent extends AbstractIntegrityEvent
{
    private static Log logger = LogFactory.getLog(AssocSourceTypeIntegrityEvent.class);
    
    public AssocSourceTypeIntegrityEvent(
            NodeService nodeService,
            DictionaryService dictionaryService,
            NodeRef sourceNodeRef,
            QName assocTypeQName)
    {
        super(nodeService, dictionaryService, sourceNodeRef, assocTypeQName, null);
    }
    
    public void checkIntegrity(List<IntegrityRecord> eventResults)
    {
        QName assocTypeQName = getTypeQName();
        NodeRef sourceNodeRef = getNodeRef();
        // if the node is gone then the check is irrelevant
        QName sourceNodeTypeQName = getNodeType(sourceNodeRef);
        if (sourceNodeTypeQName == null)
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
                    "   Source Node: " + sourceNodeRef + "\n" +
                    "   Source Node Type: " + sourceNodeTypeQName + "\n" +
                    "   Association Type: " + assocTypeQName);
            eventResults.add(result);
            return;
        }
        
        // perform required checks
        checkSourceType(eventResults, assocDef, sourceNodeRef, sourceNodeTypeQName);
    }
    
    /**
     * Checks that the source node type is valid for the association. 
     */
    protected void checkSourceType(
            List<IntegrityRecord> eventResults,
            AssociationDefinition assocDef,
            NodeRef sourceNodeRef,
            QName sourceNodeTypeQName)
    {
        // check the association source type
        ClassDefinition sourceDef = assocDef.getSourceClass();
        if (sourceDef instanceof TypeDefinition)
        {
            // the node type must be a match
            if (!dictionaryService.isSubClass(sourceNodeTypeQName, sourceDef.getName()))
            {
                IntegrityRecord result = new IntegrityRecord(
                        "The association source type is incorrect: \n" +
                        "   Source Node: " + sourceNodeRef + "\n" +
                        "   Association: " + assocDef + "\n" +
                        "   Required Source Type: " + sourceDef.getName() + "\n" +
                        "   Actual Source Type: " + sourceNodeTypeQName);
                eventResults.add(result);
            }
        }
        else if (sourceDef instanceof AspectDefinition)
        {
            // the source must have a relevant aspect
            Set<QName> sourceAspects = nodeService.getAspects(sourceNodeRef);
            boolean found = false;
            for (QName sourceAspectTypeQName : sourceAspects)
            {
                if (dictionaryService.isSubClass(sourceAspectTypeQName, sourceDef.getName()))
                {
                    found = true;
                    break;
                }
            }
            if (!found)
            {
                // Actually make sure that the association still exists
                if (assocDef.isChild())
                {
                    if (nodeService.getChildAssocs(sourceNodeRef, assocDef.getName(), RegexQNamePattern.MATCH_ALL).size() == 0)
                    {
                        // The association does not exist any more
                        return;
                    }
                }
                else
                {
                    if (nodeService.getTargetAssocs(sourceNodeRef, assocDef.getName()).size() == 0)
                    {
                        // The association does not exist any more
                        return;
                    }
                }
                // The association is still present
                IntegrityRecord result = new IntegrityRecord(
                        "The association source is missing the aspect required for this association: \n" +
                        "   Source Node: " + sourceNodeRef + "\n" +
                        "   Association: " + assocDef + "\n" +
                        "   Required Source Aspect: " + sourceDef.getName() + "\n" +
                        "   Actual Source Aspects: " + sourceAspects);
                eventResults.add(result);
            }
        }
        else
        {
            IntegrityRecord result = new IntegrityRecord(
                    "Unknown ClassDefinition subclass on the source definition: \n" +
                    "   Source Node: " + sourceNodeRef + "\n" +
                    "   Association: " + assocDef + "\n" +
                    "   Source Definition: " + sourceDef.getName());
            eventResults.add(result);
        }
    }
}
