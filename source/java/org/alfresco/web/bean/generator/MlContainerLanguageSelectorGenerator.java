package org.alfresco.web.bean.generator;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.faces.model.SelectItem;

import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.ml.ContentFilterLanguagesService;
import org.alfresco.service.cmr.ml.MultilingualContentService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;


/**
 * Generates a LANGUAGE selector component for display a list of language 
 * for a ML container.
 * 
 * The list of languages must contains the languages of each of this <b>non-empty</b>
 * translations.
 * 
 * @author Yannick Pignot
 */
public class MlContainerLanguageSelectorGenerator extends LanguageSelectorGenerator
{
   protected MultilingualContentService multilingualContentService;
   protected ContentFilterLanguagesService contentFilterLanguagesService;
   protected NodeService nodeService;
   
   protected SelectItem[] getLanguageItems()
   {
      Map<Locale, NodeRef> translations = multilingualContentService.getTranslations(node.getNodeRef());
       
       List<SelectItem> items = new ArrayList<SelectItem>();
       
       for(Map.Entry<Locale, NodeRef> entry : translations.entrySet()) 
       {
          if(!nodeService.hasAspect(entry.getValue(), ContentModel.ASPECT_MULTILINGUAL_EMPTY_TRANSLATION))
          {
             Locale loc = entry.getKey();
             
             items.add(new SelectItem(
                   loc.getLanguage(),                      
                      contentFilterLanguagesService.getLabelByCode(loc.toString())));
             
          }
       }
       
       SelectItem[] itemsArray = new SelectItem[items.size()]; 
       items.toArray(itemsArray);
       
       return itemsArray;
   }

   /**
    * Set the injected contentFilterLanguagesService
    * 
    * @param contentFilterLanguagesService
    */
   public void setContentFilterLanguagesService(
         ContentFilterLanguagesService contentFilterLanguagesService) 
   {
      this.contentFilterLanguagesService = contentFilterLanguagesService;
   }
   
   /**
    * Set the injected multilingualContentService
    * 
    * @param multilingualContentService
    */
   public void setMultilingualContentService(
         MultilingualContentService multilingualContentService) 
   {
      this.multilingualContentService = multilingualContentService;
   }

   /**
    * Set the injected nodeService
    * 
    * @param nodeService the nodeService to set
    */
   public void setNodeService(NodeService nodeService) 
   {
      this.nodeService = nodeService;
   }   
}