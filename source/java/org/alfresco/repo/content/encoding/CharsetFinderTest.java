/*
 * Copyright (C) 2005-2007 Alfresco Software Limited.
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
 * FLOSS exception.  You should have recieved a copy of the text describing 
 * the FLOSS exception, and it is also available here: 
 * http://www.alfresco.com/legal/licensing"
 */
package org.alfresco.repo.content.encoding;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.nio.charset.Charset;

import junit.framework.TestCase;

import org.alfresco.encoding.CharactersetFinder;
import org.alfresco.repo.content.MimetypeMap;
import org.alfresco.repo.content.transform.AbstractContentTransformerTest;
import org.alfresco.util.ApplicationContextHelper;
import org.springframework.context.ApplicationContext;

/**
 * @see CharsetFinderTest
 * @see CharactersetFinder
 * 
 * @author Derek Hulley
 */
public class CharsetFinderTest extends TestCase
{
    private static ApplicationContext ctx = ApplicationContextHelper.getApplicationContext();
    
    private ContentCharsetFinder charsetFinder;
    
    @Override
    public void setUp() throws Exception
    {
        charsetFinder = (ContentCharsetFinder) ctx.getBean("charset.finder");
    }
    
    public void testPlainText() throws Exception
    {
        File file = AbstractContentTransformerTest.loadQuickTestFile("txt");
        InputStream is = new BufferedInputStream(new FileInputStream(file));
        Charset charset = charsetFinder.getCharset(is, MimetypeMap.MIMETYPE_TEXT_PLAIN);
        assertNotNull(charset);
    }
}
