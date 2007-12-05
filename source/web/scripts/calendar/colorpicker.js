YAHOO.example.ColorPicker = function() {

    var Slider=YAHOO.widget.Slider;
    var Color=YAHOO.util.Color;
    var Dom=YAHOO.util.Dom;

    var pickerSize=180;
    
    var hue,picker,panel;

    // hue, int[0,359]
    var getH = function() {
        var h = (pickerSize - hue.getValue()) / pickerSize;
        h = Math.round(h*360);
        return (h == 360) ? 0 : h;
    }

    // saturation, int[0,1], left to right
    var getS = function() {
        return picker.getXValue() / pickerSize;
    }

    // value, int[0,1], top to bottom
    var getV = function() {
        return (pickerSize - picker.getYValue()) / pickerSize;
    }

    var swatchUpdate = function() {
        var h=getH(), s=getS(), v=getV();

        Dom.get("hval").value = h;
        Dom.get("sval").value = Math.round(s*100);
        Dom.get("vval").value = Math.round(v*100);

        var rgb = Color.hsv2rgb(h, s, v);

        var styleDef = "rgb(" + rgb.join(",") + ")";
        Dom.setStyle("swatch", "background-color", styleDef);

        Dom.get("rval").value = rgb[0];
        Dom.get("gval").value = rgb[1];
        Dom.get("bval").value = rgb[2];

        Dom.get("hexval").value = Color.rgb2hex(rgb[0], rgb[1], rgb[2]);
    };

    var hueUpdate = function(newOffset) {
        YAHOO.log("hue update: " + newOffset , "warn");
        var rgb = Color.hsv2rgb(getH(), 1, 1);
        var styleDef = "rgb(" + rgb.join(",") + ")";
        Dom.setStyle("pickerDiv", "background-color", styleDef);

        swatchUpdate();
    };

    pickerUpdate = function(newOffset) {
        YAHOO.log("picker update [" + newOffset.x + ", " + newOffset.y + "]" , "warn");
        swatchUpdate();
    };

    return {

        init: function () {

            hue = Slider.getVertSlider("hueBg", "hueThumb", 0, pickerSize);
            hue.subscribe("change", hueUpdate);

            picker = Slider.getSliderRegion("pickerDiv", "selector", 
                    0, pickerSize, 0, pickerSize);
            picker.subscribe("change", pickerUpdate);

            hueUpdate(0);

            panel = new YAHOO.util.DD("ddPicker");
            panel.setHandleElId("pickerHandle");

        }
    }
}();

//YAHOO.util.Event.on(window, "load", YAHOO.example.ColorPicker.init);
