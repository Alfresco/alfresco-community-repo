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
package org.alfresco.rest.rm.model.fileplancomponents;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * POJO for FilePlanComponent path parameter
 * <br>
 * @author Kristijan Conkas
 * @since 2.6
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class FilePlanComponentPath
{
    private String name;
    private boolean isComplete;
    private List<FilePlanComponentIdNamePair> elements;

    /**
     * @return the name
     */
    public String getName()
    {
        return this.name;
    }

    /**
     * @param name the name to set
     */
    public void setName(String name)
    {
        this.name = name;
    }

    /**
     * @return the isComplete
     */
    public boolean isComplete()
    {
        return this.isComplete;
    }

    /**
     * @param isComplete the isComplete to set
     */
    public void setComplete(boolean isComplete)
    {
        this.isComplete = isComplete;
    }

    /**
     * @return the elements
     */
    public List<FilePlanComponentIdNamePair> getElements()
    {
        return this.elements;
    }

    /**
     * @param elements the elements to set
     */
    public void setElements(List<FilePlanComponentIdNamePair> elements)
    {
        this.elements = elements;
    }
}
