/* توقيع المطوّر: يوسف الحمزي — Y- Dimensions — yswefalhmzy@gmail.com */
(function () {
  'use strict';

  var KEY = 'yd-plan-feedback-v2';
  var OLD_KEY = 'yd-plan-feedback-v1';
  var OLD_NOTES = 'yousef-plan-notes-v1';
  var OLD_SUGGESTIONS = 'yousef-plan-suggestions-v1';
  var PLACES = ['الواجهة', 'التطبيق', 'البيانات', 'الخادم والحساب', 'أدوات الجهاز', 'أخرى'];
  var MAX_IMAGE = 2 * 1024 * 1024;
  var zip = window.YDZip;

  var list = document.getElementById('list');
  var count = document.getElementById('count');
  var status = document.getElementById('status');
  var promptStatus = document.getElementById('promptStatus');
  var dialog = document.getElementById('entryDialog');
  var entryMeta = document.getElementById('entryMeta');
  var entryKind = document.getElementById('entryKind');
  var entryName = document.getElementById('entryName');
  var entryPlace = document.getElementById('entryPlace');
  var nameGroup = document.getElementById('nameGroup');
  var placeGroup = document.getElementById('placeGroup');
  var startButton = document.getElementById('startRecording');
  var cancelDialogButton = document.getElementById('cancelDialog');
  var editorShell = document.getElementById('editorShell');
  var editor = document.getElementById('editor');
  var editorToolbar = document.getElementById('editorToolbar');
  var alphaList = document.getElementById('alphaList');
  var imageGroup = document.getElementById('imageGroup');
  var imageInput = document.getElementById('imageInput');
  var imagePreview = document.getElementById('imagePreview');
  var saveButton = document.getElementById('saveEntry');
  var cancelButton = document.getElementById('cancelEditor');
  var downloadButton = document.getElementById('downloadZip');
  var deleteAllButton = document.getElementById('deleteAll');
  var newEntryButton = document.getElementById('newEntry');
  var copyPromptButton = document.getElementById('copyPrompt');
  var zipPrompt = document.getElementById('zipPrompt');

  if (!list || !zip) return;

  function read(key) {
    try {
      var value = JSON.parse(localStorage.getItem(key));
      return Array.isArray(value) ? value : [];
    } catch (error) {
      return [];
    }
  }

  function save(next) {
    try {
      localStorage.setItem(KEY, JSON.stringify(next));
      return true;
    } catch (error) {
      setMessage('الصور كبيرة؛ احذف صورة أو قلّل حجمها.');
      return false;
    }
  }

  function escapeHtml(value) {
    return String(value == null ? '' : value)
      .replace(/&/g, '&amp;')
      .replace(/</g, '&lt;')
      .replace(/>/g, '&gt;')
      .replace(/"/g, '&quot;')
      .replace(/'/g, '&#39;');
  }

  function plainToHtml(text) {
    return '<div>' + escapeHtml(text).replace(/\n/g, '<br>') + '</div>';
  }

  function oldPlace(section) {
    if (!section) return 'أخرى';
    if (['stats', 'tools', 'account'].indexOf(section) >= 0) return 'الواجهة';
    if (section === 'device') return 'أدوات الجهاز';
    if (['server', 'updates', 'general'].indexOf(section) >= 0) return 'الخادم والحساب';
    return 'التطبيق';
  }

  function migrate() {
    var existing = read(KEY);
    if (localStorage.getItem(KEY) !== null) return existing;
    var old = read(OLD_KEY);
    var notes = read(OLD_NOTES);
    var suggestions = read(OLD_SUGGESTIONS);
    var migrated = [];
    old.forEach(function (item) {
      migrated.push({
        id: String(item.id || Date.now() + Math.random()),
        kind: item.kind,
        title: String(item.title || ''),
        place: String(item.place || 'أخرى'),
        html: item.html || plainToHtml(item.text || ''),
        images: Array.isArray(item.images) ? item.images : [],
        at: Number(item.at) || Date.now()
      });
    });
    notes.forEach(function (note) {
      migrated.push({
        id: 'note-' + (note.id || Date.now() + Math.random()),
        kind: 'note', place: oldPlace(note.section),
        html: plainToHtml(note.text || ''), images: [], at: Number(note.at) || Date.now()
      });
    });
    suggestions.forEach(function (suggestion) {
      migrated.push({
        id: 'sug-' + (suggestion.id || Date.now() + Math.random()),
        kind: 'suggestion', title: String(suggestion.title || 'بدون اسم'),
        html: plainToHtml(suggestion.text || ''), images: [], at: Number(suggestion.at) || Date.now()
      });
    });
    if (migrated.length) {
      save(migrated);
      try {
        localStorage.removeItem(OLD_KEY);
        localStorage.removeItem(OLD_NOTES);
        localStorage.removeItem(OLD_SUGGESTIONS);
      } catch (error) {}
    }
    return migrated;
  }

  var items = migrate().filter(function (item) {
    return item && (item.kind === 'suggestion' || item.kind === 'note');
  }).map(function (item) {
    if (!item.html) item.html = plainToHtml(item.text || '');
    if (!Array.isArray(item.images)) item.images = [];
    return item;
  });

  var draftImages = [];
  var pendingHtml = '';
  var pendingImages = [];
  var editingId = null;
  var draftKind = 'suggestion';
  var draftName = '';
  var draftPlace = PLACES[0];
  var reading = false;

  function date(value) {
    try { return new Date(value).toLocaleString('ar'); } catch (error) { return ''; }
  }

  function setMessage(text) {
    status.textContent = text;
  }

  function setPromptMessage(text) {
    promptStatus.textContent = text;
  }

  function kindLabel(item) {
    return item.kind === 'suggestion' ? 'اقتراح' : 'ملاحظة';
  }

  function featureName(item) {
    return item.kind === 'suggestion' ? (item.title || 'بدون اسم') : (item.place || 'أخرى');
  }

  function htmlToText(html) {
    var holder = document.createElement('div');
    holder.innerHTML = html || '';
    holder.querySelectorAll('li').forEach(function (li) {
      var parent = li.parentElement;
      var depth = 0;
      var walker = parent;
      while (walker) {
        if (walker.tagName === 'OL' || walker.tagName === 'UL') depth += 1;
        walker = walker.parentElement;
      }
      var indent = '  '.repeat(Math.max(0, depth - 1));
      var prefix = '• ';
      if (parent && parent.tagName === 'OL') {
        var index = Array.prototype.indexOf.call(parent.children, li) + 1;
        prefix = parent.classList.contains('alpha')
          ? String.fromCharCode(97 + ((index - 1) % 26)) + '. '
          : index + '. ';
      }
      li.insertAdjacentText('afterbegin', indent + prefix);
    });
    holder.querySelectorAll('br').forEach(function (br) {
      br.replaceWith(document.createTextNode('\n'));
    });
    holder.querySelectorAll('p,div,h1,h2,h3,li').forEach(function (node) {
      node.appendChild(document.createTextNode('\n'));
    });
    return holder.textContent.replace(/\n{3,}/g, '\n\n').trim();
  }

  function renameImages() {
    draftImages.forEach(function (image, index) {
      image.name = zip.ordinalName(index, zip.imageExt(image.dataUrl, image.original));
    });
  }

  function thumbHtml(image, index, scope, id) {
    var key = scope === 'draft' ? 'data-draft-img' : 'data-saved-img';
    var idPart = scope === 'draft' ? '' : ' data-item="' + escapeHtml(id) + '"';
    return '<figure class="thumb">' +
      '<img src="' + escapeHtml(image.dataUrl) + '" alt="' + escapeHtml(image.name) + '">' +
      '<figcaption>' + escapeHtml(image.name) + '</figcaption>' +
      '<button type="button" class="thumb-del" ' + key + '="' + index + '"' + idPart + '>حذف</button>' +
      '</figure>';
  }

  function renderDraftImages() {
    renameImages();
    imagePreview.innerHTML = draftImages.length
      ? draftImages.map(function (image, index) { return thumbHtml(image, index, 'draft', ''); }).join('')
      : '<span class="muted">لا صور بعد.</span>';
  }

  function render() {
    var sorted = items.slice().sort(function (a, b) { return (b.at || 0) - (a.at || 0); });
    if (!sorted.length) {
      list.innerHTML = '<div class="empty">لا توجد مدخلات بعد.</div>';
    } else {
      list.innerHTML = sorted.map(function (item) {
        var badge = kindLabel(item);
        var title = featureName(item);
        var head = item.kind === 'suggestion'
          ? '<h3>' + escapeHtml(title) + '</h3>'
          : '<h3>مكان المشكلة: ' + escapeHtml(title) + '</h3>';
        var images = item.images.length
          ? '<div class="saved-images">' + item.images.map(function (image, index) {
              return thumbHtml(image, index, 'saved', item.id);
            }).join('') + '</div>'
          : '';
        return '<article class="item">' +
          '<div class="meta"><span class="pill">' + badge + '</span><time>' + escapeHtml(date(item.at)) + '</time></div>' +
          head +
          '<pre class="code"><code>' + escapeHtml(htmlToText(item.html)) + '</code></pre>' +
          images +
          '<div class="item-actions">' +
          '<button class="del" type="button" data-edit="' + escapeHtml(item.id) + '">تعديل</button>' +
          '<button class="del" type="button" data-del="' + escapeHtml(item.id) + '">حذف</button>' +
          '</div></article>';
      }).join('');
    }
    var suggestions = items.filter(function (item) { return item.kind === 'suggestion'; }).length;
    count.textContent = '(' + items.length + ' كلها · ' + suggestions + ' اقتراح · ' + (items.length - suggestions) + ' ملاحظة)';
  }

  function sortedItems() {
    return items.slice().sort(function (a, b) { return (b.at || 0) - (a.at || 0); });
  }

  function folderName(index) {
    return 'notes/entry-' + (index + 1) + '/';
  }

  function buildRootMarkdown() {
    var lines = [
      '# كل الاقتراحات والملاحظات',
      '',
      'التاريخ: ' + new Date().toLocaleDateString('ar'),
      'التنسيق: A — مجلد لكل مدخل، وall-notes.md في الجذر.',
      ''
    ];
    sortedItems().forEach(function (item, index) {
      var folder = folderName(index);
      var images = item.images || [];
      lines.push('## ' + (index + 1) + '. [' + kindLabel(item) + '] ' + featureName(item));
      lines.push('المجلد: `' + folder + '`');
      if (images.length) {
        lines.push('الصور: ' + images.map(function (image) { return '`' + image.name + '`'; }).join('، '));
      } else {
        lines.push('');
        lines.push(htmlToText(item.html));
      }
      lines.push('');
    });
    return lines.join('\n').trim() + '\n';
  }

  function buildEntryMarkdown(item, folder) {
    var lines = [
      '# [' + kindLabel(item) + '] ' + featureName(item),
      '',
      'المجلد: `' + folder + '`',
      '',
      htmlToText(item.html)
    ];
    if (item.images && item.images.length) {
      lines.push('', '## الصور');
      item.images.forEach(function (image) { lines.push('- `' + image.name + '`'); });
    }
    return lines.join('\n').trim() + '\n';
  }

  function buildZipFiles() {
    var files = [{ name: 'all-notes.md', data: zip.textBytes(buildRootMarkdown()) }];
    sortedItems().forEach(function (item, index) {
      var folder = folderName(index);
      files.push({ name: folder + 'note.md', data: zip.textBytes(buildEntryMarkdown(item, folder)) });
      (item.images || []).forEach(function (image) {
        files.push({ name: folder + image.name, data: zip.dataUrlBytes(image.dataUrl) });
      });
    });
    return files;
  }

  function setKind() {
    var isSuggestion = entryKind.value === 'suggestion';
    nameGroup.hidden = !isSuggestion;
    placeGroup.hidden = isSuggestion;
  }

  function resetEditor() {
    editorShell.hidden = true;
    editor.innerHTML = '';
    draftImages = [];
    pendingHtml = '';
    pendingImages = [];
    editingId = null;
    renderDraftImages();
  }

  function openDialog(item) {
    editingId = item ? String(item.id) : null;
    entryKind.value = item ? item.kind : 'suggestion';
    entryName.value = item && item.kind === 'suggestion' ? (item.title || '') : '';
    entryPlace.value = item && item.kind === 'note' ? (item.place || PLACES[0]) : PLACES[0];
    pendingHtml = item ? (item.html || '') : '';
    pendingImages = item ? (item.images || []).map(function (image) {
      return { name: image.name, dataUrl: image.dataUrl, original: image.original || image.name };
    }) : [];
    draftImages = [];
    setKind();
    if (typeof dialog.showModal === 'function') dialog.showModal();
    else dialog.setAttribute('open', '');
  }

  function startRecording() {
    draftKind = entryKind.value;
    draftName = entryName.value.trim();
    draftPlace = entryPlace.value;
    if (draftKind === 'suggestion' && !draftName) {
      setMessage('اكتب الاسم.');
      return;
    }
    if (typeof dialog.close === 'function') dialog.close();
    else dialog.removeAttribute('open');
    editorShell.hidden = false;
    imageGroup.hidden = draftKind !== 'note';
    editor.innerHTML = pendingHtml;
    draftImages = pendingImages.slice();
    renderDraftImages();
    editor.focus();
    setMessage('اكتب النص ثم احفظ.');
  }

  function saveEntry() {
    if (reading) { setMessage('انتظر تحميل الصور.'); return; }
    var html = editor.innerHTML.trim();
    var text = htmlToText(html);
    if (!text) { setMessage('اكتب النص.'); return; }
    renameImages();
    var now = Date.now();
    var item = {
      id: editingId || String(now) + Math.random().toString(16).slice(2, 7),
      kind: draftKind,
      html: html,
      images: draftImages.slice(),
      at: now
    };
    if (draftKind === 'suggestion') item.title = draftName;
    else item.place = draftPlace;
    var index = editingId ? items.findIndex(function (entry) { return String(entry.id) === editingId; }) : -1;
    if (index >= 0) {
      item.at = items[index].at || now;
      items[index] = item;
    } else {
      items.push(item);
    }
    if (!save(items)) return;
    render();
    resetEditor();
    setMessage('تم الحفظ.');
  }

  function copyText(text, onDone, onFail) {
    if (navigator.clipboard && navigator.clipboard.writeText) {
      navigator.clipboard.writeText(text).then(onDone).catch(function () {
        var area = document.createElement('textarea');
        area.value = text;
        area.style.position = 'fixed';
        area.style.opacity = '0';
        document.body.appendChild(area);
        area.select();
        var ok = document.execCommand('copy');
        area.remove();
        if (ok) onDone(); else onFail();
      });
    } else {
      onFail();
    }
  }

  function readImage(file) {
    return new Promise(function (resolve, reject) {
      var reader = new FileReader();
      reader.onload = function () {
        resolve({ dataUrl: String(reader.result), original: file.name });
      };
      reader.onerror = reject;
      reader.readAsDataURL(file);
    });
  }

  function addImages(files) {
    var chosen = Array.prototype.slice.call(files || []);
    var accepted = chosen.filter(function (file) { return /^image\//.test(file.type) && file.size <= MAX_IMAGE; });
    if (!accepted.length) { setMessage('اختر صورة أصغر من 2 ميجابايت.'); return; }
    reading = true;
    setMessage('جارٍ قراءة الصور.');
    Promise.all(accepted.map(readImage)).then(function (loaded) {
      loaded.forEach(function (image) { draftImages.push(image); });
      renameImages();
      renderDraftImages();
      reading = false;
      setMessage('أُضيفت الصور.');
    }).catch(function () {
      reading = false;
      setMessage('تعذّرت قراءة الصور.');
    });
  }

  entryPlace.innerHTML = PLACES.map(function (name) {
    return '<option value="' + escapeHtml(name) + '">' + escapeHtml(name) + '</option>';
  }).join('');

  newEntryButton.addEventListener('click', function () { openDialog(null); });
  entryKind.addEventListener('change', setKind);
  entryMeta.addEventListener('submit', function (event) { event.preventDefault(); startRecording(); });
  startButton.addEventListener('click', startRecording);
  cancelDialogButton.addEventListener('click', function () {
    if (typeof dialog.close === 'function') dialog.close();
    else dialog.removeAttribute('open');
  });
  saveButton.addEventListener('click', saveEntry);
  cancelButton.addEventListener('click', function () { resetEditor(); setMessage('أُلغي.'); });

  editorToolbar.addEventListener('click', function (event) {
    var button = event.target.closest('[data-command]');
    if (!button) return;
    event.preventDefault();
    editor.focus();
    document.execCommand(button.getAttribute('data-command'), false, null);
  });

  alphaList.addEventListener('click', function () {
    editor.focus();
    document.execCommand('insertOrderedList', false, null);
    var selection = window.getSelection();
    var node = selection && selection.anchorNode;
    var element = node && (node.nodeType === 1 ? node : node.parentElement);
    var ordered = element && element.closest('ol');
    if (ordered) ordered.classList.toggle('alpha');
  });

  imageInput.addEventListener('change', function (event) {
    addImages(event.target.files);
    event.target.value = '';
  });

  imagePreview.addEventListener('click', function (event) {
    var button = event.target.closest('[data-draft-img]');
    if (!button) return;
    draftImages.splice(Number(button.getAttribute('data-draft-img')), 1);
    renderDraftImages();
  });

  downloadButton.addEventListener('click', function () {
    if (!items.length) { setMessage('لا توجد مدخلات بعد.'); return; }
    var blob;
    try {
      blob = zip.createZip(buildZipFiles());
    } catch (error) {
      setMessage('تعذّر تجهيز الملف.');
      return;
    }
    var url = URL.createObjectURL(blob);
    var link = document.createElement('a');
    link.href = url;
    link.download = 'y-dimensions-feedback.zip';
    document.body.appendChild(link);
    link.click();
    link.remove();
    window.setTimeout(function () { URL.revokeObjectURL(url); }, 1000);
    setMessage('نُزّل ملف ZIP.');
  });

  deleteAllButton.addEventListener('click', function () {
    if (!items.length) { setMessage('لا توجد مدخلات بعد.'); return; }
    if (!window.confirm('حذف كل الاقتراحات والملاحظات؟')) return;
    items = [];
    save(items);
    render();
    resetEditor();
    setMessage('حُذف الكل.');
  });

  copyPromptButton.addEventListener('click', function () {
    var text = zipPrompt.textContent.trim();
    copyText(text, function () { setPromptMessage('نُسخ البرومت.'); }, function () { setPromptMessage('حدد النص وانسخه يدوياً.'); });
  });

  list.addEventListener('click', function (event) {
    var edit = event.target.closest('[data-edit]');
    if (edit) {
      var found = items.find(function (item) { return String(item.id) === edit.getAttribute('data-edit'); });
      if (found) openDialog(found);
      return;
    }
    var remove = event.target.closest('[data-del]');
    if (remove) {
      items = items.filter(function (item) { return String(item.id) !== remove.getAttribute('data-del'); });
      save(items);
      render();
      setMessage('تم الحذف.');
      return;
    }
    var removeImage = event.target.closest('[data-saved-img]');
    if (removeImage) {
      var id = removeImage.getAttribute('data-item');
      var index = Number(removeImage.getAttribute('data-saved-img'));
      var target = items.find(function (item) { return String(item.id) === id; });
      if (!target) return;
      target.images.splice(index, 1);
      target.images.forEach(function (image, position) {
        image.name = zip.ordinalName(position, zip.imageExt(image.dataUrl, image.original || image.name));
      });
      save(items);
      render();
      setMessage('حُذفت الصورة.');
    }
  });

  setKind();
  render();
  resetEditor();
})();
/* توقيع المطوّر: يوسف الحمزي — Y- Dimensions — yswefalhmzy@gmail.com */
