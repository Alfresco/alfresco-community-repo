/*
 * Copyright (C) 2005-2010 Alfresco Software Limited.
 *
 * This file is part of Alfresco
 *
 * Alfresco is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Alfresco is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Alfresco. If not, see <http://www.gnu.org/licenses/>.
 */
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