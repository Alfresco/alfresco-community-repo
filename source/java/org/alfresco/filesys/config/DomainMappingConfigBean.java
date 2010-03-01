/*
 * Copyright (C) 2005-2010 Alfresco Software Limited.
 *
 * This file is part of Alfresco
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
 */
package org.alfresco.filesys.config;

import org.springframework.beans.factory.BeanNameAware;

// TODO: Auto-generated Javadoc
/**
 * The Class DomainMappingConfigBean.
 * 
 * @author dward
 */
public class DomainMappingConfigBean implements BeanNameAware
{

    /** The name. */
    private String name;

    /** The subnet. */
    private String subnet;

    /** The mask. */
    private String mask;

    /** The range from. */
    private String rangeFrom;

    /** The range to. */
    private String rangeTo;

    /**
     * Gets the name.
     * 
     * @return the name
     */
    public String getName()
    {
        return name;
    }

    /* (non-Javadoc)
     * @see org.springframework.beans.factory.BeanNameAware#setBeanName(java.lang.String)
     */
    public void setBeanName(String name)
    {
        this.name = name;
    }

    /**
     * Gets the subnet.
     * 
     * @return the subnet
     */
    public String getSubnet()
    {
        return subnet;
    }

    /**
     * Sets the subnet.
     * 
     * @param subnet
     *            the new subnet
     */
    public void setSubnet(String subnet)
    {
        this.subnet = subnet;
    }

    /**
     * Gets the mask.
     * 
     * @return the mask
     */
    public String getMask()
    {
        return mask;
    }

    /**
     * Sets the mask.
     * 
     * @param mask
     *            the new mask
     */
    public void setMask(String mask)
    {
        this.mask = mask;
    }

    /**
     * Gets the range from.
     * 
     * @return the range from
     */
    public String getRangeFrom()
    {
        return rangeFrom;
    }

    /**
     * Sets the range from.
     * 
     * @param rangeFrom
     *            the new range from
     */
    public void setRangeFrom(String rangeFrom)
    {
        this.rangeFrom = rangeFrom;
    }

    /**
     * Gets the range to.
     * 
     * @return the range to
     */
    public String getRangeTo()
    {
        return rangeTo;
    }

    /**
     * Sets the range to.
     * 
     * @param rangeTo
     *            the new range to
     */
    public void setRangeTo(String rangeTo)
    {
        this.rangeTo = rangeTo;
    }
}
