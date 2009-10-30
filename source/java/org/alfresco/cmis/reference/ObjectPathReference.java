/*
 * Copyright (C) 2005-2007 Alfresco Software Limited.
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
package org.alfresco.cmis.reference;

import org.alfresco.cmis.CMISRepositoryReference;
import org.alfresco.cmis.CMISServices;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.StoreRef;


/**
 * Path Object Reference
 * 
 * @author davidc
 */
public class ObjectPathReference extends AbstractObjectReference
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
    public ObjectPathReference(CMISServices cmisServices, CMISRepositoryReference repo, String path)
    {
        super(cmisServices, repo);
        this.path = path.startsWith("/") ? path : "/" + path;
        this.path = (!cmisServices.getDefaultRootPath().equals("/")) ? cmisServices.getDefaultRootPath() + this.path : this.path;
        String[] splitPath = this.path.split("/");
        String[] pathSegments = new String[splitPath.length -1];
        System.arraycopy(splitPath, 1, pathSegments, 0, splitPath.length -1);
        this.reference = new String[2 + pathSegments.length];
        StoreRef storeRef = repo.getStoreRef();
        reference[0] = storeRef.getProtocol();
        reference[1] = storeRef.getIdentifier();
        System.arraycopy(pathSegments, 0, reference, 2, pathSegments.length);
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
        return "ObjectPathReference[storeRef=" + repo.getStoreRef() + ",path=" + path + "]";
    }

}
