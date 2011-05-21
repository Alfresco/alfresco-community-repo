/*
 * Copyright (C) 2005-2010 Alfresco Software Limited.
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
package org.alfresco.repo.admin.patch.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.alfresco.repo.admin.patch.AbstractPatch;
import org.alfresco.repo.domain.patch.PatchDAO;
import org.alfresco.repo.domain.permissions.AccessControlListDAO;
import org.alfresco.repo.domain.permissions.AclDAO;
import org.alfresco.repo.security.permissions.ACLType;
import org.alfresco.repo.security.permissions.AccessControlListProperties;
import org.alfresco.repo.security.permissions.impl.AclChange;
import org.alfresco.repo.transaction.RetryingTransactionHelper;
import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.extensions.surf.util.I18NUtil;

/**
 * Fix ACLs that inherit and have issues with inheritance to correctly inherit from their primary parent, that may have
 * failed on upgrade or that have any other issue according to the DB
 */
public class FixAclInheritancePatch extends AbstractPatch
{
    private static final String MSG_SUCCESS = "patch.fixAclInheritance.result";

    private static Log logger = LogFactory.getLog(FixAclInheritancePatch.class);

    private AclDAO aclDAO;

    private PatchDAO patchDAO;

    private AccessControlListDAO accessControlListDao;

    private RetryingTransactionHelper retryingTransactionHelper;

    private long count = 0;

    /**
     * @param aclDaoComponent
     *            the aclDaoComponent to set
     */
    public void setAclDAO(AclDAO aclDAO)
    {
        this.aclDAO = aclDAO;
    }

    public void setPatchDAO(PatchDAO patchDAO)
    {
        this.patchDAO = patchDAO;
    }

    public void setAccessControlListDao(AccessControlListDAO accessControlListDao)
    {
        this.accessControlListDao = accessControlListDao;
    }

    /**
     * @param retryingTransactionHelper
     *            the retryingTransactionHelper to set
     */
    public void setRetryingTransactionHelper(RetryingTransactionHelper retryingTransactionHelper)
    {
        this.retryingTransactionHelper = retryingTransactionHelper;
    }

    @Override
    protected String applyInternal() throws Exception
    {
        // Fix unwired inheritance first as the other fixes depend on it and the fix can create D-D issues

        List<Map<String, Object>> rows = retryingTransactionHelper.doInTransaction(new RetryingTransactionCallback<List<Map<String, Object>>>()
        {

            @Override
            public List<Map<String, Object>> execute() throws Throwable
            {
                return patchDAO.getAclsThatInheritWithInheritanceUnset();
            }
        }, false, true);

        for (Map<String, Object> row : rows)
        {
            Long childAclId = (Long) row.get("childAclId");
            Long childAclType = (Long) row.get("childAclType");
            Long primaryParentAclId = (Long) row.get("primaryParentAclId");
            Long primaryParentAclType = (Long) row.get("primaryParentAclType");
            Long childNodeId = (Long) row.get("childNodeId");

            ACLType childType = ACLType.getACLTypeFromId(childAclType.intValue());
            ACLType parentType = ACLType.getACLTypeFromId(primaryParentAclType.intValue());

            RetryingTransactionCallback<Void> cb = null;

            switch (childType)
            {
            case DEFINING:
                cb = new FixInherited(primaryParentAclId, childAclId);
                retryingTransactionHelper.doInTransaction(cb, false, true);
                count++;
                break;
            case FIXED:
                break;
            case GLOBAL:
                break;
            case LAYERED:
                break;
            case OLD:
                break;
            case SHARED:
                cb = new FixSharedUnsetInheritanceCallback(childNodeId, primaryParentAclId, childAclId);
                retryingTransactionHelper.doInTransaction(cb, false, true);
                count++;
                break;
            }

        }

        // If we fixed up any D - S relationships with and create a new inherited ACL we may break some D - D
        // relationships
        // Fix these up after as they appear as broken inheritance

        rows = retryingTransactionHelper.doInTransaction(new RetryingTransactionCallback<List<Map<String, Object>>>()
        {

            @Override
            public List<Map<String, Object>> execute() throws Throwable
            {
                return patchDAO.getSharedAclsThatDoNotInheritCorrectlyFromTheirDefiningAcl();
            }
        }, false, true);

        for (Map<String, Object> row : rows)
        {
            Long inheritedAclId = (Long) row.get("inheritedAclId");
            Long inheritedAclType = (Long) row.get("inheritedAclType");
            Long aclId = (Long) row.get("aclId");
            Long aclType = (Long) row.get("aclType");

            ACLType inheritedType = ACLType.getACLTypeFromId(inheritedAclType.intValue());
            ACLType type = ACLType.getACLTypeFromId(aclType.intValue());

            FixSharedAclCallback cb = new FixSharedAclCallback(inheritedAclId, aclId);
            retryingTransactionHelper.doInTransaction(cb, false, true);
            count++;
        }

        rows = retryingTransactionHelper.doInTransaction(new RetryingTransactionCallback<List<Map<String, Object>>>()
        {

            @Override
            public List<Map<String, Object>> execute() throws Throwable
            {
                return patchDAO.getSharedAclsThatDoNotInheritCorrectlyFromThePrimaryParent();
            }
        }, false, true);

        for (Map<String, Object> row : rows)
        {
            Long childAclId = (Long) row.get("childAclId");
            Long childAclType = (Long) row.get("childAclType");
            Long primaryParentAclId = (Long) row.get("primaryParentAclId");
            Long primaryParentAclType = (Long) row.get("primaryParentAclType");
            Long childNodeId = (Long) row.get("childNodeId");

            ACLType childType = ACLType.getACLTypeFromId(childAclType.intValue());
            ACLType parentType = ACLType.getACLTypeFromId(primaryParentAclType.intValue());

            FixSharedAclCallback cb = new FixSharedAclCallback(primaryParentAclId, childAclId);
            retryingTransactionHelper.doInTransaction(cb, false, true);
            count++;
        }

        rows = retryingTransactionHelper.doInTransaction(new RetryingTransactionCallback<List<Map<String, Object>>>()
        {

            @Override
            public List<Map<String, Object>> execute() throws Throwable
            {
                return patchDAO.getDefiningAclsThatDoNotInheritCorrectlyFromThePrimaryParent();
            }
        }, false, true);

        for (Map<String, Object> row : rows)
        {
            Long childAclId = (Long) row.get("childAclId");
            Long childAclType = (Long) row.get("childAclType");
            Long primaryParentAclId = (Long) row.get("primaryParentAclId");
            Long primaryParentAclType = (Long) row.get("primaryParentAclType");
            Long childNodeId = (Long) row.get("childNodeId");

            ACLType childType = ACLType.getACLTypeFromId(childAclType.intValue());
            ACLType parentType = ACLType.getACLTypeFromId(primaryParentAclType.intValue());

            FixInherited cb = new FixInherited(primaryParentAclId, childAclId);
            retryingTransactionHelper.doInTransaction(cb, false, true);
            count++;
            break;

        }

        rows = retryingTransactionHelper.doInTransaction(new RetryingTransactionCallback<List<Map<String, Object>>>()
        {

            @Override
            public List<Map<String, Object>> execute() throws Throwable
            {
                return patchDAO.getAclsThatInheritFromNonPrimaryParent();
            }
        }, false, true);

        for (Map<String, Object> row : rows)
        {
            Long childAclId = (Long) row.get("childAclId");
            Long childAclType = (Long) row.get("childAclType");
            Long primaryParentAclId = (Long) row.get("primaryParentAclId");
            Long primaryParentAclType = (Long) row.get("primaryParentAclType");
            Long childNodeId = (Long) row.get("childNodeId");

            ACLType childType = ACLType.getACLTypeFromId(childAclType.intValue());
            ACLType parentType = ACLType.getACLTypeFromId(primaryParentAclType.intValue());

            RetryingTransactionCallback<Void> cb = null;
            switch (childType)
            {
            case DEFINING:
                cb = new FixInherited(primaryParentAclId, childAclId);
                retryingTransactionHelper.doInTransaction(cb, false, true);
                count++;
                break;
            case FIXED:
                break;
            case GLOBAL:
                break;
            case LAYERED:
                break;
            case OLD:
                break;
            case SHARED:
                cb = new SetFixedAclsCallback(childNodeId, primaryParentAclId, childAclId);
                retryingTransactionHelper.doInTransaction(cb, false, true);
                count++;
                break;
            }

        }

        // build the result message
        String msg = I18NUtil.getMessage(FixAclInheritancePatch.MSG_SUCCESS, count);
        // done
        return msg;

    }

    private class FixSharedUnsetInheritanceCallback implements RetryingTransactionCallback<Void>
    {

        Long childNodeId;

        Long primaryParentAclId;

        Long childAclId;

        FixSharedUnsetInheritanceCallback(Long childNodeId, Long primaryParentAclId, Long childAclId)
        {
            this.childNodeId = childNodeId;
            this.primaryParentAclId = primaryParentAclId;
            this.childAclId = childAclId;
        }

        /*
         * (non-Javadoc)
         * @see org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback#execute()
         */
        @Override
        public Void execute() throws Throwable
        {
            Long inheritedAclId = aclDAO.getInheritedAccessControlList(primaryParentAclId);
            if (inheritedAclId.equals(childAclId))
            {
                // child acl does match inherited from primary parent
                // needs to set inherits_from correctly and fix up
                aclDAO.fixSharedAcl(primaryParentAclId, childAclId);
            }
            else
            {
                // child acl does not match inherited from primary parent
                // need to replace the shared acl
                List<AclChange> changes = new ArrayList<AclChange>();
                accessControlListDao.setFixedAcls(childNodeId, primaryParentAclId, null, childAclId, changes, true);
            }
            return null;
        }

    }

    private class SetFixedAclsCallback implements RetryingTransactionCallback<Void>
    {
        Long childNodeId;

        Long primaryParentAclId;

        Long childAclId;

        SetFixedAclsCallback(Long childNodeId, Long primaryParentAclId, Long childAclId)
        {
            this.childNodeId = childNodeId;
            this.primaryParentAclId = primaryParentAclId;
            this.childAclId = childAclId;
        }

        /*
         * (non-Javadoc)
         * @see org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback#execute()
         */
        @Override
        public Void execute() throws Throwable
        {
            List<AclChange> changes = new ArrayList<AclChange>();
            accessControlListDao.setFixedAcls(childNodeId, primaryParentAclId, null, childAclId, changes, true);
            return null;
        }
    }

    private class FixSharedAclCallback implements RetryingTransactionCallback<Void>
    {

        Long inheritedAclId;

        Long aclId;

        FixSharedAclCallback(Long inheritedAclId, Long aclId)
        {
            this.inheritedAclId = inheritedAclId;
            this.aclId = aclId;
        }

        /*
         * (non-Javadoc)
         * @see org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback#execute()
         */
        @Override
        public Void execute() throws Throwable
        {
            aclDAO.fixSharedAcl(inheritedAclId, aclId);
            return null;
        }

    }

    private class FixInherited implements RetryingTransactionCallback<Void>
    {

        Long primaryParentAclId;

        Long childAclId;

        FixInherited(Long primaryParentAclId, Long childAclId)
        {
            this.primaryParentAclId = primaryParentAclId;
            this.childAclId = childAclId;
        }

        /*
         * (non-Javadoc)
         * @see org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback#execute()
         */
        @Override
        public Void execute() throws Throwable
        {
            aclDAO.enableInheritance(childAclId, primaryParentAclId);
            return null;
        }
    }
}
