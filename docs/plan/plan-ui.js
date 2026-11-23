/* القائمة الجانبية على الجوال + وسم خلايا الجداول بعناوين أعمدتها */
(function(){
  var side=document.getElementById('side'), scrim=document.getElementById('scrim'), btn=document.getElementById('menuBtn');
  function set(open){
    if(!side||!btn) return;
    side.classList.toggle('open',open); scrim.classList.toggle('on',open);
    btn.setAttribute('aria-expanded',String(open));
    document.body.style.overflow=open?'hidden':'';
  }
  if(btn){
    btn.addEventListener('click',function(){set(!side.classList.contains('open'))});
    scrim.addEventListener('click',function(){set(false)});
    document.addEventListener('keydown',function(e){if(e.key==='Escape') set(false)});
    side.addEventListener('click',function(e){if(e.target.closest('a')) set(false)});
  }
  var cur=side&&side.querySelector('[aria-current="page"]');
  if(cur&&cur.scrollIntoView) try{cur.scrollIntoView({block:'center'})}catch(e){}

  function label(){
    document.querySelectorAll('.scroll table').forEach(function(t){
      var heads=[].map.call(t.querySelectorAll('thead th'),function(h){return h.textContent.trim()});
      t.querySelectorAll('tbody tr').forEach(function(tr){
        [].forEach.call(tr.children,function(td,i){if(heads[i]&&!td.hasAttribute('data-l')) td.setAttribute('data-l',heads[i])});
      });
    });
  }
  var timer=null;
  new MutationObserver(function(){clearTimeout(timer);timer=setTimeout(label,30)}).observe(document.body,{childList:true,subtree:true});
  label();
})();
