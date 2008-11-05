/*
 * Copyright (C) 2005-2007 Alfresco Software Limited.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.

 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.

 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.

 * As a special exception to the terms and conditions of version 2.0 of 
 * the GPL, you may redistribute this Program in connection with Free/Libre 
 * and Open Source Software ("FLOSS") applications as described in Alfresco's 
 * FLOSS exception.  You should have recieved a copy of the text describing 
 * the FLOSS exception, and it is also available here: 
 * http://www.alfresco.com/legal/licensing"
 */
package org.alfresco.repo.web.scripts.dictionary;

import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/*
 * Helper class for Dictionary Service webscripts
 * @author Saravanan Sellathurai
 */

public class DictionaryHelper 
{
	private static final String NAME_DELIMITER = "_";
    private NamespaceService namespaceservice;
    private Map<String, String> prefixesAndUrlsMap;
    private Map<String, String> urlsAndPrefixesMap;
    private Collection<String> prefixes;
    private DictionaryService dictionaryservice;
    
    /**
     * Set the namespaceService property.
     * 
     * @param namespaceService The namespace service instance to set
     */
    public void setNamespaceService(NamespaceService namespaceservice)
    {
    	this.namespaceservice = namespaceservice;
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
    	this.prefixes = this.namespaceservice.getPrefixes();
        this.prefixesAndUrlsMap = new HashMap<String, String>(prefixes.size());
        this.urlsAndPrefixesMap = new HashMap<String, String>(prefixes.size());
        for (String prefix : prefixes)
        {
        	String url = this.namespaceservice.getNamespaceURI(prefix);
        	this.prefixesAndUrlsMap.put(prefix, url);
            this.urlsAndPrefixesMap.put(url, prefix);           
        }
	 }
    
    public String getNamespaceURIfromQname(QName qname){
    	return qname.getNamespaceURI();
    	
   
    }
	/**
     * 
     * @param className     the class name as cm_person
     * @return String       the full name in the following format {namespaceuri}shorname
     */
    public String getFullNamespaceURI(String classname)
    {
       	String result = null;
       	String prefix = this.getPrefix(classname);
		String url = this.prefixesAndUrlsMap.get(prefix);
		String name = this.getShortName(classname);
		result = "{" + url + "}"+ name;
		return result;
    }
    
    /**
     * 
     * @param className     the class name as cm_person
     * @return String       the full name in the following format {namespaceuri}shorname
     */
    public String getNamespaceURIfromPrefix(String prefix)
    {
       	String result = null;
       	if(this.isValidPrefix(prefix))	result = this.prefixesAndUrlsMap.get(prefix);
		return result;
    }
    
     /*
     * checks whether the classname (eg.cm_author) is a valid one
     */
    public boolean isValidClassname(String classname)
    {
    	boolean result = false;
    	QName qname = QName.createQName(this.getFullNamespaceURI(classname));
    	if(this.isValidPrefix(this.getPrefix(classname))&& this.dictionaryservice.getClass(qname)!=null) result = true;
    	return result;
    }
    
    /*
     * checks whether the prefix is a valid one 
     */
    public boolean isValidPrefix(String prefix)
    {
    	boolean result = false;
    	if(this.prefixes.contains(prefix)) result = true;
    	return result;
    }
    
           
    /*
     * returns the prefix from the classname of the format cm_person 
     * here cm represents the prefix
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
    
    /*
     * returns the shortname from the classname of the format cm_person 
     * here person represents the shortname
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
    
    /*
     * returns a string map or prefixes and urls - with prefix as the key
     */
    public Map<String, String> getPrefixesAndUrlsMap()
    {
    	return prefixesAndUrlsMap;
    }
    
    /*
     * returns a string map of urls and prefixes - with url as the key
     */
    public Map<String, String> getUrlsAndPrefixesMap()
    {
    	return urlsAndPrefixesMap;
    }
}
