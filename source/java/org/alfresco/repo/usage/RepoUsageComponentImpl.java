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
package org.alfresco.repo.usage;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock.ReadLock;
import java.util.concurrent.locks.ReentrantReadWriteLock.WriteLock;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.ibatis.IdsEntity;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.domain.qname.QNameDAO;
import org.alfresco.repo.domain.query.CannedQueryDAO;
import org.alfresco.repo.lock.JobLockService;
import org.alfresco.repo.lock.LockAcquisitionException;
import org.alfresco.repo.transaction.AlfrescoTransactionSupport;
import org.alfresco.repo.transaction.AlfrescoTransactionSupport.TxnReadState;
import org.alfresco.service.cmr.admin.RepoUsage;
import org.alfresco.service.cmr.admin.RepoUsage.LicenseMode;
import org.alfresco.service.cmr.admin.RepoUsage.UsageType;
import org.alfresco.service.cmr.admin.RepoUsageStatus;
import org.alfresco.service.cmr.admin.RepoUsageStatus.RepoUsageLevel;
import org.alfresco.service.cmr.attributes.AttributeService;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.security.AuthorityService;
import org.alfresco.service.namespace.QName;
import org.alfresco.service.transaction.TransactionService;
import org.alfresco.util.PropertyCheck;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.extensions.surf.util.I18NUtil;

/**
 * Low-level implementation to answer repository usage queries
 * 
 * @author Derek Hulley
 * @since 3.5
 */
public class RepoUsageComponentImpl implements RepoUsageComponent
{
    private static final String QUERY_NS = "alfresco.query.usages";
    private static final String QUERY_SELECT_COUNT_PERSONS_NOT_DISABLED = "select_CountPersonsNotDisabled";
    private static final String QUERY_SELECT_COUNT_DOCUMENTS = "select_CountDocuments";
    
    private static Log logger = LogFactory.getLog(RepoUsageComponentImpl.class);
    
    private TransactionService transactionService;
    private AuthorityService authorityService;
    private AttributeService attributeService;
    private DictionaryService dictionaryService;
    private JobLockService jobLockService;
    private CannedQueryDAO cannedQueryDAO;
    private QNameDAO qnameDAO;
    
    private RepoUsage restrictions;
    private ReadLock restrictionsReadLock;
    private WriteLock restrictionsWriteLock;
    private Set<RestrictionObserver> restrictionObservers = new HashSet<RestrictionObserver>();
    
    /**
     * Defaults
     */
    public RepoUsageComponentImpl()
    {
        this.restrictions = new RepoUsage(null, null, null, LicenseMode.UNKNOWN, null, false);
        
        ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
        restrictionsReadLock = lock.readLock();
        restrictionsWriteLock = lock.writeLock();
    }

    /**
     * @param transactionService        service that tells if the server is read-only or not
     */
    public void setTransactionService(TransactionService transactionService)
    {
        this.transactionService = transactionService;
    }

    /**
     * @param authorityService          service to check for admin rights
     */
    public void setAuthorityService(AuthorityService authorityService)
    {
        this.authorityService = authorityService;
    }

    /**
     * @param attributeService          service used to store usage attributes
     */
    public void setAttributeService(AttributeService attributeService)
    {
        this.attributeService = attributeService;
    }

    /**
     * @param dictionaryService         component to resolve types and subtypes
     */
    public void setDictionaryService(DictionaryService dictionaryService)
    {
        this.dictionaryService = dictionaryService;
    }

    /**
     * @param jobLockService            service to prevent duplicate work when updating usages
     */
    public void setJobLockService(JobLockService jobLockService)
    {
        this.jobLockService = jobLockService;
    }

    /**
     * @param cannedQueryDAO            DAO for executing queries
     */
    public void setCannedQueryDAO(CannedQueryDAO cannedQueryDAO)
    {
        this.cannedQueryDAO = cannedQueryDAO;
    }

    /**
     * @param qnameDAO                  DAO for getting IDs of QNames
     */
    public void setQnameDAO(QNameDAO qnameDAO)
    {
        this.qnameDAO = qnameDAO;
    }

    @Override
    public void observeRestrictions(RestrictionObserver observer)
    {
        restrictionObservers.add(observer);
    }
    
    /**
     * Check that all properties are properly set
     */
    public void init()
    {
        PropertyCheck.mandatory(this, "transactionService", transactionService);
        PropertyCheck.mandatory(this, "authorityService", authorityService);
        PropertyCheck.mandatory(this, "attributeService", attributeService);
        PropertyCheck.mandatory(this, "dictionaryService", dictionaryService);
        PropertyCheck.mandatory(this, "jobLockService", jobLockService);
        PropertyCheck.mandatory(this, "cannedQueryDAO", cannedQueryDAO);
        PropertyCheck.mandatory(this, "qnameDAO", qnameDAO);
    }
    
    /**
     * Checks that the 'System' user is active in a read-write txn.
     */
    private final void checkTxnState(TxnReadState txnStateNeeded)
    {
        switch (txnStateNeeded)
        {
            case TXN_READ_WRITE:
                if (AlfrescoTransactionSupport.getTransactionReadState() != TxnReadState.TXN_READ_WRITE)
                {
                    throw AlfrescoRuntimeException.create("system.usage.err.no_txn_readwrite");
                }
                break;
            case TXN_READ_ONLY:
                if (AlfrescoTransactionSupport.getTransactionReadState() == TxnReadState.TXN_NONE)
                {
                    throw AlfrescoRuntimeException.create("system.usage.err.no_txn");
                }
                break;
        }
    }

    @Override
    public void setRestrictions(RepoUsage restrictions)
    {
        checkTxnState(TxnReadState.TXN_NONE);
        restrictionsWriteLock.lock();
        try
        {
            this.restrictions = restrictions;
        }
        finally
        {
            restrictionsWriteLock.unlock();
        }
        
        // Fire observers
        for(RestrictionObserver observer : restrictionObservers )
        {
            observer.onChangeRestriction(restrictions);
        }
    }

    @Override
    public RepoUsage getRestrictions()
    {
        // No need to check txn state and any user can get this info.
        restrictionsReadLock.lock();
        try
        {
            return restrictions;
        }
        finally
        {
            restrictionsReadLock.unlock();
        }
    }

    @Override
    public boolean updateUsage(UsageType usageType)
    {
        checkTxnState(TxnReadState.TXN_READ_WRITE);
        
        boolean updateUsers = false;
        boolean updateDocuments = false;
        switch (usageType)
        {
            case USAGE_DOCUMENTS:
                updateDocuments = true;
                break;
            case USAGE_USERS:
                updateUsers = true;
                break;
            case USAGE_ALL:
                updateUsers = true;
                updateDocuments = true;
        }
        
        if (updateUsers && !updateUsers())
        {
            return false;
        }
        if (updateDocuments && !updateDocuments())
        {
            return false;
        }
        
        // Done
        if (logger.isDebugEnabled())
        {
            RepoUsage usage = getUsageImpl();
            logger.debug("Updated repo usage: " + usage);
        }
        // The update succeeded and the locks held
        return true;
    }
    
    /**
     * Update number of users with appropriate locking
     */
    private boolean updateUsers()
    {
        String lockToken = null;
        try
        {
            // Lock to prevent concurrent queries
            lockToken = jobLockService.getLock(LOCK_USAGE_USERS, LOCK_TTL);
            // Count users
            IdsEntity idsParam = new IdsEntity();
            idsParam.setIdOne(qnameDAO.getOrCreateQName(ContentModel.ASPECT_PERSON_DISABLED).getFirst());
            idsParam.setIdTwo(qnameDAO.getOrCreateQName(ContentModel.TYPE_PERSON).getFirst());
            Long userCount = cannedQueryDAO.executeCountQuery(QUERY_NS, QUERY_SELECT_COUNT_PERSONS_NOT_DISABLED, idsParam);
            
            // We subtract one to cater for 'guest', which is implicit
            userCount = userCount > 0L ? userCount - 1L : 0L;

            // Lock again to be sure we still have the right to update
            jobLockService.refreshLock(lockToken, LOCK_USAGE_USERS, LOCK_TTL);
            attributeService.setAttribute(
                    new Long(System.currentTimeMillis()),
                    KEY_USAGE_ROOT, KEY_USAGE_CURRENT, KEY_USAGE_LAST_UPDATE_USERS);
            attributeService.setAttribute(
                    userCount,
                    KEY_USAGE_ROOT, KEY_USAGE_CURRENT, KEY_USAGE_USERS);
            // Success
            return true;
        }
        catch (LockAcquisitionException e)
        {
            logger.debug("Failed to get lock for user counts: " + e.getMessage());
            return false;
        }
        finally
        {
            if (lockToken != null)
            {
                jobLockService.releaseLock(lockToken, LOCK_USAGE_USERS);
            }
        }
    }
    
    /**
     * Update number of documents with appropriate locking
     */
    private boolean updateDocuments()
    {
        String lockToken = null;
        try
        {
            // Lock to prevent concurrent queries
            lockToken = jobLockService.getLock(LOCK_USAGE_DOCUMENTS, LOCK_TTL);
            // Count documents
            Set<QName> searchTypeQNames = new HashSet<QName>(11);
            Collection<QName> qnames = dictionaryService.getSubTypes(ContentModel.TYPE_CONTENT, true);
            searchTypeQNames.addAll(qnames);
            searchTypeQNames.add(ContentModel.TYPE_CONTENT);
            qnames = dictionaryService.getSubTypes(ContentModel.TYPE_LINK, true);
            searchTypeQNames.addAll(qnames);
            searchTypeQNames.add(ContentModel.TYPE_LINK);
            Set<Long> searchTypeQNameIds = qnameDAO.convertQNamesToIds(searchTypeQNames, false);
            IdsEntity idsParam = new IdsEntity();
            idsParam.setIds(new ArrayList<Long>(searchTypeQNameIds));
            Long documentCount = cannedQueryDAO.executeCountQuery(QUERY_NS, QUERY_SELECT_COUNT_DOCUMENTS, idsParam);
            
            // Lock again to be sure we still have the right to update
            jobLockService.refreshLock(lockToken, LOCK_USAGE_DOCUMENTS, LOCK_TTL);
            attributeService.setAttribute(
                    new Long(System.currentTimeMillis()),
                    KEY_USAGE_ROOT, KEY_USAGE_CURRENT, KEY_USAGE_LAST_UPDATE_DOCUMENTS);
            attributeService.setAttribute(
                    documentCount,
                    KEY_USAGE_ROOT, KEY_USAGE_CURRENT, KEY_USAGE_DOCUMENTS);
            // Success
            return true;
        }
        catch (LockAcquisitionException e)
        {
            logger.debug("Failed to get lock for document counts: " + e.getMessage());
            return false;
        }
        finally
        {
            if (lockToken != null)
            {
                jobLockService.releaseLock(lockToken, LOCK_USAGE_DOCUMENTS);
            }
        }
    }

    /**
     * Build the usage component.  Protect with a read lock, transaction check and authentication check.
     */
    private RepoUsage getUsageImpl()
    {
        // Fetch persisted usage data
        Long lastUpdateUsers = (Long) attributeService.getAttribute(
                KEY_USAGE_ROOT, KEY_USAGE_CURRENT, KEY_USAGE_LAST_UPDATE_USERS);
        Long users = (Long) attributeService.getAttribute(
                KEY_USAGE_ROOT, KEY_USAGE_CURRENT, KEY_USAGE_USERS);
        Long lastUpdateDocuments = (Long) attributeService.getAttribute(
                KEY_USAGE_ROOT, KEY_USAGE_CURRENT, KEY_USAGE_LAST_UPDATE_DOCUMENTS);
        Long documents = (Long) attributeService.getAttribute(
                KEY_USAGE_ROOT, KEY_USAGE_CURRENT, KEY_USAGE_DOCUMENTS);

        final Long lastUpdate;
        if (lastUpdateUsers == null)
        {
            lastUpdate = lastUpdateDocuments;
        }
        else if (lastUpdateDocuments == null)
        {
            lastUpdate = lastUpdateUsers;
        }
        else if (lastUpdateDocuments.compareTo(lastUpdateUsers) > 0)
        {
            lastUpdate = lastUpdateDocuments;
        }
        else
        {
            lastUpdate = lastUpdateUsers;
        }
        
        // Combine with current restrictions
        RepoUsage usage = new RepoUsage(
                lastUpdate,
                users,
                documents,
                restrictions.getLicenseMode(),
                restrictions.getLicenseExpiryDate(),
                transactionService.getAllowWrite() == false);
        return usage;
    }

    @Override
    public RepoUsage getUsage()
    {
        checkTxnState(TxnReadState.TXN_READ_ONLY);
        restrictionsReadLock.lock();
        try
        {
            // Combine with current restrictions
            RepoUsage usage = getUsageImpl();
            // Done
            if (logger.isDebugEnabled())
            {
                logger.debug("Retrieved repo usage: " + usage);
            }
            return usage;
        }
        finally
        {
            restrictionsReadLock.unlock();
        }
    }
    
    /**
     * Calculate and retrieve full status alerts based on the usage and license expiry state.
     * 
     * @return              Returns the usage status bean
     */
    public RepoUsageStatus getUsageStatus()
    {
        RepoUsage usage = getUsage();
        RepoUsage restrictions = getRestrictions();
        
        RepoUsageLevel level = RepoUsageLevel.OK;
        List<String> warnings = new ArrayList<String>(1);
        List<String> errors = new ArrayList<String>(1);
        
        // Check users
        long usersCurrent = usage.getUsers() == null ? 0L : usage.getUsers();
        long usersMax = restrictions.getUsers() == null ? Long.MAX_VALUE : restrictions.getUsers();
        if (usersCurrent > usersMax)
        {
            errors.add(I18NUtil.getMessage("system.usage.err.limit_users_exceeded", usersMax, usersCurrent));
            level = RepoUsageLevel.LOCKED_DOWN;
        }
        else if (usersCurrent == usersMax)
        {
            warnings.add(I18NUtil.getMessage("system.usage.warn.limit_users_reached", usersMax, usersCurrent));
            level = RepoUsageLevel.WARN_ALL;
        }
        else if (usersCurrent >= (0.9 * usersMax) || usersCurrent >= (usersMax - 1))
        {
            warnings.add(I18NUtil.getMessage("system.usage.warn.limit_users_approached", usersMax, usersCurrent));
            level = RepoUsageLevel.WARN_ADMIN;
        }
        
        // Check documents
        long documentsCurrent = usage.getDocuments() == null ? 0L : usage.getDocuments();
        long documentsMax = restrictions.getDocuments() == null ? Long.MAX_VALUE : restrictions.getDocuments();
        if (documentsCurrent > documentsMax)
        {
            errors.add(I18NUtil.getMessage("system.usage.err.limit_documents_exceeded", documentsMax, documentsCurrent));
            level = RepoUsageLevel.LOCKED_DOWN;
        }
        else if (documentsCurrent > 0.99 * documentsMax)
        {
            warnings.add(I18NUtil.getMessage("system.usage.warn.limit_documents_reached", documentsMax, documentsCurrent));
            if (level.ordinal() < RepoUsageLevel.WARN_ALL.ordinal())
            {
                level = RepoUsageLevel.WARN_ALL;
            }
        }
        else if (documentsCurrent > 0.9 * documentsMax)
        {
            warnings.add(I18NUtil.getMessage("system.usage.warn.limit_documents_approached", documentsMax, documentsCurrent));
            if (level.ordinal() < RepoUsageLevel.WARN_ADMIN.ordinal())
            {
                level = RepoUsageLevel.WARN_ADMIN;
            }
        }
        
        // Check the license expiry
        Long licenseExpiryDate = restrictions.getLicenseExpiryDate();
        if (licenseExpiryDate != null)
        {
            long remainingMs = licenseExpiryDate - System.currentTimeMillis();
            double remainingDays = (double) remainingMs / (double)(24*3600000);
            if (remainingDays <= 0.0)
            {
                errors.add(I18NUtil.getMessage("system.usage.err.limit_license_expired"));
                level = RepoUsageLevel.LOCKED_DOWN;
            }
            else if (remainingDays <= 7.0)
            {
                warnings.add(I18NUtil.getMessage("system.usage.err.limit_license_expiring", (int)remainingDays));
                if (level.ordinal() < RepoUsageLevel.WARN_ADMIN.ordinal())
                {
                    level = RepoUsageLevel.WARN_ADMIN;
                }
            }
            else if (remainingDays <= 30.0)
            {
                warnings.add(I18NUtil.getMessage("system.usage.err.limit_license_expiring", (int)remainingDays));
                if (level.ordinal() < RepoUsageLevel.WARN_ALL.ordinal())
                {
                    level = RepoUsageLevel.WARN_ALL;
                }
            }
        }
        
        RepoUsageStatus status = new RepoUsageStatus(restrictions, usage, level, warnings, errors);
        // Done
        if (logger.isDebugEnabled())
        {
            logger.debug("Usage status generated: " + status);
        }
        return status;
    }
}
