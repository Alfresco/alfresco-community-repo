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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.Serializable;
import java.time.Instant;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

import org.alfresco.service.namespace.QName;

public class AuditRecordUtilsTest
{
    @SuppressWarnings("unchecked")
    @Test
    public void testGenerateAuditRecordBuilderTest()
    {
        var testData = new HashMap<String, Serializable>();

        testData.put("/alfresco-access/transaction/path", "/app:company_home");
        testData.put("/alfresco-access/transaction/user", "admin");
        testData.put("/alfresco-access/transaction/sub-actions", "updateNodeProperties");
        var now = Instant.now();
        testData.put("/alfresco-access/transaction/properties/from", (Serializable) Map.of(QName.createQName("modified"), Date.from(now)));
        testData.put("/alfresco-access/transaction/properties/to", (Serializable) Map.of(QName.createQName("modified"), Date.from(now)));

        var builder = AuditRecordUtils.generateAuditRecordBuilder(testData, "/alfresco-access/".length());
        builder.setAuditRecordType("alfresco-access");
        var auditRecord = builder.build();

        assertNotNull(auditRecord);
        assertEquals("alfresco-access", auditRecord.getAuditApplicationId());

        var auditData = auditRecord.getAuditData();
        assertEquals(1, auditData.size());

        var transaction = (HashMap<String, ?>) auditData.get("transaction");
        assertNotNull(transaction);
        assertEquals(4, transaction.size());
        assertEquals(testData.get("/alfresco-access/transaction/path"), transaction.get("path"));
        assertEquals(testData.get("/alfresco-access/transaction/user"), transaction.get("user"));
        assertEquals(testData.get("/alfresco-access/transaction/sub-actions"), transaction.get("sub-actions"));

        var properties = (HashMap<String, Object>) transaction.get("properties");
        assertNotNull(properties);
        assertEquals(2, properties.size());
        assertEquals(testData.get("/alfresco-access/transaction/properties/from"), properties.get("from"));
        assertEquals(testData.get("/alfresco-access/transaction/properties/to"), properties.get("to"));

    }
}
