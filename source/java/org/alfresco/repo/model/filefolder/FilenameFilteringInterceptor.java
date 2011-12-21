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

import java.util.Iterator;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.model.filefolder.HiddenAspect.Visibility;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.service.cmr.model.FileInfo;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.Path;
import org.alfresco.service.cmr.repository.Path.Element;
import org.alfresco.service.cmr.security.PermissionService;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.FileFilterMode;
import org.alfresco.util.FileFilterMode.Client;
import org.alfresco.util.FileFilterMode.Mode;
import org.alfresco.util.PatternFilter;
import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * An interceptor that intercepts FileFolderService methods, ensuring system, temporary and hidden files
 * and paths are marked with the correct aspects.
 * 
 */
public class FilenameFilteringInterceptor implements MethodInterceptor
{
    private static Log logger = LogFactory.getLog(FilenameFilteringInterceptor.class);

    private NodeService nodeService;
    private PermissionService permissionService;

    private PatternFilter temporaryFiles;
    private PatternFilter systemPaths;
    private HiddenAspect hiddenAspect;

    public FilenameFilteringInterceptor()
    {
    }
    
	/**
     * A list of regular expressions that represent patterns of temporary files.
     * 
     */
	public void setTemporaryFiles(PatternFilter temporaryFiles)
	{
		this.temporaryFiles = temporaryFiles;
	}
    
	public void setHiddenAspect(HiddenAspect hiddenAspect)
    {
        this.hiddenAspect = hiddenAspect;
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
	
	public Client getClient()
    {
        return FileFilterMode.getClient();
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
    
    private int getSystemFileVisibilityMask()
    {
        int mask = 0;
        mask |= hiddenAspect.getClientVisibilityMask(Client.cifs, Visibility.HiddenAttribute);
        mask |= hiddenAspect.getClientVisibilityMask(Client.webdav, Visibility.Visible);
        mask |= hiddenAspect.getClientVisibilityMask(Client.nfs, Visibility.Visible);
        mask |= hiddenAspect.getClientVisibilityMask(Client.ftp, Visibility.Visible);
        return mask;
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
	            	FileInfoImpl fileInfo = (FileInfoImpl)ret;
		            permissionService.setPermission(fileInfo.getNodeRef(), PermissionService.ALL_AUTHORITIES, PermissionService.FULL_CONTROL, true);	            
		            
		            // it's always marked temporary and hidden
		            checkTemporaryAspect(true, fileInfo);
		            hiddenAspect.hideNode(fileInfo, getSystemFileVisibilityMask());
	            }
	            else
	            {
	            	// it's not a temporary file/folder, create as normal
	            	ret = invocation.proceed();
	            	
	            	FileInfoImpl fileInfo = (FileInfoImpl)ret;

	            	if(isSystemPath(nodeRef, filename))
	            	{
	                    // it's on a system path, check whether temporary, hidden and noindex aspects need to be applied
	            		checkTemporaryAspect(true, fileInfo);
	            		hiddenAspect.hideNode(fileInfo, getSystemFileVisibilityMask());
	            	}
	            	else
	            	{
	            	    // check whether it's a temporary or hidden file
			            checkTemporaryAspect(temporaryFiles.isFiltered(filename), (FileInfo)ret);
			            hiddenAspect.checkHidden(fileInfo);
	            	}
	            }
            }
            else
            {
                ret = invocation.proceed();

                FileInfoImpl fileInfo = (FileInfoImpl)ret;

	            checkTemporaryAspect(temporaryFiles.isFiltered(filename), fileInfo);
            }
        }
        else if (methodName.startsWith("rename") ||
        		methodName.startsWith("move") ||
        		methodName.startsWith("copy"))
        {
            ret = invocation.proceed();

            FileInfoImpl fileInfo = (FileInfoImpl) ret;
            String filename = fileInfo.getName();

            if (logger.isDebugEnabled())
            {
                logger.debug("Checking filename returned by " + methodName + ": " + filename);
            }

            // check against all the regular expressions
            checkTemporaryAspect(temporaryFiles.isFiltered(filename), fileInfo);
            if(getMode() == Mode.ENHANCED)
            {
                hiddenAspect.checkHidden(fileInfo);
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

