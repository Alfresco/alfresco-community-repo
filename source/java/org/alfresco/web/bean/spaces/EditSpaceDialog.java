package org.alfresco.web.bean.spaces;

import java.io.Serializable;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.faces.context.FacesContext;

import org.alfresco.config.Config;
import org.alfresco.config.ConfigElement;
import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.dictionary.DataTypeDefinition;
import org.alfresco.service.cmr.dictionary.PropertyDefinition;
import org.alfresco.service.cmr.model.FileExistsException;
import org.alfresco.service.cmr.repository.AssociationRef;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;
import org.alfresco.web.app.Application;
import org.alfresco.web.bean.dialog.BaseDialogBean;
import org.alfresco.web.bean.repository.Node;
import org.alfresco.web.bean.repository.Repository;
import org.alfresco.web.ui.common.component.UIListItem;

/**
 * Dialog bean to edit an existing space.
 * 
 * @author gavinc
 */
public class EditSpaceDialog extends BaseDialogBean
{
   protected Node editableNode;
   
   @Override
   public void init()
   {
      super.init();
      
      // setup the space being edited
      this.editableNode = this.browseBean.getActionSpace();
   }
   
   /**
    * Returns the editable node
    * 
    * @return The editable node
    */
   public Node getEditableNode()
   {
      return this.editableNode;
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
      List<UIListItem> icons = null;
      
      QName type = QName.createQName(this.editableNode.getType().toString());
      String typePrefixForm = type.toPrefixString(this.namespaceService);
      
      Config config = Application.getConfigService(FacesContext.getCurrentInstance()).
            getConfig(typePrefixForm + " icons");
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
                     icons = new ArrayList<UIListItem>(iconsCfg.getChildCount());
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
         
         UIListItem item = new UIListItem();
         item.setValue("space-icon-default");
         item.getAttributes().put("image", "/images/icons/space-icon-default.gif");
         icons.add(item);
      }
      
      return icons;
   }

   @Override
   protected String finishImpl(FacesContext context, String outcome) throws Exception
   {
      // update the existing node in the repository
      NodeRef nodeRef = this.editableNode.getNodeRef();
      Map<String, Object> editedProps = this.editableNode.getProperties();
      
      // handle the name property separately, perform a rename in case it changed
      String name = (String)editedProps.get(ContentModel.PROP_NAME);
      if (name != null)
      {
         this.fileFolderService.rename(nodeRef, name);
      }
      
      // get the current set of properties from the repository
      Map<QName, Serializable> repoProps = this.nodeService.getProperties(nodeRef);
      
      // overwrite the current properties with the edited ones
      Iterator<String> iterProps = editedProps.keySet().iterator();
      while (iterProps.hasNext())
      {
         String propName = iterProps.next();
         QName qname = QName.createQName(propName);
         
         // make sure the property is represented correctly
         Serializable propValue = (Serializable)editedProps.get(propName);
         
         // check for empty strings when using number types, set to null in this case
         if ((propValue != null) && (propValue instanceof String) && 
             (propValue.toString().length() == 0))
         {
            PropertyDefinition propDef = this.dictionaryService.getProperty(qname);
            if (propDef != null)
            {
               if (propDef.getDataType().getName().equals(DataTypeDefinition.DOUBLE) || 
                   propDef.getDataType().getName().equals(DataTypeDefinition.FLOAT) ||
                   propDef.getDataType().getName().equals(DataTypeDefinition.INT) || 
                   propDef.getDataType().getName().equals(DataTypeDefinition.LONG))
               {
                  propValue = null;
               }
            }
         }
         
         repoProps.put(qname, propValue);
      }
      
      // send the properties back to the repository
      this.nodeService.setProperties(nodeRef, repoProps);
      
      // we also need to persist any association changes that may have been made
      
      // add any associations added in the UI
      Map<String, Map<String, AssociationRef>> addedAssocs = this.editableNode.getAddedAssociations();
      for (Map<String, AssociationRef> typedAssoc : addedAssocs.values())
      {
         for (AssociationRef assoc : typedAssoc.values())
         {
            this.nodeService.createAssociation(assoc.getSourceRef(), assoc.getTargetRef(), assoc.getTypeQName());
         }
      }
      
      // remove any association removed in the UI
      Map<String, Map<String, AssociationRef>> removedAssocs = this.editableNode.getRemovedAssociations();
      for (Map<String, AssociationRef> typedAssoc : removedAssocs.values())
      {
         for (AssociationRef assoc : typedAssoc.values())
         {
            this.nodeService.removeAssociation(assoc.getSourceRef(), assoc.getTargetRef(), assoc.getTypeQName());
         }
      }
      
      // add any child associations added in the UI
      Map<String, Map<String, ChildAssociationRef>> addedChildAssocs = this.editableNode.getAddedChildAssociations();
      for (Map<String, ChildAssociationRef> typedAssoc : addedChildAssocs.values())
      {
         for (ChildAssociationRef assoc : typedAssoc.values())
         {
            this.nodeService.addChild(assoc.getParentRef(), assoc.getChildRef(), assoc.getTypeQName(), assoc.getTypeQName());
         }
      }
      
      // remove any child association removed in the UI
      Map<String, Map<String, ChildAssociationRef>> removedChildAssocs = this.editableNode.getRemovedChildAssociations();
      for (Map<String, ChildAssociationRef> typedAssoc : removedChildAssocs.values())
      {
         for (ChildAssociationRef assoc : typedAssoc.values())
         {
            this.nodeService.removeChild(assoc.getParentRef(), assoc.getChildRef());
         }
      }
            
      return outcome;
   }
   
   @Override
   protected String doPostCommitProcessing(FacesContext context, String outcome)
   {
      this.editableNode.reset();
      
      return outcome;
   }

   /**
    * Formats the error message to display if an error occurs during finish processing
    * 
    * @param The exception
    * @return The formatted message
    */
   @Override
   protected String formatErrorMessage(Throwable exception)
   {
      if (exception instanceof FileExistsException)
      {
         return MessageFormat.format(Application.getMessage(
               FacesContext.getCurrentInstance(), Repository.ERROR_EXISTS), 
               ((FileExistsException)exception).getExisting().getName());
      }
      else
      {
         return MessageFormat.format(Application.getMessage(
               FacesContext.getCurrentInstance(), "error_space"), 
               exception.getMessage());
      }
   }
}
