var thl = thl || {};

if(typeof(maps) === 'undefined') {
    maps = { "features": {}};
}

/**
 Handle each bar chart presentation
 */
function selectChartType (e) {
  if (e.is('.map')) {
    return 'map';
  }
  if (e.is('.bar')) {
    return 'barchart';
  }
  if (e.is('.column')) {
    return 'columnchart';
  }
  if (e.is('.column-mekko')) {
    return 'columnmekkochart';
  }
  if (e.is('.line')) {
    return 'linechart';
  }
  if (e.is('.radar')) {
    return 'radarchart';
  }
  if (e.is('.pie')) {
    return 'piechart';
  }
  if (e.is('.gauge')) {
    return 'gaugechart';
  }
  if (e.is('.table')) {
    return 'table';
  }
  if (e.is('.list')) {
    return 'list';
  }
}

(function ($, d3) {


  function wrap(text, width) {
    // https://bl.ocks.org/mbostock/7555321
    text.each(function() {
      var text = d3.select(this),
          words = text.text().split(/\s+/).reverse(),
          word,
          line = [],
          lineNumber = 0,
          lineHeight = 1.1, // ems
          y = text.attr('y'),
          x = text.attr('x')
          dy = parseFloat(text.attr("dy")),
          tspan = text.text(null).append("tspan").attr("x", x).attr("y", y).attr("dy", dy + "em");
      while (word = words.pop()) {
        line.push(word);
        tspan.text(line.join(" "));
        if (tspan.node().getComputedTextLength() > width) {
          line.pop();
          tspan.text(line.join(" "));
          line = [word];
          tspan = text.append("tspan").attr("x", x).attr("y", y).attr("dy", ++lineNumber * lineHeight + dy + "em").text(word);
        }
      }
    });
  }

  function numberFormat(str) {
    if(str === undefined || str === '' || str === null) {
      return '';
    }
    return (''+str).replace(/(\d)(?=(\d{3})+(\.|$))/g, '$1\xa0') // Use non-breaking space as a thousands separator
      .replace(/\./g, ',')
  }

  thl.pivot = thl.pivot || {};
  thl.pivot.svgToImg = function (doc, width, height, opt, callback) {
    var isMap = false;
    if (opt.legendData) {         
      isMap = true;
    }      
    var mapWidth = opt.target[0].offsetWidth;   
    var svgHeight;    
    if(doc.attr('height') !== undefined) {
      svgHeight = +doc.attr('height');      
      
    } else {
      var vb = +doc[0].getAttribute('viewBox');
      if(vb){
        svgHeight = vb.split(' ')[3];
      }      
    }    

    var svgWidth = (+svgHeight/height)*width;
    if(doc.attr('width') !== undefined) {
      svgWidth = +doc.attr('width');
    } else {
      var vb = +doc[0].getAttribute('viewBox');
      if(vb){
        svgWidth = vb.split(' ')[2];
      }   
    }  
  
    var heightRatio = (height / svgHeight)
    var croppedImgWidth = width / heightRatio;
    
    var left = (mapWidth/2)-(width/2);    
    var leftStart = 0;    
    if (left < 0) {
      leftStart = -left;
      left = 0;
    }
    if (croppedImgWidth > svgWidth) {
      croppedImgWidth = svgWidth;
    }
    var legend = getLegendSvg(opt.legendData);

    var data;
    if(doc.attr('height')) {
      data = doc.parent().html();
    } else {
      data = doc.parent().html().replace('<svg ', '<svg width="' + width + '" height="' + height + '" ');
    }
    data = data.replace(/&nbsp;/g,' ');
    if(data.indexOf('xmlns') < 0) {
      // xmlns is required to draw svg to canvas
      data = data.replace('<svg',  '<svg xmlns="http://www.w3.org/2000/svg" ');
    }
    var lData;
    var DOMURL = window.URL || window.webkitURL || window;
    var lImg = new Image();
    if(isMap){
      if(legend.attr('height')) {
        lData = legend.parent().html();
      } else {
        lData = legend.parent().html().replace('<svg ', '<svg "' + width + '" height="' + height + '" ');
      }
      lData = lData.replace(/&nbsp;/g,' ');
      if(lData.indexOf('xmlns') < 0) {
        // xmlns is required to draw svg to canvas
        lData = lData.replace('<svg',  '<svg xmlns="http://www.w3.org/2000/svg" ');
      }

      var lBlob = new Blob([lData], {type: 'image/svg+xml'});
      var lUrl = DOMURL.createObjectURL(lBlob);    
      lImg.src = lUrl;
    }
    var blob = new Blob([data], {type: 'image/svg+xml'});
    var img = new Image();
    var drawCanvasAndSetUrlFunction = function() {

      try {
        var mapHeightAdd = isMap ? 40: 0;
        var canvas = $('<canvas>').attr('width', width ).attr('height', height+mapHeightAdd ).get(0);       
        var ctx = canvas.getContext('2d');
        ctx.fillStyle = '#ffffff';
        
        if (isMap) {
          ctx.fillRect(0, 0, +width , +height+mapHeightAdd );
          ctx.drawImage(img,
            left, 0, //map image where to start inserting
            croppedImgWidth, svgHeight,   //what size is the map    
            leftStart, 0,  //where to put it in on the canvas
            croppedImgWidth, svgHeight);     //what size to scretch map    
             ctx.drawImage(lImg,0,0);
        } else {
          ctx.fillRect(0, 0, width, height);
          ctx.drawImage(img,0,0);
        }
        
        callback(canvas);
        DOMURL.revokeObjectURL(url);
        if(isMap){
          DOMURL.revokeObjectURL(lUrl);
        }

      } catch (e) {
        $(img).remove();
      }
    };

    img.onload =  drawCanvasAndSetUrlFunction;
    var url = DOMURL.createObjectURL(blob);     
   img.src = url;

  };

  function getLegendSvg(legendData) {    
    var clonedSvg =   $('<svg></svg>').attr('xmlns','http://www.w3.org/2000/svg').attr('width', 500 ).attr('height', 400 );
    if(!legendData){
      return clonedSvg;
    }
    var clonedSvgAsD3Obj = d3.select(clonedSvg.get(0));
    var legendXPosition = 40;
    var legendContainer = clonedSvgAsD3Obj
      .append('g')
      .attr('transform', 'translate(' + legendXPosition + ', 15)');

    legendContainer 
      .append('text')
      .text(legendData.title)
      .attr({ x: 0, y: 28 })
      .style({
        fill: '#030303', 'font-size': '18px', 'font-family': 'Source Sans Pro', stroke: 'none'
      });

    var labels = legendContainer
      .selectAll('g')
      .data(legendData.labels )
      .enter()
      .append('g')
      .attr('transform', function(d, i) { return 'translate(5,' + (36 + i * 24) + ')'; });

    var labelMarkers = labels
      .append('rect')
      .attr({
        x: 0,
        y: 10,
        width: 15,
        height: 15,
        stroke: 'none',
        fill: function(d) { return d.color; }
      });

    labels
      .append('text')
      .text(function(d) { return d.label; })
      .attr({
        x: 20,
        y: 20,
        fill: '#030303',
        'font-size': '12px',
        'font-family': 'Source Sans Pro',
        stroke: 'none'
      });

    clonedSvg = $(clonedSvgAsD3Obj[0]).appendTo('<div>');
    return clonedSvg;
  };

  thl.pivot.exportImg = function (opt) {
    $(opt.target[0]).find('.img-action a').each(function (e) {
      var width = 800;
      var height = 400;         
      if (opt.legendData) {         
        width = 600;
      }         
      var link = $(this);
      if (link.attr('href') === '#') {
        var svg = $(this).closest('.presentation').find('svg');             
        thl.pivot.svgToImg(svg, +width, +height, opt, function (canvas) {
          try {
            link.attr('href', canvas.toDataURL());
            link.attr('download', opt.target.attr('id') + '.png');
            link.click();
          } catch (e) {
            link.remove();
          }
        });
      }
    });
  }
  thl.pivot.summary = function (labels, dimensionData) {
    /*
     * Color palette for charts
     */
    var colors = [
        '#2f62ad',
        '#7cd0d8',
        '#571259',
        '#5faf2c',
        '#B14A72',
        '#3b007f',
        '#16994a',
        '#cccc72',
        '#0e1e47',
        '#25a5a2',
        '#cc7acc',
        '#244911',
        '#9f7fcc',
        '#06602b',
        '#a59c2b',
        '#7699d6',
        '#11414c',
        '#993499',
        '#84b266',
        '#7a2242',
        '#6938af',
        '#71cc96',
        '#595616'
      ],
      /*
       * Color palette for good, average and bad values
       */
      trafficLightColors = [
        '#d888a9',
        '#b2b2b2',
        '#7699d6'
      ],
      canDrill = function (nodeId) {
        if (typeof dimensionData[nodeId] === 'undefined') {
          return false;
        }
        return isDrillEnabled && dimensionData[nodeId] && dimensionData[nodeId].hasChild;
      },
      submitDrillDown = function (nodeId, dimension, drillUp) {
        if (drillUp || canDrill(nodeId)) {
          var form = $('#summary-form');
          form
            .find('input[name=drill-' + dimension + ']')
            .remove();
          form
            .append(
              $('<input type="hidden">')
                .attr('name', 'drill-' + dimension)
                .val(nodeId)
          )
            .submit();
        }
      };

    return {
      submitDrillDown: function (nodeId, dimension) {
        submitDrillDown(nodeId, dimension, true);
      },
      /*
       * Draws a table based on the dataset in the presentation element.
       * Required the followin parameters
       *  - dataset -> a representations of the jsonstat dataset
       *  - columnCount -> number of column levels in the table
       *  - rowCount -> number of row levels in the table
       *  - target -> parent element for the table
       */
      drawMap: function (opt) {
        var geojson,
         showMarkers = false, 
         visibleCategory = '',
         selected = { properties: undefined},
        // Different categories [eg. maakunta, kunta] and their markers to different
        // layers
         layersByCategory = {},
         layerGroupsByCategory = {},
         markerGroupsByCategory = {};

        var colors;
        if(opt.palette === 'gray') {
          colors = ['#b2b2b2', '#8c8c8c','#666666','#3f3f3f','#191919'];
        } else if (opt.palette == 'ranking') {
          colors = ['#bf4073', '#d888a9','#B2B2B2','#7699d6', '#2f62ad'];
        } else {
          colors = ['#b2b2b2', '#7cd0d8','#7699d6', '#2f61ad', '#0e1e47'];
        }

        var areaCodes = {}
        $.each(dimensionData, function(i, v) {
          if(v.dim === 'area') {
            areaCodes[v.code] = i;
          }
        });

        var limits;
        var values = []
        $.each(opt.dataset.Data(), function(i, v) {
          if(/^\d+(\.\d+)?$/.test(v.value)) {
            values.push(+v.value);
          }
        });
        values.sort(function (a,b) { return a-b;});
        if(opt.limits === undefined || opt.limits === '') {
          limits = [
            values[0],
            values[Math.floor(values.length / 5)],
            values[Math.floor(2 * values.length / 5)],
            values[Math.floor(3 * values.length / 5)],
            values[Math.floor(4 * values.length / 5)],
            values[values.length -1]
          ];
        } else {
          limits = opt.limits.split(',');
        }


        function mapLimitIndex(limits, i) {
          var j = i;
          if(limits.length === 4) {
            if(opt.palette === 'ranking') {
              switch(j) {
                case 0: j = 1; break;
                case 1: j = 2; break;
                case 2: 
                case 3:
                case 4: j = 3; break;
              }
            } else {
              switch(j) {
                case 0: j = 2; break;
                case 1: j = 3; break;
                case 2:
                case 3: j = 4; break;
              }
            }
          }
          return opt.order === 'desc' ? 4 - j : j;
        }

        function getFontSize(zoom) {
            if (typeof (zoom) === 'undefined' || zoom <= 3) {
                return '100%';
            }
            return (zoom <= 4) ? '130%' : '160%';
        }

        var crs = new L.Proj.CRS('EPSG:3067', '+proj=utm +zone=35 +ellps=GRS80 +towgs84=0,0,0,0,0,0,0 +units=m +no_defs', {
          origin : [0, 0],
          resolutions : [3000, 2048, 1024, 512, 256],
          bounds : [-548576, 6291456, 1548576, 8388608]
        });

        function openBoundValue(limit) {
          var numDecimals = opt.decimals;
          if (numDecimals == -1 || (!numDecimals && numDecimals !== 0)) {
            var decimalParts = limit.toString().split('.');
            if (decimalParts.length < 2) {
              numDecimals = 0;
            } else {
              numDecimals = decimalParts[1].length || 0;
            }
          }
          var step = Math.pow(10, -numDecimals);

          if (opt.include === 'gte') {
            return (Number(limit) - step).toFixed(numDecimals);
          }
          else if (opt.include === 'lte') {
            return (Number(limit) + step).toFixed(numDecimals);
          }
        }

        var legend = L.control({position : 'bottomleft'});
        var tooltip = $('<span></span>').addClass('maptip').hide();

        opt.legendData = {
          title: opt.label,
          labels: []
        };
        legend.update = function() {
          var ul = $('<ul>');
          var lastBound = Number.MAX_VALUE;
          for(var i = 0; i < limits.length - 1; ++i) {
            if(lastBound == limits[i + 1]) {
              continue;
            }
            var li = $('<li>')
            var lbl = opt.limitLabels();
            if(lbl.length > i) {
                li.text(lbl[i]);
            } else {
                if (opt.include === 'gte') {
                  var upperLimit = (i === limits.length-2) ? limits[i + 1] : openBoundValue(limits[i + 1]);
                  li.text(numberFormat(limits[i]) + '\u2013' + numberFormat(upperLimit));
                }
                else if (opt.include === 'lte') {
                  var lowerLimit = (i === 0) ? limits[0] : openBoundValue(limits[i]);
                  li.text(numberFormat(lowerLimit) + '\u2013' + numberFormat(limits[i + 1]));
                }
            }
            var l = $('<span></span>')
              .css('background', colors[mapLimitIndex(limits, i)]);
            li.prepend(l);
            ul.append(li);
            lastBound = limits[i + 1];
            opt.legendData.labels.push({label: li.text(), color: colors[mapLimitIndex(limits, i)]});
          }
          $(this._div)
            .append($('<strong></strong>').text(opt.label))
            .append(ul);
        };
        legend.onAdd = function(map) {
          this._div = L.DomUtil.create('div', 'map-info legend');
          this.update();
          return this._div;
        };
         
        var map = L.map(opt.target.get(0), {
          crs : crs,
          maxZoom : 4,
          scrollWheelZoom : false
        }).setView([65, 25], 0);
        map.getPanes().markerPane.style.visibility = 'hidden';
        map.on('zoomend', function() {
          var currentZoom = map.getZoom();
          map.getPanes().markerPane.style.fontSize = getFontSize(currentZoom);
        });

        L.Proj.geoJson(maps.features.MAA, {
          style: {
              fillColor : '#f2f2f2',
              weight : 2,
              opacity : 1,
              fillOpacity : 1,
              color : '#666666'
          }
        }).addTo(map);
     

        geojson = L.Proj.geoJson(maps.features[opt.stage], {
          style: function (feature) {
            var v = opt.dataset.Data({'area': areaCodes[feature.properties.code]});
            var color = '#f2f2f2'

            if(v !== undefined && /^\d+(\.\d+)?$/.test(v.value)) {
              color = undefined;
              for(var i = 1; i < limits.length - 1; ++i) {
                if(opt.include === 'lte' && +v.value <= limits[i]) {
                  color = colors[mapLimitIndex(limits, i - 1)];
                  break;
                } else if (opt.include === 'gte' && +v.value < limits[i]) {
                  color = colors[mapLimitIndex(limits, i - 1)];
                  break;
                }
              }
              if(color === undefined) {
                color = colors[mapLimitIndex(limits, 4)];
              }
            } else {
              color = '#f2f2f2';
            }

            return {
                fillColor : color,
                weight : 1,
                opacity : 1,
                fillOpacity : 1,
                color : '#666666'
            };
          },
          onEachFeature: function(feature, layer) {
            layer.on({
              mouseover: function(e) {
                var v = opt.dataset.Data({'area': areaCodes[feature.properties.code]});
                if(v !== undefined && v.value !== undefined && v.value != null) {
                  tooltip.text(feature.properties.name + ': ' + numberFormat(v.value));
                } else {
                  tooltip.text(feature.properties.name);
                }
                tooltip.show();
                var x = e.containerPoint.x;
                if(x + tooltip.width() > opt.target.width() - 50) {
                  x = opt.target.width() - tooltip.width() - 15;
                }
                var y = e.containerPoint.y;
                if(y + tooltip.height() > opt.target.height() - 50) {
                  y = opt.target.height() - tooltip.height() - 15;
                }
                tooltip.css('left', x + 'px');
                tooltip.css('top', y + 'px');
         
                layer.setStyle({
                    weight : 2,
                    dashArray : '',
                    fillOpacity : 1
                });
              },
              mouseout : function (e) {
                tooltip.hide();
                geojson.resetStyle(e.target);
              },
              click: function(e) {
                map.fitBounds(e.target.getBounds());
                selected = e.target.feature;
              }
            });
          }
        }).addTo(map);
        legend.addTo(map);
        opt.target.append(tooltip);
        opt.target.addClass(opt.palette)
        thl.pivot.exportImg(opt);

      },
      drawList: function(opt) {
        var CHARACTER_WIDTH = (3*14)/5;
        var wrapper = $('<div>').addClass('table-responsive')
        var table = $('<table>').addClass('table table-bordered');
        var thead = $('<thead>');
        var thr = $('<tr>')
        thr.append('<th></th>');
        $.each(opt.dataset.Dimension(1).id, function(g, w) {
            thr.append($('<th>').text(labels[w]));
        });
        thead.append(thr);
        var tbody = $('<tbody>');
        var cls = opt.target.attr('id') + '-col-';
        var columnWidths = [];
        $.each(opt.dataset.Dimension(0).id, function(k, v) {
            var key = {};
            key[opt.dataset.id[0]] = v;
            var tr = $('<tr>');
            tr.append($('<th>').text(labels[v]));
            $.each(opt.dataset.Dimension(1).id, function(g, w) {
                key[opt.dataset.id[1]] = w;
                var val = opt.dataset.Data(key);
                if(val == null || val.value == null) {
                    val = '..';
                } else {
                    val = numberFormat('' + val.value);
                    var width = val.length * CHARACTER_WIDTH;
                    if(columnWidths[g] === undefined || columnWidths[g] < w) {
                        columnWidths[g] = width;
                    }
                }
                tr.append($('<td>').append($('<span></span>').text(val).addClass(cls + g)));
            });
            tbody.append(tr);
        });

        table.append(thead).append(tbody);
        wrapper.append(table);
        opt.target.append(wrapper);
        opt.target.parent().find('img').remove();

        var rules = '';
        var column = 0;
        $.each(columnWidths, function (i, w) {
            rules += '.' + cls + (column++) + '{ width: ' + (w) + 'px; } ';
        });

        $('body')
          .append(
            $('<style></style>')
            .html(rules)
          );
      },
      drawTable: function (opt) {
        var CHARACTER_WIDTH = (3*14)/5;
        /*
         * calculates how many child elements there are for each
         * each element per level. The span for level is the
         * product of the number of elements per each lower level
         * and 1 for the deepest level
         */
        function calculateSpanPerLevel (offset, levels) {
          var spanPerLevel = [];
          // iterate levels from the end to the beginning
          for (var i = levels - 1; i >= offset; --i) {
            if (i + 1 === levels) {
              spanPerLevel.push(1);
            } else {
              spanPerLevel.push(spanPerLevel[spanPerLevel.length - 1] * opt.dataset.Dimension(i + 1).id.length);
            }
          }
          spanPerLevel.reverse();
          return spanPerLevel;
        }

        function isTotalHighlightColumn(dimensionNodeSurrogateId) {
          return opt.totalHighlightColumns.indexOf(dimensionNodeSurrogateId.toString()) > -1;
        }

        function isTotalHighlightRow(dimensionNodeSurrogateId) {
          return opt.totalHighlightRows.indexOf(dimensionNodeSurrogateId.toString()) > -1;
        }

        function setHighlightLevel(element, level) {
          element.attr('hl-level', Math.min(level, 5));
        }

        /**
         * Creates elements for the thead-element containing th elements for each
         * each column header.
         */
        function createTableHead (columnHlLevels) {
          var header = $('<thead>'),
            // A repeat factor is calculated to allow repeating of
            // header elements when there are multiple levels of
            // column headers
            repeatFactor = 1,
            spanPerLevel = calculateSpanPerLevel(opt.rowCount, opt.rowCount + opt.columnCount);

          for (var level = 0; level < spanPerLevel.length; ++level) {
            var row = $('<tr>'),
              dimension = opt.dataset.Dimension(opt.rowCount + level).id;

            // Add empty cell that spans row headings
            row.append(
              $('<th>').attr('colspan', opt.rowCount)
            );

            // Create header cells for current level
            for (var j = 0; j < repeatFactor; ++j) {
              $.each(dimension, function (i, v) {
                var node =
                $('<th>')
                  .attr('colspan', spanPerLevel[level])
                  .css('text-align', opt.align[1])
                  .text(labels[v]);
                var span = spanPerLevel[level];
                var repeatWidth = level == 0 ? 1 : spanPerLevel[level-1];
                var columnNumber = j * repeatWidth + i * span;

                if (isTotalHighlightColumn(v)) {
                  var hlLevel = columnHlLevels.getHighlightLevel(level);
                  setHighlightLevel(node, hlLevel);
                  for (var k = 0; k < span; k++) {
                    columnHlLevels[columnNumber + k] = hlLevel;
                  }
                } else if (columnHlLevels[columnNumber]) {
                  // inherit highlight from outer dimension
                  setHighlightLevel(node, columnHlLevels[columnNumber]);
                }
                row.append(node);
              });
            }
            header.append(row);

            // We have to repeat each item in level for once for each higher level
            // item
            repeatFactor = repeatFactor * dimension.length;
          }
          return header;
        }

        /**
         * Iterates over each level recursively and generates
         * a key that can be used to access data from the jsonstat
         * object.
         */
        function forEachDimension (level, max, vals, indices, f) {
          vals = $.merge([], vals);
          $.each(opt.dataset.Dimension(level).id, function (i, v) {
            vals.push(v);
            indices.push(i);
            if (level + 1 === max) {
              f(indices, vals);
            } else {
              forEachDimension(level + 1, max, vals, indices, f);
            }
            vals.pop();
            indices.pop();
          });
        }

        /**
         * Returns an event handler that allows
         * user to drill down to a specific node
         * in a table representation of a summary
         *
         * nodeId -> node to drill down to
         * dimension -> the dimension id of the node
         */
        function drillDown (nodeId, dimension) {
          return function (e) {
            e.preventDefault();
            submitDrillDown(nodeId, dimension);
          };
        }

        /**
         * Creates a th element for each row header level for the given row
         * row -> current row element
         * rowVals -> array of header node identifiers
         */
        function createRowHeaderCells (ri, row, rowVals, rowHeaders, rowIndex, rowHlLevels) {
          var hasChanged = false;
          for (var level = 0; level < opt.rowCount; ++level) {
            hasChanged = createRowHeaderCell(rowVals, row, ri, level, rowHeaders, rowIndex, hasChanged, rowHlLevels);
          }
        }

        function createRowHeaderCell (rowVals, row, ri, level, rowHeaders, rowIndex, hasChanged, rowHlLevels) {
          var nodeId = rowVals[level];
          var content = labels[nodeId];
          var th;
          if(!hasChanged && rowIndex > 0 && nodeId == rowHeaders[level][rowIndex - 1].attr('data-id')) {
            th = rowHeaders[level][rowIndex - 1];
            span = th.attr('rowspan');
            if(!span) {
              th.attr('rowspan', 2);
            } else {
              th.attr('rowspan', parseInt(span) + 1);
            }
          } else {
            th = $('<th>').attr('data-id', nodeId);
            row.append(th);
            hasChanged = true;
          }

          if (isTotalHighlightRow(nodeId)) {
            var hlLevel = rowHlLevels.getHighlightLevel(level);
            rowHlLevels[ri] = hlLevel;
            setHighlightLevel(th, hlLevel);
          } else if (rowHlLevels[ri]) {
            setHighlightLevel(th, rowHlLevels[ri]);
          }

          if(rowHeaders[level].length == rowIndex) {
            rowHeaders[level].push(th);
          } else {
            rowHeaders[level][rowIndex] = th;
          }

          if(!th.attr('rowspan')) {
            if (canDrill(nodeId)) {
              var link = $('<a>')
                .attr('href', '#')
                .text(content)
                .click(drillDown(nodeId, dimensionData[nodeId].dim));
              th.append(link);
            } else {
              th.text(content);
            }
          }
          return hasChanged;
        }

        /**
         * Creates a td element for each value cell in the given row
         * row -> current row element
         * rowIndices -> array of value indices defined by the current row, used to query data from a jsonstat object
         */
        function createRowValueCells (offset, row, rowIndices, columnWidths, cls, rowHlLevel, columnHlLevels) {
          var i = 0;
          var hasValue = false;

          forEachDimension(opt.rowCount, opt.dataset.Dimension().length, [], [], function (colIndices, colVals) {
            var key = $.merge($.merge([], rowIndices), colIndices);
            var val = opt.dataset.Data(key);
            var cell;
            if (val == null || val.value === null) {
              cell = $('<td >')
                .append('<span>..</span>')
                .css('text-align', opt.align[0])
              row.append(cell);
              if(hasValue || opt.suppress === 'none' || opt.suppress === 'zero') {
                hasValue = true;
              }
            } else {
              hasValue = hasValue || (+val.value != 0 || (opt.suppress != 'all' && opt.supress != 'zero'));
              var content = numberFormat('' + val.value);
              var zeros = false;
              var negatives = false;
              var tdAndClass = '<td>';
              if(opt.highlight==='zeros_and_negatives'){
                zeros = true;
                negatives = true;
              } else if(opt.highlight==='zeros'){
                zeros = true;
              } else if(opt.highlight==='negatives'){
                negatives = true;
              }
              if ((+val.value<0 && negatives) || (+val.value===0 && zeros)){
                tdAndClass = '<td class="text-danger">';
              }
              var span = $('<span></span>')
                .text(content)
                .addClass(cls + i);
              cell = $(tdAndClass);
              row.append(
                cell
                .append(span)
                .css('text-align', opt.align[0])
              ); // Use comma as a decimal separator
              var w = content.length * CHARACTER_WIDTH;;
              if(columnWidths[i] === undefined || columnWidths[i] < w) {
                columnWidths[i] = w;
              }
            }
            var colHlLevel = columnHlLevels[i] || 0;
            var hlLevel = colHlLevel + rowHlLevel;
            if (hlLevel) {
              setHighlightLevel(cell, hlLevel);
            }
            i += 1;
          });
          return hasValue;
        }

        function createHighlightLevelMapping(highLightRowDimensions) {
          var mapping = {};
          var cumSum = 0;
          $.each(highLightRowDimensions, function(i, hl) {
            cumSum += parseInt(hl);
            mapping[i] = cumSum;
          });
          return function(dimensionLevel) {
            return mapping[dimensionLevel];
          };
        }

        var tableContainer = $('<div></div>').addClass('table-responsive');
        var columnHlLevels = {
          getHighlightLevel: createHighlightLevelMapping(opt.colDimHighlights)
        };
        var rowHlLevels = {
          getHighlightLevel: createHighlightLevelMapping(opt.rowDimHighlights)
        };
        var table =
          $('<table>')
            .addClass('table table-bordered')
            .append(createTableHead(columnHlLevels));
        var body = $('<tbody>');

        var dim = opt.dataset.Dimension();
        var cols = 1;
        var ri = 0;
        var rowIndex = 0
        var ari = 0;
        for (var i = opt.rowCount; i < dim.length; ++i) {
          cols *= dim[i].length;
        }
        var thCount = 0;
        var rowHeaders = [];
        for(var i = 0; i < opt.rowCount; ++i) {
          rowHeaders[i] = [];
        }
        var columnWidths = {};
        var cls = 'table-' + new Date().getTime() + '-';
        forEachDimension(0, opt.rowCount, [], [], function (rowIndices, rowVals) {
          var row = $('<tr>');
          createRowHeaderCells(ri, row, rowVals, rowHeaders, rowIndex, rowHlLevels);

          if (createRowValueCells(ri * cols, row, rowIndices, columnWidths, cls, rowHlLevels[ri] || 0, columnHlLevels)) {
            if (ari === 0) {
              thCount = $(row).find('th').size();
              ari += 1;
            }
            rowIndex++;
            body.append(row);  
          } else {
            for(var i = 0; i < opt.rowCount; ++i) {
              var th = rowHeaders[i][rowIndex];
              if(th.attr('rowspan')) {
                th.attr('rowspan', +th.attr('rowspan') - 1);
              }
            }
          }
          ri += 1;
        });

        table.append(body);
        tableContainer.append(table);
        $(opt.target[0]).append(tableContainer);


        for (var i = 1; i < opt.rowCount + 1; ++i) {
          var previous;
          table.find('tbody tr th:nth-child(' + i + ')').each(function () {
            var current = $(this);
            if (previous === undefined || previous.attr('data-id') !== current.attr('data-id')) {
              previous = current;
            } else if (previous.attr('data-id') === current.attr('data-id')) {
              current.children().wrap('<span class="sr-only"></span>');
            }
          });
        }

        var rules = '';
        var column = 0;
        $.each(columnWidths, function (i, w) {
            rules += '.' + cls + (column++) + '{ width: ' + (w) + 'px; }';
        });

        $('body')
          .append(
            $('<style></style>')
            .html(rules)
          );
      },

      presentation: function (opt) {
        var domainRange,
        trueDomainRange,
          sums = [],
          percent = false,
          stacked = false,
          ordinalScale,
          barHeight = 10,
          barAndColumnMargin = 2,
          barGroupHeight,
          scaleValue,
          columnMekkoChartWidthScale,
          columnMekkoChartData = {},
          columnMekkoChartDomainLimit,
          widthMeasureIndex = -1,
          yAxisPos,
          xAxisPos,
          xAxisWidth,
          spacing,
          maxLabelLength,
          maxValueLength,
          isXAxisTicksTilted,
          MAX_LABEL_LENGTH = 300,
          MINIMUM_VALUE_LABEL_WIDTH = 50,
          CHARACTER_WIDTH = ((3 * 14) / 5.0),
          BAR_GROUP_MARGIN = 5,
          BAR_MARGIN = 5;

        function computeColumnMekkoChartVariables() {
          for (var i = 0; i < opt.series.length; i++) {
            if (opt.series[i] === opt.widthmeasure) {
              widthMeasureIndex = i;
              opt.series.splice(i, 1);
            }
          }
          var data = sortData(opt.target.attr('data-sort'));
          var currentWidth, overallWidth = 0;
          for (var i = 0; i < data.length; ++i) {
            var itemIndex = opt.dataset.Dimension(0).id.indexOf(data[i]);
            currentWidth = opt.callback(widthMeasureIndex, itemIndex);
            columnMekkoChartData[i] = {
              itemId: opt.dataset.Dimension(0).id[itemIndex],
              itemIndex: itemIndex,
              itemWidth: currentWidth,
              itemXPosition: currentWidth / 2 + overallWidth,
              sortOrder: i
            }
            overallWidth += currentWidth;
          }
          columnMekkoChartDomainLimit = overallWidth;
        }


        var drawWhiskers = function (g, series, scaledZero, ciLower, ciUpper, xPos, type) {
          // draw ci whisker

          var isBarChart = type === 'bar';
          g.enter()
            .append('line')
            .attr('class', 'ci')
            .attr('stroke', 'black')
            .attr('stroke-width', 1.5)
            .attr('x1', isBarChart ? ciLower : xPos)
            .attr('x2', isBarChart ? ciUpper : xPos)
            .attr('y1', isBarChart ? xPos : ciLower)
            .attr('y2', isBarChart ? xPos : ciUpper);
          g.enter()
            .append('line')
            .attr('class', 'ci')
            .attr('stroke', 'black')
            .attr('stroke-width', 1.5)
            .attr('x1', function (d, i) {
              if (isBarChart)
                return ciLower(d, i);
              return xPos(d, i) - 2;
            })
            .attr('x2', function (d, i) {
              if (isBarChart)
                return ciLower(d, i);
              return xPos(d, i) + 2;
            })
            .attr('y1', function (d, i) {
              if (isBarChart)
                return xPos(d, i) - 2;
              return ciLower(d, i);
            })
            .attr('y2', function (d, i) {
              if (isBarChart)
                return xPos(d, i) + 2;
              return ciLower(d, i);
            });
          g.enter()
            .append('line')
            .attr('class', 'ci')
            .attr('stroke', 'black')
            .attr('stroke-width', 1.5)
            .attr('x1', function (d, i) {
              if (isBarChart)
                return ciUpper(d, i);
              return xPos(d) - 2;
            })
            .attr('x2', function (d, i) {
              if (isBarChart)
                return ciUpper(d, i);
              return xPos(d) + 2;
            })
            .attr('y1', function (d, i) {
              if (isBarChart)
                return xPos(d, i) - 2;
              return ciUpper(d, i);
            })
            .attr('y2', function (d, i) {
              if (isBarChart)
                return xPos(d, i) + 2;
              return ciUpper(d, i);
            });
        };

        /**
         * calculates the minimum and maximum values of the data
         * and total sums. These are used to determine the range
         * in which the graph should scale.
         */
        function range () {
          var min = 0;
          var max = 0;
          for (var i = 0; i < opt.data.length; ++i) {
            var sum = 0;
            for (var j = 0; j < opt.series.length; ++j) {
              if (typeof opt.widthmeasure !== 'undefined') {
                if (opt.series[j] === opt.widthmeasure) {
                  continue;
                }
              }
              var v = opt.callback(j, i);
              min = Math.min(min, v);
              max = Math.max(max, v);
              if (opt.showCi) {
                min = Math.min(min, opt.callback(j, i, 1));
                max = Math.max(max, opt.callback(j, i, 2));
              }
              sum += v;
            }
            sums.push(sum);
            if (opt.stacked) {
              max = Math.max(max, sum);
            }
          }

        
          max = max * 1.2;

          if(min <= 0) {
            min = min * 1.2;
          } else {
            min = min - min * 0.2;
          }

          if (opt.percent) {
            domainRange = [0, 100];
          } else {
            domainRange = [Math.floor(min), Math.ceil(max)];
          }

          // Scale to range if present
          if (typeof opt.range !== 'undefined') {
            if (typeof opt.range[0] !== 'undefined') {
              domainRange[0] = +opt.range[0];
            }
            if (typeof opt.range[1] !== 'undefined') {
              domainRange[1] = +opt.range[1];
            }
          }
        }
        /**
         * Calculates the scaled value of a given entry in the data set
         * which may be based on the actual value of the value's percentage
         * of the series' sum
         */
        function value (series, i) {
          var val = opt.callback(series, i);
          if (opt.percent) {
            return scaleValue(100 * val / sums[i]);
          } else {
            return scaleValue(val);
          }
        }
        /**
         * Determines the value position in the
         * the value axis, which is the height or width
         * of the column in column or bar charts and
         * the y-coordinate for line charts
         */
        function size (series) {
          return function (d, i) {
            var val = scaleValue(0) - value(series, i);
            if (columnMekkoChartData[i]) {
              val = scaleValue(0) - value(series, columnMekkoChartData[i].itemIndex);
            }
            // if chart is stacked we have to use the actual value instead of
            // absolute value.
            return opt.stacked ? val : Math.abs(val);
          };
        }

        /**
         * Determines the offset where a bar
         * or a column should start. When the
         * column or bar chart is not stacked the
         * column should start at the y(0).
         * When the column or bar chart is stacked
         * then the bar or column or is started
         * from the cumulative value of previous
         * series.
         */
        function offsetColumn (series, p_offsets) {
          var offsets = p_offsets || [];
          return function (d, i) {
            if (opt.stacked) {
              if (offsets.length <= i) {
                offsets.push(value(series, i));
              } else {
                offsets[i] = offsets[i] - size(series)(d, i);
              }
              return offsets[i];
            } else {
              return value(series, i);
            }
          };
        }
        /**
         *  Same as offset column but for bar charts
         */
        function offsetBar (series, p_offsets) {
          var offsets = p_offsets || [];

          return function (d, i) {
            var r = scaleValue(0);
            if (opt.stacked) {
              if (offsets.length <= i) {
                offsets.push(value(series, i));
              } else {
                r = offsets[i];
                offsets[i] = offsets[i] - size(series)(d, i);
              }
            }
            return r;
          };
        }

        function label (d, i, series) {
          var label = labels[opt.dataset.Dimension(1).id[series]];
          if (opt.showCi || opt.showN) 
          {
            var sampleSize = opt.callback(series, i, 3);
            if (sampleSize) {
              label += ' (n = ' + sampleSize + ')';
            }
          }
          return label;
        }

        function showToolTip () {
          d3.event.preventDefault();
          var self = d3.select(this);
          self.attr('r', 3);
          self.attr('stroke-width', 3);
          tooltip.style('visibility', 'visible');
          tooltip.style('background-color', 'rgba(255,255,255,0.7)');
          tooltip.style('z-index', 1000);
          tooltip.style('border-color', self.attr('stroke') || self.attr('fill'));
          var title = '';          
          var txt = '';
          if(self.select('title')){
            title = self.select('title').text();
          }                
          try{
            txt = self.select('text').text();
          } catch (e) {}    
          if( txt && txt.size>0 ){
            title = title + ': ' + txt + ' '
          }  
          tooltip.text(title); 
          self.select('title').text('');        
          return false;
        }

        function hideToolTip () {
          var self = d3.select(this);         
          tooltip.style('visibility', 'hidden');
          self.attr('r', 4);
          self.attr('stroke-width', 1);     
          self.select('title').text(tooltip.text());     
        }

        function moveToolTip () {
          return tooltip
            .style('top', (d3.event.pageY - 10) + 'px')
            .style('left', (d3.event.pageX + 10) + 'px');
        }

        var draw = {
          /**
           * Plots a column chart where columns
           * may be grouped, stacekd or stacked using
           * percentage of total.
           *
           * The chart width is fixed and column
           * width is adjusted based on the number
           * of columns. If the chart contains
           * multiple series and series are not
           * stacked then columns are futher
           * adjusted so that each group contains
           * all value columns
           */
          'columnchart': function (svg) {
            var offsets = [],
              chartColumnWidth = opt.stacked ? ordinalScale.rangeBand() - 10 * barAndColumnMargin : (ordinalScale.rangeBand() - BAR_GROUP_MARGIN - opt.series.length * BAR_MARGIN) / (opt.series.length),
              scaledZero = scaleValue(0);

            svg
              .append('line')
              .style('stroke', '#808080')
              .attr('y1', scaledZero)
              .attr('y2', scaledZero)
              .attr('x1', yAxisPos)
              .attr('x2', xAxisWidth);

            for (var series = 0; series < opt.series.length; ++series) {
              var g = svg
                .append('g')
                .selectAll('g')
                .data(opt.data);

              // draw columns
              g.enter()
                .append('rect')
                .attr('class', 'series series' + series)
                .attr('fill', function (d, i) {
                  if (opt.em) {
                    return opt.em.indexOf(d) >= 0 ? colors[series] : '#808080';
                  } else {
                    return colors[series];
                  }
                })
                .attr('title', function (d, i) {
                  return opt.callback(series, i);
                })
                .attr('width', Math.max(chartColumnWidth, 1))
                .attr('height', size(series))
                .attr('x', function (d, i) {
                  if (opt.stacked) {
                    return ordinalScale(d) + 5 * barAndColumnMargin;
                  } else {
                    return ordinalScale(d) + BAR_GROUP_MARGIN / 1.5 + series * (chartColumnWidth + BAR_MARGIN);
                  }
                })
                .attr('y', opt.stacked ? offsetColumn(series, offsets) : function (d, i) {
                  var val = value(series, i);
                  return val - scaledZero >= 0 ? scaledZero : scaledZero + (val - scaledZero);
                })
                .style('cursor', function (d) {
                  if (canDrill(d)) {
                    return 'pointer';
                  }
                })
                .on('click', function (d) {
                  submitDrillDown(d, dimensionData[d].dim);
                })
                .on('mouseover', showToolTip)
                .on('mouseout', hideToolTip)
                .on('mousemove', moveToolTip)
                .append('svg:title')
                .text(function (d, i) {
                  return label(d, i, series) + '(' + labels[d] + '): ' + numberFormat(opt.callback(series, i));
                });

              if (opt.showCi) {
                drawWhiskers(g, series, scaledZero,
                  function (d, i) {
                    var val = scaleValue(opt.callback(series, i, 1));
                    return val - scaledZero >= 0 ? scaledZero : scaledZero + (val - scaledZero);
                  },
                  function (d, i) {
                    var val = scaleValue(opt.callback(series, i, 2));
                    return val - scaledZero >= 0 ? scaledZero : scaledZero + (val - scaledZero);
                  },
                  function (d, i) {
                    return ordinalScale(d) + BAR_GROUP_MARGIN / 1.5 + series * (chartColumnWidth + BAR_MARGIN) + chartColumnWidth / 2 ;
                  }, 'column');
              }
            }
          },
          /**
           * Plots a column chart where columnn width and height are defined in data.
           */
          'columnmekkochart': function (svg) {
            if (opt.series.length > 1) {
              console.warn('Multiple series is not supported for this chart type');
            }

            var scaledZero = scaleValue(0);
            svg
              .append('line')
              .style('stroke', '#808080')
              .attr('y1', scaledZero)
              .attr('y2', scaledZero)
              .attr('x1', yAxisPos)
              .attr('x2', xAxisWidth);

            var xPosition = yAxisPos;
            for (var series = 0; series < opt.series.length; ++series) {
              var g = svg
                .append('g')
                .selectAll('g')
                .data(Object.keys(columnMekkoChartData));

              // draw columns
              g.enter()
                .append('rect')
                .attr('class', 'series series' + series)
                .attr('fill', function (d, i) {
                  if (opt.em) {
                    var id = columnMekkoChartData[i].itemId;
                    return opt.em.indexOf(id) >= 0 ? colors[series] : '#808080';
                  } else {
                    return colors[series];
                  }
                })
                .attr('title', function (d, i) {
                  return opt.callback(series, i);
                })
                .attr('width', function(d, i) {
                  var width = columnMekkoChartData[i].itemWidth;
                  return columnMekkoChartWidthScale(width);
                })
                .attr('height', size(series))
                .attr('x', function (d, i) {
                  var prevXPosition = xPosition;
                  xPosition += Number(d3.select(this).attr('width'));
                  return prevXPosition;
                })
                .attr('y', function (d, i) {
                  var val = value(series, i);
                  if (columnMekkoChartData[i]) {
                    val = value(series, columnMekkoChartData[i].itemIndex);
                  }
                  return val - scaledZero >= 0 ? scaledZero : scaledZero + (val - scaledZero);
                })
                .style('cursor', function (d) {
                  if (canDrill(d)) {
                    return 'pointer';
                  }
                })
                .on('click', function (d) {
                  submitDrillDown(d, dimensionData[d].dim);
                })
                .on('mouseover', showToolTip)
                .on('mouseout', hideToolTip)
                .on('mousemove', moveToolTip)
                .append('svg:title')
                .text(function (d, i) {
                  if (opt.type === 'columnmekkochart') {
                    var itemIndex = columnMekkoChartData[i].itemIndex;
                    var widthMeasureValue = opt.callback(widthMeasureIndex, itemIndex);
                    return label(d, itemIndex, series) + '(' + labels[opt.widthmeasure] + ' ' + widthMeasureValue + '): ' + numberFormat(opt.callback(series, itemIndex));
                  }
                  return label(d, i, series) + '(' + labels[d] + '): ' + numberFormat(opt.callback(series, i));
                });

            }
          },
          /**
           * Plots a bar chart where bars may be
           * grouped, stacked or stacked using
           * percentage of total
           */
          'barchart': function (svg) {
            var offsets = [],
              g = svg.append('g').attr('transform', 'translate(' + opt.margin + ', 0)'),
              chartBarHeight = opt.stacked ? ordinalScale.rangeBand() - 2 * barAndColumnMargin : (ordinalScale.rangeBand() - opt.series.length * BAR_MARGIN - BAR_GROUP_MARGIN) / opt.series.length,
              scaledZero = scaleValue(0),
              padding = BAR_GROUP_MARGIN / 2.0;

            svg
              .append('line')
              .style('stroke', '#808080')
              .attr('x1', scaledZero + 10)
              .attr('x2', scaledZero + 10)
              .attr('y1', 0)
              .attr('y2', xAxisPos);

            for (var series = 0; series < opt.series.length; ++series) {
              var sg = g
                .append('g')
                .selectAll('g')
                .data(opt.data);

              sg.enter()
                .append('rect')
                .attr('class', 'series series' + series)
                .attr('fill', function (d, i) {
                  if (opt.em) {
                    return opt.em.indexOf(d) >= 0 ? colors[series] : '#519b2f';
                  } else {
                    return colors[series];
                  }
                })
                .attr('title', function (d, i) {
                  return opt.callback(series, i);
                })
                .attr('height', chartBarHeight)
                .attr('width', function (d, i) {
                  return Math.abs(value(series, i) - scaledZero);
                })
                .attr('x', opt.stacked ? offsetBar(series, offsets) : function (d, i) {
                  var val = value(series, i);
                  return val - scaledZero >= 0 ? scaledZero : scaledZero + (val - scaledZero);
                })
                .attr('y', function (d, i) {
                  if (!opt.stacked) {
                    return ordinalScale(d) + padding + series * (chartBarHeight + BAR_MARGIN);
                  } else {
                    return ordinalScale(d);
                  }
                })
                .style('cursor', function (d) {
                  if (canDrill(d)) {
                    return 'pointer';
                  }
                })
                .on('click', function (d) {
                  submitDrillDown(d, dimensionData[d].dim);
                })
                .on('mouseover', showToolTip)
                .on('mouseout', hideToolTip)
                .on('mousemove', moveToolTip)
                .append('svg:title')
                .text(function (d, i) {
                  return label(d, i, series) + '(' + labels[d] + '): ' + numberFormat(opt.callback(series, i));
                });
              if (opt.showCi) {
                drawWhiskers(sg, series, scaledZero,
                  function (d, i) {
                    return scaleValue(opt.callback(series, i, 1));
                  },
                  function (d, i) {
                    return scaleValue(opt.callback(series, i, 2));
                  },
                  function (d, i) {
                	  if (!opt.stacked) {
                          return ordinalScale(d) + chartBarHeight + series * (chartBarHeight + BAR_MARGIN )-(BAR_MARGIN/2+1);
                        } else {
                          return ordinalScale(d) + chartBarHeight  ;
                        }
                  }, 'bar');
              }
            }
          },
          /**
           * Plots a square angled line for each series where line
           * points are y(x)
           */
          'linechart': function (svg) {
            for (var series = 0; series < opt.series.length; ++series) {
              var datum = opt.data

              // plot ci
              if (opt.showCi) {
                var area =
                  d3.svg.area()
                    .defined(function(d, i) { return null != opt.callback(series, i);})
                    .x(function (d, i) {
                      return ordinalScale(datum[i]) + spacing / 2.0 - 3;
                    })
                    .y0(function (d, i) {
                      return scaleValue(opt.callback(series, i, 1));
                    })
                    .y1(function (d, i) {
                      return scaleValue(opt.callback(series, i, 2));
                    });
                svg
                  .append('path')
                  .data([datum])
                  .attr('fill', colors[series])
                  .attr('fill-opacity', 0.3)
                  .attr('d', area);
              }

              // Plot lines
              svg
                .append('path')
                .attr('class', 'series series' + series)
                .attr('stroke', series < colors.length ? colors[series] : '#000')
                .attr('stroke-width', 2)
                .attr('fill', 'none')
                .datum(datum)
                .attr('d', d3.svg.line()
                  .y(offsetColumn(series))
                  .defined(function(d, i) { return null != opt.callback(series, i);})
                  .x(function (d, i) {
                    return ordinalScale(d) + spacing / 2.0 - 3;
                  })
              )
                .append('svg:title')
                .text(function (d, i) {
                  return labels[opt.dataset.Dimension(1).id[series]];
                });

              // Plot tick marks
              svg
                .append('g')
                .selectAll('g')
                .data(datum)
                .enter()
                .append('circle')
                .attr('class', 'series series' + series)
                .attr('fill', '#fff')
                .attr('stroke', colors[series])
                .attr('r', function(d, i) {
                  return opt.callback(series, i) == null ? 0 : 5;
                })
                .attr('stroke-width', function(d, i) {
                  return opt.callback(series, i) == null ? 0 : 3;
                })
                .attr('cx', function (d, i) {
                  return ordinalScale(d) + spacing / 2.0 - 3;
                })
                .attr('cy', offsetColumn(series))
                .on('mouseover', showToolTip)
                .on('mouseout', hideToolTip)
                .on("mousemove", moveToolTip)
                .append('svg:title')
                .text(function (d, i) {
                  return label(d, i, series) + '(' + labels[d] + '): ' + numberFormat(opt.callback(series, i));
                });
              }
          },
          'piechart': function (svg) {
            var dataIndex = [];
            var nonZeroData = opt.data
              .filter(function (v, i) {
                // Filter out zero values as arc length of 0 = NaN
                // Keep tabs of the original indices as they are
                // needed to access the data later on
                if (opt.callback(0, i) !== 0) {
                  dataIndex.push(i);
                  return true;
                }
                return false;
              });

            // Define the arc radius for the pie chart
            var innerArcRadius = (opt.chartHeight - 8 * opt.margin) / 2;
            var arc = d3.svg.arc()
              .outerRadius(innerArcRadius)
              .innerRadius(0);

            // Define the arc radius for the in graph value labels
            var outerArc = d3.svg.arc()
              .outerRadius(innerArcRadius + 2 * opt.margin)
              .innerRadius(innerArcRadius + 2 * opt.margin);
          
            var lineArc = d3.svg.arc()
              .outerRadius(innerArcRadius + 2 * opt.margin+5)
              .innerRadius(innerArcRadius - 2 * opt.margin);

            var secondArc = d3.svg.arc()
              .outerRadius(innerArcRadius + 1 * opt.margin)
              .innerRadius(innerArcRadius + 1 * opt.margin);

            var pie = d3.layout.pie(nonZeroData)
              .sort(null)
              .value(function (d, i) {
                return opt.callback(0, dataIndex[i]);
              });
            var c = svg.append('g')
              .attr('transform', 'translate(' + opt.width / 2 + ',' + opt.chartHeight / 2 + ')');

            var g = c.selectAll('.arc')
              .data(pie(nonZeroData))
              .enter()
              .append('g')
              .attr('class', function (d, i) { return 'arc series series' + i; })
              .style('cursor', function (d) {
                if (canDrill(d.data)) {
                  return 'pointer';
                }
              })
              .on('click', function (d, i) {
                var nodeId = opt.dataset.Dimension(0).id[dataIndex[i]];
                submitDrillDown(nodeId, dimensionData[nodeId].dim);
              })
              .on('mouseover', showToolTip)
              .on('mouseout', hideToolTip)
              .on('mousemove', moveToolTip)
              ;
            g.append('svg:title')
              .text(function (d, i) {
                return labels[opt.dataset.Dimension(0).id[dataIndex[i]]];
              });
           
           
            g.append('path')
            .attr('d', arc)
            .style('fill', function (d, i) {
              return colors[dataIndex[i]];
            })
            .style('stroke', '#fff');      

            function midAngle (d) {
              return d.startAngle + (d.endAngle - d.startAngle) / 2;
            }
            var toggleArc=false;
            g.append('text')
              .attr('transform', function (d) { 
                var pos = outerArc.centroid(d);
                if(toggleArc){
                  pos = secondArc.centroid(d);                  
                }
                toggleArc = toggleArc?false:true;
                pos[0] = innerArcRadius * (midAngle(d) < Math.PI ? 1.2 : -1.2);
                return 'translate(' + pos + ')';
              })
              .attr('dy', '.35em')
              .style('text-anchor', function (d) {
                return midAngle(d) < Math.PI ? 'start' : 'end';
              })
              .text(function (d, i) {
                return numberFormat(opt.callback(0, dataIndex[i]));
              });
              toggleArc=false;
              g.append('polyline')
              .attr('points', function (d) {
                if (d.value !== 0) {
                  var pos = outerArc.centroid(d);
                  var anglePos = outerArc.centroid(d);
                  if(toggleArc){
                    pos = secondArc.centroid(d);   
                    anglePos = secondArc.centroid(d);              
                  }
                  toggleArc = toggleArc?false:true;
                  var angle = midAngle(d);
                  pos[0] = innerArcRadius * 0.95 * (midAngle(d) < Math.PI ? 1.2 : -1.2);
                  return [lineArc.centroid(d), anglePos, pos];
                }
              })
              .attr('fill', 'none')
              .attr('stroke', '#c3c2c6  ');
          },
          'radarchart': function (chart) {
    
            var minValue = domainRange[0];
            var maxValue = domainRange[1];
            var factor = 1;
            var radians = 2 * Math.PI;
            var total = opt.data.length;
            var radius = factor*(opt.height - 10 * opt.margin)/2;
            var offset = opt.data.length % 2 !== 0 ? 0 : 0;
            var radiansPerSegment = radians/total;
            var axisTicks = 5;
            var centerOffset = opt.width/2 - radius;
            var datum = opt.data.slice().reverse();
             
            var radianFactor = [];
             for (var i = 0; i < opt.data.length; ++i) {
              radianFactor.push([
                radius * Math.sin((i + offset)*radiansPerSegment),
                radius * Math.cos((i + offset)*radiansPerSegment)
              ]);
             }

            var g = chart
              .append('g');
             
      
            for(var j=0; j < axisTicks; j++){
              var levelFactor = factor*radius*((j+1)/axisTicks);
              g.selectAll(".levels")
               .data(datum)
               .enter()
               .append("svg:line")
               .attr("x1", function(d, i){return levelFactor*(1-factor*Math.sin(i*radiansPerSegment));})
               .attr("y1", function(d, i){return levelFactor*(1-factor*Math.cos(i*radiansPerSegment));})
               .attr("x2", function(d, i){return levelFactor*(1-factor*Math.sin((i+1)*radiansPerSegment));})
               .attr("y2", function(d, i){return levelFactor*(1-factor*Math.cos((i+1)*radiansPerSegment));})
               .attr("class", "line")
               .style("stroke", "grey")
               .style("stroke-opacity", "0.75")
               .style("stroke-width", "0.3px")
               .attr("transform", "translate(" + (radius-levelFactor) + ", " + (radius-levelFactor) + ")");
            }

            for(var j=0; j < axisTicks; j++){
              var levelFactor = radius*((j+1)/axisTicks);
              g.selectAll(".levels")
               .data([1]) //dummy data
               .enter()
               .append("svg:text")
               .attr("x", function(d){return levelFactor*(1-Math.sin(0));})
               .attr("y", function(d){return levelFactor*(1-Math.cos(0));})
               .attr("class", "legend")
               .style("font-family", "sans-serif")
               .style("font-size", "10px")
               .attr("transform", "translate(" + (radius-levelFactor + 2) + ", " + (radius-levelFactor) + ")")
               .attr("fill", "#737373")
               .text(numberFormat((j+1)*(maxValue-minValue)/axisTicks + minValue));
            }
            

             var axis = g.selectAll(".axis")
              .data(datum)
              .enter()
              .append("g")
              .attr("class", "axis");
              axis.append("line")
                  .attr("x1", radius)
                  .attr("y1", radius)
                  .attr("x2", function(d, i){return radius*(1-factor*Math.sin(i*radiansPerSegment));})
                  .attr("y2", function(d, i){return radius*(1-factor*Math.cos(i*radiansPerSegment));})
                  .attr("class", "line")
                  .style("stroke", "grey")
                  .style("stroke-width", "1px");
            
              var maxAxisLabelHeight = 0;
              var axisLabels = axis.append("text")
                .attr("class", "legend")
                .text(function(d){return labels[d]; })
                .style("font-family", "Source sans pro")
                .style("font-size", "11px")
                .attr("text-anchor", "middle")
                .attr("dy", "1.5em")
                .attr("transform", function(d, i){return "translate(0, -10)"})
                .attr("x", function(d, i){return radius*(1-Math.sin((i+1)*radiansPerSegment))-80*Math.sin((i+1)*radiansPerSegment);})
                .attr("y", function(d, i){return radius*(1-Math.cos((i+1)*radiansPerSegment))-20*Math.cos((i+1)*radiansPerSegment);})
                .call(wrap, 35 + radius*(1-Math.sin(1)*radiansPerSegment)-60*Math.sin(1*radiansPerSegment))
                .call(function(text) {
                  text.each(function(d, i) {
                    var text = d3.select(this);
                    maxAxisLabelHeight = Math.max(maxAxisLabelHeight, text.node().getBBox().height);
                    if(i * radiansPerSegment < 0.5*Math.PI || i * radiansPerSegment > 1.25*Math.PI) {
                      text.selectAll('tspan').attr('y', text.attr('y') - Math.cos((i+1)*radiansPerSegment) * text.node().getBBox().height);
                    }
                  });
                });

              var legendHeight = chart.select('.legend').node().getBBox().height;
              var scaleFactor = (opt.height - legendHeight - opt.margin * 3) / opt.height;

              g.attr('transform', 'scale('+ scaleFactor+') translate(' + centerOffset/scaleFactor + ',' + (opt.margin * 3 + maxAxisLabelHeight-20)+ ')');

             for (var series = 0; series < opt.series.length; ++series) {
              var sg = g
                .append('g')
                .selectAll('g')
                .data([1]);
              sg.enter()
                .append('polygon')
                .attr('class', 'series series' + series)
                .attr('fill-opacity', '0.0')
                .attr('fill', function (d, i) {
                    return colors[series];
                  })
                .attr('stroke-width', '1px')
                .attr('stroke', function (d, i) {
                  return colors[series];
                })
                .attr("points",function(d) {
                 var str="";
                 for(var i = 0; i < opt.data.length; ++i) {
                    var val = Math.max(Math.min(opt.callback(series,i), maxValue), minValue)
                    val = (val - minValue) / (maxValue-minValue);
                    var x = radius + val*radianFactor[i][0];
                    var y = radius + -val*radianFactor[i][1];
                    str = str + x + "," + y + " ";
                 }
                 return str;
                });
              }
              for (var series = 0; series < opt.series.length; ++series) {
                 g
                  .append('g')
                  .selectAll('g')
                  .data(opt.data)
                  .enter()
                  .append('circle')
                  .attr('class', 'series series' + series)
                  .attr('fill', '#fff')
                  .attr('stroke', colors[series])
                  .attr('r', function(d, i) {
                    return opt.callback(series, i) == null ? 0 : 4;
                  })
                  .attr('stroke-width', function(d, i) {
                    return opt.callback(series, i) == null ? 0 : 2;
                  })
                  .attr('cx', function (d, i) {
                    var val = Math.max(Math.min(opt.callback(series,i), maxValue), minValue)
                    val = (val - minValue) / (maxValue-minValue);
                    return  radius + val*radianFactor[i][0];
                  })
                  .attr('cy', function (d, i) {
                    var val = Math.max(Math.min(opt.callback(series,i), maxValue), minValue)
                    val = (val - minValue) / (maxValue-minValue);
                    return radius + -val*radianFactor[i][1];
                  })
                  .style('cursor', function (d, i) {
                    if (canDrill(d)) {
                      return 'pointer';
                    }
                  })
                  .on('click', function (d) {
                    submitDrillDown(d, dimensionData[d].dim);
                  })
                  .on('mouseover', showToolTip)
                  .on('mouseout', hideToolTip)
                  .on('mousemove', moveToolTip)
                  .append('svg:title')
                  .text(function (d, i) {
                    return label(d, i, series) + '(' + labels[opt.data[i]] + '): ' + numberFormat(opt.callback(series, i));
                  });
                
            }

          },
          'gaugechart': function (chart) {
            chart = chart.append('g');

            var scaleRadiusOuter = (opt.chartHeight - 2 * opt.margin) / 2,
              scaleRadiusInner = scaleRadiusOuter - 15,
              lineRadiusOuter = scaleRadiusInner - 15,
              lineRadiusInner = lineRadiusOuter - 1;
            var textStyle = {
              'class': 'd3font',
              'font-size': '14px'
            };

            // ulompi asteikko
            var scale = d3.svg.arc()
              .outerRadius(scaleRadiusOuter)
              .innerRadius(scaleRadiusInner)
              .startAngle(function (d) {
                return cScale(d[0]);
              })
              .endAngle(function (d) {
                return cScale(d[1]);
              });


            var limits;
            var areas = opt.limitAreas();
            var areaLimits = [];
            for(var i = 0; i < areas.length; ++i) {
                if(i == 0) {
                    areaLimits[i] = 0
                } else {
                    areaLimits[i] = areaLimits[i-1] + areas[i - 1]
                }
            }
            if(opt.limits === undefined || opt.limits === '') {
               var min = Number.MAX_VALUE, max = Number.MIN_VALUE;
               $.each(opt.data, function(k) {
                var v = opt.callback(0, k);
                min = Math.min(min, v);
                max = Math.max(max, v);
               });
               limits = [min, Math.round((0.25*(max-min)), 1), Math.round((0.75*(max-min)),1), max];
            } else {
               var limits = opt.limits.split(',');
               for(var i = 0; i < limits.length; ++i) {
                limits[i] = +limits[i];
               }
            }
            var scaleData = [];
            var scaleMin = limits[0];
            var scaleMax = limits[limits.length - 1];
            for(var i = 1; i < limits.length; ++i) {
              scaleData.push([limits[i-1], limits[i], '']);
            }
            scaleData.push([limits[limits.length-1], limits[limits.length-1]]);

            function transformAngle(a) {
                if(areas.length > 0) {
                    for(var i = 0; i < limits.length - 1; ++i) {
                        if(a <= limits[i + 1]) {
                            var r = ((a - limits[i]) / (limits[i + 1] - limits[i]));
                            return areaLimits[i] + areas[i]*r;
                        }
                    }
                } else {
                    return a;
                }
            }

          // cScale is for drawing gauge background (uses radians)
            var cScale = d3.scale.linear()
              .range([
                60 * (Math.PI / 180), 300 * (Math.PI / 180)
              ])
              .domain([
                scaleMin, scaleMax
              ]);
            // needleAngleScale is for drawing needles at right angle (uses degrees)
            var needleAngleScale = d3.scale.linear()
              .range([-120, 120])
              .domain([scaleMin, scaleMax]);

            var palette = [];
            switch (opt.palette) {
              case 'greenyellowred':
                palette = ['#7699d6', '#b2b2b2', '#d888a9'];
                break;
              case 'gray':
                palette = ['#8c8c8c', '#b2b2b2', '#8c8c8c'];
                break;
              default:
                palette = ['#d888a9', '#b2b2b2', '#7699d6'];
            }


            chart.append('g')
              .attr('class', 'scale')
              .selectAll('path')
              .data(scaleData)
              .enter()
                  .append('path')
                  .attr('class', function (d) {
                    return 'scale ' + d[2];
                  })
                  .attr('id', function(d,i) {return opt.domTarget.id + '-limit-' + i;})
                  .attr('fill', function (d, i) {
                    return palette[i];
                  })
                  .attr('d', function(d,i) {
                    return scale([transformAngle(d[0]), transformAngle(d[1])]);
                  })
                  .attr('transform', 'translate(200,200) rotate(180)');

             var lbl = opt.limitLabels();

             if(lbl.length > 0) {
                 chart.append('g')
                   .attr('class', 'axistitle')
                   .selectAll('text')
                   .data(scaleData)
                   .enter()
                    .append('text')
                    .attr('text-anchor', 'start')
                    .attr('dy', -6)
                    .append('textPath')
                    .attr('title', function(d, i) { return lbl[i];})
                    .text(function(d, i) { return lbl[i];})
                    .attr('xlink:href', function(d,i) {return '#' + opt.domTarget.id + '-limit-' + i;})
                    ;
             } else {
                chart.append('g')
                  .attr('class', 'axistitle')
                  .attr('transform', 'translate(200,200)')
                  .selectAll('text')
                  .data(scaleData)
                  .enter()
                        .append('text')
                        .attr('x', function(d, i) {
                            return (lineRadiusInner - 20) * Math.sin(-cScale(transformAngle(d[0])))
                         })
                        .attr('y', function(d) {
                            return (lineRadiusInner - 20) * Math.cos(-cScale(transformAngle(d[0])));
                        })
                        .attr(textStyle)
                        .attr('text-anchor', function(d) {
                            if(transformAngle(d[0]) < 40) { return 'start'; }
                            if(transformAngle(d[0]) > 60) { return 'end'; }
                            return 'middle';
                        })
                        .attr('title', function(d, i) { return numberFormat(d[0]); })
                        .text(function(d) { return numberFormat(d[0]); });
            }

            chart.append('g')
                  .attr('class', 'axistics')
                  .attr('transform', 'translate(200,200)')
                  .selectAll('line')
                  .data(scaleData)
                  .enter()
                        .append('line')
                        .attr('x1', function(d, i) {
                            return (lineRadiusOuter - 10) * Math.sin(-cScale(transformAngle(d[0])))
                         })
                        .attr('y1', function(d) {
                            return (lineRadiusOuter - 10) * Math.cos(-cScale(transformAngle(d[0])));
                        })
                        .attr('x2', function(d, i) {
                            return (lineRadiusOuter) * Math.sin(-cScale(transformAngle(d[0])))
                         })
                        .attr('y2', function(d) {
                            return (lineRadiusOuter) * Math.cos(-cScale(transformAngle(d[0])));
                        })
                        .attr('stroke','#ddd')

            // Pelkkä pikseliviiva
            var arc = d3.svg.arc()
              .outerRadius(lineRadiusOuter)
              .innerRadius(lineRadiusInner)
              .startAngle(55 * (Math.PI / 180))
              .endAngle(305 * (Math.PI / 180));

            var plot = chart.append('g')
              .attr('class', 'arc');

            plot.append('path')
              .attr('d', arc)
              .attr('class', 'gauge')
              .style('fill', '#ddd')
              .attr('transform', 'translate(200,200) rotate(180)');

            // viisari svg path
            var needlepoints = 'M23.1,149.3L15.7,2.4C15.6,1.1,14.5,0,13.1,0s-2.6,1.1-2.6,2.4L2.9,149.3c-1.8,2.2-2.9,5-2.9,8 c0,7,5.8,12.7,13,12.7c7.2,0,13-5.7,13-12.7C26,154.3,24.9,151.5,23.1,149.3z';
            // needles
            var needle = chart.selectAll('g.needle')
              .data(opt.data)
              .enter()
              .append('g')
              .attr('class', function (d, i) {
                return 'needle series series' + i;
              })
              .attr('transform', function (d, i) {
                var v = opt.callback(0, i);
                return 'translate(' + (scaleRadiusOuter-3) + ', ' + 45 + ') rotate(' + needleAngleScale(transformAngle(v)) + ', 13, 157)';
              })
              .style('cursor', function (d) {
                if (canDrill(d)) {
                  return 'pointer';
                }
              })
              .on('click', function (d, i) {
                var nodeId = opt.dataset.Dimension(0).id[i];
                submitDrillDown(nodeId, dimensionData[nodeId].dim);
              })
              .on('mouseover', showToolTip)
              .on('mouseout', hideToolTip)
              .on('mousemove', moveToolTip);

            needle.append('svg:title')
              .text(function (d, i) {
                return labels[opt.dataset.Dimension(0).id[i]] + ': ' + numberFormat(opt.callback(0, i));
              });

            needle.append('path')
              .attr('fill', function (d, i) {
                return colors[i];
              })
              .attr('d', needlepoints);

            // valkoinen keskiympyrä
            chart.append('circle')
              .attr('class', 'circle')
              .attr('r', 12)
              .attr('cx', 200)
              .attr('cy', 201)
              .attr('fill', '#fff')
              // use color of top most needle
              // FIXME: doesn't work when multiple needles are exactly on top of each
              // other
              .attr('stroke', colors[opt.data.length - 1])
              .attr('stroke-width', 4);

            function appendLine (chart, x1, x2, y1, y2, strokeColor) {
              chart.append('line')
                .attr('x1', x1)
                .attr('x2', x2)
                .attr('y1', y1)
                .attr('y2', y2)
                .attr('stroke', strokeColor);
            }

            function appendText (chart, x, y, styleAttributes, text) {
              chart.append('text')
                .attr('x', x)
                .attr('y', y)
                .attr(styleAttributes)
                .attr('title', numberFormat(text))
                .text(numberFormat(text));
            }

          }
        };

        /**
         * Draws a vertical value axis
         * that uses valueScale to determine
         * axis domain
         */
        function drawVerticalValueAxis (yAxisPos, svg) {
         svg
            .append('g')
            .attr('class', 'axis')
            .attr('transform', 'translate(' + yAxisPos + ', 0)')
            .call(
              d3.svg.axis()
                .orient('left')
                .scale(scaleValue)
                .tickFormat(locale.numberFormat(',2r'))
                .innerTickSize(-(xAxisWidth - 4 * opt.margin))
          );
        }

        function drawHorizontalValueAxis (svg) {
          var axis = svg
            .append('g')
            .attr('class', 'x axis')
            .attr('transform', 'translate(' + opt.margin + ',' + xAxisPos + ')')
            .call(
              d3.svg.axis()
                .orient('bottom')
                .scale(scaleValue)
                .tickFormat(locale.numberFormat(',2r'))
                .innerTickSize(-xAxisPos)
          );
          if (isXAxisTicksTilted) {
            svg
              .selectAll('.x.axis text')
              .style('text-anchor', 'end')
              .attr('x', '-.8em')
              .attr('dy', '.15em')
              .attr('transform', 'rotate(-45)');
          }
        }

        /**
         * Draws a horizontal axis for
         * ordinal values for which y(x) is called
         */
        function drawHorizontalOrdinalAxis (svg, showInnerTick, scaleFunction) {
          var xAxis = d3.svg.axis()
            .scale(scaleFunction || ordinalScale)
            .orient('bottom')
            .tickFormat(function (d, i) {
              var labelId = (opt.type === 'columnmekkochart' ? columnMekkoChartData[i].itemId : d);
              if (opt.legendless === 'yes' && ['columnchart', 'columnmekkochart'].indexOf(opt.type) >= -1) {
                return '';
              }
              else if (opt.legendless === 'nonEmphasized' && ['columnchart', 'columnmekkochart'].indexOf(opt.type) >= -1) {
                return opt.em.indexOf(labelId) >= 0 ? labels[labelId] : '';
              }
              else {
                return pxWidth(labels[labelId].length) <= MAX_LABEL_LENGTH ? labels[labelId] : labels[labelId].substring(0, MAX_LABEL_LENGTH / CHARACTER_WIDTH - 3) + '...';
              }
            });
          if (opt.type === 'columnmekkochart') {
            xAxis
              .ticks(opt.dataset.Dimension(0).length)
              .tickValues(function() {
                return Object.values(columnMekkoChartData)
                  .sort(function(a, b) { return a.sortOrder - b.sortOrder; })
                  .map(function(dataItem) { return dataItem.itemXPosition});
              });
          }
          if (showInnerTick) {
            xAxis.innerTickSize(-xAxisPos);
          }
          svg.append('g')
            .attr('class', 'x axis')
            .attr('transform', 'translate(0, ' + (xAxisPos + (opt.type === 'linechart' ? 5 : 0)) + ')')
            .call(xAxis)
            .on('mouseover', function (d) {
              // TODO
            })
            .append('svg:title')
            .text(function (d) {
              return labels[d];
            });

          if (isXAxisTicksTilted) {
            svg
              .selectAll('.x.axis text')
              .style('text-anchor', 'end')
              .attr('x', '-.8em')
              .attr('dy', '.15em')
              .attr('transform', 'rotate(-45)');
          }
        }

        /**
         * Draws a horizontal axis for
         * ordinal values for which y(x) is called
         */
        function drawVerticalOrdinalAxis (yAxisPos, svg, showInnerTick) {
          var yAxis = d3.svg.axis()
            .scale(ordinalScale)
            .orient('left')
            .tickFormat(function (d) {
              return pxWidth(labels[d]?labels[d].length:0) <= MAX_LABEL_LENGTH ? labels[d] : labels[d].substring(0, MAX_LABEL_LENGTH / CHARACTER_WIDTH - 3) + '...';
            });
          if (showInnerTick) {
            yAxis.innerTickSize(-xAxisWidth + 4 * opt.margin);
          }
          var axis = svg.append('g')
            .attr('class', 'y axis')
            .attr('transform', 'translate(' + yAxisPos + ',0)')
            .call(yAxis)
            .selectAll('.tick')
            .on('mouseover', function (d) {
              // TODO
            })
            .append('svg:title')
            .text(function (d) {
              return labels[d];
            });
        }

        function drawLegend (svg, labelSeries, columns, offset, labelLength) {
          var dataIndex = [];
          var nonZeroData = [];
          if(opt.type === 'piechart') {
            nonZeroData = opt.data
            .filter(function (v, i) {
              // Filter out zero values as arc length of 0 = NaN
              // Keep tabs of the original indices as they are
              // needed to access the data later on
              if (opt.callback(0, i) !== 0) {
                dataIndex.push(i);
                return true;
              }
              return false;
            });
          }

          var g = svg.append('g')
            .attr('class', 'legend')
            .attr('transform', 'translate(' + (opt.margin + 10) + ', ' + (offset + 20) + ')');
          var xOffset = 0;
          var yOffset = 0;
          var lastLegendGroup = null;
          for (var i = 0; i < labelSeries.length; ++i) {
            if (opt.type !== 'piechart' || dataIndex.indexOf(i) >= 0) {
              var j = i;
              if (opt.type === 'piechart') {
                j = dataIndex.indexOf(i);
              }
              var label = labels[labelSeries[i]];
              if (opt.type === 'columnmekkochart') {
                label = 'x: ' + labels[opt.widthmeasure] + ', y: ' + label;
              }

              if (lastLegendGroup != null) {
                xOffset += lastLegendGroup.node().getBBox().width + 30 - lastLegendGroup.node().getBBox().x;
              }
              if (xOffset + pxWidth(label.length) + 30 > opt.width) {
                xOffset = 0;
                yOffset += 20;
              }

              var sg = g.append('g');
              lastLegendGroup = sg;
              sg.attr('transform', 'translate(' + xOffset + ', ' + yOffset + ')');
              sg.attr('data-ref', j);
              sg.on('mouseover', function () {
                $(svg.node()).closest('.presentation').addClass('highlight');
                svg.selectAll('.series' + d3.select(this).attr('data-ref')).classed('highlight', true);
              });
              sg.on('mouseout', function () {
                $(svg.node()).closest('.presentation').removeClass('highlight');
                svg.selectAll('.series' + d3.select(this).attr('data-ref')).classed('highlight', false);
              });
              sg.append('rect')
                .attr('fill', colors[i])
                .attr('width', 10)
                .attr('height', 10)
                .attr('x', 0)
                .attr('y', -9);
              sg.append('text')
                .text(label)
                .attr('x', 13);
              sg.attr('width');
            }
          }
          return yOffset;
        }

        function pxWidth (strLen) {
          // Assume 3:5 width to height ratio for the font size
          // Assume 12px font size
          return strLen * CHARACTER_WIDTH;
        }

        /**
         * Calculates the width of the widest label in the
         * ordinal axis.
         */
        function calculateMaxLabelSize (data) {
          var maxLength = 0;
          $.each(data, function (i, v) {
            maxLength = Math.max(labels[v]?labels[v].length:0, maxLength);
          });

          return Math.min(pxWidth(maxLength), MAX_LABEL_LENGTH);
        }


        /**
         * Calculates the width of the widest
         * value in the value axis. The widest value may be the
         * smallest or the largest value of the domain.
         * Assume equal decimal precision on all values in
         * axis labels.
         */
        function calculateValueSize (range) {
          var max = Math.max(
            (range[0].toString().replace(/(\d)(?=(\d{3})+(\.|$))/g, '$1 ')).length,
            (range[1].toString().replace(/(\d)(?=(\d{3})+(\.|$))/g, '$1 ')).length
          );

          return Math.max(pxWidth(max) + (max / 3.0 * CHARACTER_WIDTH), MINIMUM_VALUE_LABEL_WIDTH);
        }

        /**
         * Creates a sorted copy of opt.data in the preferred order
         * or if no sorting is enable then opt.data itself.
         * We have to return a sorted copy instead of sorting the data
         * in place as json stat callback are implemented using
         * index reference instead of id references. If the original
         * domain is out of order we no longer can determine the correct
         * index for each domain id.
         */
        function sortData (sortMode) {

          if (sortMode === 'asc' || sortMode === 'desc') {
            var data = opt.data.slice();
            data.sort(function (a, b) {
              var v1 = opt.callback(0, opt.data.indexOf(a));
              var v2 = opt.callback(0, opt.data.indexOf(b));
              if (v1 === v2) {
                return 0;
              }
              return v1 < v2 ? -1 : 1;
            });
            if (sortMode === 'desc') {
              data.reverse();
            }
            return data;
          }
          return opt.data;
        }

        var locale = d3.locale({
          'decimal': ',',
          'thousands': ' ',
          'grouping': [3],
          'currency': ['€', ''],
          'dateTime': '%a %b %e %X %Y',
          'date': '%d.%m.%Y',
          'time': '%H:%M:%S',
          'periods': ['AM', 'PM'],
          'days': ['Sunday', 'Monday', 'Tuesday', 'Wednesday', 'Thursday', 'Friday', 'Saturday'],
          'shortDays': ['Sun', 'Mon', 'Tue', 'Wed', 'Thu', 'Fri', 'Sat'],
          'months': ['January', 'February', 'March', 'April', 'May', 'June', 'July', 'August', 'September', 'October', 'November', 'December'],
          'shortMonths': ['Jan', 'Feb', 'Mar', 'Apr', 'May', 'Jun', 'Jul', 'Aug', 'Sep', 'Oct', 'Nov', 'Dec']
        });

        ordinalScale = d3.scale.ordinal().domain(sortData(opt.target.attr('data-sort')));

        barGroupHeight = barAndColumnMargin;
        // opt.series.length === 1 ?
        // barHeight + barAndColumnMargin :
        // opt.data.length * (barHeight + barAndColumnMargin);

        range();
        scaleValue = d3.scale.linear().domain(domainRange);

        maxLabelLength = calculateMaxLabelSize(opt.data);
        maxValueLength = calculateValueSize(domainRange);
        yAxisPos = (
          ['barchart'].indexOf(opt.type) >= 0 ? maxLabelLength + 20 : maxValueLength
        );
        xAxisWidth = opt.width - yAxisPos;

        isXAxisTicksTilted = function() {
          // x axis should be tilted if the longest tick label
          // may overlap another tick label.
          if ('barchart' === opt.type) {
            return maxValueLength > xAxisWidth / scaleValue.ticks().length;
          }
          else if ('columnmekkochart' === opt.type) {
            var legendless = opt.legendless || 'N/A';
            if (legendless === 'yes') {
              return false;
            }
            return legendless !== 'nonEmphasized' || legendless === 'nonEmphasized' && (!opt.em || opt.em.length > 1);
          } 
          else {
           return maxLabelLength > xAxisWidth / opt.data.length;
          }
        }();

        if (isXAxisTicksTilted) {
          // When x axis is tilted the first one or two tick labels
          // may be long enough to overflow the chart area which
          // results to clipping. If this is going to happen
          // we have to widen the space reserved for the y axis
          var labelLength = ['barchart'].indexOf(opt.type) >= 0 ? maxValueLength / 1.42 : maxLabelLength / 1.42;
          if (labelLength > yAxisPos) {
            yAxisPos = labelLength;
          }
        }
        xAxisPos = opt.height - opt.margin - 6 - (
          isXAxisTicksTilted ? (
            // if x axis is tilted 45 degrees then
            // the space between the axis and the bottom of the
            // graph is width of the longest tick label
            // scaled to the hypotenuse of a right angled
            // equilateral triangle which is sqrt(2)
            //
            ['barchart'].indexOf(opt.type) >= 0 ? maxValueLength / 1.42 : maxLabelLength / 1.42
            ) : opt.margin
        );
        spacing = xAxisWidth / opt.data.length;

        var svgContainer =
          opt.target
            .append('div')
            .attr('class', 'svg-container'),
          svg = svgContainer
            .append('svg')
            .attr('xmlns', 'http://www.w3.org/2000/svg')
            .attr('preserveAspectRatio', 'xMinYMin meet');
        var tooltip = d3.select('body')
            .append('div')
            .style('position', 'absolute')
            .style('z-index', '10')
            .style('visibility', 'hidden')
            .style('background', '#fff')
            .style('border', '2px solid #808080')
            .style('padding', '6px 12px')
            .style('border-radius', '4px');

        if (['columnchart', 'linechart', 'columnmekkochart'].indexOf(opt.type) >= 0) {
          scaleValue.range([xAxisPos, opt.margin]);
          ordinalScale.rangeRoundBands([yAxisPos, xAxisWidth]);
          drawVerticalValueAxis(yAxisPos, svg);
          if (opt.type === 'columnmekkochart') {
            computeColumnMekkoChartVariables();
            columnMekkoChartWidthScale = d3.scale
              .linear()
              .domain([0, columnMekkoChartDomainLimit])
              .range([yAxisPos, xAxisWidth]);

            drawHorizontalOrdinalAxis(svg, opt.type === 'linechart', columnMekkoChartWidthScale);
            // change range because axis area and column area have different offsets
            columnMekkoChartWidthScale.range([0, xAxisWidth - yAxisPos]);
          }
          else {
            drawHorizontalOrdinalAxis(svg, opt.type === 'linechart');
          }
        } else if (['barchart'].indexOf(opt.type) >= 0) {
          if (opt.stacked) {
            opt.height = opt.data.length * (barHeight + BAR_GROUP_MARGIN) + opt.margin * 2;
          } else {
            opt.height = opt.series.length * (opt.data.length * (barGroupHeight + 15) + opt.margin * 2) + opt.data.length * BAR_GROUP_MARGIN;
          }
          if (opt.data.length * opt.series.length === 1) {
            opt.height = Math.max(100, opt.height);
          } else if (opt.data.length * opt.series.length < 6) {
            opt.height *= 5 / (opt.series.length * opt.data.length);
          }

          xAxisPos = opt.height - 2 * opt.margin - 6;
          ordinalScale.rangeRoundBands([0, xAxisPos - 5]);
          scaleValue.range([yAxisPos, yAxisPos + xAxisWidth - 4 * opt.margin]);

          drawHorizontalValueAxis(svg);
          drawVerticalOrdinalAxis(yAxisPos, svg, false);
          svg.selectAll('.tick line')          
          .style('stroke-width', '0px');
        }
        svg.selectAll('.tick line')
          .style('stroke', '#dcdfe2');
        svg.selectAll('.axis path')
          .style('stroke', 'none')
          .style('fill', 'none');

        var dataSeries = ['piechart', 'gaugechart'].indexOf(opt.type) >= 0 ? opt.data : opt.series;
        if (opt.type === 'columnmekkochart') {
          dataSeries = dataSeries.filter(function(s) { return s !== opt.widthmeasure; });
        }
        var maxLegendLabelLength = calculateMaxLabelSize(dataSeries) + 40;
        var labelColumns = Math.max(1, Math.floor(opt.width / (maxLegendLabelLength)));
        var legendOffset = opt.height;

        opt.chartHeight = opt.height;
        opt.height = opt.height + 40 + drawLegend(svg, dataSeries, labelColumns, legendOffset, maxLegendLabelLength);

        // Assign viewbox here as bar chart modified opt.height value
        svg
          .attr('viewBox', '0 0 ' + opt.width + ' ' + opt.height);
        draw[opt.type](svg);

        // Assing aspect ratio as padding-bottom to allow chart to expand vertically
        svgContainer
          .attr('style', 'padding-bottom: ' + 100 * (opt.height / opt.width) + '%');

        svg.selectAll('text')
          .style('font-family', 'Source Sans Pro')
          .style('font-size', '10pt')
          .style('color', '#606060');

        // Genereate data urls for each chart so that user can download
        // them as png files
        thl.pivot.exportImg(opt);
      }
    };
  };


  /**
   * Displays a modal dialog where the user can select which dimension nodes
   * they'd like to display in a column or a row. Selectable nodes are
   * added to a select list in the dimensions order and indented accordingly.
   * The current selection is added to the selected list. User is provided
   * with a simple infix search and buttons for select all and remove all
   * events
   **/
  function populateModal (e) {

    var group = $(this).closest('.form-group');

    $('#subset-selector').modal('show');
    $('#subset-selector h4 span').text(group.find('label').text());

    var selectable = $('#subset-selector .selectable .options');
    var selected = $('#subset-selector .selected .options');

    /* Clear previous dialog values */
    selectable.find('div').remove();
    selected.find('div').remove();

    var sort = 0; // Determines the current sort order in dimension
    var nodeOptions = {}; // hash index for options for quicker handling

    group.find('select option').each(function () {
      var option = $('<div></div>')
        .text(this.innerText)
        .attr('data-val', this.value)
        .attr('data-sort', ++sort)
        .addClass('l' + $(this).attr('data-level'));
      selectable.append(option);
      if (this.selected) {
        selected.append(option.clone());
      }
      nodeOptions[this.value] = option;
    });

    /* wire remove all button */
    selected.closest('.form-group').siblings('.btn').click(function () {
      selected.find('div').remove();
    });

    /* wire select all button */
    selectable
      .closest('.form-group')
      .siblings('.btn')
      .click(function () {
        /* clear selected list so that we don't have to check for duplicates */
        selected.find('div').remove();
        /* clone all selectable nodes and add them to the selected list */
        selectable
          .find('div:not(.disabled)')
          .clone()
          .click(function () { $(this).remove(); })
          .appendTo(selected);
      });

    /* wire removal of single selected node to click event */
    selected.find('div').click(function () { $(this).remove(); });

    /* Add option to selected list on click */
    selectable.find('div').click(
      function () {
        /* Only add option once */

        var option = $(this).clone();
        if (selected.find('div[data-val=' + option.attr('data-val') + ']').length > 0) {
          return;
        }

        var sort = parseInt(option.attr('data-sort'), 10);
        var isAdded = false;

        /* Remove option onclick */
        option.click(function () { $(this).remove(); });

        /* add option to selected list in it's place */
        selected.find('div').each(function () {
          if (parseInt($(this).attr('data-sort'), 10) > sort) {
            $(this).before(option);
            isAdded = true;
            return false;
          }
          return true;
        })
        .removeClass('just-added');
        if (!isAdded) {
          selected.append(option);
        }
        option.addClass('just-added');
        selected.scrollTop(selected.scrollTop() - selected.offset().top + option.offset().top - selected.height() / 2);
      });

    /* create row or column subset http parameter value and reload cube */
    $('#subset-selector .save').click(function () {
      var opt = group.find('select option');
      opt.prop('selected', false);
      selected.find('div').each(function () {
        var val = $(this).attr('data-val');
        opt.each(function () {
          if(this.value === val) {
            this.selected = true;
          }
        });
      });
      group.closest('form').submit();
      // We have to add the extra separator in case only one item has been selected
      // changeView(input, dim.id + thl.separator + values.join(thl.subsetSeparator) + thl.subsetSeparator);
    });
  }

  $(document).ready(function () {
    $('.filter-toggle .btn').click(function () {
      $('.col-sm-3').toggleClass('active');
    });
    $('select[multiple]').each(function () {
      var btn = $('<a>')
        .addClass('btn btn-default')
        .text(thl.messages['select'])
        .click(populateModal);

      $(this)
        .after(btn)
        .after('<br />')
        .hide();
    });

    $('#subset-selector .selectable, #subset-selector .selected').each(function () {
      var self = $(this);
      var filter = self.find('input');
      var previousValue = '';
      filter.keyup(function () {
        var re = new RegExp(this.value, 'i');
        var options = self.find('.options').children();
        if (this.value.length < previousValue.length) {
          options.show();
          options.toggleClass('disabled', false);
        }
        previousValue = this.value;
        options
          .filter(function () {
            return !re.test($(this).text());
          })
          .toggleClass('disabled', true)
          .hide();
      });
    });

    if(typeof(labels) === 'undefined') { labels = []; }
    if(typeof(dimensionData) === 'undefined') { dimensionData = {}; }
    var summary = thl.pivot.summary(labels, dimensionData);
    $('.presentation.map, .presentation.list, .presentation.bar, .presentation.line, .presentation.column, .presentation.column-mekko, .presentation.pie, .presentation.gauge, .presentation.table, .presentation.radar')
      .each(function () {
        var p = this;
        var callback = function (data) {
          if (data.dataset.value.length === 0) {
            $(p).children('img').remove();
            return;
          }
          var dataset = JSONstat(data).Dataset(0),
            target = $(p),
            type = selectChartType(target);

         if ('map' === type) {
            summary
            .drawMap({
              target: target,
              dataset: dataset,
              stage: target.attr('data-stage'),
              palette: target.attr('data-palette'),
              limits: target.attr('data-limits'),
              order: target.attr('data-limit-order'),
              include: target.attr('data-limit-include') || 'gte',
              decimals: target.attr('data-decimals'),
              label: target.attr('data-label'),
              limitLabels: function() {
                  if(target.attr('data-limits') === undefined) {
                      return [];
                  }
                  var l = target.attr('data-limits').split(',').length;
                  var lbl = [];
                  for(var i = 0; i < l - 1; ++i) {
                      var value = target.attr('data-limit-' + i);
                      if(value !== undefined)
                          lbl.push(value);
                      else
                          break;
                  }
                  return lbl;
                },
            });
          } else if ('table' === type) {
            summary
              .drawTable({
                target: $(p),
                dataset: dataset,
                rowCount: parseInt(target.attr('data-row-count')),
                columnCount: parseInt(target.attr('data-column-count')),
                rowDimHighlights: target.attr('data-row-dim-highlights').trim().split(' '),
                colDimHighlights: target.attr('data-column-dim-highlights').trim().split(' '),
                align: target.attr('data-align').split(' '),
                suppress: target.attr('data-suppress'),
                highlight: target.attr('data-highlight'),
                totalHighlightRows: target.attr('data-row-highlight-nodes').trim().split(' '),
                totalHighlightColumns: target.attr('data-column-highlight-nodes').trim().split(' ')
              });
          } else if ('list' === type) {
            summary
                .drawList({
                   target: $(p),
                   dataset: dataset
                });
          } else {
            summary.presentation({
              domTarget: p,
              target: d3.select(p),
              dataset: dataset,
              type: selectChartType($(p)),
              series: dataset.Dimension(1).id,
              data: dataset.Dimension(0).id,
              callback: function (series, i, measure) {
                var val;
                if (typeof measure === 'undefined') {
                  measure = 0;
                }
                if (dataset.Dimension().length === 2) {
                  // Only one series dimension and measure
                  val = dataset.Data([i, series]);
                } else {
                  // Multiple series and measure
                  var key = [];
                  key[0] = i;
                  key[1] = series;
                  for(var i = 2; i < dataset.Dimension().length - 1; ++i) {
                    key[i] = 0;
                  }
                  key[dataset.Dimension().length - 1] = measure;
                  val = dataset.Data(key);
                }
                val = val && val.value !== null ? +(val.value.replace(',', '.')) : null;
                val = isNaN(val) ? null : val;
                return val;
              },
              width: 800,
              height: 400,
              margin: 10,
              stacked: $(p).attr('data-stacked') === 'true',
              percent: $(p).attr('data-percent') === 'true',
              range: [$(p).attr('data-min'), $(p).attr('data-max')],
              palette: $(p).attr('data-palette'),              
              showCi: $(p).attr('data-ci') === 'true',
              showN: $(p).attr('data-n') === 'true',
              em: $(p).attr('data-em') ? $(p).attr('data-em').split(',') : undefined,
              limits: target.attr('data-limits'),
              limitLabels: function() {
                if(target.attr('data-limits') === undefined) {
                    return [];
                }
                var l = target.attr('data-limits').split(',').length;
                var lbl = [];
                for(var i = 0; i < l - 1 ; ++i) {
                    var value = target.attr('data-limit-' + i);
                    if(value !== undefined)
                        lbl.push(value);
                    else
                        break;
                }
                return lbl;
              },
              limitAreas: function() {
                if(target.attr('data-limits') === undefined) {
                    return [];
                }
                var l = target.attr('data-limits').split(',').length;
                var area = [];
                for(var i = 0; i < l - 1; ++i) {
                    var value = target.attr('data-limitarea-' + i);
                    if(value !== undefined)
                        area.push(+value);
                    else
                        break;
                }
                return area;
              },
              order: target.attr('data-limit-order'),
              include: target.attr('data-limit-include'),
              legendless: target.attr('data-legendless'),
              widthmeasure: target.attr('data-width-measure')
            });
          }
          $(p).children('img').remove();
        };
        var url = $(p).attr('data-ref');
        if(url.length > 2000) {
          $.post(
            url.substring(0, url.indexOf('json') + 4), 
            url.substring(url.indexOf('json') + 5),
            callback, 'json');
        } else {
          // prefer GET for caching
          $.getJSON(url, callback, 'json');
        }
      }); // end of each presentation.bar

    $('#summary-form select, #summary-form input').change(
      function () {
        if (!$(this).is('.search-control')) {
          $(this).closest('form').submit();
        }
      }
    );

    $('.search-control').each(function () {
      var search = $(this);
      var menu = search.closest('.dropdown').find('.dropdown-menu');
      menu.css('width', '100%');
      var select = search.closest('.form-group').find('select');
      var options = select.children();

      if (!select.prop('multiple')) {
        options.each(function () {
          var li = $('<li>');
          var a = $('<a>')
          .prop('href', '#')
          .prop('data-val', this.value)
          .text($(this).text().trim());
          a.click(function (e) {
            e.preventDefault();
            search
              .closest('.form-group')
              .find('select')
              .val(a.prop('data-val'))
              .change();
            search.closest('.dropdown').toggleClass('open', false);
          });
          li.append(a);
          menu.append(li);
        });
        search.closest('.form-group').find('select').hide();
      }
      menu.find('small a').click(function (e) {
        search.closest('.dropdown').toggleClass('open', false);
      });
      search.keyup(function (e) {
        if (!select.prop('multiple')) {
          search.closest('.dropdown').toggleClass('open', true);
          switch(e.which) {
          case 40: // down
              break;
          case 13: // select first on enter
              var a = menu.find('li:visible a');
              if(a.length > 0) {
                $(a.get(0)).click();
              }
              search.closest('.dropdown').toggleClass('open', false);
              break;
          case 27: // hide on esc
              search.closest('.dropdown').toggleClass('open', false);
              break;
          default:
        	  var searchValue = new RegExp('' + this.value.toLowerCase().trim());           
            menu.find('li').each(function (i) {
              if(i == 0) {
                return;
              }
              var self = $(this);
              if (searchValue.test(self.text().toLowerCase())) {
                self.show();               
              } else {
                self.hide();
              }
            });
          }
        } else {
          var searchValue = new RegExp('' + this.value.toLowerCase().trim());
          options.each(function () {
            var self = $(this);
            if (searchValue.test(self.text().trim().toLowerCase())) {
              self.show();
              self.prop('disabled', false);
            } else {
              self.hide();
              self.prop('disabled', true);
            }
          });
        }
      });
    });

    $('.breadcrumb a').click(function (e) {
      e.preventDefault();
      summary.submitDrillDown($(this).attr('data-node'), $(this).attr('data-dim'));
    });
  });
})(window.jQuery, window.d3);
