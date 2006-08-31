/*
 * Copyright (C) 2005 Alfresco, Inc.
 *
 * Licensed under the Mozilla Public License version 1.1 
 * with a permitted attribution clause. You may obtain a
 * copy of the License at
 *
 *   http://www.alfresco.org/legal/license.txt
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific
 * language governing permissions and limitations under the
 * License.
 */
package org.alfresco.repo.audit;

import javax.transaction.UserTransaction;

import org.alfresco.service.cmr.audit.AuditService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.transaction.TransactionService;
import org.alfresco.util.ApplicationContextHelper;
import org.springframework.context.ApplicationContext;

/**
 * The implementation of the AuditService for application auditing.
 * 
 * @author Andy Hind
 */
public class AuditServiceImpl implements AuditService
{
    private AuditComponent auditComponent;

    public AuditServiceImpl()
    {
        super();
    }

    public void setAuditComponent(AuditComponent auditComponent)
    {
        this.auditComponent = auditComponent;
    }

    public void audit(String source, String description)
    {
        auditComponent.audit(source, description, null, (Object[]) null);
    }

    public void audit(String source, String description, NodeRef key)
    {
        auditComponent.audit(source, description, key, (Object[]) null);
    }

    public void audit(String source, String description, Object... args)
    {
        auditComponent.audit(source, description, null, args);
    }

    public void audit(String source, String description, NodeRef key, Object... args)
    {
        auditComponent.audit(source, description, key, args);
    }

    public static void main(String[] args) throws Exception
    {
        ApplicationContext ctx = ApplicationContextHelper.getApplicationContext();
        AuditService as = (AuditService) ctx.getBean("AuditService");

        TransactionService txs = (TransactionService) ctx.getBean("transactionComponent");
        UserTransaction tx = txs.getUserTransaction();
        tx.begin();
        as.audit("AuditedApp", "First");
        as.audit("AuditedApp", "Second", new NodeRef(new StoreRef("test", "audit"), "id"));
        as.audit("AuditedApp", "Third", new Object[]{"one", "two", "three"});
        as.audit("AuditedApp", "Fourth", new NodeRef(new StoreRef("test", "audit"), "id"),  new Object[]{"one", "two", "three"});
        
        as.audit("UnAuditedApp", "First");
        as.audit("UnAuditedApp", "Second", new NodeRef(new StoreRef("test", "audit"), "id"));
        as.audit("UnAuditedApp", "Third", new Object[]{"one", "two", "three"});
        as.audit("UnAuditedApp", "Fourth", new NodeRef(new StoreRef("test", "audit"), "id"),  new Object[]{"one", "two", "three"});

        tx.commit();

    }
}
