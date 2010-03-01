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
package org.alfresco.repo.avm;

import java.util.Calendar;
import java.util.Date;

import org.alfresco.model.WCMAppModel;
import org.alfresco.repo.importer.ImporterBootstrap;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.search.ResultSet;
import org.alfresco.service.cmr.search.SearchService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.transaction.TransactionService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Bean that is responsible for locating expired content and routing
 * it for review to the most relevant user.
 * 
 * @author gavinc
 */
public class AVMDeploymentAttemptCleaner
{
    // defaults in case these properties are not configured in Spring
    protected long maxAge = 180L;
    
    protected NodeService nodeService;
    protected TransactionService transactionService;
    protected SearchService searchService;
    protected ImporterBootstrap importerBootstrap;
    
    private static Log logger = LogFactory.getLog(AVMDeploymentAttemptCleaner.class);

    public AVMDeploymentAttemptCleaner()
    {
    }
    
    public void setMaxAge(long maxAge)
    {
       this.maxAge = new Long(maxAge);
    }
    
    public void setNodeService(NodeService nodeService)
    {
        this.nodeService = nodeService;
    }
    
    public void setTransactionService(TransactionService transactionService)
    {
        this.transactionService = transactionService;
    }

    public void setSearchService(SearchService searchService)
    {
        this.searchService = searchService;
    }
    
    public void setImporterBootstrap(ImporterBootstrap importerBootstrap)
    {
        this.importerBootstrap = importerBootstrap;
    }
    
    /**
     * Executes the expired content processor.
     * The work is performed within a transaction running as the system user.
     */
    public void execute()
    {
        // setup a wrapper object to run the processor within a transaction.
        AuthenticationUtil.RunAsWork<String> authorisedWork = new AuthenticationUtil.RunAsWork<String>()
        {
            public String doWork() throws Exception
            {
                RetryingTransactionCallback<String> expiredContentWork = new RetryingTransactionCallback<String>()
                {
                    public String execute() throws Exception
                    {
                         cleanAttempts();
                         return null;
                     }
                 };
                 return transactionService.getRetryingTransactionHelper().doInTransaction(expiredContentWork);
             }
         };
         
         // perform the work as the system user
         AuthenticationUtil.runAs(authorisedWork, AuthenticationUtil.getAdminUserName());
    }
    
    /**
     * Entry point.
     */
    private void cleanAttempts()
    {
        // calculate the date 'maxAge' days before today
        long daysInMs = 1000L*60L*60L*24L*this.maxAge;
        Date toDate = new Date(new Date().getTime() - daysInMs);
        
        // build the query to find deployment attempts older than this.maxAge
        Calendar cal = Calendar.getInstance();
        cal.setTime(toDate);
        StringBuilder query = new StringBuilder("@");
        query.append(NamespaceService.WCMAPP_MODEL_PREFIX);
        query.append("\\:");
        query.append(WCMAppModel.PROP_DEPLOYATTEMPTTIME.getLocalName());
        query.append(":[0001\\-01\\-01T00:00:00 TO ");
        query.append(cal.get(Calendar.YEAR));
        query.append("\\-");
        query.append((cal.get(Calendar.MONTH)+1));
        query.append("\\-");
        query.append(cal.get(Calendar.DAY_OF_MONTH));
        query.append("T00:00:00]");
        
        if (logger.isDebugEnabled())
            logger.debug("Finding old deploymentattempt nodes using query: " + query.toString());
        
        // do the query
        ResultSet results = null;
        try
        {
            // execute the query
            results = searchService.query(this.importerBootstrap.getStoreRef(), 
                     SearchService.LANGUAGE_LUCENE, query.toString());
         
            if (logger.isDebugEnabled())
                logger.debug("Deleting " + results.length() + " old deployment attempts");
         
            // iterate through the attempt nodes and delete them
            for (NodeRef attempt : results.getNodeRefs())
            {
                this.nodeService.deleteNode(attempt);
                
                if (logger.isDebugEnabled())
                    logger.debug("Deleted deployment attempt: " + attempt);
            }
        }
        finally
        {
            if (results != null)
            {
                results.close();
            }
        }
    }
}
