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
