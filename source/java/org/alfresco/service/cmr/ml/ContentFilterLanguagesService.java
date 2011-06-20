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
package org.alfresco.service.cmr.ml;

import java.util.List;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.service.Auditable;
import org.alfresco.service.NotAuditable;
import org.alfresco.service.PublicService;


/**
 * This service interface provides support for content filter languages .
 * 
 * @author Yannick Pignot
 *
 */
public interface ContentFilterLanguagesService
{
    
    /**
     * I18N message prefix to found the translation of a language label with a 
     * given lang code.     
     */
    public static final String MESSAGE_PREFIX = "content_filter_lang.";

    /**
     * Get the order of the specified language code
     * 
     * @param code
     * @return
     * @throws AlfrescoRuntimeException if the code doesn't exist
     */
    @NotAuditable
    public int getOrderByCode(String code);
    
    /**
     * Get the language of the specified language code
     * 
     * @param code
     * @return
     * @throws AlfrescoRuntimeException if the code doesn't exist
     */
    @NotAuditable
    public String getLabelByCode(String code);
        
    /**
     * Get ordered list of languages code
     * 
     * @return the map of displays indexed by extension
     */
    @Auditable
    public List<String> getFilterLanguages();
    
    /**
     * Get the the odered filter which results form an extract of availableLanguages on the filterLanguages 
     * 
     * @param availableLanguages the languages list whose will be removed from the filterLanguages
     * @return
     */
    @Auditable
    public List<String> getMissingLanguages(List<String> availableLanguages);
    
    /**
     * @return the default content filter language, null if it's not set. 
     */
    @Auditable
    public String getDefaultLanguage();
     
    /**
     * Since <code>java.util.Locale</code> uses and returns <b>old</b> ISO code and the content-filter-lang.xml
     * respects the <b>new ones</b>. This method convert new codes into old codes:      
     *  <p>(he, yi, and id) new codes to (iw, ji, and in) old codes </p>
     * 
     * @param code the ISO language code to convert
     * @return the convertion of the codes he, yi, and id or the given code 
     */
    @NotAuditable
    public String convertToOldISOCode(String code);
    
    /**
     * Since <code>java.util.Locale</code> uses and returns <b>old</b> ISO code and the content-filter-lang.xml
     * respects the <b>new ones</b>. This method convert old codes into new codes:      
     *  <p>(iw, ji, and in) old codes to (he, yi, and id) new codes </p>
     * 
     * @param code the ISO language code to convert
     * @return the convertion of the codes iw, ji, and in or the given code 
     */    
    @NotAuditable
    public String convertToNewISOCode(String code);
}
