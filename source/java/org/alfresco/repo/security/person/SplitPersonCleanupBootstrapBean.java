/*
 * Copyright (C) 2005-2010 Alfresco Software Limited.
 *
 * This file is part of Alfresco
 *
 * Alfresco is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Alfresco is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Alfresco. If not, see <http://www.gnu.org/licenses/>.
 */
package org.alfresco.repo.security.person;

import java.util.Set;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.datatype.DefaultTypeConverter;
import org.alfresco.service.cmr.security.PersonService;
import org.alfresco.service.transaction.TransactionService;
import org.springframework.extensions.surf.util.AbstractLifecycleBean;
import org.alfresco.util.GUID;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.safehaus.uuid.UUID;
import org.springframework.context.ApplicationEvent;

/**
 * Remove all duplicate users that have previously been split and had guids added to the uid. This been should be wired up into a custom bootstrap process
 * 
 * @author Andy Hind
 */
public class SplitPersonCleanupBootstrapBean extends AbstractLifecycleBean
{
    protected final static Log log = LogFactory.getLog(SplitPersonCleanupBootstrapBean.class);

    private NodeService nodeService;

    private PersonService personService;

    private TransactionService transactionService;

    public void setNodeService(NodeService nodeService)
    {
        this.nodeService = nodeService;
    }

    public void setPersonService(PersonService personService)
    {
        this.personService = personService;
    }

    public void setTransactionService(TransactionService transactionService)
    {
        this.transactionService = transactionService;
    }

    @Override
    protected void onBootstrap(ApplicationEvent event)
    {
        // reindex
        log.info("Removing person entries with GUIDS at the end of the uid ...");
        int count = removePeopleWithGUIDBasedIds();
        log.info("... removed " + count);
    }

    /**
     * Can have uid+GUID or uid + "(" + GUID + ")"
     * 
     * @return
     */
    private int removePeopleWithGUIDBasedIds()
    {
        Integer count = transactionService.getRetryingTransactionHelper().doInTransaction(
                new RetryingTransactionCallback<Integer>()
                {
                    public Integer execute() throws Exception
                    {
                        int count = 0;
                        // A GUID should be 36 chars

                        Set<NodeRef> people = personService.getAllPeople();
                        for (NodeRef person : people)
                        {
                            String uid = DefaultTypeConverter.INSTANCE.convert(String.class, nodeService.getProperty(
                                    person, ContentModel.PROP_USERNAME));
                            if (isUIDWithGUID(uid))
                            {
                                // Delete via the person service to get the correct tidy up
                                personService.deletePerson(uid);
                                if (log.isDebugEnabled())
                                {
                                    log.debug("... removed person with uid " + uid);
                                }
                                log.info("... removed person with uid " + uid);
                                count++;
                            }
                        }
                        return count;
                    }

                });
        return count.intValue();

    }

    private boolean isUIDWithGUID(String uid)
    {
        if (uid.length() > 36)
        {
            // uid + GUID
            // Check the last 36 chars are a valid guid
            String guidString = uid.substring(uid.length() - 36);
            try
            {
                @SuppressWarnings("unused")
                UUID id = new UUID(guidString);
                // We have a valid guid.
                return true;
            }
            catch (NumberFormatException e)
            {
                // Not a valid GUID
            }
        }

        if (uid.length() > 38)
        {
            // UID + "(" + GUID + ")"
            String guidString = uid.substring(uid.length() - 38);
            if (guidString.startsWith("(") && guidString.endsWith(")"))
            {
                guidString = guidString.substring(1, 37);
                try
                {
                    @SuppressWarnings("unused")
                    UUID id = new UUID(guidString);
                    // We have a valid guid.
                    return true;
                }
                catch (NumberFormatException e)
                {
                    // Not a valid GUID
                }
            }

        }

        return false;
    }

    @Override
    protected void onShutdown(ApplicationEvent event)
    {
        // Nothing to do
    }

    public static void main(String[] args)
    {
        SplitPersonCleanupBootstrapBean tester = new SplitPersonCleanupBootstrapBean();
        String[] test = new String[] { "andy", "andy" + GUID.generate(), "andy(" + GUID.generate() + ")",
                GUID.generate() + "banana", "andy" + GUID.generate() + "banana",
                "adbadbaddbadbadbadbabdbadbadbabdabdbbadbadbabdbadbadbadb"

        };

        for (String uid : test)
        {
            System.out.println(uid + " ... is a uid with guid = " + tester.isUIDWithGUID(uid));
        }
    }
}
