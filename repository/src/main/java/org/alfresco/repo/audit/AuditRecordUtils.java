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

import java.util.HashMap;
import java.util.Map;

public class AuditRecordUtils
{
    /**
     * This method will generate {@link AuditRecord#builder()} from provided flat audit data. Provided data will be translated from `key - value` structure to json structure. Generated builder will be preloaded with {@link AuditRecord#auditApplicationId} and {@link AuditRecord#auditData}. This method splits key by `/` and maps its value to the nested json as a {@link AuditRecord#auditData}. In addition, it extracts root from key as {@link AuditRecord#auditApplicationId}.
     * 
     * @param data
     *            represent `key - value` structured map that contains audit data.
     * @return preloaded {@link AuditRecord#builder()}.
     */
    @SuppressWarnings("unchecked")
    public static AuditRecord.Builder generateAuditRecordBuilder(Map<String, ?> data)
    {
        var auditRecordBuilder = AuditRecord.builder();
        var rootNode = new HashMap<String, Object>();

        data.forEach((k, v) -> {
            var keys = k.split("/");

            var startPoint = 0;
            if (keys.length > 1 && keys[startPoint].isEmpty())
            {
                startPoint = 1;
                auditRecordBuilder.setAuditApplicationId(keys[startPoint]);
            }
            else
            {
                auditRecordBuilder.setAuditApplicationId(keys[startPoint]);
            }

            var current = rootNode;
            for (int i = startPoint + 1; i < keys.length - 1; i++)
            {
                current = (HashMap<String, Object>) current.computeIfAbsent(keys[i], newMap -> new HashMap<String, Object>());
            }
            current.put(keys[keys.length - 1], v);
        });
        auditRecordBuilder.setAuditData(rootNode);

        return auditRecordBuilder;
    }
}
