/*
 * Copyright (C) 2005 Alfresco, Inc.
 *
 * Licensed under the Mozilla Public License version 1.1 
 * with a permitted attribution clause. You may obtain a
 * copy of the License at
 *
 *   http://www.alfresco.org/legal/license.txt
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific
 * language governing permissions and limitations under the
 * License.
 */
package org.alfresco.repo.dictionary;

import java.util.Locale;

import org.alfresco.i18n.I18NUtil;
import org.alfresco.service.cmr.dictionary.ModelDefinition;
import org.alfresco.service.namespace.QName;
import org.springframework.util.StringUtils;


/**
 * Helper for obtaining display labels for data dictionary items
 * 
 * @author David Caruana
 */
public class M2Label
{

    /**
     * Get label for data dictionary item given specified locale
     * 
     * @param locale
     * @param model
     * @param type
     * @param item
     * @param label
     * @return
     */
    public static String getLabel(Locale locale, ModelDefinition model, String type, QName item, String label)
    {
        String key = model.getName().toPrefixString();
        if (type != null)
        {
            key += "." + type;
        }
        if (item != null)
        {
            key += "." + item.toPrefixString();
        }
        key += "." + label;
        key = StringUtils.replace(key, ":", "_");
        return I18NUtil.getMessage(key, locale);
    }
    
    /**
     * Get label for data dictionary item
     * 
     * @param model
     * @param type
     * @param item
     * @param label
     * @return
     */
    public static String getLabel(ModelDefinition model, String type, QName item, String label)
    {
        return getLabel(I18NUtil.getLocale(), model, type, item, label);
    }
    
}
