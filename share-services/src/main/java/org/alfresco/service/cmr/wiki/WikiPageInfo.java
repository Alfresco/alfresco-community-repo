package org.alfresco.service.cmr.wiki;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

import org.alfresco.repo.security.permissions.PermissionCheckValue;
import org.alfresco.service.cmr.repository.NodeRef;

/**
 * This class represents a Wiki Paeg in a site 
 * 
 * @author Nick Burch
 * @since 4.0
 */
public interface WikiPageInfo extends Serializable, PermissionCheckValue 
{
   /**
    * @return the NodeRef of the underlying wiki page
    */
   NodeRef getNodeRef();
   
   /**
    * @return the NodeRef of the site container this belongs to
    */
   NodeRef getContainerNodeRef();
   
   /**
    * @return the name of the wiki page
    */
   String getSystemName();
   
   /**
    * @return the Title of the wiki page
    */
   String getTitle();
   
   /**
    * Sets the Title of the wiki page
    */
   void setTitle(String title);
   
   /**
    * @return the HTML Content of the wiki page
    */
   String getContents();
   
   /**
    * Sets the (HTML) Content of the wiki page
    */
   void setContents(String contentHTML);
   
   /**
    * @return the creator of the wiki page
    */
   String getCreator();
   
   /**
    * @return the modifier of the wiki page
    */
   String getModifier();
   
   /**
    * @return the creation date and time
    */
   Date getCreatedAt();
   
   /**
    * @return the modification date and time
    */
   Date getModifiedAt();
   
   /**
    * @return the Tags associated with the wiki page 
    */
   List<String> getTags();
}
