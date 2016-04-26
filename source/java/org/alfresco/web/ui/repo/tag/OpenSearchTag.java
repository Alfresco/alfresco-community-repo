package org.alfresco.web.ui.repo.tag;

import org.alfresco.web.ui.common.tag.HtmlComponentTag;

/**
 * Tag class for the OpenSearch component
 * 
 * @author gavinc
 */
public class OpenSearchTag extends HtmlComponentTag
{
   /**
    * @see javax.faces.webapp.UIComponentTag#getComponentType()
    */
   public String getComponentType()
   {
      return "org.alfresco.faces.OpenSearch";
   }

   /**
    * @see javax.faces.webapp.UIComponentTag#getRendererType()
    */
   public String getRendererType()
   {
      return null;
   }
}
