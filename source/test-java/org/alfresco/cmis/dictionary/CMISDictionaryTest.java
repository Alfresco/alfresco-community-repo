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
package org.alfresco.cmis.dictionary;

import org.alfresco.cmis.CMISPropertyDefinition;
import org.alfresco.cmis.CMISTypeDefinition;
import org.alfresco.cmis.mapping.BaseCMISTest;
import org.alfresco.test_category.OwnJVMTestsCategory;
import org.junit.experimental.categories.Category;

@Category(OwnJVMTestsCategory.class)
public class CMISDictionaryTest extends BaseCMISTest
{
    public void testAllTypes()
    {
        for (CMISTypeDefinition type : cmisDictionaryService.getAllTypes())
        {
            System.out.println(type);
        }
    }
    
    public void testBaseTypes()
    {
        for (CMISTypeDefinition type : cmisDictionaryService.getBaseTypes())
        {
            System.out.println(type);
        }
    }
    public void testSubTypes()
    {
        for (CMISTypeDefinition type : cmisDictionaryService.getAllTypes())
        {
            System.out.println(type.getTypeId() + " children:");
            for (CMISTypeDefinition subType : type.getSubTypes(false))
            {
                System.out.println(" " + subType.getTypeId());
            }
            System.out.println(type.getTypeId() + " descendants:");
            for (CMISTypeDefinition subType : type.getSubTypes(true))
            {
                System.out.println(" " + subType.getTypeId());
            }
        }
    }

    public void testTypeIds()
    {
        for (CMISTypeDefinition typeDef : cmisDictionaryService.getAllTypes())
        {
            CMISTypeDefinition typeDefLookup = cmisDictionaryService.findType(typeDef.getTypeId());
            assertNotNull(typeDefLookup);
            assertEquals(typeDef, typeDefLookup);
        }
    }

    public void testBasicPropertyDefinitions()
    {
        for (CMISTypeDefinition type : cmisDictionaryService.getAllTypes())
        {
            System.out.println(type.getTypeId() + " properties:");
            for (CMISPropertyDefinition proDef : type.getPropertyDefinitions().values())
            {
                System.out.println(" " + proDef);
            }
        }
    }
}
