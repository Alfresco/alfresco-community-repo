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

import static org.alfresco.util.ParameterCheck.mandatory;

import java.io.Serializable;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.alfresco.model.ContentModel;
import org.alfresco.module.org_alfresco_module_rm.classification.ClassificationServiceException.InvalidNode;
import org.alfresco.module.org_alfresco_module_rm.classification.ClassificationServiceException.LevelIdNotFound;
import org.alfresco.module.org_alfresco_module_rm.classification.ClassificationServiceException.MissingConfiguration;
import org.alfresco.module.org_alfresco_module_rm.classification.ClassificationServiceException.ReasonIdNotFound;
import org.alfresco.module.org_alfresco_module_rm.classification.model.ClassifiedContentModel;
import org.alfresco.module.org_alfresco_module_rm.util.ServiceBaseImpl;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.service.cmr.attributes.AttributeService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.QName;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Neil Mc Erlean
 * @since 3.0
 */
public class ClassificationServiceImpl extends ServiceBaseImpl
                                       implements ClassificationService, ClassifiedContentModel
{
    private static final Serializable[] LEVELS_KEY = new String[] { "org.alfresco",
                                                              "module.org_alfresco_module_rm",
                                                              "classification.levels" };
    private static final Serializable[] REASONS_KEY = new String[] { "org.alfresco",
                                                               "module.org_alfresco_module_rm",
                                                               "classification.reasons" };
    private static final Logger LOGGER = LoggerFactory.getLogger(ClassificationServiceImpl.class);

    private AttributeService attributeService; // TODO What about other code (e.g. REST API) accessing the AttrService?
    private NodeService nodeService;
    private ClassificationServiceDAO classificationServiceDao;

    /** The classification levels currently configured in this server. */
    private ClassificationLevelManager levelManager;
    /** The classification reasons currently configured in this server. */
    private ClassificationReasonManager reasonManager;

    public void setAttributeService(AttributeService service) { this.attributeService = service; }
    public void setNodeService(NodeService service) { this.nodeService = service; }

    /** Set the object from which configuration options will be read. */
    public void setClassificationServiceDAO(ClassificationServiceDAO classificationServiceDao) { this.classificationServiceDao = classificationServiceDao; }

    void initConfiguredClassificationLevels()
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
            this.levelManager = new ClassificationLevelManager(configurationLevels);
        }
        else
        {
            this.levelManager = new ClassificationLevelManager(allPersistedLevels);
        }
    }

    void initConfiguredClassificationReasons()
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
            this.reasonManager = new ClassificationReasonManager(classpathReasons);
        }
        else
        {
            if (isEmpty(classpathReasons) || !classpathReasons.equals(persistedReasons))
            {
                LOGGER.warn("Classification reasons configured in classpath do not match those stored in Alfresco. "
                            + "Alfresco will use the unchanged values stored in the database.");
                // RM-2073 says that we should log a warning and proceed normally.
            }
            this.reasonManager = new ClassificationReasonManager(persistedReasons);
        }
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
     * Gets the list (in descending order) of classification levels - as persisted in the system.
     * @return the list of classification levels if they have been persisted, else {@code null}.
     */
    List<ClassificationLevel> getPersistedLevels()
    {
        return authenticationUtil.runAsSystem(new AuthenticationUtil.RunAsWork<List<ClassificationLevel>>()
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
    List<ClassificationLevel> getConfigurationLevels()
    {
        return classificationServiceDao.getConfiguredLevels();
    }

    /**
     * Gets the list of classification reasons as persisted in the system.
     * @return the list of classification reasons if they have been persisted, else {@code null}.
     */
    List<ClassificationReason> getPersistedReasons()
    {
        return authenticationUtil.runAsSystem(new AuthenticationUtil.RunAsWork<List<ClassificationReason>>()
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
    List<ClassificationReason> getConfigurationReasons()
    {
        return classificationServiceDao.getConfiguredReasons();
    }

    /**
     * Create a list containing all classification levels up to and including the supplied level.
     *
     * @param allLevels The list of all the classification levels starting with the highest security.
     * @param targetLevel The highest security classification level that should be returned. If this is not found then
     *            an empty list will be returned.
     * @return an immutable list of the levels that a user at the target level can see.
     */
    List<ClassificationLevel> restrictList(List<ClassificationLevel> allLevels, ClassificationLevel targetLevel)
    {
        int targetIndex = allLevels.indexOf(targetLevel);
        if (targetIndex == -1) { return Collections.emptyList(); }
        List<ClassificationLevel> subList = allLevels.subList(targetIndex, allLevels.size());
        return Collections.unmodifiableList(subList);
    }

    @Override
    public List<ClassificationLevel> getClassificationLevels()
    {
        if (levelManager == null)
        {
            return Collections.emptyList();
        }
        // FIXME Currently assume user has highest security clearance, this should be fixed as part of RM-2112.
        ClassificationLevel usersLevel = levelManager.getMostSecureLevel();
        return restrictList(levelManager.getClassificationLevels(), usersLevel);
    }

    @Override public List<ClassificationReason> getClassificationReasons()
    {
        return reasonManager == null ? Collections.<ClassificationReason>emptyList() :
                Collections.unmodifiableList(reasonManager.getClassificationReasons());
    }

    @Override
    public void classifyContent(String classificationLevelId, String classificationAuthority,
                Set<String> classificationReasonIds, NodeRef content)
    {
        mandatory("classificationLevelId", classificationLevelId);
        mandatory("classificationAuthority", classificationAuthority);
        mandatory("classificationReasonIds", classificationReasonIds);
        mandatory("content", content);

        if (!nodeService.getType(content).equals(ContentModel.TYPE_CONTENT))
        {
            throw new InvalidNode(content, "The supplied node is not a content node.");
        }
        if (nodeService.hasAspect(content, ASPECT_CLASSIFIED))
        {
            throw new UnsupportedOperationException(
                        "The content has already been classified. Reclassification is currently not supported.");
        }

        Map<QName, Serializable> properties = new HashMap<QName, Serializable>();
        checkClassificationLevelId(classificationLevelId);

        // Initial classification id
        if (nodeService.getProperty(content, PROP_INITIAL_CLASSIFICATION) == null)
        {
            properties.put(PROP_INITIAL_CLASSIFICATION, classificationLevelId);
        }

        // Current classification id
        properties.put(PROP_CURRENT_CLASSIFICATION, classificationLevelId);

        // Classification authority
        properties.put(PROP_CLASSIFICATION_AUTHORITY, classificationAuthority);

        // Classification reason ids
        HashSet<String> classificationReasons = new HashSet<>();
        for (String classificationReasonId : classificationReasonIds)
        {
            checkClassificationReasonId(classificationReasonId);
            classificationReasons.add(classificationReasonId);
        }
        properties.put(PROP_CLASSIFICATION_REASONS, classificationReasons);

        // Add aspect
        nodeService.addAspect(content, ASPECT_CLASSIFIED, properties);
    }

    /**
     * Helper method to check if a classification level with the given id exists
     *
     * @param classificationLevelId {@link String} The id of the classification level
     * @throws {@link LevelIdNotFound} throws an exception if a classification level with given id does not exist
     */
    private void checkClassificationLevelId(String classificationLevelId) throws LevelIdNotFound
    {
        levelManager.findLevelById(classificationLevelId);
    }

    /**
     * Helper method to check if a classification reason with the given id exists
     *
     * @param classificationReasonId {@String} The id of the classification reason
     * @throws {@link ReasonIdNotFound} throws an exception if a classification reason with the given id does not exist
     */
    private void checkClassificationReasonId(String classificationReasonId) throws ReasonIdNotFound
    {
        reasonManager.findReasonById(classificationReasonId);
    }
}
