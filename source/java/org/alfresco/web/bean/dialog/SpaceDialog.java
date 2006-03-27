package org.alfresco.web.bean.dialog;

import java.io.Serializable;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.faces.context.FacesContext;
import javax.transaction.UserTransaction;

import org.alfresco.config.Config;
import org.alfresco.config.ConfigElement;
import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.model.FileExistsException;
import org.alfresco.service.cmr.model.FileFolderService;
import org.alfresco.service.cmr.model.FileInfo;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.search.SearchService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.web.app.Application;
import org.alfresco.web.bean.BrowseBean;
import org.alfresco.web.bean.NavigationBean;
import org.alfresco.web.bean.repository.Repository;
import org.alfresco.web.ui.common.Utils;
import org.alfresco.web.ui.common.component.UIListItem;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


/**
 * Dialog bean to create a space
 * 
 * @author gavinc
 */
public class SpaceDialog extends BaseDialogBean
{
   protected static final String ERROR = "error_space";
   private static final String SPACE_ICON_DEFAULT = "space-icon-default";
//   private static final String DEFAULT_SPACE_TYPE_ICON = "/images/icons/space.gif";
   private static final String ICONS_LOOKUP_KEY = " icons";
   
   protected NodeService nodeService;
   protected FileFolderService fileFolderService;
   protected NamespaceService namespaceService;
   protected SearchService searchService;
   protected NavigationBean navigator;
   protected BrowseBean browseBean;
   
   protected String spaceType;
   protected String icon;
   protected String name;
   protected String description;

   
   private static Log logger = LogFactory.getLog(SpaceDialog.class);
   
   /**
    * @param nodeService The nodeService to set.
    */
   public void setNodeService(NodeService nodeService)
   {
      this.nodeService = nodeService;
   }

   /**
    * @param fileFolderService used to manipulate folder/folder model nodes
    */
   public void setFileFolderService(FileFolderService fileFolderService)
   {
      this.fileFolderService = fileFolderService;
   }

   /**
    * @param searchService the service used to find nodes
    */
   public void setSearchService(SearchService searchService)
   {
      this.searchService = searchService;
   }
   
   /**
    * @param namespaceService The NamespaceService
    */
   public void setNamespaceService(NamespaceService namespaceService)
   {
      this.namespaceService = namespaceService;
   }
   
   /**
    * @param navigator The NavigationBean to set.
    */
   public void setNavigator(NavigationBean navigator)
   {
      this.navigator = navigator;
   }

   /**
    * @param browseBean The BrowseBean to set.
    */
   public void setBrowseBean(BrowseBean browseBean)
   {
      this.browseBean = browseBean;
   }
   
   /**
    * @return Returns the description.
    */
   public String getDescription()
   {
      return description;
   }
   
   /**
    * @param description The description to set.
    */
   public void setDescription(String description)
   {
      this.description = description;
   }
   
   /**
    * @return Returns the icon.
    */
   public String getIcon()
   {
      return icon;
   }
   
   /**
    * @param icon The icon to set.
    */
   public void setIcon(String icon)
   {
      this.icon = icon;
   }
   
   /**
    * @return Returns the name.
    */
   public String getName()
   {
      return name;
   }
   
   /**
    * @param name The name to set.
    */
   public void setName(String name)
   {
      this.name = name;
   }

   /**
    * @return Returns the spaceType.
    */
   public String getSpaceType()
   {
      return spaceType;
   }
   
   /**
    * @param spaceType The spaceType to set.
    */
   public void setSpaceType(String spaceType)
   {
      this.spaceType = spaceType;
   }
   
   /**
    * Returns a list of icons to allow the user to select from.
    * The list can change according to the type of space being created.
    * 
    * @return A list of icons
    */
   @SuppressWarnings("unchecked")
   public List<UIListItem> getIcons()
   {
      // NOTE: we can't cache this list as it depends on the space type
      //       which the user can change during the advanced space wizard
      
      List<UIListItem> icons = null;
      
      QName type = QName.createQName(this.spaceType);
      String typePrefixForm = type.toPrefixString(this.namespaceService);
      
      Config config = Application.getConfigService(FacesContext.getCurrentInstance()).
            getConfig(typePrefixForm + ICONS_LOOKUP_KEY);
      if (config != null)
      {
         ConfigElement iconsCfg = config.getConfigElement("icons");
         if (iconsCfg != null)
         {
            boolean first = true;
            for (ConfigElement icon : iconsCfg.getChildren())
            {
               String iconName = icon.getAttribute("name");
               String iconPath = icon.getAttribute("path");
               
               if (iconName != null && iconPath != null)
               {
                  if (first)
                  {
                     // if this is the first icon create the list and make 
                     // the first icon in the list the default
                     
                     icons = new ArrayList<UIListItem>(iconsCfg.getChildCount());
                     if (this.icon == null)
                     {
                        // set the default if it is not already
                        this.icon = iconName;
                     }
                     first = false;
                  }
                  
                  UIListItem item = new UIListItem();
                  item.setValue(iconName);
                  item.getAttributes().put("image", iconPath);
                  icons.add(item);
               }
            }
         }
      }
      
      // if we didn't find any icons display one default choice
      if (icons == null)
      {
         icons = new ArrayList<UIListItem>(1);
         this.icon = SPACE_ICON_DEFAULT;
         
         UIListItem item = new UIListItem();
         item.setValue("space-icon-default");
         item.getAttributes().put("image", "/images/icons/space-icon-default.gif");
         icons.add(item);
      }
      
      return icons;
   }
   
   /**
    * Initialises the wizard
    */
   public void init()
   {
      super.init();
      
      // reset all variables
      this.spaceType = ContentModel.TYPE_FOLDER.toString();
      this.icon = null;
      this.name = null;
      this.description = "";
   }
   
   @Override
   public String finish()
   {
      String outcome = DIALOG_CLOSE;
      
      UserTransaction tx = null;
   
      try
      {
         FacesContext context = FacesContext.getCurrentInstance();
         tx = Repository.getUserTransaction(context);
         tx.begin();
         
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
         
         FileInfo fileInfo = fileFolderService.create(
               parentNodeRef,
               this.name,
               Repository.resolveToQName(this.spaceType));
         NodeRef nodeRef = fileInfo.getNodeRef();
         
         if (logger.isDebugEnabled())
            logger.debug("Created folder node with name: " + this.name);

         // apply the uifacets aspect - icon, title and description props
         Map<QName, Serializable> uiFacetsProps = new HashMap<QName, Serializable>(5);
         uiFacetsProps.put(ContentModel.PROP_ICON, this.icon);
         uiFacetsProps.put(ContentModel.PROP_TITLE, this.name);
         uiFacetsProps.put(ContentModel.PROP_DESCRIPTION, this.description);
         this.nodeService.addAspect(nodeRef, ContentModel.ASPECT_UIFACETS, uiFacetsProps);
         
         if (logger.isDebugEnabled())
            logger.debug("Added uifacets aspect with properties: " + uiFacetsProps);
         
         // commit the transaction
         tx.commit();
      }
      catch (FileExistsException e)
      {
         // rollback the transaction
         try { if (tx != null) {tx.rollback();} } catch (Exception ex) {}
         // print status message  
         String statusMsg = MessageFormat.format(
               Application.getMessage(
                     FacesContext.getCurrentInstance(), "error_exists"), 
                     e.getExisting().getName());
         Utils.addErrorMessage(statusMsg);
         // no outcome
         outcome = null;
      }
      catch (Throwable e)
      {
         // rollback the transaction
         try { if (tx != null) {tx.rollback();} } catch (Exception ex) {}
         Utils.addErrorMessage(MessageFormat.format(Application.getMessage(
               FacesContext.getCurrentInstance(), ERROR), e.getMessage()), e);
         outcome = null;
      }
      
      return outcome;
   }
}
