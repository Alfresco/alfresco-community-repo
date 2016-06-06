package org.alfresco.repo.wiki;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.wiki.WikiPageInfo;

/**
 * An implementation of {@link WikiPageInfo}
 *
 * @author Nick Burch (based on existing webscript controllers in the REST API)
 * @since 4.0
 */
public class WikiPageInfoImpl implements WikiPageInfo 
{
   private NodeRef nodeRef;
   private NodeRef containerNodeRef;
   private String systemName;
   private String title;
   private String contents;
   private String creator;
   private String modifier;
   private Date createdAt;
   private Date modifiedAt;
   private List<String> tags = new ArrayList<String>();

   /**
    * Creates a new, empty WikiPageInfo
    */
   public WikiPageInfoImpl()
   {
   }
   
   /**
    * Create a WikiPageInfo object from an existing node
    */
   public WikiPageInfoImpl(NodeRef nodeRef, NodeRef containerNodeRef, String systemName)
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
   public String getContents() 
   {
      return contents;
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

   @Override
   public void setContents(String contents) 
   {
      this.contents = contents;
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
}
