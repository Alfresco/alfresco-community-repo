/*
 * Copyright (C) 2005-2012 Alfresco Software Limited.
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
package org.alfresco.module.org_alfresco_module_rm.test.service;

import java.util.Arrays;
import java.util.List;
import java.util.Set;

import org.alfresco.module.org_alfresco_module_rm.dod5015.DOD5015Model;
import org.alfresco.module.org_alfresco_module_rm.record.RecordService;
import org.alfresco.module.org_alfresco_module_rm.test.util.BaseRMTestCase;
import org.alfresco.service.namespace.QName;

public class RecordServiceImplTest extends BaseRMTestCase
{

   /**
    * @see RecordService#getRecordMetaDataAspects()
    */
   public void testGetRecordMetaDataAspects() throws Exception
   {
      doTestInTransaction(new Test<Void>()
      {
         @Override
         public Void run()
         {
            Set<QName> aspects = recordService.getRecordMetaDataAspects();
            assertNotNull(aspects);
            assertEquals(5, aspects.size());
            assertTrue(aspects.containsAll(getAspectList()));

            return null;
         }

         /**
          * Helper method for getting a list of record meta data aspects
          * 
          * @return Record meta data aspects as list
          */
         private List<QName> getAspectList()
         {
            QName[] aspects = new QName[]
            {
               DOD5015Model.ASPECT_DIGITAL_PHOTOGRAPH_RECORD,
               DOD5015Model.ASPECT_PDF_RECORD,
               DOD5015Model.ASPECT_WEB_RECORD,
               DOD5015Model.ASPECT_SCANNED_RECORD,
               ASPECT_RECORD_META_DATA
            };

            return Arrays.asList(aspects);
         }
      });
   }

   /**
    * @see RecordService#isRecord(org.alfresco.service.cmr.repository.NodeRef)
    */
   public void testIsRecord() throws Exception
   {
      doTestInTransaction(new Test<Void>()
      {
         @Override
         public Void run()
         {
            // FIXME
            return null;
         }
      });
   }

   /**
    * @see RecordService#isDeclared(org.alfresco.service.cmr.repository.NodeRef)
    */
   public void testIsDeclared() throws Exception
   {
      doTestInTransaction(new Test<Void>()
      {
         @Override
         public Void run()
         {
            // FIXME
            return null;
         }
      });
   }

   /**
    * @see RecordService#createRecordFromDocument(org.alfresco.service.cmr.repository.NodeRef, org.alfresco.service.cmr.repository.NodeRef)
    */
   public void testCreateRecordFromDocument() throws Exception
   {
      doTestInTransaction(new Test<Void>()
      {
         @Override
         public Void run()
         {
            // FIXME
            return null;
         }
      });
   }

   /**
    * @see RecordService#getUnfiledRecordContainer(org.alfresco.service.cmr.repository.NodeRef)
    */
   public void testGetUnfiledRecordContainer() throws Exception
   {
      doTestInTransaction(new Test<Void>()
      {
         @Override
         public Void run()
         {
            // FIXME
            return null;
         }
      });
   }

}
