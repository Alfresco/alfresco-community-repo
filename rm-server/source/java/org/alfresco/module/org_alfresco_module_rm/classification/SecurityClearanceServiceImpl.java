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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.google.common.collect.ImmutableMap;
import org.alfresco.module.org_alfresco_module_rm.util.ServiceBaseImpl;
import org.alfresco.query.PagingRequest;
import org.alfresco.query.PagingResults;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.security.PersonService;
import org.alfresco.service.cmr.security.PersonService.PersonInfo;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.Pair;
import org.alfresco.util.ParameterCheck;

/**
 * @author Neil Mc Erlean
 * @since 3.0
 */
public class SecurityClearanceServiceImpl extends ServiceBaseImpl implements SecurityClearanceService
{
    private ClassificationService classificationService;
    private PersonService         personService;

    public void setClassificationService(ClassificationService service) { this.classificationService = service; }
    public void setPersonService        (PersonService service)         { this.personService = service; }

    @Override
    public SecurityClearance getUserSecurityClearance()
    {
        final String currentUser = authenticationUtil.getFullyAuthenticatedUser();
        ParameterCheck.mandatoryString("currentUser", currentUser);

        return getUserSecurityClearance(currentUser);
    }

    private SecurityClearance getUserSecurityClearance(final String userName)
    {
        final NodeRef    personNode = personService.getPerson(userName, false);
        final PersonInfo personInfo = personService.getPerson(personNode);

        final ClassificationLevel classificationLevel;

        if (nodeService.hasAspect(personNode, ASPECT_SECURITY_CLEARANCE))
        {
            final String clearanceLevel = (String)nodeService.getProperty(personNode, PROP_CLEARANCE_LEVEL);
            // TODO Should we fall back to a default here or give an error?
            classificationLevel = clearanceLevel == null ? classificationService.getDefaultClassificationLevel() :
                                                           classificationService.getClassificationLevelById(clearanceLevel);
        }
        else { classificationLevel = classificationService.getDefaultClassificationLevel(); }

        return new SecurityClearance(personInfo, classificationLevel);
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

    @Override
    public void setUserSecurityClearance(String userName, String clearanceId)
    {
        ParameterCheck.mandatoryString("userName", userName);
        ParameterCheck.mandatoryString("clearanceId", clearanceId);

        final NodeRef personNode = personService.getPerson(userName, false);
        // This is just used to check the current user has clearance to see the specified level; it will throw a
        // LevelIdNotFound exception if not.
        classificationService.getClassificationLevelById(clearanceId);

        if (nodeService.hasAspect(personNode, ASPECT_SECURITY_CLEARANCE))
        {
            nodeService.setProperty(personNode, PROP_CLEARANCE_LEVEL, clearanceId);
        }
        else
        {
            Map<QName, Serializable> properties = ImmutableMap.of(PROP_CLEARANCE_LEVEL, (Serializable) clearanceId);
            nodeService.addAspect(personNode, ASPECT_SECURITY_CLEARANCE, properties);
        }
    }
}
