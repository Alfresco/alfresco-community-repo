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

import org.alfresco.module.org_alfresco_module_rm.jscript.app.BaseEvaluator;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;

/**
 * Transfered indicator
 * 
 * @author Roy Wetherall
 */
public class HasAspectEvaluator extends BaseEvaluator
{       
    private String prefixAspectQNameString;
    
    public void setAspect(String aspect)
    {
        prefixAspectQNameString = aspect;
    }
    
    @Override
    protected boolean evaluateImpl(NodeRef nodeRef)
    {
        QName aspect = QName.createQName(prefixAspectQNameString, namespaceService);
        return nodeService.hasAspect(nodeRef, aspect);        
    }
}
