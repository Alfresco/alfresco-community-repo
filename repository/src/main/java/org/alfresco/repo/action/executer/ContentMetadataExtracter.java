/*
 * #%L
 * Alfresco Repository
 * %%
 * Copyright (C) 2005 - 2016 Alfresco Software Limited
 * %%
 * This file is part of the Alfresco software. 
 * If the software was purchased under a paid Alfresco license, the terms of 
 * the paid license agreement will prevail.  Otherwise, the software is 
 * provided under the following open source license terms:
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
 * #L%
 */
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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.content.metadata.AbstractMappingMetadataExtracter;
import org.alfresco.repo.content.metadata.MetadataExtracter;
import org.alfresco.repo.content.metadata.MetadataExtracterRegistry;
import org.alfresco.service.cmr.action.Action;
import org.alfresco.service.cmr.action.ParameterDefinition;
import org.alfresco.service.cmr.dictionary.ClassDefinition;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.dictionary.PropertyDefinition;
import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.service.cmr.repository.ContentService;
import org.alfresco.service.cmr.repository.InvalidNodeRefException;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.datatype.DefaultTypeConverter;
import org.alfresco.service.cmr.tagging.TaggingService;
import org.alfresco.service.namespace.QName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Extract metadata from any added content.
 * <p>
 * Currently, the default {@linkplain org.alfresco.repo.content.metadata.MetadataExtracter.OverwritePolicy overwrite policy}
 * for each extracter is used. (TODO: Add overwrite policy as a parameter.)
 * 
 * @see org.alfresco.repo.content.metadata.MetadataExtracter.OverwritePolicy
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
    private TaggingService taggingService;
    private MetadataExtracterRegistry metadataExtracterRegistry;
    private boolean carryAspectProperties = true;
    
    
    private boolean enableStringTagging = false;
    
    // Default list of separators (when enableStringTagging is enabled)
    protected List<String> stringTaggingSeparators = Arrays.asList(",", ";", "\\|");
    
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
     * @param taggingService The TaggingService to set.
     */
    public void setTaggingService(TaggingService taggingService)
    {
        this.taggingService = taggingService;
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
     * Whether or not to enable mapping of simple strings to cm:taggable tags
     * 
     * @param enableStringTagging       <tt>true</tt> find or create tags for each string 
     *                                  mapped to cm:taggable.  <tt>false</tt> (default) 
     *                                  ignore mapping strings to tags.
     */
    public void setEnableStringTagging(boolean enableStringTagging)
    {
        this.enableStringTagging = enableStringTagging;
    }

    /**
     * List of string separators - note: all will be applied to a given string
     * 
     * @param stringTaggingSeparators
     */
    public void setStringTaggingSeparators(List<String> stringTaggingSeparators)
    {
        this.stringTaggingSeparators = stringTaggingSeparators;
    }

    /**
     * Iterates the values of the taggable property which the metadata
     * extractor should have already attempted to convert values to {@link NodeRef}s.
     * <p>
     * If conversion by the metadata extracter failed due to a MalformedNodeRefException
     * the taggable property should still contain raw string values.
     * <p>
     * Mixing of NodeRefs and string values is permitted so each raw value is
     * checked for a valid NodeRef representation and if so, converts to a NodeRef, 
     * if not, adds as a tag via the {@link TaggingService}.
     * 
     * @param actionedUponNodeRef The NodeRef being actioned upon
     * @param propertyDef the PropertyDefinition of the taggable property
     * @param rawValue the raw value from the metadata extracter
     */
    protected void addTags(NodeRef actionedUponNodeRef, PropertyDefinition propertyDef, Serializable rawValue)
    {
        if (rawValue == null)
        {
            return;
        }

        List<String> tags = new ArrayList<String>();

        if (logger.isDebugEnabled())
        {
            logger.debug("converting " + rawValue + " of type " + rawValue.getClass().getCanonicalName() + " to tags");
        }

        if (rawValue instanceof Collection<?>)
        {
            for (Object singleValue : (Collection<?>) rawValue)
            {
                if (singleValue instanceof String)
                {
                    if (NodeRef.isNodeRef((String) singleValue))
                    {
                        // Convert to a NodeRef
                        Serializable convertedPropertyValue = (Serializable) DefaultTypeConverter.INSTANCE.convert(
                                propertyDef.getDataType(),
                                (String) singleValue);
                        try
                        {
                            NodeRef nodeRef = (NodeRef) convertedPropertyValue;
                            String tagName = (String) nodeService.getProperty(nodeRef, ContentModel.PROP_NAME);

                            if (logger.isTraceEnabled())
                            {
                                logger.trace("adding string tag name'" + tagName + "' (from tag nodeRef "+nodeRef+") to " + actionedUponNodeRef);
                            }

                            tags.addAll(splitTag(tagName));
                        }
                        catch (InvalidNodeRefException e)
                        {
                            if (logger.isWarnEnabled())
                            {
                                logger.warn("tag nodeRef Invalid: " + e.getMessage());
                            }
                        }
                    }
                    else
                    {
                        // Must be a simple string

                        if (logger.isTraceEnabled())
                        {
                            logger.trace("adding string tag name'" + singleValue + "' to " + actionedUponNodeRef);
                        }

                        tags.addAll(splitTag((String)singleValue));
                    }
                }
                else if (singleValue instanceof NodeRef)
                {
                    NodeRef nodeRef = (NodeRef)singleValue;
                    String tagName = (String) nodeService.getProperty(nodeRef, ContentModel.PROP_NAME);

                    if (logger.isTraceEnabled())
                    {
                        logger.trace("adding string tag name'" + tagName + "' (for nodeRef "+nodeRef+") to " + actionedUponNodeRef);
                    }

                    tags.addAll(splitTag(tagName));
                }
            }
        }
        else if (rawValue instanceof String)
        {
            if (logger.isTraceEnabled())
            {
                logger.trace("adding string tag name'" + (String)rawValue + "' to " + actionedUponNodeRef);
            }
            
            tags.addAll(splitTag((String)rawValue));
        }

        if (logger.isDebugEnabled())
        {
            logger.debug("adding tags '" + tags + "' to " + actionedUponNodeRef);
        }

        try
        {
            taggingService.addTags(actionedUponNodeRef, tags);
        }
        catch (IllegalArgumentException iae)
        {
            // defensive catch-all - do not prevent uploads due to unexpected failure in "addTags"
            if (logger.isWarnEnabled())
            {
                logger.warn("Cannot add tags '"+tags+"' - "+iae.getMessage());
            }
        }
    }

    protected List<String> splitTag(String str)
    {
        List<String> result = new ArrayList<>();
        if ((str != null) && (!str.equals("")))
        {
            result.add(str.trim());

            if (stringTaggingSeparators != null)
            {
                for (String sep : stringTaggingSeparators)
                {
                    List<String> splitTags = new ArrayList<>(result.size());
                    for (String tag : result)
                    {
                        String[] parts = tag.split(sep);
                        for (String part : parts)
                        {
                            splitTags.add(part.trim());
                        }
                    }
                    result = splitTags;
                }
            }
        }

        return result;
    }
    
    /**
     * @see org.alfresco.repo.action.executer.ActionExecuter#execute(Action,
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
        if (enableStringTagging && (extracter instanceof AbstractMappingMetadataExtracter))
        {
            ((AbstractMappingMetadataExtracter) extracter).setEnableStringTagging(enableStringTagging);
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
                if (enableStringTagging && propertyContainerDef.getName().equals(ContentModel.ASPECT_TAGGABLE))
                {
                    Serializable oldValue = nodeProperties.get(propertyQName);
                    addTags(actionedUponNodeRef, propertyDef, oldValue);
                    // Replace the raw value with the created tag NodeRefs
                    nodeProperties.put(ContentModel.PROP_TAGS, 
                            nodeService.getProperty(actionedUponNodeRef, ContentModel.PROP_TAGS));
                }
                else
                {
                    QName aspectQName = propertyContainerDef.getName();
                    requiredAspectQNames.add(aspectQName);
                    // Get all properties associated with the aspect
                    Set<QName> aspectProperties = propertyContainerDef.getProperties().keySet();
                    aspectPropertyQNames.addAll(aspectProperties);
                }
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