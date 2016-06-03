package org.alfresco.repo.content;

import java.util.Locale;

import junit.framework.TestCase;

import org.springframework.extensions.surf.util.I18NUtil;
import org.alfresco.service.cmr.repository.ContentData;
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
        
        property = new ContentData("test://uuu", "mmm", 123L, "eee", I18NUtil.getLocale());

        // convert to a string
        propertyStr = property.toString();
        assertEquals("Incorrect property string representation",
                "contentUrl=test://uuu|mimetype=mmm|size=123|encoding=eee|locale=" + localeStr, propertyStr);
        
        // convert back
        checkProperty = ContentData.createContentProperty(propertyStr);
        assertEquals("Conversion from string failed", property, checkProperty);
    }
    
    public void testEquals()
    {
        ContentData contentData1 = new ContentData("abc://xxx", MimetypeMap.MIMETYPE_BINARY, 600L, "UTF-8", Locale.ENGLISH);
        ContentData contentData2 = new ContentData("abc://xxx", MimetypeMap.MIMETYPE_BINARY, 600L, "UTF-8", Locale.ENGLISH);
        ContentData contentData3 = new ContentData("abc://XXX", MimetypeMap.MIMETYPE_BINARY, 600L, "UTF-8", Locale.ENGLISH);
        ContentData contentData4 = new ContentData("abc://xxx", MimetypeMap.MIMETYPE_TEXT_PLAIN, 600L, "UTF-8", Locale.ENGLISH);
        ContentData contentData5 = new ContentData("abc://xxx", MimetypeMap.MIMETYPE_BINARY, 500L, "UTF-8", Locale.ENGLISH);
        ContentData contentData6 = new ContentData("abc://xxx", MimetypeMap.MIMETYPE_BINARY, 600L, "UTF-16", Locale.ENGLISH);
        ContentData contentData7 = new ContentData("abc://xxx", MimetypeMap.MIMETYPE_BINARY, 600L, "UTF-8", Locale.CHINESE);
        assertEquals(contentData1, contentData2);
        assertNotSame(contentData1, contentData3);
        assertNotSame(contentData1, contentData4);
        assertNotSame(contentData1, contentData5);
        assertNotSame(contentData1, contentData6);
        assertNotSame(contentData1, contentData7);
    }
}
