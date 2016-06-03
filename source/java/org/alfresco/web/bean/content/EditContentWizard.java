package org.alfresco.web.bean.content;

import java.io.File;
import java.util.Map;

import javax.faces.context.FacesContext;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.content.MimetypeMap;
import org.alfresco.service.cmr.repository.ContentWriter;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.web.bean.repository.Node;

/**
 * Bean implementation for the "Edit Content Wizard" dialog
 */
public class EditContentWizard extends CreateContentWizard
{
   private static final long serialVersionUID = 1640754719164511019L;
   
   private NodeRef nodeRef;

   // ------------------------------------------------------------------------------
   // Wizard implementation
   
   @Override
   public void init(final Map<String, String> parameters)
   {
      super.init(parameters);
      Node node = this.navigator.getDispatchContextNode();
      if (node == null)
      {
         throw new IllegalArgumentException("Edit Form wizard requires action node context.");
      }
      this.nodeRef = node.getNodeRef();

      this.content = this.getContentService().getReader(nodeRef, ContentModel.PROP_CONTENT).getContentString();
      
      this.fileName = (String)getNodeService().getProperty(nodeRef, ContentModel.PROP_NAME); // getName() ...
      this.mimeType = MimetypeMap.MIMETYPE_XML;
   }

   @Override
   public String back()
   {
      return super.back();
   }
   
   @Override
   protected void saveContent(File fileContent, String strContent) throws Exception
   {
      ContentWriter writer = getContentService().getWriter(nodeRef, ContentModel.PROP_CONTENT, true);
      writer.putContent(strContent);
   }
   
   @Override
   protected String doPostCommitProcessing(FacesContext context, String outcome)
   {
      return outcome;
   }
}
