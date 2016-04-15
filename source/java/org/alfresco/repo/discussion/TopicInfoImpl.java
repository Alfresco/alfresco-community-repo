package org.alfresco.repo.discussion;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.alfresco.service.cmr.discussion.TopicInfo;
import org.alfresco.service.cmr.repository.NodeRef;

/**
 * An implementation of {@link TopicInfo}
 *
 * @author Nick Burch (based on existing webscript controllers in the REST API)
 * @since 4.0
 */
public class TopicInfoImpl implements TopicInfo 
{
   private NodeRef nodeRef;
   private NodeRef containerNodeRef;
   private String systemName;
   private String title;
   private String creator;
   private String modifier;
   private Date createdAt;
   private Date modifiedAt;
   private String shortSiteName;
   private List<String> tags = new ArrayList<String>();

   /**
    * Creates a new, empty {@link TopicInfo}
    */
   public TopicInfoImpl()
   {
   }
   
   /**
    * Create a {@link TopicInfo} object from an existing node
    */
   public TopicInfoImpl(NodeRef nodeRef, NodeRef containerNodeRef, String systemName)
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
   public String getCreator() 
   {
      return creator;
   }

   @Override
   public String getModifier() 
   {
      return modifier;
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
   public List<String> getTags() 
   {
      return tags;
   }

   @Override
   public void setTitle(String title) 
   {
      this.title = title;
   }

   public void setCreator(String creator) 
   {
      this.creator = creator;
   }

   public void setModifier(String modifier) 
   {
      this.modifier = modifier;
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
   
   @Override
   public int hashCode()
   {
      return nodeRef.hashCode();
   }

   @Override
   public boolean equals(Object obj)
   {
      if (obj instanceof TopicInfoImpl)
      {
         TopicInfoImpl tii = (TopicInfoImpl) obj;
         if(nodeRef.equals(tii.nodeRef))
         {
            return true;
         }
      }
      return false;
   }

   @Override
   public String getShortSiteName()
   {
      return shortSiteName;
   }
   
   public void setShortSiteName(String shortSiteName)
   {
      this.shortSiteName = shortSiteName;
   }
}
