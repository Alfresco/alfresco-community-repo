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
import java.util.List;

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
 * @since 3.0
 */
public class SecurityClearanceServiceImpl extends ServiceBaseImpl implements SecurityClearanceService
{
    /** The clearance levels currently configured in this server. */
    private ClearanceLevelManager clearanceManager;

    private ClassificationService classificationService;
    private PersonService         personService;

    public void setClearanceManager(ClearanceLevelManager clearanceManager) { this.clearanceManager = clearanceManager; }
    public void setClassificationService(ClassificationService service) { this.classificationService = service; }
    public void setPersonService(PersonService service) { this.personService = service; }

    /**
     * Initialise and create a {@link ClearanceLevelManager}. This assumes that the {@link ClassificationService} has
     * already been initialised.
     */
    void initialise()
    {
        ArrayList<ClearanceLevel> clearanceLevels = new ArrayList<ClearanceLevel>();
        List<ClassificationLevel> classificationLevels = classificationService.getClassificationLevels();
        for (ClassificationLevel classificationLevel : classificationLevels)
        {
        	if (!ClassificationLevelManager.UNCLASSIFIED.equals(classificationLevel))
        	{
        		clearanceLevels.add(new ClearanceLevel(classificationLevel, classificationLevel.getDisplayLabelKey()));
        	}
        }
        this.clearanceManager = new ClearanceLevelManager(clearanceLevels);
    }

    /** Get the clearance manager (for use in unit testing). */
    protected ClearanceLevelManager getClearanceManager() { return clearanceManager; }

    /**
     * @see org.alfresco.module.org_alfresco_module_rm.classification.SecurityClearanceService#hasClearance(org.alfresco.service.cmr.repository.NodeRef)
     */
    @Override
    public boolean hasClearance(NodeRef nodeRef) 
    {
    	boolean result = false;
    	
    	// get the nodes current classification
    	ClassificationLevel currentClassification = classificationService.getCurrentClassification(nodeRef);
    	if (ClassificationLevelManager.UNCLASSIFIED.equals(currentClassification))
    	{
    		// since the node is not classified user has clearance
    		result = true;
    	}
    	else
    	{
    		// get the users security clearance
    		SecurityClearance securityClearance = getUserSecurityClearance();
    		if (!ClearanceLevelManager.NO_CLEARANCE.equals(securityClearance.getClearanceLevel()))
    		{
    			// get the users highest classification clearance
    			ClassificationLevel highestClassification = securityClearance.getClearanceLevel().getHighestClassificationLevel();
    			
    			// if classification is less than or equal to highest classification then user has clearance
    			List<ClassificationLevel> allClassificationLevels = classificationService.getClassificationLevels();
    			int highestIndex = allClassificationLevels.indexOf(highestClassification);
    			int currentIndex = allClassificationLevels.indexOf(currentClassification);
    			
    			if (highestIndex <= currentIndex)
    			{
    				// user has clearance
    				result = true;
    			}	
    		}
    	}
    	
    	return result;
    }
    
    @Override
    public SecurityClearance getUserSecurityClearance()
    {
        final String currentUser = authenticationUtil.getFullyAuthenticatedUser();
        ParameterCheck.mandatoryString("currentUser", currentUser);

        return getUserSecurityClearance(currentUser);
    }

    /**
     * Gets the users security clearnace.
     * 
     * @param  userName						user name
     * @return {@link SecurityClearance}	provides information about the user and their clearance level
     */
    private SecurityClearance getUserSecurityClearance(final String userName)
    {
        final NodeRef    personNode = personService.getPerson(userName, false);
        final PersonInfo personInfo = personService.getPerson(personNode);

        final ClassificationLevel classificationLevel;

        if (nodeService.hasAspect(personNode, ASPECT_SECURITY_CLEARANCE))
        {
            final String clearanceLevelValue = (String)nodeService.getProperty(personNode, PROP_CLEARANCE_LEVEL);

            classificationLevel = clearanceLevelValue == null ? classificationService.getUnclassifiedClassificationLevel() :
                                                           		classificationService.getClassificationLevelById(clearanceLevelValue);
        }
        else 
        { 
        	classificationLevel = classificationService.getUnclassifiedClassificationLevel(); 
        }

        ClearanceLevel clearanceLevel = clearanceManager.findLevelByClassificationLevelId(classificationLevel.getId());
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

    @Override
    public SecurityClearance setUserSecurityClearance(String userName, String clearanceId)
    {
        ParameterCheck.mandatoryString("userName", userName);
        ParameterCheck.mandatoryString("clearanceId", clearanceId);

        final NodeRef personNode = personService.getPerson(userName, false);
        // This is just used to check the current user has clearance to see the specified level; it will throw a
        // LevelIdNotFound exception if not.
        classificationService.getClassificationLevelById(clearanceId);

        nodeService.setProperty(personNode, PROP_CLEARANCE_LEVEL, clearanceId);

        return getUserSecurityClearance(userName);
    }
}
