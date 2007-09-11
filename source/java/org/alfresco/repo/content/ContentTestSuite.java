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
package org.alfresco.repo.content;

import org.alfresco.repo.content.cleanup.ContentStoreCleanerTest;
import org.alfresco.repo.content.encoding.CharsetFinderTest;
import org.alfresco.repo.content.filestore.FileContentStoreTest;
import org.alfresco.repo.content.filestore.NoRandomAccessFileContentStoreTest;
import org.alfresco.repo.content.filestore.ReadOnlyFileContentStoreTest;
import org.alfresco.repo.content.metadata.HtmlMetadataExtracterTest;
import org.alfresco.repo.content.metadata.MappingMetadataExtracterTest;
import org.alfresco.repo.content.metadata.OfficeMetadataExtracterTest;
import org.alfresco.repo.content.metadata.OpenDocumentMetadataExtracterTest;
import org.alfresco.repo.content.metadata.OpenOfficeMetadataExtracterTest;
import org.alfresco.repo.content.metadata.PdfBoxMetadataExtracterTest;
import org.alfresco.repo.content.replication.ContentStoreReplicatorTest;
import org.alfresco.repo.content.replication.ReplicatingContentStoreTest;
import org.alfresco.repo.content.transform.BinaryPassThroughContentTransformerTest;
import org.alfresco.repo.content.transform.ComplexContentTransformerTest;
import org.alfresco.repo.content.transform.ContentTransformerRegistryTest;
import org.alfresco.repo.content.transform.HtmlParserContentTransformerTest;
import org.alfresco.repo.content.transform.OpenOfficeContentTransformerTest;
import org.alfresco.repo.content.transform.MailContentTransformerTest;
import org.alfresco.repo.content.transform.PdfBoxContentTransformerTest;
import org.alfresco.repo.content.transform.PoiHssfContentTransformerTest;
import org.alfresco.repo.content.transform.RuntimeExecutableContentTransformerTest;
import org.alfresco.repo.content.transform.StringExtractingContentTransformerTest;
import org.alfresco.repo.content.transform.TextMiningContentTransformerTest;
import org.alfresco.repo.content.transform.TextToPdfContentTransformerTest;

// TODO:  This class is currently missing
// import org.alfresco.repo.content.transform.TextToPdfContentTransformerTest;

import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * Suite for content-related tests.
 * 
 * @author Derek Hulley
 */
public class ContentTestSuite extends TestSuite
{
    public static Test suite() 
    {
        TestSuite suite = new TestSuite();
        
        suite.addTestSuite(ContentStoreCleanerTest.class);
        suite.addTestSuite(CharsetFinderTest.class);
        suite.addTestSuite(FileContentStoreTest.class);
        suite.addTestSuite(NoRandomAccessFileContentStoreTest.class);
        suite.addTestSuite(ReadOnlyFileContentStoreTest.class);
        suite.addTestSuite(MappingMetadataExtracterTest.class);
        suite.addTestSuite(HtmlMetadataExtracterTest.class);
        suite.addTestSuite(OfficeMetadataExtracterTest.class);
        suite.addTestSuite(OpenDocumentMetadataExtracterTest.class);
        suite.addTestSuite(OpenOfficeMetadataExtracterTest.class);
        suite.addTestSuite(PdfBoxMetadataExtracterTest.class);
        suite.addTestSuite(ContentStoreReplicatorTest.class);
        suite.addTestSuite(ReplicatingContentStoreTest.class);
        suite.addTestSuite(BinaryPassThroughContentTransformerTest.class);
        suite.addTestSuite(ComplexContentTransformerTest.class);
        suite.addTestSuite(ContentTransformerRegistryTest.class);
        suite.addTestSuite(HtmlParserContentTransformerTest.class);
        suite.addTestSuite(OpenOfficeContentTransformerTest.class);
        suite.addTestSuite(PdfBoxContentTransformerTest.class);
        suite.addTestSuite(PoiHssfContentTransformerTest.class);
        suite.addTestSuite(RuntimeExecutableContentTransformerTest.class);
        suite.addTestSuite(StringExtractingContentTransformerTest.class);
        suite.addTestSuite(TextMiningContentTransformerTest.class);
        suite.addTestSuite(TextToPdfContentTransformerTest.class);
        suite.addTestSuite(MailContentTransformerTest.class);
        suite.addTestSuite(ContentDataTest.class);
        suite.addTestSuite(MimetypeMapTest.class);
        suite.addTestSuite(RoutingContentServiceTest.class);
        suite.addTestSuite(RoutingContentStoreTest.class);
                
        return suite;
    }
}
