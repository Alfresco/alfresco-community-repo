package org.alfresco.repo.links;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.alfresco.service.cmr.links.LinkInfo;
import org.alfresco.service.cmr.repository.NodeRef;

/**
 * An implementation of {@link LinkInfo}
 *
 * @author Nick Burch (based on existing webscript controllers in the REST API)
 * @since 4.0
 */
public class LinkInfoImpl implements LinkInfo 
{
   private NodeRef nodeRef;
   private NodeRef containerNodeRef;
   private String systemName;
   private String title;
   private String description;
   private String url;
   private String creator;
   private Date createdAt;
   private Date modifiedAt;
   private boolean internal;
   private List<String> tags = new ArrayList<String>();

   /**
    * Creates a new, empty LinkInfo
    */
   public LinkInfoImpl()
   {
   }
   
   /**
    * Createa a LinkInfo object from an existing node
    */
   public LinkInfoImpl(NodeRef nodeRef, NodeRef containerNodeRef, String systemName)
   {
      this.nodeRef = nodeRef;
      this.containerNodeRef = containerNodeRef;
      this.systemName = systemName;
   }

   @Override
   public NodeRef getContainerNodeRef() 
   {
      return containerNodeRef;
   }

   @Override
   public NodeRef getNodeRef() 
   {
      return nodeRef;
   }
   
   @Override
   public String getSystemName() 
   {
      return systemName;
   }

   @Override
   public String getTitle() 
   {
      return title;
   }

   @Override
   public String getDescription() 
   {
      return description;
   }

   @Override
   public String getURL() 
   {
      return url;
   }
   
   @Override
   public String getCreator() 
   {
      return creator;
   }

   @Override
   public Date getCreatedAt() 
   {
      return createdAt;
   }

   @Override
   public Date getModifiedAt() 
   {
      return modifiedAt;
   }

   @Override
   public boolean isInternal() 
   {
      return internal;
   }

   @Override
   public List<String> getTags() 
   {
      return tags;
   }

   @Override
   public void setTitle(String title) 
   {
      this.title = title;
   }

   @Override
   public void setDescription(String description) 
   {
      this.description = description;
   }

   @Override
   public void setURL(String url) 
   {
      this.url = url;
   }
   
   @Override
   public void setInternal(boolean internal) 
   {
      this.internal = internal;
   }
   
   public void setCreator(String creator) 
   {
      this.creator = creator;
   }

   public void setCreatedAt(Date createdAt) 
   {
      this.createdAt = createdAt;
   }

   public void setModifiedAt(Date modifiedAt) 
   {
      this.modifiedAt = modifiedAt;
   }

   public void setTags(List<String> tags)
   {
      this.tags = tags;
   }
}
