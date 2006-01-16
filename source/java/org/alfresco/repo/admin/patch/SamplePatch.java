/*
 * Copyright (C) 2005 Alfresco, Inc.
 *
 * Licensed under the Mozilla Public License version 1.1 
 * with a permitted attribution clause. You may obtain a
 * copy of the License at
 *
 *   http://www.alfresco.org/legal/license.txt
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific
 * language governing permissions and limitations under the
 * License.
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
        setApplyAfterVersion("1.0.0");
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
