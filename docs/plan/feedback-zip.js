/* توقيع المطوّر: يوسف الحمزي — Y- Dimensions — yswefalhmzy@gmail.com */
(function (global) {
  'use strict';

  var encoder = new TextEncoder();
  var crcTable = [];
  for (var n = 0; n < 256; n += 1) {
    var c = n;
    for (var k = 0; k < 8; k += 1) c = (c & 1) ? (0xEDB88320 ^ (c >>> 1)) : (c >>> 1);
    crcTable[n] = c >>> 0;
  }

  function textBytes(value) {
    return encoder.encode(String(value == null ? '' : value));
  }

  function base64Bytes(base64) {
    if (typeof atob === 'function') {
      var binary = atob(base64);
      var out = new Uint8Array(binary.length);
      for (var i = 0; i < binary.length; i += 1) out[i] = binary.charCodeAt(i);
      return out;
    }
    return new Uint8Array(Buffer.from(base64, 'base64'));
  }

  function dataUrlBytes(url) {
    var parts = String(url || '').split(',');
    return base64Bytes(parts.length > 1 ? parts[1] : '');
  }

  function imageExt(url, original) {
    var match = /^data:(image\/[a-z0-9.+-]+);base64/i.exec(String(url || ''));
    var map = {
      'image/png': 'png', 'image/jpeg': 'jpg', 'image/jpg': 'jpg',
      'image/webp': 'webp', 'image/gif': 'gif', 'image/svg+xml': 'svg'
    };
    if (match && map[match[1]]) return map[match[1]];
    var tail = String(original || '').split('.').pop().toLowerCase().replace(/[^a-z0-9]/g, '');
    return tail || 'png';
  }

  function ordinalName(index, ext) {
    var names = ['الأولى', 'الثانية', 'الثالثة', 'الرابعة', 'الخامسة', 'السادسة', 'السابعة', 'الثامنة', 'التاسعة', 'العاشرة'];
    return (names[index] || 'الصورة-' + (index + 1)) + '.' + String(ext || 'png').replace(/^\./, '');
  }

  function crc32(bytes) {
    var c = 0xFFFFFFFF;
    for (var i = 0; i < bytes.length; i += 1) c = crcTable[(c ^ bytes[i]) & 0xFF] ^ (c >>> 8);
    return (c ^ 0xFFFFFFFF) >>> 0;
  }

  function dosDateTime(value) {
    var year = Math.max(1980, value.getFullYear());
    return {
      time: (value.getHours() << 11) | (value.getMinutes() << 5) | (value.getSeconds() >> 1),
      date: ((year - 1980) << 9) | ((value.getMonth() + 1) << 5) | value.getDate()
    };
  }

  function createZip(files) {
    var parts = [];
    var central = [];
    var offset = 0;
    var stamps = dosDateTime(new Date());
    (files || []).forEach(function (file) {
      var nameBytes = textBytes(file.name);
      var data = file.data instanceof Uint8Array ? file.data : textBytes(file.data);
      var crc = crc32(data);
      var local = new Uint8Array(30 + nameBytes.length);
      var lv = new DataView(local.buffer);
      lv.setUint32(0, 0x04034b50, true);
      lv.setUint16(4, 20, true);
      lv.setUint16(6, 0x0800, true);
      lv.setUint16(8, 0, true);
      lv.setUint16(10, stamps.time, true);
      lv.setUint16(12, stamps.date, true);
      lv.setUint32(14, crc, true);
      lv.setUint32(18, data.length, true);
      lv.setUint32(22, data.length, true);
      lv.setUint16(26, nameBytes.length, true);
      lv.setUint16(28, 0, true);
      local.set(nameBytes, 30);
      var header = new Uint8Array(46 + nameBytes.length);
      var hv = new DataView(header.buffer);
      hv.setUint32(0, 0x02014b50, true);
      hv.setUint16(4, 20, true);
      hv.setUint16(6, 20, true);
      hv.setUint16(8, 0x0800, true);
      hv.setUint16(10, 0, true);
      hv.setUint16(12, stamps.time, true);
      hv.setUint16(14, stamps.date, true);
      hv.setUint32(16, crc, true);
      hv.setUint32(20, data.length, true);
      hv.setUint32(24, data.length, true);
      hv.setUint16(28, nameBytes.length, true);
      hv.setUint16(30, 0, true);
      hv.setUint16(32, 0, true);
      hv.setUint16(34, 0, true);
      hv.setUint16(36, 0, true);
      hv.setUint32(38, 0, true);
      hv.setUint32(42, offset, true);
      header.set(nameBytes, 46);
      parts.push(local, data);
      central.push(header);
      offset += local.length + data.length;
    });
    var centralSize = 0;
    central.forEach(function (part) { centralSize += part.length; });
    var end = new Uint8Array(22);
    var ev = new DataView(end.buffer);
    ev.setUint32(0, 0x06054b50, true);
    ev.setUint16(4, 0, true);
    ev.setUint16(6, 0, true);
    ev.setUint16(8, central.length, true);
    ev.setUint16(10, central.length, true);
    ev.setUint32(12, centralSize, true);
    ev.setUint32(16, offset, true);
    ev.setUint16(20, 0, true);
    return new Blob(parts.concat(central, [end]), { type: 'application/zip' });
  }

  var api = {
    textBytes: textBytes,
    dataUrlBytes: dataUrlBytes,
    imageExt: imageExt,
    ordinalName: ordinalName,
    createZip: createZip
  };
  global.YDZip = api;
  if (typeof module !== 'undefined' && module.exports) module.exports = api;
})(typeof window !== 'undefined' ? window : globalThis);
/* توقيع المطوّر: يوسف الحمزي — Y- Dimensions — yswefalhmzy@gmail.com */
