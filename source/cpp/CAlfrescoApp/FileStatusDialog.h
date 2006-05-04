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
#include "afxcmn.h"

#include "alfresco\Alfresco.hpp"

// CFileStatusDialog dialog

class CFileStatusDialog : public CDialog
{
	DECLARE_DYNAMIC(CFileStatusDialog)

public:
	CFileStatusDialog( AlfrescoFileInfoList& fileList, CWnd* pParent = NULL);   // standard constructor
	virtual ~CFileStatusDialog();

// Dialog Data
	enum { IDD = IDD_FILESTATUS };

	// Initialize the dialog

	BOOL OnInitDialog();

protected:
	virtual void DoDataExchange(CDataExchange* pDX);    // DDX/DDV support

	DECLARE_MESSAGE_MAP()
	CListCtrl m_listCtrl;

protected:
	// File information list

	AlfrescoFileInfoList& m_fileList;
};
