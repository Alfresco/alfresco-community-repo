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
package org.alfresco.repo.cmis.reference;

import org.alfresco.cmis.CMISRepositoryReference;
import org.alfresco.cmis.CMISServices;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.StoreRef;


/**
 * Node Path Object Reference
 * 
 * Note: This path is from the root of the store
 * 
 * @author davidc
 */
public class NodePathReference extends AbstractObjectReference
{
    protected String path;
    protected String[] reference;

    /**
     * Construct
     * 
     * @param cmisServices
     * @param repo
     * @param path
     */
    public NodePathReference(CMISServices cmisServices, CMISRepositoryReference repo, String path)
    {
        super(cmisServices, repo);
        this.path = path;
        String[] splitPath = this.path.split("/");
        this.reference = new String[2 + splitPath.length];
        StoreRef storeRef = repo.getStoreRef();
        reference[0] = storeRef.getProtocol();
        reference[1] = storeRef.getIdentifier();
        System.arraycopy(splitPath, 0, reference, 2, splitPath.length);
    }

    /*
     * (non-Javadoc)
     * @see org.alfresco.cmis.CMISObjectReference#getNodeRef()
     */
    public NodeRef getNodeRef()
    {
        return cmisServices.getNode("path", reference);
    }
    
    /**
     * @return  path
     */
    public String getPath()
    {
        return path;
    }
    
    @Override
    public String toString()
    {
        return "NodePathReference[storeRef=" + repo.getStoreRef() + ",path=" + path + "]";
    }

}
