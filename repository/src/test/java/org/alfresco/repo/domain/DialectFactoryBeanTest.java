/*
 * #%L
 * Alfresco Repository
 * %%
 * Copyright (C) 2005 - 2018 Alfresco Software Limited
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
package org.alfresco.repo.domain;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import org.alfresco.repo.domain.dialect.DialectFactoryBean;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;


/**
 * @author Erik Knizat
 */
public class DialectFactoryBeanTest
{
    private static final String MARIADB_DIALECT_NAME = "org.alfresco.repo.domain.dialect.MariaDBDialect";
    private static final String MARIA_DB_DRIVER_NAME = "MariaDB connector/J";

    @Test
    public void testMariaDBDialectGetsAdded()
    {
//        DialectFactoryBean dfb = new DialectFactoryBean();
//        Map<String, String> driverDialectMap = new HashMap<>();
//        driverDialectMap.put(MARIA_DB_DRIVER_NAME, MARIADB_DIALECT_NAME);
//        dfb.setDriverDialectMap(driverDialectMap);
//        Properties props = new Properties();
//        dfb.overrideDialectPropertyForDriver(props, MARIA_DB_DRIVER_NAME);
//
//        assertNotNull("The dialect property was not set for the driver.", props.getProperty((Environment.DIALECT)));
//        assertEquals("Dialect name did not match.", MARIADB_DIALECT_NAME, props.getProperty((Environment.DIALECT)));
    }

    @Test
    public void testDialectNotAddedIfNotSpecifiedForDriver()
    {
//        DialectFactoryBean dfb = new DialectFactoryBean();
//        Map<String, String> driverDialectMap = new HashMap<>();
//        dfb.setDriverDialectMap(driverDialectMap); // Add empty dialect driver map
//        Properties props = new Properties();
//        dfb.overrideDialectPropertyForDriver(props, MARIA_DB_DRIVER_NAME);
//
//        assertNull("Dialect name property was set for unspecified driver name.", props.getProperty((Environment.DIALECT)));
    }

}
