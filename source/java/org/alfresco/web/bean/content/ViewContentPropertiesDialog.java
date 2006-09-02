package org.alfresco.web.bean.content;

import java.util.Map;

import javax.faces.context.FacesContext;

import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.repository.ContentData;
import org.alfresco.web.app.Application;
import org.alfresco.web.bean.dialog.BaseDialogBean;
import org.alfresco.web.bean.repository.Node;

/**
 * Bean implementation of the "View Content Properties" dialog.
 * 
 * @author gavinc
 */
public class ViewContentPropertiesDialog extends BaseDialogBean
{
   protected static final String TEMP_PROP_MIMETYPE = "mimetype";
   
   protected Node viewingNode;
   
   // ------------------------------------------------------------------------------
   // Dialog implementation
   
   @Override
   public void init(Map<String, String> parameters)
   {
      super.init(parameters);
      
      // setup the editable node
      this.viewingNode = new Node(this.browseBean.getDocument().getNodeRef());
      
      // special case for Mimetype - since this is a sub-property of the ContentData object
      // we must extract it so it can be edited in the client, then we check for it later
      // and create a new ContentData object to wrap it and it's associated URL
      ContentData content = (ContentData)this.viewingNode.getProperties().get(ContentModel.PROP_CONTENT);
      if (content != null)
      {
         this.viewingNode.getProperties().put(TEMP_PROP_MIMETYPE, content.getMimetype());
      }
      
      // add the specially handled 'size' property
      this.viewingNode.addPropertyResolver("size", this.browseBean.resolverSize);
   }
   
   @Override
   protected String finishImpl(FacesContext context, String outcome)
         throws Exception
   {
      // nothing to do as the finish button is not shown and the dialog is read only
         
      return outcome;
   }
   
   @Override
   public String getCancelButtonLabel()
   {
      return Application.getMessage(FacesContext.getCurrentInstance(), "close");
   }
   
   // ------------------------------------------------------------------------------
   // Bean getters and setters

   /**
    * Returns the node being viewed
    * 
    * @return The node being viewed
    */
   public Node getViewingNode()
   {
      return this.viewingNode;
   }
}
