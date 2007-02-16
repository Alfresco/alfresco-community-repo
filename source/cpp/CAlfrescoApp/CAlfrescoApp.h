/*
 * Copyright (C) 2005-2006 Alfresco, Inc.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.

 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.

 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.

 * As a special exception to the terms and conditions of version 2.0 of 
 * the GPL, you may redistribute this Program in connection with Free/Libre 
 * and Open Source Software ("FLOSS") applications as described in Alfresco's 
 * FLOSS exception.  You should have recieved a copy of the text describing 
 * the FLOSS exception, and it is also available here: 
 * http://www.alfresco.com/legal/licensing"
 */

#pragma once

#ifndef __AFXWIN_H__
	#error include 'stdafx.h' before including this file for PCH
#endif

#include "resource.h"		// main symbols

// Includes

#include "alfresco\Alfresco.hpp"
#include "alfresco\Desktop.hpp"

using namespace Alfresco;

// CAlfrescoApp:
// See CAlfrescoApp.cpp for the implementation of this class
//

class CAlfrescoApp : public CWinApp
{
public:
	CAlfrescoApp();

// Overrides
	public:
	virtual BOOL InitInstance();

// Implementation

	DECLARE_MESSAGE_MAP()

private:
	// Main Alfresco interface functions

	bool buildDesktopParameters( AlfrescoInterface& alfresco, StringList& paths, AlfrescoActionInfo& actionInfo, DesktopParams& params);

	// Copy files/folders using the Windows shell

	bool copyFilesUsingShell(const String& fromPath, const String& toPath, bool& aborted);

	// Run the action

	bool runAction( AlfrescoInterface& alfresco, StringList& pathList, AlfrescoActionInfo& actionInfo);

	// Post-process actions, command line launch and browse to URL

	bool doCommandLine( AlfrescoInterface& alfresco, const String& cmdLine);
	bool doURL( AlfrescoInterface& alfresco, const String&  url);
};

extern CAlfrescoApp theApp;