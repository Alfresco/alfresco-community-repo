// PresentationContext: UI State updating.
// Author: unl
// Copyright 2005 Chibacon

/**
 * Constructor.
 */
PresentationContext = function() {
};

// Static member

PresentationContext.CHIBA_PSEUDO_ITEM = "chiba-pseudo-item";
PresentationContext.PROTOTYPE_CLONES = new Array();
PresentationContext.GENERATED_IDS = new Array();

// Event handler

/**
 * Handles chiba-load-uri.
 */
PresentationContext.prototype.handleLoadURI = function(uri, show) {
    _debug("PresentationContext.handleLoadURI: uri='" + uri + "', show='" + show + "'");
    window.open(uri, show == "new" ? "_blank" : "_self");
};

/**
 * Handles chiba-render-message.
 */
PresentationContext.prototype.handleRenderMessage = function(message, level) {
    _debug("PresentationContext.handleRenderMessage: message='" + message + "', level='" + level + "'");
    if (level == "modal") {
        alert(message);
    }
    else {
        alert("TODO: PresentationContext.handleRenderMessage: message='" + message + "', level='" + level + "'");
    }
};

/**
 * Handles chiba-replace-all.
 */
PresentationContext.prototype.handleReplaceAll = function() {
    _debug("PresentationContext.handleReplaceAll: ?");

    window.open("SubmissionResponse", "_self");
};

/**
 * Handles chiba-state-changed.
 */
PresentationContext.prototype.handleStateChanged = function(targetId, valid, readonly, required, enabled, value) {
    _debug("PresentationContext.handleStateChanged: targetId='" + targetId + "',  valid='" + valid + "',  readonly='" + readonly + "',  required='" + required + "',  enabled='" + enabled + "',  value='" + value + "'");

    var target = document.getElementById(targetId);
    if (target == null) {
        alert("target '" + targetId + "' not found");
        return;
    }

    if (value != null) {
        PresentationContext._setControlValue(targetId, value);
    }

    if (valid != null) {
        PresentationContext._setValidProperty(target, eval(valid));
    }
    if (readonly != null) {
        PresentationContext._setReadonlyProperty(target, eval(readonly));
    }
    if (required != null) {
        PresentationContext._setRequiredProperty(target, eval(required));
    }
    if (enabled != null) {
        PresentationContext._setEnabledProperty(target, eval(enabled));
    }
};

/**
 * Handles chiba-state-changed for helper elements.
 */
PresentationContext.prototype.handleHelperChanged = function(parentId, type, value) {
    _debug("PresentationContext.handleHelperChanged: parentId='" + parentId + "',  type='" + type + "',  value='" + value + "'");
    switch (type) {
        case "label":
            PresentationContext._setControlLabel(parentId, value);
            return;
        case "help":
            PresentationContext._setControlHelp(parentId, value);
            return;
        case "hint":
            PresentationContext._setControlHint(parentId, value);
            return;
        case "alert":
            PresentationContext._setControlAlert(parentId, value);
            return;
        case "value":
            PresentationContext._setControlValue(parentId, value);
            return;
    }
};

/**
 * Handles chiba-prototype-cloned.
 */
PresentationContext.prototype.handlePrototypeCloned = function(targetId, type, originalId, prototypeId) {
    _debug("PresentationContext.handlePrototypeCloned: targetId='" + targetId + "',  type='" + type + "',  originalId='" + originalId + "',  prototypeId='" + prototypeId + "'");
    if (type == "itemset") {
        PresentationContext._cloneSelectorPrototype(targetId, originalId, prototypeId);
    }
    else {
        PresentationContext._cloneRepeatPrototype(targetId, originalId, prototypeId);
    }
};

/**
 * Handles chiba-id-generated.
 */
PresentationContext.prototype.handleIdGenerated = function(targetId, originalId) {
    _debug("PresentationContext.handleIdGenerated: targetId='" + targetId + "',  originalId='" + originalId + "'");
    PresentationContext._setGeneratedId(targetId, originalId);
};

/**
 * Handles chiba-item-inserted.
 */
PresentationContext.prototype.handleItemInserted = function(targetId, type, originalId, position) {
    _debug("PresentationContext.handleItemInserted: targetId='" + targetId + "',  type='" + type + "',  originalId='" + originalId + "',  position='" + position + "'");
    if (type == "itemset") {
        PresentationContext._insertSelectorItem(targetId, originalId, position);
    }
    else {
        PresentationContext._insertRepeatItem(targetId, originalId, position);
    }
};

/**
 * Handles chiba-item-deleteed.
 */
PresentationContext.prototype.handleItemDeleted = function(targetId, type, originalId, position) {
    _debug("PresentationContext.handleItemDeleted: targetId='" + targetId + "',  type='" + type + "',  originalId='" + originalId + "',  position='" + position + "'");
    if (type == "itemset") {
        PresentationContext._deleteSelectorItem(targetId, originalId, position);
    }
    else {
        PresentationContext._deleteRepeatItem(targetId, originalId, position);
    }
};

/**
 * Handles chiba-index-changed.
 */
PresentationContext.prototype.handleIndexChanged = function(targetId, originalId, index) {
    _debug("PresentationContext.handleIndexChanged: targetId='" + targetId + "',  originalId='" + originalId + "',  index='" + index + "'");
    PresentationContext._setRepeatIndex(targetId, originalId, index);
};

/**
 * Handles chiba-switch-toggled.
 */
PresentationContext.prototype.handleSwitchToggled = function(deselectedId, selectedId) {
    _debug("PresentationContext.handleSwitchToggled: deselectedId='" + deselectedId + "', selectedId='" + selectedId + "'");

    if (deselectedId != "switch-toggles") {
        var deselected = document.getElementById(deselectedId);
        _replaceClass(deselected, "selected-case", "deselected-case");
        var inactive = document.getElementById(deselectedId + "-tab");
        _replaceClass(inactive, "active-tab", "inactive-tab");
    }
    if (selectedId != "switch-toggles") {
        var selected = document.getElementById(selectedId);
        _replaceClass(selected, "deselected-case", "selected-case");
        var active = document.getElementById(selectedId + "-tab");
        _replaceClass(active, "inactive-tab", "active-tab");
    }
};

// static utilities

PresentationContext._setValidProperty = function(target, valid) {
//    _debug("PresentationContext._setValidProperty: " + target + "='" + valid + "'");

    if (valid) {
        _replaceClass(target, "invalid", "valid");
    }
    else {
        _replaceClass(target, "valid", "invalid");
    }
};

PresentationContext._setReadonlyProperty = function(target, readonly) {
//    _debug("PresentationContext._setReadonlyProperty: " + target + "='" + readonly + "'");

    if (readonly) {
        _replaceClass(target, "readwrite", "readonly");
    }
    else {
        _replaceClass(target, "readonly", "readwrite");
    }

    var targetId = target.getAttribute("id");
    if (document.getElementById(targetId + "-date-display")) {
        // special treatment for calendar
        PresentationContext._updateCalendar(targetId, "date", readonly);
        return;
    }
    if (document.getElementById(targetId + "-dateTime-display")) {
        // special treatment for calendar
        PresentationContext._updateCalendar(targetId, "dateTime", readonly);
        return;
    }

    var value = document.getElementById(targetId + "-value");
    if (value) {
        if (value.nodeName.toLowerCase() == "input" && value.type.toLowerCase() == "hidden") {
            // special treatment for radiobuttons/checkboxes
            PresentationContext._updateSelectors(value, readonly);
            return;
        }

        if (readonly) {
            value.setAttribute("disabled", "disabled");
        }
        else {
            value.removeAttribute("disabled");
        }
    }
};

PresentationContext._setRequiredProperty = function(target, required) {
//    _debug("PresentationContext._setRequiredProperty: " + target + "='" + required + "'");

    if (required) {
        _replaceClass(target, "optional", "required");
    }
    else {
        _replaceClass(target, "required", "optional");
    }
};

PresentationContext._setEnabledProperty = function(target, enabled) {
//    _debug("PresentationContext._setEnabledProperty: " + target + "='" + enabled + "'");

    if (enabled) {
        _replaceClass(target, "disabled", "enabled");
    }
    else {
        _replaceClass(target, "enabled", "disabled");
    }

    // handle labels too, they might be rendered elsewhere
    var targetId = target.getAttribute("id");
    var label = document.getElementById(targetId + "-label");
    if (label) {
        if (enabled) {
            _replaceClass(label, "disabled", "enabled");
        }
        else {
            _replaceClass(label, "enabled", "disabled");
        }
    }
};

PresentationContext._setControlValue = function(targetId, value) {
//    _debug("PresentationContext.setControlValue: " + targetId + "='" + value + "'");

    var control = document.getElementById(targetId + "-value");
    if (control == null) {
        alert("value for '" + targetId + "' not found");
        return;
    }

    var listValue = " " + value + " ";
    switch (control.nodeName.toLowerCase()) {
        case "a":
            // <xf:output appearance="anchor"/>
            control.href = value;
            break;
        case "img":
            // <xf:output appearance="image"/>
            control.src = value;
            break;
        case "input":
            if (control.type.toLowerCase() == "hidden") {
                // check for date control
                if (document.getElementById(targetId + "-date-display") || document.getElementById(targetId + "-date-button")) {
                    control.value = value;
                    calendarUpdate(targetId, value, "date");
                    break;
                }

                // check for dateTime control
                if (document.getElementById(targetId + "-dateTime-display") || document.getElementById(targetId + "-dateTime-button")) {
                    control.value = value;
                    calendarUpdate(targetId, value, "dateTime");
                    break;
                }

                // special treatment for radiobuttons/checkboxes
                var elements = eval("document.chibaform.elements");
                var box;
                var boxValue;
                for (var i = 0; i < elements.length; i++) {
                    if (elements[i].name == control.name && elements[i].type != "hidden") {
                        box = elements[i];
                        boxValue = " " + box.value + " ";
                        if (listValue.indexOf(boxValue) > -1) {
                            box.checked = true;
                        }
                        else {
                            box.checked = false;
                        }
                    }
                }
                break;
            }

            if (control.type.toLowerCase() == "button") {
                // ignore
                break;
            }

            if (control.type.toLowerCase() == "file") {
                // ignore
                break;
            }

            control.value = value;
            break;
        case "option":
            control.value = value;
            break;
        case "span":
            // <xf:output appearance="colorbox"/>
            if (_hasClass(control, "colorbox")) {
                control.style.backgroundColor = value;
                break;
            }

            _setElementText(control, value);
            break;
        case "select":
            // special treatment for options
            var options = control.options.length;
            var option;
            var optionValue;
            for (var i = 0; i < options; i++) {
                option = control.options[i];
                optionValue = " " + option.value + " ";
                if (listValue.indexOf(optionValue) > -1) {
                    option.selected = true;
                }
                else {
                    option.selected = false;
                }
            }
            break;
        case "table":
            if(_hasClass(control,"range-widget")){
                var oldValue = document.getElementsByClassName('rangevalue', document.getElementById(targetId))[0];
                if(oldValue){
                    oldValue.className = "step";
                }
                var newValue = document.getElementById(targetId + value);
                if(newValue){
                    newValue.className = "step rangevalue";
                }
            }
            break;
        case "textarea":
            control.value = value;
            break;
        default:
            alert("unknown control '" + control.nodeName + "'");
    }
};

PresentationContext._setControlLabel = function(parentId, value) {
//    _debug("PresentationContext._setControlLabel: " + parentId + "='" + value + "'");

    var element = document.getElementById(parentId + "-label");
    if (element != null) {
        // update label element
        _setElementText(element, value);
        return;
    }

    // heuristics: look for implicit labels
    var control = document.getElementById(parentId + "-value");
    switch (control.nodeName.toLowerCase()) {
        case "a":
        // <xf:output appearance="anchor"/>
            _setElementText(control, value);
            break;
        case "span":
        // <xf:output appearance="colorbox"/>
            _setElementText(control, value);
            break;
        case "option":
            control.text = value;
            break;
        case "input":
            if (control.type.toLowerCase() == "button") {
                control.value = value;
                break;
            }
            // fall through
        default:
            // dirty hack for compact repeats: lookup enclosing table
            var td = document.getElementById(parentId);
            if (td != null && td.nodeName.toLowerCase() == "td") {
                var tr = td.parentNode;
                if (tr != null && tr.nodeName.toLowerCase() == "tr") {
                    var tbody = tr.parentNode;
                    if (tbody != null && tbody.nodeName.toLowerCase() == "tbody") {
                        var table = tbody.parentNode;
                        if (table != null && table.nodeName.toLowerCase() == "table") {
                            if (_hasClass(table, "compact-repeat")) {
                                _debug("ignoring label for '" + parentId + "' in compact repeat");
                                break;
                            }
                        }
                    }
                }
            }

            // complain, finally
            alert("label for '" + parentId + "' not found");
    }
};

PresentationContext._setControlHelp = function(parentId, value) {
//    _debug("PresentationContext._setControlHelp: " + parentId + "='" + value + "'");

    alert("TODO: PresentationContext._setControlHelp: " + parentId + "='" + value + "'");
};

PresentationContext._setControlHint = function(parentId, value) {
//    _debug("PresentationContext._setControlHint: " + parentId + "='" + value + "'");

    var element = document.getElementById(parentId + "-value");
    if (element != null) {
        if (element.nodeName.toLowerCase() == "input" && element.type == "hidden") {
            // special treatment for radiobuttons/checkboxes
            var boxes = eval("document.chibaform." + element.name + ".length;");
            var box;
            for (var i = 0; i < boxes; i++) {
                box = eval("document.chibaform." + element.name + "[" + i + "]");
                box.title = value;
            }
        }
        else {
            element.title = value;
        }
    }
    else {
        alert("hint for '" + parentId + "' not found");
    }
};

PresentationContext._setControlAlert = function(parentId, value) {
//    _debug("PresentationContext._setControlAlert: " + parentId + "='" + value + "'");

    var element = document.getElementById(parentId + "-alert");
    if (element != null) {
        _setElementText(element, value);
    }
    else {
        alert("alert for '" + parentId + "' not found");
    }
};

/**
 * Clones a repeat prototype.
 *
 * @param target the repeat id.
 * @param value the prototype id (original repeat id).
 */
PresentationContext._cloneRepeatPrototype = function(targetId, originalId, prototypeId) {
//    _debug("PresentationContext._cloneRepeatPrototype: [" + targetId + "/" + originalId + "]='" + prototypeId + "'");

    var clone = document.getElementById(originalId + "-prototype").cloneNode(true);
    clone.setAttribute("id", prototypeId);
    _replaceClass(clone, "repeat-prototype", "repeat-item");
    PresentationContext.PROTOTYPE_CLONES.push(clone);

    var ids = new Array();
    PresentationContext.GENERATED_IDS.push(ids);
};

/**
 * Clones a selector prototype.
 *
 * @param target the selector id.
 * @param value the prototype id (original selector id).
 */
PresentationContext._cloneSelectorPrototype = function(targetId, originalId, prototypeId) {
//    _debug("PresentationContext._cloneSelectorPrototype: [" + targetId + "/" + originalId + "]='" + prototypeId + "'");

    // clone prototype and make it an item
    var clone;
    var proto = document.getElementById(originalId + "-prototype");

    if (proto.nodeName.toLowerCase() == "select") {
        // special handling for option prototypes, since their prototype
        // element needs a wrapper element for carrying the prototype id
        var optionIndex;
        for (var i = 0; i < proto.childNodes.length; i++) {
            if (proto.childNodes[i].nodeType == 1) {
                optionIndex = i;
                break;
            }
        }
        proto = proto.childNodes[optionIndex];

        // create an option object rather than cloning it, otherwise IE won't
        // display it as an additional option !!!
        clone = new Option("", "");
        clone.selected = false;
        clone.defaultSelected = false;
        clone.id = proto.id;
        clone.title = proto.title;
    }
    else {
        clone = proto.cloneNode(true);
        clone.setAttribute("id", prototypeId);
    }

    clone.className = "selector-item";
    PresentationContext.PROTOTYPE_CLONES.push(clone);

    var ids = new Array();
    PresentationContext.GENERATED_IDS.push(ids);
};

/**
 * Sets a generated id on the current prototype clone.
 *
 * @param target the generated id.
 * @param value the original id.
 */
PresentationContext._setGeneratedId = function(targetId, originalId) {
//    _debug("PresentationContext._setGeneratedId: " + targetId + "='" + originalId + "'");

    var array = PresentationContext.GENERATED_IDS[PresentationContext.GENERATED_IDS.length - 1];
    array[originalId] = targetId;
    array[originalId + "-value"] = targetId + "-value";
    array[originalId + "-label"] = targetId + "-label";
    array[originalId + "-alert"] = targetId + "-alert";
    array[originalId + "-required"] = targetId + "-required";

    if (!array[PresentationContext.CHIBA_PSEUDO_ITEM]) {
        // we have to add a special 'chiba-pseudo-item' mapping, since for itemsets
        // within repeats there is no item yet at the time the prototype is generated.
        // thus, there is no original id in the prototype !!!
        array[PresentationContext.CHIBA_PSEUDO_ITEM] = targetId;
        array[PresentationContext.CHIBA_PSEUDO_ITEM + "-value"] = targetId + "-value";
        array[PresentationContext.CHIBA_PSEUDO_ITEM + "-label"] = targetId + "-label";
    }
};

/**
 * Inserts the current prototype clone as a repeat item.
 *
 * @param target the repeat id.
 * @param value the insert position.
 */
PresentationContext._insertRepeatItem = function(targetId, originalId, position) {
//    _debug("PresentationContext._insertRepeatItem: [" + targetId + "/" + originalId + "]='" + position + "'");

    // apply generated ids to prototype
    var prototypeClone = PresentationContext.PROTOTYPE_CLONES.pop();
    var generatedIds = PresentationContext.GENERATED_IDS.pop();
    PresentationContext._applyGeneratedIds(prototypeClone, generatedIds);

    // setup indices
    var currentPosition = 0;
    var targetPosition = parseInt(position);
    var insertIndex = -1;

    // lookup repeat
    var targetElement;
    if (PresentationContext.PROTOTYPE_CLONES.length > 0) {
        // nested repeat
        var enclosingPrototype = PresentationContext.PROTOTYPE_CLONES[PresentationContext.PROTOTYPE_CLONES.length - 1];
        targetElement = _getElementById(enclosingPrototype, originalId);
    }
    else {
        // top-level repeat
        targetElement = document.getElementById(targetId);
    }

    var repeatElement = PresentationContext._getRepeatNode(targetElement);
    var repeatItems = repeatElement.childNodes;

    for (var i = 0; i < repeatItems.length; i++) {
        // lookup elements
        if (repeatItems[i].nodeType == 1) {
            // lookup repeat item
            if (_hasClass(repeatItems[i], "repeat-item")) {
                currentPosition++;

                // store insert index (position *at* insert item)
                if (currentPosition == targetPosition) {
                    insertIndex = i;
                    break;
                }
            }
        }
    }

    // detect reference node
    var referenceNode = null;
    if (insertIndex > -1) {
        referenceNode = repeatItems[insertIndex];
    }

    // insert prototype clone
    repeatElement.insertBefore(prototypeClone, referenceNode);
};

/**
 * Inserts the current prototype clone as a selector item.
 *
 * @param target the selector id.
 * @param value the insert position.
 */
PresentationContext._insertSelectorItem = function(targetId, originalId, position) {
//    _debug("PresentationContext._insertSelectorItem: [" + targetId + "/" + originalId + "]='" + position + "'");

    // apply generated ids to prototype
    var prototypeClone = PresentationContext.PROTOTYPE_CLONES.pop();
    var generatedIds = PresentationContext.GENERATED_IDS.pop();
    PresentationContext._applyGeneratedIds(prototypeClone, generatedIds);

    // setup indices
    var currentPosition = 0;
    var targetPosition = parseInt(position);
    var insertIndex = -1;

    // lookup itemset
    var itemsetElement;
    if (PresentationContext.PROTOTYPE_CLONES.length > 0) {
        // nested repeat
        var enclosingPrototype = PresentationContext.PROTOTYPE_CLONES[PresentationContext.PROTOTYPE_CLONES.length - 1];
        itemsetElement = _getElementById(enclosingPrototype, originalId);
    }
    else {
        // top-level repeat
        itemsetElement = document.getElementById(targetId);
    }

    var itemsetItems = itemsetElement.childNodes;
    for (var i = 0; i < itemsetItems.length; i++) {
        // lookup elements
        if (itemsetItems[i].nodeType == 1) {
            // lookup repeat item
            if (_hasClass(itemsetItems[i], "selector-item")) {
                currentPosition++;

                // store insert index (position *at* insert item)
                if (currentPosition == targetPosition) {
                    insertIndex = i;
                    break;
                }
            }
        }
    }

    // detect reference node
    var referenceNode = null;
    if (insertIndex > -1) {
        referenceNode = itemsetItems[insertIndex];
    }

    // insert prototype clone
    itemsetElement.insertBefore(prototypeClone, referenceNode);
};

/**
 * Deletes a repeat item.
 *
 * @param target the repeat id.
 * @param value the delete position.
 */
PresentationContext._deleteRepeatItem = function(targetId, originalId, position) {
//    _debug("PresentationContext._deleteRepeatItem: [" + targetId + "/" + originalId + "]='" + position + "'");

    var currentPosition = 0;
    var targetPosition = parseInt(position);
    var deleteIndex = -1;
    var nextIndex = -1;

    var targetElement = document.getElementById(targetId);
    var repeatElement = PresentationContext._getRepeatNode(targetElement);
    var repeatItems = repeatElement.childNodes;
    for (var i = 0; i < repeatItems.length; i++) {
        // lookup elements
        if (repeatItems[i].nodeType == 1) {
            // lookup repeat item
            if (_hasClass(repeatItems[i], "repeat-item")) {
                currentPosition++;

                // store delete index (position *at* delete item)
                if (currentPosition == targetPosition) {
                    deleteIndex = i;
                }

                // check for next item
                if (currentPosition > targetPosition) {
                    nextIndex = i;
                    break;
                }
            }
        }
    }

    // check for next item to be selected
    var deleteItem = repeatItems[deleteIndex];
    if (_hasClass(deleteItem, "repeat-index") && nextIndex > -1) {
        var nextItem = repeatItems[nextIndex];

        // reset repeat index manually since it won't change when it is set to
        // the item to delete and there is a following item
        _removeClass(deleteItem, "repeat-item");
        _addClass(nextItem, "repeat-index");
    }

    // delete item
    repeatElement.removeChild(deleteItem);
};

/**
 * Deletes a selector item.
 *
 * @param target the selector id.
 * @param value the delete position.
 */
PresentationContext._deleteSelectorItem = function(targetId, originalId, position) {
//    _debug("PresentationContext._deleteSelectorItem: [" + targetId + "/" + originalId + "]='" + position + "'");

    var itemset = document.getElementById(targetId);
    var items = itemset.childNodes;
    var currentPosition = 0;
    var targetPosition = parseInt(position);
    var deleteIndex = -1;

    for (var i = 0; i < items.length; i++) {
        // lookup elements
        if (items[i].nodeType == 1) {
            // lookup repeat item
            if (_hasClass(items[i], "selector-item")) {
                currentPosition++;

                // store delete index (position *at* delete item)
                if (currentPosition == targetPosition) {
                    deleteIndex = i;
                    break;
                }
            }
        }
    }

    // delete item
    itemset.removeChild(items[deleteIndex]);
};

/**
 * Sets the index of a repeat.
 *
 * @param target the repeat id.
 * @param value the repeat index.
 */
PresentationContext._setRepeatIndex = function(targetId, originalId, index) {
//    _debug("PresentationContext._setRepeatIndex: [" + targetId + "/" + originalId + "]='" + index + "'");

    var currentPosition = 0;
    var targetPosition = parseInt(index);

    if (targetPosition > 0) {
        var targetElement;
        if (PresentationContext.PROTOTYPE_CLONES.length > 0) {
            // nested repeat
            var enclosingPrototype = PresentationContext.PROTOTYPE_CLONES[PresentationContext.PROTOTYPE_CLONES.length - 1];
            targetElement = _getElementById(enclosingPrototype, originalId);
        }
        else {
            // top-level repeat
            targetElement = document.getElementById(targetId);
        }

        var repeatElement = PresentationContext._getRepeatNode(targetElement);
        var repeatItems = repeatElement.childNodes;
        for (var i = 0; i < repeatItems.length; i++) {
            // lookup repeat items
            if (repeatItems[i].nodeType == 1 && _hasClass(repeatItems[i], "repeat-item")) {
                currentPosition++;

                if (currentPosition == targetPosition) {
                    // select item
                    _addClass(repeatItems[i], "repeat-index");
                }
                else {
                    // deselect item
                    _removeClass(repeatItems[i], "repeat-index");
                }

                // remove preselection
                _removeClass(repeatItems[i], "repeat-index-pre");
            }
        }
    }
};

PresentationContext._updateSelectors = function(control, readonly) {
    var id = control.id.substring(0, control.id.indexOf("-value"));
    var count = eval("document.chibaform." + control.name + ".length;");
    var selector;
    var label;
    var position
    for (var i = 0; i < count; i++) {
        position = i + 1;
        selector = eval("document.chibaform." + control.name + "[" + i + "]");
        label = document.getElementById(id + "-" + position + "-label");
        if (readonly) {
            selector.setAttribute("disabled", "disabled");
            if (label != null) {
                label.setAttribute("disabled", "disabled");
            }
        }
        else {
            selector.removeAttribute("disabled");
            if (label != null) {
                label.removeAttribute("disabled");
            }
        }
    }
};

PresentationContext._updateCalendar = function(id, type, readonly) {
    var display = document.getElementById(id + "-" + type + "-display");
    display.disabled = readonly;
    display.readonly = !readonly;

    var button = document.getElementById(id + "-" + type + "-button");
    if (readonly) {
        _replaceClass(button, "enabled", "disabled");
    }
    else {
        _replaceClass(button, "disabled", "enabled");
    }
};

PresentationContext._getRepeatNode = function(element) {
    var items = element.childNodes;

    for (var i = 0; i < items.length; i++) {
        if (items[i].nodeType == 1 && items[i].nodeName.toLowerCase() == "tbody") {
            return items[i];
        }
    }

    return element;
};

PresentationContext._applyGeneratedIds = function(element, ids) {
    var id = element.getAttribute("id");
    if (id) {
        var generatedId = ids[id];
        if (generatedId) {
//            _debug("applying '" + generatedId + "' to '" + id + "'");
            element.setAttribute("id", generatedId);

            // apply to for-attribute of labels
            if (element.nodeName.toLowerCase() == "label") {
                var generatedFor = generatedId.substring(0, generatedId.length - 6) + "-value";
//                _debug("applying '" + generatedFor + "' for '" + id + "'");
                element.setAttribute("for", generatedFor);
            }
        }
    }

    // hack for hidden inputs and multiple inputs with same name. this doesn't
    // work for radiobuttons in IE, since input fields dyamically have to be
    // created as follows:
    //     document.createElement("<INPUT NAME='...''></INPUT");
    // (this is not a joke, but has been found at msdn.microsoft.com/ie/webdev)
    // todo: configurable data prefix, at least ;-(
    var CHIBA_DATA_PREFIX = "d_";
    if (element.nodeName.toLowerCase() == "input" && element.name && element.name.substring(0, 2) == CHIBA_DATA_PREFIX) {
        id = element.name.substring(2, element.name.length);
        var otherId = ids[id];
        if (otherId) {
//            _debug("applying '" + CHIBA_DATA_PREFIX + otherId + "' to '" + CHIBA_DATA_PREFIX + id + "'");
            element.setAttribute("name", CHIBA_DATA_PREFIX + otherId);
            if (element.checked) {
                element.checked = false;
                element.defaultChecked = false;
            }
        }
    }

    var toApply = element.childNodes;
    for (var index = 0; index < toApply.length; index++) {
        if (toApply[index].nodeType == 1) {
            PresentationContext._applyGeneratedIds(toApply[index], ids);
        }
    }
};
