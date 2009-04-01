/*
 * Copyright (C) 2005-2009 Alfresco Software Limited.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.

 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.

 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.

 * As a special exception to the terms and conditions of version 2.0 of 
 * the GPL, you may redistribute this Program in connection with Free/Libre 
 * and Open Source Software ("FLOSS") applications as described in Alfresco's 
 * FLOSS exception.  You should have received a copy of the text describing 
 * the FLOSS exception, and it is also available here: 
 * http://www.alfresco.com/legal/licensing"
 */
package org.alfresco.filesys.config;

// TODO: Auto-generated Javadoc
/**
 * The Class DomainMappingConfigBean.
 * 
 * @author dward
 */
public class DomainMappingConfigBean
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

    /**
     * Sets the name.
     * 
     * @param name
     *            the new name
     */
    public void setName(String name)
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
