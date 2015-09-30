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

import static org.alfresco.module.org_alfresco_module_rm.classification.ClassificationLevelManager.UNCLASSIFIED_ID;
import static org.alfresco.module.org_alfresco_module_rm.util.RMParameterCheck.checkNotBlank;
import static org.alfresco.util.ParameterCheck.mandatory;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

import org.alfresco.model.ContentModel;
import org.alfresco.model.QuickShareModel;
import org.alfresco.module.org_alfresco_module_rm.classification.ClassificationException.InvalidNode;
import org.alfresco.module.org_alfresco_module_rm.classification.ClassificationException.LevelIdNotFound;
import org.alfresco.module.org_alfresco_module_rm.classification.ClassificationException.ReasonIdNotFound;
import org.alfresco.module.org_alfresco_module_rm.classification.model.ClassifiedContentModel;
import org.alfresco.module.org_alfresco_module_rm.freeze.FreezeService;
import org.alfresco.module.org_alfresco_module_rm.referredmetadata.ReferredMetadataService;
import org.alfresco.module.org_alfresco_module_rm.util.ServiceBaseImpl;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.security.authentication.AuthenticationUtil.RunAsWork;
import org.alfresco.repo.security.permissions.AccessDeniedException;
import org.alfresco.service.cmr.repository.InvalidNodeRefException;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;

import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

/**
 * A service to handle the classification of content.
 *
 * @author tpage
 * @since 2.4.a
 */
public class ContentClassificationServiceImpl extends ServiceBaseImpl
                                              implements ContentClassificationService, ClassifiedContentModel
{
    private ClassificationLevelManager levelManager;
    private ClassificationReasonManager reasonManager;
    private SecurityClearanceService securityClearanceService;
    private ClassificationServiceBootstrap classificationServiceBootstrap;
    private FreezeService freezeService;
    private ReferredMetadataService referredMetadataService;

    public void setLevelManager(ClassificationLevelManager levelManager) { this.levelManager = levelManager; }
    public void setReasonManager(ClassificationReasonManager reasonManager) { this.reasonManager = reasonManager; }
    public void setSecurityClearanceService(SecurityClearanceService securityClearanceService) { this.securityClearanceService = securityClearanceService; }
    public void setClassificationServiceBootstrap(ClassificationServiceBootstrap classificationServiceBootstrap) { this.classificationServiceBootstrap = classificationServiceBootstrap; }
    public void setFreezeService(FreezeService service) { this.freezeService = service; }
    public void setReferredMetadataService(ReferredMetadataService service)
    {
        this.referredMetadataService = service;
    }

    public void init()
    {
        this.levelManager = classificationServiceBootstrap.getClassificationLevelManager();
        this.reasonManager = classificationServiceBootstrap.getClassificationReasonManager();
    }

    /**
     * @see org.alfresco.module.org_alfresco_module_rm.classification.ContentClassificationService#getCurrentClassification(org.alfresco.service.cmr.repository.NodeRef)
     */
    @Override
    public ClassificationLevel getCurrentClassification(final NodeRef nodeRef)
    {
        return AuthenticationUtil.runAsSystem(new RunAsWork<ClassificationLevel>()
        {
            public ClassificationLevel doWork() throws Exception
            {
                final String classificationId;

                if (nodeService.hasAspect(nodeRef, ASPECT_CLASSIFIED))
                {
                    classificationId = (String)nodeService.getProperty(nodeRef, PROP_CURRENT_CLASSIFICATION);
                }
                else if (referredMetadataService.isReferringMetadata(nodeRef, ASPECT_CLASSIFIED))
                {
                    classificationId = (String) referredMetadataService.getReferredProperty(nodeRef, PROP_CURRENT_CLASSIFICATION);
                    // Note that this property value could be null/missing.
                }
                else
                {
                    classificationId = null;
                }

                // by default everything is unclassified
                return classificationId == null ? ClassificationLevelManager.UNCLASSIFIED : levelManager.findLevelById(classificationId);
            }
        });
    };

    /**
     * @see org.alfresco.module.org_alfresco_module_rm.classification.ContentClassificationService#classifyContent(ClassificationAspectProperties, NodeRef)
     */
    @Override
    public void classifyContent(ClassificationAspectProperties classificationAspectProperties, final NodeRef content)
    {
        validateProperties(classificationAspectProperties);
        validateContent(content);

        final Map<QName, Serializable> properties = createPropertiesMap(classificationAspectProperties, content);

        // Add aspect
        authenticationUtil.runAsSystem(new RunAsWork<Void>()
        {
            public Void doWork()
            {
                if (freezeService.isFrozen(content))
                {
                    throw new AccessDeniedException("Frozen nodes can not be classified.");
                }

                nodeService.addAspect(content, ASPECT_CLASSIFIED, properties);
                return null;
            }
        });
    }

    /**
     * Validate the properties contained in the {@link ClassificationAspectProperties}.
     *
     * @param classificationAspectProperties The DTO containing properties to be stored on the aspect.
     */
    protected void validateProperties(ClassificationAspectProperties classificationAspectProperties)
    {
        String classificationLevelId = classificationAspectProperties.getClassificationLevelId();
        checkNotBlank("classificationLevelId", classificationLevelId);
        checkNotBlank("classifiedBy", classificationAspectProperties.getClassifiedBy());
        mandatory("classificationReasonIds", classificationAspectProperties.getClassificationReasonIds());

        if (!securityClearanceService.isCurrentUserClearedForClassification(classificationLevelId))
        {
            throw new LevelIdNotFound(classificationLevelId);
        }

        for (String classificationReasonId : classificationAspectProperties.getClassificationReasonIds())
        {
            // Check the classification reason id - an exception will be thrown if the id cannot be found
            reasonManager.findReasonById(classificationReasonId);
        }
    }

    /**
     * Check the node is suitable for classifying.
     *
     * @param content The node to be classified.
     */
    protected void validateContent(NodeRef content)
    {
        mandatory("content", content);

        if (!dictionaryService.isSubClass(nodeService.getType(content), ContentModel.TYPE_CONTENT))
        {
            throw new InvalidNode(content, "The supplied node is not a content node.");
        }
        if (nodeService.hasAspect(content, QuickShareModel.ASPECT_QSHARE))
        {
            throw new IllegalStateException("A shared content cannot be classified.");
        }
    }

    /**
     * Create a map suitable for storing against the aspect from the data transfer object.
     *
     * @param propertiesDTO The properties data transfer object.
     * @param content The node to be classified.
     * @return A map from {@link QName QNames} to values.
     */
    protected Map<QName, Serializable> createPropertiesMap(
                ClassificationAspectProperties propertiesDTO, NodeRef content)
    {
        final Map<QName, Serializable> propertiesMap = new HashMap<>();

        if (nodeService.getProperty(content, PROP_INITIAL_CLASSIFICATION) == null)
        {
            propertiesMap.put(PROP_INITIAL_CLASSIFICATION, propertiesDTO.getClassificationLevelId());
        }
        propertiesMap.put(PROP_CURRENT_CLASSIFICATION, propertiesDTO.getClassificationLevelId());
        propertiesMap.put(PROP_CLASSIFICATION_AGENCY, propertiesDTO.getClassificationAgency());
        propertiesMap.put(PROP_CLASSIFIED_BY, propertiesDTO.getClassifiedBy());
        propertiesMap.put(PROP_CLASSIFICATION_REASONS, new HashSet<>(propertiesDTO.getClassificationReasonIds()));
        propertiesMap.put(PROP_DOWNGRADE_DATE, propertiesDTO.getDowngradeDate());
        propertiesMap.put(PROP_DOWNGRADE_EVENT, propertiesDTO.getDowngradeEvent());
        propertiesMap.put(PROP_DOWNGRADE_INSTRUCTIONS, propertiesDTO.getDowngradeInstructions());
        propertiesMap.put(PROP_DECLASSIFICATION_DATE, propertiesDTO.getDeclassificationDate());
        propertiesMap.put(PROP_DECLASSIFICATION_EVENT, propertiesDTO.getDeclassificationEvent());
        propertiesMap.put(PROP_DECLASSIFICATION_EXEMPTIONS, new HashSet<>(propertiesDTO.getExemptionCategoryIds()));

        String lastReclassifyBy = propertiesDTO.getLastReclassifyBy();
        if (isNotBlank(lastReclassifyBy))
        {
            propertiesMap.put(PROP_LAST_RECLASSIFY_BY, lastReclassifyBy);
        }

        String lastReclassifyReason = propertiesDTO.getLastReclassifyReason();
        if (isNotBlank(lastReclassifyReason))
        {
            propertiesMap.put(PROP_LAST_RECLASSIFY_REASON, lastReclassifyReason);
        }

        return propertiesMap;
    }

    /**
     * @see org.alfresco.module.org_alfresco_module_rm.classification.ContentClassificationService#hasClearance(org.alfresco.service.cmr.repository.NodeRef)
     */
    @Override
    public boolean hasClearance(NodeRef nodeRef)
    {
    	boolean result = true;
    	if (nodeService.exists(nodeRef))
    	{
    		// Get the node's current classification
    		ClassificationLevel currentClassification = getCurrentClassification(nodeRef);
    		result = securityClearanceService.isCurrentUserClearedForClassification(currentClassification.getId());
    	}
    	return result;
    }

    /**
     * @see org.alfresco.module.org_alfresco_module_rm.classification.ContentClassificationService#isClassified(org.alfresco.service.cmr.repository.NodeRef)
     */
    @Override
    public boolean isClassified(NodeRef nodeRef)
    {
        mandatory("nodeRef", nodeRef);

        boolean isClassified = false;

        final String currentClassification;
        if (nodeService.hasAspect(nodeRef, ASPECT_CLASSIFIED))
        {
            currentClassification = (String) nodeService.getProperty(nodeRef, PROP_CURRENT_CLASSIFICATION);
            isClassified = currentClassification != null && ! UNCLASSIFIED_ID.equals(currentClassification);
        }
        else if (referredMetadataService.isReferringMetadata(nodeRef, ASPECT_CLASSIFIED))
        {
            currentClassification = (String) referredMetadataService.getReferredProperty(nodeRef, PROP_CURRENT_CLASSIFICATION);
            // This could be a null/missing property.

            isClassified = currentClassification != null && ! UNCLASSIFIED_ID.equals(currentClassification);
        }

        return isClassified;
    }

    /**
     * @see org.alfresco.module.org_alfresco_module_rm.classification.ContentClassificationService#editClassifiedContent(ClassificationAspectProperties, NodeRef)
     */
    @Override
    public void editClassifiedContent(ClassificationAspectProperties classificationAspectProperties, NodeRef content)
                    throws LevelIdNotFound, ReasonIdNotFound, InvalidNodeRefException, InvalidNode
    {
        classifyContent(classificationAspectProperties, content);
    }
}
