/*
 * Copyright (C) 2005-2009 Alfresco Software Limited.
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
package org.alfresco.repo.cmis.ws.utils;

import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.cmis.ws.EnumUnfileObject;
import org.alfresco.repo.model.filefolder.FileInfoImpl;
import org.alfresco.service.cmr.coci.CheckOutCheckInService;
import org.alfresco.service.cmr.model.FileFolderService;
import org.alfresco.service.cmr.model.FileInfo;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.ContentData;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.version.VersionService;
import org.alfresco.service.namespace.QName;

/**
 * This class enumerates Documents and Folders hierarchy that begins from specified folder. Iterator returns Document or Empty Folder Objects those may be removed or deleted from
 * repository according to {@link EnumUnfileObject} <b>unfileObject</b> and {@link Boolean} <b>continueOnFailure</b> parameters. After hierarchy enumerating completion iterator may
 * introduce Object Id(s) {@link List} those were not deleted due to {@link Exception}(s)
 * 
 * @author Dmitry Velichkevich
 */
public class CmisObjectIterator implements Iterator<FileInfo>
{
    private EnumUnfileObject unfillingStrategy;
    private boolean continueOnFailure;

    private NodeService nodeService;
    private FileFolderService fileFolderService;
    private VersionService versionService;
    private CheckOutCheckInService checkOutCheckInService;
    private CmisObjectsUtils objectsUtils;

    private Map<FileInfo, List<FileInfo>> hierarchy = new HashMap<FileInfo, List<FileInfo>>();
    private LinkedList<FileInfo> objects = new LinkedList<FileInfo>();
    private List<String> failToDelete = new LinkedList<String>();

    private FileInfo next;
    private boolean lastDeleted;
    private boolean nextFound;
    private boolean deleteAllVersions;

    /**
     * Mandatory constructor
     * 
     * @param rootObject - {@link NodeRef} instance that represents Root folder for hierarchy
     * @param unfillingStrategy - {@link EnumUnfileObject} value that determines File-able Objects deletion or removing strategy
     * @param continueOnFailure - {@link Boolean} value that determines whether it is necessary continue deletion after some {@link Exception} occurred
     * @param nodeService - A {@link NodeService} implementation instance for manipulating with Object Properties
     * @param fileFolderService - A {@link FileFolderService} implementation instance for manipulating with Objects
     * @param objectsUtils - {@link CmisObjectsUtils} service instance for simplifying manipulations with Objects and Object Properties
     */
    public CmisObjectIterator(NodeRef rootObject, EnumUnfileObject unfillingStrategy, boolean continueOnFailure, boolean deleteAllVersions, NodeService nodeService, FileFolderService fileFolderService,
            VersionService versionService, CheckOutCheckInService checkOutCheckInService, CmisObjectsUtils objectsUtils)
    {
        this.unfillingStrategy = unfillingStrategy;
        this.deleteAllVersions = deleteAllVersions;
        this.continueOnFailure = continueOnFailure;
        this.nodeService = nodeService;
        this.fileFolderService = fileFolderService;
        this.versionService = versionService;
        this.checkOutCheckInService = checkOutCheckInService;
        this.objectsUtils = objectsUtils;
        objects.add(new ParentedFileInfo(fileFolderService.getFileInfo(rootObject), null));
    }

    /**
     * This method performs searching for next Object (see {@link CmisObjectIterator})
     */
    public boolean hasNext()
    {
        if (nextFound)
        {
            return true;
        }
        if (objects.isEmpty() || (!failToDelete.isEmpty() && !continueOnFailure))
        {
            return false;
        }
        for (next = objects.removeFirst(); processFolder() && !objects.isEmpty(); next = objects.removeFirst())
        {
        }
        nextFound = (null != next) && (!next.isFolder() || (null == receiveChildren(next.getNodeRef())));
        return nextFound;
    }

    private boolean processFolder()
    {
        if (next.isFolder())
        {
            List<ChildAssociationRef> children = receiveChildren(next.getNodeRef());
            if (null != children)
            {
                objects.addFirst(next);
                for (ChildAssociationRef child : children)
                {
                    FileInfo info = fileFolderService.getFileInfo(child.getChildRef());
                    ParentedFileInfo childInfo = new ParentedFileInfo(info, next);
                    objects.addFirst(childInfo);
                    addChildToParent(next, childInfo);
                }
                return true;
            }
        }
        return false;
    }

    private void addChildToParent(FileInfo parent, FileInfo child)
    {
        if (null != parent)
        {
            List<FileInfo> children;
            if (hierarchy.containsKey(parent))
            {
                children = hierarchy.get(parent);
            }
            else
            {
                children = new LinkedList<FileInfo>();
                hierarchy.put(parent, children);
            }
            children.add(child);
        }
    }

    private List<ChildAssociationRef> receiveChildren(NodeRef folderRef)
    {
        if (null != folderRef)
        {
            List<ChildAssociationRef> result = nodeService.getChildAssocs(folderRef);
            if ((null != result) && !result.isEmpty())
            {
                return result;
            }
        }
        return null;
    }

    /**
     * This method returns currently enumerated Object and changes flag to enumerate next Object
     * 
     * @see CmisObjectIterator
     * @see CmisObjectIterator#hasNext()
     * @see CmisObjectIterator#remove()
     */
    public FileInfo next()
    {
        nextFound = false;
        return next;
    }

    /**
     * This method removes currently enumerated Object (see {@link CmisObjectIterator}). If Object deletion or removing fails due to {@link Exception} then according to
     * <b>continueOnFailure</b> parameter current Object Id and (if <b>continueOnFailure</b>="<b>true</b>") current Object Parent(s)' Id(s) will be stored in <b>failedToDelete</b>
     * {@link List}. Not deleted Object and its Parent(s) never will be enumerated again
     * 
     * @see CmisObjectIterator
     * @see CmisObjectIterator#hasNext()
     * @see CmisObjectIterator#next()
     */
    public void remove()
    {
        lastDeleted = false;
        NodeRef parentRef = null;
        ParentedFileInfo info = null;
        if (null != next)
        {
            info = (ParentedFileInfo) next;
            EnumUnfileObject howToDelete = null;
            parentRef = (null != info.getExactParent()) ? (info.getExactParent().getNodeRef()) : (null);
            if ((null != parentRef) && ((EnumUnfileObject.DELETESINGLEFILED == unfillingStrategy) || !objectsUtils.isPrimaryObjectParent(parentRef, info.getNodeRef())))
            {
                howToDelete = EnumUnfileObject.DELETESINGLEFILED;
            }
            else
            {
                howToDelete = EnumUnfileObject.DELETE;
            }             
            if (EnumUnfileObject.DELETESINGLEFILED == howToDelete) 
            {
                lastDeleted = objectsUtils.removeObject(info.getNodeRef(), parentRef); 
            }
            else 
            {
                if (deleteAllVersions)
                {
                    NodeRef workingCopyRef = (objectsUtils.isWorkingCopy(info.getNodeRef())) ? (info.getNodeRef()) : (checkOutCheckInService.getWorkingCopy(info.getNodeRef()));
                    if (null == workingCopyRef)
                    {
                        versionService.deleteVersionHistory(info.getNodeRef());
                    }                    
                }
                lastDeleted = (objectsUtils.deleteObject(info.getNodeRef()));
            }
            
            
        }
        if (!lastDeleted && (null != info))
        {
            failToDelete.add(generateId(info));
            if ((null == info.getExactParent()) && info.isFolder())
            {
                removeFolderFromCollections(info);
            }
            if (continueOnFailure)
            {
                for (; null != info.getExactParent(); info = (ParentedFileInfo) info.getExactParent())
                {
                    failToDelete.add(generateId(info.getExactParent()));
                    removeFolderFromCollections(info.getExactParent());
                }
            }
        }
    }

    private void removeFolderFromCollections(FileInfo object)
    {
        if (hierarchy.containsKey(object))
        {
            hierarchy.remove(object);
            objects.remove(object);
        }
    }

    private String generateId(FileInfo object)
    {
        StringBuilder generator = new StringBuilder(object.getNodeRef().toString());
        Map<QName, Serializable> properties = object.getProperties();
        String versionLabel = (null != properties) ? ((String) properties.get(ContentModel.PROP_VERSION_LABEL)) : (null);
        if (null != versionLabel)
        {
            generator.append(CmisObjectsUtils.NODE_REFERENCE_ID_DELIMETER).append(versionLabel);
        }
        return generator.toString();
    }

    public boolean wasLastRemoved()
    {
        return lastDeleted;
    }

    public List<String> getFailToDelete()
    {
        return failToDelete;
    }

    /**
     * This class is {@link org.alfresco.jlan.server.filesys.FileInfo} implementation that uses {@link FileInfoImpl} instances to delegate core functionality. The main target of
     * this class is extended standard {@link FileInfo} Objects with Parent Object
     * 
     * @author Dmitry Velichkevich
     */
    private class ParentedFileInfo implements FileInfo, Serializable
    {
        private static final long serialVersionUID = -3024276223816623074L;

        private FileInfo delegator;
        private FileInfo exactParent;

        public ParentedFileInfo(FileInfo delegator, FileInfo exactParent)
        {
            this.delegator = delegator;
            this.exactParent = exactParent;
        }

        public ContentData getContentData()
        {
            return delegator.getContentData();
        }

        public Date getCreatedDate()
        {
            return delegator.getCreatedDate();
        }

        public NodeRef getLinkNodeRef()
        {
            return delegator.getLinkNodeRef();
        }

        public Date getModifiedDate()
        {
            return delegator.getModifiedDate();
        }

        public String getName()
        {
            return delegator.getName();
        }

        public NodeRef getNodeRef()
        {
            return delegator.getNodeRef();
        }

        public Map<QName, Serializable> getProperties()
        {
            return delegator.getProperties();
        }

        public boolean isFolder()
        {
            return delegator.isFolder();
        }

        public boolean isLink()
        {
            return delegator.isLink();
        }

        public FileInfo getExactParent()
        {
            return exactParent;
        }

        @Override
        public boolean equals(Object obj)
        {
            return delegator.equals(obj);
        }

        @Override
        public int hashCode()
        {
            return delegator.hashCode();
        }

        @Override
        public String toString()
        {
            return delegator.toString();
        }
    }
}
