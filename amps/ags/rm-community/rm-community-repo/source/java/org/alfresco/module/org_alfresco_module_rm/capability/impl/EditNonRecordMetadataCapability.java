/*
 * #%L
 * Alfresco Records Management Module
 * %%
 * Copyright (C) 2005 - 2022 Alfresco Software Limited
 * %%
 * This file is part of the Alfresco software.
 * -
 * If the software was purchased under a paid Alfresco license, the terms of
 * the paid license agreement will prevail.  Otherwise, the software is
 * provided under the following open source license terms:
 * -
 * Alfresco is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * -
 * Alfresco is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * -
 * You should have received a copy of the GNU Lesser General Public License
 * along with Alfresco. If not, see <http://www.gnu.org/licenses/>.
 * #L%
 */

package org.alfresco.module.org_alfresco_module_rm.capability.impl;

import org.alfresco.module.org_alfresco_module_rm.capability.declarative.DeclarativeCapability;
import org.alfresco.module.org_alfresco_module_rm.record.RecordServiceImpl;
import org.alfresco.module.org_alfresco_module_rm.util.TransactionalResourceHelper;
import org.alfresco.service.cmr.repository.NodeRef;

import net.sf.acegisecurity.vote.AccessDecisionVoter;

/**
 * Edit non record metadata capability
 *
 * @author Roy Wetherall
 * @since 2.3
 */
public class EditNonRecordMetadataCapability extends DeclarativeCapability
{
    /** transaction resource helper */
    private TransactionalResourceHelper transactionalResourceHelper;
    
    /**
     * @param transactionalResourceHelper   transaction resource helper
     */
    public void setTransactionalResourceHelper(TransactionalResourceHelper transactionalResourceHelper)
    {
        this.transactionalResourceHelper = transactionalResourceHelper;
    }
    
    @Override
    public int evaluate(NodeRef nodeRef)
    {
        // check if this node is a new record
        if (transactionalResourceHelper.getSet(RecordServiceImpl.KEY_NEW_RECORDS).contains(nodeRef))
        {
            // since this is a new record created within this transaction, ignore the usual capability check
            // under the assumption that the user has CreateRecord
            // @see https://issues.alfresco.com/jira/browse/RM-1956
            return AccessDecisionVoter.ACCESS_GRANTED;
        }
        
        return super.evaluate(nodeRef);
    }

   
}
