/*
 * Copyright (C) 2005-2007 Alfresco Software Limited.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.

 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.

 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.

 * As a special exception to the terms and conditions of version 2.0 of
 * the GPL, you may redistribute this Program in connection with Free/Libre
 * and Open Source Software ("FLOSS") applications as described in Alfresco's
 * FLOSS exception.  You should have recieved a copy of the text describing
 * the FLOSS exception, and it is also available here:
 * http://www.alfresco.com/legal/licensing"
 */
package org.alfresco.web.bean;

import java.io.Serializable;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;

import org.alfresco.i18n.I18NUtil;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.version.common.VersionLabelComparator;
import org.alfresco.service.cmr.ml.ContentFilterLanguagesService;
import org.alfresco.service.cmr.ml.EditionService;
import org.alfresco.service.cmr.ml.MultilingualContentService;
import org.alfresco.service.cmr.repository.InvalidNodeRefException;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.version.Version;
import org.alfresco.service.cmr.version.VersionHistory;
import org.alfresco.service.cmr.version.VersionService;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.ParameterCheck;
import org.alfresco.web.app.Application;
import org.alfresco.web.app.servlet.DownloadContentServlet;
import org.alfresco.web.bean.repository.MapNode;
import org.alfresco.web.bean.repository.Node;
import org.alfresco.web.bean.repository.Repository;
import org.alfresco.web.ui.common.Utils;
import org.alfresco.web.ui.common.component.UIActionLink;

/**
 * Bean with generic function helping the rendering of the versioned properties
 *
 * @author Yanick Pignot
 */
public class VersionedDocumentDetailsBean
{
	/** Dependencies */
	protected VersionService versionService;
    protected EditionService editionService;
    protected NodeService nodeService;
    protected MultilingualContentService multilingualContentService;
    protected ContentFilterLanguagesService contentFilterLanguagesService;

    private static final Comparator VERSION_LABEL_COMPARATOR = new VersionLabelComparator();

    /** Determine if the version is a translation of a old edition */
    private boolean fromPreviousEditon;

    /** The version selected by the user */
    private Version documentVersion;
    private VersionHistory versionHistory;

    /** The multilingual information of the selected version selected by the user */
    private Version documentEdition;
    private VersionHistory editionHistory;


    public void init()
    {
        fromPreviousEditon = false;
        documentVersion = null;
        versionHistory = null;
        documentEdition = null;
        editionHistory = null;
    }

    /**
     * Set which version of the current node that the user want to display the properties
     */
    public void setBrowsingVersion(ActionEvent event)
    {
       init();

       // Get the properties of the action event
       UIActionLink link = (UIActionLink)event.getComponent();
       Map<String, String> params = link.getParameterMap();

       String versionLabel = params.get("versionLabel");
       String id = params.get("id");
       String lang = params.get("lang");

       setBrowsingVersion(id, versionLabel, lang);
    }


    /**
     * Implementation of setBrowsingVersion action event to be use with the needed parameters.
     */
    private void setBrowsingVersion(String id, String versionLabel, String lang)
    {
	   // test if the mandatories parameter are valid
	   ParameterCheck.mandatoryString("The id of the node", id);
	   ParameterCheck.mandatoryString("The version of the node", versionLabel);

       try
       {
    	   // try to get the nodeRef with the given ID. This node is not a versioned node.
    	   NodeRef currentNodeRef = new NodeRef(Repository.getStoreRef(), id);

    	   // the threatment is different if the node is a translation or a mlContainer
    	   if(nodeService.getType(currentNodeRef).equals(ContentModel.TYPE_MULTILINGUAL_CONTAINER))
    	   {
               //  test if the lang parameter is valid
               ParameterCheck.mandatoryString("The lang of the node", lang);

               fromPreviousEditon = true;

               versionLabel = cleanVersionLabel(versionLabel);

               // set the edition information of the mlContainer of the selected translation version
               this.editionHistory  = editionService.getEditions(currentNodeRef);
               this.documentEdition = editionHistory.getVersion(versionLabel);

               // set the version to display
               this.documentVersion = getBrowsingVersionForMLContainer(currentNodeRef, versionLabel, lang);
    	   }
    	   else
    	   {
               fromPreviousEditon = false;

               // set the version history
               this.versionHistory = versionService.getVersionHistory(currentNodeRef);
               // set the version to display
               this.documentVersion = getBrowsingVersionForDocument(currentNodeRef, versionLabel);
    	   }
       }
       catch (InvalidNodeRefException refErr)
       {
          Utils.addErrorMessage(MessageFormat.format(Application.getMessage(
        		  FacesContext.getCurrentInstance(), Repository.ERROR_NODEREF), new Object[] {id}) );
       }
    }

   /**
    * Navigates to next item in the list of versioned content for the current VersionHistory
    */
   @SuppressWarnings("unchecked")
   public void nextItem(ActionEvent event)
   {
       // Get the properties of the action event
       UIActionLink link = (UIActionLink)event.getComponent();
       Map<String, String> params = link.getParameterMap();

       String versionLabel = params.get("versionLabel");

       // if the version is not specified, get the next version
       if(versionLabel == null)
       {
           List<Version> nextVersions = new ArrayList<Version>(this.versionHistory.getSuccessors(this.documentVersion));

           // if the version history doesn't contains successor, get the root version (the first one)
           if(nextVersions == null || nextVersions.size() < 1)
           {
               this.documentVersion = versionHistory.getRootVersion();
           }
           else
           {
               Collections.sort(nextVersions, VERSION_LABEL_COMPARATOR);
               this.documentVersion = nextVersions.get(0);
           }
       }
       else
       {
           this.documentVersion = this.versionHistory.getVersion(versionLabel);
       }
   }

   /**
    * Navigates to previous item in the list of versioned content for the current VersionHistory
    */
   @SuppressWarnings("unchecked")
   public void previousItem(ActionEvent event)
   {
       // Get the properties of the action event
       UIActionLink link = (UIActionLink)event.getComponent();
       Map<String, String> params = link.getParameterMap();

       String versionLabel = params.get("versionLabel");

       // if the version is not specified, get the next version
       if(versionLabel == null)
       {
           Version prevVersion = this.versionHistory.getPredecessor(this.documentVersion);

           // if the version history doesn't contains predecessor, get the last version
           if(prevVersion == null)
           {
               List<Version> allVersions = new ArrayList<Version>(this.versionHistory.getAllVersions());
               Collections.sort(allVersions, VERSION_LABEL_COMPARATOR);

               this.documentVersion = allVersions.get(0);
           }
           else
           {
               this.documentVersion = prevVersion;
           }
       }
       else
       {
           this.documentVersion = this.versionHistory.getVersion(versionLabel);
       }
   }

   /**
    * Returns a list of objects representing the translations of the given version of the mlContainer
    *
    * @return List of translations
    */
    @SuppressWarnings("unchecked")
    public List getTranslations()
    {
       // get the version of the mlContainer and its translations
       List<VersionHistory> translationsList = editionService.getVersionedTranslations(this.documentEdition);

       Map<Locale, NodeRef> translationNodeRef;

       // if translation size == 0, the edition is the current edition and the translations are not yet attached.
       if(translationsList.size() == 0)
       {
           // the selection edition is the current: use the multilingual content service
           translationNodeRef = multilingualContentService.getTranslations(this.documentEdition.getVersionedNodeRef());
       }
       else
       {
           translationNodeRef = new HashMap<Locale, NodeRef>(translationsList.size());

           // get the last version of the translation in the given lang of the edition
           for (VersionHistory history : translationsList)
           {
               //   get the list of versions and sort them ascending according their version label
               List<Version> orderedVersions = new ArrayList<Version>(history.getAllVersions());
               Collections.sort(orderedVersions, VERSION_LABEL_COMPARATOR);

               // the last version is the first version of the list
               Version lastVersion = orderedVersions.get(0);

               // fill the list of translation
               if(lastVersion != null)
               {
                   NodeRef versionNodeRef = lastVersion.getFrozenStateNodeRef();
                   Locale locale = (Locale) nodeService.getProperty(versionNodeRef, ContentModel.PROP_LOCALE);
                   translationNodeRef.put(locale, versionNodeRef);
               }
           }
       }

       // the list of client-side translation to return
       List<MapNode> translations = new ArrayList<MapNode>(translationNodeRef.size());

       for (Map.Entry<Locale, NodeRef> entry : translationNodeRef.entrySet())
       {
           Locale locale  = entry.getKey();
           NodeRef nodeRef = entry.getValue();

           //  create a map node representation of the translation
           MapNode mapNode = new MapNode(nodeRef);

           String lgge = (locale != null) ?
                 // convert the locale into new ISO codes
                 contentFilterLanguagesService.convertToNewISOCode(locale.getLanguage()).toUpperCase()
                 : null;

           mapNode.put("name", nodeService.getProperty(nodeRef, ContentModel.PROP_NAME));
           mapNode.put("language", lgge);
           mapNode.put("url", DownloadContentServlet.generateBrowserURL(nodeRef, mapNode.getName()));

           mapNode.put("notEmpty", new Boolean(!nodeService.hasAspect(nodeRef, ContentModel.ASPECT_MULTILINGUAL_EMPTY_TRANSLATION)));

           // add the client side version to the list
           translations.add(mapNode);

       }

       return translations;
    }


    /**
     * Returns a list of objects representing the versions of the
     * current document
     *
     * @return List of previous versions
     */
    public List getVersionHistory()
    {
       List<MapNode> versions = new ArrayList<MapNode>();

       for (Version version : this.versionHistory.getAllVersions())
       {
          // create a map node representation of the version
          MapNode clientVersion = new MapNode(version.getFrozenStateNodeRef());
          clientVersion.put("versionLabel", version.getVersionLabel());
          clientVersion.put("notes", version.getDescription());
          clientVersion.put("author", version.getCreator());
          clientVersion.put("versionDate", version.getCreatedDate());

          if(getFrozenStateDocument().hasAspect(ContentModel.ASPECT_MULTILINGUAL_EMPTY_TRANSLATION))
          {
              clientVersion.put("url", null);
          }
          else
          {
              clientVersion.put("url", DownloadContentServlet.generateBrowserURL(version.getFrozenStateNodeRef(),
                      clientVersion.getName()));
          }

          // add the client side version to the list
          versions.add(clientVersion);
       }

       return versions;
    }

   /**
    * @return true if the version is a translation of a previous edition
    */
   public boolean isFromPreviousEditon()
   {
       return fromPreviousEditon;
   }

   /**
    * Returns the URL to download content for the current document
    *
    * @return Content url to download the current document
    */
   public String getUrl()
   {
       return DownloadContentServlet.generateBrowserURL(getFrozenStateNodeRef(), getName());
   }

  /**
   * @return the versioned node selected by the user
   */
   public Node getFrozenStateDocument()
   {
       return new Node(getFrozenStateNodeRef());
   }

  /**
   * @return the versioned node ref selected by the user
   */
   public NodeRef getFrozenStateNodeRef()
   {
       return documentVersion.getFrozenStateNodeRef();
   }

   /**
    * @return the edition of the mlContainer of the selected verion of the translation
    */
   public Node getMultilingualContainerDocument()
   {
       return new Node(documentEdition.getFrozenStateNodeRef());
   }

  /**
   * @return the name of selected version
   */
   public String getName()
   {
      String name  = (String) nodeService.getProperty(getFrozenStateNodeRef(), ContentModel.PROP_NAME);
      return name;
   }

   /**
    * @return the file type image URL of the version
    */
   public String getFileType32()
   {
       String fileType = Utils.getFileTypeImage(getName(), false);
       return fileType;
   }

   public boolean isEmptyTranslation()
   {
       return nodeService.hasAspect(getFrozenStateNodeRef(), ContentModel.ASPECT_MULTILINGUAL_EMPTY_TRANSLATION);
   }

   /**
    * @return the version to display of the document selected by the user
    */
   public Version getVersion()
   {
       return this.documentVersion;
   }

   /**
    * @return the next page to display according to which page the dialog is coming from
    */
   public String getOutcome()
   {
       return (this.fromPreviousEditon) ? "showMLContainerDetails" :  "showDocDetails";
   }

   /**
    * Util method which remove the eventual '(actual)'  label from the version label.
    */
   private String cleanVersionLabel(String versionLabel)
   {
       // remove the (actual vesrion) label if needed
       int idx = versionLabel.indexOf(' ');
       if(idx > -1)
       {
           versionLabel = versionLabel.substring(0, idx);
       }
       return versionLabel;
   }

   /**
    * Util method which returns the given version of a node
    */
   private Version getBrowsingVersionForDocument(NodeRef document, String versionLabel)
   {
	   return this.versionService.getVersionHistory(document).getVersion(versionLabel);
   }

   /**
    * Util method which returns the current version of a node
    */
   private Version getBrowsingCurrentVersionForMLContainer(NodeRef document, String lang)
   {
	   NodeRef translation = multilingualContentService.getTranslationForLocale(document, I18NUtil.parseLocale(lang));
	   this.versionHistory = versionService.getVersionHistory(translation);

	   return versionService.getCurrentVersion(translation);
   }

   /**
    * Util method which return the last version of a translation of a given edition of a mlContainer according its language
    */
   @SuppressWarnings("unchecked")
   private Version getBrowsingVersionForMLContainer(NodeRef document, String editionLabel, String lang)
   {
       // get the list of translations of the given edition of the mlContainer
	   List<VersionHistory> translations = editionService.getVersionedTranslations(this.documentEdition);

	   // if translation size == 0, the edition is the current edition and the translations are not yet attached.
	   if(translations.size() == 0)
	   {
		   // the selection edition is the current.
		   return getBrowsingCurrentVersionForMLContainer(document, lang);
	   }
	   else
	   {
		   Version versionToReturn = null;

		   // get the last version of the translation in the given lang of the edition
		   for (VersionHistory history : translations)
		   {
			   //	get the list of versions and sort them ascending according their version label
	           List<Version> orderedVersions = new ArrayList<Version>(history.getAllVersions());
	           Collections.sort(orderedVersions, VERSION_LABEL_COMPARATOR);

	           // the last version is the first version of the list
	           Version lastVersion = orderedVersions.get(0);

			   if(lastVersion != null)
			   {
				   Map<QName, Serializable> properties = editionService.getVersionedMetadatas(lastVersion);
				   Locale locale = (Locale) properties.get(ContentModel.PROP_LOCALE);

				   if(locale.getLanguage().equalsIgnoreCase(lang))
				   {
					   versionToReturn = lastVersion;
                       this.versionHistory = history;
					   break;
				   }
			   }
		   }
		   return versionToReturn;
	   }

   }


  /**
   * @param versionService the Version Service to set
   */
   public void setVersionService(VersionService versionService)
   {
      this.versionService = versionService;
   }

   /**
    * @param editionService the Edition Service to set
	*/
   public void setEditionService(EditionService editionService)
   {
       this.editionService = editionService;
   }

   /**
    * @param nodeService the Node Service to set
    */
   public void setNodeService(NodeService nodeService)
   {
       this.nodeService = nodeService;
   }

   /**
    * @param contentFilterLanguagesService the Content Filter Languages Service to set
    */
   public void setContentFilterLanguagesService(ContentFilterLanguagesService contentFilterLanguagesService)
   {
      this.contentFilterLanguagesService = contentFilterLanguagesService;
   }

   /**
    * @param Multilingual Content Service the Multilingual Content Service to set
    */
   public void setMultilingualContentService(MultilingualContentService multilingualContentService)
   {
      this.multilingualContentService = multilingualContentService;
   }
}