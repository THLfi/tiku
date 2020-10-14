[#ftl][#setting locale="fi"]
[#macro label e][#if e?? && e.label??]${(e.label.getValue(lang)!"n/a")?json_string}[#else]???[/#if][/#macro]
{
[#if nodes??]
[#list nodes as node]
        "id": "${node.id?json_string}",
        "sid": [#if node.surrogateId??]${node.surrogateId?json_string}[#else]-1[/#if],
        "label": "[@label node /]",
        "uri": "${node.reference?json_string}"
        [#if node.dimension??],"dimension": "[@label node.dimension /]"[/#if]
        [#if node.level??],"stage": "[@label node.level /]"[/#if]
        [#if node.code??],"code":"${node.code?json_string}"[/#if]
        [#if node.sort??],"sort":${node.sort}[/#if]
        [#if node.decimals??],"decimals":${node.decimals}[/#if]
        [#if node.properties!?size > 0]
        ,"properties": {
            [#list node.properties! as p]
            [#if p_index > 0],[/#if]"${p.key?json_string}": "${p.value?json_string}"
            [/#list]
        }[/#if]
        [#if node.parent??]
          ,"parent": "https://sampo.thl.fi${rc.contextPath}/${env}/${lang}/${subject}/${hydra}/${cube}/${node.parent.surrogateId}"
        [/#if]
        [#if node.children?size > 0]
          ,"children": [
            [#list node.children as child]
            [#if child_index > 0],[/#if]"https://sampo.thl.fi${rc.contextPath}/${env}/${lang}/${subject}/${hydra}/${cube}/${child.surrogateId}"
            [/#list]
          ]
        [/#if]
[/#list]
[/#if]
}
