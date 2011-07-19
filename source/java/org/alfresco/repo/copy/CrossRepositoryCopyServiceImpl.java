/*
 * Copyright (C) 2005-2010 Alfresco Software Limited.
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
 * along with Alfresco. If not, see <http://www.gnu.org/licenses/>.
 */
package org.alfresco.repo.copy;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.avm.AVMNodeConverter;
import org.alfresco.service.cmr.avm.AVMNodeDescriptor;
import org.alfresco.service.cmr.avm.AVMService;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.model.FileFolderService;
import org.alfresco.service.cmr.model.FileInfo;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.ContentData;
import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.service.cmr.repository.ContentService;
import org.alfresco.service.cmr.repository.ContentWriter;
import org.alfresco.service.cmr.repository.CopyService;
import org.alfresco.service.cmr.repository.CrossRepositoryCopyService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.Pair;

/**
 * Cross Repository Copying.
 * 
 * @author britt
 */
public class CrossRepositoryCopyServiceImpl implements CrossRepositoryCopyService
{
    /**
     * The NodeService reference.
     */
    private NodeService fNodeService;

    /**
     * The FileFolderService reference.
     */
    private FileFolderService fFileFolderService;

    /**
     * The regular CopyService reference.
     */
    private CopyService fCopyService;

    /**
     * The AVMService.
     */
    private AVMService fAVMService;

    /**
     * The ContentService.
     */
    private ContentService fContentService;

    /**
     * The DictionaryService.
     */
    private DictionaryService fDictionaryService;

    /**
     * A default constructor.
     */
    public CrossRepositoryCopyServiceImpl()
    {
    }

    // Setters for Spring.

    public void setAvmService(AVMService service)
    {
        fAVMService = service;
    }

    public void setContentService(ContentService service)
    {
        fContentService = service;
    }

    public void setCopyService(CopyService service)
    {
        fCopyService = service;
    }

    public void setDictionaryService(DictionaryService service)
    {
        fDictionaryService = service;
    }

    public void setFileFolderService(FileFolderService service)
    {
        fFileFolderService = service;
    }

    public void setNodeService(NodeService service)
    {
        fNodeService = service;
    }

    /**
     * This copies recursively src, which may be a container or a content type to dst, which must be a container. Copied
     * nodes will have the copied from aspect applied to them.
     * 
     * @param src
     *            The node to copy.
     * @param dst
     *            The container to copy it into.
     * @param name
     *            The name to give the copy.
     */
    public void copy(NodeRef src, NodeRef dst, String name)
    {
        StoreRef srcStoreRef = src.getStoreRef();
        StoreRef dstStoreRef = dst.getStoreRef();
        if (srcStoreRef.getProtocol().equals(StoreRef.PROTOCOL_AVM))
        {
            if (dstStoreRef.getProtocol().equals(StoreRef.PROTOCOL_AVM))
            {
                copyAVMToAVM(src, dst, name);
            }
            else if (dstStoreRef.getProtocol().equals(StoreRef.PROTOCOL_WORKSPACE))
            {
                copyAVMToRepo(src, dst, name);
            }
        }
        else if (srcStoreRef.getProtocol().equals(StoreRef.PROTOCOL_WORKSPACE))
        {
            if (dstStoreRef.getProtocol().equals(StoreRef.PROTOCOL_AVM))
            {
                copyRepoToAVM(src, dst, name);
            }
            else if (dstStoreRef.getProtocol().equals(StoreRef.PROTOCOL_WORKSPACE))
            {
                copyRepoToRepo(src, dst, name);
            }
        }
    }

    /**
     * Handle copying from AVM to AVM
     * 
     * @param src
     *            Source node.
     * @param dst
     *            Destination directory node.
     * @param name
     *            Name to give copy.
     */
    private void copyAVMToAVM(NodeRef src, NodeRef dst, String name)
    {
        Pair<Integer, String> srcStorePath = AVMNodeConverter.ToAVMVersionPath(src);
        Pair<Integer, String> dstStorePath = AVMNodeConverter.ToAVMVersionPath(dst);
        fAVMService.copy(srcStorePath.getFirst(), srcStorePath.getSecond(), dstStorePath.getSecond(), name);
    }

    /**
     * Handle copying from AVM to Repo.
     * 
     * @param src
     *            Source node.
     * @param dst
     *            Destination Container.
     * @param name
     *            The name to give the copy.
     */
    private void copyAVMToRepo(NodeRef src, NodeRef dst, String name)
    {
        Pair<Integer, String> versionPath = AVMNodeConverter.ToAVMVersionPath(src);
        AVMNodeDescriptor desc = fAVMService.lookup(versionPath.getFirst(), versionPath.getSecond());
        NodeRef existing = fFileFolderService.searchSimple(dst, name);
        if (desc.isFile())
        {
            if (existing != null && !fNodeService.getType(existing).equals(ContentModel.TYPE_CONTENT))
            {
                fFileFolderService.delete(existing);
                existing = null;
            }
            NodeRef childRef = null;
            if (existing == null)
            {
                childRef = fFileFolderService.create(dst, name, ContentModel.TYPE_CONTENT).getNodeRef();
            }
            else
            {
                childRef = existing;
            }
            InputStream in = fAVMService.getFileInputStream(desc);
            ContentData cd = fAVMService.getContentDataForRead(versionPath.getFirst(), desc.getPath());
            ContentWriter writer = fContentService.getWriter(childRef, ContentModel.PROP_CONTENT, true);
            writer.setEncoding(cd.getEncoding());
            writer.setMimetype(cd.getMimetype());
            OutputStream out = writer.getContentOutputStream();
            copyData(in, out);
            copyPropsAndAspectsAVMToRepo(src, childRef);
        }
        else
        {
            if (existing != null && !fNodeService.getType(existing).equals(ContentModel.TYPE_FOLDER))
            {
                fFileFolderService.delete(existing);
                existing = null;
            }
            NodeRef childRef = null;
            if (existing == null)
            {
                childRef = fFileFolderService.create(dst, name, ContentModel.TYPE_FOLDER).getNodeRef();
            }
            else
            {
                childRef = existing;
            }
            copyPropsAndAspectsAVMToRepo(src, childRef);
            Map<String, AVMNodeDescriptor> listing = fAVMService.getDirectoryListing(desc);
            for (Map.Entry<String, AVMNodeDescriptor> entry : listing.entrySet())
            {
                NodeRef srcChild = AVMNodeConverter.ToNodeRef(versionPath.getFirst(), entry.getValue().getPath());
                copyAVMToRepo(srcChild, childRef, entry.getKey());
            }
        }
    }

    /**
     * Helper that copies aspects and properties.
     * 
     * @param src
     *            The source AVM node.
     * @param dst
     *            The destination Repo node.
     */
    private void copyPropsAndAspectsAVMToRepo(NodeRef src, NodeRef dst)
    {
        Map<QName, Serializable> props = fNodeService.getProperties(src);
        fNodeService.setProperties(dst, props);
        Set<QName> aspects = fNodeService.getAspects(src);
        Map<QName, Serializable> empty = new HashMap<QName, Serializable>();
        for (QName aspect : aspects)
        {
            fNodeService.addAspect(dst, aspect, empty);
        }
// 4.0: Derek Hulley: The cm:copiedFrom aspect is not that important and
//                    AVM doesn't support associations
//        if (!fNodeService.hasAspect(dst, ContentModel.ASPECT_COPIEDFROM))
//        {
//            empty.put(ContentModel.PROP_COPY_REFERENCE, src);
//            fNodeService.addAspect(dst, ContentModel.ASPECT_COPIEDFROM, empty);
//        }
//        else
//        {
//            fNodeService.setProperty(dst, ContentModel.PROP_COPY_REFERENCE, src);
//        }
    }

    /**
     * Handle copying from Repo to AVM.
     * 
     * @param src
     *            The source node.
     * @param dst
     *            The destingation directory.
     * @param name
     *            The name to give the copy.
     */
    private void copyRepoToAVM(NodeRef src, NodeRef dst, String name)
    {
        QName srcType = fNodeService.getType(src);
        Pair<Integer, String> versionPath = AVMNodeConverter.ToAVMVersionPath(dst);
        String childPath = AVMNodeConverter.ExtendAVMPath(versionPath.getSecond(), name);
        NodeRef childNodeRef = AVMNodeConverter.ToNodeRef(-1, childPath);
        if (fDictionaryService.isSubClass(srcType, ContentModel.TYPE_CONTENT))
        {
            ContentReader reader = fContentService.getReader(src, ContentModel.PROP_CONTENT);
            InputStream in = reader.getContentInputStream();
            AVMNodeDescriptor desc = fAVMService.lookup(-1, childPath);
            if (desc != null && !desc.isFile())
            {
                fAVMService.removeNode(childPath);
                desc = null;
            }
            if (desc == null)
            {
                try
                {
                    fAVMService.createFile(versionPath.getSecond(), name).close();
                }
                catch (IOException e)
                {
                    throw new AlfrescoRuntimeException("I/O Error.", e);
                }
            }
            ContentWriter writer = fAVMService.getContentWriter(childPath, true);
            writer.setEncoding(reader.getEncoding());
            writer.setMimetype(reader.getMimetype());
            OutputStream out = writer.getContentOutputStream();
            copyData(in, out);
            copyPropsAndAspectsRepoToAVM(src, childNodeRef, childPath);
            return;
        }
        if (fDictionaryService.isSubClass(srcType, ContentModel.TYPE_FOLDER))
        {
            AVMNodeDescriptor desc = fAVMService.lookup(-1, childPath);
            if (desc != null && !desc.isDirectory())
            {
                fAVMService.removeNode(childPath);
                desc = null;
            }
            if (desc == null)
            {
                fAVMService.createDirectory(versionPath.getSecond(), name);
            }
            copyPropsAndAspectsRepoToAVM(src, childNodeRef, childPath);
            List<FileInfo> listing = fFileFolderService.list(src);
            for (FileInfo info : listing)
            {
                copyRepoToAVM(info.getNodeRef(), childNodeRef, info.getName());
            }
            return;
        }
    }

    /**
     * Helper to copy properties and aspects.
     * 
     * @param src
     *            The source node.
     * @param dst
     *            The destination node.
     * @param dstPath
     *            The destination AVM path.
     */
    private void copyPropsAndAspectsRepoToAVM(NodeRef src, NodeRef dst, String dstPath)
    {
        Map<QName, Serializable> props = fNodeService.getProperties(src);
        fNodeService.setProperties(dst, props);
        Set<QName> aspects = fNodeService.getAspects(src);
        for (QName aspect : aspects)
        {
            fAVMService.addAspect(dstPath, aspect);
        }
// 4.0: Derek Hulley: The cm:copiedFrom aspect is not that important and
//      AVM doesn't support associations
//        if (!fAVMService.hasAspect(-1, dstPath, ContentModel.ASPECT_COPIEDFROM))
//        {
//            fAVMService.addAspect(dstPath, ContentModel.ASPECT_COPIEDFROM);
//        }
//        fNodeService.setProperty(dst, ContentModel.PROP_COPY_REFERENCE, src);
    }

    /**
     * Handle copying from Repo to Repo.
     * 
     * @param src
     *            The source node.
     * @param dst
     *            The destination container.
     * @param name
     *            The name to give the copy.
     */
    private void copyRepoToRepo(NodeRef src, NodeRef dst, String name)
    {
        ChildAssociationRef assocRef = fNodeService.getPrimaryParent(src);
        fCopyService.copyAndRename(src, dst, ContentModel.ASSOC_CONTAINS, assocRef.getQName(), true);
    }

    private void copyData(InputStream in, OutputStream out)
    {
        try
        {
            byte[] buff = new byte[8192];
            int read = 0;
            while ((read = in.read(buff)) != -1)
            {
                out.write(buff, 0, read);
            }
            in.close();
            out.close();
        }
        catch (IOException e)
        {
            throw new AlfrescoRuntimeException("I/O Error.", e);
        }
    }
}
