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
package org.alfresco.filesys.config;

// TODO: Auto-generated Javadoc
/**
 * The Class MemoryPacketConfigBean.
 * 
 * @author dward
 */
public class MemoryPacketConfigBean
{

    /** The size. */
    private Long size;

    /** The init. */
    private Integer init;

    /** The max. */
    private Integer max;

    /**
     * Gets the size.
     * 
     * @return the size
     */
    public Long getSize()
    {
        return size;
    }

    /**
     * Sets the size.
     * 
     * @param size
     *            the new size
     */
    public void setSize(Long size)
    {
        this.size = size;
    }

    /**
     * Gets the inits the.
     * 
     * @return the inits the
     */
    public Integer getInit()
    {
        return init;
    }

    /**
     * Sets the inits the.
     * 
     * @param init
     *            the new inits the
     */
    public void setInit(Integer init)
    {
        this.init = init;
    }

    /**
     * Gets the max.
     * 
     * @return the max
     */
    public Integer getMax()
    {
        return max;
    }

    /**
     * Sets the max.
     * 
     * @param max
     *            the new max
     */
    public void setMax(Integer max)
    {
        this.max = max;
    }

}
