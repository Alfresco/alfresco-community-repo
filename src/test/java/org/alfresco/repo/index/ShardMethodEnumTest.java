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

package org.alfresco.repo.index;

import org.alfresco.repo.index.shard.ShardMethodEnum;

import org.junit.Assert;
import org.junit.Test;

/**
 * Unit test to verify enum works as expected
 * @author Michael Suzuki
 *
 */
public class ShardMethodEnumTest
{
    @Test
    public void testTypeACLLegacy()
    {
        Assert.assertEquals(ShardMethodEnum.MOD_ACL_ID, ShardMethodEnum.getShardMethod("MOD_ACL_ID"));
    }
    @Test
    public void testTypeACLBasedOnMurmurHash()
    {
        Assert.assertEquals(ShardMethodEnum.ACL_ID, ShardMethodEnum.getShardMethod("ACL_ID"));
    }
    @Test
    public void testTypeDBID()
    {
        Assert.assertEquals(ShardMethodEnum.DB_ID, ShardMethodEnum.getShardMethod("DB_ID"));
    }
    @Test
    public void testTypeDateTimeStamp()
    {
        Assert.assertEquals(ShardMethodEnum.DATE, ShardMethodEnum.getShardMethod("DATE"));
    }
    @Test
    public void testTypeDateYear()
    {
        Assert.assertEquals(ShardMethodEnum.DATE_YEAR, ShardMethodEnum.getShardMethod("DATE_YEAR"));
    }
    @Test
    public void testTypeDateMonth()
    {
        Assert.assertEquals(ShardMethodEnum.DATE_MONTH, ShardMethodEnum.getShardMethod("DATE_MONTH"));
    }
    @Test
    public void testTypeProperty()
    {
        Assert.assertEquals(ShardMethodEnum.PROPERTY, ShardMethodEnum.getShardMethod("PROPERTY"));
    }
    @Test
    public void testUnknown()
    {
        Assert.assertEquals(ShardMethodEnum.UNKOWN, ShardMethodEnum.getShardMethod("UNKOWN"));
    }
    @Test
    public void testNull()
    {
        Assert.assertEquals(ShardMethodEnum.UNKOWN, ShardMethodEnum.getShardMethod(null));
    }
    @Test
    public void testEmpty()
    {
        Assert.assertEquals(ShardMethodEnum.UNKOWN, ShardMethodEnum.getShardMethod(""));
    }
}
