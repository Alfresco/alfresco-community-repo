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
package org.alfresco.module.org_alfresco_module_rm.model.rma.aspect;

import java.util.Set;

import org.alfresco.module.org_alfresco_module_rm.model.BaseBehaviourBean;
import org.alfresco.module.org_alfresco_module_rm.relationship.Relationship;
import org.alfresco.module.org_alfresco_module_rm.relationship.RelationshipService;
import org.alfresco.module.org_alfresco_module_rm.version.RecordableVersionService;
import org.alfresco.repo.node.NodeServicePolicies;
import org.alfresco.repo.policy.annotation.Behaviour;
import org.alfresco.repo.policy.annotation.BehaviourBean;
import org.alfresco.repo.policy.annotation.BehaviourKind;
import org.alfresco.repo.security.authentication.AuthenticationUtil.RunAsWork;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.version.Version;

/**
 * rmv:versionRecord behaviour bean
 *
 * @author Roy Wetherall
 * @since 2.3.1
 */
@BehaviourBean
(
   defaultType = "rmv:versionRecord"
)
public class VersionRecordAspect extends    BaseBehaviourBean
                                 implements NodeServicePolicies.BeforeDeleteNodePolicy
{
    /** recordable version service */
    private RecordableVersionService recordableVersionService;
    
    /** relationship service */
    private RelationshipService relationshipService;
    
    /**
     * @param recordableVersionService  recordable version service
     */
    public void setRecordableVersionService(RecordableVersionService recordableVersionService)
    {
        this.recordableVersionService = recordableVersionService;
    }    
    
    /**
     * @param relationshipService relationship service
     */
    public void setRelationshipService(RelationshipService relationshipService)
    {
        this.relationshipService = relationshipService;
    }
    
    /**
     * If the record is a version record then delete the associated version entry
     * 
     * @see org.alfresco.repo.node.NodeServicePolicies.BeforeDeleteNodePolicy#beforeDeleteNode(org.alfresco.service.cmr.repository.NodeRef)
     */
    @Override
    @Behaviour (kind = BehaviourKind.CLASS)
    public void beforeDeleteNode(final NodeRef nodeRef)
    {
        final Version version = recordableVersionService.getRecordedVersion(nodeRef);
        if (version != null)
        {
            authenticationUtil.runAsSystem(new RunAsWork<Void>()
            {
                @Override
                public Void doWork()
                {
                    behaviourFilter.disableBehaviour();
                    try
                    {
                        // mark the associated version as destroyed
                        recordableVersionService.destroyRecordedVersion(version);
                                                            
                        // re-organise the versions relationships ...
                        // if there is only one "to" reference since a version can only have one predecessor
                        Set<Relationship> tos = relationshipService.getRelationshipsTo(nodeRef, RelationshipService.RELATIONSHIP_VERSIONS);
                        if (!tos.isEmpty() && tos.size() == 1)
                        {
                            // if there is some "from" references
                            Set<Relationship> froms = relationshipService.getRelationshipsFrom(nodeRef, RelationshipService.RELATIONSHIP_VERSIONS);
                            if (!froms.isEmpty())
                            {
                                // get predecessor version relationship
                                Relationship to = tos.iterator().next();
                                
                                for (Relationship from : froms)
                                {
                                    // point the "to" the all the "from's"
                                    relationshipService.addRelationship(RelationshipService.RELATIONSHIP_VERSIONS, to.getSource(), from.getTarget());
                                }
                            }
                        }
                    }
                    finally
                    {
                        behaviourFilter.enableBehaviour();
                    }

                    return null;
                }
            });
        }         
    }
}
