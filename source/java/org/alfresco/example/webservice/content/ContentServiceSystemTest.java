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
package org.alfresco.example.webservice.content;

import org.alfresco.example.webservice.BaseWebServiceSystemTest;
import org.alfresco.example.webservice.repository.UpdateResult;
import org.alfresco.example.webservice.types.CML;
import org.alfresco.example.webservice.types.CMLCreate;
import org.alfresco.example.webservice.types.ContentFormat;
import org.alfresco.example.webservice.types.NamedValue;
import org.alfresco.example.webservice.types.ParentReference;
import org.alfresco.example.webservice.types.Predicate;
import org.alfresco.example.webservice.types.Reference;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.content.MimetypeMap;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class ContentServiceSystemTest extends BaseWebServiceSystemTest
{
   private static final String CONTENT = "This is a small piece of content to test the create service call";
   private static final String UPDATED_CONTENT = "This is some updated content to test the write service call";
   
   private String fileName = "unit-test.txt";
   
   public void testContentService() 
       throws Exception
   {
       ParentReference parentRef = new ParentReference();
       parentRef.setStore(getStore());
       parentRef.setUuid(getRootNodeReference().getUuid());
       parentRef.setAssociationType(ContentModel.ASSOC_CHILDREN.toString());
       parentRef.setChildName(ContentModel.ASSOC_CHILDREN.toString());
       
       NamedValue[] properties = new NamedValue[]{new NamedValue(ContentModel.PROP_NAME.toString(), this.fileName)};
       CMLCreate create = new CMLCreate("1", parentRef, ContentModel.TYPE_CONTENT.toString(), properties);
       CML cml = new CML();
       cml.setCreate(new CMLCreate[]{create});
       UpdateResult[] result = this.repositoryService.update(cml);     
       
       Reference newContentNode = result[0].getDestination();       
       String property = ContentModel.PROP_CONTENT.toString();
       Predicate predicate = new Predicate(new Reference[]{newContentNode}, getStore(), null);
              
       // First check a node that has no content set
       Content[] contents1 = this.contentService.read(predicate, property);
       assertNotNull(contents1);
       assertEquals(1, contents1.length);
       Content content1 = contents1[0];
       assertNotNull(content1);
       assertEquals(0, content1.getLength());
       assertEquals(newContentNode.getUuid(), content1.getNode().getUuid());
       assertEquals(property, content1.getProperty());
       assertNull(content1.getUrl());
       assertNull(content1.getFormat());
       
       // Write content 
       Content content2 = this.contentService.write(newContentNode, property, CONTENT.getBytes(), new ContentFormat(MimetypeMap.MIMETYPE_TEXT_PLAIN, "UTF-8"));
       assertNotNull(content2);
       assertTrue((content2.getLength() > 0));
       assertEquals(newContentNode.getUuid(), content2.getNode().getUuid());
       assertEquals(property, content2.getProperty());
       assertNotNull(content2.getUrl());
       assertNotNull(content2.getFormat());       
       ContentFormat format2 = content2.getFormat();
       assertEquals(MimetypeMap.MIMETYPE_TEXT_PLAIN, format2.getMimetype());
       assertEquals("UTF-8", format2.getEncoding());
       assertEquals(CONTENT, getContentAsString(content2.getUrl()));
              
       // Read content
       Content[] contents3 = this.contentService.read(predicate, property);
       assertNotNull(contents3);
       assertEquals(1, contents3.length);
       Content content3 = contents3[0];
       assertNotNull(content3);
       assertTrue((content3.getLength() > 0));
       assertEquals(newContentNode.getUuid(), content3.getNode().getUuid());
       assertEquals(property, content3.getProperty());
       assertNotNull(content3.getUrl());
       assertNotNull(content3.getFormat());       
       ContentFormat format3 = content3.getFormat();
       assertEquals(MimetypeMap.MIMETYPE_TEXT_PLAIN, format3.getMimetype());
       assertEquals("UTF-8", format3.getEncoding());
       assertEquals(CONTENT, getContentAsString(content3.getUrl()));
       
       // Update content
       Content content4 = this.contentService.write(newContentNode, property, UPDATED_CONTENT.getBytes(), new ContentFormat(MimetypeMap.MIMETYPE_TEXT_CSS, "UTF-8"));
       assertNotNull(content4);
       assertTrue((content4.getLength() > 0));
       assertEquals(newContentNode.getUuid(), content4.getNode().getUuid());
       assertEquals(property, content4.getProperty());
       assertNotNull(content4.getUrl());
       assertNotNull(content4.getFormat());       
       ContentFormat format4 = content4.getFormat();
       assertEquals(MimetypeMap.MIMETYPE_TEXT_CSS, format4.getMimetype());
       assertEquals("UTF-8", format4.getEncoding());
       assertEquals(UPDATED_CONTENT, getContentAsString(content4.getUrl()));
       
       // Read updated content
       Content[] contents5 = this.contentService.read(predicate, property);
       assertNotNull(contents5);
       assertEquals(1, contents5.length);
       Content content5 = contents5[0];
       assertNotNull(content5);
       assertTrue((content5.getLength() > 0));
       assertEquals(newContentNode.getUuid(), content5.getNode().getUuid());
       assertEquals(property, content5.getProperty());
       assertNotNull(content5.getUrl());
       assertNotNull(content5.getFormat());       
       ContentFormat format5 = content5.getFormat();
       assertEquals(MimetypeMap.MIMETYPE_TEXT_CSS, format5.getMimetype());
       assertEquals("UTF-8", format5.getEncoding());
       assertEquals(UPDATED_CONTENT, getContentAsString(content5.getUrl()));
       
       // Clear content
       Content[] contents6 = this.contentService.clear(predicate, property);
       assertNotNull(contents6);
       assertEquals(1, contents6.length);
       Content content6 = contents6[0];
       assertNotNull(content6);
       assertEquals(0, content6.getLength());
       assertEquals(newContentNode.getUuid(), content6.getNode().getUuid());
       assertEquals(property, content6.getProperty());
       assertNull(content6.getUrl());
       assertNull(content6.getFormat());
       
       // Read cleared content
       Content[] contents7 = this.contentService.read(predicate, property);
       assertNotNull(contents7);
       assertEquals(1, contents7.length);
       Content content7 = contents7[0];
       assertNotNull(content7);
       assertEquals(0, content7.getLength());
       assertEquals(newContentNode.getUuid(), content7.getNode().getUuid());
       assertEquals(property, content7.getProperty());
       assertNull(content7.getUrl());
       assertNull(content7.getFormat());
   }
}
