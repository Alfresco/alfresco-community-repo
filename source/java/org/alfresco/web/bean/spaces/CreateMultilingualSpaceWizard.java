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
package org.alfresco.web.bean.spaces;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.faces.context.FacesContext;

import org.alfresco.i18n.I18NUtil;
import org.alfresco.model.ApplicationModel;
import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.model.FileInfo;
import org.alfresco.service.cmr.repository.MLText;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.DynamicNamespacePrefixResolver;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.web.app.Application;
import org.alfresco.web.app.servlet.FacesHelper;
import org.alfresco.web.bean.CreateMultilingualPropertiesBean;
import org.alfresco.web.bean.UserPreferencesBean;
import org.alfresco.web.bean.repository.Node;
import org.alfresco.web.bean.repository.Repository;


public class CreateMultilingualSpaceWizard extends CreateSpaceWizard
{

      protected boolean showOtherProperties = true;


      /**
       * @return Determines whether the edit properties dialog should be
       *         shown when this one ends
       */
      public boolean isShowOtherProperties()
      {
       return this.showOtherProperties;
      }

      /**
       * @param showOthers Sets whether the edit properties dialog is shown
       */
      public void setShowOtherProperties(boolean showOthers)
      {
         this.showOtherProperties = showOthers;
      }


      @Override
      protected String finishImpl(FacesContext context, String outcome) throws Exception
      {
       String newSpaceId = null;
       CreateMultilingualPropertiesBean createMultilingualPropertiesBean = (CreateMultilingualPropertiesBean) FacesHelper.getManagedBean(context, "CreateMultilingualPropertiesBean");


          if (this.createFrom.equals("scratch"))
          {
            // create the space (just create a folder for now)
            NodeRef parentNodeRef;

            String nodeId = this.navigator.getCurrentNodeId();

            if (nodeId == null)
            {
             parentNodeRef = this.nodeService.getRootNode(Repository.getStoreRef());
            }
            else
            {
             parentNodeRef = new NodeRef(Repository.getStoreRef(), nodeId);
            }


            FileInfo fileInfo = fileFolderService.create(parentNodeRef, this.name, Repository.resolveToQName(this.spaceType));
            NodeRef nodeRef   = fileInfo.getNodeRef();
            newSpaceId        = nodeRef.getId();

            // apply the uifacets aspect - icon, title and description props
            Map<QName, Serializable> uiFacetsProps = new HashMap<QName, Serializable>(5);

            uiFacetsProps.put(ApplicationModel.PROP_ICON,    this.icon);
            // uiFacetsProps.put(ContentModel.PROP_TITLE,       this.title);
            // uiFacetsProps.put(ContentModel.PROP_DESCRIPTION, this.description);


            // Ajout de l icone
            this.nodeService.addAspect(nodeRef, ApplicationModel.ASPECT_UIFACETS, uiFacetsProps);

            Locale language = I18NUtil.parseLocale(createMultilingualPropertiesBean.getNewlanguage());

            this.nodeService.setProperty(nodeRef, ContentModel.PROP_TITLE, new MLText(language, this.title));
            this.nodeService.setProperty(nodeRef, ContentModel.PROP_DESCRIPTION, new MLText(language, this.description));


            // remember the created node
            this.createdNode = nodeRef;
            // Passer le noeud en parametre afin de le recuperer dans un autre dialogue
            this.browseBean.setDocument(new Node(this.createdNode));

            setTitle(""); setDescription(""); setName("");
          }
          else if (this.createFrom.equals("existing"))
          {
            // copy the selected space and update the name, description and icon
            NodeRef sourceNode = this.existingSpaceId;
            NodeRef parentSpace = new NodeRef(Repository.getStoreRef(), this.navigator.getCurrentNodeId());

            // copy from existing
            NodeRef copiedNode = this.fileFolderService.copy(sourceNode, parentSpace, this.name).getNodeRef();

             Locale usrLocale = I18NUtil.parseLocale(getUserPreferencesBean().getContentFilterLanguage());

            // also need to set the new title, description and icon properties

            this.nodeService.setProperty(copiedNode, ContentModel.PROP_TITLE, new MLText(usrLocale, this.title));
            this.nodeService.setProperty(copiedNode, ContentModel.PROP_DESCRIPTION, new MLText(usrLocale, this.description));
            this.nodeService.setProperty(copiedNode, ApplicationModel.PROP_ICON, this.icon);

            newSpaceId = copiedNode.getId();

            // remember the created node
            this.createdNode = copiedNode;
          }
          else if (this.createFrom.equals("template"))
          {
            // copy the selected space and update the name, description and icon
            NodeRef sourceNode = new NodeRef(Repository.getStoreRef(),  this.templateSpaceId);
            NodeRef parentSpace = new NodeRef(Repository.getStoreRef(), this.navigator.getCurrentNodeId());

            // copy from the template
            NodeRef copiedNode = this.fileFolderService.copy(sourceNode, parentSpace, this.name).getNodeRef();
            // also need to set the new title, description and icon properties
            this.nodeService.setProperty(copiedNode, ContentModel.PROP_TITLE,       this.title);
            this.nodeService.setProperty(copiedNode, ContentModel.PROP_DESCRIPTION, this.description);
            this.nodeService.setProperty(copiedNode, ApplicationModel.PROP_ICON,    this.icon);

            newSpaceId = copiedNode.getId();


            // remember the created node
            this.createdNode = copiedNode;
          }

          // if the user selected to save the space as a template space copy the new
          // space to the templates folder
          if (this.saveAsTemplate)
          {
            // get hold of the Templates node
            DynamicNamespacePrefixResolver namespacePrefixResolver = new DynamicNamespacePrefixResolver(null);
            namespacePrefixResolver.registerNamespace(NamespaceService.APP_MODEL_PREFIX, NamespaceService.APP_MODEL_1_0_URI);

            String xpath = Application.getRootPath(FacesContext.getCurrentInstance()) + "/" +
                  Application.getGlossaryFolderName(FacesContext.getCurrentInstance()) + "/" +
                  Application.getSpaceTemplatesFolderName(FacesContext.getCurrentInstance());

            NodeRef rootNodeRef = this.nodeService.getRootNode(Repository.getStoreRef());
            List<NodeRef> templateNodeList = this.searchService.selectNodes(
                  rootNodeRef,
                  xpath, null, namespacePrefixResolver, false);
            if (templateNodeList.size() == 1)
            {
               // get the first item in the list as we from test above there is only one!
               NodeRef templateNode = templateNodeList.get(0);
               NodeRef sourceNode = new NodeRef(Repository.getStoreRef(), newSpaceId);
               // copy this to the template location
               fileFolderService.copy(sourceNode, templateNode, this.templateName);
            }
          }


         return outcome;
      }


      /**
       * @param preferences   The UserPreferencesBean to set
       */
      public void setUserPreferencesBean(UserPreferencesBean preferences)
      {
         this.preferences = preferences;
      }

      /**
       *
       * @return the preferences of the user
       */
      public UserPreferencesBean getUserPreferencesBean()
      {
         return preferences;
      }



      /** The user preferences bean reference */
      protected UserPreferencesBean              preferences;
      protected CreateMultilingualPropertiesBean createMultilingualPropertiesBean;
}

