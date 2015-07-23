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
import org.alfresco.module.org_alfresco_module_rm.classification.ClassificationException.MissingConfiguration;
import org.alfresco.module.org_alfresco_module_rm.classification.model.ClassifiedContentModel;
import org.alfresco.module.org_alfresco_module_rm.classification.validation.ClassificationLevelFieldsValidator;
import org.alfresco.module.org_alfresco_module_rm.classification.validation.ClassificationReasonFieldsValidator;
import org.alfresco.module.org_alfresco_module_rm.classification.validation.ClassificationSchemeEntityValidator;
import org.alfresco.module.org_alfresco_module_rm.classification.validation.ExemptionCategoryFieldsValidator;
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
 * @since 3.0.a
 */
public class ClassificationServiceBootstrap extends AbstractLifecycleBean implements ClassifiedContentModel
{
    /** Logging utility for the class. */
    private static final Logger LOGGER = LoggerFactory.getLogger(ClassificationServiceBootstrap.class);

    private final AuthenticationUtil authenticationUtil;
    private final TransactionService transactionService;
    private AttributeService attributeService;
    /** The classification levels currently configured in this server. */
    private ClassificationLevelManager classificationLevelManager = new ClassificationLevelManager();
    /** The classification reasons currently configured in this server. */
    private ClassificationReasonManager classificationReasonManager = new ClassificationReasonManager();
    /** The clearance levels currently configured in this server. */
    private ClearanceLevelManager clearanceLevelManager = new ClearanceLevelManager();
    /** The exemption categories currently configured in this server. */
    private ExemptionCategoryManager exemptionCategoryManager = new ExemptionCategoryManager();
    private ClassificationServiceDAO classificationServiceDAO;
    private ClassificationLevelFieldsValidator classificationLevelFieldsValidator = new ClassificationLevelFieldsValidator();
    private ClassificationSchemeEntityValidator<ClassificationLevel> classificationLevelValidator = new ClassificationSchemeEntityValidator<>(classificationLevelFieldsValidator);
    private ClassificationReasonFieldsValidator classificationReasonFieldsValidator = new ClassificationReasonFieldsValidator();
    private ClassificationSchemeEntityValidator<ClassificationReason> classificationReasonValidator = new ClassificationSchemeEntityValidator<>(classificationReasonFieldsValidator);
    private ExemptionCategoryFieldsValidator exemptionCategoryFieldsValidator = new ExemptionCategoryFieldsValidator();
    private ClassificationSchemeEntityValidator<ExemptionCategory> exemptionCategoryValidator = new ClassificationSchemeEntityValidator<>(exemptionCategoryFieldsValidator);

    private boolean isInitialised = false;

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
    protected void setExemptionCategoryManager(ExemptionCategoryManager exemptionCategoryManager) { this.exemptionCategoryManager = exemptionCategoryManager; }
    /** Used in unit tests. */
    protected void setClearanceLevelManager(ClearanceLevelManager clearanceLevelManager) { this.clearanceLevelManager = clearanceLevelManager; }

    public ClassificationLevelManager getClassificationLevelManager() { return classificationLevelManager; }
    public ClassificationReasonManager getClassificationReasonManager() { return classificationReasonManager; }
    public ExemptionCategoryManager getExemptionCategoryManager() { return exemptionCategoryManager; }
    public ClearanceLevelManager getClearanceLevelManager() { return clearanceLevelManager; }

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
                        List<ClassificationLevel> levels = getConfiguredSchemeEntities(
                                    ClassificationLevel.class, LEVELS_KEY, classificationLevelValidator);
                        classificationLevelManager.setClassificationLevels(levels);

                        List<ClassificationReason> reasons = getConfiguredSchemeEntities(
                                    ClassificationReason.class, REASONS_KEY, classificationReasonValidator);
                        classificationReasonManager.setClassificationReasons(reasons);

                        List<ExemptionCategory> exemptionCategories = getConfiguredSchemeEntities(
                                    ExemptionCategory.class, EXEMPTION_CATEGORIES_KEY, exemptionCategoryValidator);
                        exemptionCategoryManager.setExemptionCategories(exemptionCategories);

                        initConfiguredClearanceLevels(classificationLevelManager.getClassificationLevels());
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
     * Gets an ordered list of some attribute persisted in the system.
     * @return the list of values if they have been persisted, else {@code null}.
     */
    private <T> List<T> getPersistedValues(final Serializable[] key)
    {
        return authenticationUtil.runAsSystem(new RunAsWork<List<T>>()
        {
            @Override
            @SuppressWarnings("unchecked")
            public List<T> doWork() throws Exception
            {
                return (List<T>) attributeService.getAttribute(key);
            }
        });
    }

    private static boolean isEmpty(List<?> l) { return l == null || l.isEmpty(); }

    /** Helper method for debug-logging of sensitive lists. */
    private String loggableStatusOf(List<? extends ClassificationSchemeEntity> l)
    {
        if      (l == null)   { return "null"; }
        else if (l.isEmpty()) { return "empty"; }
        else                  { return "non-empty"; }
    }

    protected <T extends ClassificationSchemeEntity> List<T> getConfiguredSchemeEntities(Class<T> clazz, Serializable[] key, ClassificationSchemeEntityValidator<T> validator)
    {
        final List<T> persistedValues = getPersistedValues(key);
        final List<T> classpathValues = classificationServiceDAO.getConfiguredValues(clazz);

        // Note! We cannot log the entities or even the size of these lists for security reasons.
        LOGGER.debug("Persisted {}: {}", clazz.getSimpleName(), loggableStatusOf(persistedValues));
        LOGGER.debug("Classpath {}: {}", clazz.getSimpleName(), loggableStatusOf(classpathValues));

        validator.validate(classpathValues, clazz.getSimpleName());

        if (isEmpty(persistedValues))
        {
            if (isEmpty(classpathValues))
            {
                throw new MissingConfiguration(clazz.getSimpleName() + " configuration is missing.");
            }
            attributeService.setAttribute((Serializable) classpathValues, key);
            return classpathValues;
        }
        else
        {
            if (isEmpty(classpathValues) || !classpathValues.equals(persistedValues))
            {
                LOGGER.warn(clazz.getSimpleName() + " data configured in classpath does not match data stored in Alfresco. "
                            + "Alfresco will use the unchanged values stored in the database.");
            }
            return persistedValues;
        }
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
