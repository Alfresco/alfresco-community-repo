/*
 * Copyright (C) 2005-2007 Alfresco Software Limited.
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
package org.alfresco.repo.admin.patch;

import org.alfresco.service.transaction.TransactionService;

public class SamplePatch extends AbstractPatch
{
    public static final String MSG_SUCCESS = "SamplePatch applied successfully";
    public static final String MSG_FAILURE = "SamplePatch failed to apply";
    
    private boolean mustFail;

    /**
     * Default constructor for Spring config
     */
    public SamplePatch()
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
     * @param mustFail true if this instance must always fail to apply
     */
    /* protected */ SamplePatch(boolean mustFail, TransactionService transactionService)
    {
        this.mustFail = mustFail;
        setTransactionService(transactionService);
        setId("SamplePatch");
        setDescription("This is a sample patch");
        setFixesFromSchema(0);
        setFixesToSchema(1000);
        setTargetSchema(1001);
    }
    
    /**
     * Does nothing
     * 
     * @return Returns a success or failure message dependent on the constructor used
     */
    @Override
    protected String applyInternal() throws Exception
    {
        if (mustFail)
        {
            throw new Exception(MSG_FAILURE);
        }
        else
        {
            return MSG_SUCCESS;
        }
    }
}
