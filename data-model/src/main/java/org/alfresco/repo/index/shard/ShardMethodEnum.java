/*
 * #%L
 * Alfresco Data model classes
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
package org.alfresco.repo.index.shard;
/**
 * Enum that details sharding type.
 *
 * @author Andy
 * @author Michael Suzuki
 * @author agazzarini
 */
public enum ShardMethodEnum
{
    MOD_ACL_ID,
    ACL_ID,
    DB_ID,
    DB_ID_RANGE,
    EXPLICIT_ID,
    EXPLICIT_ID_FALLBACK_LRIS,
    LAST_REGISTERED_INDEXING_SHARD,
    DATE,
    UNKOWN,
    PROPERTY;
    
    public static ShardMethodEnum getShardMethod(String shardMethod)
    {
        if(shardMethod == null)
        {
            return UNKOWN;
        }

        switch (shardMethod.toUpperCase())
        {
            case "MOD_ACL_ID":
                return MOD_ACL_ID;
            case "ACL_ID":
                return ACL_ID;
            case "DB_ID":
                return DB_ID;
            case "DB_ID_RANGE":
                return DB_ID_RANGE;
            case "DATE":
                return DATE;
            case "PROPERTY":
                return PROPERTY;
            case "EXPLICIT_ID":
            case "EXPLICIT_ID_FALLBACK_DBID":
                return EXPLICIT_ID;
            case "LRIS":
            case "LAST_REGISTERED_INDEXING_SHARD":
                return LAST_REGISTERED_INDEXING_SHARD;
            case "EXPLICIT_ID_FALLBACK_LRIS":
                return EXPLICIT_ID_FALLBACK_LRIS;
            default:
                return UNKOWN;
        }
    }
    
    /**
     * Returns true if the method if any of the alias for EXPLICIT_ID methods, false otherwise
     * @param shardMethod String representing the ShardMethod name
     * @return true if the method is EXPLICIT_ID based
     */
    public static boolean isExplicitIdMethod(String shardMethod)
    {
        
        switch (shardMethod.toUpperCase())
        {
            case "EXPLICIT_ID":
            case "EXPLICIT_ID_FALLBACK_DBID":
            case "EXPLICIT_ID_FALLBACK_LRIS":
                return true;
            default:
                return false;
        }
        
    }
    
}