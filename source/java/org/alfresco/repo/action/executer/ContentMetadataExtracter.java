/*
 * Copyright (C) 2005 Jesper Steen Møller
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
package org.alfresco.repo.action.executer;

import java.io.Serializable;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.content.metadata.MetadataExtracter;
import org.alfresco.repo.content.metadata.MetadataExtracterRegistry;
import org.alfresco.service.cmr.action.Action;
import org.alfresco.service.cmr.action.ParameterDefinition;
import org.alfresco.service.cmr.dictionary.ClassDefinition;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.dictionary.PropertyDefinition;
import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.service.cmr.repository.ContentService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.QName;

/**
 * Extract metadata from any added content.
 * <p>
 * Currently, the default {@linkplain MetadataExtracter.OverwritePolicy overwrite policy}
 * for each extracter is used. (TODO: Add overwrite policy as a parameter.)
 * 
 * @see MetadataExtracter.OverwritePolicy
 * 
 * @author Jesper Steen Møller
 */
public class ContentMetadataExtracter extends ActionExecuterAbstractBase
{
    /**
     * The node service
     */
    private NodeService nodeService;

    /**
     * Set the node service
     * 
     * @param nodeService the node service
     */
    public void setNodeService(NodeService nodeService)
    {
        this.nodeService = nodeService;
    }

    /**
     * Our content service
     */
    private ContentService contentService;

    /**
     * @param contentService The contentService to set.
     */
    public void setContentService(ContentService contentService)
    {
        this.contentService = contentService;
    }
    
    /**
     * The dictionary service
     */
    private DictionaryService dictionaryService;
    
    /**
     * @param dictService  The DictionaryService to set.
     */
    public void setDictionaryService(DictionaryService dictService)
    {
        this.dictionaryService = dictService;
    }

    /**
     * Our Extracter
     */
    private MetadataExtracterRegistry metadataExtracterRegistry;

    /**
     * @param metadataExtracterRegistry The metadataExtracterRegistry to set.
     */
    public void setMetadataExtracterRegistry(MetadataExtracterRegistry metadataExtracterRegistry)
    {
        this.metadataExtracterRegistry = metadataExtracterRegistry;
    }

    /**
     * @see org.alfresco.repo.action.executer.ActionExecuter#execute(org.alfresco.service.cmr.repository.NodeRef,
     *      NodeRef)
     */
    public void executeImpl(Action ruleAction, NodeRef actionedUponNodeRef)
    {
        if (!nodeService.exists(actionedUponNodeRef))
        {
            // Node is gone
            return;
        }
        ContentReader reader = contentService.getReader(actionedUponNodeRef, ContentModel.PROP_CONTENT);
        // The reader may be null, e.g. for folders and the like
        if (reader == null || reader.getMimetype() == null)
        {
            // No content to extract data from
            return;
        }
        String mimetype = reader.getMimetype();
        MetadataExtracter extracter = metadataExtracterRegistry.getExtracter(mimetype);
        if (extracter == null)
        {
            // There is no extracter to use
            return;
        }
        
        // Get all the node's properties
        Map<QName, Serializable> nodeProperties = nodeService.getProperties(actionedUponNodeRef);
        
        // TODO: The override policy should be a parameter here.  Instead, we'll use the default policy
        //       set on the extracter.
        // Give the node's properties to the extracter to be modified
        Map<QName, Serializable> modifiedProperties = extracter.extract(
                reader,
                /*OverwritePolicy.PRAGMATIC,*/
                nodeProperties);

        // If none of the properties where changed, then there is nothing more to do
        if (modifiedProperties.size() == 0)
        {
            return;
        }
        
        // Check that all properties have the appropriate aspect applied
        Set<QName> requiredAspectQNames = new HashSet<QName>(3);
        
        for (QName propertyQName : modifiedProperties.keySet())
        {
            PropertyDefinition propertyDef = dictionaryService.getProperty(propertyQName);
            if (propertyDef == null)
            {
                // The property is not defined in the model
                continue;
            }
            ClassDefinition propertyContainerDef = propertyDef.getContainerClass();
            if (propertyContainerDef.isAspect())
            {
                QName aspectQName = propertyContainerDef.getName();
                requiredAspectQNames.add(aspectQName);
            }
        }
        
        // Add all the properties to the node BEFORE we add the aspects
        nodeService.setProperties(actionedUponNodeRef, nodeProperties);
        
        // Add each of the aspects, as required
        for (QName requiredAspectQName : requiredAspectQNames)
        {
             if (nodeService.hasAspect(actionedUponNodeRef, requiredAspectQName))
             {
                 // The node has the aspect already
                 continue;
             }
             else
             {
                 nodeService.addAspect(actionedUponNodeRef, requiredAspectQName, null);
             }
        }
    }

    @Override
    protected void addParameterDefinitions(List<ParameterDefinition> arg0)
    {
        // None!
    }
}