/*
 * Copyright (C) 2005-2014 Alfresco Software Limited.
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
package org.alfresco.repo.admin.patch.impl;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.alfresco.repo.admin.patch.AbstractPatch;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.tagging.TagScope;
import org.alfresco.service.cmr.tagging.TaggingService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.extensions.surf.util.I18NUtil;

/**
 * This patches Alfresco tags to remove broken char sequences from tag names
 * 
 * @see <a href="https://issues.alfresco.com/jira/browse/MNT-11871">MNT-11871</a>
 * @author sergey.shcherbovich
 */
public class ReplaceForbiddenTagCharSequencesPatch extends AbstractPatch 
{
    private String REPLACER = "_";
    private String[] BAD_TAG_SEQUENCES = new String[] {"\n", "|"};
    
    private static Log logger = LogFactory.getLog(ReplaceForbiddenTagCharSequencesPatch.class);
    
    private static String SUCCESS_MESSAGE = "patch.replaceForbiddenTagCharSequencesPatch.result";
    
    private TaggingService taggingService;
    
    @Override
    protected String applyInternal() throws Exception 
    {
        Set<TagScope> tagScopesToRefresh = new HashSet<>();
        
        for (StoreRef storeRef : nodeService.getStores())
        {
            for (String bad : BAD_TAG_SEQUENCES)
            {
                List<String> brokenTags = taggingService.getTags(storeRef, bad);
                
                for (String tag : brokenTags)
                {
                    List<NodeRef> taggetNodes = taggingService.findTaggedNodes(storeRef, tag);
                    
                    String newTag = findName(storeRef, bad, tag);
                    
                    if (logger.isDebugEnabled())
                    {
                        logger.debug("Broken tag in " + storeRef + " is " + tag + ", rename to " + newTag);
                    }
                    
                    taggingService.changeTag(storeRef, tag, newTag);
                    
                    for (NodeRef nodeRef : taggetNodes)
                    {
                        tagScopesToRefresh.addAll(taggingService.findAllTagScopes(nodeRef));
                    }
                }
            }
        }
        
        if (logger.isDebugEnabled())
        {
            logger.debug("Scopes to refresh : " + tagScopesToRefresh.size());
        }
        
        for (TagScope tagScope : tagScopesToRefresh)
        {
            taggingService.refreshTagScope(tagScope.getNodeRef(), false);
        }
        
        return I18NUtil.getMessage(SUCCESS_MESSAGE);
    }
    
    private String findName(StoreRef storeRef, String badSequence, String tag)
    {
        String newTag = tag.replace(badSequence, REPLACER);
        
        if (taggingService.getTagNodeRef(storeRef, newTag) != null)
        {
            return findName(storeRef, badSequence, tag.replace(badSequence, badSequence + REPLACER));
        }
        
        return newTag;
    }

    public void setTaggingService(TaggingService taggingService) 
    {
        this.taggingService = taggingService;
    }
}
