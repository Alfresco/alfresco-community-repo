package org.alfresco.repo.audit;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.databind.node.ObjectNode;

public class AuditRecordUtils
{
    /**
     * This method will generate {@link AuditRecord#builder()} from provided structured audit data. Provided data will be translated from `key - value` structure to json structure. Generated builder will be preloaded with {@link AuditRecord#auditApplicationId} and {@link AuditRecord#auditData}
     *
     * @param data
     *            represent `key - value` structured map that contains audit data.
     * @return preloaded {@link AuditRecord#builder()}.
     */
    @SuppressWarnings("unchecked")
    public static AuditRecord.Builder generateAuditRecordBuilder(Map<String, ?> data)
    {
        var auditRecordBuilder = AuditRecord.builder();

        var rootNode = new HashMap<String, Serializable>();
        data.forEach((k, v) -> {
            var keys = k.split("/");
            auditRecordBuilder.setAuditApplicationId(keys[0]);
            var current = rootNode;
            for (int i = 1; i < keys.length - 1; i++)
            {
                if (!current.containsKey(keys[i]) || !(current.get(keys[i]) instanceof ObjectNode))
                {
                    current.put(keys[i], new HashMap<String, Serializable>());
                }
                current = (HashMap<String, Serializable>) current.get(keys[i]);
            }
            current.put(keys[keys.length - 1], v.toString());
        });

        auditRecordBuilder.setAuditData(rootNode);
        return auditRecordBuilder;
    }
}
