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
package org.alfresco.repo.content.transform;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.alfresco.repo.content.transform.magick.ImageMagickContentTransformerTest;
import org.alfresco.util.ApplicationContextHelper;
import org.springframework.context.ApplicationContext;


/**
 * Content Transformation test suite
 * 
 * @author Roy Wetherall
 */
public class TransformTestSuite extends TestSuite
{
    /**
     * Asks {@link ApplicationContextHelper} to give us a 
     *  suitable, perhaps cached context for use in our tests
     */
    public static ApplicationContext getContext() {
       ApplicationContextHelper.setUseLazyLoading(false);
       ApplicationContextHelper.setNoAutoStart(true);
       return ApplicationContextHelper.getApplicationContext(
            new String[] { "classpath:alfresco/minimal-context.xml" }
       );
    }
    
    /**
     * Creates the test suite
     * 
     * @return  the test suite
     */
    public static Test suite() 
    {
        // Setup the context
        getContext();
       
        // Off we go
        TestSuite suite = new TestSuite();
        suite.addTestSuite(BinaryPassThroughContentTransformerTest.class);
        suite.addTestSuite(ComplexContentTransformerTest.class);
        suite.addTestSuite(ContentTransformerRegistryTest.class);
        suite.addTestSuite(HtmlParserContentTransformerTest.class);
        suite.addTestSuite(MailContentTransformerTest.class);
        suite.addTestSuite(MediaWikiContentTransformerTest.class);
        suite.addTestSuite(OpenOfficeContentTransformerTest.class);
        suite.addTestSuite(PdfBoxContentTransformerTest.class);
        suite.addTestSuite(PoiHssfContentTransformerTest.class);
        suite.addTestSuite(RuntimeExecutableContentTransformerTest.class);
        suite.addTestSuite(StringExtractingContentTransformerTest.class);
        suite.addTestSuite(TextMiningContentTransformerTest.class);
        suite.addTestSuite(TextToPdfContentTransformerTest.class);
        suite.addTestSuite(ImageMagickContentTransformerTest.class);
        return suite;
    }
}
