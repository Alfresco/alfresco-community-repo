/*
 * #%L
 * Alfresco Repository
 * %%
 * Copyright (C) 2005 - 2016 Alfresco Software Limited
 * %%
 * This file is part of the Alfresco software. 
 * If the software was purchased under a paid Alfresco license, the terms of 
 * the paid license agreement will prevail.  Otherwise, the software is 
 * provided under the following open source license terms:
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
 * #L%
 */
package org.alfresco.repo.security.authentication.ldap;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.management.openmbean.CompositeData;
import javax.management.openmbean.CompositeDataSupport;
import javax.management.openmbean.CompositeType;
import javax.management.openmbean.OpenDataException;
import javax.management.openmbean.OpenType;
import javax.management.openmbean.SimpleType;
import javax.management.openmbean.TabularData;
import javax.management.openmbean.TabularDataSupport;
import javax.management.openmbean.TabularType;

import org.alfresco.repo.security.authentication.AuthenticationException;
import org.alfresco.repo.security.authentication.AuthenticationStep;
import org.alfresco.repo.security.sync.ChainingUserRegistrySynchronizerStatus;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Monitoring methods and properties to be exposed via the
 * JMX admin console.
 */

public class Monitor
{
    LDAPAuthenticationComponentImpl component;
    ChainingUserRegistrySynchronizerStatus syncMonitor;
    String id;
    
    private static Log logger = LogFactory.getLog(Monitor.class);
    
    public void setLDAPAuthenticationComponent(LDAPAuthenticationComponentImpl component)
    {
        this.component = component;
    }
    
    public void setChainingUserRegistrySynchronizerStatus(ChainingUserRegistrySynchronizerStatus syncStatus)
    {
        this.syncMonitor = syncStatus;
    }
       
    /**
     * test authenticate
     * 
     * @param userName String
     * @param credentials String
     * @throws AuthenticationException
     */
    public CompositeData testAuthenticate(String userName, String credentials)
    {
        String stepKeys[] = {"id", "stepMessage", "success"};
        String stepDescriptions[] = {"id", "stepMessage", "success"};
        OpenType<?> stepTypes[] = {SimpleType.INTEGER, SimpleType.STRING, SimpleType.BOOLEAN };
     
        try
        {
            String[] key = {"id"};
            CompositeType sType = new CompositeType("Authentication Step", "Step", stepKeys, stepDescriptions, stepTypes);
            TabularType tType = new TabularType("Diagnostic", "Authentication Steps", sType, key);
            TabularData table = new TabularDataSupport(tType);
     
            String attributeKeys[] = {"authenticationMessage", "success", "diagnostic"};
            String attributeDescriptions[] = {"authenticationMessage", "success", "diagnostic"};
            OpenType<?> attributeTypes[] = {SimpleType.STRING, SimpleType.BOOLEAN, tType};  
            try
            {
                component.authenticate(userName, credentials.toCharArray());
            
                CompositeType cType = new CompositeType("Authentication Result", "Result Success", attributeKeys, attributeDescriptions, attributeTypes);
                Map<String, Object> value = new HashMap<String, Object>();
                value.put("authenticationMessage", "Test Passed");
                value.put("success", true);
                value.put("diagnostic", table);
                CompositeDataSupport row = new CompositeDataSupport(cType, value);
                return row;
            }
            catch (AuthenticationException ae)
            {                      
                CompositeType cType = new CompositeType("Authentication Result", "Result Failed", attributeKeys, attributeDescriptions, attributeTypes);
                Map<String, Object> value = new HashMap<String, Object>();
                value.put("authenticationMessage", ae.getMessage());
                value.put("success", false);

                if(ae.getDiagnostic() != null)
                {
                    int i = 0;
                    for(AuthenticationStep step : ae.getDiagnostic().getSteps())
                    {
                        Map<String, Object> x = new HashMap<String, Object>();
                        x.put("id", i++);
                        x.put("stepMessage", step.getMessage());
                        x.put("success", step.isSuccess());
                        CompositeDataSupport row = new CompositeDataSupport(sType, x);
                        table.put(row);
                        
                    }
                }
                
                value.put("diagnostic", table);
                
                CompositeDataSupport row = new CompositeDataSupport(cType, value);
            
                return row;
            }

        }
        catch (OpenDataException oe)
        {
            logger.error("", oe);
            return null;
        }
    }
    
    public int getNumberFailedAuthentications()
    {
        return component.getNumberFailedAuthentications();
    }
    
    public int getNumberSuccessfulAuthentications()
    {
        return component.getNumberSuccessfulAuthentications();
    }
    
    public String getSynchronizationStatus()
    {
        return syncMonitor.getSynchronizationStatus(getZone(component.getId()));
    }
    
//    public Date getSynchronizationLastUserUpdateTime()
//    {
//    	// TODO This method fails due to a unable to find transaction error - Comment out for now
//        return syncMonitor.getSynchronizationLastUserUpdateTime(getZone(component.getId()));
//    }
//    
//    public Date getSynchronizationLastGroupUpdateTime()
//    {
//	    // TODO This method fails due to a unable to find transaction error - Comment out for now
//        return syncMonitor.getSynchronizationLastGroupUpdateTime(getZone(component.getId()));
//    }
//    
    public String getSynchronizationLastError()
    {
        return syncMonitor.getSynchronizationLastError(getZone(component.getId()));
    }
    
    public String getSynchronizationSummary()
    {
        return syncMonitor.getSynchronizationSummary(getZone(component.getId()));
    }
    
    public String getLastRunOnServer()
    {
        return syncMonitor.getLastRunOnServer();
    }
    
    public Date getSyncStartTime()
    {
    	return syncMonitor.getSyncStartTime();
    }
    
    public Date getSyncEndTime()
    {
    	return syncMonitor.getSyncEndTime();
    }
    
    /**
     * Get the zone for an ldap authentication component.  e.g given [managed,ldap1] return ldap1
     * @param id ths id of the subsystem
     * @return the zone
     */
    private String getZone(String id)
    {

        String s = id.replace("[", "");
        String s2 = s.replace("]", "");
        String[] ids = s2.split(",");
        
        String x = ids[ids.length -1].trim();
        
        return x;

    }    
}
