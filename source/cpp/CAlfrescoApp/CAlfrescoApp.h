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

	bool doFolderStatus( AlfrescoInterface& alfresco, const wchar_t* fileSpec = L"*.*");
	bool doCheckInOut( AlfrescoInterface& alfresco, StringList& files);
	bool doCheckIn( AlfrescoInterface& alfresco, PTR_AlfrescoFileInfo& fileInfo);
	bool doCheckOut( AlfrescoInterface& alfresco, PTR_AlfrescoFileInfo& fileInfo);
};

extern CCAlfrescoAppApp theApp;