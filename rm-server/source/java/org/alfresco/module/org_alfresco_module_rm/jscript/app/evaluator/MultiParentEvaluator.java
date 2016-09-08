/*
 * Copyright (C) 2005-2011 Alfresco Software Limited.
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
package org.alfresco.module.org_alfresco_module_rm.jscript.app.evaluator;

import java.util.List;

import org.alfresco.model.ContentModel;
import org.alfresco.module.org_alfresco_module_rm.jscript.app.BaseEvaluator;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.security.authentication.AuthenticationUtil.RunAsWork;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.RegexQNamePattern;

/**
 * Determines whether a node has multiple parents within a file plan
 * 
 * @author Roy Wetherall
 */
public class MultiParentEvaluator extends BaseEvaluator
{
    @Override
    protected boolean evaluateImpl(final NodeRef nodeRef)
    {           
        return AuthenticationUtil.runAsSystem(new RunAsWork<Boolean>()
        {
            @Override
            public Boolean doWork() throws Exception
            {
                List<ChildAssociationRef> parents = nodeService.getParentAssocs(nodeRef, ContentModel.ASSOC_CONTAINS, RegexQNamePattern.MATCH_ALL);
                int count = 0;
                for (ChildAssociationRef parent : parents)
                {
                    if (nodeService.hasAspect(parent.getParentRef(), ASPECT_FILE_PLAN_COMPONENT) == true)
                    {
                        count++;
                    }
                }
                
                return (count > 1);                
            }
        });
    }
}
