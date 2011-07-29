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
/**
 * Filesystem Rule Evaluator to support Scenarios.
 * <p>
 * Low level operations (create, update, delete etc) are passed into the RuleEvaluator.
 * <p>
 * The RuleEvaluator is configured with a list of Scenarios which process the operations as and 
 * when their patterns match.
 * The RuleEvaluator evaluates the stream of operations and returns commands to execute.
 * <p>
 * The Command Executor executes the commands returned from the RuleEvaluator.
 * <p>
 * Each Scenario is a Factory for A ScenarioInstance.  The RuleEvaluator contains a set of active scenario instances.
 * <p>
 * @since 4.0
 */
package org.alfresco.filesys.repo.rules;
