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
      return "WorkflowDefinition[id=" + id + ",version=" + version + ",title=" + title + ",startTask=" + startTaskDefinition.toString() + "]";
   }
}
