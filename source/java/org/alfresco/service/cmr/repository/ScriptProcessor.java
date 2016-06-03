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
