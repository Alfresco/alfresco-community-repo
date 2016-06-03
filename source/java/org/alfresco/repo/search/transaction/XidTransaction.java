package org.alfresco.repo.search.transaction;

import javax.transaction.Transaction;
import javax.transaction.xa.Xid;

public interface XidTransaction extends Xid, Transaction
{

}
