[#ftl]
var thl = {
    messages: {
      'cube.dimension.sort.asc': '${message("cube.dimension.sort.asc")?js_string}',
      'cube.dimension.sort.desc': '${message("cube.dimension.sort.desc")?js_string}',
      'cube.dimension.hide': '${message("cube.dimension.hide")?js_string}'
    },
    pivot : {
        loadDimensions: function (dim) {
            thl.pivot.dim = dim;
        }
    },
    separator : '-',
    subsetSeparator: '.',
    toggleField : function(f) {
        if(f.is(':disabled')) {
            f.prop('disabled', false);
        } else {
            f.prop('disabled', true);
        }
    },
    rows : [
    [#list pivot.rows as level][#compress]
    [#if level.dimension??]
    [#if level_index > 0],[/#if] {
        dimension: "${level.dimension.id}",
        nodes: [[#list level.nodes as node] [#if node_index > 0],[/#if] "${node.surrogateId}"[/#list]]
    }
    [/#if]
    [/#compress][/#list]
    ],
    columns : [
    [#list pivot.columns as level][#compress]
    [#if level_index > 0],[/#if] {
        dimension: "${level.dimension.id}",
        nodes: [[#list level.nodes as node][#if node_index > 0],[/#if] "${node.surrogateId}"[/#list]]
    }
    [/#compress][/#list]
    ]
};
