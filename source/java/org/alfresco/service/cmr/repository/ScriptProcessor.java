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
package org.alfresco.service.cmr.repository;

import java.util.Map;

import org.alfresco.processor.Processor;
import org.alfresco.service.namespace.QName;

/**
 * Script processor interface
 * 
 * @author Roy Wetherall
 */
public interface ScriptProcessor extends Processor
{    
    /**
     * Execute script
     * 
     * @param location  the location of the script 
     * @param model     context model
     * @return Object   the result of the script
     */
    public Object execute(ScriptLocation location, Map<String, Object> model);
    
    /**
     * Execute script
     * 
     * @param nodeRef       the script node reference
     * @param contentProp   the content property of the script
     * @param model         the context model
     * @return Object       the result of the script
     */
    public Object execute(NodeRef nodeRef, QName contentProp, Map<String, Object> model);
    
    /** 
     * Execute script
     * 
     * @param location  the classpath string locating the script
     * @param model     the context model
     * @return Object   the result of the script
     */
    public Object execute(String location, Map<String, Object> model);
    
    /**
     * Execute script string
     * 
     * @param script    the script string
     * @param model     the context model
     * @return Obejct   the result of the script 
     */
    public Object executeString(String script, Map<String, Object> model);
    
    /**
     * Reset the processor - such as clearing any internal caches etc.
     */
    public void reset();
}
