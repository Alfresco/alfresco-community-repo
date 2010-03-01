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
package org.alfresco.repo.tagging;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.action.ParameterDefinitionImpl;
import org.alfresco.repo.action.executer.ActionExecuterAbstractBase;
import org.alfresco.repo.content.MimetypeMap;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.service.cmr.action.Action;
import org.alfresco.service.cmr.action.ParameterDefinition;
import org.alfresco.service.cmr.dictionary.DataTypeDefinition;
import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.service.cmr.repository.ContentService;
import org.alfresco.service.cmr.repository.ContentWriter;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.tagging.TagDetails;
import org.alfresco.service.cmr.tagging.TagScope;
import org.alfresco.service.cmr.tagging.TaggingService;

/**
 * Update tag scopes action executer.
 * 
 * NOTE:  This action is used to facilitate the async update of tag scopes.  It is not intended for general usage.
 * 
 * @author Roy Wetherall
 */
public class UpdateTagScopesActionExecuter extends ActionExecuterAbstractBase
{
    /** Node Service */
    private NodeService nodeService;
    
    /** Content Service */
    private ContentService contentService;
    
    /** Tagging Service */
    private TaggingService taggingService;
    
    /** Action name and parameters */
    public static final String NAME = "update-tagscope";
    public static final String PARAM_TAG_UPDATES = "tag_updates";
    
    /**
     * Set the node service
     * 
     * @param nodeService   node service
     */
    public void setNodeService(NodeService nodeService)
    {
        this.nodeService = nodeService;
    }
    
    /**
     * Set the content service
     * 
     * @param contentService    the content service
     */
    public void setContentService(ContentService contentService)
    {
        this.contentService = contentService;
    }
    
    /**
     * Set the tagging service
     * 
     * @param taggingService    the tagging service
     */
    public void setTaggingService(TaggingService taggingService)
    {
        this.taggingService = taggingService;
    }
    
    /**
     * @see org.alfresco.repo.action.executer.ActionExecuterAbstractBase#executeImpl(org.alfresco.service.cmr.action.Action, org.alfresco.service.cmr.repository.NodeRef)
     */
    @Override
    protected void executeImpl(final Action action, final NodeRef actionedUponNodeRef)
    {
        try
        {            
            if (this.nodeService.exists(actionedUponNodeRef) == true)
            {
                // Run the update as the system user
                AuthenticationUtil.runAs(new AuthenticationUtil.RunAsWork<Object>()
                {
                    @SuppressWarnings("unchecked")
                    public Object doWork() throws Exception
                    {
                        // Get the parameter values
                        Map<String, Boolean> tagUpdates = (Map<String, Boolean>)action.getParameterValue(PARAM_TAG_UPDATES);
                        
                        // Get the tag scopes for the actioned upon node
                        List<TagScope> tagScopes = taggingService.findAllTagScopes(actionedUponNodeRef);
                        
                        // Update each tag scope
                        for (TagScope tagScope : tagScopes)
                        {
                            NodeRef tagScopeNodeRef = tagScope.getNodeRef();                
                            List<TagDetails> tags = null;
                            
                            // Get the current tags
                            ContentReader contentReader = contentService.getReader(tagScopeNodeRef, ContentModel.PROP_TAGSCOPE_CACHE);
                            if (contentReader == null)
                            {
                                tags = new ArrayList<TagDetails>(1);
                            }
                            else
                            {
                                tags = TaggingServiceImpl.readTagDetails(contentReader.getContentInputStream());
                            }
                            
                            for (String tagName : tagUpdates.keySet())
                            {
                                boolean isAdd = tagUpdates.get(tagName).booleanValue();
                                                 
                                TagDetails currentTag = null;
                                for (TagDetails tag : tags)
                                {
                                    if (tag.getName().equals(tagName) == true)
                                    {
                                        currentTag = tag;
                                        break;
                                    }
                                }
                                
                                if (isAdd == true)
                                {
                                    if (currentTag == null)
                                    {
                                        tags.add(new TagDetailsImpl(tagName, 1));
                                    }
                                    else
                                    {
                                        ((TagDetailsImpl)currentTag).incrementCount();
                                    }
                                 
                                }
                                else
                                {
                                    if (currentTag != null)
                                    {
                                        int currentTagCount = currentTag.getCount();                        
                                        if (currentTagCount == 1)
                                        {
                                            tags.remove(currentTag);
                                        }
                                        else
                                        {
                                            ((TagDetailsImpl)currentTag).decrementCount();
                                        }
                                    }
                                }
                            }
                            
                            // Order the list
                            Collections.sort(tags);
                            
                            // Write new content back to tag scope
                            String tagContent = TaggingServiceImpl.tagDetailsToString(tags);
                            ContentWriter contentWriter = contentService.getWriter(tagScopeNodeRef, ContentModel.PROP_TAGSCOPE_CACHE, true);
                            contentWriter.setEncoding("UTF-8");
                            contentWriter.setMimetype(MimetypeMap.MIMETYPE_TEXT_PLAIN);
                            contentWriter.putContent(tagContent);    
                        }

                        return null;
                    }
                    
                }, AuthenticationUtil.getSystemUserName());                
            }        
        }
        catch (RuntimeException exception)
        {
            exception.printStackTrace();
            throw new RuntimeException("Unable to update the tag scopes.", exception);
        }
    }

    /**
     * @see org.alfresco.repo.action.ParameterizedItemAbstractBase#addParameterDefinitions(java.util.List)
     */
    @Override
    protected void addParameterDefinitions(List<ParameterDefinition> paramList)
    {
        paramList.add(new ParameterDefinitionImpl(PARAM_TAG_UPDATES, DataTypeDefinition.ANY, true, getParamDisplayLabel(PARAM_TAG_UPDATES)));        
    }

}
