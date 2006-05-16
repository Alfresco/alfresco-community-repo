package org.alfresco.web.bean.forums;

import java.util.Map;

import javax.faces.context.FacesContext;

import org.alfresco.model.ForumModel;
import org.alfresco.web.app.Application;
import org.alfresco.web.bean.content.CreateContentWizard;
import org.alfresco.web.bean.repository.Repository;
import org.alfresco.web.ui.common.Utils;

/**
 * Bean implementation of the "New Post Dialog".
 * 
 * @author gavinc
 */
public class CreatePostDialog extends CreateContentWizard
{
   // ------------------------------------------------------------------------------
   // Wizard implementation
   
   @Override
   public void init(Map<String, String> parameters)
   {
      super.init(parameters);
      
      // set up for creating a post
      this.objectType = ForumModel.TYPE_POST.toString();
      
      // make sure we don't show the edit properties dialog after creation
      this.showOtherProperties = false;
   }
   
   @Override
   protected String finishImpl(FacesContext context, String outcome) throws Exception
   {
      // create appropriate values for filename and content type
      this.fileName = ForumsBean.createPostFileName();
      this.mimeType = Repository.getMimeTypeForFileName(
                  FacesContext.getCurrentInstance(), this.fileName);
      
      // remove link breaks and replace with <br/>
      this.content = Utils.replaceLineBreaks(this.content);
      
      return super.finishImpl(context, outcome);
   }

   @Override
   public String getFinishButtonLabel()
   {
      return Application.getMessage(FacesContext.getCurrentInstance(), "post");
   }
}
