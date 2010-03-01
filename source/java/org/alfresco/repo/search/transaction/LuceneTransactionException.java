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
package org.alfresco.repo.search.transaction;

import org.springframework.transaction.TransactionException;

/**
 * @author Andy Hind
 */
public class LuceneTransactionException extends TransactionException
{
    private static final long serialVersionUID = 3978985453464335925L;

    public LuceneTransactionException(String arg0)
    {
        super(arg0);
    }

    public LuceneTransactionException(String arg0, Throwable arg1)
    {
        super(arg0, arg1);
    }
}
