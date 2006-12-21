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
package org.alfresco.web.bean.wcm;

import java.io.Serializable;
import java.util.Map;

import org.alfresco.service.namespace.QName;

/**
 * Interface contract for wrapper classes that encapsulate workflow configuration data.
 * 
 * @author Kevin Roast
 */
public interface WorkflowConfiguration
{
   /**
    * @return definition name of the workflow
    */
   public String getName();
   
   /**
    * @return the param map for the workflow
    */
   public Map<QName, Serializable> getParams();
   
   /**
    * @param params     The param map for the workflow
    */
   public void setParams(Map<QName, Serializable> params);
   
   /**
    * @return the filename pattern match regex for the workflow
    */
   public String getFilenamePattern();
   
   /**
    * @param pattern    The filename pattern match regex for the workflow  
    */
   public void setFilenamePattern(String pattern);
   
   /**
    * @return the QName type of the underlying node representing the workflow
    */
   public QName getType();

   /**
    * @param type       QName type of the underlying node representing the workflow
    */
   public void setType(QName type);
}
