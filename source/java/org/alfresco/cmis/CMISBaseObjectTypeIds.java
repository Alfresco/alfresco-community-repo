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
 * Enumeration of base Alfresco Object type Ids. Possible values:<br />
 * <b>DOCUMENT</b>,<br />
 * <b>FOLDER</b>,<br />
 * <b>RELATIONSHIP</b>,<br />
 * <b>POLICY</b>
 * 
 * @author Dmitry Velichkevich
 */
public enum CMISBaseObjectTypeIds implements EnumLabel
{
    DOCUMENT("cmis:document"), FOLDER("cmis:folder"), RELATIONSHIP("cmis:relationship"), POLICY("cmis:policy");

    private String label;

    CMISBaseObjectTypeIds(String label)
    {
        this.label = label;
    }

    public String getLabel()
    {
        return label;
    }

    public static final EnumFactory<CMISBaseObjectTypeIds> FACTORY = new EnumFactory<CMISBaseObjectTypeIds>(CMISBaseObjectTypeIds.class);
}
