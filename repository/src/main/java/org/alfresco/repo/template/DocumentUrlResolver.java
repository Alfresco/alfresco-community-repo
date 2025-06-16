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
package org.alfresco.repo.template;

import java.text.MessageFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import freemarker.template.*;
import org.springframework.extensions.surf.util.I18NUtil;
import org.springframework.util.CollectionUtils;

public class DocumentUrlResolver extends BaseTemplateProcessorExtension implements TemplateMethodModelEx
{

    ResourceBundle resourceBundle;
    private final String KEY_ROOT_URL = "root_url";
    private final String KEY_PROD_VER = "prod_ver";
    private final String KEY_COMMUNITY_LINK = "community_link";
    private final String KEY_ENTERPRISE_LINK = "enterprise_link";

    private static final Pattern PLACEHOLDER_PATTERN = Pattern.compile("\\$\\{([^}]+)}");

    private final List<String> mainKeyList = List.of(
            KEY_ROOT_URL,
            KEY_PROD_VER,
            KEY_COMMUNITY_LINK,
            KEY_ENTERPRISE_LINK);

    public void setUrlBundle(String bundleName)
    {
        resourceBundle = ResourceBundle.getBundle(bundleName);
    }

    @Override
    public Object exec(List list) throws TemplateModelException
    {
        if (CollectionUtils.isEmpty(list))
        {
            return "";
        }
        Object arg0 = list.get(0);
        String propertyKey = null;
        if (arg0 instanceof SimpleScalar)
        {
            propertyKey = ((SimpleScalar) arg0).getAsString();
        }
        return resolvePropertyValue(propertyKey, list.subList(1, list.size()).toArray());
    }

    public Object jspExec(String message, Object... args)
    {
        return resolvePropertyValue(message, args);
    }

    private String resolvePropertyValue(String messageKey, Object... args)
    {
        if (resourceBundle == null || messageKey == null)
        {
            return null;
        }

        String message = resourceBundle.containsKey(messageKey)
                ? resourceBundle.getString(messageKey)
                : I18NUtil.getMessage(messageKey);

        if (message == null)
        {
            return null;
        }

        String resolved = resolvePlaceholdersRecursively(message);

        if (args != null && args.length > 0)
        {
            return MessageFormat.format(resolved, args);
        }
        return resolved;
    }

    private String resolvePlaceholdersRecursively(String message)
    {
        Matcher matcher = PLACEHOLDER_PATTERN.matcher(message);
        StringBuffer result = new StringBuffer();
        boolean found = false;

        while (matcher.find())
        {
            found = true;
            String key = matcher.group(1);
            String replacement = resourceBundle.containsKey(key)
                    ? resolvePlaceholdersRecursively(resourceBundle.getString(key))
                    : "${" + key + "}";
            matcher.appendReplacement(result, Matcher.quoteReplacement(replacement));
        }
        matcher.appendTail(result);

        // If any placeholder was found and replaced, check again recursively
        return found ? resolvePlaceholdersRecursively(result.toString()) : result.toString();
    }
}
