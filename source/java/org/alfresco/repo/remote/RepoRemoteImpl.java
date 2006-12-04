/**
 * 
 */
package org.alfresco.repo.remote;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.Map;

import org.alfresco.service.cmr.remote.RepoRemote;
import org.alfresco.service.cmr.remote.RepoRemoteTransport;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.Pair;

/**
 * Client side implementation of RepoRemote interface.
 * @author britt
 */
public class RepoRemoteImpl implements RepoRemote
{
    /**
     * The underlying remote transport.
     */
    private RepoRemoteTransport fTransport;

    /**
     * Default constructor.
     */
    public RepoRemoteImpl()
    {
    }
    
    /**
     * Set the transport instance.
     */
    public void setRepoRemoteTransport(RepoRemoteTransport transport)
    {
        fTransport = transport;
    }
    
    /* (non-Javadoc)
     * @see org.alfresco.service.cmr.remote.RepoRemote#createDirectory(org.alfresco.service.cmr.repository.NodeRef, java.lang.String)
     */
    public NodeRef createDirectory(NodeRef base, String path) 
    {
        return fTransport.createDirectory(ClientTicketHolder.GetTicket(), base, path);
    }

    /* (non-Javadoc)
     * @see org.alfresco.service.cmr.remote.RepoRemote#createFile(org.alfresco.service.cmr.repository.NodeRef, java.lang.String)
     */
    public OutputStream createFile(NodeRef base, String path) 
    {
        return new RepoRemoteOutputStream(fTransport.createFile(ClientTicketHolder.GetTicket(), base, path),
                                          fTransport);
    }

    /* (non-Javadoc)
     * @see org.alfresco.service.cmr.remote.RepoRemote#getListing(org.alfresco.service.cmr.repository.NodeRef)
     */
    public Map<String, Pair<NodeRef, QName>> getListing(NodeRef dir) 
    {
        return fTransport.getListing(ClientTicketHolder.GetTicket(), dir);
    }

    /* (non-Javadoc)
     * @see org.alfresco.service.cmr.remote.RepoRemote#getRoot()
     */
    public NodeRef getRoot() 
    {
        return fTransport.getRoot(ClientTicketHolder.GetTicket());
    }

    /* (non-Javadoc)
     * @see org.alfresco.service.cmr.remote.RepoRemote#lookup(org.alfresco.service.cmr.repository.NodeRef, java.lang.String)
     */
    public NodeRef lookup(NodeRef base, String path) 
    {
        return fTransport.lookup(ClientTicketHolder.GetTicket(), base, path);
    }

    /* (non-Javadoc)
     * @see org.alfresco.service.cmr.remote.RepoRemote#readFile(org.alfresco.service.cmr.repository.NodeRef)
     */
    public InputStream readFile(NodeRef fileRef) 
    {
        return new RepoRemoteInputStream(fTransport.readFile(ClientTicketHolder.GetTicket(), fileRef),
                                         fTransport);
    }

    /* (non-Javadoc)
     * @see org.alfresco.service.cmr.remote.RepoRemote#readFile(org.alfresco.service.cmr.repository.NodeRef, java.lang.String)
     */
    public InputStream readFile(NodeRef base, String path) 
    {
        return new RepoRemoteInputStream(fTransport.readFile(ClientTicketHolder.GetTicket(), base, path), 
                                         fTransport);
    }

    /* (non-Javadoc)
     * @see org.alfresco.service.cmr.remote.RepoRemote#removeNode(org.alfresco.service.cmr.repository.NodeRef)
     */
    public void removeNode(NodeRef toRemove) 
    {
        fTransport.removeNode(ClientTicketHolder.GetTicket(), toRemove);
    }

    /* (non-Javadoc)
     * @see org.alfresco.service.cmr.remote.RepoRemote#removeNode(org.alfresco.service.cmr.repository.NodeRef, java.lang.String)
     */
    public void removeNode(NodeRef base, String path) 
    {
        fTransport.removeNode(ClientTicketHolder.GetTicket(), base, path);
    }

    /* (non-Javadoc)
     * @see org.alfresco.service.cmr.remote.RepoRemote#rename(org.alfresco.service.cmr.repository.NodeRef, java.lang.String, java.lang.String)
     */
    public void rename(NodeRef base, String src, String dst) 
    {
        fTransport.rename(ClientTicketHolder.GetTicket(), base, src, dst);
    }

    /* (non-Javadoc)
     * @see org.alfresco.service.cmr.remote.RepoRemote#writeFile(org.alfresco.service.cmr.repository.NodeRef, java.lang.String)
     */
    public OutputStream writeFile(NodeRef base, String path) 
    {
        return new RepoRemoteOutputStream(fTransport.writeFile(ClientTicketHolder.GetTicket(), base, path),
                                          fTransport);
    }
}
