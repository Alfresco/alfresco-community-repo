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
package org.alfresco.repo.admin.patch;

import org.alfresco.service.transaction.TransactionService;

public class SimplePatch extends AbstractPatch
{
    public static final String MSG_SUCCESS = "SimplePatch applied successfully";

    /**
     * Default constructor for Spring config
     */
    public SimplePatch()
    {
    }
    
    /**
     * Overrides the base class version to do nothing, i.e. it does not self-register
     */
    @Override
    public void init()
    {
    }
    
    /**
     * Helper constructor for some tests.  Default properties are set automatically.
     * 
     * @param transactionService TransactionService
     * @param requiresTransaction true if transaction required
     */
    /* protected */ SimplePatch(TransactionService transactionService, boolean requiresTransaction)
    {
        setTransactionService(transactionService);
        setId("SimplePatch");
        setDescription("This is a simple patch");
        setFixesFromSchema(0);
        setFixesToSchema(1000);
        setTargetSchema(1001);
        setRequiresTransaction(requiresTransaction);
    }
    
    /**
     * Does nothing
     * 
     * @return Returns a success or failure message dependent on the constructor used
     */
    @Override
    protected String applyInternal() throws Exception
    {
        return MSG_SUCCESS;
    }

}
