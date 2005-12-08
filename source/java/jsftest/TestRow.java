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
package jsftest;

import java.util.Date;

/**
 * @author kevinr
 */
public class TestRow
{
   /**
    * Test a row bean with various data types
    */
   public TestRow(String name, int count, boolean valid, float relevance, Date created)
   {
      this.name = name;
      this.count = count;
      this.valid = valid;
      this.relevance = relevance;
      this.created = created;
   }
   
   public String getName()
   {
      return name;
   }
   
   public int getCount()
   {
      return count;
   }
   
   public boolean getValid()
   {
      return valid;
   }
   
   public float getRelevance()
   {
      return relevance;
   }

   public Date getCreated()
   {
      return created;
   }
   
   public void setCreated(Date date)
   {
      this.created = date;
   }
   
   public TestRow getObject()
   {
      return this;
   }
   
   
   private String name;
   private int count;
   private boolean valid;
   private float relevance;
   private Date created;
}
