package org.alfresco.repo.domain.node;

import java.io.Serializable;

/**
 * Data type carrying the ID of a <code>ContentData</code> reference.
 * 
 * @author Derek Hulley
 * @since 3.2.1
 */
public class ContentDataId implements Serializable
{
    private static final long serialVersionUID = -4980820192507809266L;

    private final Long id;

    public ContentDataId(Long id)
    {
        super();
        this.id = id;
    }

    @Override
    public String toString()
    {
        return "ContentDataId [id=" + id + "]";
    }

    public Long getId()
    {
        return id;
    }
}
