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
package org.alfresco.web.ui.common;

import javax.transaction.UserTransaction;

import org.alfresco.repo.transaction.RetryingTransactionHelper;

/**
 * Unchecked exception wrapping an already-reported exception.  The dialog code can use this to
 * detect whether or not to report further to the user.
 * 
 * @author Derek Hulley
 * @since 3.1
 */
public class ReportedException extends RuntimeException
{
    private static final long serialVersionUID = -4179045854462002741L;

    public ReportedException(Throwable e)
    {
        super(e);
    }
    
    /**
     * Throws the given exception if we are still in an active transaction,
     * this ensures that we cross the transaction boundary and thus cause
     * the transaction to rollback.
     * 
     * @param error The error to be thrown
    * @throws Throwable 
     */
    public static void throwIfNecessary(Throwable error)
    {
       if (error != null)
       {
          UserTransaction txn = RetryingTransactionHelper.getActiveUserTransaction();
          if (txn != null)
          {
             throw new ReportedException(error);
          }
       }
    }
}
