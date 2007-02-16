/*
 * Copyright (C) 2005 Alfresco, Inc.
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
package org.alfresco.service.cmr.workflow;


/**
 * Workflow Definition Data Object
 *  
 * @author davidc
 */
public class WorkflowDefinition
{
   //XXarielb these should most likely all be private
   public final String id;
   public final String name;
   public final String version;
   public final String title;
   public final String description;
   public final WorkflowTaskDefinition startTaskDefinition;

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
    public String toString()
    {
        return "WorkflowDefinition[id=" + id + ",name=" + name + ",version=" + version + ",title=" + title + ",startTask=" + ((startTaskDefinition == null) ? "undefined" : startTaskDefinition.toString()) + "]";
    }
}
