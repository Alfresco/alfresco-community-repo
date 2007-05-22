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
package org.alfresco.repo.model.ml;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.alfresco.config.Config;
import org.alfresco.config.ConfigElement;
import org.alfresco.config.ConfigLookupContext;
import org.alfresco.config.ConfigService;
import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.i18n.I18NUtil;
import org.alfresco.service.cmr.ml.ContentFilterLanguagesService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Provides a an implementation of the <b>Content Filter Languages Service</b>
 *
 * {@link org.alfresco.service.cmr.ml.ContentFilterLanguagesService Content Filter Languages Service}
 *
 * @author yanipig
 */
public class ContentFilterLanguagesMap implements ContentFilterLanguagesService
{
    private static final String CONFIG_AREA = "content-filter-lang";

    private static final String CONFIG_CONDITION = "Languages Filter Map";

    private static final String USED_STANDARD_CONFIG_CONDITION = "Standard In Use";
    private static final String USED_STANDARD_ELEMENT = "standard";

    private static final String DEFAULT_LANGUAGE_LIST_STANDARD = "ISO 639-1";

    private static final String ATTR_CODE = "code";
    private static final String ATTR_ORDER = "order";
    private static final String ATTR_DEFAULT = "default";

    private static final Log logger = LogFactory.getLog(ContentFilterLanguagesMap.class);

    private ConfigService configService;

    private List<String> orderedLangCodes;
    private Map<String, String> languagesByCode;

    private String defaultLanguage = null;

    /**
     * @param configService the config service to use to read languages
     */
    public ContentFilterLanguagesMap(ConfigService configService)
    {
        this.configService = configService;
    }


    /* (non-Javadoc)
     * @see org.alfresco.service.cmr.ml.ContentFilterLanguagesService#getFilterLanguages()
     */
    public List<String> getFilterLanguages()
    {
        return orderedLangCodes;
    }

    /* (non-Javadoc)
     * @see org.alfresco.service.cmr.ml.ContentFilterLanguagesService#getMissingLanguages(java.util.List)
     */
    public List<String> getMissingLanguages(List<String> availableLanguages)
    {

        if(availableLanguages == null || availableLanguages.size() == 0)
        {
            return orderedLangCodes;
        }

        List<String> returnList = new ArrayList<String>(orderedLangCodes.size() - availableLanguages.size());

        int index = 0;

        for(String lang : orderedLangCodes)
        {
            if(!availableLanguages.contains(lang))
            {
                returnList.add(index, lang);
                index ++;
            }
        }
        return returnList;
    }

    /* (non-Javadoc)
     * @see org.alfresco.service.cmr.ml.ContentFilterLanguagesService#getLabelByCode(java.lang.String)
     */
    public String getLabelByCode(String code)
    {
        // get the translated language label
        String label;

        label = I18NUtil.getMessage(MESSAGE_PREFIX + code);

        // if not found, get the default name (found in content-filter-lang.xml)
        if(label == null || label.length() == 0)
        {
            label = languagesByCode.get(code);
        }

        // if not found, return the language code
        if(label == null || label.length() == 0)
        {
            label = code + " (label not found)";
        }

        return label;
    }

    /* (non-Javadoc)
     * @see org.alfresco.service.cmr.ml.ContentFilterLanguagesService#getOrderByCode(java.lang.String)
     */
    public int getOrderByCode(String code)
    {
        if (orderedLangCodes.contains(code))
        {
            return orderedLangCodes.indexOf(code);
        }
        else
        {
            throw new AlfrescoRuntimeException("Language code not found : " + code);
        }
    }

    public String getDefaultLanguage()
    {
        return this.defaultLanguage ;
    }

    /**
     * Initialises the map using the configuration service provided
     */
    public void init()
    {
        ConfigLookupContext clContext = new ConfigLookupContext(CONFIG_AREA);

        // get which standart is defined by the user (ISO 639-1 by default)
        String standard = DEFAULT_LANGUAGE_LIST_STANDARD;

        Config configStandard  = configService.getConfig(USED_STANDARD_CONFIG_CONDITION, clContext);

        if(configStandard != null
                && configStandard.getConfigElement(USED_STANDARD_ELEMENT) != null)
        {
            standard = configStandard.getConfigElement(USED_STANDARD_ELEMENT).getValue();
        }
        else
        {
            logger.warn("No standard configured, use by default : " + DEFAULT_LANGUAGE_LIST_STANDARD);
        }


        Config configConditions    = configService.getConfig(CONFIG_CONDITION, clContext);
        Map<String, ConfigElement> configElements = configConditions.getConfigElements();

        ConfigElement configLanguages = null;

        // get the list of languages of the matched standard
        if(configElements.containsKey(standard))
        {
            configLanguages = configElements.get(standard);
        }

        // if the santard is not matched, get the first list
        else
        {
            configLanguages = configElements.values().iterator().next();
            logger.warn("Ignoring prefered standard doesn't found, choose : " + configLanguages.getName());
        }

        List<ConfigElement> languages = configLanguages.getChildren();

        // get the size of the lists
        int listSize = languages.size();
        this.orderedLangCodes = new ArrayList<String>(listSize);
        this.languagesByCode  = new HashMap<String, String>(listSize);

        // construct the languages map and list
        for (ConfigElement langElem : languages)
        {
            String code  = convertToOldISOCode(langElem.getAttribute(ATTR_CODE));
            String order = langElem.getAttribute(ATTR_ORDER);
            String value = langElem.getValue();
            String def   = langElem.getAttribute(ATTR_DEFAULT);

            orderedLangCodes.add(Integer.parseInt(order) - 1, code);

            languagesByCode.put(code, value);

            if(def != null && Boolean.parseBoolean(def))
            {
                if(defaultLanguage != null)
                {
                    logger.warn("Ignoring default attribute is not unique le last matched will be used");
                }

                this.defaultLanguage = code;
            }
        }

        // make the collections read-only
        this.orderedLangCodes = Collections.unmodifiableList(this.orderedLangCodes);
        this.languagesByCode  = Collections.unmodifiableMap(this.languagesByCode);
    }

    /* (non-Javadoc)
     * @see org.alfresco.service.cmr.ml.ContentFilterLanguagesService#convertToOldISOCode(java.lang.String)
     */
    public String convertToOldISOCode(String code)
    {
        if(code.equalsIgnoreCase("he"))
            code = "iw";
        else if(code.equalsIgnoreCase("id"))
            code = "in";
        else if(code.equalsIgnoreCase("yi"))
            code = "ji";

        return code;
    }


    /* (non-Javadoc)
     * @see org.alfresco.service.cmr.ml.ContentFilterLanguagesService#convertToNewISOCode(java.lang.String)
     */
    public String convertToNewISOCode(String code) {

        if(code.equalsIgnoreCase("iw"))
            code = "he";
        else if(code.equalsIgnoreCase("in"))
            code = "id";
        else if(code.equalsIgnoreCase("ji"))
            code = "yi";

        return code;
    }
}
