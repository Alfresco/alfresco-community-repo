/*
 * Copyright (C) 2005-2014 Alfresco Software Limited.
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

package org.alfresco.module.org_alfresco_module_rm.test.integration.relationship;

import java.util.Set;
import org.alfresco.module.org_alfresco_module_rm.relationship.Relationship;
import org.alfresco.module.org_alfresco_module_rm.test.util.BaseRMTestCase;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.util.GUID;

/**
 * Delete relationship test.
 * 
 * @author Ana Bozianu
 * @since 2.3
 */
public class DeleteRelationshipTest extends BaseRMTestCase
{    
	public void testDeleteRelationship() throws Exception
    {
    	doBehaviourDrivenTest(new BehaviourDrivenTest()
        {
            /** test data */
        	NodeRef sourceNode;
        	NodeRef targetNode;
        	String associationName = "obsoletes";
                
            public void given()
            {
            	
            	// create the source record
            	sourceNode = utils.createRecord(rmFolder, GUID.generate());
             
                //create the target record
            	targetNode = utils.createRecord(rmFolder, GUID.generate());
                
                //create relationship
                relationshipService.addRelationship(associationName, sourceNode, targetNode);
            }
            
            public void when()
            {
                //delete relationship  
            	relationshipService.removeRelationship(associationName, sourceNode, targetNode);
            }
            
            public void then()
            {
               //check if relationship is deleted
            	Set<Relationship> relationships = relationshipService.getRelationshipsFrom(sourceNode);
            	for(Relationship r : relationships)
            	{
            		assertFalse(r.getTarget().equals(targetNode) && r.getUniqueName().equals(associationName));
            	}
            }
        });           
    }
    
  
}
