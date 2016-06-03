package org.alfresco.web.ui.repo.tag;

import org.alfresco.web.ui.repo.RepoConstants;

/**
 * Tag that combines the multi value editor component and 
 * the selector renderer.
 * 
 * @author gavinc
 */
public class MultiValueSelectorTag extends BaseMultiValueTag
{
   /**
    * @see javax.faces.webapp.UIComponentTag#getRendererType()
    */
   public String getRendererType()
   {
      return RepoConstants.ALFRESCO_FACES_SELECTOR_RENDERER;
   }
}
