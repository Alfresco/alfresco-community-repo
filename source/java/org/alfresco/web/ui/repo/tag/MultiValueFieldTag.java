package org.alfresco.web.ui.repo.tag;

import org.alfresco.web.ui.repo.RepoConstants;

/**
 * Tag that combines the multi value editor component and 
 * the field renderer.
 * 
 * @author gavinc
 *
 */
public class MultiValueFieldTag extends BaseMultiValueTag
{
   /**
    * @see javax.faces.webapp.UIComponentTag#getRendererType()
    */
   public String getRendererType()
   {
      return RepoConstants.ALFRESCO_FACES_FIELD_RENDERER;
   }
}
