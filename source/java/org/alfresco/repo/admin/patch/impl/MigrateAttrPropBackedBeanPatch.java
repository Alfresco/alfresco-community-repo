/*
 * Copyright (C) 2005-2010 Alfresco Software Limited.
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
package org.alfresco.repo.admin.patch.impl;

import java.io.Serializable;
import java.net.URLDecoder;
import java.util.HashMap;
import java.util.Map;

import javax.management.Attribute;
import javax.management.AttributeList;
import javax.management.MBeanServerConnection;
import javax.management.ObjectName;

import org.alfresco.repo.admin.patch.AbstractPatch;
import org.alfresco.repo.domain.patch.PatchDAO;
import org.alfresco.service.cmr.attributes.AttributeService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.ibatis.session.ResultContext;
import org.apache.ibatis.session.ResultHandler;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.extensions.surf.util.I18NUtil;

/**
 * Migrate Property-Backed Bean attributes (from 'alf_*attribute*' to 'alf_prop_*')
 * 
 * @author janv
 * @since 3.4
 */
public class MigrateAttrPropBackedBeanPatch extends AbstractPatch implements ApplicationContextAware
{
    private Log logger = LogFactory.getLog(this.getClass());
    
    private static final String ROOT_KEY_PBB = ".PropertyBackedBeans"; // see also PropertyBackBeanAdapter.ROOT_ATTRIBUTE_PATH
    
    private static final String MSG_SUCCESS = "patch.migrateAttrPropBackedBeans.result";
    
    private AttributeService attributeService;
    private PatchDAO patchDAO;
    private MBeanServerConnection mbeanServer;
    
    public void setAttributeService(AttributeService attributeService)
    {
        this.attributeService = attributeService;
    }
    
    public void setPatchDAO(PatchDAO patchDAO)
    {
        this.patchDAO = patchDAO;
    }
    
    /* (non-Javadoc)
     * @see org.springframework.context.ApplicationContextAware#setApplicationContext(org.springframework.context.ApplicationContext)
     */
    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException
    {
        // Optional dependency - may not exist in community builds
        this.mbeanServer = (MBeanServerConnection) applicationContext.getBean("alfrescoMBeanServer");
    }

    @Override
    protected String applyInternal() throws Exception
    {
        long startTime = System.currentTimeMillis();
        
        PBBesultHandler handler = new PBBesultHandler();
        patchDAO.migrateOldAttrPropertyBackedBeans(handler);
        handler.setComponent(handler.currentComponentName, handler.attributeMap); // set last component attribute (if any)
        
        if (handler.total > 0)
        {
            logger.info("Processed "+handler.total+" Property-Backed Component attrs ("+handler.totalProps+" props) in "+(System.currentTimeMillis()-startTime)/1000+" secs");
        }
        
        // build the result message
        String msg = I18NUtil.getMessage(MSG_SUCCESS, handler.total, handler.totalProps);
        // done
        return msg;
    }
    
    private class PBBesultHandler implements ResultHandler
    {
        private int total = 0;
        private int totalProps = 0;
        
        private Map<String, String> attributeMap = new HashMap<String, String>(10);
        private String currentComponentName = "";
        
        private PBBesultHandler()
        {
        }
        @SuppressWarnings("unchecked")
        public void handleResult(ResultContext context)
        {
            Map<String, Object> result = (Map<String, Object>)context.getResultObject();
            
            String componentName = (String)result.get("componentName");
            String propName = (String)result.get("propName");
            String propValue = (String)result.get("propValue");
            
            if (! currentComponentName.equals(componentName))
            {
                // write out previous component - note: does nothing on 1st call
                setComponent(currentComponentName, attributeMap);
                
                currentComponentName = componentName;
                attributeMap.clear();
            }
            
            attributeMap.put(propName, propValue);
            
            totalProps++;
            
            if (logger.isTraceEnabled())
            {
                logger.trace("Read PBB [componentName="+componentName+", propName="+propName+", propValue="+propValue+"]");
            }
        }
        
        // note: args should not be null
        public void setComponent(String componentName, Map<String, String> attributeMap)
        {
            if (componentName.equals("") || attributeMap.size() == 0)
            {
                return;
            }
            
            boolean done = false;
            try
            {
                // Go through the subsystem MBean interface in case the subsystem is already live and the cluster needs
                // to be resynced

                // Decode the bean ID to a hierarchical object name
                String[] components = componentName.split("\\$");
                StringBuilder nameBuff = new StringBuilder(200).append("Alfresco:Type=Configuration,Category=").append(
                        URLDecoder.decode(components[0], "UTF-8"));
                for (int i = 1; i < components.length; i++)
                {
                    nameBuff.append(",id").append(i).append('=').append(URLDecoder.decode(components[i], "UTF-8"));
                }

                ObjectName name = new ObjectName(nameBuff.toString());
                if (mbeanServer != null && mbeanServer.isRegistered(name))
                {
                    AttributeList attributeList = new AttributeList();
                    for (Map.Entry<String, String> entry : attributeMap.entrySet())
                    {
                        attributeList.add(new Attribute(entry.getKey(), entry.getValue()));
                    }
                    mbeanServer.setAttributes(name, attributeList);
                    // We've successfully persisted the attributes. Job done
                    done = true;
                }
            }
            catch (Exception e)
            {
                if (logger.isWarnEnabled())
                {
                    logger
                            .warn(
                                    "Exception migrating attributes of subsystem "
                                            + componentName
                                            + ". Falling back to repository-only operation. Subsystem may remain out of sync until reboot.",
                                    e);
                }
            }
            
            // Fallback: perhaps the subsystem isn't up yet and hasn't exported its bean. Or perhaps an error occurred
            // above. Let's persist the new property anyway.
            if (!done)
            {
                attributeService.setAttribute((Serializable) attributeMap, ROOT_KEY_PBB, componentName);
            }

            if (logger.isTraceEnabled())
            {
                logger.trace("Set PBB component attr [name="+componentName+", attributeMap="+attributeMap+"]");
            }
            
            total++;
            
            if (logger.isDebugEnabled() && (total == 0 || (total % 1000 == 0) ))
            {
                logger.debug("   Handled " + total + " Chaining URS attrs");
            }
        }
    }
}
