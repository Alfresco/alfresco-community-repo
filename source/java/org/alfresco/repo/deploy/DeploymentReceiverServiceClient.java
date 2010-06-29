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

package org.alfresco.repo.deploy;

import java.io.OutputStream;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.io.Serializable;

import org.alfresco.deployment.DeploymentReceiverService;
import org.alfresco.deployment.DeploymentReceiverTransport;
import org.alfresco.deployment.DeploymentToken;
import org.alfresco.deployment.FileDescriptor;

/**
 * Client side implementation of DeploymentReceiverService which decorates a 
 * DeploymentReceiverTransport instance.
 * 
 * This class adds code to the send and finishSend methods.
 * 
 * @author britt
 */
public class DeploymentReceiverServiceClient implements
        DeploymentReceiverService
{
    /**
     * The underlying transport.
     */
    private DeploymentReceiverTransport fTransport;
    
    public DeploymentReceiverServiceClient()
    {
    }
    
    public void setDeploymentReceiverTransport(DeploymentReceiverTransport transport)
    {
        fTransport = transport;
    }
    
    /* (non-Javadoc)
     * @see org.alfresco.deployment.DeploymentReceiverService#abort(java.lang.String)
     */
    public void abort(String ticket)
    {
        fTransport.abort(ticket);
    }

    /* (non-Javadoc)
     * @see org.alfresco.deployment.DeploymentReceiverService#begin(java.lang.String, java.lang.String, java.lang.String)
     */
    public DeploymentToken begin(String target, String storeName, int version, String user, char[] password)
    {
        return fTransport.begin(target, storeName, version, user, password);
    }
    
    /* (non-Javadoc)
     * @see org.alfresco.deployment.DeploymentReceiverService#commit(java.lang.String)
     */
    public void prepare(String ticket)
    {
        fTransport.prepare(ticket);
    }

    /* (non-Javadoc)
     * @see org.alfresco.deployment.DeploymentReceiverService#commit(java.lang.String)
     */
    public void commit(String ticket)
    {
        fTransport.commit(ticket);
    }

    /* (non-Javadoc)
     * @see org.alfresco.deployment.DeploymentReceiverService#delete(java.lang.String, java.lang.String)
     */
    public void delete(String ticket, String path)
    {
        fTransport.delete(ticket, path);
    }

    /* (non-Javadoc)
     * @see org.alfresco.deployment.DeploymentReceiverService#finishSend(java.lang.String, java.io.OutputStream)
     */
    public void finishSend(String ticket, OutputStream out)
    {
        DeploymentClientOutputStream dcOut = (DeploymentClientOutputStream)out;
        fTransport.finishSend(dcOut.getTicket(), dcOut.getOutputToken());
    }

    /* (non-Javadoc)
     * @see org.alfresco.deployment.DeploymentReceiverService#getListing(java.lang.String, java.lang.String)
     */
    public List<FileDescriptor> getListing(String ticket, String path)
    {
        return fTransport.getListing(ticket, path);
    }

    /* (non-Javadoc)
     * @see org.alfresco.deployment.DeploymentReceiverService#mkdir(java.lang.String, java.lang.String, java.lang.String)
     */
    public void createDirectory(String ticket, String path, String guid, Set<String>aspects, Map<String, Serializable> properties)
    {
        fTransport.createDirectory(ticket, path, guid, aspects, properties);
    }

    /* (non-Javadoc)
     * @see org.alfresco.deployment.DeploymentReceiverService#send(java.lang.String, java.lang.String, java.lang.String)
     */
    public OutputStream send(String ticket, boolean create, String path, String guid, String encoding, String mimeType, Set<String>aspects, Map<String, Serializable> props)
    {
        String outputToken = fTransport.getSendToken(ticket, create, path, guid, encoding, mimeType, aspects, props);
        return new DeploymentClientOutputStream(fTransport, ticket, outputToken);
    }

    /* (non-Javadoc)
     * @see org.alfresco.deployment.DeploymentReceiverService#shutDown(java.lang.String, java.lang.String)
     */
    public void shutDown(String user, char[] password)
    {
        fTransport.shutDown(user, password);
    }

    public void updateDirectory(String ticket, String path, String guid, Set<String>aspects, Map<String, Serializable> props)
    {
        fTransport.updateDirectory(ticket, path, guid, aspects, props);
    }
}
