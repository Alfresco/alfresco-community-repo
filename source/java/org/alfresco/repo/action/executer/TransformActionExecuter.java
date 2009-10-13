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
package org.alfresco.repo.action.executer;

import java.util.List;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.action.ParameterDefinitionImpl;
import org.alfresco.service.cmr.action.Action;
import org.alfresco.service.cmr.action.ParameterDefinition;
import org.alfresco.service.cmr.dictionary.DataTypeDefinition;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.service.cmr.repository.ContentService;
import org.alfresco.service.cmr.repository.ContentWriter;
import org.alfresco.service.cmr.repository.CopyService;
import org.alfresco.service.cmr.repository.MimetypeService;
import org.alfresco.service.cmr.repository.NoTransformerException;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.TransformationOptions;
import org.alfresco.service.cmr.rule.RuleServiceException;
import org.alfresco.service.namespace.QName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Transfor action executer
 * 
 * @author Roy Wetherall
 */
public class TransformActionExecuter extends ActionExecuterAbstractBase 
{    

    /** Error messages */
    public static final String ERR_OVERWRITE = "Unable to overwrite copy because more than one have been found.";
    private static final String CONTENT_READER_NOT_FOUND_MESSAGE = "Can not find Content Reader for document. Operation can't be performed";
    private static final String TRANSFORMING_ERROR_MESSAGE = "Some error occurred during document transforming. Error message: ";

    private static final String TRANSFORMER_NOT_EXISTS_MESSAGE_PATTERN = "Transformer for '%s' source mime type and '%s' target mime type was not found. Operation can't be performed";
    
    /**
     * The logger
     */
    private static Log logger = LogFactory.getLog(TransformActionExecuter.class); 
    
    /**
     * Action constants
     */
    public static final String NAME = "transform";
    public static final String PARAM_MIME_TYPE = "mime-type";
    public static final String PARAM_DESTINATION_FOLDER = "destination-folder";
    public static final String PARAM_ASSOC_TYPE_QNAME = "assoc-type";
    public static final String PARAM_ASSOC_QNAME = "assoc-name";
    public static final String PARAM_OVERWRITE_COPY = "overwrite-copy";
    
    /**
     * Injected services
     */
    private DictionaryService dictionaryService;
    private NodeService nodeService;
    private ContentService contentService;
    private CopyService copyService;
    private MimetypeService mimetypeService;
    
    /**
     * Set the mime type service
     * 
     * @param mimetypeService  the mime type service
     */
    public void setMimetypeService(MimetypeService mimetypeService) 
    {
        this.mimetypeService = mimetypeService;
    }
    
    /**
     * Set the node service
     * 
     * @param nodeService  set the node service
     */
    public void setNodeService(NodeService nodeService) 
    {
        this.nodeService = nodeService;
    }
    
    /**
     * Set the dictionary service
     * 
     * @param dictionaryService  the dictionary service
     */
    public void setDictionaryService(DictionaryService dictionaryService) 
    {
        this.dictionaryService = dictionaryService;
    }
    
    /**
     * Set the content service
     * 
     * @param contentService  the content service
     */
    public void setContentService(ContentService contentService) 
    {
        this.contentService = contentService;
    }
    
    /**
     * Set the copy service
     * 
     * @param copyService  the copy service
     */
    public void setCopyService(CopyService copyService) 
    {
        this.copyService = copyService;
    }
    
    /**
     * Add parameter definitions
     */
    @Override
    protected void addParameterDefinitions(List<ParameterDefinition> paramList) 
    {
        paramList.add(new ParameterDefinitionImpl(PARAM_MIME_TYPE, DataTypeDefinition.TEXT, true, getParamDisplayLabel(PARAM_MIME_TYPE)));
        paramList.add(new ParameterDefinitionImpl(PARAM_DESTINATION_FOLDER, DataTypeDefinition.NODE_REF, true, getParamDisplayLabel(PARAM_DESTINATION_FOLDER)));
        paramList.add(new ParameterDefinitionImpl(PARAM_ASSOC_TYPE_QNAME, DataTypeDefinition.QNAME, true, getParamDisplayLabel(PARAM_ASSOC_TYPE_QNAME)));
        paramList.add(new ParameterDefinitionImpl(PARAM_ASSOC_QNAME, DataTypeDefinition.QNAME, true, getParamDisplayLabel(PARAM_ASSOC_QNAME)));
        paramList.add(new ParameterDefinitionImpl(PARAM_OVERWRITE_COPY, DataTypeDefinition.BOOLEAN, false, getParamDisplayLabel(PARAM_OVERWRITE_COPY)));
    }

    /**
     * @see org.alfresco.repo.action.executer.ActionExecuterAbstractBase#executeImpl(org.alfresco.service.cmr.repository.NodeRef, org.alfresco.service.cmr.repository.NodeRef)
     */
    @Override
    protected void executeImpl(
            Action ruleAction,
            NodeRef actionedUponNodeRef) 
    {
        if (this.nodeService.exists(actionedUponNodeRef) == false)
        {
            // node doesn't exist - can't do anything
            return;
        }
        // First check that the node is a sub-type of content
        QName typeQName = this.nodeService.getType(actionedUponNodeRef);
        if (this.dictionaryService.isSubClass(typeQName, ContentModel.TYPE_CONTENT) == false)
        {
            // it is not content, so can't transform
            return;
        }
        
        // Get the mime type
        String mimeType = (String)ruleAction.getParameterValue(PARAM_MIME_TYPE);
        // Get the content reader
        ContentReader contentReader = this.contentService.getReader(actionedUponNodeRef, ContentModel.PROP_CONTENT);
        if (null == contentReader || !contentReader.exists())
        {
            throw new RuleServiceException(CONTENT_READER_NOT_FOUND_MESSAGE);
        }

        if (null == contentService.getTransformer(contentReader.getMimetype(), mimeType))
        {
            throw new RuleServiceException(String.format(TRANSFORMER_NOT_EXISTS_MESSAGE_PATTERN, contentReader.getMimetype(), mimeType));
        }
        
        // Get the details of the copy destination
        NodeRef destinationParent = (NodeRef)ruleAction.getParameterValue(PARAM_DESTINATION_FOLDER);
        QName destinationAssocTypeQName = (QName)ruleAction.getParameterValue(PARAM_ASSOC_TYPE_QNAME);
        QName destinationAssocQName = (QName)ruleAction.getParameterValue(PARAM_ASSOC_QNAME);
        
        // Get the overwirte value
        boolean overwrite = true;
        Boolean overwriteValue = (Boolean)ruleAction.getParameterValue(PARAM_OVERWRITE_COPY);
        if (overwriteValue != null)
        {
            overwrite = overwriteValue.booleanValue();
        }
        
        // Calculate the destination name
        String originalName = (String)nodeService.getProperty(actionedUponNodeRef, ContentModel.PROP_NAME);
        String newName = transformName(this.mimetypeService, originalName, mimeType, true);
        
        // Since we are overwriting we need to figure out whether the destination node exists
        NodeRef copyNodeRef = null;
        if (overwrite == true)
        {
            // Try and find copies of the actioned upon node reference
            List<NodeRef> copies = this.copyService.getCopies(actionedUponNodeRef);
            if (copies != null && copies.isEmpty() == false)
            {
                for (NodeRef copy : copies)
                {
                    // Ignore if the copy is a working copy
                    if (this.nodeService.hasAspect(copy, ContentModel.ASPECT_WORKING_COPY) == false)
                    {
                        // We can assume that we are looking for a node created by this action so the primary parent will
                        // match the destination folder and the name will be the same
                        NodeRef parent = this.nodeService.getPrimaryParent(copy).getParentRef();
                        String copyName = (String)this.nodeService.getProperty(copy, ContentModel.PROP_NAME);
                        if (parent.equals(destinationParent) == true && copyName.equals(newName) == true)
                        {
                            if (copyNodeRef == null)
                            {
                                copyNodeRef = copy;
                            }
                            else
                            {
                                throw new RuleServiceException(ERR_OVERWRITE);
                            }
                        }
                        
                    }
                }
            }
        }
        
        boolean newCopy = false;
        if (copyNodeRef == null)
        {
            // Copy the content node
            copyNodeRef = this.copyService.copy(
                    actionedUponNodeRef, 
                    destinationParent,
                    destinationAssocTypeQName,
                    destinationAssocQName,
                    false);
            newCopy = true;
        }         
        
        if (newCopy == true)
        {
            // Adjust the name of the copy
            nodeService.setProperty(copyNodeRef, ContentModel.PROP_NAME, newName);
            String originalTitle = (String)nodeService.getProperty(actionedUponNodeRef, ContentModel.PROP_TITLE);
            if (originalTitle != null && originalTitle.length() > 0)
            {
                String newTitle = transformName(this.mimetypeService, originalTitle, mimeType, false);
                nodeService.setProperty(copyNodeRef, ContentModel.PROP_TITLE, newTitle);
            }
        }
        
        // Only do the transformation if some content is available
        if (contentReader != null)
        {
            // get the writer and set it up
            ContentWriter contentWriter = this.contentService.getWriter(copyNodeRef, ContentModel.PROP_CONTENT, true);
            contentWriter.setMimetype(mimeType);                        // new mimetype
            contentWriter.setEncoding(contentReader.getEncoding());     // original encoding

            // Try and transform the content - failures are caught and allowed to fail silently.
            // This is unique to this action, and is essentially a broken pattern.
            // Clients should rather get the exception and then decide to replay with rules/actions turned off or not.
            // TODO: Check failure patterns for actions.
            try
            {
                doTransform(ruleAction, actionedUponNodeRef, contentReader, copyNodeRef, contentWriter);
            }
            catch(NoTransformerException e)
            {
                if (logger.isDebugEnabled())
                {
                    logger.debug("No transformer found to execute rule: \n" +
                            "   reader: " + contentReader + "\n" +
                            "   writer: " + contentWriter + "\n" +
                            "   action: " + this);
                }             
                throw new RuleServiceException(TRANSFORMING_ERROR_MESSAGE + e.getMessage());
            }
        }
    }    
    
    /**
     * Executed in a new transaction so that failures don't cause the entire transaction to rollback.
     */
    protected void doTransform( Action ruleAction, 
                                NodeRef sourceNodeRef, ContentReader contentReader, 
                                NodeRef destinationNodeRef, ContentWriter contentWriter)    

    {
        // Transformation options
        TransformationOptions options = new TransformationOptions(
                sourceNodeRef, ContentModel.PROP_NAME, destinationNodeRef, ContentModel.PROP_NAME);          
        
        // try to pre-empt the lack of a transformer        
        if (this.contentService.isTransformable(contentReader, contentWriter, options) == false)
        {
            throw new NoTransformerException(contentReader.getMimetype(), contentWriter.getMimetype());
        }
        
        // transform
        this.contentService.transform(contentReader, contentWriter, options);
    }
    
    /**
     * Transform name from original extension to new extension
     * 
     * @param original
     * @param newMimetype
     * 
     * @return name with new extension as appropriate for the mimetype
     */
    public static String transformName(MimetypeService mimetypeService, String original, String newMimetype, boolean alwaysAdd)
    {
        // get the current extension
        int dotIndex = original.lastIndexOf('.');
        StringBuilder sb = new StringBuilder(original.length());
        if (dotIndex > -1)
        {
            // we found it
            sb.append(original.substring(0, dotIndex));
            
            // add the new extension
            String newExtension = mimetypeService.getExtension(newMimetype);
            sb.append('.').append(newExtension);
        }
        else
        {
            // no extension so dont add a new one
            sb.append(original);
            
            if (alwaysAdd == true)
            {               
                // add the new extension
                String newExtension = mimetypeService.getExtension(newMimetype);
                sb.append('.').append(newExtension);
            }
        }
        // done
        return sb.toString();
    }
    
}
