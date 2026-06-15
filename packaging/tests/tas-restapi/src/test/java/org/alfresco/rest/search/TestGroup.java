/*
 * #%L
 * Alfresco Search Services E2E Test
 * %%
 * Copyright (C) 2005 - 2020 Alfresco Software Limited
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

package org.alfresco.rest.search;

public class TestGroup
{
    // Used for TestRail test annotation
    public static final String SEARCH = "search";
    public static final String REST_API = "rest-api";

    public static final String PREUPGRADE = "pre-upgrade";
    public static final String POSTUPGRADE = "post-upgrade";

    public static final String CONFIG_MASTER_SLAVE = "CONFIG_Master_Slave"; // Alfresco Search Services using master slave configurations
    public static final String CONFIG_MASTER ="CONFIG_Master"; // Alfresco search services using master/stand-alone mode

    public static final String CONFIG_SHARDING ="CONFIG_Sharding"; // Alfresco search services using sharded environment
    public static final String CONFIG_SHARDING_EXPLICIT ="CONFIG_Sharding_EXPLICIT"; // Alfresco search services using sharded environment and explicit routing
    public static final String CONFIG_SHARDING_DB_ID_RANGE = "CONFIG_Sharding_DB_ID_RANGE"; // Alfresco Search Services using Sharding with DB_ID_RANGE

    public static final String CONFIG_ENABLED_CASCADE_TRACKER ="Config_Enabled_Cascade_Tracker"; // Alfresco search services does not index fields related to cascaded updates

    public static final String CROSS_LOCALE_SUPPORT_DISABLED = "CROSS_LOCALE_SUPPORT_DISABLED";

    public static final String NOT_INSIGHT_ENGINE = "Not_InsightEngine"; // When Alfresco Insight Engine 1.0 isn't running

    public static final String ACS_52n = "ACS_52n"; // Alfresco Content Services 5.2.n
    public static final String ACS_60n = "ACS_60n"; // Alfresco Content Services 6.0 or above
    public static final String ACS_61n = "ACS_61n"; // Alfresco Content Services 6.1 or above
    public static final String ACS_611n = "ACS_611n"; // Alfresco Content Services 6.1.1 or above
    public static final String ACS_62n = "ACS_62n"; // Alfresco +Content Services 6.2 or above
    public static final String ACS_63n = "ACS_63n"; // Alfresco Content Services 6.3 or above
    public static final String ACS_701n = "ACS_701n"; // Alfresco Content Services 7.0.1 or above

    public static final String AGS_302 = "AGS_302"; // Alfresco governance Services 3.0.2 or above
}
