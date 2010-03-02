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
