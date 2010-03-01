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
