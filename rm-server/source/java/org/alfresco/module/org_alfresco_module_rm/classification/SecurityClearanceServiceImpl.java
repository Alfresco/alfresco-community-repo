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

import static org.alfresco.module.org_alfresco_module_rm.classification.model.ClassifiedContentModel.ASPECT_SECURITY_CLEARANCE;
import static org.alfresco.module.org_alfresco_module_rm.classification.model.ClassifiedContentModel.PROP_CLEARANCE_LEVEL;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.google.common.collect.ImmutableList;
import org.alfresco.module.org_alfresco_module_rm.classification.ClassificationServiceException.LevelIdNotFound;
import org.alfresco.module.org_alfresco_module_rm.util.ServiceBaseImpl;
import org.alfresco.query.PagingRequest;
import org.alfresco.query.PagingResults;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.security.PersonService;
import org.alfresco.service.cmr.security.PersonService.PersonInfo;
import org.alfresco.util.Pair;
import org.alfresco.util.ParameterCheck;

/**
 * @author Neil Mc Erlean
 * @author David Webster
 * @since 3.0
 */
public class SecurityClearanceServiceImpl extends ServiceBaseImpl implements SecurityClearanceService
{
    /** The clearance levels currently configured in this server. */
    private ClearanceLevelManager clearanceManager;
    /** The object containing the {@link ClassificationLevel}s in the system. */
    private ClassificationLevelManager classificationLevelManager;
    private PersonService personService;
    private ClassificationServiceBootstrap classificationServiceBootstrap;

    public void setClearanceManager(ClearanceLevelManager clearanceManager) { this.clearanceManager = clearanceManager; }
    public void setClassificationLevelManager(ClassificationLevelManager classificationLevelManager) { this.classificationLevelManager = classificationLevelManager; }
    public void setPersonService(PersonService service) { this.personService = service; }
    public void setClassificationServiceBootstrap(ClassificationServiceBootstrap classificationServiceBootstrap) { this.classificationServiceBootstrap = classificationServiceBootstrap; }

    /** Store the references to the classification and clearance level managers in this class. */
    public void init()
    {
        this.classificationLevelManager = classificationServiceBootstrap.getClassificationLevelManager();
        this.clearanceManager = classificationServiceBootstrap.getClearanceLevelManager();
    }

    @Override
    public SecurityClearance getUserSecurityClearance()
    {
        final String currentUser = authenticationUtil.getFullyAuthenticatedUser();
        ParameterCheck.mandatoryString("currentUser", currentUser);

        return getUserSecurityClearance(currentUser);
    }

    /**
     * Gets the user's security clearance.
     *
     * @param  userName user name
     * @return {@link SecurityClearance} provides information about the user and their clearance level
     */
    private SecurityClearance getUserSecurityClearance(final String userName)
    {
        if (authenticationUtil.isRunAsUserTheSystemUser())
        {
            return new SecurityClearance(null, clearanceManager.getMostSecureLevel());
        }

        final NodeRef    personNode = personService.getPerson(userName, false);
        final PersonInfo personInfo = personService.getPerson(personNode);

        ClearanceLevel clearanceLevel = ClearanceLevelManager.NO_CLEARANCE;
        if (nodeService.hasAspect(personNode, ASPECT_SECURITY_CLEARANCE))
        {
            final String clearanceLevelId = (String)nodeService.getProperty(personNode, PROP_CLEARANCE_LEVEL);
            clearanceLevel = (clearanceLevelId == null ? ClearanceLevelManager.NO_CLEARANCE
                                                       : clearanceManager.findLevelByClassificationLevelId(clearanceLevelId));
        }

        return new SecurityClearance(personInfo, clearanceLevel);
    }

    @Override
    public PagingResults<SecurityClearance> getUsersSecurityClearance(UserQueryParams queryParams)
    {
        final PagingRequest pagingRequest = new PagingRequest(queryParams.getSkipCount(),
                                                              queryParams.getMaxItems());
        // We want an accurate count of how many users there are in the system (in this query).
        // Else paging in the UI won't work properly.
        pagingRequest.setRequestTotalCountMax(Integer.MAX_VALUE);

        final PagingResults<PersonInfo> p = personService.getPeople(queryParams.getSearchTerm(),
                                                                    queryParams.getFilterProps(),
                                                                    queryParams.getSortProps(),
                                                                    pagingRequest);

        return new PagingResults<SecurityClearance>()
        {
            @Override public List<SecurityClearance> getPage()
            {
                List<SecurityClearance> pcPage= new ArrayList<>(p.getPage().size());
                for (PersonInfo pi : p.getPage())
                {
                    pcPage.add(getUserSecurityClearance(pi.getUserName()));
                }
                return pcPage;
            }

            @Override public boolean                hasMoreItems()        { return p.hasMoreItems(); }
            @Override public Pair<Integer, Integer> getTotalResultCount() { return p.getTotalResultCount(); }
            @Override public String                 getQueryExecutionId() { return p.getQueryExecutionId(); }
        };
    }

    /**
     * Check if a classification can be accessed by a user with a given clearance.
     *
     * @param clearance The clearance of the user.
     * @param classificationId The classification level to look for.
     * @return {@code true} if the user can access the classification level.
     */
    protected boolean isClearedForClassification(SecurityClearance clearance, String classificationId)
    {
        ImmutableList<ClassificationLevel> classificationLevels = classificationLevelManager.getClassificationLevels();

        String clearanceId = clearance.getClearanceLevel().getHighestClassificationLevel().getId();
        for (ClassificationLevel classificationLevel : classificationLevels)
        {
            if (classificationLevel.getId().equals(clearanceId))
            {
                return true;
            }
            else if (classificationLevel.getId().equals(classificationId))
            {
                return false;
            }
        }
        // Neither the clearance id nor the classification id were found - something's gone wrong.
        throw new LevelIdNotFound(classificationId);
    }

    @Override
    public SecurityClearance setUserSecurityClearance(String userName, String clearanceId)
    {
        ParameterCheck.mandatoryString("userName", userName);
        ParameterCheck.mandatoryString("clearanceId", clearanceId);

        final NodeRef personNode = personService.getPerson(userName, false);

        // Check the current user has clearance to see the specified level.
        SecurityClearance userSecurityClearance = getUserSecurityClearance();
        if (!isClearedForClassification(userSecurityClearance, clearanceId))
        {
            throw new LevelIdNotFound(clearanceId);
        }

        nodeService.setProperty(personNode, PROP_CLEARANCE_LEVEL, clearanceId);

        return getUserSecurityClearance(userName);
    }

    @Override
    public List<ClearanceLevel> getClearanceLevels()
    {
        if (clearanceManager == null)
        {
            return Collections.emptyList();
        }
        // FIXME Currently assume user has highest security clearance, this should be fixed as part of RM-2112.
        ClearanceLevel usersLevel = clearanceManager.getMostSecureLevel();

        return restrictList(clearanceManager.getClearanceLevels(), usersLevel);
    }

    /**
     * Create a list containing all clearance levels up to and including the supplied level.
     *
     * @param allLevels   The list of all the clearance levels starting with the highest security.
     * @param targetLevel The highest security clearance level that should be returned. If this is not found then
     *                    an empty list will be returned.
     * @return an immutable list of the levels that a user at the target level can see.
     */
    private List<ClearanceLevel> restrictList(List<ClearanceLevel> allLevels, ClearanceLevel targetLevel)
    {
        int targetIndex = allLevels.indexOf(targetLevel);
        if (targetIndex == -1) { return Collections.emptyList(); }
        List<ClearanceLevel> subList = allLevels.subList(targetIndex, allLevels.size());
        return Collections.unmodifiableList(subList);
    }
}
