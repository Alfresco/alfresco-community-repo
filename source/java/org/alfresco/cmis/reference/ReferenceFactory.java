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

import java.util.Map;

import org.alfresco.cmis.CMISDictionaryModel;
import org.alfresco.cmis.CMISDictionaryService;
import org.alfresco.cmis.CMISObjectReference;
import org.alfresco.cmis.CMISRelationshipReference;
import org.alfresco.cmis.CMISRepositoryReference;
import org.alfresco.cmis.CMISServices;
import org.alfresco.cmis.CMISTypeDefinition;
import org.alfresco.error.AlfrescoRuntimeException;


/**
 * Reference Factory
 * 
 * @author davidc
 */
public class ReferenceFactory
{
    private CMISServices cmisService;
    private CMISDictionaryService cmisDictionary;

    /**
     * @param cmisService
     */
    public void setCMISService(CMISServices cmisService)
    {
        this.cmisService = cmisService;
    }

    /**
     * @param cmisDictionary
     */
    public void setCMISDictionaryService(CMISDictionaryService cmisDictionary)
    {
        this.cmisDictionary = cmisDictionary;
    }
    
    /**
     * Create CMIS Repository Reference from URL segments
     * 
     * @param args  url arguments
     * @param templateArgs  url template arguments
     * @return  Repository Reference  (or null, in case of bad url)
     */
    public CMISRepositoryReference createRepoReferenceFromUrl(Map<String, String> args, Map<String, String> templateArgs)
    {
        String store_type = templateArgs.get("store_type");
        String store_id = templateArgs.get("store_id");
        if (store_type != null && store_id != null)
        {
            return new StoreRepositoryReference(cmisService, store_type + ":" + store_id);
        }
        
        String store = templateArgs.get("store");
        if (store != null)
        {
            return new StoreRepositoryReference(cmisService, store);
        }

        // TODO: repository id
//      String repoId = templateArgs.get("repo");
//        else if (repoId != null)
//        {
//        }
        
        return new DefaultRepositoryReference(cmisService);
    }

    /**
     * Create CMIS Object Reference from URL segments
     * 
     * @param args  url arguments
     * @param templateArgs  url template arguments
     * @return  Repository Reference  (or null, in case of bad url)
     */
    public CMISObjectReference createObjectReferenceFromUrl(Map<String, String> args, Map<String, String> templateArgs)
    {
        String nodeRef = args.get("noderef");
        if (nodeRef != null)
        {
            return new NodeRefReference(cmisService, nodeRef);
        }
        
        CMISRepositoryReference repo = createRepoReferenceFromUrl(args, templateArgs);
        String id = templateArgs.get("id");
        if (id != null)
        {
            return new ObjectIdReference(cmisService, repo, id);
        }
        
        String path = templateArgs.get("path");
        if (path == null)
        {
            path = args.get("path");
        }
        if (path != null)
        {
            return new ObjectPathReference(cmisService, repo, path);
        }
        
        String avmPath = templateArgs.get("avmpath");
        if (avmPath != null)
        {
            return new AVMPathReference(cmisService, repo, avmPath);
        }
        
        return null;
    }

    /**
     * Create CMIS Relationship Reference from URL segments
     * 
     * @param args  url arguments
     * @param templateArgs  url template arguments
     * @return  Repository Reference  (or null, in case of bad url)
     */
    public CMISRelationshipReference createRelationshipReferenceFromUrl(Map<String, String> args, Map<String, String> templateArgs)
    {
        // retrieve relationship type definition
        CMISTypeDefinition typeDefinition = null;
        String relType = templateArgs.get("rel_type");
        if (relType != null)
        {
            try
            {
                typeDefinition = cmisDictionary.findType(relType);
            }
            catch(AlfrescoRuntimeException e) {}
        }
        if (typeDefinition == null)
        {
            return null;
        }
        if (!typeDefinition.getBaseType().getTypeId().equals(CMISDictionaryModel.RELATIONSHIP_TYPE_ID))
        {
            return null;
        }
        
        // retrieve source / target nodes
        String srcStore = templateArgs.get("store");
        String srcId = templateArgs.get("id");
        String tgtStore = templateArgs.get("target_store");
        String tgtId = templateArgs.get("target_id");

        if (srcStore != null && srcId != null && tgtStore != null && tgtId != null)
        {
            return new SourceTypeTargetRelationshipReference(cmisService, typeDefinition, srcStore, srcId, tgtStore, tgtId);
        }

        return null;
    }
    
}
