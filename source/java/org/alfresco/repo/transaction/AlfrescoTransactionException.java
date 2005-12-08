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
package org.alfresco.repo.transaction;

import org.springframework.transaction.TransactionException;

/**
 * Simple concrete implementation of the base class.
 * 
 * @author Derek Hulley
 */
public class AlfrescoTransactionException extends TransactionException
{
    private static final long serialVersionUID = 3643033849898962687L;

    public AlfrescoTransactionException(String msg)
    {
        super(msg);
    }

    public AlfrescoTransactionException(String msg, Throwable ex)
    {
        super(msg, ex);
    }
}
