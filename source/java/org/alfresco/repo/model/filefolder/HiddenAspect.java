package org.alfresco.repo.model.filefolder;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.Path;
import org.alfresco.service.cmr.repository.Path.Element;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.FileFilterMode.Client;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Functionality relating to hidden files and folders.
 * 
 * Support for nodes marked as hidden but with visibility constraints for specific clients. A node
 * can have the hidden aspect applied, which means that the node is hidden. However, 
 * for specific clients it can be defined whether the node is visible or will have its hidden attribute
 * set in FileInfo.
 *
 */
public class HiddenAspect
{
    private static Log logger = LogFactory.getLog(HiddenAspect.class);

    public static enum Visibility
    {
        NotVisible, Visible, HiddenAttribute;
        
        public int getMask()
        {
            if(this == Visible)
            {
                return 2;
            }
            else if(this == HiddenAttribute)
            {
                return 1;
            }
            else if(this == NotVisible)
            {
                return 0;
            }
            else
            {
                throw new IllegalArgumentException();
            }
        }
        
        public static Visibility getVisibility(int mask)
        {
            if(mask == 2)
            {
                return Visible;
            }
            else if(mask == 1)
            {
                return HiddenAttribute;
            }
            else if(mask == 0)
            {
                return NotVisible;
            }
            else
            {
                throw new IllegalArgumentException();
            }
        }
    };

    private List<HiddenFileInfoImpl> filters = new ArrayList<HiddenFileInfoImpl>(10);
    
    private NodeService nodeService;

    public HiddenAspect()
    {
    }
    
    public void setNodeService(NodeService nodeService)
    {
        this.nodeService = nodeService;
    }
    
    public void setPatterns(List<HiddenFileFilter> filters)
    {
        for(HiddenFileFilter filter : filters)
        {
            this.filters.add(new HiddenFileInfoImpl(filter.getFilter(), filter.getVisibility(), filter.getHiddenAttribute()));
        }
    }
    
    public Client[] getClients()
    {
        return Client.values();
    }
    
    private Integer getClientIndex(Client client)
    {
        return client.ordinal();
    }

    private void addIndexControlAspect(NodeRef nodeRef)
    {
        Map<QName, Serializable> props = new HashMap<QName, Serializable>(2);
        props.put(ContentModel.PROP_IS_INDEXED, Boolean.FALSE);
        props.put(ContentModel.PROP_IS_CONTENT_INDEXED, Boolean.FALSE);
        nodeService.addAspect(nodeRef, ContentModel.ASPECT_INDEX_CONTROL, props);
        
        if (logger.isDebugEnabled())
        {
            logger.debug("Applied index control marker: " + nodeRef);
        }
    }

    private void addHiddenAspect(NodeRef nodeRef, int visibilityMask)
    {
        Map<QName, Serializable> props = new HashMap<QName, Serializable>(1);
        props.put(ContentModel.PROP_VISIBILITY_MASK, visibilityMask);
        nodeService.addAspect(nodeRef, ContentModel.ASPECT_HIDDEN, props);

        if (logger.isDebugEnabled())
        {
            logger.debug("Applied hidden marker: " + nodeRef);
        }
    }
    
    private void removeHiddenAspect(NodeRef nodeRef)
    {
        // Remove the aspect
        nodeService.removeAspect(nodeRef, ContentModel.ASPECT_HIDDEN);
        
        if (logger.isDebugEnabled())
        {
            logger.debug("Removed hidden marker: " + nodeRef);
        }        
    }

    private Visibility getVisibility(Integer mask, Client client)
    {
        if(mask == null || mask.intValue() == 0)
        {
            return Visibility.NotVisible;
        }
        
        mask = (mask.intValue() >> (getClientIndex(client))*2) & 3;

        return Visibility.getVisibility(mask);
    }

    /*
     * Determines whether the path matches any one of the hidden file patterns and, if so,
     * returns the matching pattern.
     * 
     * @param path
     * @return
     */
    private HiddenFileInfo isHidden(String path)
    {
        // check against all the filters
        HiddenFileInfoImpl matched = null;

        for(HiddenFileInfoImpl filter : filters)
        {
            if(filter.isHidden(path))
            {
                matched = filter;
                break;
            }
        }

        return matched;
    }
    
    public int getClientVisibilityMask(Client client, Visibility visibility)
    {
        return visibility.getMask() << getClientIndex(client)*2;
    }
    
    /**
     * Checks whether the node is on a hidden path
     *
     * @param nodeRef
     * @return the matching filter, or null if no match
     */
    public HiddenFileInfo isHidden(NodeRef nodeRef)
    {
        HiddenFileInfo ret = null;
        // TODO would be nice to check each part of the path in turn, bailing out if a match is found
        Path path = nodeService.getPath(nodeRef);

        Iterator<Element> it = path.iterator();
        while(it.hasNext())
        {
            Path.ChildAssocElement elem = (Path.ChildAssocElement)it.next();
            QName qname = elem.getRef().getQName();
            if(qname != null)
            {
                ret = isHidden(qname.getLocalName());
                if(ret != null)
                {
                    break;
                }
            }
        }

        return ret;
    }

    /**
     * Hides the node by applying the hidden and not indexed aspects. The node will be hidden from all clients.
     * 
     * @param client
     * @param fileInfo
     * @return
     */
    public void hideNode(NodeRef nodeRef)
    {
        addHiddenAspect(nodeRef, 0);
        addIndexControlAspect(nodeRef);
    }
    
    /**
     * Hides the node by applying the hidden and not indexed aspects. The node will be hidden from clients
     * according to the visibility mask.
     * 
     * @param client
     * @param fileInfo
     * @return
     */
    public void hideNode(NodeRef nodeRef, int clientVisibilityMask)
    {
        addHiddenAspect(nodeRef, clientVisibilityMask);
        addIndexControlAspect(nodeRef);
    }
    
    /**
     * Checks whether the file should be hidden and applies the hidden and not indexed aspects if so.
     * 
     * @param fileInfo
     * @return
     */
    public void checkHidden(FileInfoImpl fileInfo)
    {
        NodeRef nodeRef = fileInfo.getNodeRef();
        HiddenFileInfo hiddenFileInfo = checkHidden(nodeRef);
        if(hiddenFileInfo != null)
        {
            fileInfo.setHidden(true);
        }
    }

    /**
     * Hides the node by applying the hidden and not indexed aspects. The node will be hidden from clients
     * according to the visibility mask.
     * 
     * @param client
     * @param fileInfo
     * @return
     */
    public void hideNode(FileInfoImpl fileInfo, int visibilityMask)
    {
        hideNode(fileInfo.getNodeRef(), visibilityMask);
        fileInfo.setHidden(true);
    }
    
    /**
     * Checks whether the file should be hidden and applies the hidden and not indexed aspects if so.
     * 
     * @param fileInfo
     * @return
     */
    public HiddenFileInfo checkHidden(NodeRef nodeRef)
    {
        HiddenFileInfo filter = isHidden(nodeRef);
        if(filter != null)
        {
            // the file matches a pattern, apply the hidden and aspect control aspects
            addHiddenAspect(nodeRef, filter.getVisibilityMask());
            addIndexControlAspect(nodeRef);
        }
        else
        {
            // the file does not match the pattern, ensure that the hidden aspect is not present
            if(nodeService.hasAspect(nodeRef, ContentModel.ASPECT_HIDDEN))
            {
                removeHiddenAspect(nodeRef);
            }            
        }

        return filter;
    }

    /**
     * Gets the visibility constraint for the given client on the given node.
     * 
     * @param client
     * @param nodeRef
     * 
     * @return the visibility constraint for the given client and node
     */
    public Visibility getVisibility(Client client, NodeRef nodeRef)
    {
        Visibility ret = null;

        if(nodeService.hasAspect(nodeRef, ContentModel.ASPECT_HIDDEN))
        {
            Integer visibilityMask = (Integer)nodeService.getProperty(nodeRef, ContentModel.PROP_VISIBILITY_MASK);
            if(visibilityMask != null)
            {
                if(visibilityMask.intValue() == 0)
                {
                    ret = Visibility.NotVisible;
                }
                else if(client == null)
                {
                    ret = Visibility.NotVisible;
                }
                else
                {
                    ret = getVisibility(visibilityMask.intValue(), client);
                }
            }
            else
            {
                // no visibility mask property, so retain backwards compatibility with 3.4 hidden aspect behaviour
                if(client == Client.cifs)
                {
                    ret = Visibility.HiddenAttribute;
                }
                else if(client == Client.webdav || client == Client.nfs || client == Client.imap)
                {
                    ret = Visibility.Visible;
                }
                else
                {
                    ret = Visibility.NotVisible;
                }
            }
        }

        return ret;
    }

    private class HiddenFileInfoImpl implements HiddenFileInfo
    {
        private Pattern filter;
        private Set<Client> clientVisibility = new HashSet<Client>(10);
        private Set<Client> hiddenAttribute = new HashSet<Client>(10);
        private int visibilityMask;

        public HiddenFileInfoImpl(String regexp, String visibility, String hiddenAttribute)
        {
            this.filter = Pattern.compile(regexp);
            setVisibility(visibility);
            setHiddenAttribute(hiddenAttribute);
            calculateVisibilityMask();
        }

        private void setVisibility(String visibility)
        {
            if(visibility != null && !visibility.equals(""))
            {
                for(String clientStr : visibility.split(","))
                {
                    Client client = Client.getClient(clientStr);
                    this.clientVisibility.add(client);
                }
            }
        }
        
        private void setHiddenAttribute(String hiddenAttribute)
        {
            if(hiddenAttribute != null && !hiddenAttribute.equals(""))
            {
                for(String clientStr : hiddenAttribute.split(","))
                {
                    Client client = Client.getClient(clientStr);
                    this.hiddenAttribute.add(client);
                }
            }
        }
        
        private void calculateVisibilityMask()
        {
            visibilityMask = 0;
            for(Client client : getClients())
            {
                if(clientVisibility.contains(client))
                {
                    visibilityMask |= getClientVisibilityMask(client, Visibility.Visible);
                }
                else if(hiddenAttribute.contains(client))
                {
                    visibilityMask |= getClientVisibilityMask(client, Visibility.HiddenAttribute);
                }
                else
                {
                    visibilityMask |= getClientVisibilityMask(client, Visibility.NotVisible);
                }
            }
        }

        public String getFilter()
        {
            return filter.pattern();
        }

        public Set<Client> getVisibility()
        {
            return clientVisibility;
        }

        public Set<Client> getHiddenAttribute()
        {
            return hiddenAttribute;
        }

        public int getVisibilityMask()
        {
            return visibilityMask;
        }

        boolean isHidden(String path)
        {
            return filter.matcher(path).matches();
        }
    }
}
