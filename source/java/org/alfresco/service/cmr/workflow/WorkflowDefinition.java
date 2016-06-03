package org.alfresco.service.cmr.workflow;

import java.io.Serializable;

import org.alfresco.api.AlfrescoPublicApi;


/**
 * Workflow Definition Data Object
 *  
 * @author davidc
 */
@AlfrescoPublicApi
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
