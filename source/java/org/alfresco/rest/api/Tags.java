package org.alfresco.rest.api;

import java.util.List;

import org.alfresco.rest.api.model.Tag;
import org.alfresco.rest.framework.resource.parameters.CollectionWithPagingInfo;
import org.alfresco.rest.framework.resource.parameters.Paging;
import org.alfresco.rest.framework.resource.parameters.Parameters;
import org.alfresco.service.cmr.repository.StoreRef;

public interface Tags
{
    public List<Tag> addTags(String nodeId, List<Tag> tags);
    public Tag getTag(StoreRef storeRef, String tagId);
    public void deleteTag(String nodeId, String tagId);
    public CollectionWithPagingInfo<Tag> getTags(StoreRef storeRef, Paging paging);
    public Tag changeTag(StoreRef storeRef, String tagId, Tag tag);
    public CollectionWithPagingInfo<Tag> getTags(String nodeId, Parameters params);
}
