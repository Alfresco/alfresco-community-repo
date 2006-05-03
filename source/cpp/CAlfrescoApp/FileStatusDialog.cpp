/*
* Copyright (C) 2005 Alfresco, Inc.
*
* Licensed under the Alfresco Network License. You may obtain a
* copy of the License at
*
*   http://www.alfrescosoftware.com/legal/
*
* Please view the license relevant to your network subscription.
*
* BY CLICKING THE "I UNDERSTAND AND ACCEPT" BOX, OR INSTALLING,  
* READING OR USING ALFRESCO'S Network SOFTWARE (THE "SOFTWARE"),  
* YOU ARE AGREEING ON BEHALF OF THE ENTITY LICENSING THE SOFTWARE    
* ("COMPANY") THAT COMPANY WILL BE BOUND BY AND IS BECOMING A PARTY TO 
* THIS ALFRESCO NETWORK AGREEMENT ("AGREEMENT") AND THAT YOU HAVE THE   
* AUTHORITY TO BIND COMPANY. IF COMPANY DOES NOT AGREE TO ALL OF THE   
* TERMS OF THIS AGREEMENT, DO NOT SELECT THE "I UNDERSTAND AND AGREE"   
* BOX AND DO NOT INSTALL THE SOFTWARE OR VIEW THE SOURCE CODE. COMPANY   
* HAS NOT BECOME A LICENSEE OF, AND IS NOT AUTHORIZED TO USE THE    
* SOFTWARE UNLESS AND UNTIL IT HAS AGREED TO BE BOUND BY THESE LICENSE  
* TERMS. THE "EFFECTIVE DATE" FOR THIS AGREEMENT SHALL BE THE DAY YOU  
* CHECK THE "I UNDERSTAND AND ACCEPT" BOX.
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
