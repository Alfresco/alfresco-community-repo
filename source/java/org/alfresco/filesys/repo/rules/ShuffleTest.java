/*
 * Copyright (C) 2005-2010 Alfresco Software Limited.
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
package org.alfresco.filesys.repo.rules;


import java.util.ArrayList;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import junit.framework.TestCase;

/**
 * Shuffle Test
 */
//public class ShuffleTest extends TestCase
//{
//    private static Log logger = LogFactory.getLog(ShuffleTest.class);
//
//    /**
//     * A single shuffle
//     */
//    public void testSingleShuffle()
//    {
//        logger.debug("testA start");
//        RuleEvaluatorImpl evaluator = new RuleEvaluatorImpl();
//        
//        CommandExecutorImpl executor = new CommandExecutorImpl();
//        
//        ScenarioCreateShuffle shuffle1 = new ScenarioCreateShuffle();
//        shuffle1.setPattern("~WRD.*.TMP");
//        ScenarioSimpleNonBuffered bogStandard = new ScenarioSimpleNonBuffered();
//        ArrayList<Scenario> scenarios = new ArrayList<Scenario>();
//  
//        scenarios.add(bogStandard);
//        scenarios.add(shuffle1);
//
//        evaluator.setScenarios(scenarios);
//        
//        /**
//         * Bog standard create
//         */
//        Operation c = new CreateFileOperation("MER.TXT");
//        Command x = evaluator.evaluate(c);
//        executor.execute(x);
//        
//        assertTrue(x instanceof SimpleNonBufferedCommand);
//    
//        /**
//         * A simple shuffle
//         */
//        Operation c2 = new CreateFileOperation("~WRD0001.TMP");
//        x = evaluator.evaluate(c2);
//        executor.execute(x);
//        assertTrue(x instanceof SimpleNonBufferedCommand);
//        
//        Operation r1 = new RenameFileOperation("X", "Y");
//        x = evaluator.evaluate(r1);
//        executor.execute(x);
//        
//        Operation r2 = new RenameFileOperation("~WRD0001.TMP", "X");
//        x = evaluator.evaluate(r2);
//        executor.execute(x);
//        
//        Operation d1 = new DeleteFileOperation("Y");
//        x = evaluator.evaluate(d1);
//        executor.execute(x);
//        
//        assertTrue(x instanceof CompoundCommand);
//        
//        
//      
//    }
//    
//    /**
//     * Two shuffles inter-twined
//     */
//    public void testDualShuffle()
//    {
//        logger.debug("test dual shuffle ");
//        RuleEvaluatorImpl evaluator = new RuleEvaluatorImpl();
//        CommandExecutorImpl executor = new CommandExecutorImpl();
//        
//        ScenarioCreateShuffle shuffle1 = new ScenarioCreateShuffle();
//        shuffle1.setPattern("~WRD.*.TMP");
//        ScenarioSimpleNonBuffered bogStandard = new ScenarioSimpleNonBuffered();
//        ArrayList<Scenario> scenarios = new ArrayList<Scenario>();
//  
//        scenarios.add(bogStandard);
//        scenarios.add(shuffle1);
//
//        evaluator.setScenarios(scenarios);
//        
//        /**
//         * Bog standard create
//         */
//        Operation c = new CreateFileOperation("MER.TXT");
//        Command x = evaluator.evaluate(c);
//        executor.execute(x);
//    
//        /**
//         * Two shuffles interleaved
//         */
//        Operation c2 = new CreateFileOperation("~WRD0001.TMP");
//        x = evaluator.evaluate(c2);
//        executor.execute(x);
//        
//        Operation c3 = new CreateFileOperation("~WRD0002.TMP");
//        x = evaluator.evaluate(c3);
//        executor.execute(x);
//        
//        Operation r1 = new RenameFileOperation("X", "Y");
//        x = evaluator.evaluate(r1);
//        executor.execute(x);
//        
//        Operation r2 = new RenameFileOperation("A", "B");
//        x = evaluator.evaluate(r2);
//        executor.execute(x);
//        
//        Operation r3 = new RenameFileOperation("~WRD0001.TMP", "X");
//        x = evaluator.evaluate(r3);
//        executor.execute(x);
//        
//        Operation r4 = new RenameFileOperation("~WRD0002.TMP", "A");
//        x = evaluator.evaluate(r4);
//        executor.execute(x);
//        
//        Operation d1 = new DeleteFileOperation("Y");
//        x = evaluator.evaluate(d1);
//        executor.execute(x);
//        
//        Operation d2 = new DeleteFileOperation("B");
//        x = evaluator.evaluate(d2);
//        executor.execute(x);
//      
//    }
//    
//    /**
//     * Two shuffles inter-twined
//     */
//    public void testViShuffle()
//    {
//        logger.debug("test vi shuffle ");
//        RuleEvaluatorImpl evaluator = new RuleEvaluatorImpl();
//        
//        CommandExecutorImpl executor = new CommandExecutorImpl();
//        
//        ScenarioCreateShuffle shuffle1 = new ScenarioCreateShuffle();
//        shuffle1.setPattern("~WRD.*.TMP");
//        
//        // Search for files ending with tilda.
//        ScenarioRenameShuffle shuffle2 = new ScenarioRenameShuffle();
//        shuffle2.setPattern(".*~$");
//        
//        ScenarioSimpleNonBuffered bogStandard = new ScenarioSimpleNonBuffered();
//        ArrayList<Scenario> scenarios = new ArrayList<Scenario>();
//  
//        scenarios.add(bogStandard);
//        scenarios.add(shuffle1);
//        scenarios.add(shuffle2);
//
//        evaluator.setScenarios(scenarios);
//              
//        Operation r1 = new RenameFileOperation("X", "X~");
//        Command ex = evaluator.evaluate(r1);
//        //ex.execute(r1);
//        
//        Operation c = new CreateFileOperation("X");
//        ex = evaluator.evaluate(c);
//        //ex.execute(c); 
//          
//    }
//
//    
//    /**
//     * A negative tests
//     */
//    public void testAntiPattern()
//    {
//        logger.debug("test Anti-Pattern start");
//        RuleEvaluatorImpl evaluator = new RuleEvaluatorImpl();
//        
//        CommandExecutorImpl executor = new CommandExecutorImpl();
//        
//        ScenarioCreateShuffle shuffle1 = new ScenarioCreateShuffle();
//        shuffle1.setPattern("~WRD.*.TMP");
//        ScenarioSimpleNonBuffered bogStandard = new ScenarioSimpleNonBuffered();
//        ArrayList<Scenario> scenarios = new ArrayList<Scenario>();
//  
//        scenarios.add(bogStandard);
//        scenarios.add(shuffle1);
//
//        evaluator.setScenarios(scenarios);
//        
//        /**
//         * Bog standard create
//         */
//        Operation c = new CreateFileOperation("MER.TXT");
//        Command ex = evaluator.evaluate(c);
//        //ex.execute(c);
//    
//        /**
//         * A simple shuffle
//         */
//        Operation c2 = new CreateFileOperation("~WRD0001.TMP");
//        ex = evaluator.evaluate(c2);
//        //ex.execute(c2);
//        
//        Operation d0 = new DeleteFileOperation("~WRD0001.TMP");
//        ex = evaluator.evaluate(d0);
//        //ex.execute(d0);
//        
//        Operation r1 = new RenameFileOperation("X", "Y");
//        ex = evaluator.evaluate(r1);
//        //ex.execute(r1);
//        
//        Operation r2 = new RenameFileOperation("~WRD0001.TMP", "X");
//        ex = evaluator.evaluate(r2);
//        //ex.execute(r2);
//        
//        Operation d1 = new DeleteFileOperation("Y");
//        ex = evaluator.evaluate(d1);
//        //ex.execute(d1); 
//    }    
//    
//    
//    /**
//     * A multi threaded shuffle
//     */
//    public void testMultiThreadShuffle() throws Exception
//    {
//        
//        logger.debug("test multi thread shuffle start");
//        int MAX_THREADS=10;
//        
//        CommandExecutorImpl executor = new CommandExecutorImpl();
//        
//        RuleEvaluatorImpl evaluator = new RuleEvaluatorImpl();
//      
//        ScenarioCreateShuffle shuffle1 = new ScenarioCreateShuffle();
//        shuffle1.setPattern("~WRD.*.TMP");
//        ScenarioSimpleNonBuffered bogStandard = new ScenarioSimpleNonBuffered();
//        ArrayList<Scenario> scenarios = new ArrayList<Scenario>();
//
//        scenarios.add(bogStandard);
//        scenarios.add(shuffle1);
//
//        evaluator.setScenarios(scenarios);
//        
//        class TestThread extends Thread
//        {
//            String name;
//            RuleEvaluator evaluator;
//            Exception exception;
//            
//            public TestThread( RuleEvaluator evaluator, String name)
//            {
//                this.name = name;
//                this.evaluator = evaluator;
//            }
//            
//            public void run()
//            {
//                String targetName = name;
//                String createName = "~WRD" + name + ".TMP"; 
//                String deleteName = name;
//                try
//                {
//                    /**
//                     * Bog standard create
//                     */
//                    Operation c = new CreateFileOperation("MER.TXT");
//                    Command ex = evaluator.evaluate(c);
//                    //ex.execute(c);
//                    System.out.println("DONE: " + name);
//
//                    /**
//                     * A simple shuffle
//                     */
//                    Operation c2 = new CreateFileOperation(createName);
//                    ex = evaluator.evaluate(c2);
//                    //ex.execute(c2);
//
//                    Operation r1 = new RenameFileOperation(targetName, deleteName);
//                    ex = evaluator.evaluate(r1);
//                    //ex.execute(r1);
//
//                    Operation r2 = new RenameFileOperation(createName, targetName);
//                    ex = evaluator.evaluate(r2);
//                    //ex.execute(r2);
//
//                    Operation d1 = new DeleteFileOperation(deleteName);
//                    ex = evaluator.evaluate(d1);
//                    //ex.execute(d1);
//                }
//                catch (Exception e)
//                {
//                    exception = e;
//                }
//
//            }
//        }
//        
//        ArrayList<TestThread>threads = new ArrayList<TestThread>();
//        
//        for(int i = 0; i < MAX_THREADS; i++)
//        {
//            TestThread t = new TestThread(evaluator, "~WRD" + i + ".TMP");
//            threads.add(t);
//            t.start();
//        }
//        
//        for(TestThread thread : threads)
//        {
//            try
//            {
//                thread.join();
//                if(thread.exception != null)
//                {
//                    throw thread.exception;
//                }
//            } 
//            catch (InterruptedException e)
//            {
//                // TODO Auto-generated catch block
//                e.printStackTrace();
//            }
//        }      
//    }    
//}
