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
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.dictionary.TypeDefinition;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.QName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Event raised to check nodes' aspects
 * 
 * @author Derek Hulley
 */
public class AspectsIntegrityEvent extends AbstractIntegrityEvent
{
    private static Log logger = LogFactory.getLog(AspectsIntegrityEvent.class);
    
    protected AspectsIntegrityEvent(
            NodeService nodeService,
            DictionaryService dictionaryService,
            NodeRef nodeRef)
    {
        super(nodeService, dictionaryService, nodeRef, null, null);
    }
    
    public void checkIntegrity(List<IntegrityRecord> eventResults)
    {
        NodeRef nodeRef = getNodeRef();
        if (!nodeService.exists(nodeRef))
        {
            // node has gone
            if (logger.isDebugEnabled())
            {
                logger.debug("Event ignored - node gone: " + this);
            }
            eventResults.clear();
            return;
        }
        else
        {
            checkMandatoryAspects(getNodeRef(), eventResults);
        }
    }

    /**
     * Checks that the node has the required mandatory aspects applied
     */
    private void checkMandatoryAspects(NodeRef nodeRef, List<IntegrityRecord> eventResults)
    {
        Set<QName> aspects = nodeService.getAspects(nodeRef);
        
        // get the node type
        QName nodeTypeQName = nodeService.getType(nodeRef);
        // get the aspects that should exist
        TypeDefinition typeDef = dictionaryService.getType(nodeTypeQName);
        List<AspectDefinition> mandatoryAspectDefs = typeDef.getDefaultAspects();
        
        // check
        for (AspectDefinition aspect : mandatoryAspectDefs)
        {
            if (aspects.contains(aspect.getName()))
            {
                // it's fine
                continue;
            }
            IntegrityRecord result = new IntegrityRecord(
                    "Mandatory aspect not set: \n" +
                    "   Node: " + nodeRef + "\n" +
                    "   Type: " + nodeTypeQName + "\n" +
                    "   Aspect: " + aspect.getName());
            eventResults.add(result);
            // next one
            continue;
        }
        // done
    }
}
