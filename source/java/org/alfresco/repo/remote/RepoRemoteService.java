/**
 * 
 */
package org.alfresco.repo.remote;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.model.FileFolderService;
import org.alfresco.service.cmr.model.FileInfo;
import org.alfresco.service.cmr.model.FileNotFoundException;
import org.alfresco.service.cmr.remote.RepoRemote;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.ContentService;
import org.alfresco.service.cmr.repository.ContentWriter;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.Pair;
import org.apache.log4j.Logger;

/**
 * Server side implementation of RepoRemote.
 * @author britt
 */
public class RepoRemoteService implements RepoRemote 
{
    private static Logger fgLogger = Logger.getLogger(RepoRemoteService.class);
    
    /**
     * The NodeService instance.
     */
    private NodeService fNodeService;
    
    /**
     * The ContentService instance.
     */
    private ContentService fContentService;
    
    /**
     * The FileFolderService instance.
     */
    private FileFolderService fFileFolderService;
    
    /**
     * Default constructor.
     */
    public RepoRemoteService()
    {
    }
    
    /**
     * Set the NodeService instance.
     */
    public void setNodeService(NodeService service)
    {
        fNodeService = service;
    }
    
    /**
     * Set the ContentService instance.
     */
    public void setContentService(ContentService service)
    {
        fContentService = service;
    }
    
    /**
     * Set the FileFolderService instance.
     */
    public void setFileFolderService(FileFolderService service)
    {
        fFileFolderService = service;
    }
    
    /**
     * Path splitting utility.
     * @param path The path.
     * @return A List of components.
     */
    private List<String> splitPath(String path)
    {
        String [] pathComponents = path.split("/+");
        List<String> pathList = new ArrayList<String>(pathComponents.length);
        for (String comp : pathComponents)
        {
            pathList.add(comp);
        }
        return pathList;
    }
    
    /* (non-Javadoc)
     * @see org.alfresco.service.cmr.remote.RepoRemote#createDirectory(org.alfresco.service.cmr.repository.NodeRef, java.lang.String)
     */
    public NodeRef createDirectory(NodeRef base, String path) 
    {
        Pair<NodeRef, String> parentChild = getParentChildRelative(base, path);
        FileInfo created = fFileFolderService.create(parentChild.getFirst(), 
                                                     parentChild.getSecond(),
                                                     ContentModel.TYPE_FOLDER);
        return created.getNodeRef();
    }

    /* (non-Javadoc)
     * @see org.alfresco.service.cmr.remote.RepoRemote#createFile(org.alfresco.service.cmr.repository.NodeRef, java.lang.String)
     */
    public OutputStream createFile(NodeRef base, String path) 
    {
        Pair<NodeRef, String> parentChild = getParentChildRelative(base, path);
        FileInfo info = fFileFolderService.create(parentChild.getFirst(), 
                                                  parentChild.getSecond(), 
                                                  ContentModel.TYPE_CONTENT);
        // TODO is update supposed to be true.
        ContentWriter writer = fContentService.getWriter(info.getNodeRef(), ContentModel.PROP_CONTENT, true);
        return writer.getContentOutputStream();
    }

    /* (non-Javadoc)
     * @see org.alfresco.service.cmr.remote.RepoRemote#getListing(org.alfresco.service.cmr.repository.NodeRef)
     */
    public Map<String, Pair<NodeRef, QName>> getListing(NodeRef dir) 
    {
        List<ChildAssociationRef> listing = fNodeService.getChildAssocs(dir);
        Map<String, Pair<NodeRef, QName>> result = new TreeMap<String, Pair<NodeRef, QName>>();
        for (ChildAssociationRef child : listing)
        {
            fgLogger.error(child.getQName());
            NodeRef childRef = child.getChildRef();
            QName type = fNodeService.getType(childRef);
            if (type.equals(ContentModel.TYPE_CONTENT) ||
                type.equals(ContentModel.TYPE_FOLDER))
            {
                result.put(child.getQName().getLocalName(), 
                           new Pair<NodeRef, QName>(child.getChildRef(), 
                                                    type));
            }
        }
        return result;
    }

    /* (non-Javadoc)
     * @see org.alfresco.service.cmr.remote.RepoRemote#getRoot()
     */
    public NodeRef getRoot() 
    {
        NodeRef storeRoot = fNodeService.getRootNode(new StoreRef(StoreRef.PROTOCOL_WORKSPACE, "SpacesStore"));
        List<ChildAssociationRef> listing = fNodeService.getChildAssocs(storeRoot);
        for (ChildAssociationRef child : listing)
        {
            fgLogger.error(child.getQName().getLocalName());
            if (child.getQName().getLocalName().equals("company_home"))
            {
                return child.getChildRef();
            }
        }
        throw new AlfrescoRuntimeException("Root Not Found!");
    }

    /* (non-Javadoc)
     * @see org.alfresco.service.cmr.remote.RepoRemote#lookup(org.alfresco.service.cmr.repository.NodeRef, java.lang.String)
     */
    public NodeRef lookup(NodeRef base, String path) 
    {
        List<String> pathList = splitPath(path);
        NodeRef curr = base;
        for (String name : pathList)
        {
            fgLogger.error(name);
            NodeRef next = fNodeService.getChildByName(curr, ContentModel.ASSOC_CONTAINS, name);
            if (next == null)
            {
                fgLogger.error("Wasn't a contains.");
                next = fNodeService.getChildByName(curr, ContentModel.ASSOC_CHILDREN, name);
                if (next == null)
                {
                    return null;
                }
            }
            curr = next;
        }
        return curr;
    }

    /* (non-Javadoc)
     * @see org.alfresco.service.cmr.remote.RepoRemote#readFile(org.alfresco.service.cmr.repository.NodeRef)
     */
    public InputStream readFile(NodeRef fileRef) 
    {
        return fContentService.getReader(fileRef, ContentModel.PROP_CONTENT).getContentInputStream();
    }

    /* (non-Javadoc)
     * @see org.alfresco.service.cmr.remote.RepoRemote#readFile(org.alfresco.service.cmr.repository.NodeRef, java.lang.String)
     */
    public InputStream readFile(NodeRef base, String path) 
    {
        NodeRef fileRef = lookup(base, path);
        if (fileRef == null)
        {
            throw new AlfrescoRuntimeException("Not Found: " + path);
        }
        return fContentService.getReader(fileRef, ContentModel.PROP_CONTENT).getContentInputStream();
    }

    /* (non-Javadoc)
     * @see org.alfresco.service.cmr.remote.RepoRemote#removeNode(org.alfresco.service.cmr.repository.NodeRef)
     */
    public void removeNode(NodeRef toRemove) 
    {
        fNodeService.deleteNode(toRemove);
    }

    /* (non-Javadoc)
     * @see org.alfresco.service.cmr.remote.RepoRemote#removeNode(org.alfresco.service.cmr.repository.NodeRef, java.lang.String)
     */
    public void removeNode(NodeRef base, String path) 
    {
        NodeRef toRemove = lookup(base, path);
        if (toRemove == null)
        {
            throw new AlfrescoRuntimeException("Not Found: " + path);
        }
        fNodeService.deleteNode(toRemove);
    }

    /* (non-Javadoc)
     * @see org.alfresco.service.cmr.remote.RepoRemote#rename(org.alfresco.service.cmr.repository.NodeRef, java.lang.String, java.lang.String)
     */
    public void rename(NodeRef base, String src, String dst) 
    {
        NodeRef srcRef = lookup(base, src);
        if (srcRef == null)
        {
            throw new AlfrescoRuntimeException("Not Found: " + src);
        }
        Pair<NodeRef, String> parentChild = getParentChildRelative(base, dst);
        try
        {
            fFileFolderService.move(srcRef, parentChild.getFirst(), parentChild.getSecond());
        }
        catch (FileNotFoundException e)
        {
            throw new AlfrescoRuntimeException("Parent Not Found: " + dst, e);
        }
    }

    /* (non-Javadoc)
     * @see org.alfresco.service.cmr.remote.RepoRemote#writeFile(org.alfresco.service.cmr.repository.NodeRef, java.lang.String)
     */
    public OutputStream writeFile(NodeRef base, String path) 
    {
        NodeRef target = lookup(base, path);
        return fContentService.getWriter(target, ContentModel.PROP_CONTENT, true).getContentOutputStream();
    }
    
    /**
     * Utility for getting the parent NodeRef of a relative path.
     * @param base The base node ref.
     * @param path The relative path.
     * @return A Pair with the parent node ref and the name of the child.
     */
    private Pair<NodeRef, String> getParentChildRelative(NodeRef base, String path)
    {
        List<String> pathList = splitPath(path);
        NodeRef parent;
        String name = null;
        if (pathList.size() == 1)
        {
            parent = base;
            name = pathList.get(0);
        }
        else
        {
            try
            {
                name = pathList.get(pathList.size() - 1);
                pathList.remove(pathList.size() - 1);
                FileInfo info = fFileFolderService.resolveNamePath(base, pathList);
                parent = info.getNodeRef();
            }
            catch (FileNotFoundException e)
            {
                throw new AlfrescoRuntimeException("Not Found: " + pathList, e);
            }
        }
        return new Pair<NodeRef, String>(parent, name);
    }
}
