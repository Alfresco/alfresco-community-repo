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

import java.util.Properties;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Object that can be used as a backing bean for components in the zoo
 * 
 * @author gavinc
 */
public class DummyBean
{
   private static Log logger = LogFactory.getLog(DummyBean.class);
   
   private String name;
   private Properties properties;
   
   public DummyBean()
   {
      this.properties = new Properties();
      this.properties.put("one", "This is 1");
      this.properties.put("two", "This is 2");
      this.properties.put("three", "This is 3");
      this.properties.put("four", "This is 4");
   }
   
   public Properties getProperties()
   {
      return this.properties;
   }

   /**
    * @return Returns the name.
    */
   public String getName()
   {
      return name;
   }
   
   /**
    * @param name The name to set.
    */
   public void setName(String name)
   {
      this.name = name;
   }
   
   /**
    * @see java.lang.Object#toString()
    */
   public String toString()
   {
      StringBuilder builder = new StringBuilder(super.toString());
      builder.append(" (name=").append(this.name);
      builder.append(" properties=").append(this.properties).append(")");
      return builder.toString();
   }

   /**
    * Method to call on form submit buttons 
    */
   public void submit()
   {
      if (logger.isDebugEnabled())
         logger.debug("Submit called on DummyBean, state = " + toString());
   }
}
