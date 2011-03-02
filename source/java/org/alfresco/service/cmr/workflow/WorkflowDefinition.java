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
package org.alfresco.service.cmr.workflow;

import java.io.Serializable;


/**
 * Workflow Definition Data Object
 *  
 * @author davidc
 */
public class WorkflowDefinition implements Serializable
{
   private static final long serialVersionUID = -4320345925926816927L;
   
   @Deprecated
   public final String id;
   
   @Deprecated
   public final String name;
   
   @Deprecated
   public final String version;
   
   @Deprecated
   public final String title;
   
   @Deprecated
   public final String description;
   
   transient private final WorkflowTaskDefinition startTaskDefinition;

   public WorkflowDefinition(final String id,
                             final String name,
                             final String version,
                             final String title,
                             final String description,
                             final WorkflowTaskDefinition startTaskDefinition)
   {
      this.id = id;
      this.name = name;
      this.version = version;
      this.title = title;
      this.description = description;
      this.startTaskDefinition = startTaskDefinition;
   }
   
   /** Workflow Definition unique id */
   public String getId()
   {
      return this.id;
   }
    

   /** Workflow Definition name */
   public String getName()
   {
      return this.name;
   }
    
   /** Workflow Definition version */
   public String getVersion()
   {
      return this.version;
   }
    
   /** Workflow Definition Title (Localised) */
   public String getTitle()
   {
      return this.title;
   }
    
   /** Workflow Definition Description (Localised) */
   public String getDescription()
   {
      return this.description;
   }

   /** Task Definition for Workflow Start Task (Optional) */
   public WorkflowTaskDefinition getStartTaskDefinition()
   {
      return this.startTaskDefinition;
   }
    
    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString()
    {
        return "WorkflowDefinition[id=" + id + ",name=" + name + ",version=" + version + ",title=" + title + ",startTask=" + ((getStartTaskDefinition() == null) ? "undefined" : getStartTaskDefinition().toString()) + "]";
    }
}
