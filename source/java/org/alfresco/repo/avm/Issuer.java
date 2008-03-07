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
 * http://www.alfresco.com/legal/licensing" */

package org.alfresco.repo.avm;

import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;
import org.alfresco.service.transaction.TransactionService;

/**
 * This is a helper class that knows how to issue identifiers.
 * @author britt
 */
public class Issuer
{
    /**
     * How large a block of ids to grab at a time.
     */
    private static final int BLOCK_SIZE = 100;

    /**
     * The next number to issue.
     */
    private long fNext;

    private long fLast;

    /**
     * The name of this issuer.
     */
    private String fName;

    private IssuerIDDAO fIDDAO;

    private IssuerDAO fIssuerDAO;

    private TransactionService fTxnService;

    /**
     * Default constructor.
     */
    public Issuer()
    {
        fNext = 0;
        fLast = 0;
    }

    public void setIssuerIDDAO(IssuerIDDAO dao)
    {
        fIDDAO = dao;
    }

    public void setIssuerDAO(IssuerDAO dao)
    {
        fIssuerDAO = dao;
    }

    public void setTransactionService(TransactionService service)
    {
        fTxnService = service;
    }

    public void init()
    {
        fTxnService.getRetryingTransactionHelper().doInTransaction(
        new RetryingTransactionCallback<Object>()
        {
            public Object execute()
            {
                IssuerID issuerID = fIDDAO.get(fName);
                Long id = fIssuerDAO.getIssuerValue(fName);
                if (issuerID == null || id == null || id >= issuerID.getNext())
                {
                    if (id == null)
                    {
                        id = 0L;
                    }
                    else
                    {
                        id = id + 1L;
                    }
                    if (issuerID == null)
                    {
                        issuerID = new IssuerIDImpl(fName, id);
                        fIDDAO.save(issuerID);
                    }
                    else
                    {
                        issuerID.setNext(id);
                    }
                }
                return null;
            }
        });
    }

    /**
     * Set the name of this issuer. For Spring.
     * @param name The name to set.
     */
    public void setName(String name)
    {
        fName = name;
    }

    /**
     * Issue the next number.
     * @return A serial number.
     */
    public synchronized long issue()
    {
        if (fNext >= fLast)
        {
            BlockGetter getter = new BlockGetter();
            Thread thread = new Thread(getter);
            thread.start();
            try
            {
                thread.join();
            }
            catch (InterruptedException e)
            {
                // Do nothing.
            }
            fNext = getter.fNext;
            fLast = getter.fLast;
        }
        return fNext++;
    }

    private class BlockGetter implements Runnable
    {
        public long fNext;

        public long fLast;

        /* (non-Javadoc)
         * @see java.lang.Runnable#run()
         */
        public void run()
        {
            fTxnService.getRetryingTransactionHelper().doInTransaction(
            new RetryingTransactionCallback<Object>()
            {
                public Object execute()
                {
                    IssuerID isID = fIDDAO.get(fName);
                    fNext = isID.getNext();
                    fLast = fNext + BLOCK_SIZE;
                    isID.setNext(fLast);
                    return null;
                }
            });
        }
    }
}
