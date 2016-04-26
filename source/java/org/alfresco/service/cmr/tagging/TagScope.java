package org.alfresco.service.cmr.tagging;

import java.util.List;

import org.alfresco.service.cmr.repository.NodeRef;

/**
 * Tag Scope Inteface.
 * 
 * Represents the roll up of tags within the scope of a node tree.
 * 
 * @author Roy Wetherall
 */
public interface TagScope
{
    NodeRef getNodeRef();
    
    List<TagDetails> getTags();
    
    List<TagDetails> getTags(int topN);
    
    TagDetails getTag(String tag);
    
    boolean isTagInScope(String tag);
}
