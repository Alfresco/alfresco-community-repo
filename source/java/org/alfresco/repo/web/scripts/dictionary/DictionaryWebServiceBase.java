/*
 * Copyright (C) 2005-2011 Alfresco Software Limited.
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
package org.alfresco.repo.web.scripts.dictionary;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.namespace.InvalidQNameException;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.springframework.extensions.webscripts.DeclarativeWebScript;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptException;

/**
 * Base class for Dictionary web scripts
 * 
 * @author Saravanan Sellathurai
 */
public abstract class DictionaryWebServiceBase extends DeclarativeWebScript
{
    private static final String NAME_DELIMITER = "_";
    
    private Map<String, String> prefixesAndUrlsMap;
    private Map<String, String> urlsAndPrefixesMap;
    
    /** Namespace service */
    protected NamespaceService namespaceService;
    
    /** Dictionary service */
    protected DictionaryService dictionaryservice;
    
    private static final String CLASS_FILTER_OPTION_TYPE1 = "all";
    private static final String CLASS_FILTER_OPTION_TYPE2 = "aspect";
    private static final String CLASS_FILTER_OPTION_TYPE3 = "type";

    private static final String ASSOCIATION_FILTER_OPTION_TYPE1 = "all";
    private static final String ASSOCIATION_FILTER_OPTION_TYPE2 = "general";
    private static final String ASSOCIATION_FILTER_OPTION_TYPE3 = "child";
    
    
    /**
     * Set the namespaceService property.
     * 
     * @param namespaceService The namespace service instance to set
     */
    public void setNamespaceService(NamespaceService namespaceservice)
    {
        this.namespaceService = namespaceservice;
    }
    
    /**
     * Set the dictionaryService property.
     * 
     * @param dictionaryService The dictionary service instance to set
     */
    public void setDictionaryService(DictionaryService dictionaryService)
    {
        this.dictionaryservice = dictionaryService; 
    }
    
    /**
     * Init method.
     */
    public void init()
    {
    	Collection<String> prefixes = this.namespaceService.getPrefixes();
        this.prefixesAndUrlsMap = new HashMap<String, String>(prefixes.size());
        this.urlsAndPrefixesMap = new HashMap<String, String>(prefixes.size());
        for (String prefix : prefixes)
        {
        	String url = this.namespaceService.getNamespaceURI(prefix);
        	this.prefixesAndUrlsMap.put(prefix, url);
            this.urlsAndPrefixesMap.put(url, prefix);           
        }
	 }
    
    protected QName createClassQName(String className)
    {
        QName result = null;
        int index = className.indexOf(NAME_DELIMITER);
        if (index > 0)
        {
            String prefix = className.substring(0, index);
            String shortName = className.substring(index+1);
            String url = namespaceService.getNamespaceURI(prefix);
            if (url != null && url.length() != 0 && 
                shortName != null && shortName.length() != 0)
            {
                QName classQName = QName.createQName(url, shortName);
                if (dictionaryservice.getClass(classQName) != null)
                {
                    result = classQName;
                }
            }
        }
        
        return result;
    }
    
    
    /**
     * @param qname
     * @return the namespaceuri from a qname 
     */
    public String getNamespaceURIfromQname(QName qname)
    {
    	return qname.getNamespaceURI();
    }
	
    /**
     * @param className     the class name as cm_person
     * @return String       the full name in the following format {namespaceuri}shorname
     */
    public String getFullNamespaceURI(String classname)
    {
       	try
       	{
			String result = null;
		   	String prefix = this.getPrefix(classname);
			String url = this.prefixesAndUrlsMap.get(prefix);
			String name = this.getShortName(classname);
			result = "{" + url + "}"+ name;
			return result;
       	}
       	catch (Exception e)
       	{
       		throw new WebScriptException(Status.STATUS_NOT_FOUND, "The exact classname - " + classname + "  parameter has not been provided in the URL");
       	}
    }
    
    /**
     * @param classname - checks whether the classname is valid , gets the classname as input e.g cm_person
     * @return true - if the class is valid , false - if the class is invalid
     */
    public boolean isValidClassname(String classname) 
    {
    	QName qname = null;
    	try
    	{
    		qname = QName.createQName(this.getFullNamespaceURI(classname));
    		return (dictionaryservice.getClass(qname) != null);
    	}
    	catch (InvalidQNameException e)
    	{
    		//just ignore
    	}
    	return false;
    }
    
    /**
     * @param namespaceprefix - gets a valid namespaceprefix as input
     * @return modelname from namespaceprefix - returns null if invalid namespaceprefix is given
     */
    public String getPrefixFromModelName(String modelname)
    {
    	String namespaceprefix = null;
		for(QName qnameObj:this.dictionaryservice.getAllModels())
        {
             if(qnameObj.getLocalName().equals(modelname))
             {
            	 namespaceprefix = this.getUrlsAndPrefixesMap().get(qnameObj.getNamespaceURI());
            	 break;
             }
        }
		return namespaceprefix;
    }
    
    public boolean isValidAssociationFilter(String af)
    {
    	return (af.equalsIgnoreCase(ASSOCIATION_FILTER_OPTION_TYPE1) ||
          	    af.equalsIgnoreCase(ASSOCIATION_FILTER_OPTION_TYPE2) ||
          	    af.equalsIgnoreCase(ASSOCIATION_FILTER_OPTION_TYPE3));
    }
    
    /**
     * @param classname as the input
     * @return true if it is a aspect or false if it is a Type
     */
    public boolean isValidTypeorAspect(String classname)
    {
    	try
    	{
    		QName qname = QName.createQName(this.getFullNamespaceURI(classname));
    		return ((this.dictionaryservice.getClass(qname) != null) && 
    				(this.dictionaryservice.getClass(qname).isAspect()));
    	}
    	catch (InvalidQNameException e)
    	{
    		// ignore
    	}
    	return false;
	}
    
    /**
     * @param modelname - gets the modelname as the input (modelname is without prefix ie. cm:contentmodel => where modelname = contentmodel)
     * @return true if valid or false
     */
    public boolean isValidModelName(String modelname)
    {
    	boolean value = false;
    	for (QName qnameObj:this.dictionaryservice.getAllModels())
		{
			if (qnameObj.getLocalName().equalsIgnoreCase(modelname))
			{
				value = true;
				break;
			}
		}
    	return value;
    }
    
    /**
     * @param classname - returns the prefix from the classname of the format namespaceprefix:name eg. cm_person
     * @return prefix - returns the prefix of the classname
     */
    public String getPrefix(String classname)
    {
    	String prefix = null;
        int index = classname.indexOf(NAME_DELIMITER);
        if (index > 0)
        {
            prefix = classname.substring(0, index);
        }
        return prefix;
    }
    
    /**
     * @param classname 
     * @returns the shortname from the classname of the format cm_person 
     * 			here person represents the shortname
     */
    public String getShortName(String classname)
    {
    	String shortname = null;
    	int index = classname.indexOf(NAME_DELIMITER);
        if (index > 0)
        {
        	shortname = classname.substring(index+1);
        }
        return shortname;
    }
    
    /**
     * @param input -gets a string input and validates it
     * @return null if invalid or the string itself if its valid
     */
    public String getValidInput(String input)
    {
    	if ((input != null) && (input.length() != 0))
    	{
    		return input;
    	}
    	else
    	{
    		return null;
    	}
    }
  
   /**
    * @param classfilter =>valid class filters are all,apect or type
    * @return true if valid or false if invalid
    */
    public boolean isValidClassFilter(String classfilter)
    {
    	return (classfilter.equals(CLASS_FILTER_OPTION_TYPE1) || 
          	    classfilter.equals(CLASS_FILTER_OPTION_TYPE2) || 
          	    classfilter.equals(CLASS_FILTER_OPTION_TYPE3));
    }
   
    /**
     * @return a string map or prefixes and urls - with prefix as the key
     */
    public Map<String, String> getPrefixesAndUrlsMap()
    {
    	return prefixesAndUrlsMap;
    }
    
    /**
     * @return a string map of urls and prefixes - with url as the key
     */
    public Map<String, String> getUrlsAndPrefixesMap()
    {
    	return urlsAndPrefixesMap;
    }
}
