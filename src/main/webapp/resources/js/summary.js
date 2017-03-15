var thl = thl || {};

/**
 Handle each bar chart presentation
 */
function selectChartType (e) {
  if (e.is('.bar')) {
    return 'barchart';
  }
  if (e.is('.column')) {
    return 'columnchart';
  }
  if (e.is('.line')) {
    return 'linechart';
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
}

(function ($, d3) {
  thl.pivot = thl.pivot || {};
  thl.pivot.svgToImg = function (doc, width, height, callback) {
    var svgHeight = +d3.select(doc).select('svg').attr('viewBox').split(' ')[3];
    var data = doc.innerHTML.replace('<svg ', '<svg width="' + width + '" height="' + svgHeight + '" ');
    var blob = new Blob([data], {type: 'image/svg+xml;charset=UTF-8'});
    var img = new Image();
    var DOMURL = window.URL || window.webkitURL || window;
    var url = DOMURL.createObjectURL(blob);
    $(img)
      .on('load', function () {
        try {
          var canvas = $('<canvas>').attr('width', width + 20).attr('height', svgHeight + 20).get(0);
          var ctx = canvas.getContext('2d');
          ctx.fillStyle = '#ffffff';
          ctx.fillRect(0, 0, 820, svgHeight + 20);
          ctx.drawImage(img, 10, 10);
          callback(canvas);
          DOMURL.revokeObjectURL(url);
        } catch (e) {
          $(img).remove();
        }
      });
    img.src = url;
  };
  thl.pivot.summary = function (labels, dimensionData) {
    /*
     * Color palette for charts
     */
    var colors = [
        '#2f62ad',
        '#7cd0d8',
        '#571259',
        '#5faf2c',
        '#bf4073',
        '#3b007f',
        '#16994a',
        '#cccc72',
        '#0e1e47',
        '#25a5a2',
        '#cc7acc',
        '#244911',
        '#9f7fcc',
        '#0660a2',
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
        '#ef4848',
        '#efd748',
        '#66cc66'
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
      drawTable: function (opt) {
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

        /**
         * Creates elements for the thead-element containing th elements for each
         * each column header.
         */
        function createTableHead () {
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
        function createRowHeaderCells (ri, row, rowVals, spanPerLevel) {
          for (var level = 0; level < opt.rowCount; ++level) {
          //  var rowspan = spanPerLevel[level];
          //  if (ri % rowspan === 0) {
              var nodeId = rowVals[level],
                th = $('<th>'),
                content = labels[nodeId];
            //  th.attr('rowspan', rowspan);
              if (canDrill(nodeId)) {
                var link = $('<a>')
                  .attr('href', '#')
                  .text(content)
                  .click(drillDown(nodeId, dimensionData[nodeId].dim));
                th.append(link);
              } else {
                th.text(content);
              }
              row.append(th);
        //    }
          }
        }

        /**
         * Creates a td element for each value cell in the given row
         * row -> current row element
         * rowIndices -> array of value indices defined by the current row, used to query data from a jsonstat object
         */
        function createRowValueCells (offset, row, rowIndices) {
          var i = 0;
          var hasValue = false;
          forEachDimension(opt.rowCount, opt.dataset.Dimension().length, [], [], function (colIndices, colVals) {
            var key = $.merge($.merge([], rowIndices), colIndices);
            var val = opt.dataset.Data(key);
            if (val == null || val.value === null) {
              row.append(
                $('<td>')
                .append('<span>..</span>')
                .css('text-align', opt.align[0])
              );
            } else {
              hasValue = true;
              row.append(
                $('<td>')
                .append(
                  $('<span></span>')
                  .text(
                    ('' + val.value)
                      .replace(/(\d)(?=(\d{3})+(\.|$))/g, '$1\xa0') // Use non-breaking space as a thousands separator
                      .replace(/\./g, ','))
                )
                .css('text-align', opt.align[0])
              ); // Use comma as a decimal separator
            }
            i += 1;
          });
          return hasValue;
        }

        var tableContainer = $('<div></div>').addClass('table-responsive');
        var table =
          $('<table>')
            .addClass('table table-striped table-condensed')
            .append(createTableHead()),
          body = $('<tbody>');

        var dim = opt.dataset.Dimension();
        var cols = 1;
        var ri = 0;
        for (var i = opt.rowCount; i < dim.length; ++i) {
          cols *= dim[i].length;
        }
        var rowspanPerLevel = calculateSpanPerLevel(0, opt.rowCount);
        forEachDimension(0, opt.rowCount, [], [], function (rowIndices, rowVals) {
          var row = $('<tr>');
          createRowHeaderCells(ri, row, rowVals, rowspanPerLevel);
          hasValue = createRowValueCells(ri * cols, row, rowIndices);
          ri += 1;
          if (hasValue) {
            body.append(row);
          }
        });

        table.append(body);
        tableContainer.append(table);
        $(opt.target[0]).append(tableContainer);
        var maxWidth = {};
        table
          .find('span')
          .each(function () {
            var self = $(this);
            var w = self.width();
            var i = self.closest('td').index();
            maxWidth[i] = maxWidth[i] === undefined || w > maxWidth[i] ? w : maxWidth[i];
          })
          .css('width', function () {
            return maxWidth[$(this).closest('td').index()] + 'px';
          });
      },

      presentation: function (opt) {
        var domainRange,
          sums = [],
          percent = false,
          stacked = false,
          ordinalScale,
          barHeight = 10,
          barAndColumnMargin = 2,
          barGroupHeight,
          scaleValue,
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
          BAR_GROUP_MARGIN = 15,
          BAR_MARGIN = 5;



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
          if (opt.percent) {
            domainRange = [0, 100];
          } else {
            domainRange = [min, max];
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
            // if chart is stacked we have to use the actual value instead of
            // absolute value.
            return opt.stacked ? val : Math.abs(val);
          };
        }

        function sizeConfidence (series) {
          return function (d, i) {
            var upper = scaleValue(0) - scaleValue(opt.callback(series, i, 2));
            var lower = scaleValue(0) - scaleValue(opt.callback(series, i, 1));
            return Math.abs(upper - lower);
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
          if (opt.showCi) {
            var sampleSize = opt.callback(series, i, 3);
            if (sampleSize) {
              label += ' (n = ' + sampleSize + ')';
            }
          }
          return label;
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
                .append('svg:title')
                .text(function (d, i) {
                  return opt.callback(series, i, 0) + ' ' + label(d, i, series);
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
                    return ordinalScale(d) + BAR_GROUP_MARGIN / 1.5 + series * (chartColumnWidth + BAR_MARGIN) + chartColumnWidth / 2 - 1;
                  }, 'column');
              }
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
                    return opt.em.indexOf(d) >= 0 ? colors[series] : '#808080';
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
                .append('svg:title')
                .text(function (d, i) {
                    return opt.callback(series, i, 0) + ' ' + label(d, i, series);
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
                    return ordinalScale(d) + chartBarHeight + BAR_MARGIN / 2;
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
                  .defined(function(d, i) { return null != opt.callback(series, i); })
                  .x(function (d, i) {
                    return ordinalScale(d) + spacing / 2;
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
                .attr('fill', colors[series])
                .attr('stroke', '#fff')
                .attr('r', function(d, i) {
                  return opt.callback(series, i) == null ? 0 : 4;
                })
                .attr('stroke-width', function(d, i) {
                  return opt.callback(series, i) == null ? 0 : 4;
                })
                .attr('cx', function (d, i) {
                  return ordinalScale(d) + spacing / 2;
                })
                .attr('cy', offsetColumn(series))
                .append('svg:title')
                .text(function (d, i) {
                  return opt.callback(series, i);
                });

              // plot ci
              if (opt.showCi) {
                var area =
                  d3.svg.area()
                    .x(function (d, i) {
                      return ordinalScale(datum[i]) + spacing / 2;
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
              });
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
            g.append('text')
              .attr('transform', function (d) {
                var pos = outerArc.centroid(d);
                pos[0] = innerArcRadius * (midAngle(d) < Math.PI ? 1.2 : -1.2);
                return 'translate(' + pos + ')';
              })
              .attr('dy', '.35em')
              .style('text-anchor', function (d) {
                return midAngle(d) < Math.PI ? 'start' : 'end';
              })
              .text(function (d, i) {
                return opt.callback(0, dataIndex[i]);
              });
            g.append('polyline')
              .attr('points', function (d) {
                if (d.value !== 0) {
                  var pos = outerArc.centroid(d);
                  pos[0] = innerArcRadius * 0.95 * (midAngle(d) < Math.PI ? 1.2 : -1.2);
                  return [arc.centroid(d), outerArc.centroid(d), pos];
                }
              })
              .attr('fill', 'none')
              .attr('stroke', '#808080');
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

            // cScale is for drawing gauge background (uses radians)
            var cScale = d3.scale.linear()
              .range([
                60 * (Math.PI / 180), 300 * (Math.PI / 180)
              ])
              .domain([
                0, 100
              ]);
            // needleAngleScale is for drawing needles at right angle (uses degrees)
            var needleAngleScale = d3.scale.linear()
              .range([-120, 120])
              .domain(
                scaleValue.domain()
            );

            var scaleData = [
              [
                0, 25, 'low'
              ],
              [
                25, 75, 'average'
              ],
              [
                75, 100, 'high'
              ]
            ];
            var palette = [];
            switch (opt.palette) {
              case 'greenyellowred':
                palette = ['#66cc66', '#efd748', '#ef4848'];
                break;
              case 'gray':
                palette = ['#8c8c8c', '#b2b2b2', '#8c8c8c'];
                break;
              default:
                palette = ['#ef4848', '#efd748', '#66cc66'];
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
              .attr('fill', function (d, i) {
                return palette[i];
              })
              .attr('d', scale)
              .attr('transform', 'translate(200,200) rotate(180)');

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

            appendLine(chart, 62, 71, 279, 274, '#ddd');
            appendLine(chart, 63, 72, 119, 124, '#ddd');
            appendLine(chart, 327, 338, 125, 119, '#ddd');
            appendLine(chart, 329, 338, 275, 280, '#ddd');

            appendText(chart, 74, 277, textStyle, domainRange[0]);
            appendText(chart, 74, 139, textStyle, Math.round((domainRange[1] - domainRange[0]) * 0.25));
            appendText(chart, 310, 130, textStyle, Math.round((domainRange[1] - domainRange[0]) * 0.75));
            appendText(chart, 307, 277, textStyle, domainRange[1]);

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
                return 'translate(' + scaleRadiusOuter + ', ' + 45 + ') rotate(' + needleAngleScale(v) + ', 13, 157)';
              })
              .style('cursor', function (d) {
                if (canDrill(d)) {
                  return 'pointer';
                }
              })
              .on('click', function (d, i) {
                var nodeId = opt.dataset.Dimension(0).id[i];
                submitDrillDown(nodeId, dimensionData[nodeId].dim);
              });

            needle.append('svg:title')
              .text(function (d, i) {
                return labels[opt.dataset.Dimension(0).id[i]];
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
                .attr('title', text)
                .text(text);
            }

            function appendLegendElements (chart, baseX, baseY, rectStyleAttributes, textStyleAttributes, text) {
              chart.append('rect')
                .attr('x', baseX)
                .attr('y', baseY)
                .attr(rectStyleAttributes);

              chart.append('text')
                .attr('x', baseX + 20)
                .attr('y', baseY + 12)
                .attr(textStyleAttributes)
                .attr('title', text)
                .text(text);
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
        function drawHorizontalOrdinalAxis (svg, showInnerTick) {
          var xAxis = d3.svg.axis()
            .scale(ordinalScale)
            .orient('bottom')
            .tickFormat(function (d) {
              return pxWidth(labels[d].length) <= MAX_LABEL_LENGTH ? labels[d] : labels[d].substring(0, MAX_LABEL_LENGTH / CHARACTER_WIDTH - 3) + '...';
            });
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
              return pxWidth(labels[d].length) <= MAX_LABEL_LENGTH ? labels[d] : labels[d].substring(0, MAX_LABEL_LENGTH / CHARACTER_WIDTH - 3) + '...';
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
              sg.append('circle')
                .attr('fill', colors[i])
                .attr('r', 6)
                .attr('cx', 0)
                .attr('cy', -5);
              sg.append('text')
                .text(label)
                .attr('x', 10);
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
            maxLength = Math.max(labels[v].length, maxLength);
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
            range[0].toString().replace(/(\d)(?=(\d{3})+(\.|$))/g, '$1 ').length,
            range[1].toString().replace(/(\d)(?=(\d{3})+(\.|$))/g, '$1 ').length
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

        isXAxisTicksTilted = (
          // x axis should be tilted if the longest tick label
          // may overlap another tick label.
          ['barchart'].indexOf(opt.type) >= 0 ? maxValueLength > xAxisWidth / scaleValue.ticks().length : maxLabelLength > xAxisWidth / opt.data.length
        );

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

        if (['columnchart', 'linechart'].indexOf(opt.type) >= 0) {
          scaleValue.range([xAxisPos, opt.margin]);
          ordinalScale.rangeRoundBands([yAxisPos, xAxisWidth]);
          drawVerticalValueAxis(yAxisPos, svg);
          drawHorizontalOrdinalAxis(svg, opt.type === 'linechart');
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
        }
        svg.selectAll('.tick line')
          .style('stroke', '#efefef')
          .style('stroke-width', '1px');
        svg.selectAll('.axis path')
          .style('stroke', 'none')
          .style('fill', 'none');

        var dataSeries = ['piechart', 'gaugechart'].indexOf(opt.type) >= 0 ? opt.data : opt.series;
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
          .style('font-family', 'arial')
          .style('color', '#808080');

        // Genereate data urls for each chart so that user can download
        // them as png files
        $(opt.target[0]).find('.img-action a').each(function (e) {
          var link = $(this);
          if (link.attr('href') === '#') {
            thl.pivot.svgToImg(svgContainer[0][0], 800, 400, function (canvas) {
              try {
                link.attr('href', canvas.toDataURL());
                link.attr('download', opt.target[0][0].id + '.png');
                link.click();
              } catch (e) {
                link.remove();
              }
            });
          }
        });
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
        .attr('data-sort', ++sort);
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

    var summary = thl.pivot.summary(labels, dimensionData);
    $('.presentation.bar, .presentation.line, .presentation.column, .presentation.pie, .presentation.gauge, .presentation.table')
      .each(function () {
        var p = this;
        $.getJSON($(p).attr('data-ref'), function (data) {
          if (data.dataset.value.length === 0) {
            $(p).children('img').remove();
            return;
          }
          var dataset = JSONstat(data).Dataset(0),
            target = $(p),
            type = selectChartType(target);

          if ('table' === type) {
            summary
              .drawTable({
                target: $(p),
                dataset: dataset,
                rowCount: parseInt(target.attr('data-row-count')),
                columnCount: parseInt(target.attr('data-column-count')),
                align: target.attr('data-align').split(' ')
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
                  val = dataset.Data([i, series, measure]);
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
              em: $(p).attr('data-em') ? $(p).attr('data-em').split(',') : undefined
            });
          }
          $(p).children('img').remove();
        }); // end of getJson
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
            var searchValue = new RegExp('^' + this.value.toLowerCase().trim());
            menu.find('li').each(function () {
              var self = $(this);
              if (searchValue.test(self.text().toLowerCase())) {
                self.show();
              } else {
                self.hide();
              }
            });
          }
        } else {
          var searchValue = new RegExp('^' + this.value.toLowerCase().trim());
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
