/*
 * Copyright (C) 2005-2006 Alfresco, Inc.
 *
 * Licensed under the Mozilla Public License version 1.1 
 * with a permitted attribution clause. You may obtain a
 * copy of the License at
 *
 *   http://www.alfresco.org/legal/license.txt
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific
 * language governing permissions and limitations under the
 * License.
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

// CCAlfrescoAppApp:
// See CAlfrescoApp.cpp for the implementation of this class
//

class CCAlfrescoAppApp : public CWinApp
{
public:
	CCAlfrescoAppApp();

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
};

extern CCAlfrescoAppApp theApp;