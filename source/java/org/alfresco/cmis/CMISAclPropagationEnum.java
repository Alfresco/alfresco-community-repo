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
 * CMIS ACL propagation
 * 
 * Used to request a particular behaviour or report back behaviour.
 * 
 * @author andyh
 *
 */
public enum CMISAclPropagationEnum  implements EnumLabel
{
    /**
     * The ACL only applies to the object
     * (not yet supported in Alfresco)
     */
    OBJECT_ONLY("objectonly"),
    /**
     * ACLs are applied to all inheriting objects
     * (the default in Alfresco)
     */
    PROPAGATE("propagate"),
    /**
     * Some other mechanism by which ACL changes influence other ACL's non-direct ACEs.
     */
    REPOSITORY_DETERMINED("repositorydetermined");
    
    private String label;

    /**
     * Construct
     * 
     * @param label
     */
    CMISAclPropagationEnum(String label)
    {
        this.label = label;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.alfresco.cmis.EnumLabel#label()
     */
    public String getLabel()
    {
        return label;
    }

    /**
     * Factory for CMISAclPropagationEnum
     */
    public static EnumFactory<CMISAclPropagationEnum> FACTORY = new EnumFactory<CMISAclPropagationEnum>(CMISAclPropagationEnum.class, PROPAGATE, true);

}
