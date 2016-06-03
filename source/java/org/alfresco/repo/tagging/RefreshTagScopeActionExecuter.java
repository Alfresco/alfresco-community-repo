package org.alfresco.repo.tagging;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.action.executer.ActionExecuterAbstractBase;
import org.alfresco.repo.content.MimetypeMap;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.service.cmr.action.Action;
import org.alfresco.service.cmr.action.ParameterDefinition;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.ContentService;
import org.alfresco.service.cmr.repository.ContentWriter;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.tagging.TagDetails;
import org.alfresco.service.cmr.tagging.TaggingService;

/**
 * Refresh tag scope action executer
 * 
 * NOTE:  This action is used to facilitate the async refresh of a tag scope.  It is not intended for general usage.
 * 
 * @author Roy Wetherall
 */
public class RefreshTagScopeActionExecuter extends ActionExecuterAbstractBase
{
    /** Node Service */
    private NodeService nodeService;
    
    /** Content Service */
    private ContentService contentService;
    
    /** Tagging Service */
    private TaggingService taggingService;
    
    /** Action name and parameters */
    public static final String NAME = "refresh-tagscope";
    
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
        if (this.nodeService.exists(actionedUponNodeRef) == true &&
            this.nodeService.hasAspect(actionedUponNodeRef, ContentModel.ASPECT_TAGSCOPE) == true)
        {
            // Run the update as the system user
            AuthenticationUtil.runAs(new AuthenticationUtil.RunAsWork<Object>()
            {
                @SuppressWarnings("unchecked")
                public Object doWork() throws Exception
                {
                    // Create a new list of tag details
                    List<TagDetails> tags = new ArrayList<TagDetails>(10);
                    
                    // Count the tags found in all the (primary) children of the node
                    countTags(actionedUponNodeRef, tags);
                    
                    // Order the list
                    Collections.sort(tags);
                    
                    // Write new content back to tag scope
                    String tagContent = TaggingServiceImpl.tagDetailsToString(tags);
                    if(tagContent.length() > 0)
                    {
                    	// Write out tag content only if non-zero in size
	                    ContentWriter contentWriter = contentService.getWriter(actionedUponNodeRef, ContentModel.PROP_TAGSCOPE_CACHE, true);
	                    contentWriter.setEncoding("UTF-8");
	                    contentWriter.setMimetype(MimetypeMap.MIMETYPE_TEXT_PLAIN);
	                    contentWriter.putContent(tagContent);
                    }

                    return null;
                }
                
            }, AuthenticationUtil.getSystemUserName());                      
        }
    }

    private void countTags(NodeRef nodeRef, List<TagDetails> tagDetailsList)
    {
        // Add the tags of passed node
        List<String> tags = this.taggingService.getTags(nodeRef);
        for (String tag : tags)
        {
            addDetails(tag, tagDetailsList);
        }
        
        // Iterate over the children of the node
        List<ChildAssociationRef> assocs = this.nodeService.getChildAssocs(nodeRef);
        for (ChildAssociationRef assoc : assocs)
        {
            if (assoc.isPrimary() == true)
            {
                countTags(assoc.getChildRef(), tagDetailsList);
            }
        }
    }
    
    private void addDetails(String tag, List<TagDetails> tagDetailsList)
    {
        TagDetails currentTag = null;
        for (TagDetails tagDetails : tagDetailsList)
        {
            if (tagDetails.getName().equals(tag) == true)
            {
                currentTag = tagDetails;
                break;
            }
        }
        
        if (currentTag == null)
        {
            tagDetailsList.add(new TagDetailsImpl(tag, 1));
        }
        else
        {
            ((TagDetailsImpl)currentTag).incrementCount();
        }
    }

    /**
     * @see org.alfresco.repo.action.ParameterizedItemAbstractBase#addParameterDefinitions(java.util.List)
     */
    @Override
    protected void addParameterDefinitions(List<ParameterDefinition> paramList)
    {       
    }

}
