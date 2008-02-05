package org.alfresco.web.bean.content;

import java.text.MessageFormat;
import java.util.Map;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.workflow.WorkflowModel;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.InvalidNodeRefException;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.workflow.WorkflowTask;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.web.app.Application;
import org.alfresco.web.app.context.UIContextService;
import org.alfresco.web.app.servlet.DownloadContentServlet;

import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;

import org.alfresco.web.bean.FileUploadBean;
import org.alfresco.web.bean.coci.CCProperties;
import org.alfresco.web.bean.coci.CheckinCheckoutDialog;
import org.alfresco.web.bean.dialog.BaseDialogBean;
import org.alfresco.web.bean.repository.Node;
import org.alfresco.web.bean.repository.Repository;
import org.alfresco.web.ui.common.Utils;
import org.alfresco.web.ui.common.component.UIActionLink;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class EditOfflineDialog extends BaseDialogBean
{
    
    private static final String CLOSE = "close";
    public static final String MSG_ERROR_CHECKOUT = "error_checkout";
    private static Log logger = LogFactory.getLog(EditOfflineDialog.class);    
    protected CCProperties property;    
    
    public CCProperties getProperty()
    {
        return property;
    }

    public void setProperty(CCProperties property)
    {
        this.property = property;
    }

    @Override
    public String getContainerTitle()
    {      
       return "Download of '" + getProperty().getDocument().getName() + "' for offline editing.";
    }
    
    @Override
    protected String finishImpl(FacesContext context, String outcome) throws Exception
    {
        return outcome;
    }   
    
    @Override
    public String getCancelButtonLabel()
    {
        return Application.getMessage(FacesContext.getCurrentInstance(), CLOSE);       
    }
        
    public void setupContentAction(ActionEvent event)
    {
       UIActionLink link = (UIActionLink) event.getComponent();
       Map<String, String> params = link.getParameterMap();
       String id = params.get("id");
       if (id != null && id.length() != 0)
       {
          setupContentDocument(id);
          checkoutFile();
       } 
       else
       {
          property.setDocument(null);
       }
       
       resetState();
    }
    
    private Node setupContentDocument(String id)
    {
       if (logger.isDebugEnabled())
          logger.debug("Setup for action, setting current document to: " + id);

       Node node = null;
       
       try
       {
          // create the node ref, then our node representation
          NodeRef ref = new NodeRef(Repository.getStoreRef(), id);
          node = new Node(ref);
          
          // create content URL to the content download servlet with ID and expected filename
          // the myfile part will be ignored by the servlet but gives the browser a hint
          String url = DownloadContentServlet.generateDownloadURL(ref, node.getName());
          node.getProperties().put("url", url);
          node.getProperties().put("workingCopy", node.hasAspect(ContentModel.ASPECT_WORKING_COPY));
          node.getProperties().put("fileType32", Utils.getFileTypeImage(node.getName(), false)); 
          
          // remember the document
          property.setDocument(node);
          
          // refresh the UI, calling this method now is fine as it basically makes sure certain
          // beans clear the state - so when we finish here other beans will have been reset
          UIContextService.getInstance(FacesContext.getCurrentInstance()).notifyBeans();
       }
       catch (InvalidNodeRefException refErr)
       {
          Utils.addErrorMessage(MessageFormat.format(Application.getMessage(
                FacesContext.getCurrentInstance(), Repository.ERROR_NODEREF), new Object[] {id}) );
       }
       
       return node;
    }
    
    public  void resetState()
    {
       // delete the temporary file we uploaded earlier
       if (property.getFile() != null)
       {
          property.getFile().delete();
       }
       
       property.setFile(null);
       property.setFileName(null);
       property.setKeepCheckedOut(false);
       property.setMinorChange(true);
       property.setCopyLocation(CCProperties.COPYLOCATION_CURRENT);
       property.setVersionNotes("");
       property.setSelectedSpaceId(null);
       property.setWorkflowAction(false);
       property.setWorkflowTaskId(null);
       
       // remove the file upload bean from the session
       FacesContext ctx = FacesContext.getCurrentInstance();
       ctx.getExternalContext().getSessionMap().remove(FileUploadBean.FILE_UPLOAD_BEAN_NAME);
    }
    
    public void checkoutFile()
    {        
        final Node node = property.getDocument();
        if (node != null)
        {
            try
            {
                if (logger.isDebugEnabled())
                    logger.debug("Trying to checkout content node Id: " + node.getId());

                // checkout the node content to create a working copy
                if (logger.isDebugEnabled())
                {
                    logger.debug("Checkout copy location: " + property.getCopyLocation());
                    logger.debug("Selected Space Id: " + property.getSelectedSpaceId());
                }
                NodeRef workingCopyRef = null;
                if (property.getCopyLocation().equals(CCProperties.COPYLOCATION_OTHER) && property.getSelectedSpaceId() != null)
                {
                    // checkout to a arbituary parent Space
                    NodeRef destRef = property.getSelectedSpaceId();

                    ChildAssociationRef childAssocRef = nodeService.getPrimaryParent(destRef);
                    workingCopyRef = property.getVersionOperationsService().checkout(node.getNodeRef(), destRef, ContentModel.ASSOC_CONTAINS, childAssocRef.getQName());
                }
                else
                {
                    // checkout the content to the current space
                    workingCopyRef = property.getVersionOperationsService().checkout(node.getNodeRef());

                    // if this is a workflow action and there is a task id
                    // present we need
                    // to also link the working copy to the workflow package so
                    // it appears
                    // in the resources panel in the manage task dialog
                    if (property.isWorkflowAction() && property.getWorkflowTaskId() != null && (property.getWorkflowTaskId().equals("null") == false))
                    {
                        WorkflowTask task = property.getWorkflowService().getTaskById(property.getWorkflowTaskId());
                        if (task != null)
                        {
                            NodeRef workflowPackage = (NodeRef) task.properties.get(WorkflowModel.ASSOC_PACKAGE);
                            if (workflowPackage != null)
                            {
                                nodeService.addChild(workflowPackage, workingCopyRef, ContentModel.ASSOC_CONTAINS, QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, QName
                                        .createValidLocalName((String) nodeService.getProperty(workingCopyRef, ContentModel.PROP_NAME))));

                                if (logger.isDebugEnabled())
                                    logger.debug("Added working copy to workflow package: " + workflowPackage);
                            }
                        }
                    }
                }

                // set the working copy Node instance
                Node workingCopy = new Node(workingCopyRef);
                property.setWorkingDocument(workingCopy);

                // create content URL to the content download servlet with ID
                // and expected filename
                // the myfile part will be ignored by the servlet but gives the
                // browser a hint
                String url = DownloadContentServlet.generateDownloadURL(workingCopyRef, workingCopy.getName());

                workingCopy.getProperties().put("url", url);
                workingCopy.getProperties().put("fileType32", Utils.getFileTypeImage(workingCopy.getName(), false));
               
            }
            catch (Throwable err)
            {
                Utils.addErrorMessage(Application.getMessage(FacesContext.getCurrentInstance(), MSG_ERROR_CHECKOUT) + err.getMessage(), err);
            }
        }
        else
        {
            logger.warn("WARNING: checkoutFile called without a current Document!");
        }        
    }

}
