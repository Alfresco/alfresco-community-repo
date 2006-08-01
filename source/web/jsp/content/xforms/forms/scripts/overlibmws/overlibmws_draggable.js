/*
 overlibmws_draggable.js plug-in module - Copyright Foteos Macrides 2002=2005
   For support of the DRAGGABLE feature.
   Initial: August 24, 2002 - Last Revised: January 12, 2005
 See the Change History and Command Reference for overlibmws via:

	http://www.macridesweb.com/oltest/

 Published under an open source license: http://www.macridesweb.com/oltest/license.html
*/

OLloaded=0;
OLregCmds('draggable');

// DEFAULT CONFIGURATION
if(OLud('draggable'))var ol_draggable=0;
// END CONFIGURATION

var o3_draggable=0,o3_dragging=0,OLmMv,OLcX,OLcY,OLcbX,OLcbY;
function OLloadDraggable(){OLload('draggable');}
function OLparseDraggable(pf,i,ar){
var k=i;
if(k<ar.length){if(Math.abs(ar[k])==DRAGGABLE){OLtoggle(ar[k],pf+'draggable');return k;}}
return -1;
}

function OLcheckDrag(){
if(o3_draggable){if(o3_sticky&&(o3_frame==self))initDrag();else o3_draggable=0;}
}
function initDrag(){
OLmMv=OLdw.onmousemove;o3_dragging=0;
if(OLns4){document.captureEvents(Event.MOUSEDOWN|Event.CLICK);
document.onmousedown=OLgrabEl;;document.onclick=function(e){return routeEvent(e);}}
else{over.onmousedown=OLgrabEl;OLsetDrgCur(1);}
}
function OLsetDrgCur(d){if(!OLns4)over.style.cursor=(d?'move':'auto');}

function OLgrabEl(e){
var e=(e||event);
var cKy=(OLns4?e.modifiers&Event.ALT_MASK:(!OLop7?e.altKey:e.ctrlKey));o3_dragging=1;
if(cKy){OLsetDrgCur(0);document.onmouseup=function(){OLsetDrgCur(1);o3_dragging=0;}
return(OLns4?routeEvent(e):true);}
OLx=(e.pageX||e.clientX+OLfd().scrollLeft);OLy=(e.pageY||e.clientY+OLfd().scrollTop);
if(OLie4)over.onselectstart=function(){return false;}
if(OLns4){OLcX=OLx;OLcY=OLy;document.captureEvents(Event.MOUSEUP)}else{
OLcX=OLx-(OLns4?over.left:parseInt(over.style.left));
OLcY=OLy-(OLns4?over.top:parseInt(over.style.top));
if((OLshadowPI)&&bkdrop&&o3_shadow){OLcbX=OLx-(parseInt(bkdrop.style.left));
OLcbY=OLy-(parseInt(bkdrop.style.top));}}OLdw.onmousemove=OLmoveEl;
document.onmouseup=function(){
if(OLie4)over.onselectstart=null;o3_dragging=0;OLdw.onmousemove=OLmMv;}
return(OLns4?routeEvent(e):false);
}

function OLmoveEl(e){
var e=(e||event);
OLx=(e.pageX||e.clientX+OLfd().scrollLeft);OLy=(e.pageY||e.clientY+OLfd().scrollTop);
if(o3_dragging){if(OLns4){over.moveBy(OLx-OLcX,OLy-OLcY);
if(OLshadowPI&&bkdrop&&o3_shadow)bkdrop.moveBy(OLx-OLcX,OLy-OLcY);}
else{OLrepositionTo(over,OLx-OLcX,OLy-OLcY);
if((OLiframePI)&&OLie55&&OLifsP1)OLrepositionTo(OLifsP1,OLx-OLcX,OLy-OLcY);
if((OLshadowPI)&&bkdrop&&o3_shadow){OLrepositionTo(bkdrop,OLx-OLcbX,OLy-OLcbY);
if((OLiframePI)&&OLie55&&OLifsSh)OLrepositionTo(OLifsSh,OLx-OLcbX,OLy-OLcbY);}}
if(OLhidePI)OLhideUtil(0,1,1,0,0,0);}if(OLns4){OLcX=OLx;OLcY=OLy;}
return false;
}

function OLclearDrag(){
if(OLns4){document.releaseEvents(Event.MOUSEDOWN|Event.MOUSEUP|Event.CLICK);
document.onmousedown=document.onclick=null;}else{over.onmousedown=null;OLsetDrgCur(0);}
document.onmouseup=null;o3_dragging=0;
}

OLregRunTimeFunc(OLloadDraggable);
OLregCmdLineFunc(OLparseDraggable);

OLdraggablePI=1;
OLloaded=1;
