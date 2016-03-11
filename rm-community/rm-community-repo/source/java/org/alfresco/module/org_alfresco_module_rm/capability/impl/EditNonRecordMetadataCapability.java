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
