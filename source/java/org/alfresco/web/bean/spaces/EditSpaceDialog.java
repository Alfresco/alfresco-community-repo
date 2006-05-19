package org.alfresco.web.bean.spaces;

import java.io.Serializable;
import java.util.Iterator;
import java.util.Map;

import javax.faces.context.FacesContext;

import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.dictionary.DataTypeDefinition;
import org.alfresco.service.cmr.dictionary.PropertyDefinition;
import org.alfresco.service.cmr.repository.AssociationRef;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;
import org.alfresco.web.app.Application;
import org.alfresco.web.bean.repository.Node;

/**
 * Dialog bean to edit an existing space.
 * 
 * @author gavinc
 */
public class EditSpaceDialog extends CreateSpaceDialog
{
   protected Node editableNode;
   
   @Override
   public void init(Map<String, String> parameters)
   {
      super.init(parameters);
      
      // setup the space being edited
      this.editableNode = new Node(this.browseBean.getActionSpace().getNodeRef());
      this.spaceType = this.editableNode.getType().toString();
   }
   
   @Override
   public String getFinishButtonLabel()
   {
      return Application.getMessage(FacesContext.getCurrentInstance(), "ok");
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

   @Override
   protected String finishImpl(FacesContext context, String outcome) throws Exception
   {
      // update the existing node in the repository
      NodeRef nodeRef = this.browseBean.getActionSpace().getNodeRef();
      Map<String, Object> editedProps = this.editableNode.getProperties();
      
      // handle the name property separately, perform a rename in case it changed
      String name = (String)editedProps.get(ContentModel.PROP_NAME);
      if (name != null)
      {
         this.fileFolderService.rename(nodeRef, name);
      }
      
      // get the current set of properties from the repository
      Map<QName, Serializable> repoProps = this.nodeService.getProperties(nodeRef);
      
      // add the "uifacets" aspect if required, properties will get set below
      if (this.nodeService.hasAspect(nodeRef, ContentModel.ASPECT_UIFACETS) == false)
      {
         this.nodeService.addAspect(nodeRef, ContentModel.ASPECT_UIFACETS, null);
      }
      
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
      this.browseBean.getActionSpace().reset();
      
      return outcome;
   }
}
