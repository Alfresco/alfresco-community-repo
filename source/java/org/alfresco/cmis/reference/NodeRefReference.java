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

import java.util.regex.Pattern;

import org.alfresco.cmis.CMISObjectReference;
import org.alfresco.cmis.CMISRepositoryReference;
import org.alfresco.cmis.CMISServices;
import org.alfresco.service.cmr.repository.NodeRef;

/**
 * NodeRef Object Reference
 * 
 * NOTE: Also takes into account versioned NodeRefs - protocol://store/id;version
 * 
 * @author davidc
 */
public class NodeRefReference implements CMISObjectReference
{
    // versioned objectId pattern
    // TODO: encapsulate elsewhere
    private static final Pattern versionedNodeRefPattern = Pattern.compile(".+://.+/.+;.+");    
    private ObjectIdReference reference;

    /**
     * Construct
     * 
     * @param cmisServices
     * @param nodeRef
     * @param version
     */
    public NodeRefReference(CMISServices cmisServices, NodeRef nodeRef, String version)
    {
        CMISRepositoryReference repo = new StoreRepositoryReference(cmisServices, nodeRef.getStoreRef());
        reference = new ObjectIdReference(cmisServices, repo, nodeRef.getId() + ";" + version);
    }

    /**
     * Construct
     * 
     * @param cmisServices
     * @param nodeRefStr
     */
    public NodeRefReference(CMISServices cmisServices, String nodeRefStr)
    {
        NodeRef nodeRef;
        String version = null;
        if (versionedNodeRefPattern.matcher(nodeRefStr).matches())
        {
            int vIdx = nodeRefStr.lastIndexOf(";");
            nodeRef = new NodeRef(nodeRefStr.substring(0, vIdx));
            version = nodeRefStr.substring(vIdx +1);
        }
        else
        {
            nodeRef = new NodeRef(nodeRefStr);
        }
        
        CMISRepositoryReference repo = new StoreRepositoryReference(cmisServices, nodeRef.getStoreRef());
        reference = new ObjectIdReference(cmisServices, repo, nodeRef.getId() + ";" + version);
    }

    /*
     * (non-Javadoc)
     * @see org.alfresco.cmis.CMISObjectReference#getRepositoryReference()
     */
    public CMISRepositoryReference getRepositoryReference()
    {
        return reference.getRepositoryReference();
    }

    /*
     * (non-Javadoc)
     * @see org.alfresco.cmis.CMISObjectReference#getNodeRef()
     */
    public NodeRef getNodeRef()
    {
        return reference.getNodeRef();
    }
    
    @Override
    public String toString()
    {
        return reference.toString();
    }

}
