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

#include "stdafx.h"
#include "CAlfrescoApp.h"
#include "FileStatusDialog.h"

#include "util\Long.h"

// CFileStatusDialog dialog

IMPLEMENT_DYNAMIC(CFileStatusDialog, CDialog)
CFileStatusDialog::CFileStatusDialog(AlfrescoFileInfoList& fileList, CWnd* pParent /*=NULL*/)
	: CDialog(CFileStatusDialog::IDD, pParent),
	m_fileList( fileList)
{
}

CFileStatusDialog::~CFileStatusDialog()
{
}

void CFileStatusDialog::DoDataExchange(CDataExchange* pDX)
{
	CDialog::DoDataExchange(pDX);
	DDX_Control(pDX, IDC_FILELIST, m_listCtrl);
}


BEGIN_MESSAGE_MAP(CFileStatusDialog, CDialog)
END_MESSAGE_MAP()

/**
 * Initialize the dialog
 */
BOOL CFileStatusDialog::OnInitDialog() {

	// Call the base class

	CDialog::OnInitDialog();

	// Add headers to the list control

	m_listCtrl.InsertColumn( 0, L"Name", LVCFMT_LEFT, 200);
	m_listCtrl.InsertColumn( 1, L"Mime-type", LVCFMT_LEFT, 140);
	m_listCtrl.InsertColumn( 2, L"Size", LVCFMT_RIGHT, 80);
	m_listCtrl.InsertColumn( 3, L"Status", LVCFMT_LEFT, 100);
	m_listCtrl.InsertColumn( 4, L"Owner", LVCFMT_LEFT, 100);

	// Add the list view data

	for ( unsigned int i = 0; i < m_fileList.size(); i++) {

		// Get the current file information

		const AlfrescoFileInfo* pInfo = m_fileList.getInfoAt( i);

		// Add the item to the list view
		
		if ( pInfo != NULL) {

			// Insert a new item in the view

			int nIndex = m_listCtrl.InsertItem( 0, pInfo->getName());

			if ( pInfo->isType() == TypeFile) {

				// Display the mime-type and content length

				m_listCtrl.SetItemText( nIndex, 1, pInfo->getContentType());
				m_listCtrl.SetItemText( nIndex, 2, Long::toString( pInfo->getContentLength()));

				String status;
				String owner;

				if ( pInfo->isWorkingCopy()) {
					status = L"Work";
				}
				else if ( pInfo->getLockType() != LockNone) {
					status = L"Locked";
					owner  = pInfo->getLockOwner();
				}

				m_listCtrl.SetItemText( nIndex, 3, status);
				m_listCtrl.SetItemText( nIndex, 4, owner);
			}
		}
	}

	// Clear the file info list

	m_fileList.clear();

	return FALSE;
}

// CFileStatusDialog message handlers
