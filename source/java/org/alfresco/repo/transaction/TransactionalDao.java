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
package org.alfresco.repo.transaction;


/**
 * Contract for a DAO to interact with a transaction.
 * 
 * @author davidc
 */
public interface TransactionalDao
{
    /**
     * Allows the dao to flush any consuming resources.  This mechanism is
     * used primarily during long-lived transactions to ensure that system resources
     * are not used up.
     * <p>
     * This method must not be used for implementing business logic.
     */
    void flush();
    
    /**
     * Are there any pending changes which must be synchronized with the store?
     * 
     * @return true => changes are pending
     */
    public boolean isDirty();
    
    /**
     * This callback provides a chance for the DAO to do any pre-commit work.
     * 
     * @since 1.4.5
     */
    public void beforeCommit();
}
