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
package org.alfresco.repo.admin.patch.impl;

import java.io.InputStream;
import java.util.List;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.admin.patch.AbstractPatch;
import org.alfresco.repo.content.MimetypeMap;
import org.alfresco.repo.model.Repository;
import org.alfresco.service.cmr.repository.ContentService;
import org.alfresco.service.cmr.repository.ContentWriter;
import org.alfresco.service.cmr.repository.NodeRef;
import org.springframework.extensions.surf.util.I18NUtil;

/**
 * MNT-13190: Fix template
 * 
 * @author Viachaslau Tsikhanovich
 *
 */
public class FixTemplatePatch extends AbstractPatch
{
    private static final String MSG_SUCCESS = "patch.fixWebscriptTemplate.result";
    private static final String MSG_SKIP = "patch.fixWebscriptTemplate.skip";
    
    private Repository repository;
    protected ContentService contentService;
    private String target;
    private String source;

    public void setRepository(Repository repository)
    {
        this.repository = repository;
    }
    
    public void setContentService(ContentService contentService)
    {
        this.contentService = contentService;
    }
    
    public void setTarget(String target)
    {
        this.target = target;
    }

    public void setSource(String source)
    {
        this.source = source;
    }


    @Override
    protected String applyInternal() throws Exception
    {
        List<NodeRef> refs = searchService.selectNodes(
                repository.getRootHome(), 
                target, 
                null, 
                namespaceService, 
                false);
        if (refs.size() < 1)
        {
            // skip as it can be deleted
            return I18NUtil.getMessage(MSG_SKIP);
        }
        else
        {
            updateContent(refs.get(0));
        }

        return I18NUtil.getMessage(MSG_SUCCESS);
    }

    private void updateContent(NodeRef nodeRef)
    {
        // Make versionable
        nodeService.addAspect(nodeRef, ContentModel.ASPECT_VERSIONABLE, null);
        
        // Update content
        InputStream is = this.getClass().getClassLoader().getResourceAsStream(source);
        if (is != null)
        {
            ContentWriter contentWriter = contentService.getWriter(nodeRef, ContentModel.PROP_CONTENT, true);
            contentWriter.setMimetype(MimetypeMap.MIMETYPE_TEXT_PLAIN);
            contentWriter.setEncoding("UTF-8");
            contentWriter.putContent(is);
        }
    }

}
