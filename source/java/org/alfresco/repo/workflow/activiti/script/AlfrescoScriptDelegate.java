/*
 * Copyright (C) 2005-2011 Alfresco Software Limited.
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

package org.alfresco.repo.workflow.activiti.script;

import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.engine.delegate.JavaDelegate;
import org.alfresco.service.cmr.repository.ScriptService;

/**
 * A {@link JavaDelegate} that executes a script against the {@link ScriptService}.
 * 
 * The script that is executed can be set using field 'script'. A non-default 
 * script-processor can be set in the field 'scriptProcessor'. Optionally, you can run 
 * the script as a different user than the default by setting the field 'runAs'. 
 * By default, the user this script as the current logged-in user. If no user is 
 * currently logged in (eg. flow triggered by timer) the system user will be used instead.
 * 
 * @author Frederik Heremans
 * @since 3.5
 */
public class AlfrescoScriptDelegate extends DelegateExecutionScriptBase implements JavaDelegate {

	@Override
	public void execute(DelegateExecution execution) throws Exception {
		runScript(execution);
	}
}
