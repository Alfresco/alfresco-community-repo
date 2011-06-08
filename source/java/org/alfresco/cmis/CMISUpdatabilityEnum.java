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
package org.alfresco.cmis;

import org.alfresco.opencmis.EnumFactory;
import org.alfresco.opencmis.EnumLabel;

/**
 * CMIS Updatability Enum
 * 
 * @author davidc
 */
public enum CMISUpdatabilityEnum implements EnumLabel
{
    READ_ONLY("readonly"),
    READ_AND_WRITE("readwrite"),
    READ_AND_WRITE_WHEN_CHECKED_OUT("whencheckedout"),
    ON_CREATE("oncreate");

    
    private String label;
    
    /**
     * Construct
     * 
     * @param label
     */
    CMISUpdatabilityEnum(String label)
    {
        this.label = label;
    }

    /* (non-Javadoc)
     * @see org.alfresco.cmis.EnumLabel#label()
     */
    public String getLabel()
    {
        return label;
    }

    public static EnumFactory<CMISUpdatabilityEnum> FACTORY = new EnumFactory<CMISUpdatabilityEnum>(CMISUpdatabilityEnum.class, null, true); 
}
