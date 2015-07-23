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

import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.alfresco.model.ContentModel;
import org.alfresco.model.QuickShareModel;
import org.alfresco.module.org_alfresco_module_rm.classification.ClassificationException.InvalidNode;
import org.alfresco.module.org_alfresco_module_rm.classification.ClassificationException.LevelIdNotFound;
import org.alfresco.module.org_alfresco_module_rm.classification.model.ClassifiedContentModel;
import org.alfresco.module.org_alfresco_module_rm.util.ServiceBaseImpl;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.security.authentication.AuthenticationUtil.RunAsWork;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;

/**
 * A service to handle the classification of content.
 *
 * @author tpage
 */
public class ContentClassificationServiceImpl extends ServiceBaseImpl
                                              implements ContentClassificationService, ClassifiedContentModel
{
    private ClassificationLevelManager levelManager;
    private ClassificationReasonManager reasonManager;
    private SecurityClearanceService securityClearanceService;
    private ClassificationServiceBootstrap classificationServiceBootstrap;

    public void setLevelManager(ClassificationLevelManager levelManager) { this.levelManager = levelManager; }
    public void setReasonManager(ClassificationReasonManager reasonManager) { this.reasonManager = reasonManager; }
    public void setSecurityClearanceService(SecurityClearanceService securityClearanceService) { this.securityClearanceService = securityClearanceService; }
    public void setClassificationServiceBootstrap(ClassificationServiceBootstrap classificationServiceBootstrap) { this.classificationServiceBootstrap = classificationServiceBootstrap; }

    public void init()
    {
        this.levelManager = classificationServiceBootstrap.getClassificationLevelManager();
        this.reasonManager = classificationServiceBootstrap.getClassificationReasonManager();
    }

    @Override
    public ClassificationLevel getCurrentClassification(final NodeRef nodeRef)
    {
        return AuthenticationUtil.runAsSystem(new RunAsWork<ClassificationLevel>()
        {
            public ClassificationLevel doWork() throws Exception
            {
                // by default everything is unclassified
                ClassificationLevel result = ClassificationLevelManager.UNCLASSIFIED;

                if (nodeService.hasAspect(nodeRef, ASPECT_CLASSIFIED))
                {
                    String classificationId = (String)nodeService.getProperty(nodeRef, PROP_CURRENT_CLASSIFICATION);
                    result = levelManager.findLevelById(classificationId);
                }

                return result;
            }
        });
    };

    @Override
    public void classifyContent(String classificationLevelId, String classifiedBy, String classificationAgency,
                                Set<String> classificationReasonIds, final NodeRef content)
    {
        checkNotBlank("classificationLevelId", classificationLevelId);
        checkNotBlank("classifiedBy", classifiedBy);
        mandatory("classificationReasonIds", classificationReasonIds);
        mandatory("content", content);

        if (!dictionaryService.isSubClass(nodeService.getType(content), ContentModel.TYPE_CONTENT))
        {
            throw new InvalidNode(content, "The supplied node is not a content node.");
        }
        if (nodeService.hasAspect(content, ASPECT_CLASSIFIED))
        {
            throw new UnsupportedOperationException(
                        "The content has already been classified. Reclassification is currently not supported.");
        }
        if (nodeService.hasAspect(content, QuickShareModel.ASPECT_QSHARE))
        {
            throw new IllegalStateException("A shared content cannot be classified.");
        }
        if (!securityClearanceService.isCurrentUserClearedForClassification(classificationLevelId))
        {
            throw new LevelIdNotFound(classificationLevelId);
        }

        final Map<QName, Serializable> properties = new HashMap<>();
        // Initial classification id
        if (nodeService.getProperty(content, PROP_INITIAL_CLASSIFICATION) == null)
        {
            properties.put(PROP_INITIAL_CLASSIFICATION, classificationLevelId);
        }

        // Current classification id
        properties.put(PROP_CURRENT_CLASSIFICATION, classificationLevelId);

        // Classification agency
        properties.put(PROP_CLASSIFICATION_AGENCY, classificationAgency);

        properties.put(PROP_CLASSIFIED_BY, classifiedBy);

        // Classification reason ids
        HashSet<String> classificationReasons = new HashSet<>();
        for (String classificationReasonId : classificationReasonIds)
        {
            // Check the classification reason id - an exception will be thrown if the id cannot be found
            reasonManager.findReasonById(classificationReasonId);
            classificationReasons.add(classificationReasonId);
        }
        properties.put(PROP_CLASSIFICATION_REASONS, classificationReasons);

        // Add aspect
        authenticationUtil.runAs(new RunAsWork<Void>()
        {
            public Void doWork()
            {
                nodeService.addAspect(content, ASPECT_CLASSIFIED, properties);
                return null;
            }
        }, authenticationUtil.getAdminUserName());
    }

    @Override
    public boolean hasClearance(NodeRef nodeRef)
    {
        // Get the node's current classification
        ClassificationLevel currentClassification = getCurrentClassification(nodeRef);
        return securityClearanceService.isCurrentUserClearedForClassification(currentClassification.getId());
    }

    /**
     * @see org.alfresco.module.org_alfresco_module_rm.classification.ContentClassificationService#isClassified(org.alfresco.service.cmr.repository.NodeRef)
     */
    @Override
    public boolean isClassified(NodeRef nodeRef)
    {
        mandatory("nodeRef", nodeRef);

        boolean isClassified = false;
        String currentClassification = (String) nodeService.getProperty(nodeRef, PROP_CURRENT_CLASSIFICATION);

        if (nodeService.hasAspect(nodeRef, ASPECT_CLASSIFIED) &&
                !(UNCLASSIFIED_ID).equals(currentClassification))
        {
            isClassified = true;
        }

        return isClassified;
    }
}
