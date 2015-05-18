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

import static org.alfresco.module.org_alfresco_module_rm.util.RMParameterCheck.checkNotBlank;

import java.util.Collections;
import java.util.List;

import org.alfresco.module.org_alfresco_module_rm.classification.ClassificationServiceException.LevelIdNotFound;
import org.alfresco.module.org_alfresco_module_rm.classification.ClassificationServiceException.ReasonIdNotFound;
import org.alfresco.module.org_alfresco_module_rm.classification.model.ClassifiedContentModel;
import org.alfresco.module.org_alfresco_module_rm.util.ServiceBaseImpl;
import org.alfresco.service.cmr.repository.NodeService;

/**
 * @author Neil Mc Erlean
 * @since 3.0
 */
public class ClassificationServiceImpl extends ServiceBaseImpl
                                       implements ClassificationService, ClassifiedContentModel
{
    /** The classification levels currently configured in this server. */
    private ClassificationLevelManager levelManager;
    /** The classification reasons currently configured in this server. */
    private ClassificationReasonManager reasonManager;
    private ClassificationServiceBootstrap classificationServiceBootstrap;

    public void setNodeService(NodeService service) { this.nodeService = service; }
    public void setClassificationServiceBootstrap(ClassificationServiceBootstrap classificationServiceBootstrap)
    {
        this.classificationServiceBootstrap = classificationServiceBootstrap;
    }

    /** Store the references to the classification level and reason managers in this class. */
    public void init()
    {
        levelManager = classificationServiceBootstrap.getClassificationLevelManager();
        reasonManager = classificationServiceBootstrap.getClassificationReasonManager();
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

    @Override public ClassificationLevel getUnclassifiedClassificationLevel()
    {
        return ClassificationLevelManager.UNCLASSIFIED;
    }

    /**
     * @see org.alfresco.module.org_alfresco_module_rm.classification.ClassificationService#getClassificationLevelById(java.lang.String)
     */
    @Override
    public ClassificationLevel getClassificationLevelById(String classificationLevelId) throws LevelIdNotFound
    {
        checkNotBlank("classificationLevelId", classificationLevelId);
        return levelManager.findLevelById(classificationLevelId);
    }

    /**
     * @see org.alfresco.module.org_alfresco_module_rm.classification.ClassificationService#getClassificationReasonById(java.lang.String)
     */
    @Override
    public ClassificationReason getClassificationReasonById(String classificationReasonId) throws ReasonIdNotFound
    {
        checkNotBlank("classificationReasonId", classificationReasonId);
        return reasonManager.findReasonById(classificationReasonId);
    }
}
