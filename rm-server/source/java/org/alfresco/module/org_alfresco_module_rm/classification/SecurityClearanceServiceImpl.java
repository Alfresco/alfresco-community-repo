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

import static java.util.Arrays.asList;
import static org.alfresco.model.ContentModel.PROP_FIRSTNAME;
import static org.alfresco.model.ContentModel.PROP_LASTNAME;
import static org.alfresco.model.ContentModel.PROP_USERNAME;
import static org.alfresco.module.org_alfresco_module_rm.classification.model.ClassifiedContentModel.ASPECT_SECURITY_CLEARANCE;
import static org.alfresco.module.org_alfresco_module_rm.classification.model.ClassifiedContentModel.PROP_CLEARANCE_LEVEL;

import org.alfresco.module.org_alfresco_module_rm.util.AuthenticationUtil;
import org.alfresco.module.org_alfresco_module_rm.util.ServiceBaseImpl;
import org.alfresco.query.PagingRequest;
import org.alfresco.query.PagingResults;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.security.PersonService;
import org.alfresco.service.cmr.security.PersonService.PersonInfo;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.Pair;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

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

    public Pair<PersonInfo, ClassificationLevel> getUserSecurityClearance()
    {
        final String currentUser = authenticationUtil.getFullyAuthenticatedUser();
        Objects.requireNonNull(currentUser, "Fully authenticated user is null, which is not allowed.");

        return getUserSecurityClearance(currentUser);
    }

    private Pair<PersonInfo, ClassificationLevel> getUserSecurityClearance(final String userName)
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

        return new Pair<>(personInfo, classificationLevel);
    }

    public PagingResults<Pair<PersonInfo, ClassificationLevel>> getUsersSecurityClearance(String userNameFragment,
                                                                                          boolean sortAscending,
                                                                                          PagingRequest req)
    {
        final List<QName> filterProps = asList(PROP_USERNAME, PROP_FIRSTNAME, PROP_LASTNAME);
        final List<Pair<QName, Boolean>> sortProps = asList(new Pair<>(PROP_USERNAME, sortAscending));

        final PagingResults<PersonInfo> p = personService.getPeople(userNameFragment, filterProps, sortProps, req);

        return new PagingResults<Pair<PersonInfo, ClassificationLevel>>()
        {
            @Override public List<Pair<PersonInfo, ClassificationLevel>> getPage()
            {
                List<Pair<PersonInfo, ClassificationLevel>> pcPage= new ArrayList<>(p.getPage().size());
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
}
