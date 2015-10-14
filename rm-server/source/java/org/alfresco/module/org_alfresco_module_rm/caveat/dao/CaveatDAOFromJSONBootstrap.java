/*
 * Copyright (C) 2005-2015 Alfresco Software Limited.
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
package org.alfresco.module.org_alfresco_module_rm.caveat.dao;

import static org.alfresco.module.org_alfresco_module_rm.caveat.CaveatConstants.CAVEAT_ATTRIBUTE_KEY;

import java.io.Serializable;
import java.util.Map;

import com.google.common.collect.ImmutableMap;
import org.alfresco.module.org_alfresco_module_rm.caveat.CaveatException.MalformedConfiguration;
import org.alfresco.module.org_alfresco_module_rm.caveat.scheme.CaveatGroup;
import org.alfresco.module.org_alfresco_module_rm.util.AuthenticationUtil;
import org.alfresco.repo.security.authentication.AuthenticationUtil.RunAsWork;
import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;
import org.alfresco.service.cmr.attributes.AttributeService;
import org.alfresco.service.transaction.TransactionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEvent;
import org.springframework.extensions.surf.util.AbstractLifecycleBean;

/**
 * This class is responsible for ensuring all caveat data is loaded from the JSON configuration on start-up.
 *
 * @author Tom Page
 * @since 2.4.a
 */
public class CaveatDAOFromJSONBootstrap extends AbstractLifecycleBean
{
    /** Logging utility for the class. */
    private static final Logger LOGGER = LoggerFactory.getLogger(CaveatDAOFromJSONBootstrap.class);

    private final AuthenticationUtil authenticationUtil;
    private final TransactionService transactionService;
    private AttributeService attributeService;
    private CaveatDAOInterface caveatDAO;

    private boolean isInitialised = false;

    public CaveatDAOFromJSONBootstrap(AuthenticationUtil authUtil,
                                      TransactionService txService,
                                      AttributeService attributeService,
                                      CaveatDAOInterface caveatDAO)
    {
        this.authenticationUtil = authUtil;
        this.transactionService = txService;
        this.attributeService = attributeService;
        this.caveatDAO = caveatDAO;
    }

    /** Set the object from which configuration options will be read. */
    public void setClassificationServiceDAO(CaveatDAOInterface caveatDAO) { this.caveatDAO = caveatDAO; }
    public void setAttributeService(AttributeService attributeService) { this.attributeService = attributeService; }

    public boolean isInitialised()
    {
        return isInitialised;
    }

    @Override public void onBootstrap(ApplicationEvent event)
    {
        authenticationUtil.runAsSystem(new org.alfresco.repo.security.authentication.AuthenticationUtil.RunAsWork<Void>()
        {
            public Void doWork()
            {
                RetryingTransactionCallback<Void> callback = new RetryingTransactionCallback<Void>()
                {
                    public Void execute()
                    {
                        initialiseConfiguredCaveatGroups(CAVEAT_ATTRIBUTE_KEY);
                        isInitialised = true;
                        return null;
                    }
                };
                transactionService.getRetryingTransactionHelper().doInTransaction(callback);
                return null;
            }
        });
    }

    /**
     * Gets the caveat configuration persisted in the system.
     *
     * @return the persisted caveat groups if they have been persisted, else {@code null}.
     */
    private ImmutableMap<String, CaveatGroup> getPersistedCaveatGroups(final Serializable[] key)
    {
        return authenticationUtil.runAsSystem(new RunAsWork<ImmutableMap<String, CaveatGroup>>()
        {
            @Override
            @SuppressWarnings("unchecked")
            public ImmutableMap<String, CaveatGroup> doWork() throws Exception
            {
                // TODO: Although an ImmutableMap is stored, after restarting the server, a HashMap is returned.
                // Investigate why this is, and whether we can avoid creating a new copy of the map here.
                Map<String, CaveatGroup> persistedMap = (Map<String, CaveatGroup>) attributeService.getAttribute(key);
                if (persistedMap == null)
                {
                    return null;
                }
                return ImmutableMap.copyOf(persistedMap);
            }
        });
    }

    /** Return true if the map is null or empty. */
    private boolean isEmpty(Map<String, CaveatGroup> caveatGroups)
    {
        return (caveatGroups == null || caveatGroups.isEmpty());
    }

    /** Helper method for debug-logging sensitive caveat group information. */
    private String loggableStatusOf(Map<String, CaveatGroup> caveatGroups)
    {
        if      (caveatGroups == null)   { return "null"; }
        else if (caveatGroups.isEmpty()) { return "empty"; }
        else                             { return "non-empty"; }
    }

    /**
     * Get the configured caveat groups and marks from the JSON file.
     *
     * @param key The attribute service key for the caveat scheme.
     */
    protected void initialiseConfiguredCaveatGroups(Serializable[] key)
    {
        final ImmutableMap<String, CaveatGroup> persistedGroups = getPersistedCaveatGroups(key);
        final ImmutableMap<String, CaveatGroup> classpathGroups = caveatDAO.getCaveatGroups();

        // Note! We cannot log the entities or even the size of these lists for security reasons.
        LOGGER.debug("Persisted CaveatGroup: {}", loggableStatusOf(persistedGroups));
        LOGGER.debug("Classpath CaveatGroup: {}", loggableStatusOf(classpathGroups));

        if (isEmpty(classpathGroups))
        {
            throw new MalformedConfiguration("CaveatGroup configuration is missing.");
        }
        if (!classpathGroups.equals(persistedGroups))
        {
            if (!isEmpty(persistedGroups))
            {
                LOGGER.warn("CaveatGroup configuration changed. This may result in unpredictable results if the caveat scheme is already in use.");
            }
            attributeService.setAttribute(classpathGroups, key);
        }
    }

    @Override protected void onShutdown(ApplicationEvent event)
    {
        // Intentionally empty.
    }
}
