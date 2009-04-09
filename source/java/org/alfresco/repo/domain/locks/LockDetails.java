/*
 * Copyright (C) 2005-2009 Alfresco Software Limited.
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
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.

 * As a special exception to the terms and conditions of version 2.0 of 
 * the GPL, you may redistribute this Program in connection with Free/Libre 
 * and Open Source Software ("FLOSS") applications as described in Alfresco's 
 * FLOSS exception.  You should have recieved a copy of the text describing 
 * the FLOSS exception, and it is also available here: 
 * http://www.alfresco.com/legal/licensing"
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
