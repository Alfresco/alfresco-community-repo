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
package org.alfresco.module.org_alfresco_module_rm.classification;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import com.google.common.collect.ImmutableList;
import org.alfresco.module.org_alfresco_module_rm.classification.ClassificationServiceException.MissingConfiguration;
import org.alfresco.module.org_alfresco_module_rm.classification.model.ClassifiedContentModel;
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
 * This class is responsible for initialising any Classification-specific data on server bootstrap.
 *
 * @author Neil Mc Erlean
 * @since 3.0
 */
public class ClassificationServiceBootstrap extends AbstractLifecycleBean implements ClassifiedContentModel
{
    /** Logging utility for the class. */
    private static final Logger LOGGER = LoggerFactory.getLogger(ClassificationServiceBootstrap.class);

    private final AuthenticationUtil           authenticationUtil;
    private final TransactionService           transactionService;
    private AttributeService attributeService;
    /** The classification levels currently configured in this server. */
    private ClassificationLevelManager classificationLevelManager = new ClassificationLevelManager();
    /** The classification reasons currently configured in this server. */
    private ClassificationReasonManager classificationReasonManager = new ClassificationReasonManager();
    /** The clearance levels currently configured in this server. */
    private ClearanceLevelManager clearanceLevelManager = new ClearanceLevelManager();
    private ClassificationServiceDAO classificationServiceDAO;

    public ClassificationServiceBootstrap(AuthenticationUtil authUtil,
                                          TransactionService txService,
                                          AttributeService attributeService,
                                          ClassificationServiceDAO classificationServiceDAO)
    {
        this.authenticationUtil       = authUtil;
        this.transactionService       = txService;
        this.attributeService         = attributeService;
        this.classificationServiceDAO = classificationServiceDAO;
    }

    /** Set the object from which configuration options will be read. */
    public void setClassificationServiceDAO(ClassificationServiceDAO classificationServiceDAO) { this.classificationServiceDAO = classificationServiceDAO; }
    public void setAttributeService(AttributeService attributeService) { this.attributeService = attributeService; }
    /** Used in unit tests. */
    protected void setClearanceLevelManager(ClearanceLevelManager clearanceLevelManager) { this.clearanceLevelManager = clearanceLevelManager; }

    public ClassificationLevelManager getClassificationLevelManager() { return classificationLevelManager; }
    public ClassificationReasonManager getClassificationReasonManager() { return classificationReasonManager; }
    public ClearanceLevelManager getClearanceLevelManager() { return clearanceLevelManager; }

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
                        initConfiguredClassificationLevels();
                        initConfiguredClassificationReasons();
                        initConfiguredClearanceLevels(classificationLevelManager.getClassificationLevels());
                        return null;
                    }
                };
                transactionService.getRetryingTransactionHelper().doInTransaction(callback);
                return null;
            }
        });
    }

    /**
     * Initialise the system's classification levels by loading the values from a configuration file and storing them
     * in the attribute service and the {@link ClassificationLevelManager}.
     */
    protected void initConfiguredClassificationLevels()
    {
        final List<ClassificationLevel> allPersistedLevels  = getPersistedLevels();
        final List<ClassificationLevel> configurationLevels = getConfigurationLevels();

        // Note! We cannot log the level names or even the size of these lists for security reasons.
        LOGGER.debug("Persisted classification levels: {}", loggableStatusOf(allPersistedLevels));
        LOGGER.debug("Classpath classification levels: {}", loggableStatusOf(configurationLevels));

        if (configurationLevels == null || configurationLevels.isEmpty())
        {
            throw new MissingConfiguration("Classification level configuration is missing.");
        }
        else if (!configurationLevels.equals(allPersistedLevels))
        {
            attributeService.setAttribute((Serializable) configurationLevels, LEVELS_KEY);
            this.classificationLevelManager.setClassificationLevels(configurationLevels);
        }
        else
        {
            this.classificationLevelManager.setClassificationLevels(allPersistedLevels);
        }
    }

    /**
     * Gets the list (in descending order) of classification levels - as persisted in the system.
     * @return the list of classification levels if they have been persisted, else {@code null}.
     */
    private List<ClassificationLevel> getPersistedLevels()
    {
        return authenticationUtil.runAsSystem(new RunAsWork<List<ClassificationLevel>>()
        {
            @Override
            @SuppressWarnings("unchecked")
            public List<ClassificationLevel> doWork() throws Exception
            {
                return (List<ClassificationLevel>) attributeService.getAttribute(LEVELS_KEY);
            }
        });
    }

    /** Gets the list (in descending order) of classification levels - as defined in the system configuration. */
    private List<ClassificationLevel> getConfigurationLevels()
    {
        return classificationServiceDAO.getConfiguredLevels();
    }

    private static boolean isEmpty(List<?> l) { return l == null || l.isEmpty(); }

    /** Helper method for debug-logging of sensitive lists. */
    private String loggableStatusOf(List<?> l)
    {
        if      (l == null)   { return "null"; }
        else if (l.isEmpty()) { return "empty"; }
        else                  { return "non-empty"; }
    }

    /**
     * Initialise the system's classification reasons by loading the values from a configuration file and storing them
     * in the attribute service and the {@link ClassificationReasonManager}.
     */
    protected void initConfiguredClassificationReasons()
    {
        final List<ClassificationReason> persistedReasons = getPersistedReasons();
        final List<ClassificationReason> classpathReasons = getConfigurationReasons();

        // Note! We cannot log the reasons or even the size of these lists for security reasons.
        LOGGER.debug("Persisted classification reasons: {}", loggableStatusOf(persistedReasons));
        LOGGER.debug("Classpath classification reasons: {}", loggableStatusOf(classpathReasons));

        if (isEmpty(persistedReasons))
        {
            if (isEmpty(classpathReasons))
            {
                throw new MissingConfiguration("Classification reason configuration is missing.");
            }
            attributeService.setAttribute((Serializable) classpathReasons, REASONS_KEY);
            this.classificationReasonManager.setClassificationReasons(classpathReasons);
        }
        else
        {
            if (isEmpty(classpathReasons) || !classpathReasons.equals(persistedReasons))
            {
                LOGGER.warn("Classification reasons configured in classpath do not match those stored in Alfresco. "
                            + "Alfresco will use the unchanged values stored in the database.");
                // RM-2073 says that we should log a warning and proceed normally.
            }
            this.classificationReasonManager.setClassificationReasons(persistedReasons);
        }
    }

    /**
     * Gets the list of classification reasons as persisted in the system.
     * @return the list of classification reasons if they have been persisted, else {@code null}.
     */
    private List<ClassificationReason> getPersistedReasons()
    {
        return authenticationUtil.runAsSystem(new RunAsWork<List<ClassificationReason>>()
        {
            @Override
            @SuppressWarnings("unchecked")
            public List<ClassificationReason> doWork() throws Exception
            {
                return (List<ClassificationReason>) attributeService.getAttribute(REASONS_KEY);
            }
        });
    }

    /** Gets the list of classification reasons - as defined and ordered in the system configuration. */
    private List<ClassificationReason> getConfigurationReasons()
    {
        return classificationServiceDAO.getConfiguredReasons();
    }

    /**
     * Initialise and create a {@link ClearanceLevelManager}.
     *
     * @param classificationLevels The list of classification levels to use to create clearance levels from.
     */
    protected void initConfiguredClearanceLevels(ImmutableList<ClassificationLevel> classificationLevels)
    {
        List<ClearanceLevel> clearanceLevels = new ArrayList<>();
        for (ClassificationLevel classificationLevel : classificationLevels)
        {
            String displayLabelKey = classificationLevel.getDisplayLabelKey();
            if (classificationLevel.equals(ClassificationLevelManager.UNCLASSIFIED))
            {
                displayLabelKey = "rm.classification.noClearance";
            }
            clearanceLevels.add(new ClearanceLevel(classificationLevel, displayLabelKey));
        }
        this.clearanceLevelManager.setClearanceLevels(clearanceLevels);
    }

    @Override protected void onShutdown(ApplicationEvent event)
    {
        // Intentionally empty.
    }
}
