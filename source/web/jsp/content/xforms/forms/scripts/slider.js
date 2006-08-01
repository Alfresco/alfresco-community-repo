var SLIDER_DEBUG = false;

/*	slider control 
 *  methodnames with a leading '_' stand for a "private" method
 *
 *
 *	the slider control renders similar to the JSlider swing control from Java.
 *	it is set by "start", "end", "step", and "labelStep". 
 *	start and end define the range, step defines the size of a (minor) "tick", so
 *	the number of valid values is (start - end) / step.
 *	labelStep is the size of (major) "ticks". usually it is bigger than step, but
 *	it might be equal. Labels are only shown at major ticks. minor ticks only show
 *	a (smaller) scale line.
 
 *	restrictions:
 *  start, end and labelStep must be multiples of step. like:
 * 	(-2, 2, 0.5, 1) is correct.
 *	(-2, 2, 0.5, 0.75) is not correct (0.75 is not a multiple of 0.5).
 *	
 *	"API":
 *	a slider can be created with 
 *		createSlider(formname, sliderID, sliderName(=controlName), width, height, start, end, step).
 *		createSlider(formname, sliderID, sliderName(=controlName), width, height, start, end, step, labelStep, showInputField, onClick, onChange).
 *	the value can be set with 
 *		setSliderValue(sliderID, value).
 *	the layout can be set with 
 *		setLayout(layoutName);
 *			there are different layouts for the control.
 *			"default"	is simple test layout
 *			"windows"	is the windows XP slider layout
 *			...
 *			all known layout names are provided as a public Constant
 * 
 * OPEN ISSUES:
 * - behaviour at a reload of the page is errorous:
 * 	 slider is set back to tick 0, but is not replaced with empty
 *	 picture on a click to another tick.
 * - height attribute is not implemented.
 * - font size and family are not yet configurable nor dynamic.
 */



// *******************************************************
// ****************** PUBLIC CONSTANTS *******************
// *******************************************************
var LAYOUT_DEFAULT = "default";
var LAYOUT_WINDOWS = "windows";

var IMAGES_DIR = "../script/";


// *******************************************************
// ****************** PRIVATE CONSTANTS ******************
// *******************************************************
var _TICK_CONTROL_PREFIX = "_T_"; // used as prefix for current tick number (internal hidden field)
var _PICTURE_NAME_PREFIX = "_PT_"; // prefix for image name inside a slider



// *******************************************************
// ************* PRIVATE GLOBAL VARIABLES ****************
// *******************************************************
var _layout = new Array();	// the current layout parameters - are set with setLayout()
var _sliders = new Array(); // global array with all slider's data arrays - for easier access

// *******************************************************
// ******************** INIT LAYOUT **********************
// *******************************************************
	setLayout(LAYOUT_DEFAULT); // set the current layout data to "simple"


// *******************************************************
// ********************* PUBLIC METHODS ******************
// *******************************************************

function createSlider(parentElementID, sliderID, sliderName, width, height, start, end, step, onChange, label, labelStep) {
	// alert("create slider '" + sliderName + "' id='" + sliderID + "'");
	
	// check for correct slider ID
	if (_sliders[sliderID]) {
		alert('there is already a slider with ID \'' + sliderName + '\'');
		return false;
	}

	// check, if start and end are dividable by step and if labelStep is a multiple of step.
	if (start % step != 0) {
		alert('slider \'' + sliderName + '\': start(' + start + ') must be a multiple of step(' + step + ')');
		return false;
	}
	if (end % step != 0) {
		alert('slider \'' + sliderName + '\': end(' + end + ') must be a multiple of step(' + step + ')');
		return false;
	}

	// if start and end are in reverse order, range is inversed
	var range;
	if (start >= end) {
		range = start - end;
	} else {
		range = end - start;
	}
	var ticks = range / step + 1;

	if (labelStep) {
		if (labelStep % step != 0) {
			alert('slider \'' + sliderName + '\': labelStep(' + labelStep + ') must be a multiple of step (' + step + ')');
			return false;
		}
	} else {
		labelStep = step * (range / 5);
		if (labelStep < 2) {
			labelStep = 2;
		}
	}

	// cell dimensions
	var cellWidth;
	if (width && width > 0) {
		cellWidth = Math.round(width / ticks);
	} else {
		cellWidth = _layout['minimalCellWidth'];
	}
	width = ticks * cellWidth;
	
	var ctrlType = "hidden";
	var border = 0;
	if (SLIDER_DEBUG) {
		ctrlType = "text";
		border = 1;
	}

	var pointerWidth = _layout['pointerWidth'];
	if (cellWidth < _layout['pointerWidth']) {
		pointerWidth = cellWidth;
	}

	var spacerWidthLeft = parseInt((cellWidth - _layout['scaleLineWidth']) / 2);
	var spacerWidthRight = cellWidth - spacerWidthLeft - _layout['scaleLineWidth'];

	sliderData = new Array();
//	sliderData["formName"] = formName;
	sliderData["sliderID"] = sliderID;
	sliderData["sliderName"] = sliderName;
	sliderData["width"] = width;
	sliderData['height'] = height;
	sliderData['start'] = 1 * start;
	sliderData['end'] = 1 * end;
	sliderData['step'] = 1 * step;
	sliderData['labelStep'] = labelStep;
	sliderData['range'] = range;
	sliderData['ticks'] = ticks;
	sliderData['cellWidth'] = cellWidth;
	sliderData['sliderPointerWidth'] = pointerWidth;
	sliderData['spacerWidthLeft'] = spacerWidthLeft;
	sliderData['spacerWidthRight'] = spacerWidthRight;
	sliderData['label'] = label;
	sliderData['onChange'] = onChange;
	
	// put slider into global array
	_sliders[sliderName] = sliderData;
	
	var html = '<input type="' + ctrlType + '" name="' + sliderName + '" id="' + sliderID + '" value="' + start + '"';
	if (onChange) {
		html += ' onClick="' + onChange + '"';
	}
	html += '>\n';
	html += '<input type="' + (SLIDER_DEBUG ? 'text' : 'hidden') + '" name="' + _TICK_CONTROL_PREFIX + sliderName + '" id="' + _TICK_CONTROL_PREFIX + sliderID + '" value="' + 0 + '">\n';
	html += '<table width="' + width + '" border="' + border + '">\n';
	
	if (_layout['order'] == "up") {
		html += _labelRow(sliderData);
		html += _ticksRow(sliderData);
		html += _scaleRow(sliderData);
		html += _pointerRow(sliderData);
	} else {
		html += _labelRow(sliderData);
		html += _pointerRow(sliderData);
		html += _scaleRow(sliderData);
		html += _ticksRow(sliderData);
	}

	html += '</table>';
	document.getElementById(parentElementID).innerHTML = html;
	
	return true;
}


function setSlider(sliderName, value) {
	/**	set the slider <sliderName> to the value.
	 *	@return	false, if unknown slider or invalid value. true otherwise.
	 */
	var sliderData = _sliders[sliderName];
	if (typeof sliderData == "undefined") {
		alert("slider '" + sliderName + "' is not defined");
		return false;
	}
	if (sliderData["start"] > value) {
		alert("slider '" + sliderName + "': value '" + value + "' is smaller then minimum (" + sliderData["start"] + ")");
		return false;
	}
	if (value > sliderData["end"]) {
		alert("slider '" + sliderName + "': value '" + value + "' is greater then maximum (" + sliderData["end"] + ")");
		return false;
	}
	if (value % sliderData["step"] != 0) { 
		alert("slider '" + sliderName + "': value '" + value + "' must be a multiple of labelStep (" + sliderData["step"] + ")");
		return false;
	}
	var tick = (value - sliderData["start"]) / sliderData["step"];
	return _setSlider(sliderName, value, tick);
}

function setSliderLabel(sliderName, label) {
	/**	set the slider <sliderName> label.
	 *	@return	false, if unknown slider or invalid value. true otherwise.
	 */
	var sliderData = _sliders[sliderName];
	if (typeof sliderData == "undefined") {
		alert("slider '" + sliderName + "' is not defined");
		return false;
	}
	
}



// *******************************************************
// ********************* PRIVATE METHODS ******************
// *******************************************************

function _setSlider(sliderName, value, step) {
	// alert('setSlider: ' + sliderName + ' to ' + value + ' at step ' + step); 
	var sliderData = _sliders[sliderName];
	var elm = document.getElementById(sliderData["sliderID"]);
	var oldValue = elm.value;
	var elmStep = document.getElementById(_TICK_CONTROL_PREFIX + sliderData["sliderID"]);
	var oldStep = elmStep.value;
	if (elm && elmStep && (step != oldStep)) {
		elm.value = value;
		elmStep.value = step;
		
		if (SLIDER_DEBUG) {
			alert(elm.name + ' -> ' + elm.value + ' old: ' + oldStep);
			alert(document.images[_PICTURE_NAME_PREFIX + sliderName + '_' + oldStep].name);
			alert(document.images[_PICTURE_NAME_PREFIX + sliderName + '_' + step].name);
		}
		
		document.images[_PICTURE_NAME_PREFIX + sliderName + '_' + oldStep].src = _layout['spacerURL'];
		document.images[_PICTURE_NAME_PREFIX + sliderName + '_' + oldStep].width = sliderData['cellWidth'];

		document.images[_PICTURE_NAME_PREFIX + sliderName + '_' + step].src = _layout["pointerURL"];
		document.images[_PICTURE_NAME_PREFIX + sliderName + '_' + step].width = sliderData['sliderPointerWidth'];
		
		// explicitly do onChange, because it is not triggered, when changes are programmatically.
		if (sliderData['onChange'] && value != oldValue) {
			// eval(sliderData['onChange']);
			elm.click();
		}
		
	}
	return false;
}

function _ticksRow(sliderData) {
	var html = '<tr><td>\n\t<table width="100%" cellspacing="0" cellpadding="0"><tr>\n';
	if (sliderData["cellWidth"] > 15) {
		for (i=0; sliderData['ticks'] >= i; i++) {
			if (i == 0 || i == (sliderData['ticks']-1) || ((i * sliderData['step']) % sliderData['labelStep'] == 0)) {
				html += '\t\t<td width="' + sliderData['cellWidth'] + '" align="center" nowrap>' + _fix(sliderData['start'] + (i * sliderData['step']), 2) + '</td>\n';
			} else  {
				html += '\t\t<td width="' + sliderData['cellWidth'] + '"></td>\n';
			}
		}
	} else {
		var labels = sliderData['range'] / sliderData['labelStep'];
		var defaultWidth = sliderData['width'] / labels;
		for (i=0; labels >= i; i++) {
			var width = defaultWidth;
			var align = "center";
			if (i == 0) {
				width = parseInt(width / 2);
				align = "left";
			}
			if (i == labels) {
				width = parseInt(width / 2);
				align = "right";
			}
			html += '\t\t<td width="' + width + '" align="' + align + '" nowrap>' + _fix(sliderData['start'] + (i * sliderData['labelStep']), 2) + '</td>\n';
		}
	}
	html += '\t</tr></table>\n</td></tr>\n';
	return html;
}

function _scaleRow(sliderData) {
	var html = '<tr><td>\n\t<table width="100%" cellspacing="0" cellpadding="0"><tr>\n';
	for (i=0; sliderData['ticks'] >= i; i++) {
		var sVal = (sliderData['start'] + (i * sliderData['step']));
		if (i == 0 || i == (sliderData['ticks']-1) || ((i * sliderData['step']) % sliderData['labelStep'] == 0)) {
			var lineH = _layout['scaleLabelLineHeight'];
		} else  {
			var lineH = _layout['scaleTickLineHeight'];
		}
		html += '\t\t<td width="' + sliderData['cellWidth'] + '" align="center" valign="bottom">' +
		'<table cellspacing="0" cellpadding="0" width="100%"><tr>' +
		'<td width="1" valign="bottom">' +
		_a_starttag(sliderData, sVal, i) + 
		'<img src="' + _layout['spacerURL'] + '" border="0" width="' + sliderData['spacerWidthLeft'] + '" height="' + _layout['scaleLabelLineHeight'] + '" align="bottom" alt="' + sVal + '"/></td>' +
		'</a>' +
		'<td width="1" valign="bottom">' +
		_a_starttag(sliderData, sVal, i) + 
		'<img src="' + _layout['scaleLineURL'] + '" border="0" height="' + lineH + '" width="' + _layout['scaleLineWidth'] + '" align="bottom" alt="' + sVal + '"/></td>' +
		'</a>' +
		'<td width="1" valign="bottom">' +
		_a_starttag(sliderData, sVal, i) + 
		'<img src="' + _layout['spacerURL'] + '" border="0" width="' + sliderData['spacerWidthRight'] + '" height="' + _layout['scaleLabelLineHeight'] + '" align="bottom" alt="' + sVal + '"/></td>' +
		'</a>' +
		'</tr></table>' +
		'</td>\n';
	}
	html += '\t</tr></table>\n</td></tr>\n';
	return html;
}

function _pointerRow(sliderData) {

	var ticks = sliderData['ticks'];
	var html = '<tr><td>\n\t<table width="100%" cellspacing="0" cellpadding="0"><tr>\n';
	for (i=0; ticks >= i; i++) {
		var pictureName = _PICTURE_NAME_PREFIX + sliderData['sliderName'] + '_' + i;
		var sVal = (sliderData['start'] + (i * sliderData['step']));

		var url = _layout['spacerURL'];
		var width = sliderData['cellWidth'];
		if (i == 0) {
			url = _layout['pointerURL'];
			width = sliderData['sliderPointerWidth'];
		}

		html += '\t\t<td align="center" width="' + sliderData['cellWidth'] + '">\n';
		html += 
		_a_starttag(sliderData, sVal, i) + 
		'<img name="' + pictureName + '" border="0" src="' + url + '" width="' + width + '" height="' + _layout['pointerHeight'] + '" alt="' + sVal + '"/>' +
		'</a>';
		html += '</td>\n';
	}
	html += '\t</tr></table>\n</td></tr>\n';
	return html;
}

function _labelRow(sliderData) {
	if (sliderData['label'] && sliderData['label'].length > 0) {
		var html = '<tr><td id="' + sliderData['sliderID'] + '-label">' + sliderData['label'] + '</td></tr>\n';
		return html;
	} else {
		return '';
	}
}

// gibt die zahl auf decimal stellen nach dem komma zurück
function _fix(val, decimal) {
	if (!isNaN(val)) {
		return parseInt(val * 100) / 100;
	}
}

function _a_starttag(sliderData, value, tick) {
	return '<a href="js:/set value to \'' + value + '\'" onclick="return _setSlider(\'' + sliderData['sliderName'] + '\', \'' + value + '\', \'' + tick + '\')">';
}




// *******************************************************
// ********************* LAYOUT METHODS ******************
// *******************************************************

function setLayout(layout) {
	switch (layout) {
		case "windows":
			_layout['order'] 				= "down";
			_layout['scaleLineURL'] 		= IMAGES_DIR + "slider/windowsXP/tick.gif";
			_layout['scaleLineWidth'] 		= 1;
			_layout['scaleTickLineHeight']	= 4;
			_layout['scaleLabelLineHeight']	= 7;
			_layout['pointerURL'] 			= IMAGES_DIR + "slider/windowsXP/pointer.gif";
			_layout['pointerWidth'] 		= 11;
			_layout['pointerHeight'] 		= 20;
			_layout['spacerURL'] 			= IMAGES_DIR + "slider/space.gif";
			_layout['minimalCellWidth'] 	= 15;
			break;
		default:
			_layout['order'] 				= "up";
			_layout['scaleLineURL'] 		= IMAGES_DIR + "slider/tick.gif";
			_layout['scaleLineWidth'] 		= 2;
			_layout['scaleTickLineHeight']	= 5;
			_layout['scaleLabelLineHeight']	= 10;
			_layout['pointerURL'] 			= IMAGES_DIR + "slider/pointer.gif";
			_layout['pointerWidth'] 		= 1;
			_layout['pointerHeight'] 		= 20;
			_layout['spacerURL'] 			= IMAGES_DIR + "slider/space.gif";
			_layout['minimalCellWidth'] 	= 15;
	}
}

