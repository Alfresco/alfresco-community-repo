package org.alfresco.repo.node.getchildren;

import java.util.Date;

import org.alfresco.repo.query.NodeBackedEntity;

/**
 * Parameter objects for {@link GetChildrenAuditableCannedQuery}.
 * 
 * @author Nick Burch
 * @since 4.0
 */
public class GetChildrenAuditableCannedQueryParams extends NodeBackedEntity
{
    private String creatorFilter;
    private Date   createdBefore;
    private Date   createdAfter;
    private String modifierFilter;
    private Date   modifiedBefore;
    private Date   modifiedAfter;
    
    public GetChildrenAuditableCannedQueryParams(Long parentNodeId,
                                         Long nameQNameId,
                                         Long contentTypeQNameId,
                                         String creatorFilter,
                                         Date createdFrom, Date createdTo,
                                         String modifierFilter,
                                         Date modifiedFrom, Date modifiedTo)
                                         
    {
        super(parentNodeId, nameQNameId, contentTypeQNameId);
        this.creatorFilter = creatorFilter;
        this.createdAfter  = createdFrom;
        this.createdBefore = createdTo;
        this.modifierFilter = modifierFilter;
        this.modifiedAfter  = modifiedFrom;
        this.modifiedBefore = modifiedTo;
    }

   public String getCreatorFilter() 
   {
      return creatorFilter;
   }

   public Date getCreatedBefore() 
   {
      return createdBefore;
   }

   public Date getCreatedAfter() 
   {
      return createdAfter;
   }

   public String getModifierFilter() 
   {
      return modifierFilter;
   }

   public Date getModifiedBefore() 
   {
      return modifiedBefore;
   }

   public Date getModifiedAfter() 
   {
      return modifiedAfter;
   }
}
