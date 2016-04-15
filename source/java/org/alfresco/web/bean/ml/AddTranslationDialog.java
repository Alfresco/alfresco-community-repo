package org.alfresco.web.bean.ml;

import java.util.Map;

import javax.faces.context.FacesContext;
import javax.faces.model.SelectItem;

import org.springframework.extensions.surf.util.I18NUtil;
import org.alfresco.service.cmr.ml.MultilingualContentService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.web.app.Application;
import org.alfresco.web.bean.users.UserPreferencesBean;
import org.alfresco.web.bean.content.AddContentDialog;
import org.alfresco.web.bean.repository.Repository;

/**
 * Dialog bean to upload a new document and to add it to an existing MLContainer.
 *
 * @author yanipig
 */
public class AddTranslationDialog extends AddContentDialog
{   
   private static final long serialVersionUID = 5588241907778464543L;
   private static final String MSG_OK = "ok";
   
   transient private MultilingualContentService multilingualContentService;
   private UserPreferencesBean userPreferencesBean;

   // the multilingual container where to add this translation
   protected NodeRef mlTranslation;

   // Locale of the new translation
   private String language;

   //  languages available in the ML container yet
   private SelectItem[] unusedLanguages;


   /* (non-Javadoc)
    * @see org.alfresco.web.bean.content.AddContentDialog#init(java.util.Map)
    */
   @Override
   public void init(Map<String, String> parameters)
   {
      super.init(parameters);

      this.language = null;
      this.mlTranslation = this.browseBean.getDocument().getNodeRef();
      setFileName(null);
      unusedLanguages = null;
   }

   /* (non-Javadoc)
    * @see org.alfresco.web.bean.content.AddContentDialog#finishImpl(javax.faces.context.FacesContext, java.lang.String)
    */
   @Override
   protected String finishImpl(FacesContext context, String outcome) throws Exception
   {
      // add the new file to the repository
      outcome = super.finishImpl(context, outcome);

      // add a new translation
      getMultilingualContentService().addTranslation(this.createdNode, this.mlTranslation, I18NUtil.parseLocale(this.language));

      return "dialog:close:browse";
   }

   @Override
   public String cancel()
   {
      super.cancel();

      return getDefaultFinishOutcome();
   }

   public boolean getFinishButtonDisabled()
   {
      return author == null || author.length() < 1 || language == null;
   }

   /**
    * @return the locale of this new translation
    */
   public String getLanguage()
   {
      return language;
   }

   /**
    * @param language the locale of this new translation
    */
   public void setLanguage(String language)
   {
      this.language = language;
   }

   /**
    * @param unusedLanguages
    */
   public void setUnusedLanguages(SelectItem[] unusedLanguages)
   {
      this.unusedLanguages = unusedLanguages;
   }

   /**
    * Method calls by the dialog to limit the list of languages.
    *
    * @return the list of availables translation in the MLContainer
    */

   public SelectItem[] getUnusedLanguages()
   {
      if(unusedLanguages == null)
      {
         unusedLanguages = userPreferencesBean.getAvailablesContentFilterLanguages(this.mlTranslation, false);
      }

      return unusedLanguages;
   }

   public MultilingualContentService getMultilingualContentService()
   {
      if (multilingualContentService == null)
      {
         multilingualContentService = Repository.getServiceRegistry(FacesContext.getCurrentInstance()).getMultilingualContentService();
      }
      return multilingualContentService;
   }

   public void setMultilingualContentService(MultilingualContentService multilingualContentService)
   {
      this.multilingualContentService = multilingualContentService;
   }

   public UserPreferencesBean getUserPreferencesBean()
   {
      return userPreferencesBean;
   }

   public void setUserPreferencesBean(UserPreferencesBean userPreferencesBean)
   {
      this.userPreferencesBean = userPreferencesBean;
   }
   
   @Override
   public String getFinishButtonLabel()
   {
      return Application.getMessage(FacesContext.getCurrentInstance(), MSG_OK);
   }
}
