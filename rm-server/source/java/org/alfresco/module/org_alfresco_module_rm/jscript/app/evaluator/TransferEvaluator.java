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

import org.alfresco.module.org_alfresco_module_rm.jscript.app.BaseEvaluator;
import org.alfresco.module.org_alfresco_module_rm.model.RecordsManagementModel;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.RegexQNamePattern;

/**
 * @author Roy Wetherall
 */
public class TransferEvaluator extends BaseEvaluator
{
    private boolean transferAccessionIndicator = false;
    
    public void setTransferAccessionIndicator(boolean transferAccessionIndicator)
    {
        this.transferAccessionIndicator = transferAccessionIndicator;
    }
    
    @Override
    protected boolean evaluateImpl(NodeRef nodeRef)
    {
        List<ChildAssociationRef> parents = nodeService.getParentAssocs(nodeRef, RecordsManagementModel.ASSOC_TRANSFERRED, RegexQNamePattern.MATCH_ALL);
        if (parents.size() == 1)
        {
            boolean actual = ((Boolean)nodeService.getProperty(parents.get(0).getParentRef(), RecordsManagementModel.PROP_TRANSFER_ACCESSION_INDICATOR)).booleanValue();
            return (actual == transferAccessionIndicator);
        }
        else
        {
            return false;
        }
    }
}
