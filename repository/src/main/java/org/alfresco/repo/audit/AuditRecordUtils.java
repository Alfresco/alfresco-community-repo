/*
 * #%L
 * Alfresco Repository
 * %%
 * Copyright (C) 2025 Alfresco Software Limited
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
package org.alfresco.repo.audit;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import org.alfresco.service.cmr.repository.NodeRef;

public final class AuditRecordUtils
{
    private AuditRecordUtils()
    {
        // This is a utility class and cannot be instantiated.
    }

    /**
     * Generates an {@link AuditRecord.Builder} from flat audit data.
     * <p>
     * This method:
     * <ul>
     * <li>Translates flat {@code key-value} pairs into a nested JSON structure.</li>
     * <li>Preloads the builder with the provided arguments.</li>
     * <li>Splits keys by {@code /} to build the nested structure.</li>
     * <li>Uses the root key as the application ID.</li>
     * <li>Assumes each key starts with the same root, constructed as {@code '/' + auditedApplicationName + '/'}, which is removed before splitting.</li>
     * </ul>
     *
     * @param data
     *            a map containing flat audit data as `key-value` pairs
     * @param keyRootLength
     *            is a length of key root.
     * @return a preloaded {@link AuditRecord.Builder}
     */
    public static AuditRecord.Builder generateAuditRecordBuilder(Map<String, Serializable> data, int keyRootLength)
    {
        var auditRecordBuilder = AuditRecord.builder();

        var rootNode = createRootNode(data, keyRootLength);

        auditRecordBuilder.setAuditRecordData(rootNode);

        return auditRecordBuilder;
    }

    @SuppressWarnings("unchecked")
    private static HashMap<String, Serializable> createRootNode(Map<String, Serializable> data, int keyRootLength)
    {
        var rootNode = new HashMap<String, Serializable>();

        data.forEach((k, v) -> {
            var keys = k.substring(keyRootLength).split("/");

            var current = rootNode;
            for (int i = 0; i < keys.length - 1; i++)
            {
                current = (HashMap<String, Serializable>) current.computeIfAbsent(keys[i], newMap -> new HashMap<String, Serializable>());
            }
            current.put(keys[keys.length - 1], decodeValueByInstance(v));
        });
        return rootNode;
    }

    @SuppressWarnings("unchecked")
    private static Serializable decodeValueByInstance(Serializable value)
    {
        if (value instanceof HashMap<?, ?>)
        {
            return createRootNode((HashMap<String, Serializable>) value, 0);
        }
        else if (value instanceof NodeRef)
        {
            return ((NodeRef) value).getId();
        }
        else
        {
            return value;
        }
    }
}
