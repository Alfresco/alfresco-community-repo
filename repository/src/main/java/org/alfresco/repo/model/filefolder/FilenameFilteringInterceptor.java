/*
 * #%L
 * Alfresco Repository
 * %%
 * Copyright (C) 2005 - 2016 Alfresco Software Limited
 * %%
 * This file is part of the Alfresco software. 
 * If the software was purchased under a paid Alfresco license, the terms of 
 * the paid license agreement will prevail.  Otherwise, the software is 
 * provided under the following open source license terms:
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
 * along with Alfresco. If not, see <http://www.gnu.org/licenses/>.
 * #L%
 */
package org.alfresco.repo.model.filefolder;

import java.util.Iterator;
import java.util.regex.Pattern;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.model.filefolder.HiddenAspect.Visibility;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.service.cmr.model.FileInfo;
import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.service.cmr.repository.ContentService;
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

/**
 * An interceptor that intercepts FileFolderService methods, ensuring system, temporary and hidden files and paths are marked with the correct aspects.
 * 
 * @author alex.mukha
 */
public class FilenameFilteringInterceptor implements MethodInterceptor
{
    private static Log logger = LogFactory.getLog(FilenameFilteringInterceptor.class);

    private static final String XLSX_MIMETYPE = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";

    private static final String MACOS_TEMPORARY_FILE_NAME_PREFIX = "._";
    private static final Pattern XSL_MACOS_TEMPORARY_FILENAME_FITLER = Pattern.compile("^(\\" + MACOS_TEMPORARY_FILE_NAME_PREFIX + ")?[0-9,a-f]{8}$", Pattern.CASE_INSENSITIVE
            | Pattern.UNICODE_CASE);

    private NodeService nodeService;
    private PermissionService permissionService;

    private ContentService contentService;

    private PatternFilter temporaryFiles;
    private PatternFilter systemPaths;
    private HiddenAspect hiddenAspect;
    private boolean enabled = true;

    public FilenameFilteringInterceptor()
    {}

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

    public void setEnabled(boolean enabled)
    {
        this.enabled = enabled;
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
     * @param nodeService
     *            the service to use to apply the <b>sys:temporary</b> aspect
     */
    public void setNodeService(NodeService nodeService)
    {
        this.nodeService = nodeService;
    }

    public void setPermissionService(PermissionService permissionService)
    {
        this.permissionService = permissionService;
    }

    public void setContentService(ContentService contentService)
    {
        this.contentService = contentService;
    }

    public ContentService getContentService()
    {
        return contentService;
    }

    private void checkTemporaryAspect(boolean isTemporary, FileInfo fileInfo)
    {
        checkTemporaryAspect(isTemporary, fileInfo.getNodeRef());
    }

    private void checkTemporaryAspect(boolean isTemporary, NodeRef nodeRef)
    {
        if (isTemporary)
        {
            // it matched, so apply the temporary and hidden aspects
            nodeService.addAspect(nodeRef, ContentModel.ASPECT_TEMPORARY, null);

            if (logger.isDebugEnabled())
            {
                logger.debug("Applied temporary marker: " + nodeRef);
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
                    logger.debug("Removed temporary marker: " + nodeRef);
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
        while (it.hasNext())
        {
            Path.ChildAssocElement elem = (Path.ChildAssocElement) it.next();
            QName qname = elem.getRef().getQName();
            if (qname != null && systemPaths.isFiltered(qname.getLocalName()))
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
        if (!enabled)
        {
            return invocation.proceed();
        }

        // execute and get the result
        String methodName = invocation.getMethod().getName();
        Object ret = null;

        // do the invocation
        if (methodName.startsWith("create"))
        {
            NodeRef nodeRef = (NodeRef) invocation.getArguments()[0];
            String filename = (String) invocation.getArguments()[1];

            if (getMode() == Mode.ENHANCED)
            {
                if (systemPaths.isFiltered(filename))
                {
                    // it's a system file/folder, create as system and allow full control to all authorities
                    ret = runAsSystem(invocation);
                    FileInfoImpl fileInfo = (FileInfoImpl) ret;
                    permissionService.setPermission(fileInfo.getNodeRef(), PermissionService.ALL_AUTHORITIES, PermissionService.FULL_CONTROL, true);

                    // it's always marked temporary and hidden
                    checkTemporaryAspect(true, fileInfo);
                    hiddenAspect.hideNode(fileInfo, getSystemFileVisibilityMask(), false, false, false);
                }
                else
                {
                    // it's not a temporary file/folder, create as normal
                    ret = invocation.proceed();

                    FileInfoImpl fileInfo = (FileInfoImpl) ret;

                    if (isSystemPath(nodeRef, filename))
                    {
                        // it's on a system path, check whether temporary, hidden and noindex aspects need to be applied
                        checkTemporaryAspect(true, fileInfo);
                        hiddenAspect.hideNode(fileInfo, getSystemFileVisibilityMask(), false, false, false);
                    }
                    else
                    {
                        // check whether it's a temporary or hidden file
                        FileInfo sourceInfo = (FileInfo) ret;
                        boolean isTmp = isTemporaryObject(filename, sourceInfo.getNodeRef());
                        checkTemporaryAspect(isTmp, sourceInfo);
                        boolean isHidden = hiddenAspect.checkHidden(fileInfo, false, false);
                        if (isHidden && fileInfo instanceof FileInfoImpl)
                        {
                            ((FileInfoImpl) fileInfo).setHidden(true);
                        }
                    }
                }
            }
            else
            {
                ret = invocation.proceed();

                FileInfoImpl fileInfo = (FileInfoImpl) ret;

                boolean isTmp = isTemporaryObject(filename, fileInfo.getNodeRef());
                checkTemporaryAspect(isTmp, fileInfo);
            }
        }
        else if (methodName.startsWith("move"))
        {
            Object[] args = invocation.getArguments();
            NodeRef sourceNodeRef = (NodeRef) args[0];
            String newName = (String) args[args.length - 1];

            if (newName != null)
            {
                // Name is changing
                // check against all the regular expressions
                boolean isTmp = isTemporaryObject(newName, sourceNodeRef);
                checkTemporaryAspect(isTmp, sourceNodeRef);
            }

            // now do the move
            ret = invocation.proceed();

            if (getMode() == Mode.ENHANCED)
            {
                hiddenAspect.checkHidden(sourceNodeRef, true, true);
            }
        }
        else if (methodName.startsWith("copy"))
        {
            ret = invocation.proceed();

            FileInfoImpl fileInfo = (FileInfoImpl) ret;
            String filename = fileInfo.getName();

            if (logger.isDebugEnabled())
            {
                logger.debug("Checking filename returned by " + methodName + ": " + filename);
            }

            // check against all the regular expressions
            boolean isTmp = isTemporaryObject(filename, fileInfo.getNodeRef());
            checkTemporaryAspect(isTmp, fileInfo);
            if (getMode() == Mode.ENHANCED)
            {
                boolean isHidden = hiddenAspect.checkHidden(fileInfo, true, true);
                if (isHidden && fileInfo instanceof FileInfoImpl)
                {
                    ((FileInfoImpl) fileInfo).setHidden(true);
                }
            }
            /* TODO should these two calls be before the proceed? However its the same problem as create The node needs to be created before we can add aspects. */
        }
        else if (methodName.startsWith("rename"))
        {
            Object[] args = invocation.getArguments();

            if (args != null && args.length == 2)
            {
                /**
                 * Expecting rename(NodeRef, newName)
                 */
                String newName = (String) args[1];
                NodeRef sourceNodeRef = (NodeRef) args[0];

                if (logger.isDebugEnabled())
                {
                    logger.debug("Checking filename returned by " + methodName + ": " + newName);
                }

                // check against all the regular expressions
                boolean isTmp = isTemporaryObject(newName, sourceNodeRef);
                checkTemporaryAspect(isTmp, sourceNodeRef);

                ret = invocation.proceed();

                if (getMode() == Mode.ENHANCED)
                {
                    boolean isHidden = hiddenAspect.checkHidden(sourceNodeRef, true, true);
                    if (isHidden && ret instanceof FileInfoImpl)
                    {
                        ((FileInfoImpl) ret).setHidden(true);
                    }
                }

                return ret;
            }
            else
            {
                /**
                 * expected rename(NodeRef, String) - got something else...
                 */
                throw new AlfrescoRuntimeException("FilenameFilteringInterceptor: unknown rename method");
            }
        }
        else
        {
            ret = invocation.proceed();
        }

        // done
        return ret;
    }

    /**
     * Determines whether specified <code>name</code> matches any pattern of temporary file names. <br>
     * Also it checks special case of new XLS document creation in MacOS:
     * <ul>
     * <li>its name doesn’t have extension (on MacOS, but it has ‘.tmp’ extension on Windows);
     * <li>length of its name equals to 8;
     * <li>its name contains only hexadecimal digits (0-9, A-F);
     * <li>it has Mimetype equal to ‘application/vnd.openxmlformats-officedocument.spreadsheetml.sheet’.
     * </ul>
     * 
     * @param name
     *            - {@link String} value which contains name of node
     * @param nodeRef
     *            - {@link NodeRef} instance of the node
     * @return {@link boolean} value. <code>true</code> if <code>name</code> is name of temporary object.
     */
    private boolean isTemporaryObject(String name, NodeRef nodeRef)
    {
        boolean isFiltered = temporaryFiles.isFiltered(name);
        if (isFiltered)
        {
            return true;
        }

        // This pattern must be validated in conjunction with mimetype validation only!
        boolean result = XSL_MACOS_TEMPORARY_FILENAME_FITLER.matcher(name).matches();

        if (result && !name.startsWith(MACOS_TEMPORARY_FILE_NAME_PREFIX))
        {
            ContentReader contentReader = contentService.getReader(nodeRef, ContentModel.PROP_CONTENT);

            if (null != contentReader)
            {
                result = XLSX_MIMETYPE.equals(contentReader.getMimetype());
            }
            else
            {
                // MNT-10561
                // We are unable to determine the mimetype so assume it's NOT temporary
                result = false;
            }
        }
        return result;
    }
}
