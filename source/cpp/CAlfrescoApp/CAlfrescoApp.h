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