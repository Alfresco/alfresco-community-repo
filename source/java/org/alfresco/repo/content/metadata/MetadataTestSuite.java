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
package org.alfresco.repo.content.metadata;

import org.alfresco.util.ApplicationContextHelper;
import org.springframework.context.ApplicationContext;

import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * Metadata extractor test suite
 * 
 * @author Nick Burch
 */
public class MetadataTestSuite extends TestSuite 
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
       suite.addTestSuite( HtmlMetadataExtracterTest.class );
       suite.addTestSuite( MailMetadataExtracterTest.class );
       suite.addTestSuite( MP3MetadataExtracterTest.class );
       suite.addTestSuite( OfficeMetadataExtracterTest.class );
       suite.addTestSuite( OpenDocumentMetadataExtracterTest.class );
       suite.addTestSuite( OpenOfficeMetadataExtracterTest.class );
       suite.addTestSuite( PdfBoxMetadataExtracterTest.class );
       suite.addTestSuite( RFC822MetadataExtracterTest.class );
       
       return suite;
   }
}
