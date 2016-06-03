package org.alfresco.repo.domain.node;

import org.alfresco.service.cmr.repository.ContentData;

/**
 * <code>ContentData</code>-derived class with ID.
 * 
 * @author Derek Hulley
 * @since 3.4
 */
public class ContentDataWithId extends ContentData
{
    private static final long serialVersionUID = -5305648398812370806L;

    private final Long id;

    public ContentDataWithId(ContentData contentData, Long id)
    {
        super(contentData);
        this.id = id;
    }

    @Override
    public String toString()
    {
        return getInfoUrl() + "|id=" + (id == null ? "" : id.toString());
    }

    public Long getId()
    {
        return id;
    }
}
