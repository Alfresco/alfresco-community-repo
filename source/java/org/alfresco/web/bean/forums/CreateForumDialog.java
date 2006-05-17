package org.alfresco.web.bean.forums;

import java.util.Map;

import javax.faces.context.FacesContext;

import org.alfresco.model.ForumModel;
import org.alfresco.web.app.Application;
import org.alfresco.web.bean.spaces.CreateSpaceDialog;

/**
 * Bean used to implement the "Create Forum Dialog".
 * 
 * @author gavinc
 */
public class CreateForumDialog extends CreateSpaceDialog
{
   // ------------------------------------------------------------------------------
   // Wizard implementation
   
   @Override
   public void init(Map<String, String> parameters)
   {
      super.init(parameters);
      
      this.spaceType = ForumModel.TYPE_FORUM.toString();
   }
   
   @Override
   public String getFinishButtonLabel()
   {
      return Application.getMessage(FacesContext.getCurrentInstance(), "create_forum");
   }
}
