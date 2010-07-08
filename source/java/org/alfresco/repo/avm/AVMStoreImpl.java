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
 * along with Alfresco. If not, see <http://www.gnu.org/licenses/>. */

package org.alfresco.repo.avm;

import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;

import org.alfresco.model.ContentModel;
import org.alfresco.model.WCMModel;
import org.alfresco.repo.avm.util.AVMUtil;
import org.alfresco.repo.avm.util.RawServices;
import org.alfresco.repo.avm.util.SimplePath;
import org.alfresco.repo.domain.PropertyValue;
import org.alfresco.repo.domain.permissions.Acl;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.security.permissions.ACLCopyMode;
import org.alfresco.repo.security.permissions.AccessDeniedException;
import org.alfresco.service.cmr.avm.AVMBadArgumentException;
import org.alfresco.service.cmr.avm.AVMException;
import org.alfresco.service.cmr.avm.AVMExistsException;
import org.alfresco.service.cmr.avm.AVMNodeDescriptor;
import org.alfresco.service.cmr.avm.AVMNotFoundException;
import org.alfresco.service.cmr.avm.AVMStoreDescriptor;
import org.alfresco.service.cmr.avm.AVMWrongTypeException;
import org.alfresco.service.cmr.avm.VersionDescriptor;
import org.alfresco.service.cmr.dictionary.AspectDefinition;
import org.alfresco.service.cmr.dictionary.DataTypeDefinition;
import org.alfresco.service.cmr.dictionary.PropertyDefinition;
import org.alfresco.service.cmr.repository.ContentData;
import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.service.cmr.repository.ContentWriter;
import org.alfresco.service.cmr.repository.InvalidNodeRefException;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.security.PermissionService;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.GUID;
import org.alfresco.util.Pair;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * A Repository contains a current root directory and a list of
 * root versions.  Each root version corresponds to a separate snapshot
 * operation.
 * @author britt
 */
public class AVMStoreImpl implements AVMStore
{
    private static Log logger = LogFactory.getLog(AVMStoreImpl.class);
    /**
     * The primary key.
     */
    private long fID;

    /**
     * The name of this AVMStore.
     */
    private String fName;

    /**
     * The current root directory.
     */
    private DirectoryNode fRoot;
    
    /**
     * The next version id.
     */
    private int fNextVersionID;

    /**
     * The version (for concurrency control).
     */
    private long fVers;

    /**
     * Acl for this store.
     */
    private Acl fACL;
    
    /**
     * The AVMRepository.
     */
    transient private AVMRepository fAVMRepository;
    
    /**
     * Default constructor.
     */
    public AVMStoreImpl()
    {
        fAVMRepository = AVMRepository.GetInstance();
    }
    
    /**
     * Make a brand new AVMStore.
     * @param repo The AVMRepository.
     * @param name The name of the AVMStore.
     */
    public AVMStoreImpl(AVMRepository repo, String name)
    {
        // Make ourselves up and save.
        fAVMRepository = repo;
        
        setName(name);
        setNextVersionID(0);
        setRoot(null);
        
        AVMDAOs.Instance().fAVMStoreDAO.save(this);
        
        String creator = RawServices.Instance().getAuthenticationContext().getCurrentUserName();
        if (creator == null)
        {
            creator = RawServices.Instance().getAuthenticationContext().getSystemUserName();
        }
        setProperty(ContentModel.PROP_CREATOR, new PropertyValue(null, creator));
        setProperty(ContentModel.PROP_CREATED, new PropertyValue(null, new Date(System.currentTimeMillis())));
        
        // Make up the initial version record and save.
        long time = System.currentTimeMillis();
        
        PlainDirectoryNode dir = new PlainDirectoryNodeImpl(this);
        dir.setIsRoot(true);
        AVMDAOs.Instance().fAVMNodeDAO.save(dir);
        
        setRoot(dir);
        
        VersionRoot versionRoot = new VersionRootImpl(this,
                                                      getRoot(),
                                                      getNextVersionID(),
                                                      time,
                                                      creator,
                                                      AVMUtil.INITIAL_SNAPSHOT,
                                                      AVMUtil.INITIAL_SNAPSHOT);
        setNextVersionID(getNextVersionID()+1);
        
        AVMDAOs.Instance().fAVMStoreDAO.update(this);
        AVMDAOs.Instance().fVersionRootDAO.save(versionRoot);
    }
    
    /**
     * Set the primary key
     * @param id The primary key.
     */
    public void setId(long id)
    {
        fID = id;
    }
    
    /**
     * Get the primary key.
     * @return The primary key.
     */
    public long getId()
    {
        return fID;
    }

    /**
     * Set a new root for this.
     * @param root
     */
    public void setNewRoot(DirectoryNode root)
    {
        fRoot = root;
    }

    /**
     * Snapshot this store.  This creates a new version record.
     * @return The version id of the new snapshot.
     */
    public Map<String, Integer> createSnapshot(String tag, String description, Map<String, Integer> snapShotMap)
    {
        long start = System.currentTimeMillis();
        
        long rootID = getRoot().getId();
        AVMStoreImpl me = (AVMStoreImpl)AVMDAOs.Instance().fAVMStoreDAO.getByID(getId());
        VersionRoot lastVersion = AVMDAOs.Instance().fVersionRootDAO.getMaxVersion(me);
        List<VersionLayeredNodeEntry> layeredEntries =
            AVMDAOs.Instance().fVersionLayeredNodeEntryDAO.get(lastVersion);
        // Is there no need for a snapshot?
        DirectoryNode root = (DirectoryNode)AVMDAOs.Instance().fAVMNodeDAO.getByID(rootID);
        if (!root.getIsNew() && layeredEntries.size() == 0)
        {
            if (logger.isTraceEnabled())
            {
                logger.trace("createSnapshot: no snapshot required: "+me.getName()+" ["+me.getId()+"] - lastVersion = "+lastVersion.getVersionID() + "("+tag+", "+description+")");
            }
            
            // So, we set the tag and description fields of the latest version.
            if (tag != null || description != null)
            {
                lastVersion.setTag(tag);
                lastVersion.setDescription(description);
                
                AVMDAOs.Instance().fVersionRootDAO.update(lastVersion);
            }
            snapShotMap.put(getName(), lastVersion.getVersionID());
            
            if (logger.isTraceEnabled())
            {
                logger.trace("createSnapshot: no snapshot required: "+me.getName()+(tag != null ? "("+tag+")" : "")+" [lastVersion = "+lastVersion.getVersionID()+"]");
            }
            
            return snapShotMap;
        }
        if (logger.isTraceEnabled())
        {
            logger.trace("createSnapshot: snapshot: "+me.getName()+" ["+me.getId()+"] - lastVersion="+lastVersion.getVersionID()+", layeredEntries="+layeredEntries.size());
        }
        
        snapShotMap.put(getName(), me.getNextVersionID());
        // Force copies on all the layered nodes from last snapshot.
        for (VersionLayeredNodeEntry entry : layeredEntries)
        {
            String[] pathParts = AVMUtil.splitPath(entry.getPath());
            Lookup lookup = me.lookup(-1, pathParts[1], false, false);
            if (lookup == null)
            {
                continue;
            }
            if (lookup.getCurrentNode().getType() != AVMNodeType.LAYERED_DIRECTORY &&
                lookup.getCurrentNode().getType() != AVMNodeType.LAYERED_FILE)
            {
                continue;
            }
            if (lookup.getCurrentNode().getIsNew())
            {
                continue;
            }
            
            if (lookup.getCurrentNode().getType() == AVMNodeType.LAYERED_DIRECTORY)
            {
                fAVMRepository.forceCopy(entry.getPath());
                me = (AVMStoreImpl)AVMDAOs.Instance().fAVMStoreDAO.getByID(getId());
            }
            else if (lookup.getCurrentNode().getType() == AVMNodeType.LAYERED_FILE)
            {
                String parentName[] = AVMUtil.splitBase(entry.getPath());
                AVMNode child = lookup.getCurrentNode();
                DirectoryNode parent = lookup.getCurrentNodeDirectory();
                
                AVMNode newChild = ((LayeredFileNode)child).copyLiterally(lookup);
                newChild.setAncestor(child);
                parent.putChild(parentName[1], newChild);
            }
        }
        
        if (logger.isTraceEnabled())
        {
            logger.trace("createSnapshot: force copy: "+me.getName()+(tag != null ? "("+tag+")" : "")+" [lastVersion="+lastVersion.getVersionID()+", layeredEntriesCnt="+layeredEntries.size()+"] in " + (System.currentTimeMillis() - start) + " msecs");
        }
        
        // Clear out the new nodes.
        List<Long> allLayeredNodeIDs = AVMDAOs.Instance().fAVMNodeDAO.getNewLayeredInStoreIDs(me);
        
        AVMDAOs.Instance().fAVMNodeDAO.clearNewInStore(me);
        
        AVMDAOs.Instance().fAVMNodeDAO.clear();
        List<Long> layeredNodeIDs = new ArrayList<Long>();
        for (Long layeredID : allLayeredNodeIDs)
        {
            Layered layered = (Layered)AVMDAOs.Instance().fAVMNodeDAO.getByID(layeredID);
            
            String indirection = null;
            if (layered != null)
            {
                indirection = layered.getIndirection();
            }
            
            if (indirection == null)
            {
                continue;
            }
            layeredNodeIDs.add(layeredID);
            String storeName = AVMUtil.getStoreName(indirection);
            if (!snapShotMap.containsKey(storeName))
            {
                AVMStore store = AVMDAOs.Instance().fAVMStoreDAO.getByName(storeName);
                if (store == null)
                {
                    layered.setIndirectionVersion(-1);
                }
                else
                {
                    store.createSnapshot(null, null, snapShotMap);
                    layered = (Layered)AVMDAOs.Instance().fAVMNodeDAO.getByID(layeredID);
                    layered.setIndirectionVersion(snapShotMap.get(storeName));
                }
            }
            else
            {
                layered.setIndirectionVersion(snapShotMap.get(storeName));
            }
            
            AVMDAOs.Instance().fAVMNodeDAO.update(layered);
        }
        
        // Make up a new version record.
        String user = RawServices.Instance().getAuthenticationContext().getCurrentUserName();
        if (user == null)
        {
            user = RawServices.Instance().getAuthenticationContext().getSystemUserName();
        }
        
        me = (AVMStoreImpl)AVMDAOs.Instance().fAVMStoreDAO.getByID(getId());
        VersionRoot versionRoot = new VersionRootImpl(me,
                                                      me.getRoot(),
                                                      me.getNextVersionID(),
                                                      System.currentTimeMillis(),
                                                      user,
                                                      tag,
                                                      description);
        
        me.setNextVersionID(me.getNextVersionID()+1);
        
        AVMDAOs.Instance().fAVMStoreDAO.update(me);
        
        AVMDAOs.Instance().fVersionRootDAO.save(versionRoot);
        
        int vlneCnt = 0;
        
        for (Long nodeID : layeredNodeIDs)
        {
            AVMNode node = AVMDAOs.Instance().fAVMNodeDAO.getByID(nodeID);
            List<String> paths = fAVMRepository.getVersionPaths(versionRoot, node);
            for (String path : paths)
            {
                VersionLayeredNodeEntry entry =
                    new VersionLayeredNodeEntryImpl(versionRoot, path);
                AVMDAOs.Instance().fVersionLayeredNodeEntryDAO.save(entry);
            }
            
            vlneCnt = vlneCnt+paths.size();
        }
        
        if (logger.isDebugEnabled())
        {
            logger.debug("Raw snapshot: "+me.getName()+(tag != null ? "("+tag+")" : "")+" [versionRootId="+versionRoot.getId()+", layeredNodeIDsCnt="+layeredNodeIDs.size()+", versionLayeredNodeEntriesCnt="+vlneCnt+"] in " + (System.currentTimeMillis() - start) + " msecs");
        }
        
        return snapShotMap;
    }

    /**
     * Create a new directory.
     * @param path The path to the containing directory.
     * @param name The name of the new directory.
     */
    public void createDirectory(String path, String name, List<QName> aspects, Map<QName, PropertyValue> properties)
    {
        Lookup lPath = lookupDirectory(-1, path, true);
        if (lPath == null)
        {
            throw new AVMNotFoundException("Path " + path + " not found.");
        }
        DirectoryNode dir = (DirectoryNode)lPath.getCurrentNode();
        if (!fAVMRepository.can(this, dir, PermissionService.ADD_CHILDREN, lPath.getDirectlyContained()))
        {
            throw new AccessDeniedException("Not allowed to write: " + path);
        }
        Pair<AVMNode, Boolean> temp = dir.lookupChild(lPath, name, true);
        AVMNode child = (temp == null) ? null : temp.getFirst();
        if (child != null && child.getType() != AVMNodeType.DELETED_NODE)
        {
            throw new AVMExistsException("Child exists: " + name);
        }
        DirectoryNode newDir = null;
        if (lPath.isLayered())  // Creating a directory in a layered context creates
                                // a LayeredDirectoryNode that gets its indirection from
                                // its parent.
        {
            // TODO - collapse save/update
            newDir = new LayeredDirectoryNodeImpl((String)null, this, null, null, ACLCopyMode.INHERIT);
            ((LayeredDirectoryNodeImpl)newDir).setPrimaryIndirection(false);
            ((LayeredDirectoryNodeImpl)newDir).setLayerID(lPath.getTopLayer().getLayerID());
            
            newDir.copyACLs(dir, ACLCopyMode.INHERIT);
            
            AVMDAOs.Instance().fAVMNodeDAO.update(newDir);
        }
        else
        {
            newDir = new PlainDirectoryNodeImpl(this);
            
            newDir.copyACLs(dir, ACLCopyMode.INHERIT);
            
            AVMDAOs.Instance().fAVMNodeDAO.save(newDir);
        }
        
        // newDir.setVersionID(getNextVersionID());
        if (child != null)
        {
            newDir.setAncestor(child);
        }
        //dir.updateModTime();
        dir.putChild(name, newDir);
        if (aspects != null)
        {
            Set<QName> aspectQNames = new HashSet<QName>(newDir.getAspects());
            aspectQNames.addAll(aspects);
            ((DirectoryNodeImpl)newDir).setAspects(aspectQNames);
        }
        if (properties != null)
        {
            Map<QName, PropertyValue> props = new HashMap<QName, PropertyValue>(newDir.getProperties());
            props.putAll(properties);
            ((DirectoryNodeImpl)newDir).setProperties(props);
        }
    }

    /**
     * Create a new layered directory.
     * @param srcPath The target indirection for a layered node.
     * @param dstPath The containing directory for the new node.
     * @param name The name of the new node.
     */
    public void createLayeredDirectory(String srcPath, String dstPath,
                                       String name)
    {
        Lookup lPath = lookupDirectory(-1, dstPath, true);
        if (lPath == null)
        {
            throw new AVMNotFoundException("Path " + dstPath + " not found.");
        }
        DirectoryNode dir = (DirectoryNode)lPath.getCurrentNode();
        Pair<AVMNode, Boolean> temp = dir.lookupChild(lPath, name, true);
        AVMNode child = (temp == null) ? null : temp.getFirst();
        if (child != null && child.getType() != AVMNodeType.DELETED_NODE)
        {
            throw new AVMExistsException("Child exists: " +  name);
        }
        Long parentAcl = dir.getAcl() == null ? null : dir.getAcl().getId();
        
        LayeredDirectoryNode newDir =
            new LayeredDirectoryNodeImpl(srcPath, this, null, parentAcl, ACLCopyMode.INHERIT);
        
        if (lPath.isLayered())
        {
            // When a layered directory is made inside of a layered context,
            // it gets its layer id from the topmost layer in its lookup
            // path.
            LayeredDirectoryNode top = lPath.getTopLayer();
            newDir.setLayerID(top.getLayerID());
        }
        else
        {
            // Otherwise we issue a brand new layer id.
            
            // note: re-use generated node id as a layer id
            newDir.setLayerID(newDir.getId());
        }
        
        AVMDAOs.Instance().fAVMNodeDAO.update(newDir);
        
        if (child != null)
        {
            newDir.setAncestor(child);
        }
        
        // newDir.setVersionID(getNextVersionID());
        //dir.updateModTime();
        dir.putChild(name, newDir);
    }

    /**
     * Create a new file.
     * @param path The path to the directory to contain the new file.
     * @param name The name to give the new file.
     * initial content.
     */
    public OutputStream createFile(String path, String name)
    {
        return createFile(path, name, null, null).getContentOutputStream();
    }

    /**
     * Create a file with the given contents.
     * @param path The path to the containing directory.
     * @param name The name to give the new file.
     * @param data The contents.
     */
    public void createFile(String path, String name, File data, List<QName> aspects, Map<QName, PropertyValue> properties)
    {
        createFile(path, name, aspects, properties).putContent(data);
    }
    
    private ContentWriter createFile(String path, String name, List<QName> aspects, Map<QName, PropertyValue> properties)
    {
        Lookup lPath = lookupDirectory(-1, path, true);
        if (lPath == null)
        {
            throw new AVMNotFoundException("Path " + path + " not found.");
        }
        DirectoryNode dir = (DirectoryNode)lPath.getCurrentNode();
        if (!fAVMRepository.can(this, dir, PermissionService.ADD_CHILDREN, lPath.getDirectlyContained()))
        {
            throw new AccessDeniedException("Not allowed to write: " + path);
        }
        Pair<AVMNode, Boolean> temp = dir.lookupChild(lPath, name, true);
        AVMNode child = (temp == null) ? null : temp.getFirst();
        if (child != null && child.getType() != AVMNodeType.DELETED_NODE)
        {
            throw new AVMExistsException("Child exists: " + name);
        }
        
        PlainFileNodeImpl file = new PlainFileNodeImpl(this);
        
        file.setContentData(new ContentData(null,
                RawServices.Instance().getMimetypeService().guessMimetype(name),
                -1,
                "UTF-8"));
        
        file.copyACLs(dir, ACLCopyMode.INHERIT);
        
        AVMDAOs.Instance().fAVMNodeDAO.save(file);
        
        // file.setVersionID(getNextVersionID());
        //dir.updateModTime();
        dir.putChild(name, file);
        if (child != null)
        {
            file.setAncestor(child);
        }
        
        if (aspects != null)
        {
            Set<QName> aspectQNames = new HashSet<QName>(aspects.size());
            aspectQNames.addAll(aspects);
            file.setAspects(aspectQNames);
        }
        if (properties != null)
        {
            Map<QName, PropertyValue> props = new HashMap<QName, PropertyValue>(properties.size());
            props.putAll(properties);
            file.setProperties(props);
        }
        
        return createContentWriter(AVMNodeConverter.ExtendAVMPath(path, name), true);
    }

    /**
     * Create a new layered file.
     * @param srcPath The target indirection for the layered file.
     * @param dstPath The path to the directory to contain the new file.
     * @param name The name of the new file.
     */
    public void createLayeredFile(String srcPath, String dstPath, String name)
    {
        Lookup lPath = lookupDirectory(-1, dstPath, true);
        if (lPath == null)
        {
            throw new AVMNotFoundException("Path " + dstPath + " not found.");
        }
        DirectoryNode dir = (DirectoryNode)lPath.getCurrentNode();
        if (!fAVMRepository.can(this, dir, PermissionService.ADD_CHILDREN, lPath.getDirectlyContained()))
        {
            throw new AccessDeniedException("Not allowed to write: " + dstPath);
        }
        Pair<AVMNode, Boolean> temp = dir.lookupChild(lPath, name, true);
        AVMNode child = (temp == null) ? null : temp.getFirst();
        if (child != null && child.getType() != AVMNodeType.DELETED_NODE)
        {
            throw new AVMExistsException("Child exists: " + name);
        }
        // TODO Reexamine decision to not check validity of srcPath. Warning for now.
        String[] srcPathParts = srcPath.split(":");
        String[] dstPathParts = dstPath.split(":");
        
        Lookup lPathSrc = null;
        if (srcPathParts[0].equals(dstPathParts[0]))
        {
            lPathSrc = lookup(-1, srcPathParts[1], false, false);
        }
        else
        {
            AVMStore srcStore = AVMDAOs.Instance().fAVMStoreDAO.getByName(srcPathParts[0]);
            if (srcStore != null)
            {
                lPathSrc = srcStore.lookup(-1, srcPathParts[1], false, false);
            }
        }
        
        AVMNode srcNode = null;
        if (lPathSrc == null)
        {
            logger.warn("CreateLayeredFile: srcPath not found: "+srcPath);
        }
        else
        {
            srcNode = (AVMNode)lPathSrc.getCurrentNode();
            if (! (srcNode instanceof FileNode))
            {
                logger.warn("CreateLayeredFile: srcPath is not a file: "+srcPath);
            }
        }
        
        LayeredFileNodeImpl newFile =
            new LayeredFileNodeImpl(srcPath, this, null);
        
        newFile.copyACLs(dir, ACLCopyMode.INHERIT);
        
        AVMDAOs.Instance().fAVMNodeDAO.save(newFile);
        
        if (child != null)
        {
            newFile.setAncestor(child);
        }
        else
        {
            if ((srcNode != null) && (srcNode instanceof FileNode))
            {
                newFile.setAncestor((FileNode)srcNode);
            }
        }
        
        // newFile.setVersionID(getNextVersionID());
        //dir.updateModTime();
        dir.putChild(name, newFile);
    }

    /**
     * Get an input stream from a file.
     * @param version The version id to look under.
     * @param path The path to the file.
     * @return An InputStream.
     */
    public InputStream getInputStream(int version, String path)
    {
        ContentReader reader = getContentReader(version, path);
        if (reader == null)
        {
            // TODO This is wrong, wrong, wrong. Do something about it
            // sooner rather than later.
            throw new AVMNotFoundException(path + " has no content.");
        }
        return reader.getContentInputStream();
    }

    /**
     * Get a ContentReader from a file.
     * @param version The version to look under.
     * @param path The path to the file.
     * @return A ContentReader.
     */
    public ContentReader getContentReader(int version, String path)
    {
        try
        {
            NodeRef nodeRef = AVMNodeConverter.ToNodeRef(version, getName() + ":" + path);
            return RawServices.Instance().getContentService().getReader(nodeRef, ContentModel.PROP_CONTENT);
        }
        catch (InvalidNodeRefException inre)
        {
            throw new AVMNotFoundException("Path " + path + " not found.");
        }
    }

    /**
     * Get a ContentWriter to a file.
     * @param path The path to the file.
     * @return A ContentWriter.
     * @param update true if the property must be updated atomically when the content write
     *      stream is closed (attaches a listener to the stream); false if the client code
     *      will perform the updates itself.
     */
    public ContentWriter createContentWriter(String path, boolean update)
    {
        try
        {
            NodeRef nodeRef = AVMNodeConverter.ToNodeRef(-1, getName() + ":" + path);
            ContentWriter writer =
                RawServices.Instance().getContentService().getWriter(nodeRef, ContentModel.PROP_CONTENT, update);
            return writer;
        }
        catch (InvalidNodeRefException inre)
        {
            throw new AVMNotFoundException("Path " + path + " not found.");
        }
    }

    /**
     * Get a listing from a directory.
     * @param version The version to look under.
     * @param path The path to the directory.
     * @return A List of FolderEntries.
     */
    public SortedMap<String, AVMNodeDescriptor> getListing(int version, String path,
                                                           boolean includeDeleted)
    {
        Lookup lPath = lookupDirectory(version, path, false);
        if (lPath == null)
        {
            throw new AVMNotFoundException("Path " + path + " not found.");
        }
        DirectoryNode dir = (DirectoryNode)lPath.getCurrentNode();
        if (!fAVMRepository.can(this, dir, PermissionService.READ_CHILDREN, lPath.getDirectlyContained()))
        {
            throw new AccessDeniedException("Not allowed to read: " + path);
        }
        Map<String, AVMNode> listing = dir.getListing(lPath, includeDeleted);
        return translateListing(listing, lPath);
    }

    /**
     * Get the list of nodes directly contained in a directory.
     * @param version The version to look under.
     * @param path The path to the directory.
     * @return A Map of names to descriptors.
     */
    public SortedMap<String, AVMNodeDescriptor> getListingDirect(int version, String path,
                                                                 boolean includeDeleted)
    {
        Lookup lPath = lookupDirectory(version, path, false);
        if (lPath == null)
        {
            throw new AVMNotFoundException("Path " + path + " not found.");
        }
        DirectoryNode dir = (DirectoryNode)lPath.getCurrentNode();
        if (!fAVMRepository.can(this, dir, PermissionService.READ_CHILDREN, lPath.getDirectlyContained()))
        {
            throw new AccessDeniedException("Not allowed to read: " + path);
        }
        if (lPath.isLayered() && dir.getType() != AVMNodeType.LAYERED_DIRECTORY)
        {
            return new TreeMap<String, AVMNodeDescriptor>();
        }
        Map<String, AVMNode> listing = dir.getListingDirect(lPath, includeDeleted);
        return translateListing(listing, lPath);
    }

    /**
     * Helper to convert an internal representation of a directory listing
     * to an external representation.
     * @param listing The internal listing, a Map of names to nodes.
     * @param lPath The Lookup for the directory.
     * @return A Map of names to descriptors.
     */
    private SortedMap<String, AVMNodeDescriptor>
        translateListing(Map<String, AVMNode> listing, Lookup lPath)
    {
        SortedMap<String, AVMNodeDescriptor> results = new TreeMap<String, AVMNodeDescriptor>(String.CASE_INSENSITIVE_ORDER);
        for (String name : listing.keySet())
        {
            // TODO consider doing this at a lower level.
            AVMNode child = listing.get(name);
            AVMNodeDescriptor desc = child.getDescriptor(lPath, name);
            results.put(name, desc);
        }
        return results;
    }

    /**
     * Get the names of the deleted nodes in a directory.
     * @param version The version to look under.
     * @param path The path to the directory.
     * @return A List of names.
     */
    public List<String> getDeleted(int version, String path)
    {
        Lookup lPath = lookupDirectory(version, path, false);
        if (lPath == null)
        {
            throw new AVMNotFoundException("Path " + path + " not found.");
        }
        DirectoryNode dir = (DirectoryNode)lPath.getCurrentNode();
        if (!fAVMRepository.can(this, dir, PermissionService.READ_CHILDREN, lPath.getDirectlyContained()))
        {
            throw new AccessDeniedException("Not allowed to read: " + path);
        }
        List<String> deleted = dir.getDeletedNames();
        return deleted;
    }

    /**
     * Get an output stream to a file.
     * @param path The path to the file.
     * @return An OutputStream.
     */
    public OutputStream getOutputStream(String path)
    {
        ContentWriter writer = createContentWriter(path, true);
        return writer.getContentOutputStream();
    }

    /**
     * Remove a node and everything underneath it.
     * @param path The path to the containing directory.
     * @param name The name of the node to remove.
     */
    public void removeNode(String path, String name)
    {
        Lookup lPath = lookupDirectory(-1, path, true);
        if (lPath == null)
        {
            throw new AVMNotFoundException("Path " + path + " not found.");
        }
        
        DirectoryNode dir = (DirectoryNode)lPath.getCurrentNode();
        Pair<AVMNode, Boolean> temp = dir.lookupChild(lPath, name, false);
        AVMNode child = (temp == null) ? null : temp.getFirst();
        if (child == null)
        {
            Lookup lPathToChild = lookup(-1, path+"/"+name, true, false);
            if (lPathToChild != null)
            {
                // ETHREEOH-2297
                child = lPathToChild.getCurrentNode();
            }
            if (child == null)
            {
                throw new AVMNotFoundException("Does not exist: " + name);
            }
            
            dir = lPathToChild.getCurrentNodeDirectory();
        }
        
        if (!fAVMRepository.can(this, child, PermissionService.DELETE_NODE, false))
        {
            throw new AVMNotFoundException("Not allowed to delete in store : " + getName() +"  at " + path);
        }
        
        if (dir != null)
        {
            dir.removeChild(lPath, name);
            //dir.updateModTime();
        }
    }

    /**
     * Allow a name which has been deleted to be visible through that layer.
     * @param dirPath The path to the containing directory.
     * @param name The name to uncover.
     */
    public void uncover(String dirPath, String name)
    {
        Lookup lPath = lookupDirectory(-1, dirPath, true);
        if (lPath == null)
        {
            throw new AVMNotFoundException("Directory path " + dirPath + " not found.");
        }
        DirectoryNode node = (DirectoryNode)lPath.getCurrentNode();
        if (node.getType() != AVMNodeType.LAYERED_DIRECTORY)
        {
            throw new AVMWrongTypeException("Not a layered directory: " + dirPath);
        }
        Pair<AVMNode, Boolean> temp = node.lookupChild(lPath, name, true);
        AVMNode child = (temp == null) ? null : temp.getFirst();
        if(child == null)
        {
            throw new AVMNotFoundException("No child to recover at "+dirPath+" called "+name);
        }
        if (!fAVMRepository.can(this, child, PermissionService.DELETE_NODE, false))
        {
            throw new AccessDeniedException("Not allowed to uncover: " + dirPath + "  ->  "+name);
        }
        ((LayeredDirectoryNode)node).uncover(lPath, name);
        node.updateModTime();
        
        AVMDAOs.Instance().fAVMNodeDAO.update(node);
    }
    
    /**
     * Get the set of all extant versions for this AVMStore.
     * @return A Set of version ids.
     */
    public List<VersionDescriptor> getVersions()
    {
        List<VersionRoot> versions = AVMDAOs.Instance().fVersionRootDAO.getAllInAVMStore(this);
        List<VersionDescriptor> descs = new ArrayList<VersionDescriptor>();
        for (VersionRoot vr : versions)
        {
            VersionDescriptor desc =
                new VersionDescriptor(getName(),
                                      vr.getVersionID(),
                                      vr.getCreator(),
                                      vr.getCreateDate(),
                                      vr.getTag(),
                                      vr.getDescription());
            descs.add(desc);
        }
        return descs;
    }

    /**
     * Get the versions between the given dates (inclusive). From or
     * to may be null but not both.
     * @param from The earliest date.
     * @param to The latest date.
     * @return The Set of matching version IDs.
     */
    public List<VersionDescriptor> getVersions(Date from, Date to)
    {
        List<VersionRoot> versions = AVMDAOs.Instance().fVersionRootDAO.getByDates(this, from, to);
        List<VersionDescriptor> descs = new ArrayList<VersionDescriptor>();
        for (VersionRoot vr : versions)
        {
            VersionDescriptor desc =
                new VersionDescriptor(getName(),
                                      vr.getVersionID(),
                                      vr.getCreator(),
                                      vr.getCreateDate(),
                                      vr.getTag(),
                                      vr.getDescription());
            descs.add(desc);
        }
        return descs;
    }

    
    
    public List<VersionDescriptor> getVersionsTo(int version)
    {
        List<VersionRoot> versions = AVMDAOs.Instance().fVersionRootDAO.getByVersionsTo(this, version);
        List<VersionDescriptor> descs = new ArrayList<VersionDescriptor>();
        for (VersionRoot vr : versions)
        {
            VersionDescriptor desc =
                new VersionDescriptor(getName(),
                                      vr.getVersionID(),
                                      vr.getCreator(),
                                      vr.getCreateDate(),
                                      vr.getTag(),
                                      vr.getDescription());
            descs.add(desc);
        }
        return descs;
    }

    public List<VersionDescriptor> getVersionsFrom(int version)
    {
        List<VersionRoot> versions = AVMDAOs.Instance().fVersionRootDAO.getByVersionsFrom(this, version);
        List<VersionDescriptor> descs = new ArrayList<VersionDescriptor>();
        for (VersionRoot vr : versions)
        {
            VersionDescriptor desc =
                new VersionDescriptor(getName(),
                                      vr.getVersionID(),
                                      vr.getCreator(),
                                      vr.getCreateDate(),
                                      vr.getTag(),
                                      vr.getDescription());
            descs.add(desc);
        }
        return descs;
    }
    
    
    

    public List<VersionDescriptor> getVersionsBetween(int startVersion, int endVersion)
    {
        List<VersionRoot> versions = AVMDAOs.Instance().fVersionRootDAO.getByVersionsBetween(this, startVersion, endVersion);
        List<VersionDescriptor> descs = new ArrayList<VersionDescriptor>();
        for (VersionRoot vr : versions)
        {
            VersionDescriptor desc =
                new VersionDescriptor(getName(),
                                      vr.getVersionID(),
                                      vr.getCreator(),
                                      vr.getCreateDate(),
                                      vr.getTag(),
                                      vr.getDescription());
            descs.add(desc);
        }
        return descs;
    }

    /**
     * Get the AVMRepository.
     * @return The AVMRepository
     */
    public AVMRepository getAVMRepository()
    {
        return fAVMRepository;
    }

    /**
     * Lookup up a path.
     * @param version The version to look in.
     * @param path The path to look up.
     * @param write Whether this is in the context of a write.
     * @return A Lookup object.
     */
    public Lookup lookup(int version, String path, boolean write, boolean includeDeleted)
    {
        SimplePath sPath = new SimplePath(path);
        return RawServices.Instance().getLookupCache().lookup(this, version, sPath, write, includeDeleted);
    }

    /**
     * Get the root node descriptor.
     * @param version The version to get.
     * @return The descriptor.
     */
    public AVMNodeDescriptor getRoot(int version)
    {
        AVMNode root = null;
        if (version < 0)
        {
            root = getRoot();
        }
        else
        {
            root = AVMDAOs.Instance().fAVMNodeDAO.getAVMStoreRoot(this, version);
        }
        if (!fAVMRepository.can(this, root, PermissionService.READ_CHILDREN, true))
        {
            throw new AccessDeniedException("Not allowed to read: " + getName() + "@" + version);
        }
        return root.getDescriptor(getName() + ":", "", null, -1);
    }

    /**
     * Lookup a node and insist that it is a directory.
     * @param version The version to look under.
     * @param path The path to the directory.
     * @param write Whether this is in a write context.
     * @return A Lookup object.
     */
    public Lookup lookupDirectory(int version, String path, boolean write)
    {
        // Just do a regular lookup and assert that the last element
        // is a directory.
        Lookup lPath = lookup(version, path, write, false);
        if (lPath == null)
        {
            return null;
        }
        if (lPath.getCurrentNode().getType() != AVMNodeType.PLAIN_DIRECTORY &&
            lPath.getCurrentNode().getType() != AVMNodeType.LAYERED_DIRECTORY)
        {
            return null;
        }
        return lPath;
    }

    /**
     * Get the effective indirection path for a layered node.
     * @param version The version to look under.
     * @param path The path to the node.
     * @return The effective indirection.
     */
    public String getIndirectionPath(int version, String path)
    {
        Lookup lPath = lookup(version, path, false, false);
        if (lPath == null)
        {
            throw new AVMNotFoundException("Path " + path + " not found.");
        }
        if (!lPath.isLayered())
        {
            return null;
        }
        AVMNode node = lPath.getCurrentNode();
        if (!fAVMRepository.can(this, node, PermissionService.READ_PROPERTIES, lPath.getDirectlyContained()))
        {
            throw new AccessDeniedException("Not allowed to read: " + path);
        }
        if (node.getType() == AVMNodeType.LAYERED_DIRECTORY)
        {
            LayeredDirectoryNode dir = (LayeredDirectoryNode)node;
            return dir.getUnderlying(lPath);
        }
        else if (node.getType() == AVMNodeType.LAYERED_FILE)
        {
            LayeredFileNode file = (LayeredFileNode)node;
            return file.getUnderlying(lPath);
        }
        return lPath.getIndirectionPath();
    }

    /**
     * Make the indicated node a primary indirection.
     * @param path The path to the node.
     */
    public void makePrimary(String path)
    {
        Lookup lPath = lookupDirectory(-1, path, true);
        if (lPath == null)
        {
            throw new AVMNotFoundException("Path " + path + " not found.");
        }
        DirectoryNode dir = (DirectoryNode)lPath.getCurrentNode();
        if (!lPath.isLayered())
        {
            throw new AVMException("Not in a layered context: " + path);
        }
        if (!fAVMRepository.can(this, dir, PermissionService.WRITE_PROPERTIES, lPath.getDirectlyContained()))
        {
            throw new AccessDeniedException("Not allowed to write: " + path);
        }
        dir.turnPrimary(lPath);
        dir.updateModTime();
        
        AVMDAOs.Instance().fAVMNodeDAO.update(dir);
    }

    /**
     * Change the indirection of a layered directory.
     * @param path The path to the layered directory.
     * @param target The target indirection to set.
     */
    public void retargetLayeredDirectory(String path, String target)
    {
        Lookup lPath = lookupDirectory(-1, path, true);
        if (lPath == null)
        {
            throw new AVMNotFoundException("Path " + path + " not found.");
        }
        DirectoryNode dir = (DirectoryNode)lPath.getCurrentNode();
        if (!lPath.isLayered())
        {
            throw new AVMException("Not in a layered context: " + path);
        }
        if (!fAVMRepository.can(this, dir, PermissionService.WRITE_PROPERTIES, lPath.getDirectlyContained()))
        {
            throw new AccessDeniedException("Not allowed to write: " + path);
        }
        dir.retarget(lPath, target);
        dir.updateModTime();
        
        AVMDAOs.Instance().fAVMNodeDAO.update(dir);
    }

    /**
     * Set the name of this AVMStore.
     * @param name
     */
    public void setName(String name)
    {
        fName = name;
    }

    /**
     * Get the name of this AVMStore.
     * @return The name.
     */
    public String getName()
    {
        return fName;
    }

    /* (non-Javadoc)
     * @see org.alfresco.repo.avm.AVMStore#getAcl()
     */
    public Acl getStoreAcl()
    {
        return fACL;
    }

    public void setStoreAcl(Acl acl)
    {
        fACL = acl;
    }

    /**
     * Set the next version id.
     * @param nextVersionID
     */
    public void setNextVersionID(int nextVersionID)
    {
        fNextVersionID = nextVersionID;
    }

    /**
     * Get the next version id.
     * @return The next version id.
     */
    public int getNextVersionID()
    {
        return fNextVersionID;
    }

    /**
     * This gets the last extant version id.
     */
    public int getLastVersionID()
    {
        Integer lastVersionId = AVMDAOs.Instance().fVersionRootDAO.getMaxVersionID(this);
        if (lastVersionId == null)
        {
            return 0;
        }
        else
        {
            return lastVersionId.intValue();
        }
    }

    /**
     * Set the root directory.
     * @param root
     */
    public void setRoot(DirectoryNode root)
    {
        fRoot = root;
    }

    /**
     * Get the root directory.
     * @return The root directory.
     */
    public DirectoryNode getRoot()
    {
        return fRoot;
    }

    /**
     * Set the version (for concurrency control).
     * @param vers  The version for optimistic locks.
     */
    public void setVers(long vers)
    {
        fVers = vers;
    }

    /**
     * Get the version (for concurrency control).
     * @return The version for optimistic locks.
     */
    public long getVers()
    {
        return fVers;
    }

    /**
     * Equals override.
     * @param obj
     * @return Equality.
     */
    @Override
    public boolean equals(Object obj)
    {
        if (this == obj)
        {
            return true;
        }
        if (!(obj instanceof AVMStore))
        {
            return false;
        }
        return getId() == ((AVMStore)obj).getId();
    }

    /**
     * Get a hash code.
     * @return The hash code.
     */
    @Override
    public int hashCode()
    {
        return (int)getId();
    }

    /**
     * Purge all nodes reachable only via this version and repository.
     * @param version
     */
    public void purgeVersion(int version)
    {
        if (version == 0)
        {
            throw new AVMBadArgumentException("Cannot purge initial version");
        }
        VersionRoot vRoot = AVMDAOs.Instance().fVersionRootDAO.getByVersionID(this, version);
        if (vRoot == null)
        {
            throw new AVMNotFoundException("Version not found.");
        }
        AVMDAOs.Instance().fVersionLayeredNodeEntryDAO.delete(vRoot);
        AVMNode root = vRoot.getRoot();
        if (!fAVMRepository.can(null, root, PermissionService.DELETE_CHILDREN, true))
        {
            throw new AccessDeniedException("Not allowed to purge: " + getName() + "@" + version);
        }
        root.setIsRoot(false);
        AVMDAOs.Instance().fAVMNodeDAO.update(root);
        AVMDAOs.Instance().fVersionRootDAO.delete(vRoot);
        if (root.equals(getRoot()))
        {
            // We have to set a new current root.
            vRoot = AVMDAOs.Instance().fVersionRootDAO.getMaxVersion(this);
            setRoot(vRoot.getRoot());
            AVMDAOs.Instance().fAVMStoreDAO.update(this);
        }
    }

    // TODO permissions?
    /**
     * Get the descriptor for this.
     * @return An AVMStoreDescriptor
     */
    public AVMStoreDescriptor getDescriptor()
    {
        // Get the creator ensuring that nulls are not hit
        PropertyValue creatorValue = getProperty(ContentModel.PROP_CREATOR);
        String creator = (creatorValue == null ? AuthenticationUtil.SYSTEM_USER_NAME : (String) creatorValue.getValue(DataTypeDefinition.TEXT));
        creator = (creator == null ? AuthenticationUtil.SYSTEM_USER_NAME : creator);
        // Get the created date ensuring that nulls are not hit
        PropertyValue createdValue = getProperty(ContentModel.PROP_CREATED);
        Date created = createdValue == null ? (new Date()) : (Date) createdValue.getValue(DataTypeDefinition.DATE);
        created = (created == null) ? (new Date()) : created;
        return new AVMStoreDescriptor(getId(), getName(), creator, created.getTime());
    }

    /**
     * Set the opacity of a layered directory. An opaque directory hides
     * what is pointed at by its indirection.
     * @param path The path to the layered directory.
     * @param opacity True is opaque; false is not.
     */
    public void setOpacity(String path, boolean opacity)
    {
        Lookup lPath = lookup(-1, path, true, false);
        if (lPath == null)
        {
            throw new AVMNotFoundException("Path " + path + " not found.");
        }
        AVMNode node = lPath.getCurrentNode();
        if (!(node instanceof LayeredDirectoryNode))
        {
            throw new AVMWrongTypeException("Not a LayeredDirectoryNode.");
        }
        if (!fAVMRepository.can(this, node, PermissionService.WRITE_PROPERTIES, lPath.getDirectlyContained()))
        {
            throw new AccessDeniedException("Not allowed to write: " + path);
        }
        ((LayeredDirectoryNode)node).setOpacity(opacity);
        node.updateModTime();
        
        AVMDAOs.Instance().fAVMNodeDAO.update(node);
    }

    // TODO Does it make sense to set properties on DeletedNodes?
    /**
     * Set a property on a node.
     * @param path The path to the node.
     * @param name The name of the property.
     * @param value The value to set.
     */
    public void setNodeProperty(String path, QName name, PropertyValue value)
    {
        Lookup lPath = lookup(-1, path, true, true);
        if (lPath == null)
        {
            throw new AVMNotFoundException("Path " + path + " not found.");
        }
        AVMNode node = lPath.getCurrentNode();
        if (!fAVMRepository.can(this, node, PermissionService.WRITE_PROPERTIES, lPath.getDirectlyContained()))
        {
            throw new AccessDeniedException("Not allowed to write: " + path);
        }
        
        node.setProperty(name, value);
        
        node.setGuid(GUID.generate());
        
        AVMDAOs.Instance().fAVMNodeDAO.update(node); // guid and property
    }

    /**
     * Set a collection of properties on a node.
     * @param path The path to the node.
     * @param properties The Map of QNames to PropertyValues.
     */
    public void setNodeProperties(String path, Map<QName, PropertyValue> properties)
    {
        Lookup lPath = lookup(-1, path, true, true);
        if (lPath == null)
        {
            throw new AVMNotFoundException("Path " + path + " not found.");
        }
        AVMNode node = lPath.getCurrentNode();
        if (!fAVMRepository.can(this, node, PermissionService.WRITE_PROPERTIES, lPath.getDirectlyContained()))
        {
            throw new AccessDeniedException("Not allowed to write: " + path);
        }
        if (properties != null)
        {
            Map<QName, PropertyValue> props = new HashMap<QName, PropertyValue>(properties.size());
            props.putAll(properties);
            node.addProperties(props);
        }
        node.setGuid(GUID.generate());
        
        AVMDAOs.Instance().fAVMNodeDAO.update(node);
    }

    /**
     * Get a property by name.
     * @param version The version to lookup.
     * @param path The path to the node.
     * @param name The name of the property.
     * @return A PropertyValue or null if not found.
     */
    public PropertyValue getNodeProperty(int version, String path, QName name)
    {
        Lookup lPath = lookup(version, path, false, true);
        if (lPath == null)
        {
            throw new AVMNotFoundException("Path " + path + " not found.");
        }
        AVMNode node = lPath.getCurrentNode();
        if (!fAVMRepository.can(this, node, PermissionService.READ_PROPERTIES, lPath.getDirectlyContained()))
        {
            throw new AccessDeniedException("Not allowed to read: " + path);
        }
        
        return node.getProperty(name);
    }

    /**
     * Get all the properties associated with a node.
     * @param version The version to lookup.
     * @param path The path to the node.
     * @return A Map of QNames to PropertyValues.
     */
    public Map<QName, PropertyValue> getNodeProperties(int version, String path)
    {
        Lookup lPath = lookup(version, path, false, true);
        if (lPath == null)
        {
            throw new AVMNotFoundException("Path " + path + " not found.");
        }
        AVMNode node = lPath.getCurrentNode();
        if (!fAVMRepository.can(this, node, PermissionService.READ_PROPERTIES, lPath.getDirectlyContained()))
        {
            throw new AccessDeniedException("Not allowed to read: " + path);
        }
        
        return node.getProperties();
    }

    /**
     * Delete a single property from a node.
     * @param path The path to the node.
     * @param name The name of the property.
     */
    public void deleteNodeProperty(String path, QName name)
    {
        Lookup lPath = lookup(-1, path, true, true);
        if (lPath == null)
        {
            throw new AVMNotFoundException("Path " + path + " not found.");
        }
        AVMNode node = lPath.getCurrentNode();
        if (!fAVMRepository.can(this, node, PermissionService.WRITE_PROPERTIES, lPath.getDirectlyContained()))
        {
            throw new AccessDeniedException("Not allowed to write: " + path);
        }
        node.setGuid(GUID.generate());

        node.deleteProperty(name);
        
        AVMDAOs.Instance().fAVMNodeDAO.update(node);
    }

    /**
     * Delete all properties from a node.
     * @param path The path to the node.
     */
    public void deleteNodeProperties(String path)
    {
        Lookup lPath = lookup(-1, path, true, true);
        if (lPath == null)
        {
            throw new AVMNotFoundException("Path " + path + " not found.");
        }
        AVMNode node = lPath.getCurrentNode();
        if (!fAVMRepository.can(this, node, PermissionService.WRITE_PROPERTIES, lPath.getDirectlyContained()))
        {
            throw new AccessDeniedException("Not allowed to write: " + path);
        }
        node.setGuid(GUID.generate());
        node.deleteProperties();
        
        AVMDAOs.Instance().fAVMNodeDAO.update(node);
    }

    /**
     * Set a property on this store. Replaces if property already exists.
     * @param name The QName of the property.
     * @param value The actual PropertyValue.
     */
    public void setProperty(QName name, PropertyValue value)
    {
        AVMStoreProperty prop = new AVMStorePropertyImpl();
        prop.setStore(this);
        prop.setQname(name);
        prop.setValue(value);
        AVMDAOs.Instance().fAVMStorePropertyDAO.save(prop);
    }

    /**
     * Set a group of properties on this store. Replaces any property that exists.
     * @param properties A Map of QNames to PropertyValues to set.
     */
    public void setProperties(Map<QName, PropertyValue> properties)
    {
        for (QName name : properties.keySet())
        {
            setProperty(name, properties.get(name));
        }
    }

    /**
     * Get a property by name.
     * @param name The QName of the property to fetch.
     * @return The PropertyValue or null if non-existent.
     */
    public PropertyValue getProperty(QName name)
    {
        return AVMDAOs.Instance().fAVMStorePropertyDAO.get(this, name);
    }

    /**
     * Get all the properties associated with this store.
     * @return A Map of the properties.
     */
    public Map<QName, PropertyValue> getProperties()
    {
        return AVMDAOs.Instance().fAVMStorePropertyDAO.get(this);
    }

    /**
     * Delete a property.
     * @param name The name of the property to delete.
     */
    public void deleteProperty(QName name)
    {
        AVMDAOs.Instance().fAVMStorePropertyDAO.delete(this, name);
    }

    /**
     * Get the ContentData on a file.
     * @param version The version to look under.
     * @param path The path to the file.
     * @return The ContentData corresponding to the file.
     */
    public ContentData getContentDataForRead(int version, String path)
    {
        Lookup lPath = lookup(version, path, false, false);
        if (lPath == null)
        {
            throw new AVMNotFoundException("Path " + path + " not found.");
        }
        AVMNode node = lPath.getCurrentNode();
        if (!(node instanceof FileNode))
        {
            throw new AVMWrongTypeException("File Expected.");
        }
        if (!fAVMRepository.can(this, node, PermissionService.READ_CONTENT, lPath.getDirectlyContained()))
        {
            throw new AccessDeniedException("Not allowed to read: " + path);
        }
        ContentData content = ((FileNode)node).getContentData(lPath);
        // AVMDAOs.Instance().fAVMNodeDAO.evict(node);
        return content;
    }

    /**
     * Get the ContentData on a file for writing.
     * @param path The path to the file.
     * @return The ContentData corresponding to the file.
     */
    public ContentData getContentDataForWrite(String path)
    {
        Lookup lPath = lookup(-1, path, true, false);
        if (lPath == null)
        {
            throw new AVMNotFoundException("Path " + path + " not found.");
        }
        AVMNode node = lPath.getCurrentNode();
        if (!(node instanceof FileNode))
        {
            throw new AVMWrongTypeException("File Expected.");
        }
        if (!fAVMRepository.can(this, node, PermissionService.WRITE_CONTENT, lPath.getDirectlyContained()))
        {
            throw new AccessDeniedException("Not allowed to write content: " + path);
        }
        // TODO Set modifier.
        node.updateModTime();
        node.setGuid(GUID.generate());
        
        //AVMDAOs.Instance().fAVMNodeDAO.update(node);
        // TODO review 'optimisation'
        AVMDAOs.Instance().fAVMNodeDAO.updateModTimeAndGuid(node);
        
        ContentData content = ((FileNode)node).getContentData(lPath);
        // AVMDAOs.Instance().fAVMNodeDAO.evict(node);
        return content;
    }

    // Not doing permission checking because it will already have been done
    // at the getContentDataForWrite point.
    /**
     * Set the ContentData for a file.
     * @param path The path to the file.
     * @param data The ContentData to set.
     */
    public void setContentData(String path, ContentData data)
    {
        Lookup lPath = lookup(-1, path, true, false);
        if (lPath == null)
        {
            throw new AVMNotFoundException("Path " + path + " not found.");
        }
        AVMNode node = lPath.getCurrentNode();
        if (!(node instanceof FileNode))
        {
            throw new AVMWrongTypeException("File Expected.");
        }
        ((FileNode)node).setContentData(data);
        node.updateModTime();
        
        AVMDAOs.Instance().fAVMNodeDAO.update(node);
    }

    /**
     * Set meta data, aspects, properties, acls, from another node.
     * @param path The path to the node to set metadata on.
     * @param from The node to get the metadata from.
     */
    public void setMetaDataFrom(String path, AVMNode from)
    {
        Lookup lPath = lookup(-1, path, true, true);
        if (lPath == null)
        {
            throw new AVMNotFoundException("Path not found: " + path);
        }
        AVMNode node = lPath.getCurrentNode();
        if (!fAVMRepository.can(this, node, PermissionService.WRITE_PROPERTIES, lPath.getDirectlyContained()))
        {
            throw new AccessDeniedException("Not allowed to write properties: " + path);
        }
        node.copyMetaDataFrom(from, node.getAcl() == null ? null : node.getAcl().getInheritsFrom());
        node.setGuid(GUID.generate());
        
        AVMDAOs.Instance().fAVMNodeDAO.update(node);
    }

    /**
     * Add an aspect to a node.
     * @param path The path to the node.
     * @param aspectName The name of the aspect.
     */
    public void addAspect(String path, QName aspectName)
    {
        Lookup lPath = lookup(-1, path, true, true);
        if (lPath == null)
        {
            throw new AVMNotFoundException("Path " + path + " not found.");
        }
        AVMNode node = lPath.getCurrentNode();
        if (!fAVMRepository.can(this, node, PermissionService.WRITE_PROPERTIES, lPath.getDirectlyContained()))
        {
            throw new AccessDeniedException("Not allowed to write: " + path);
        }
        
        node.addAspect(aspectName);
        node.setGuid(GUID.generate());
        
        AVMDAOs.Instance().fAVMNodeDAO.update(node);
    }

    /**
     * Get all aspects on a given node.
     * @param version The version to look under.
     * @param path The path to the node.
     * @return A List of the QNames of the aspects.
     */
    public Set<QName> getAspects(int version, String path)
    {
        Lookup lPath = lookup(version, path, false, true);
        if (lPath == null)
        {
            throw new AVMNotFoundException("Path " + path + " not found.");
        }
        AVMNode node = lPath.getCurrentNode();
        if (!fAVMRepository.can(this, node, PermissionService.READ_PROPERTIES, lPath.getDirectlyContained()))
        {
            throw new AccessDeniedException("Not allowed to read properties: " + path);
        }
        
        return node.getAspects();
    }

    /**
     * Remove an aspect and all its properties from a node.
     * @param path The path to the node.
     * @param aspectName The name of the aspect.
     */
    public void removeAspect(String path, QName aspectName)
    {
        Lookup lPath = lookup(-1, path, true, true);
        if (lPath == null)
        {
            throw new AVMNotFoundException("Path " + path + " not found.");
        }
        AVMNode node = lPath.getCurrentNode();
        if (!fAVMRepository.can(this, node, PermissionService.WRITE_PROPERTIES, lPath.getDirectlyContained()))
        {
            throw new AccessDeniedException("Not allowed to write properties: " + path);
        }
        
        node.removeAspect(aspectName);
        
        AspectDefinition def = RawServices.Instance().getDictionaryService().getAspect(aspectName);
        Map<QName, PropertyDefinition> properties = def.getProperties();
        
        for (QName propertyQName : properties.keySet())
        {
            node.deleteProperty(propertyQName);
        }
        
        node.setGuid(GUID.generate());
        
        AVMDAOs.Instance().fAVMNodeDAO.update(node);
    }

    /**
     * Does a given node have a given aspect.
     * @param version The version to look under.
     * @param path The path to the node.
     * @param aspectName The name of the aspect.
     * @return Whether the node has the aspect.
     */
    public boolean hasAspect(int version, String path, QName aspectName)
    {
        Lookup lPath = lookup(version, path, false, true);
        if (lPath == null)
        {
            throw new AVMNotFoundException("Path " + path + " not found.");
        }
        AVMNode node = lPath.getCurrentNode();
        if (!fAVMRepository.can(this, node, PermissionService.READ_PROPERTIES, lPath.getDirectlyContained()))
        {
            throw new AccessDeniedException("Not allowed to read properties: " + path);
        }
        
        return node.getAspects().contains(aspectName);
    }

    /**
     * Set the ACL on a node.
     * @param path The path to the node.
     * @param acl The ACL to set.
     */
    public void setACL(String path, Acl acl)
    {
        Lookup lPath = lookup(-1, path, true, true);
        if (lPath == null)
        {
            throw new AVMNotFoundException("Path " + path + " not found.");
        }
        AVMNode node = lPath.getCurrentNode();
        if (!fAVMRepository.can(this, node, PermissionService.CHANGE_PERMISSIONS, lPath.getDirectlyContained()))
        {
            throw new AccessDeniedException("Not allowed to change permissions: " + path);
        }
        node.setAcl(acl);
        node.setGuid(GUID.generate());
        
        AVMDAOs.Instance().fAVMNodeDAO.update(node);
    }

    /**
     * Get the ACL on a node.
     * @param version The version to look under.
     * @param path The path to the node.
     * @return The ACL.
     */
    public Acl getACL(int version, String path)
    {
        Lookup lPath = lookup(version, path, false, false);
        if (lPath == null)
        {
            throw new AVMNotFoundException("Path " + path + " not found.");
        }
        if (!fAVMRepository.can(this, lPath.getCurrentNode(), PermissionService.READ_PERMISSIONS, lPath.getDirectlyContained()))
        {
            throw new AccessDeniedException("Not allowed to read permissions: " + path + " in "+getName());
        }
        return lPath.getCurrentNode().getAcl();
    }

    /**
     * Link a node into a directory, directly.
     * @param parentPath The path to the directory.
     * @param name The name to give the parent.
     * @param toLink The node to link.
     */
    public void link(String parentPath, String name, AVMNodeDescriptor toLink)
    {
        Lookup lPath = lookupDirectory(-1, parentPath, true);
        if (lPath == null)
        {
            String pathParts[] = AVMUtil.splitBase(parentPath);
            Lookup lPath2 = lookup(-1, pathParts[0], true, false);
            if (lPath2 != null)
            {
                DirectoryNode parent = (DirectoryNode)lPath2.getCurrentNode();
                Pair<AVMNode, Boolean> temp = parent.lookupChild(lPath2, pathParts[1], false);
                if ((temp != null) && (temp.getFirst() != null))
                {
                    DirectoryNode dir = (DirectoryNode)temp.getFirst();
                    
                    if (logger.isDebugEnabled())
                    {
                        logger.debug("Found: "+dir);
                    }
                    
                    boolean directlyContained = false;
                    
                    if (!fAVMRepository.can(null, dir, PermissionService.ADD_CHILDREN, directlyContained))
                    {
                        throw new AccessDeniedException("Not allowed to add children: " + parentPath);
                    }
                    
                    AVMNodeDescriptor desc = fAVMRepository.forceCopy(AVMUtil.buildAVMPath(this.getName(), parentPath));
                    fAVMRepository.link(desc, name, toLink);
                    return;
                }
            }
            
            throw new AVMNotFoundException("Path " + parentPath + " not found.");
        }
        DirectoryNode dir = (DirectoryNode)lPath.getCurrentNode();
        if (!fAVMRepository.can(null, dir, PermissionService.ADD_CHILDREN, lPath.getDirectlyContained()))
        {
            throw new AccessDeniedException("Not allowed to add children: " + parentPath);
        }
        dir.link(lPath, name, toLink);
    }
    
    /**
     * Update a link to a node in a directory, directly.
     * @param parentPath The path to the directory.
     * @param name The name to give the parent.
     * @param toLink The node to link.
     */
    public void updateLink(String parentPath, String name, AVMNodeDescriptor toLink)
    {
        Lookup lPath = lookupDirectory(-1, parentPath, true);
        if (lPath == null)
        {
            throw new AVMNotFoundException("Path " + parentPath + " not found.");
        }
        DirectoryNode dir = (DirectoryNode)lPath.getCurrentNode();
        
        Lookup cPath = new Lookup(lPath, AVMDAOs.Instance().fAVMNodeDAO, AVMDAOs.Instance().fAVMStoreDAO);
        Pair<AVMNode, Boolean> result = dir.lookupChild(cPath, name, true);
        if (result != null)
        {
            AVMNode child = result.getFirst();
            if (!fAVMRepository.can(null, child, PermissionService.WRITE, cPath.getDirectlyContained()))
            {
                throw new AccessDeniedException("Not allowed to update node: " +  parentPath + "/" +name );
            }
            dir.removeChild(lPath, name);
        }
        dir.link(lPath, name, toLink);
    }

    /**
     * Revert a head path to a given version. This works by cloning
     * the version to revert to, and then linking that new version into head.
     * The reverted version will have the previous head version as ancestor.
     * @param path The path to the parent directory.
     * @param name The name of the node to revert.
     * @param toRevertTo The descriptor of the version to revert to.
     */
    public void revert(String path, String name, AVMNodeDescriptor toRevertTo)
    {
        Lookup lPath = lookupDirectory(-1, path, true);
        if (lPath == null)
        {
            throw new AVMNotFoundException("Path " + path + " not found.");
        }
        DirectoryNode dir = (DirectoryNode)lPath.getCurrentNode();
       
        Pair<AVMNode, Boolean> temp = dir.lookupChild(lPath, name, true);
        AVMNode child = (temp == null) ? null : temp.getFirst();
        if (child == null)
        {
            throw new AVMNotFoundException("Node not found: " + name);
        }
        if (!fAVMRepository.can(null, child, PermissionService.WRITE, false))
        {
            throw new AccessDeniedException("Not allowed to revert: " + path);
        }
        AVMNode revertNode = AVMDAOs.Instance().fAVMNodeDAO.getByID(toRevertTo.getId());
        if (revertNode == null)
        {
            throw new AVMNotFoundException(toRevertTo.toString());
        }
        AVMNode toLink = revertNode.copy(lPath);
        dir.putChild(name, toLink);
        toLink.changeAncestor(child);
        toLink.setVersionID(child.getVersionID() + 1);
        
        toLink.addAspect(WCMModel.ASPECT_REVERTED);
        
        PropertyValue value = new PropertyValue(null, toRevertTo.getId());
        toLink.setProperty(WCMModel.PROP_REVERTED_ID, value);
        
        AVMDAOs.Instance().fAVMNodeDAO.update(toLink);
    }

    /* (non-Javadoc)
     * @see org.alfresco.repo.avm.AVMStore#setGuid(java.lang.String, java.lang.String)
     */
    public void setGuid(String path, String guid)
    {
        Lookup lPath = lookup(-1, path, true, true);
        if (lPath == null)
        {
            throw new AVMNotFoundException("Path not found: " + path);
        }
        AVMNode node = lPath.getCurrentNode();
        if (!fAVMRepository.can(this, node, PermissionService.WRITE_PROPERTIES, lPath.getDirectlyContained()))
        {
            throw new AccessDeniedException("Not allowed to write properties: " + path);
        }
        node.setGuid(guid);
        
        AVMDAOs.Instance().fAVMNodeDAO.update(node);
    }

    /* (non-Javadoc)
     * @see org.alfresco.repo.avm.AVMStore#setEncoding(java.lang.String, java.lang.String)
     */
    public void setEncoding(String path, String encoding)
    {
        Lookup lPath = lookup(-1, path, true, false);
        if (lPath == null)
        {
            throw new AVMNotFoundException("Path not found: " + path);
        }
        AVMNode node = lPath.getCurrentNode();
        if (node.getType() != AVMNodeType.PLAIN_FILE)
        {
            throw new AVMWrongTypeException("Not a File: " + path);
        }
        if (!fAVMRepository.can(this, node, PermissionService.WRITE_PROPERTIES, lPath.getDirectlyContained()))
        {
            throw new AccessDeniedException("Not allowed to write properties: " + path);
        }
        PlainFileNode file = (PlainFileNode)node;
        ContentData contentData = file.getContentData();
        contentData = ContentData.setEncoding(contentData, encoding);
        file.setContentData(contentData);
        
        AVMDAOs.Instance().fAVMNodeDAO.update(file);
    }

    /* (non-Javadoc)
     * @see org.alfresco.repo.avm.AVMStore#setMimeType(java.lang.String, java.lang.String)
     */
    public void setMimeType(String path, String mimeType)
    {
        Lookup lPath = lookup(-1, path, true, false);
        if (lPath == null)
        {
            throw new AVMNotFoundException("Path not found: " + path);
        }
        AVMNode node = lPath.getCurrentNode();
        if (node.getType() != AVMNodeType.PLAIN_FILE)
        {
            throw new AVMWrongTypeException("Not a File: " + path);
        }
        if (!fAVMRepository.can(this, node, PermissionService.WRITE_PROPERTIES, lPath.getDirectlyContained()))
        {
            throw new AccessDeniedException("Not allowed to write properties: " + path);
        }
        PlainFileNode file = (PlainFileNode)node;
        ContentData contentData = file.getContentData();
        contentData = ContentData.setMimetype(contentData, mimeType);
        file.setContentData(contentData);
        
        AVMDAOs.Instance().fAVMNodeDAO.update(file);
    }
    
    // for debug
    @Override
    public String toString()
    {
        return getName();
    }
}
