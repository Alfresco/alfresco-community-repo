/*
 * Copyright (C) 2005 Jesper Steen Møller
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
package org.alfresco.repo.action.executer;

import java.io.Serializable;
import java.util.HashMap;
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
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

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
    private static Log logger = LogFactory.getLog(ContentMetadataExtracter.class);
    
    public static final String EXECUTOR_NAME = "extract-metadata";
    
    private NodeService nodeService;
    private ContentService contentService;
    private DictionaryService dictionaryService;
    private MetadataExtracterRegistry metadataExtracterRegistry;
    private boolean carryAspectProperties = true;
    
    public ContentMetadataExtracter()
    {
    }

    /**
     * @param nodeService the node service
     */
    public void setNodeService(NodeService nodeService)
    {
        this.nodeService = nodeService;
    }

    /**
     * @param contentService The contentService to set.
     */
    public void setContentService(ContentService contentService)
    {
        this.contentService = contentService;
    }
    
    /**
     * @param dictService  The DictionaryService to set.
     */
    public void setDictionaryService(DictionaryService dictService)
    {
        this.dictionaryService = dictService;
    }

    /**
     * @param metadataExtracterRegistry The metadataExtracterRegistry to set.
     */
    public void setMetadataExtracterRegistry(MetadataExtracterRegistry metadataExtracterRegistry)
    {
        this.metadataExtracterRegistry = metadataExtracterRegistry;
    }

    /**
     * Whether or not aspect-related properties must be carried to the new version of the node
     * 
     * @param carryAspectProperties     <tt>true</tt> (default) to carry all aspect-linked
     *                                  properties forward.  <tt>false</tt> will clean the
     *                                  aspect of any unextracted values.
     */
    public void setCarryAspectProperties(boolean carryAspectProperties)
    {
        this.carryAspectProperties = carryAspectProperties;
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
            if(logger.isDebugEnabled())
            {
                logger.debug("no content or mimetype - do nothing");
            }
            // No content to extract data from
            return;
        }
        String mimetype = reader.getMimetype();
        MetadataExtracter extracter = metadataExtracterRegistry.getExtracter(mimetype);
        if (extracter == null)
        {
            if(logger.isDebugEnabled())
            {
                logger.debug("no extracter for mimetype:" + mimetype);
            }
            // There is no extracter to use
            return;
        }
        
        // Get all the node's properties
        Map<QName, Serializable> nodeProperties = nodeService.getProperties(actionedUponNodeRef);
        
        // TODO: The override policy should be a parameter here.  Instead, we'll use the default policy
        //       set on the extracter.
        // Give the node's properties to the extracter to be modified
        Map<QName, Serializable> modifiedProperties = null;
        try
        {
            modifiedProperties = extracter.extract(
                    reader,
                    /*OverwritePolicy.PRAGMATIC,*/
                    nodeProperties);
        }
        catch (Throwable e)
        {
            // Extracters should attempt to handle all error conditions and extract
            // as much as they can.  If, however, one should fail, we don't want the
            // action itself to fail.  We absorb and report the exception here to
            // solve ETHREEOH-1936 and ALFCOM-2889.
            if (logger.isDebugEnabled())
            {
                logger.debug(
                        "Raw metadata extraction failed: \n" +
                        "   Extracter: " + this + "\n" +
                        "   Node:      " + actionedUponNodeRef + "\n" +
                        "   Content:   " + reader,
                        e);
            }
            else
            {
                logger.warn(
                        "Raw metadata extraction failed (turn on DEBUG for full error): \n" +
                        "   Extracter: " + this + "\n" +
                        "   Node:      " + actionedUponNodeRef + "\n" +
                        "   Content:   " + reader + "\n" +
                        "   Failure:   " + e.getMessage());
            }
            modifiedProperties = new HashMap<QName, Serializable>(0);
        }

        // If none of the properties where changed, then there is nothing more to do
        if (modifiedProperties.size() == 0)
        {
            return;
        }
        
        // Check that all properties have the appropriate aspect applied
        Set<QName> requiredAspectQNames = new HashSet<QName>(3);
        Set<QName> aspectPropertyQNames = new HashSet<QName>(17);
        
        /**
         * The modified properties contain null values as well.  As we are only interested
         * in the keys, this will force aspect aspect properties to be removed even if there
         * are no settable properties pertaining to the aspect.
         */
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
                // Get all properties associated with the aspect
                Set<QName> aspectProperties = propertyContainerDef.getProperties().keySet();
                aspectPropertyQNames.addAll(aspectProperties);
            }
        }
        
        if (!carryAspectProperties)
        {
            // Remove any node properties that are defined on the aspects but were not extracted
            for (QName aspectPropertyQName : aspectPropertyQNames)
            {
                if (!modifiedProperties.containsKey(aspectPropertyQName))
                {
                    // Simple case: This property was not extracted
                    nodeProperties.remove(aspectPropertyQName);
                }
                else if (modifiedProperties.get(aspectPropertyQName) == null)
                {
                    // Trickier (ALF-1823): The property was extracted as 'null'
                    nodeProperties.remove(aspectPropertyQName);
                }
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