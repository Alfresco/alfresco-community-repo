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
package org.alfresco.repo.tagging.script;

/**
 * Stores  total tags count together with tags to be sent to UI
 * 
 * @author Viachaslau Tsikhanovich
 *
 */
public class PagedTagsWrapper
{

    private String[] tagNames;
    
    private String total;

    public PagedTagsWrapper(String[] tagNames, int total)
    {
        super();
        this.setTagNames(tagNames);
        this.setTotal(total);
    }

    public String[] getTagNames()
    {
        return tagNames;
    }

    public void setTagNames(String[] tagNames)
    {
        this.tagNames = tagNames;
    }

    public String getTotal()
    {
        return total;
    }

    public void setTotal(int total)
    {
        this.total = String.valueOf(total);
    }

}
