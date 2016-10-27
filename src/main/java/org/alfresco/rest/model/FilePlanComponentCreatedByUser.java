/*
 * #%L
 * Alfresco Records Management Module
 * %%
 * Copyright (C) 2005 - 2016 Alfresco Software Limited
 * %%
 * License rights for this program may be obtained from Alfresco Software, Ltd.
 * pursuant to a written agreement and any use of this program without such an
 * agreement is prohibited.
 * #L%
 */
package org.alfresco.rest.model;

/**
 * POJO for file plan component created by object
 * @author Kristijan Conkas
 * @since 1.0
 */
public class FilePlanComponentCreatedByUser
{
    private String id;
    private String displayName;
    /**
     * @return the id
     */
    public String getId()
    {
        return this.id;
    }
    /**
     * @param id the id to set
     */
    public void setId(String id)
    {
        this.id = id;
    }
    /**
     * @return the displayName
     */
    public String getDisplayName()
    {
        return this.displayName;
    }
    /**
     * @param displayName the displayName to set
     */
    public void setDisplayName(String displayName)
    {
        this.displayName = displayName;
    }
}
