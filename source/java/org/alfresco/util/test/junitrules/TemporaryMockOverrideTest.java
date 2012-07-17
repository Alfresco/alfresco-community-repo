/*
 * Copyright (C) 2005-2012 Alfresco Software Limited.
 *
 * This file is part of Alfresco
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
 */

package org.alfresco.util.test.junitrules;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;

/**
 * Test class for {@link TemporaryMockOverride}.
 * 
 * @author Neil McErlean
 * @since Odin
 */
public class TemporaryMockOverrideTest
{
    private static final String REAL_DATA   = "Hello";
    private static final String MOCKED_DATA = "--";
    
    private final FooService realFooService = new FooServiceImpl();
    private final BarService realBarService = new BarServiceImpl();
    private final AbcService realAbcService = new AbcServiceImpl();
    
    @Before public void init()
    {
        FooServiceImpl fooServiceImpl = (FooServiceImpl)realFooService;
        fooServiceImpl.setBarService(realBarService);
        fooServiceImpl.setAbcService(realAbcService);
    }
    
    @Test public void mockFieldsWithinServiceAndThenEnsureTheProperFieldValuesAreRestoredAfterCleanup() throws Throwable
    {
        TemporaryMockOverride mockRule = new TemporaryMockOverride();
        mockRule.before();
        
        assertEquals("Original BarService giving wrong data.", REAL_DATA, realFooService.getBarService().getString());
        assertEquals("Original AbcService giving wrong data.", REAL_DATA, realFooService.getAbcService().getString());
        
        BarService mockedBarService = mock(BarService.class);
        when(mockedBarService.getString()).thenReturn(MOCKED_DATA);
        
        AbcService mockedAbcService = mock(AbcService.class);
        when(mockedAbcService.getString()).thenReturn(MOCKED_DATA);
        
        FooServiceImpl fooServiceWithMockedServices = new FooServiceImpl();
        // We'll start it off with the 'correct' values
        fooServiceWithMockedServices.setBarService(realBarService);
        fooServiceWithMockedServices.setAbcService(realAbcService);
        
        // ...and then set the mocked values via the rule, which will remember the old values and revert them for us automatically.
        mockRule.setTemporaryField(fooServiceWithMockedServices, "barService", mockedBarService);
        mockRule.setTemporaryField(fooServiceWithMockedServices, "abcService", mockedAbcService);
        
        
        assertEquals("Mocked BarService giving wrong data.", MOCKED_DATA, fooServiceWithMockedServices.getBarService().getString());
        assertEquals("Mocked AbcService giving wrong data.", MOCKED_DATA, fooServiceWithMockedServices.getAbcService().getString());
        
        mockRule.after();
        
        // Now it should all be magically reverted.
        assertEquals("BarService giving wrong data.", REAL_DATA, fooServiceWithMockedServices.getBarService().getString());
        assertEquals("AbcService giving wrong data.", REAL_DATA, fooServiceWithMockedServices.getAbcService().getString());
    }
    
    @Test(expected=IllegalArgumentException.class) public void mockNonExistentFieldsWithinService() throws Throwable
    {
        TemporaryMockOverride mockRule = new TemporaryMockOverride();
        mockRule.before();
        
        assertEquals("Original BarService giving wrong data.", REAL_DATA, realFooService.getBarService().getString());
        
        BarService mockedBarService = mock(BarService.class);
        when(mockedBarService.getString()).thenReturn(MOCKED_DATA);
        
        FooServiceImpl fooServiceWithMockedServices = new FooServiceImpl();
        // We'll start it off with the 'correct' values
        fooServiceWithMockedServices.setBarService(realBarService);
        
        // ...and then set an illegal mocked value via the rule.
        mockRule.setTemporaryField(fooServiceWithMockedServices, "noSuchService", mockedBarService);
        
        
        assertEquals("Mocked BarService giving wrong data.", MOCKED_DATA, fooServiceWithMockedServices.getBarService().getString());
        
        mockRule.after();
        
        // Now it should all be magically reverted.
        assertEquals("BarService giving wrong data.", REAL_DATA, fooServiceWithMockedServices.getBarService().getString());
    }
    
    public interface FooService { public BarService getBarService(); public AbcService getAbcService(); }
    
    public class FooServiceImpl implements FooService
    {
        private BarService barService;
        private AbcService abcService;
        
        public BarService getBarService()                { return this.barService; }
        public void setBarService(BarService barService) { this.barService = barService; }
        public AbcService getAbcService()                { return this.abcService; }
        public void setAbcService(AbcService abcService) { this.abcService = abcService; }
    }
    
    public interface BarService { public String getString(); }
    
    public class BarServiceImpl implements BarService { public String getString() { return REAL_DATA; } }
    
    public interface AbcService { public String getString(); }
    
    public class AbcServiceImpl implements AbcService { public String getString() { return REAL_DATA; } }
}

