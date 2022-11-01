/*
 * #%L
 * Alfresco Remote API
 * %%
 * Copyright (C) 2005 - 2022 Alfresco Software Limited
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
 *  GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Alfresco. If not, see <http://www.gnu.org/licenses/>.
 * #L%
 */
package org.alfresco.rest.api.model.rules;

import java.util.Objects;
import java.util.StringJoiner;

import org.alfresco.rest.framework.resource.UniqueId;
import org.alfresco.service.Experimental;

@Experimental
public class RuleSetting
{
    public static final String IS_INHERITANCE_ENABLED_KEY = "-isInheritanceEnabled-";

    private String key;
    private Object value;

    @UniqueId
    public String getKey()
    {
        return key;
    }

    public void setKey(String key)
    {
        this.key = key;
    }

    public Object getValue()
    {
        return value;
    }

    public void setValue(Object value)
    {
        this.value = value;
    }

    @Override
    public String toString()
    {
        return "RuleSetting{"
                + new StringJoiner(", ")
                    .add("key=" + key)
                    .add("value=" + value.toString())
                    .toString()
                + "}";
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o)
        {
            return true;
        }
        if (!(o instanceof RuleSetting))
        {
            return false;
        }
        RuleSetting that = (RuleSetting) o;
        return Objects.equals(key, that.key)
                && Objects.equals(value, that.value);
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(key, value);
    }

    public static RuleSetting.Builder builder()
    {
        return new RuleSetting.Builder();
    }

    public static class Builder
    {
        private String key;
        private Object value;

        public RuleSetting.Builder key(String key)
        {
            this.key = key;
            return this;
        }

        public RuleSetting.Builder value(Object value)
        {
            this.value = value;
            return this;
        }

        public RuleSetting create()
        {
            final RuleSetting ruleSetting = new RuleSetting();
            ruleSetting.setKey(key);
            ruleSetting.setValue(value);
            return ruleSetting;
        }
    }
}
