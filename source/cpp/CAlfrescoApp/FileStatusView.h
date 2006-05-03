#pragma once


// CFileStatusView view

class CFileStatusView : public CListView
{
	DECLARE_DYNCREATE(CFileStatusView)

protected:
	CFileStatusView();           // protected constructor used by dynamic creation
	virtual ~CFileStatusView();

public:
#ifdef _DEBUG
	virtual void AssertValid() const;
	virtual void Dump(CDumpContext& dc) const;
#endif

protected:
	DECLARE_MESSAGE_MAP()
};


