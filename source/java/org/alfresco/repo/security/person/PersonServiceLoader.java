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
package org.alfresco.repo.security.person;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.security.AuthenticationService;
import org.alfresco.service.cmr.security.PersonService;
import org.alfresco.service.transaction.TransactionService;
import org.alfresco.util.ApplicationContextHelper;
import org.alfresco.util.ArgumentHelper;
import org.alfresco.util.GUID;
import org.alfresco.util.PropertyMap;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.context.ApplicationContext;

/**
 * A simple program to load users into the system.
 * 
 * @author Derek Hulley
 * @since V2.1-A
 */
public class PersonServiceLoader
{
    private static Log logger = LogFactory.getLog(PersonServiceLoader.class);
    
    private final ApplicationContext ctx;
    private final int batchSize;
    private final int batchCount;
;
    
    private PersonServiceLoader(ApplicationContext ctx, int batchSize, int batchCount)
    {
        this.ctx = ctx;
        this.batchSize = batchSize;
        this.batchCount = batchCount;
    }
    
    public void run(String user, String pwd) throws Exception
    {
        final ServiceRegistry serviceRegistry = (ServiceRegistry) ctx.getBean(ServiceRegistry.SERVICE_REGISTRY);
        final AuthenticationService authenticationService = serviceRegistry.getAuthenticationService();
        final PersonService personService = serviceRegistry.getPersonService();
        final TransactionService transactionService = serviceRegistry.getTransactionService();
        
        authenticationService.authenticate(user, pwd.toCharArray());
        
        RetryingTransactionCallback<Integer> makeUsersCallback = new RetryingTransactionCallback<Integer>()
        {
            public Integer execute() throws Throwable
            {
                for (int i = 0; i < batchSize; i++)
                {
                    String firstName = "" + System.currentTimeMillis();
                    String lastName = String.format("%05d", i);
                    String username = GUID.generate();
                    String emailAddress = String.format("%s.%s@xyz.com", firstName, lastName);
                    PropertyMap properties = new PropertyMap(7);
                    properties.put(ContentModel.PROP_USERNAME, username);
                    properties.put(ContentModel.PROP_FIRSTNAME, firstName);
                    properties.put(ContentModel.PROP_LASTNAME, lastName);
                    properties.put(ContentModel.PROP_EMAIL, emailAddress);
                    personService.createPerson(properties);
                }
                return batchSize;
            }
        };
        
        for (int i = 0; i < batchCount; i ++)
        {
            long start = System.nanoTime();
            transactionService.getRetryingTransactionHelper().doInTransaction(makeUsersCallback, false, true);
            long end = System.nanoTime();
            double deltaMs = (double) (end - start) / 1000000.0D;
            double ave = deltaMs / (double) batchSize;
            System.out.println("\n" +
            		"Batch users created: \n" +
            		"   Batch Number:    " + i + "\n" +
            		"   Batch Size:      " + batchSize + "\n" +
            		"   Batch Time (ms): " + Math.floor(deltaMs) + "\n" +
            		"   Average (ms):    " + Math.floor(ave));
        }
    }
    
    public static void main(String[] args)
    {
        StringBuilder sb = new StringBuilder();
        sb.append("\n")
          .append("Usage\n")
          .append("   PersonServiceLoader  --user=<username> --pwd=<password> --batch-count=<batch-count> -- batch-size=<batch-size> \n");
        String usage = sb.toString();
        ArgumentHelper argHelper = new ArgumentHelper(usage, args);
        try
        {
            String user = argHelper.getStringValue("user", true, true);
            String pwd = argHelper.getStringValue("pwd", true, true);
            int batchCount = argHelper.getIntegerValue("batch-count", true, 1, Integer.MAX_VALUE);
            int batchSize = argHelper.getIntegerValue("batch-size", true, 1, Integer.MAX_VALUE);

            ApplicationContext ctx = ApplicationContextHelper.getApplicationContext();

            // Create the worker instance
            PersonServiceLoader loader = new PersonServiceLoader(ctx, batchSize, batchCount);
            loader.run(user, pwd);
            
            // All done
            ApplicationContextHelper.closeApplicationContext();
            System.exit(0);
        }
        catch (IllegalArgumentException e)
        {
            System.out.println(e.getMessage());
            argHelper.printUsage();
            System.exit(1);
        }
        catch (Throwable e)
        {
            logger.error("PersonServiceLoader (userCount, batchSize) failed.", e);
            System.exit(1);
        }
    }
}
