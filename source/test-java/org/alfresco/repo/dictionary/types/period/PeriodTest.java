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
package org.alfresco.repo.dictionary.types.period;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import junit.framework.TestCase;

import org.alfresco.service.cmr.dictionary.DataTypeDefinition;
import org.alfresco.service.cmr.repository.Period;
import org.alfresco.service.cmr.repository.PeriodProvider;
import org.alfresco.test_category.OwnJVMTestsCategory;
import org.alfresco.util.ApplicationContextHelper;
import org.alfresco.util.CachingDateFormat;
import org.junit.FixMethodOrder;
import org.junit.experimental.categories.Category;
import org.junit.runners.MethodSorters;

/**
 * Tests for period implementations - persistence and search is tested elsewhere
 * 
 * @author andyh
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@Category(OwnJVMTestsCategory.class)
public class PeriodTest extends TestCase
{

    @Override
    protected void setUp() throws Exception
    {
        super.setUp();
        ApplicationContextHelper.getApplicationContext();
    }

    /**
     * "none"
     */
    public void test01NoPeriod()
    {
        assertTrue(Period.getProviderNames().contains(NoPeriod.PERIOD_TYPE));
        Period period = new Period(NoPeriod.PERIOD_TYPE);
        assertNull(period.getNextDate(new Date()));

        PeriodProvider provider = Period.getProvider(NoPeriod.PERIOD_TYPE);
        assertNull(provider.getDefaultExpression());
        assertEquals(provider.getExpressionMutiplicity(), PeriodProvider.ExpressionMutiplicity.NONE);
        assertNull(provider.getNextDate(new Date(), null));
        assertNull(provider.getNextDate(new Date(), ""));
        assertNull(provider.getNextDate(new Date(), "meep"));
        assertEquals(provider.getPeriodType(), NoPeriod.PERIOD_TYPE);
        assertNull(provider.getExpressionDataType());
        assertEquals("None", provider.getDisplayLabel());
    }

    /**
     * Days
     */
    public void test02Days()
    {
        assertTrue(Period.getProviderNames().contains(Days.PERIOD_TYPE));
        Period period = new Period(Days.PERIOD_TYPE);
        Date now = new Date();
        assertNotNull(period.getNextDate(now));
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(now);
        calendar.add(Calendar.DAY_OF_YEAR, 1);
        assertEquals(period.getNextDate(now), calendar.getTime());

        period = new Period(Days.PERIOD_TYPE + "|0");
        assertNotNull(period.getNextDate(now));
        calendar.setTime(now);
        calendar.add(Calendar.DAY_OF_YEAR, 0);
        assertEquals(period.getNextDate(now), calendar.getTime());

        period = new Period(Days.PERIOD_TYPE + "|1");
        assertNotNull(period.getNextDate(now));
        calendar.setTime(now);
        calendar.add(Calendar.DAY_OF_YEAR, 1);
        assertEquals(period.getNextDate(now), calendar.getTime());

        period = new Period(Days.PERIOD_TYPE + "|2");
        assertNotNull(period.getNextDate(now));
        calendar.setTime(now);
        calendar.add(Calendar.DAY_OF_YEAR, 2);
        assertEquals(period.getNextDate(now), calendar.getTime());

        PeriodProvider provider = Period.getProvider(Days.PERIOD_TYPE);
        assertEquals(provider.getDefaultExpression(), "1");
        assertEquals(provider.getExpressionMutiplicity(), PeriodProvider.ExpressionMutiplicity.OPTIONAL);
        assertNotNull(provider.getNextDate(now, "0"));
        assertNotNull(provider.getNextDate(now, "1"));
        assertNotNull(provider.getNextDate(now, "2"));
        assertEquals(provider.getPeriodType(), Days.PERIOD_TYPE);
        assertEquals(DataTypeDefinition.INT, provider.getExpressionDataType());
        assertEquals("Day", provider.getDisplayLabel());
    }

    /**
     * Weeks
     */
    public void test03Weeks()
    {
        assertTrue(Period.getProviderNames().contains(Weeks.PERIOD_TYPE));
        Period period = new Period(Weeks.PERIOD_TYPE);
        Date now = new Date();
        assertNotNull(period.getNextDate(now));
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(now);
        calendar.add(Calendar.WEEK_OF_YEAR, 1);
        assertEquals(period.getNextDate(now), calendar.getTime());

        period = new Period(Weeks.PERIOD_TYPE + "|0");
        assertNotNull(period.getNextDate(now));
        calendar.setTime(now);
        calendar.add(Calendar.WEEK_OF_YEAR, 0);
        assertEquals(period.getNextDate(now), calendar.getTime());

        period = new Period(Weeks.PERIOD_TYPE + "|1");
        assertNotNull(period.getNextDate(now));
        calendar.setTime(now);
        calendar.add(Calendar.WEEK_OF_YEAR, 1);
        assertEquals(period.getNextDate(now), calendar.getTime());

        period = new Period(Weeks.PERIOD_TYPE + "|2");
        assertNotNull(period.getNextDate(now));
        calendar.setTime(now);
        calendar.add(Calendar.WEEK_OF_YEAR, 2);
        assertEquals(period.getNextDate(now), calendar.getTime());

        PeriodProvider provider = Period.getProvider(Weeks.PERIOD_TYPE);
        assertEquals(provider.getDefaultExpression(), "1");
        assertEquals(provider.getExpressionMutiplicity(), PeriodProvider.ExpressionMutiplicity.OPTIONAL);
        assertNotNull(provider.getNextDate(now, "0"));
        assertNotNull(provider.getNextDate(now, "1"));
        assertNotNull(provider.getNextDate(now, "2"));
        assertEquals(provider.getPeriodType(), Weeks.PERIOD_TYPE);
        assertEquals(DataTypeDefinition.INT, provider.getExpressionDataType());
        assertEquals("Week", provider.getDisplayLabel());
    }

    /**
     * Months
     */
    public void test04Months()
    {
        assertTrue(Period.getProviderNames().contains(Months.PERIOD_TYPE));
        Period period = new Period(Months.PERIOD_TYPE);
        Date now = new Date();
        assertNotNull(period.getNextDate(now));
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(now);
        calendar.add(Calendar.MONTH, 1);
        assertEquals(period.getNextDate(now), calendar.getTime());

        period = new Period(Months.PERIOD_TYPE + "|0");
        assertNotNull(period.getNextDate(now));
        calendar.setTime(now);
        calendar.add(Calendar.MONTH, 0);
        assertEquals(period.getNextDate(now), calendar.getTime());

        period = new Period(Months.PERIOD_TYPE + "|1");
        assertNotNull(period.getNextDate(now));
        calendar.setTime(now);
        calendar.add(Calendar.MONTH, 1);
        assertEquals(period.getNextDate(now), calendar.getTime());

        period = new Period(Months.PERIOD_TYPE + "|2");
        assertNotNull(period.getNextDate(now));
        calendar.setTime(now);
        calendar.add(Calendar.MONTH, 2);
        assertEquals(period.getNextDate(now), calendar.getTime());

        PeriodProvider provider = Period.getProvider(Months.PERIOD_TYPE);
        assertEquals(provider.getDefaultExpression(), "1");
        assertEquals(provider.getExpressionMutiplicity(), PeriodProvider.ExpressionMutiplicity.OPTIONAL);
        assertNotNull(provider.getNextDate(now, "0"));
        assertNotNull(provider.getNextDate(now, "1"));
        assertNotNull(provider.getNextDate(now, "2"));
        assertEquals(provider.getPeriodType(), Months.PERIOD_TYPE);
        assertEquals(DataTypeDefinition.INT, provider.getExpressionDataType());
        assertEquals("Month", provider.getDisplayLabel());
    }

    /**
     * Quarters
     */

    public void test05Quarters()
    {
        assertTrue(Period.getProviderNames().contains(Quarters.PERIOD_TYPE));
        Period period = new Period(Quarters.PERIOD_TYPE);
        Date now = new Date();
        assertNotNull(period.getNextDate(now));
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(now);
        calendar.add(Calendar.MONTH, 3);
        assertEquals(period.getNextDate(now), calendar.getTime());

        period = new Period(Quarters.PERIOD_TYPE + "|0");
        assertNotNull(period.getNextDate(now));
        calendar.setTime(now);
        calendar.add(Calendar.MONTH, 0);
        assertEquals(period.getNextDate(now), calendar.getTime());

        period = new Period(Quarters.PERIOD_TYPE + "|1");
        assertNotNull(period.getNextDate(now));
        calendar.setTime(now);
        calendar.add(Calendar.MONTH, 3);
        assertEquals(period.getNextDate(now), calendar.getTime());

        period = new Period(Quarters.PERIOD_TYPE + "|2");
        assertNotNull(period.getNextDate(now));
        calendar.setTime(now);
        calendar.add(Calendar.MONTH, 6);
        assertEquals(period.getNextDate(now), calendar.getTime());

        PeriodProvider provider = Period.getProvider(Quarters.PERIOD_TYPE);
        assertEquals(provider.getDefaultExpression(), "1");
        assertEquals(provider.getExpressionMutiplicity(), PeriodProvider.ExpressionMutiplicity.OPTIONAL);
        assertNotNull(provider.getNextDate(now, "0"));
        assertNotNull(provider.getNextDate(now, "1"));
        assertNotNull(provider.getNextDate(now, "2"));
        assertEquals(provider.getPeriodType(), Quarters.PERIOD_TYPE);
        assertEquals(DataTypeDefinition.INT, provider.getExpressionDataType());
        assertEquals("Quarter", provider.getDisplayLabel());
    }

    /**
     * Years
     */
    public void test06Years()
    {
        assertTrue(Period.getProviderNames().contains(Years.PERIOD_TYPE));
        Period period = new Period(Years.PERIOD_TYPE);
        Date now = new Date();
        assertNotNull(period.getNextDate(now));
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(now);
        calendar.add(Calendar.YEAR, 1);
        assertEquals(period.getNextDate(now), calendar.getTime());

        period = new Period(Years.PERIOD_TYPE + "|0");
        assertNotNull(period.getNextDate(now));
        calendar.setTime(now);
        calendar.add(Calendar.YEAR, 0);
        assertEquals(period.getNextDate(now), calendar.getTime());

        period = new Period(Years.PERIOD_TYPE + "|1");
        assertNotNull(period.getNextDate(now));
        calendar.setTime(now);
        calendar.add(Calendar.YEAR, 1);
        assertEquals(period.getNextDate(now), calendar.getTime());

        period = new Period(Years.PERIOD_TYPE + "|2");
        assertNotNull(period.getNextDate(now));
        calendar.setTime(now);
        calendar.add(Calendar.YEAR, 2);
        assertEquals(period.getNextDate(now), calendar.getTime());

        PeriodProvider provider = Period.getProvider(Years.PERIOD_TYPE);
        assertEquals(provider.getDefaultExpression(), "1");
        assertEquals(provider.getExpressionMutiplicity(), PeriodProvider.ExpressionMutiplicity.OPTIONAL);
        assertNotNull(provider.getNextDate(now, "0"));
        assertNotNull(provider.getNextDate(now, "1"));
        assertNotNull(provider.getNextDate(now, "2"));
        assertEquals(provider.getPeriodType(), Years.PERIOD_TYPE);
        assertEquals(DataTypeDefinition.INT, provider.getExpressionDataType());
        assertEquals("Year", provider.getDisplayLabel());
    }

    /**
     * EndOfMonth
     */
    public void test07EndOfMonth()
    {
        assertTrue(Period.getProviderNames().contains(EndOfMonth.PERIOD_TYPE));
        Period period = new Period(EndOfMonth.PERIOD_TYPE);
        Date now = new Date();
        assertNotNull(period.getNextDate(now));
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(now);
        calendar.add(Calendar.MONTH, 1);
        calendar.set(Calendar.DAY_OF_MONTH, 1);
        calendar.add(Calendar.DAY_OF_MONTH, -1);
        calendar.set(Calendar.HOUR_OF_DAY, 23);
        calendar.set(Calendar.MINUTE, 59);
        calendar.set(Calendar.SECOND, 59);
        calendar.set(Calendar.MILLISECOND, 999);
        assertEquals(period.getNextDate(now), calendar.getTime());

        for (int i = -100; i < 100; i++)
        {
            period = new Period(EndOfMonth.PERIOD_TYPE + "|" + i);
            assertNotNull(period.getNextDate(now));
            calendar.setTime(now);
            calendar.add(Calendar.MONTH, i);
            calendar.set(Calendar.DAY_OF_MONTH, 1);
            calendar.add(Calendar.DAY_OF_MONTH, -1);
            calendar.set(Calendar.HOUR_OF_DAY, 23);
            calendar.set(Calendar.MINUTE, 59);
            calendar.set(Calendar.SECOND, 59);
            calendar.set(Calendar.MILLISECOND, 999);
            assertEquals(period.getNextDate(now), calendar.getTime());
        }

        PeriodProvider provider = Period.getProvider(EndOfMonth.PERIOD_TYPE);
        assertEquals(provider.getDefaultExpression(), "1");
        assertEquals(provider.getExpressionMutiplicity(), PeriodProvider.ExpressionMutiplicity.OPTIONAL);
        assertNotNull(provider.getNextDate(now, "0"));
        assertNotNull(provider.getNextDate(now, "1"));
        assertNotNull(provider.getNextDate(now, "2"));
        assertEquals(provider.getPeriodType(), EndOfMonth.PERIOD_TYPE);
        assertEquals(DataTypeDefinition.INT, provider.getExpressionDataType());
        assertEquals("End Of Month", provider.getDisplayLabel());
    }

    /**
     * EndOfYear
     */
    public void test08EndOfYear()
    {
        assertTrue(Period.getProviderNames().contains(EndOfYear.PERIOD_TYPE));
        Period period = new Period(EndOfYear.PERIOD_TYPE);
        Date now = new Date();
        assertNotNull(period.getNextDate(now));
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(now);
        calendar.add(Calendar.YEAR, 1);
        calendar.set(Calendar.DAY_OF_YEAR, 1);
        calendar.add(Calendar.DAY_OF_YEAR, -1);
        calendar.set(Calendar.HOUR_OF_DAY, 23);
        calendar.set(Calendar.MINUTE, 59);
        calendar.set(Calendar.SECOND, 59);
        calendar.set(Calendar.MILLISECOND, 999);
        assertEquals(period.getNextDate(now), calendar.getTime());

        for (int i = -100; i < 100; i++)
        {
            period = new Period(EndOfYear.PERIOD_TYPE + "|" + i);
            assertNotNull(period.getNextDate(now));
            calendar.setTime(now);
            calendar.add(Calendar.YEAR, i);
            calendar.set(Calendar.DAY_OF_YEAR, 1);
            calendar.add(Calendar.DAY_OF_YEAR, -1);
            calendar.set(Calendar.HOUR_OF_DAY, 23);
            calendar.set(Calendar.MINUTE, 59);
            calendar.set(Calendar.SECOND, 59);
            calendar.set(Calendar.MILLISECOND, 999);
            assertEquals(period.getNextDate(now), calendar.getTime());
        }

        PeriodProvider provider = Period.getProvider(EndOfYear.PERIOD_TYPE);
        assertEquals(provider.getDefaultExpression(), "1");
        assertEquals(provider.getExpressionMutiplicity(), PeriodProvider.ExpressionMutiplicity.OPTIONAL);
        assertNotNull(provider.getNextDate(now, "0"));
        assertNotNull(provider.getNextDate(now, "1"));
        assertNotNull(provider.getNextDate(now, "2"));
        assertEquals(provider.getPeriodType(), EndOfYear.PERIOD_TYPE);
        assertEquals(DataTypeDefinition.INT, provider.getExpressionDataType());
        assertEquals("End Of Year", provider.getDisplayLabel());
    }

    /**
     * End of Quarter
     * 
     * @throws ParseException
     */
    public void test09EndOfQuarter() throws ParseException
    {
        PeriodProvider provider = Period.getProvider(EndOfQuarter.PERIOD_TYPE);
        assertEquals(provider.getDefaultExpression(), "1");
        assertEquals(provider.getExpressionMutiplicity(), PeriodProvider.ExpressionMutiplicity.OPTIONAL);
        assertEquals(provider.getPeriodType(), EndOfQuarter.PERIOD_TYPE);
        assertEquals(DataTypeDefinition.INT, provider.getExpressionDataType());
        assertEquals("End Of Quarter", provider.getDisplayLabel());

        SimpleDateFormat df = CachingDateFormat.getDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS", true);
        Period period = new Period(EndOfQuarter.PERIOD_TYPE);
        Date date;
        date = df.parse("2009-01-01T00:00:00.000");
        assertEquals("2009-03-31T23:59:59.999", df.format(period.getNextDate(date)));
        date = df.parse("2009-03-31T00:00:00.000");
        assertEquals("2009-03-31T23:59:59.999", df.format(period.getNextDate(date)));
        date = df.parse("2009-04-01T00:00:00.000");
        assertEquals("2009-06-30T23:59:59.999", df.format(period.getNextDate(date)));
        date = df.parse("2009-06-01T00:00:00.000");
        assertEquals("2009-06-30T23:59:59.999", df.format(period.getNextDate(date)));
        date = df.parse("2009-06-30T23:59:59.999");
        assertEquals("2009-09-30T23:59:59.999", df.format(period.getNextDate(date)));
        date = df.parse("2009-07-01T23:59:59.999");
        assertEquals("2009-09-30T23:59:59.999", df.format(period.getNextDate(date)));
        date = df.parse("2009-09-30T23:59:59.998");
        assertEquals("2009-09-30T23:59:59.999", df.format(period.getNextDate(date)));
        date = df.parse("2009-09-30T23:59:59.999");
        assertEquals("2009-12-31T23:59:59.999", df.format(period.getNextDate(date)));
        date = df.parse("2009-10-01T23:59:00.000");
        assertEquals("2009-12-31T23:59:59.999", df.format(period.getNextDate(date)));
        date = df.parse("2009-12-31T23:59:00.000");
        assertEquals("2009-12-31T23:59:59.999", df.format(period.getNextDate(date)));

        period = new Period(EndOfQuarter.PERIOD_TYPE + "|2");
        date = df.parse("2009-01-01T00:00:00.000");
        assertEquals("2009-06-30T23:59:59.999", df.format(period.getNextDate(date)));
        date = df.parse("2009-03-31T00:00:00.000");
        assertEquals("2009-06-30T23:59:59.999", df.format(period.getNextDate(date)));
        date = df.parse("2009-04-01T00:00:00.000");
        assertEquals("2009-09-30T23:59:59.999", df.format(period.getNextDate(date)));
        date = df.parse("2009-06-01T00:00:00.000");
        assertEquals("2009-09-30T23:59:59.999", df.format(period.getNextDate(date)));
        date = df.parse("2009-06-30T23:59:59.999");
        assertEquals("2009-12-31T23:59:59.999", df.format(period.getNextDate(date)));
        date = df.parse("2009-07-01T23:59:59.999");
        assertEquals("2009-12-31T23:59:59.999", df.format(period.getNextDate(date)));
        date = df.parse("2009-09-30T23:59:59.999");
        assertEquals("2010-03-31T23:59:59.999", df.format(period.getNextDate(date)));
        date = df.parse("2009-10-01T23:59:00.000");
        assertEquals("2010-03-31T23:59:59.999", df.format(period.getNextDate(date)));
        date = df.parse("2009-12-31T23:59:00.000");
        assertEquals("2010-03-31T23:59:59.999", df.format(period.getNextDate(date)));

        period = new Period(EndOfQuarter.PERIOD_TYPE + "|3");
        date = df.parse("2009-01-01T00:00:00.000");
        assertEquals("2009-09-30T23:59:59.999", df.format(period.getNextDate(date)));
        date = df.parse("2009-03-31T00:00:00.000");
        assertEquals("2009-09-30T23:59:59.999", df.format(period.getNextDate(date)));
        date = df.parse("2009-04-01T00:00:00.000");
        assertEquals("2009-12-31T23:59:59.999", df.format(period.getNextDate(date)));
        date = df.parse("2009-06-01T00:00:00.000");
        assertEquals("2009-12-31T23:59:59.999", df.format(period.getNextDate(date)));
        date = df.parse("2009-06-30T23:59:59.999");
        assertEquals("2010-03-31T23:59:59.999", df.format(period.getNextDate(date)));
        date = df.parse("2009-07-01T23:59:59.999");
        assertEquals("2010-03-31T23:59:59.999", df.format(period.getNextDate(date)));
        date = df.parse("2009-09-30T23:59:59.999");
        assertEquals("2010-06-30T23:59:59.999", df.format(period.getNextDate(date)));
        date = df.parse("2009-10-01T23:59:00.000");
        assertEquals("2010-06-30T23:59:59.999", df.format(period.getNextDate(date)));
        date = df.parse("2009-12-31T23:59:00.000");
        assertEquals("2010-06-30T23:59:59.999", df.format(period.getNextDate(date)));

        period = new Period(EndOfQuarter.PERIOD_TYPE + "|4");
        date = df.parse("2009-01-01T00:00:00.000");
        assertEquals("2009-12-31T23:59:59.999", df.format(period.getNextDate(date)));
        date = df.parse("2009-03-31T00:00:00.000");
        assertEquals("2009-12-31T23:59:59.999", df.format(period.getNextDate(date)));
        date = df.parse("2009-04-01T00:00:00.000");
        assertEquals("2010-03-31T23:59:59.999", df.format(period.getNextDate(date)));
        date = df.parse("2009-06-01T00:00:00.000");
        assertEquals("2010-03-31T23:59:59.999", df.format(period.getNextDate(date)));
        date = df.parse("2009-06-30T23:59:59.999");
        assertEquals("2010-06-30T23:59:59.999", df.format(period.getNextDate(date)));
        date = df.parse("2009-07-01T23:59:59.999");
        assertEquals("2010-06-30T23:59:59.999", df.format(period.getNextDate(date)));
        date = df.parse("2009-09-30T23:59:59.999");
        assertEquals("2010-09-30T23:59:59.999", df.format(period.getNextDate(date)));
        date = df.parse("2009-10-01T23:59:00.000");
        assertEquals("2010-09-30T23:59:59.999", df.format(period.getNextDate(date)));
        date = df.parse("2009-12-31T23:59:00.000");
        assertEquals("2010-09-30T23:59:59.999", df.format(period.getNextDate(date)));

        period = new Period(EndOfQuarter.PERIOD_TYPE + "|5");
        date = df.parse("2009-01-01T00:00:00.000");
        assertEquals("2010-03-31T23:59:59.999", df.format(period.getNextDate(date)));
        date = df.parse("2009-03-31T00:00:00.000");
        assertEquals("2010-03-31T23:59:59.999", df.format(period.getNextDate(date)));
        date = df.parse("2009-04-01T00:00:00.000");
        assertEquals("2010-06-30T23:59:59.999", df.format(period.getNextDate(date)));
        date = df.parse("2009-06-01T00:00:00.000");
        assertEquals("2010-06-30T23:59:59.999", df.format(period.getNextDate(date)));
        date = df.parse("2009-06-30T23:59:59.999");
        assertEquals("2010-09-30T23:59:59.999", df.format(period.getNextDate(date)));
        date = df.parse("2009-07-01T23:59:59.999");
        assertEquals("2010-09-30T23:59:59.999", df.format(period.getNextDate(date)));
        date = df.parse("2009-09-30T23:59:59.999");
        assertEquals("2010-12-31T23:59:59.999", df.format(period.getNextDate(date)));
        date = df.parse("2009-10-01T23:59:00.000");
        assertEquals("2010-12-31T23:59:59.999", df.format(period.getNextDate(date)));
        date = df.parse("2009-12-31T23:59:00.000");
        assertEquals("2010-12-31T23:59:59.999", df.format(period.getNextDate(date)));
    }

    /**
     * End of F Month For 1 Oct this is the same as month
     */
    public void test10EndOfDefaultFinancialMonth()
    {
        assertTrue(Period.getProviderNames().contains(EndOfFinancialMonth.PERIOD_TYPE));
        Period period = new Period(EndOfFinancialMonth.PERIOD_TYPE);
        
        PeriodProvider provider = Period.getProvider(EndOfFinancialMonth.PERIOD_TYPE);
        AbstractEndOfCalendarPeriodProvider instance = (AbstractEndOfCalendarPeriodProvider) provider;
        instance.setStartDayOfMonth(1);
        instance.setStartMonth(Calendar.JANUARY);
        
        Date now = new Date();
        assertNotNull(period.getNextDate(now));
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(now);
        calendar.add(Calendar.MONTH, 1);
        calendar.set(Calendar.DAY_OF_MONTH, 1);
        calendar.add(Calendar.DAY_OF_MONTH, -1);
        calendar.set(Calendar.HOUR_OF_DAY, 23);
        calendar.set(Calendar.MINUTE, 59);
        calendar.set(Calendar.SECOND, 59);
        calendar.set(Calendar.MILLISECOND, 999);
        assertEquals(period.getNextDate(now), calendar.getTime());

        for (int i = -100; i < 100; i++)
        {
            period = new Period(EndOfFinancialMonth.PERIOD_TYPE + "|" + i);
            assertNotNull(period.getNextDate(now));
            calendar.setTime(now);
            calendar.add(Calendar.MONTH, i);
            calendar.set(Calendar.DAY_OF_MONTH, 1);
            calendar.add(Calendar.DAY_OF_MONTH, -1);
            calendar.set(Calendar.HOUR_OF_DAY, 23);
            calendar.set(Calendar.MINUTE, 59);
            calendar.set(Calendar.SECOND, 59);
            calendar.set(Calendar.MILLISECOND, 999);
            assertEquals(period.getNextDate(now), calendar.getTime());
        }

        assertEquals(provider.getDefaultExpression(), "1");
        assertEquals(provider.getExpressionMutiplicity(), PeriodProvider.ExpressionMutiplicity.OPTIONAL);
        assertNotNull(provider.getNextDate(now, "0"));
        assertNotNull(provider.getNextDate(now, "1"));
        assertNotNull(provider.getNextDate(now, "2"));
        assertEquals(provider.getPeriodType(), EndOfFinancialMonth.PERIOD_TYPE);
        assertEquals(DataTypeDefinition.INT, provider.getExpressionDataType());
        assertEquals("End Of Financial Month", provider.getDisplayLabel());
    }

    /**
     * EndOfFinacialYear
     * 
     * @throws ParseException
     */
    public void test11EndOfDefaultFinancialYear() throws ParseException
    {
        PeriodProvider provider = Period.getProvider(EndOfFinancialYear.PERIOD_TYPE);
        assertEquals(provider.getDefaultExpression(), "1");
        assertEquals(provider.getExpressionMutiplicity(), PeriodProvider.ExpressionMutiplicity.OPTIONAL);
        assertEquals(provider.getPeriodType(), EndOfFinancialYear.PERIOD_TYPE);
        assertEquals(DataTypeDefinition.INT, provider.getExpressionDataType());
        assertEquals("End Of Financial Year", provider.getDisplayLabel());

        SimpleDateFormat df = CachingDateFormat.getDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS", true);
        Period period = new Period(EndOfFinancialYear.PERIOD_TYPE);
        Date date;
        date = df.parse("2008-10-01T00:00:00.000");
        assertEquals("2009-09-30T23:59:59.999", df.format(period.getNextDate(date)));
        date = df.parse("2009-09-30T23:59:59.998");
        assertEquals("2009-09-30T23:59:59.999", df.format(period.getNextDate(date)));
        date = df.parse("2009-09-30T23:59:59.999");
        assertEquals("2010-09-30T23:59:59.999", df.format(period.getNextDate(date)));

        period = new Period(EndOfFinancialYear.PERIOD_TYPE + "|10");
        date = df.parse("2008-10-01T00:00:00.000");
        assertEquals("2018-09-30T23:59:59.999", df.format(period.getNextDate(date)));
        date = df.parse("2009-09-30T23:59:59.998");
        assertEquals("2018-09-30T23:59:59.999", df.format(period.getNextDate(date)));
        date = df.parse("2009-09-30T23:59:59.999");
        assertEquals("2019-09-30T23:59:59.999", df.format(period.getNextDate(date)));

    }

    /**
     * End of FQ
     * 
     * @throws ParseException
     */
    public void test12EndOfDefaultFinancialQuarter() throws ParseException
    {
        PeriodProvider provider = Period.getProvider(EndOfFinancialQuarter.PERIOD_TYPE);
        assertEquals(provider.getDefaultExpression(), "1");
        assertEquals(provider.getExpressionMutiplicity(), PeriodProvider.ExpressionMutiplicity.OPTIONAL);
        assertEquals(provider.getPeriodType(), EndOfFinancialQuarter.PERIOD_TYPE);
        assertEquals(DataTypeDefinition.INT, provider.getExpressionDataType());
        assertEquals("End Of Financial Quarter", provider.getDisplayLabel());
        
        AbstractEndOfCalendarPeriodProvider instance = (AbstractEndOfCalendarPeriodProvider) provider;
        instance.setStartDayOfMonth(1);
        instance.setStartMonth(Calendar.JANUARY);

        SimpleDateFormat df = CachingDateFormat.getDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS", true);
        Period period = new Period(EndOfFinancialQuarter.PERIOD_TYPE);
        Date date;
        date = df.parse("2008-10-01T00:00:00.000");
        assertEquals("2008-12-31T23:59:59.999", df.format(period.getNextDate(date)));
        date = df.parse("2008-12-31T23:59:59.998");
        assertEquals("2008-12-31T23:59:59.999", df.format(period.getNextDate(date)));
        date = df.parse("2008-12-31T23:59:59.999");
        assertEquals("2009-03-31T23:59:59.999", df.format(period.getNextDate(date)));

        date = df.parse("2009-01-01T00:00:00.000");
        assertEquals("2009-03-31T23:59:59.999", df.format(period.getNextDate(date)));
        date = df.parse("2009-03-31T23:59:59.998");
        assertEquals("2009-03-31T23:59:59.999", df.format(period.getNextDate(date)));
        date = df.parse("2009-03-31T23:59:59.999");
        assertEquals("2009-06-30T23:59:59.999", df.format(period.getNextDate(date)));

        date = df.parse("2009-04-01T00:00:00.000");
        assertEquals("2009-06-30T23:59:59.999", df.format(period.getNextDate(date)));
        date = df.parse("2009-06-30T23:59:59.998");
        assertEquals("2009-06-30T23:59:59.999", df.format(period.getNextDate(date)));
        date = df.parse("2009-06-30T23:59:59.999");
        assertEquals("2009-09-30T23:59:59.999", df.format(period.getNextDate(date)));

        date = df.parse("2009-07-01T00:00:00.000");
        assertEquals("2009-09-30T23:59:59.999", df.format(period.getNextDate(date)));
        date = df.parse("2009-09-30T23:59:59.998");
        assertEquals("2009-09-30T23:59:59.999", df.format(period.getNextDate(date)));
        date = df.parse("2009-09-30T23:59:59.999");
        assertEquals("2009-12-31T23:59:59.999", df.format(period.getNextDate(date)));

        date = df.parse("2009-10-01T00:00:00.000");
        assertEquals("2009-12-31T23:59:59.999", df.format(period.getNextDate(date)));
        date = df.parse("2009-12-31T23:59:59.998");
        assertEquals("2009-12-31T23:59:59.999", df.format(period.getNextDate(date)));
        date = df.parse("2009-12-31T23:59:59.999");
        assertEquals("2010-03-31T23:59:59.999", df.format(period.getNextDate(date)));
    }

    /**
     * EndOfFinacialYear
     * 
     * @throws ParseException
     */
    public void test13EndOfUKTaxYear() throws ParseException
    {
        PeriodProvider provider = Period.getProvider(EndOfFinancialYear.PERIOD_TYPE);
        assertEquals(provider.getDefaultExpression(), "1");
        assertEquals(provider.getExpressionMutiplicity(), PeriodProvider.ExpressionMutiplicity.OPTIONAL);
        assertEquals(provider.getPeriodType(), EndOfFinancialYear.PERIOD_TYPE);
        assertEquals(DataTypeDefinition.INT, provider.getExpressionDataType());
        
        AbstractEndOfCalendarPeriodProvider instance = (AbstractEndOfCalendarPeriodProvider) provider;
        instance.setStartDayOfMonth(6);
        instance.setStartMonth(Calendar.APRIL);

        SimpleDateFormat df = CachingDateFormat.getDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS", true);
        Period period = new Period(EndOfFinancialYear.PERIOD_TYPE);
        Date date;
        date = df.parse("2008-4-06T00:00:00.000");
        assertEquals("2009-04-05T23:59:59.999", df.format(period.getNextDate(date)));
        date = df.parse("2009-04-05T23:59:59.998");
        assertEquals("2009-04-05T23:59:59.999", df.format(period.getNextDate(date)));
        date = df.parse("2009-04-05T23:59:59.999");
        assertEquals("2010-04-05T23:59:59.999", df.format(period.getNextDate(date)));

        period = new Period(EndOfFinancialYear.PERIOD_TYPE + "|10");
        date = df.parse("2008-4-06T00:00:00.000");
        assertEquals("2018-04-05T23:59:59.999", df.format(period.getNextDate(date)));
        date = df.parse("2009-04-05T23:59:59.998");
        assertEquals("2018-04-05T23:59:59.999", df.format(period.getNextDate(date)));
        date = df.parse("2009-04-05T23:59:59.999");
        assertEquals("2019-04-05T23:59:59.999", df.format(period.getNextDate(date)));

    }

    /**
     * End of FQ
     * 
     * @throws ParseException
     */
    public void test14EndOfUKTaxQuarter() throws ParseException
    {
        PeriodProvider provider = Period.getProvider(EndOfFinancialQuarter.PERIOD_TYPE);
        assertEquals(provider.getDefaultExpression(), "1");
        assertEquals(provider.getExpressionMutiplicity(), PeriodProvider.ExpressionMutiplicity.OPTIONAL);
        assertEquals(provider.getPeriodType(), EndOfFinancialQuarter.PERIOD_TYPE);
        assertEquals(DataTypeDefinition.INT, provider.getExpressionDataType());

        AbstractEndOfCalendarPeriodProvider instance = (AbstractEndOfCalendarPeriodProvider) provider;
        instance.setStartDayOfMonth(6);
        instance.setStartMonth(Calendar.APRIL);
        
        SimpleDateFormat df = CachingDateFormat.getDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS", true);
        Period period = new Period(EndOfFinancialQuarter.PERIOD_TYPE);
        Date date;
        date = df.parse("2008-04-06T00:00:00.000");
        assertEquals("2008-07-05T23:59:59.999", df.format(period.getNextDate(date)));
        date = df.parse("2008-07-05T23:59:59.998");
        assertEquals("2008-07-05T23:59:59.999", df.format(period.getNextDate(date)));
        date = df.parse("2008-07-05T23:59:59.999");
        assertEquals("2008-10-05T23:59:59.999", df.format(period.getNextDate(date)));

        date = df.parse("2008-07-06T00:00:00.000");
        assertEquals("2008-10-05T23:59:59.999", df.format(period.getNextDate(date)));
        date = df.parse("2008-10-05T23:59:59.998");
        assertEquals("2008-10-05T23:59:59.999", df.format(period.getNextDate(date)));
        date = df.parse("2008-10-05T23:59:59.999");
        assertEquals("2009-01-05T23:59:59.999", df.format(period.getNextDate(date)));

        date = df.parse("2008-10-06T00:00:00.000");
        assertEquals("2009-01-05T23:59:59.999", df.format(period.getNextDate(date)));
        date = df.parse("2009-01-05T23:59:59.998");
        assertEquals("2009-01-05T23:59:59.999", df.format(period.getNextDate(date)));
        date = df.parse("2009-01-05T23:59:59.999");
        assertEquals("2009-04-05T23:59:59.999", df.format(period.getNextDate(date)));

        date = df.parse("2009-01-06T00:00:00.000");
        assertEquals("2009-04-05T23:59:59.999", df.format(period.getNextDate(date)));
        date = df.parse("2009-04-05T23:59:59.998");
        assertEquals("2009-04-05T23:59:59.999", df.format(period.getNextDate(date)));
        date = df.parse("2009-04-05T23:59:59.999");
        assertEquals("2009-07-05T23:59:59.999", df.format(period.getNextDate(date)));

        date = df.parse("2009-04-06T00:00:00.000");
        assertEquals("2009-07-05T23:59:59.999", df.format(period.getNextDate(date)));
        date = df.parse("2009-07-05T23:59:59.998");
        assertEquals("2009-07-05T23:59:59.999", df.format(period.getNextDate(date)));
        date = df.parse("2009-07-05T23:59:59.999");
        assertEquals("2009-10-05T23:59:59.999", df.format(period.getNextDate(date)));
    }

    /**
     * End of FQ
     * 
     * @throws ParseException
     */
    public void test15EndOfUKTaxMonth() throws ParseException
    {
        PeriodProvider provider = Period.getProvider(EndOfFinancialMonth.PERIOD_TYPE);
        assertEquals(provider.getDefaultExpression(), "1");
        assertEquals(provider.getExpressionMutiplicity(), PeriodProvider.ExpressionMutiplicity.OPTIONAL);
        assertEquals(provider.getPeriodType(), EndOfFinancialMonth.PERIOD_TYPE);
        assertEquals(DataTypeDefinition.INT, provider.getExpressionDataType());

        AbstractEndOfCalendarPeriodProvider instance = (AbstractEndOfCalendarPeriodProvider) provider;
        instance.setStartDayOfMonth(6);
        instance.setStartMonth(Calendar.APRIL);
        
        SimpleDateFormat df = CachingDateFormat.getDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS", true);
        Period period = new Period(EndOfFinancialMonth.PERIOD_TYPE);
        Date date;
        date = df.parse("2008-04-06T00:00:00.000");
        assertEquals("2008-05-05T23:59:59.999", df.format(period.getNextDate(date)));
        date = df.parse("2008-05-05T23:59:59.998");
        assertEquals("2008-05-05T23:59:59.999", df.format(period.getNextDate(date)));
        date = df.parse("2008-05-05T23:59:59.999");
        assertEquals("2008-06-05T23:59:59.999", df.format(period.getNextDate(date)));
        
        date = df.parse("2008-05-06T00:00:00.000");
        assertEquals("2008-06-05T23:59:59.999", df.format(period.getNextDate(date)));
        date = df.parse("2008-06-05T23:59:59.998");
        assertEquals("2008-06-05T23:59:59.999", df.format(period.getNextDate(date)));
        date = df.parse("2008-06-05T23:59:59.999");
        assertEquals("2008-07-05T23:59:59.999", df.format(period.getNextDate(date)));
        
        date = df.parse("2008-06-06T00:00:00.000");
        assertEquals("2008-07-05T23:59:59.999", df.format(period.getNextDate(date)));
        date = df.parse("2008-07-05T23:59:59.998");
        assertEquals("2008-07-05T23:59:59.999", df.format(period.getNextDate(date)));
        date = df.parse("2008-07-05T23:59:59.999");
        assertEquals("2008-08-05T23:59:59.999", df.format(period.getNextDate(date)));
        
        date = df.parse("2008-07-06T00:00:00.000");
        assertEquals("2008-08-05T23:59:59.999", df.format(period.getNextDate(date)));
        date = df.parse("2008-08-05T23:59:59.998");
        assertEquals("2008-08-05T23:59:59.999", df.format(period.getNextDate(date)));
        date = df.parse("2008-08-05T23:59:59.999");
        assertEquals("2008-09-05T23:59:59.999", df.format(period.getNextDate(date)));
        
        date = df.parse("2008-08-06T00:00:00.000");
        assertEquals("2008-09-05T23:59:59.999", df.format(period.getNextDate(date)));
        date = df.parse("2008-09-05T23:59:59.998");
        assertEquals("2008-09-05T23:59:59.999", df.format(period.getNextDate(date)));
        date = df.parse("2008-09-05T23:59:59.999");
        assertEquals("2008-10-05T23:59:59.999", df.format(period.getNextDate(date)));
        
        date = df.parse("2008-09-06T00:00:00.000");
        assertEquals("2008-10-05T23:59:59.999", df.format(period.getNextDate(date)));
        date = df.parse("2008-10-05T23:59:59.998");
        assertEquals("2008-10-05T23:59:59.999", df.format(period.getNextDate(date)));
        date = df.parse("2008-10-05T23:59:59.999");
        assertEquals("2008-11-05T23:59:59.999", df.format(period.getNextDate(date)));
        
        date = df.parse("2008-10-06T00:00:00.000");
        assertEquals("2008-11-05T23:59:59.999", df.format(period.getNextDate(date)));
        date = df.parse("2008-11-05T23:59:59.998");
        assertEquals("2008-11-05T23:59:59.999", df.format(period.getNextDate(date)));
        date = df.parse("2008-11-05T23:59:59.999");
        assertEquals("2008-12-05T23:59:59.999", df.format(period.getNextDate(date)));
        
        date = df.parse("2008-11-06T00:00:00.000");
        assertEquals("2008-12-05T23:59:59.999", df.format(period.getNextDate(date)));
        date = df.parse("2008-12-05T23:59:59.998");
        assertEquals("2008-12-05T23:59:59.999", df.format(period.getNextDate(date)));
        date = df.parse("2008-12-05T23:59:59.999");
        assertEquals("2009-01-05T23:59:59.999", df.format(period.getNextDate(date)));
        
        date = df.parse("2008-12-06T00:00:00.000");
        assertEquals("2009-01-05T23:59:59.999", df.format(period.getNextDate(date)));
        date = df.parse("2009-01-05T23:59:59.998");
        assertEquals("2009-01-05T23:59:59.999", df.format(period.getNextDate(date)));
        date = df.parse("2009-01-05T23:59:59.999");
        assertEquals("2009-02-05T23:59:59.999", df.format(period.getNextDate(date)));

        date = df.parse("2009-01-06T00:00:00.000");
        assertEquals("2009-02-05T23:59:59.999", df.format(period.getNextDate(date)));
        date = df.parse("2009-02-05T23:59:59.998");
        assertEquals("2009-02-05T23:59:59.999", df.format(period.getNextDate(date)));
        date = df.parse("2009-02-05T23:59:59.999");
        assertEquals("2009-03-05T23:59:59.999", df.format(period.getNextDate(date)));

        date = df.parse("2009-02-06T00:00:00.000");
        assertEquals("2009-03-05T23:59:59.999", df.format(period.getNextDate(date)));
        date = df.parse("2009-03-05T23:59:59.998");
        assertEquals("2009-03-05T23:59:59.999", df.format(period.getNextDate(date)));
        date = df.parse("2009-03-05T23:59:59.999");
        assertEquals("2009-04-05T23:59:59.999", df.format(period.getNextDate(date)));
        
        date = df.parse("2009-03-06T00:00:00.000");
        assertEquals("2009-04-05T23:59:59.999", df.format(period.getNextDate(date)));
        date = df.parse("2009-04-05T23:59:59.998");
        assertEquals("2009-04-05T23:59:59.999", df.format(period.getNextDate(date)));
        date = df.parse("2009-04-05T23:59:59.999");
        assertEquals("2009-05-05T23:59:59.999", df.format(period.getNextDate(date)));

        date = df.parse("2009-04-06T00:00:00.000");
        assertEquals("2009-05-05T23:59:59.999", df.format(period.getNextDate(date)));
        date = df.parse("2009-05-05T23:59:59.998");
        assertEquals("2009-05-05T23:59:59.999", df.format(period.getNextDate(date)));
        date = df.parse("2009-05-05T23:59:59.999");
        assertEquals("2009-06-05T23:59:59.999", df.format(period.getNextDate(date)));
    }
    
    /**
     * Cron
     * @throws ParseException 
     */
    public void test16Cron() throws ParseException
    {
        PeriodProvider provider = Period.getProvider(Cron.PERIOD_TYPE);
        assertEquals(provider.getDefaultExpression(), "59 59 23 * * ?");
        assertEquals(provider.getExpressionMutiplicity(), PeriodProvider.ExpressionMutiplicity.MANDATORY);
        assertEquals(provider.getPeriodType(), Cron.PERIOD_TYPE);
        assertEquals(DataTypeDefinition.TEXT, provider.getExpressionDataType());
        assertEquals("Cron Expression", provider.getDisplayLabel());
        
        SimpleDateFormat df = CachingDateFormat.getDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS", true);
        Period period = new Period(Cron.PERIOD_TYPE);
        Date date;
        date = df.parse("2009-06-02T00:00:00.000");
        assertEquals("2009-06-02T23:59:59.000", df.format(period.getNextDate(date)));
        
        period = new Period(Cron.PERIOD_TYPE+"|0 0 2 ? * 6#1"); // First Friday of month
        assertEquals("2009-06-05T02:00:00.000", df.format(period.getNextDate(date)));
    }

    /**
     * XMLDuration
     * @throws ParseException
     */
    public void test17XMLDuration() throws ParseException
    {
        PeriodProvider provider = Period.getProvider(XMLDuration.PERIOD_TYPE);
        assertEquals(provider.getDefaultExpression(), "P1D");
        assertEquals(provider.getExpressionMutiplicity(), PeriodProvider.ExpressionMutiplicity.MANDATORY);
        assertEquals(provider.getPeriodType(), XMLDuration.PERIOD_TYPE);
        assertEquals(DataTypeDefinition.TEXT, provider.getExpressionDataType());
        assertEquals("XML Duration", provider.getDisplayLabel());
        
        SimpleDateFormat df = CachingDateFormat.getDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS", true);
        Period period = new Period(XMLDuration.PERIOD_TYPE);
        Date date;
        date = df.parse("2009-06-02T01:02:03.004");
        assertEquals("2009-06-03T01:02:03.004", df.format(period.getNextDate(date)));
        
        period = new Period(XMLDuration.PERIOD_TYPE+"|P2Y6M5DT12H35M30.100S"); // First Friday of month
        assertEquals("2011-12-07T13:37:33.104", df.format(period.getNextDate(date)));
    }
    
    /**
     * Test a missing expression, ensure 1 is used as the default
     */
    public void test18MissingExpression()
    {
        assertTrue(Period.getProviderNames().contains(Days.PERIOD_TYPE));
        Date now = new Date();
        Calendar calendar = Calendar.getInstance();

        Period period = new Period(Days.PERIOD_TYPE + "|");
        assertNotNull(period.getNextDate(now));
        calendar.setTime(now);
        calendar.add(Calendar.DAY_OF_YEAR, 1);
        assertEquals(period.getNextDate(now), calendar.getTime());
    }
}
