// FileStatusView.cpp : implementation file
//

#include "stdafx.h"
#include "CAlfrescoApp.h"
#include "FileStatusView.h"


// CFileStatusView

IMPLEMENT_DYNCREATE(CFileStatusView, CListView)

CFileStatusView::CFileStatusView()
{
}

CFileStatusView::~CFileStatusView()
{
}

BEGIN_MESSAGE_MAP(CFileStatusView, CListView)
END_MESSAGE_MAP()


// CFileStatusView diagnostics

#ifdef _DEBUG
void CFileStatusView::AssertValid() const
{
	CListView::AssertValid();
}

void CFileStatusView::Dump(CDumpContext& dc) const
{
	CListView::Dump(dc);
}
#endif //_DEBUG


// CFileStatusView message handlers
