/*
 * Copyright (C) 2005 Alfresco, Inc.
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
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package org.alfresco.service.cmr.repository;

import java.util.Locale;

import junit.framework.TestCase;

import org.alfresco.i18n.I18NUtil;
import org.alfresco.service.cmr.repository.datatype.DefaultTypeConverter;

/**
 * @see org.alfresco.service.cmr.repository.ContentData
 * 
 * @author Derek Hulley
 */
public class ContentDataTest extends TestCase
{

    public ContentDataTest(String name)
    {
        super(name);
    }

    public void testToAndFromString() throws Exception
    {
        Locale locale = I18NUtil.getLocale();
        String localeStr = DefaultTypeConverter.INSTANCE.convert(String.class, locale);
        ContentData property = new ContentData(null, null, 0L, null, null);
        
        // check null string
        String propertyStr = property.toString();
        assertEquals("Null values not converted correctly",
                "contentUrl=|mimetype=|size=0|encoding=|locale=" + localeStr,
                propertyStr);
        
        // convert back
        ContentData checkProperty = ContentData.createContentProperty(propertyStr);
        assertEquals("Conversion from string failed", property, checkProperty);
        
        property = new ContentData("uuu", "mmm", 123L, "eee", I18NUtil.getLocale());

        // convert to a string
        propertyStr = property.toString();
        assertEquals("Incorrect property string representation",
                "contentUrl=uuu|mimetype=mmm|size=123|encoding=eee|locale=" + localeStr, propertyStr);
        
        // convert back
        checkProperty = ContentData.createContentProperty(propertyStr);
        assertEquals("Conversion from string failed", property, checkProperty);
    }
}
