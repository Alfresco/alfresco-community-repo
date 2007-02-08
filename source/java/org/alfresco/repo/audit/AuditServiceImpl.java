/*
 * Copyright (C) 2005 Alfresco, Inc.
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
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package org.alfresco.repo.audit;

import java.util.List;

import javax.transaction.UserTransaction;

import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.service.cmr.audit.AuditInfo;
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

    public List<AuditInfo> getAuditTrail(NodeRef nodeRef)
    {
        return  auditComponent.getAuditTrail(nodeRef);
    }
    
    public static void main(String[] args) throws Exception
    {

        ApplicationContext ctx = ApplicationContextHelper.getApplicationContext();
        AuditService as = (AuditService) ctx.getBean("AuditService");

        TransactionService txs = (TransactionService) ctx.getBean("transactionComponent");
        UserTransaction tx = txs.getUserTransaction();
        tx.begin();

        AuthenticationUtil.setSystemUserAsCurrentUser();
        try
        {

            NodeRef nodeRef = new NodeRef(new StoreRef("test", "audit"), "id");
            as.audit("AuditedApp", "First");
            System.out.println("Audit entries for node "+as.getAuditTrail(nodeRef).size());
            as.audit("AuditedApp", "Second", nodeRef);
            System.out.println("Audit entries for node "+as.getAuditTrail(nodeRef).size());
            as.audit("AuditedApp", "Third", new Object[] { "one", "two", "three" });
            System.out.println("Audit entries for node "+as.getAuditTrail(nodeRef).size());
            as.audit("AuditedApp", "Fourth",nodeRef, new Object[] { "one",
                    "two", "three" });
            System.out.println("Audit entries for node "+as.getAuditTrail(nodeRef).size());
            as.audit("UnAuditedApp", "First");
            System.out.println("Audit entries for node "+as.getAuditTrail(nodeRef).size());
            as.audit("UnAuditedApp", "Second", nodeRef);
            System.out.println("Audit entries for node "+as.getAuditTrail(nodeRef).size());
            as.audit("UnAuditedApp", "Third", new Object[] { "one", "two", "three" });
            System.out.println("Audit entries for node "+as.getAuditTrail(nodeRef).size());
            as.audit("UnAuditedApp", "Fourth", nodeRef, new Object[] { "one",
                    "two", "three" });
            System.out.println("Audit entries for node "+as.getAuditTrail(nodeRef).size());
        }
        finally
        {
            AuthenticationUtil.clearCurrentSecurityContext();
        }
        tx.commit();

    }
}
