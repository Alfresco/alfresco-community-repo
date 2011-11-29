/*
 * Copyright (C) 2005-2011 Alfresco Software Limited.
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
 * along with Alfresco. If not, see <http://www.gnu.org/licenses/>. */
package org.alfresco.repo.model.filefolder;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.alfresco.jlan.server.FileFilterMode;
import org.alfresco.jlan.server.FileFilterMode.Mode;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.service.cmr.model.FileInfo;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.Path;
import org.alfresco.service.cmr.repository.Path.Element;
import org.alfresco.service.cmr.security.PermissionService;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.PatternFilter;
import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * An interceptor that intercepts the FileFolderService create method, creating files and folders as the system user
 * and applying temporary and hidden aspects when they match a pattern.
 * 
 */
public class FilenameFilteringInterceptor implements MethodInterceptor
{
    private static Log logger = LogFactory.getLog(FilenameFilteringInterceptor.class);

    private NodeService nodeService;
    private PermissionService permissionService;

    private PatternFilter temporaryFiles;
    private PatternFilter hiddenFiles;
    private PatternFilter systemPaths;

    public FilenameFilteringInterceptor()
    {
    }
    
	/**
     * A list of regular expressions that represent patterns of hidden files.
     * 
     */
    public void setHiddenFiles(PatternFilter hiddenFiles)
    {
		this.hiddenFiles = hiddenFiles;
	}

	/**
     * A list of regular expressions that represent patterns of temporary files.
     * 
     */
	public void setTemporaryFiles(PatternFilter temporaryFiles)
	{
		this.temporaryFiles = temporaryFiles;
	}
    
	/**
     * A list of regular expressions that represent patterns of system paths.
     * 
     */
	public void setSystemPaths(PatternFilter systemPaths)
	{
		this.systemPaths = systemPaths;
	}
	
	public Mode getMode()
	{
		return FileFilterMode.getMode();
	}

    /**
     * @param nodeService the service to use to apply the <b>sys:temporary</b> aspect
     */
    public void setNodeService(NodeService nodeService)
    {
        this.nodeService = nodeService;
    }
	
	public void setPermissionService(PermissionService permissionService)
	{
		this.permissionService = permissionService;
	}

    private void checkTemporaryAspect(boolean isTemporary, FileInfo fileInfo)
    {
        NodeRef nodeRef = fileInfo.getNodeRef();

        if(isTemporary)
        {
            // it matched, so apply the temporary and hidden aspects
            nodeService.addAspect(nodeRef, ContentModel.ASPECT_TEMPORARY, null);

            if (logger.isDebugEnabled())
            {
                logger.debug("Applied temporary marker: " + fileInfo);
            }
        }
        else
        {
            // If there was NOT a match then the file should not be marked as temporary
            // after any of the operations in question.
            if (nodeService.hasAspect(nodeRef, ContentModel.ASPECT_TEMPORARY))
            {
                // Remove the aspect
                nodeService.removeAspect(nodeRef, ContentModel.ASPECT_TEMPORARY);
                
                if (logger.isDebugEnabled())
                {
                    logger.debug("Removed temporary marker: " + fileInfo);
                }
            }
        }
    }        
     
    private void checkHiddenAspect(boolean isHidden, FileInfo fileInfo)
    {
        NodeRef nodeRef = fileInfo.getNodeRef();

        if (isHidden)
        {
        	nodeService.addAspect(nodeRef, ContentModel.ASPECT_HIDDEN, null);

        	Map<QName, Serializable> props = new HashMap<QName, Serializable>(2);
        	props.put(ContentModel.PROP_IS_INDEXED, Boolean.FALSE);
            props.put(ContentModel.PROP_IS_CONTENT_INDEXED, Boolean.FALSE);
            nodeService.addAspect(nodeRef, ContentModel.ASPECT_INDEX_CONTROL, props);

            if (logger.isDebugEnabled())
            {
                logger.debug("Applied hidden marker: " + fileInfo);
            }
        }
        else
        {
        	if(nodeService.hasAspect(nodeRef, ContentModel.ASPECT_HIDDEN))
        	{
                // Remove the aspect
                nodeService.removeAspect(nodeRef, ContentModel.ASPECT_HIDDEN);
                
                if (logger.isDebugEnabled())
                {
                    logger.debug("Removed hidden marker: " + fileInfo);
                }
        	}
        }
    }

    private void addIndexedAspect(FileInfo fileInfo)
    {
        NodeRef nodeRef = fileInfo.getNodeRef();

        Map<QName, Serializable> props = new HashMap<QName, Serializable>(2);
        props.put(ContentModel.PROP_IS_INDEXED, Boolean.FALSE);
        props.put(ContentModel.PROP_IS_CONTENT_INDEXED, Boolean.FALSE);
        nodeService.addAspect(nodeRef, ContentModel.ASPECT_INDEX_CONTROL, props);
    }

    private Object runAsSystem(MethodInvocation invocation) throws Throwable
    {
    	Object ret = null;

        // We're creating in enhanced mode and have a matching filename or path. Switch to
        // the system user to do the operation.

        AuthenticationUtil.pushAuthentication();
        try
        {
        	AuthenticationUtil.setRunAsUserSystem();
            ret = invocation.proceed();
        }
        finally
        {
            AuthenticationUtil.popAuthentication();
        }
        
        return ret;
    }

    private boolean isSystemPath(NodeRef parentNodeRef, String filename)
    {
    	boolean ret = false;
        Path path = nodeService.getPath(parentNodeRef);

        Iterator<Element> it = path.iterator();
        while(it.hasNext())
        {
        	Path.ChildAssocElement elem = (Path.ChildAssocElement)it.next();
        	QName qname = elem.getRef().getQName();
        	if(qname != null && systemPaths.isFiltered(qname.getLocalName()))
        	{
        		ret = true;
        		break;
        	}
        }

        return ret;
    }
    
    public Object invoke(final MethodInvocation invocation) throws Throwable
    {
        // execute and get the result
        String methodName = invocation.getMethod().getName();
        Object ret = null;

        // do the invocation
        if (methodName.startsWith("create"))
        {
            NodeRef nodeRef  = (NodeRef)invocation.getArguments()[0];
            String filename = (String)invocation.getArguments()[1];

        	if(getMode() == Mode.ENHANCED)
        	{
	            if(systemPaths.isFiltered(filename))
	            {
	            	// it's a system file/folder, create as system and allow full control to all authorities
	            	ret = runAsSystem(invocation);
		            permissionService.setPermission(((FileInfo)ret).getNodeRef(), PermissionService.ALL_AUTHORITIES, PermissionService.FULL_CONTROL, true);	            
		            
		            // it's always marked temporary and hidden
		            checkTemporaryAspect(true, (FileInfo)ret);
		            checkHiddenAspect(true, (FileInfo)ret);
		            addIndexedAspect((FileInfo)ret);
	            }
	            else
	            {
	            	// it's not a temporary file/folder, create as normal
	            	ret = invocation.proceed();

		            // if it's on a temporary path check whether temporary and hidden aspects need to be applied
	            	if(isSystemPath(nodeRef, filename))
	            	{
	            		checkTemporaryAspect(true, (FileInfo)ret);
	            		checkHiddenAspect(true, (FileInfo)ret);
	                    addIndexedAspect((FileInfo)ret);
	            	}
	            	else
	            	{
			            checkTemporaryAspect(temporaryFiles.isFiltered(filename), (FileInfo)ret);
			            boolean isHidden = hiddenFiles.isFiltered(filename);
	            		checkHiddenAspect(isHidden, (FileInfo)ret);
	            		if(isHidden)
	            		{
	            		    addIndexedAspect((FileInfo)ret);
	            		}
	            	}
	            }
            }
            else
            {
                ret = invocation.proceed();

	            checkTemporaryAspect(temporaryFiles.isFiltered(filename), (FileInfo)ret);
            }
        }
        else if (methodName.startsWith("rename") ||
        		methodName.startsWith("move") ||
        		methodName.startsWith("copy"))
        {
            ret = invocation.proceed();

            FileInfo fileInfo = (FileInfo) ret;
            String filename = fileInfo.getName();
            
            if (logger.isDebugEnabled())
            {
                logger.debug("Checking filename returned by " + methodName + ": " + filename);
            }
            
            // check against all the regular expressions
            checkTemporaryAspect(temporaryFiles.isFiltered(filename), fileInfo);
            
            boolean isHidden = hiddenFiles.isFiltered(filename);
            checkHiddenAspect(isHidden, fileInfo);
            if(isHidden)
            {
                addIndexedAspect((FileInfo)ret);
            }
        }
        else
        {
            ret = invocation.proceed();
        }

        // done
        return ret;
    }
}

