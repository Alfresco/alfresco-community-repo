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
package org.alfresco.web.bean.wcm;

import java.io.Serializable;
import java.util.Map;

import org.alfresco.service.namespace.QName;

/**
 * Interface contract for wrapper classes that encapsulate workflow configuration data.
 * 
 * @author Kevin Roast
 */
public interface WorkflowConfiguration extends Serializable
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
