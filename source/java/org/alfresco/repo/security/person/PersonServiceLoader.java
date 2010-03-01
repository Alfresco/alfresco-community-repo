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
package org.alfresco.repo.security.person;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.transaction.AlfrescoTransactionSupport;
import org.alfresco.repo.transaction.RetryingTransactionHelper;
import org.alfresco.repo.transaction.AlfrescoTransactionSupport.TxnReadState;
import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.datatype.DefaultTypeConverter;
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

    private final int batchCount;;

    private PersonServiceLoader(ApplicationContext ctx, int batchSize, int batchCount)
    {
        this.ctx = ctx;
        this.batchSize = batchSize;
        this.batchCount = batchCount;
    }

    public void run(String user, String pwd, int threads) throws Exception
    {

        Thread runner = null;

        for (int i = 0; i < threads; i++)
        {
            runner = new Nester("Loader-" + i, runner, ctx, batchSize, batchCount);
        }
        if (runner != null)
        {
            runner.start();

            try
            {
                runner.join();
            }
            catch (InterruptedException e)
            {
                e.printStackTrace();
            }
        }

    }

    public static void main(String[] args)
    {
        StringBuilder sb = new StringBuilder();
        sb.append("\n").append("Usage\n").append(
                "   PersonServiceLoader  --user=<username> --pwd=<password> --batch-count=<batch-count> --batch-size=<batch-size> --threads=<threads>\n");
        String usage = sb.toString();
        ArgumentHelper argHelper = new ArgumentHelper(usage, args);
        try
        {
            String user = argHelper.getStringValue("user", true, true);
            String pwd = argHelper.getStringValue("pwd", true, true);
            int batchCount = argHelper.getIntegerValue("batch-count", true, 1, Integer.MAX_VALUE);
            int batchSize = argHelper.getIntegerValue("batch-size", true, 1, Integer.MAX_VALUE);
            int threads = argHelper.getIntegerValue("threads", true, 1, Integer.MAX_VALUE);

            ApplicationContext ctx = ApplicationContextHelper.getApplicationContext();

            // Create the worker instance
            PersonServiceLoader loader = new PersonServiceLoader(ctx, batchSize, batchCount);
            loader.run(user, pwd, threads);

            // check the lazy creation

            AuthenticationUtil.setFullyAuthenticatedUser(AuthenticationUtil.getSystemUserName());

            final ServiceRegistry serviceRegistry = (ServiceRegistry) ctx.getBean(ServiceRegistry.SERVICE_REGISTRY);
            final AuthenticationService authenticationService = serviceRegistry.getAuthenticationService();
            final PersonService personService = serviceRegistry.getPersonService();
            final TransactionService transactionService = serviceRegistry.getTransactionService();
            final NodeService nodeService = serviceRegistry.getNodeService();

            String firstName = "" + System.currentTimeMillis();
            String lastName = String.format("%05d", -1);
            final String username = GUID.generate();
            String emailAddress = String.format("%s.%s@xyz.com", firstName, lastName);
            PropertyMap properties = new PropertyMap(7);
            properties.put(ContentModel.PROP_USERNAME, username);
            properties.put(ContentModel.PROP_FIRSTNAME, firstName);
            properties.put(ContentModel.PROP_LASTNAME, lastName);
            properties.put(ContentModel.PROP_EMAIL, emailAddress);
            NodeRef madePerson = personService.createPerson(properties);

            NodeRef homeFolder = DefaultTypeConverter.INSTANCE.convert(NodeRef.class, nodeService.getProperty(madePerson, ContentModel.PROP_HOMEFOLDER));
            if (homeFolder != null)
            {
                throw new IllegalStateException("Home folder created eagerly");
            }

            RetryingTransactionHelper helper = transactionService.getRetryingTransactionHelper();
            helper.doInTransaction(new RetryingTransactionCallback<Void>()
            {
                public Void execute() throws Throwable
                {
                    NodeRef person = personService.getPerson(username);
                    NodeRef homeFolder = DefaultTypeConverter.INSTANCE.convert(NodeRef.class, nodeService.getProperty(person, ContentModel.PROP_HOMEFOLDER));
                    if (homeFolder == null)
                    {
                        throw new IllegalStateException("Home folder not created lazily");
                    }
                    return null;
                }
            }, true, true);

            
            NodeRef autoPerson = personService.getPerson(GUID.generate());
            NodeRef autoHomeFolder = DefaultTypeConverter.INSTANCE.convert(NodeRef.class, nodeService.getProperty(autoPerson, ContentModel.PROP_HOMEFOLDER));
            if (autoHomeFolder == null)
            {
                throw new IllegalStateException("Home folder not created lazily for auto created users");
            }

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

    static class Nester extends Thread
    {
        Thread waiter;

        int batchSize;

        int batchCount;

        ApplicationContext ctx;

        ServiceRegistry serviceRegistry;

        PersonService personService;

        TransactionService transactionService;

        Nester(String name, Thread waiter, ApplicationContext ctx, int batchSize, int batchCount)
        {
            super(name);
            this.setDaemon(true);
            this.waiter = waiter;
            this.ctx = ctx;
            serviceRegistry = (ServiceRegistry) ctx.getBean(ServiceRegistry.SERVICE_REGISTRY);
            personService = serviceRegistry.getPersonService();
            transactionService = serviceRegistry.getTransactionService();
            this.batchSize = batchSize;
            this.batchCount = batchCount;
        }

        public void run()
        {
            AuthenticationUtil.setFullyAuthenticatedUser(AuthenticationUtil.SYSTEM_USER_NAME);
            if (waiter != null)
            {
                waiter.start();
            }
            try
            {
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

                for (int i = 0; i < batchCount; i++)
                {
                    long start = System.nanoTime();
                    transactionService.getRetryingTransactionHelper().doInTransaction(makeUsersCallback, false, true);
                    long end = System.nanoTime();
                    double deltaMs = (double) (end - start) / 1000000.0D;
                    double ave = deltaMs / (double) batchSize;
                    System.out.println("\n"
                            + Thread.currentThread().getName() + "\n" + "Batch users created: \n" + "   Batch Number:    " + i + "\n" + "   Batch Size:      " + batchSize + "\n"
                            + "   Batch Time (ms): " + Math.floor(deltaMs) + "\n" + "   Average (ms):    " + Math.floor(ave));
                }
            }
            catch (Exception e)
            {
                System.out.println("End " + this.getName() + " with error " + e.getMessage());
                e.printStackTrace();
            }
            finally
            {
                AuthenticationUtil.clearCurrentSecurityContext();
            }
            if (waiter != null)
            {
                try
                {
                    waiter.join();
                }
                catch (InterruptedException e)
                {
                }
            }
        }
    }
}
