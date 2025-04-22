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
package org.alfresco.repo.model.ml;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.extensions.config.Config;
import org.springframework.extensions.config.ConfigElement;
import org.springframework.extensions.config.ConfigLookupContext;
import org.springframework.extensions.config.ConfigService;
import org.springframework.extensions.surf.util.I18NUtil;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.service.cmr.ml.ContentFilterLanguagesService;
import org.alfresco.util.EqualsHelper;

/**
 * Provides a an implementation of the <b>Content Filter Languages Service</b>
 *
 * {@link org.alfresco.service.cmr.ml.ContentFilterLanguagesService Content Filter Languages Service}
 *
 * @author Yannick Pignot
 */
public class ContentFilterLanguagesMap implements ContentFilterLanguagesService
{
    private static final String CONFIG_AREA = "content-filter-lang";

    private static final String CONFIG_CONDITION = "Languages Filter Map";

    private static final String USED_STANDARD_CONFIG_CONDITION = "Standard In Use";
    private static final String USED_STANDARD_ELEMENT = "standard";

    private static final String DEFAULT_LANGUAGE_LIST_STANDARD = "ISO 639-1";

    private static final String ATTR_CODE = "code";
    private static final String ATTR_DEFAULT = "default";

    private static final Log logger = LogFactory.getLog(ContentFilterLanguagesMap.class);

    private ConfigService configService;

    private List<String> orderedLangCodes;
    private Map<String, String> languagesByCode;

    private String defaultLanguage = null;

    /**
     * @param configService
     *            the config service to use to read languages
     */
    public void setConfigService(ConfigService configService)
    {
        this.configService = configService;
    }

    /* (non-Javadoc)
     * 
     * @see org.alfresco.service.cmr.ml.ContentFilterLanguagesService#getFilterLanguages() */
    public List<String> getFilterLanguages()
    {
        return orderedLangCodes;
    }

    /* (non-Javadoc)
     * 
     * @see org.alfresco.service.cmr.ml.ContentFilterLanguagesService#getMissingLanguages(java.util.List) */
    public List<String> getMissingLanguages(List<String> availableLanguages)
    {

        if (availableLanguages == null || availableLanguages.size() == 0)
        {
            return orderedLangCodes;
        }

        List<String> returnList = new ArrayList<String>(orderedLangCodes.size() - availableLanguages.size());

        int index = 0;

        for (String lang : orderedLangCodes)
        {
            if (!availableLanguages.contains(lang))
            {
                returnList.add(index, lang);
                index++;
            }
        }
        return returnList;
    }

    /* (non-Javadoc)
     * 
     * @see org.alfresco.service.cmr.ml.ContentFilterLanguagesService#getLabelByCode(java.lang.String) */
    public String getLabelByCode(String code)
    {
        // get the translated language label
        String label;

        label = I18NUtil.getMessage(MESSAGE_PREFIX + code);

        // if not found, get the default name (found in content-filter-lang.xml)
        if (label == null || label.length() == 0)
        {
            label = languagesByCode.get(code);
        }

        // if not found, return the language code
        if (label == null || label.length() == 0)
        {
            label = code + " (label not found)";
        }

        return label;
    }

    /* (non-Javadoc)
     * 
     * @see org.alfresco.service.cmr.ml.ContentFilterLanguagesService#getOrderByCode(java.lang.String) */
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
        return this.defaultLanguage;
    }

    /**
     * Initialises the map using the configuration service provided
     */
    public void init()
    {
        ConfigLookupContext clContext = new ConfigLookupContext(CONFIG_AREA);

        // get which standart is defined by the user (ISO 639-1 by default)
        String standard = DEFAULT_LANGUAGE_LIST_STANDARD;

        Config configStandard = configService.getConfig(USED_STANDARD_CONFIG_CONDITION, clContext);

        if (configStandard != null
                && configStandard.getConfigElement(USED_STANDARD_ELEMENT) != null)
        {
            standard = configStandard.getConfigElement(USED_STANDARD_ELEMENT).getValue();
        }
        else
        {
            logger.warn("No standard configured, use by default : " + DEFAULT_LANGUAGE_LIST_STANDARD);
        }

        Config configConditions = configService.getConfig(CONFIG_CONDITION, clContext);
        Map<String, ConfigElement> configElements = configConditions.getConfigElements();

        ConfigElement configLanguages = null;

        // get the list of languages of the matched standard
        if (configElements.containsKey(standard))
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
        this.languagesByCode = new HashMap<String, String>(listSize);

        // construct the languages map and list
        for (ConfigElement langElem : languages)
        {
            String code = convertToOldISOCode(langElem.getAttribute(ATTR_CODE));
            String value = langElem.getValue();
            String def = langElem.getAttribute(ATTR_DEFAULT);

            languagesByCode.put(code, value);

            boolean isDefault = (def != null && Boolean.parseBoolean(def));
            if (isDefault)
            {
                if (defaultLanguage != null)
                {
                    logger.warn("Content filter default language is not unique: " + code);
                }
                else
                {
                    this.defaultLanguage = code;
                }
            }
            if (EqualsHelper.nullSafeEquals(defaultLanguage, code))
            {
                orderedLangCodes.add(0, code);
            }
            else
            {
                orderedLangCodes.add(code);
            }

        }

        // make the collections read-only
        this.orderedLangCodes = Collections.unmodifiableList(this.orderedLangCodes);
        this.languagesByCode = Collections.unmodifiableMap(this.languagesByCode);
    }

    /* (non-Javadoc)
     * 
     * @see org.alfresco.service.cmr.ml.ContentFilterLanguagesService#convertToOldISOCode(java.lang.String) */
    public String convertToOldISOCode(String code)
    {
        if (code.equalsIgnoreCase("he"))
            code = "iw";
        else if (code.equalsIgnoreCase("id"))
            code = "in";
        else if (code.equalsIgnoreCase("yi"))
            code = "ji";

        return code;
    }

    /* (non-Javadoc)
     * 
     * @see org.alfresco.service.cmr.ml.ContentFilterLanguagesService#convertToNewISOCode(java.lang.String) */
    public String convertToNewISOCode(String code)
    {

        if (code.equalsIgnoreCase("iw"))
            code = "he";
        else if (code.equalsIgnoreCase("in"))
            code = "id";
        else if (code.equalsIgnoreCase("ji"))
            code = "yi";

        return code;
    }
}
