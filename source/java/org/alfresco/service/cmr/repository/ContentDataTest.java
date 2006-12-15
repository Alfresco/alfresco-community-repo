/*
 * Copyright (C) 2005 Alfresco, Inc.
 *
 * Licensed under the Mozilla Public License version 1.1 
 * with a permitted attribution clause. You may obtain a
 * copy of the License at
 *
 *   http://www.alfresco.org/legal/license.txt
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific
 * language governing permissions and limitations under the
 * License.
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
