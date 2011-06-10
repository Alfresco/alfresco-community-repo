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
package org.alfresco.repo.content;

import org.alfresco.repo.content.metadata.DWGMetadataExtracterTest;
import org.alfresco.repo.content.metadata.HtmlMetadataExtracterTest;
import org.alfresco.repo.content.metadata.MP3MetadataExtracterTest;
import org.alfresco.repo.content.metadata.MailMetadataExtracterTest;
import org.alfresco.repo.content.metadata.OfficeMetadataExtracterTest;
import org.alfresco.repo.content.metadata.OpenDocumentMetadataExtracterTest;
import org.alfresco.repo.content.metadata.OpenOfficeMetadataExtracterTest;
import org.alfresco.repo.content.metadata.PdfBoxMetadataExtracterTest;
import org.alfresco.repo.content.metadata.PoiMetadataExtracterTest;
import org.alfresco.repo.content.metadata.RFC822MetadataExtracterTest;
import org.alfresco.repo.content.metadata.TikaAutoMetadataExtracterTest;
import org.alfresco.repo.content.transform.AppleIWorksContentTransformerTest;
import org.alfresco.repo.content.transform.BinaryPassThroughContentTransformerTest;
import org.alfresco.repo.content.transform.ComplexContentTransformerTest;
import org.alfresco.repo.content.transform.ContentTransformerRegistryTest;
import org.alfresco.repo.content.transform.HtmlParserContentTransformerTest;
import org.alfresco.repo.content.transform.MailContentTransformerTest;
import org.alfresco.repo.content.transform.MediaWikiContentTransformerTest;
import org.alfresco.repo.content.transform.OpenOfficeContentTransformerTest;
import org.alfresco.repo.content.transform.PdfBoxContentTransformerTest;
import org.alfresco.repo.content.transform.PoiContentTransformerTest;
import org.alfresco.repo.content.transform.PoiHssfContentTransformerTest;
import org.alfresco.repo.content.transform.PoiOOXMLContentTransformerTest;
import org.alfresco.repo.content.transform.RuntimeExecutableContentTransformerTest;
import org.alfresco.repo.content.transform.StringExtractingContentTransformerTest;
import org.alfresco.repo.content.transform.TextMiningContentTransformerTest;
import org.alfresco.repo.content.transform.TextToPdfContentTransformerTest;
import org.alfresco.repo.content.transform.TikaAutoContentTransformerTest;
import org.alfresco.repo.content.transform.magick.ImageMagickContentTransformerTest;
import org.alfresco.util.ApplicationContextHelper;
import org.springframework.context.ApplicationContext;

import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * Metadata Extractor and Transform test suite
 * 
 * @author Nick Burch
 */
public class ContentMinimalContextTestSuite extends TestSuite 
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
       
       // Metadata tests
       suite.addTestSuite( DWGMetadataExtracterTest.class );
       suite.addTestSuite( HtmlMetadataExtracterTest.class );
       suite.addTestSuite( MailMetadataExtracterTest.class );
       suite.addTestSuite( MP3MetadataExtracterTest.class );
       suite.addTestSuite( OfficeMetadataExtracterTest.class );
       suite.addTestSuite( OpenDocumentMetadataExtracterTest.class );
       suite.addTestSuite( OpenOfficeMetadataExtracterTest.class );
       suite.addTestSuite( PdfBoxMetadataExtracterTest.class );
       suite.addTestSuite( PoiMetadataExtracterTest.class );
       suite.addTestSuite( RFC822MetadataExtracterTest.class );
       suite.addTestSuite( TikaAutoMetadataExtracterTest.class );
       
       // Transform tests
       suite.addTestSuite(BinaryPassThroughContentTransformerTest.class);
       suite.addTestSuite(ComplexContentTransformerTest.class);
       suite.addTestSuite(ContentTransformerRegistryTest.class);
       suite.addTestSuite(HtmlParserContentTransformerTest.class);
       suite.addTestSuite(MailContentTransformerTest.class);
       suite.addTestSuite(MediaWikiContentTransformerTest.class);
       suite.addTestSuite(OpenOfficeContentTransformerTest.class);
       suite.addTestSuite(PdfBoxContentTransformerTest.class);
       suite.addTestSuite(PoiContentTransformerTest.class);
       suite.addTestSuite(PoiHssfContentTransformerTest.class);
       suite.addTestSuite(PoiOOXMLContentTransformerTest.class);
       suite.addTestSuite(RuntimeExecutableContentTransformerTest.class);
       suite.addTestSuite(StringExtractingContentTransformerTest.class);
       suite.addTestSuite(TextMiningContentTransformerTest.class);
       suite.addTestSuite(TextToPdfContentTransformerTest.class);
       suite.addTestSuite(TikaAutoContentTransformerTest.class);
       suite.addTestSuite(ImageMagickContentTransformerTest.class);
       suite.addTestSuite(AppleIWorksContentTransformerTest.class);
       
       return suite;
   }
}
