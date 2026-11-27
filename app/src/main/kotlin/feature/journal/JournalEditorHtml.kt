// توقيع المطوّر: يوسف الحمزي — Y- Dimensions — yswefalhmzy@gmail.com
package feature.journal

import org.json.JSONObject
import java.util.Locale

object JournalEditorHtml {
    fun cssColor(value: Long): String =
        String.format(Locale.US, "#%06X", value.toInt() and 0xFFFFFF)

    fun build(color: Long, initial: String): String {
        val safe = JSONObject.quote(initial.replace("</", "<\\/"))
        return TEMPLATE
            .replace("__COLOR__", cssColor(color))
            .replace("__INITIAL__", safe)
    }

    private val TEMPLATE = """
<!doctype html>
<html lang="ar" dir="rtl">
<head>
<meta charset="utf-8">
<meta name="viewport" content="width=device-width,initial-scale=1">
<style>
html,body{margin:0;min-height:100%;background:__COLOR__;font-family:Cairo,system-ui,sans-serif}
body{padding:14px;box-sizing:border-box}
#paper{min-height:380px;padding:18px;border-radius:18px;outline:none;background:__COLOR__;color:#20202a;font-size:18px;line-height:1.9;overflow-wrap:anywhere}
#paper img{max-width:100%;border-radius:14px}
#paper .sticker{display:inline-block;font-size:30px;padding:4px 8px;border-radius:14px;background:rgba(255,255,255,.72);margin:2px}
#paper ol[type="a"]{list-style-type:lower-alpha}
#ink{position:absolute;inset:14px;pointer-events:none;border-radius:18px}
</style>
</head>
<body>
<div id="paper" contenteditable="true" spellcheck="false"></div>
<canvas id="ink"></canvas>
<script>
var paper=document.getElementById('paper');
var ink=document.getElementById('ink');
var ctx=ink.getContext('2d');
var drawing=false;
var last=null;
function resize(){ink.width=paper.clientWidth;ink.height=paper.clientHeight;ink.style.width=paper.clientWidth+'px';ink.style.height=paper.clientHeight+'px';}
function point(e){var r=ink.getBoundingClientRect();return {x:e.clientX-r.left,y:e.clientY-r.top};}
function start(e){if(!drawing)return;last=point(e);}
function move(e){if(!drawing||!last)return;var p=point(e);ctx.strokeStyle='#6C4CF1';ctx.lineWidth=4;ctx.lineCap='round';ctx.beginPath();ctx.moveTo(last.x,last.y);ctx.lineTo(p.x,p.y);ctx.stroke();last=p;}
function finish(){if(!drawing)return;var data=ink.toDataURL('image/png');ctx.clearRect(0,0,ink.width,ink.height);ink.style.pointerEvents='none';drawing=false;last=null;window.insertHtml('<img class="drawing" src="'+data+'">');}
ink.addEventListener('pointerdown',start);
ink.addEventListener('pointermove',move);
ink.addEventListener('pointerup',finish);
ink.addEventListener('pointerleave',finish);
window.addEventListener('resize',resize);
window.setContent=function(html){paper.innerHTML=html||'';resize();};
window.getContent=function(){return paper.innerHTML;};
window.command=function(cmd,value){paper.focus();document.execCommand(cmd,false,value||null);};
window.setBackground=function(color){document.body.style.background=color;paper.style.background=color;};
window.setFont=function(name){paper.style.fontFamily=name;};
window.insertHtml=function(html){paper.focus();document.execCommand('insertHTML',false,html);};
window.insertImage=function(src){window.insertHtml('<img src="'+src+'">');};
window.insertSticker=function(text){window.insertHtml('<span class="sticker">'+text+'</span>');};
window.alphaList=function(){paper.focus();document.execCommand('insertOrderedList',false,null);var n=window.getSelection().anchorNode;while(n&&n.nodeName!=='OL')n=n.parentNode;if(n)n.setAttribute('type','a');};
window.nestedList=function(){paper.focus();document.execCommand('insertUnorderedList',false,null);document.execCommand('indent',false,null);};
window.setDrawMode=function(on){resize();drawing=!!on;ink.style.pointerEvents=on?'auto':'none';if(on){ctx.clearRect(0,0,ink.width,ink.height);paper.focus();}else finish();};
window.setContent(__INITIAL__);
resize();
</script>
</body>
</html>
"""
}
// توقيع المطوّر: يوسف الحمزي — Y- Dimensions — yswefalhmzy@gmail.com
