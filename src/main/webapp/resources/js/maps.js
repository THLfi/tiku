// for exposing methods from this file (readability)
if(typeof(maps) === 'undefined') {
    maps = { "features": {}};
}
maps.color = {
    'high': '#66cc66',
    'average': '#efd748',
    'low': '#ef4848',
    'undef': '#ddd'
};
maps.threshold = {
    'low': 25,
    'average': 75,
    'high': 101
};
if(typeof(thl) === 'undefined') {
    thl = {
            pivot: {}
    };
}
thl.pivot.fromJsonStat = function(dataset) {
    maps.dataset = JSONstat(dataset).Dataset(0);
};

var 
 geojson,
 legend,
 info,
 map,
 showMarkers = false, 
 visibleCategory = '',
 selected = { properties: undefined},
// Different categories [eg. maakunta, kunta] and their markers to different
// layers
 layersByCategory = {},
 layerGroupsByCategory = {},
 markerGroupsByCategory = {};

maps.toggleMarkerPaneVisibility = function() {
    showMarkers = !showMarkers;
    map.getPanes().markerPane.style.visibility = (showMarkers ? 'visible' : 'hidden');
};


function extractJsonStatValue(valueObject) {
    if(valueObject === null) {
        return -1;
    }
    if (valueObject instanceof Array) {
        return valueObject[0].value;
    } else {
        return valueObject.value;
    }
}

function getFontSize(zoom) {
    if (typeof (zoom) === 'undefined' || zoom <= 3) {
        return '100%';
    }
    return (zoom <= 4) ? '130%' : '160%';
}


// Return array: [value, responseText]
function getValue(region) {
    var result = [],
        key = {
            "hpcb": maps.viewId,
            "area": maps.ids[region],
            "measure": 'R_TULOS'
        };
    result.push(extractJsonStatValue(maps.dataset.Data(key)));

    key.measure = 'R_SELITENUMERO';
    result.push(extractJsonStatValue(maps.dataset.Data(key)));
    return result;
}

function getColor(region) {
    if (typeof (region) === 'undefined') {
        return maps.color.undef;
    }
    var val = getValue(region)[0];
    if(typeof(val) === 'undefined' || null == val || val < 0) {
        return maps.color.undef;
    }
    if(val < maps.threshold.low) {
        return maps.color.low;
    }
    if(val < maps.threshold.average) {
        return maps.color.average;
    }

    return maps.color.high;
}

function style(feature) {
    return {
        fillColor : getColor(feature.id),
        weight : 1,
        opacity : 0.3,
        fillOpacity : 1,
        color : '#303030'
    };
}

function highlightFeature(e) {
    var layer = e.target;

    layer.setStyle({
        weight : 2,
        dashArray : '',
        fillOpacity : 0.9
    });

    if (!L.Browser.ie && !L.Browser.opera) {
        layer.bringToFront();
    }

    info.update(layer.feature.properties);
}

function resetHighlight(e) {
    geojson.resetStyle(e.target);
    info.update(selected.properties);
}

function zoomToFeature(e) {
    map.fitBounds(e.target.getBounds());
    selected = e.target.feature;
}

function onEachFeature(feature, layer) {
    if (feature.id.search(/^\d+$/) >= 0) {
        layer.on({
            mouseover : highlightFeature,
            mouseout : resetHighlight,
            click : zoomToFeature
        });
        var center = layer.getBounds().getCenter();
        var label = L.marker(center, {
            icon : new L.DivIcon({
                iconSize : null,
                className : 'label',
                html : '<div class="marker">' + feature.properties.name + '</div>'
            })
        });

    }
}

var crs = new L.Proj.CRS('EPSG:3067', '+proj=utm +zone=35 +ellps=GRS80 +towgs84=0,0,0,0,0,0,0 +units=m +no_defs', {
    origin : [
            0, 0
    ],
    resolutions : [
            3000, 2048, 1024, 512, 256
    ],
    bounds : [
            -548576, 6291456, 1548576, 8388608
    ]
});




$(document).ready(function() {
    
    
    info = L.control({
    			position : 'topright'
    });

    info.onAdd = function(map) {
        this._div = L.DomUtil.create('div', 'map-info'); // create a div with
        // a class "info"
        this.update();
        return this._div;
    };

    // method that we will use to update the control based on feature
    // properties passed
    info.update = function(props) {
        var val = null, 
            responseText = null;
        if(typeof(props) !== 'undefined') {
        	var data = getValue(props.id);
            val = data[0];
            responseText = (data.length > 1 ? labels['r_'+data[1]] : null);
        }
        if (typeof(props) !== 'undefined' && typeof (val) !== 'undefined') {
            this._div.innerHTML = '<p><b>' + props.name + '</b></p>' + 
                                  (val == null ? '' : '<p>' + (''+val).replace(".", ",") + '</p>') +
                                  (responseText == null ? '' : '<p>' + responseText + '</p>');
            this._div.style.opacity = 0.8;
        } else {
            this._div.innerHTML = '';
            this._div.style.opacity = 0;
        }
    };

    
    legend = L.control({
        position : 'bottomleft'
    });
    legend.onAdd = function(map) {
        this._div = L.DomUtil.create('div', 'map-info legend');
        this.update();
        return this._div;
    }
    legend.update = function() {
        this._div.innerHTML  = '<div class="legend-titles">'
            + '<div><span class="color high" style="background-color: ' + maps.color.high +'"></span> <span class="title">' + labels["result.high"] + ' (75&ndash;100)</span></div>'
            + '<div><span class="color average" style="background-color: ' + maps.color.average +'"></span> <span class="title">' + labels["result.average"] + ' (25&ndash;74)</span></div>'
            + '<div><span class="color low" style="background-color: ' + maps.color.low + '"></span> <span class="title">' + labels["result.low"] + ' (0&ndash;24)</span></div>'
            + '<div><span class="color undef" style="background-color: ' + maps.color.undef + '"></span> <span class="title">' + labels["result.not_available"] + '</span></div>'
            + '</div>';
    };
    
    $('.img-loading').hide();
    map = L.map('map', {
        crs : crs,
        maxZoom : 4,
        scrollWheelZoom : false

    }).setView([
            65, 25
    ], 0);
    // on initial zoom level markers are hidden
    map.getPanes().markerPane.style.visibility = 'hidden';

    // Hide/show markers on appropriate zoom level
    map.on('zoomend', function() {
        var currentZoom = map.getZoom();
        map.getPanes().markerPane.style.fontSize = getFontSize(currentZoom);
    });

    $.each(categories, function(i, c) {
        geojson = L.Proj.geoJson(maps.features[c], {
            style: style,
            onEachFeature: onEachFeature
        })
        .addTo(map);
    });
    
    info.addTo(map);
    legend.addTo(map);
    
});

