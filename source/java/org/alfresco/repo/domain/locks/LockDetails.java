/*
 * #%L
 * Alfresco Repository
 * %%
 * Copyright (C) 2005 - 2016 Alfresco Software Limited
 * %%
 * This file is part of the Alfresco software. 
 * If the software was purchased under a paid Alfresco license, the terms of 
 * the paid license agreement will prevail.  Otherwise, the software is 
 * provided under the following open source license terms:
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
 * #L%
 */
package org.alfresco.repo.domain.locks;

import org.alfresco.service.namespace.QName;

/**
 * Class to contain details regarding a lock.  A lock is specific to a given
 * qualified name.  For any given lock, there may exist an <b>EXCLUSIVE</b>
 * lock <u>or</u> several <b>SHARED</b> locks.
 * 
 * @author Derek Hulley
 * @since 3.2
 */
public class LockDetails
{
    /**
     * The type of lock
     * 
     * @author Derek Hulley
     * @since 3.2
     */
    public enum LockType
    {
        /**
         * A lock held by a specific transaction.  No other (exclusive or shared) locks
         * may be held for the same qualified name.
         */
        EXCLUSIVE,
        /**
         * A lock that may be held by several transactions when no exclusive lock is held
         * for the same qualified name.
         */
        SHARED;
    }
    
    private final String txnId;
    private final QName lockQName;
    private final LockType lockType;

    /**
     * 
     * @param txnId             the transaction holding the lock
     * @param lockQName         the qualified name of the lock
     * @param lockType          the type of lock
     */
    public LockDetails(String txnId, QName lockQName, LockType lockType)
    {
        this.txnId = txnId;
        this.lockQName = lockQName;
        this.lockType = lockType;
    }

    /**
     * @return                  Returns the transaction holding the lock
     */
    public String getTxnId()
    {
        return txnId;
    }

    /**
     * @return                  Returns the qualified name of the lock
     */
    public QName getLockQName()
    {
        return lockQName;
    }

    /**
     * @return                  Returns the lock type
     */
    public LockType getLockType()
    {
        return lockType;
    }
}
