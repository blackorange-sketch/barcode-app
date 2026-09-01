const CACHE = 'barcode128-v4';
const ASSETS = [
  './',
  './index.html',
  './manifest.json',
  './icon-192.png',
  './icon-512.png',
  './JsBarcode.all.min.js',
  './qrcode.min.js',
  './ocr/tesseract.min.js',
  './ocr/worker.min.js',
  './ocr/tesseract-core.wasm.js',
  './ocr/tesseract-core-simd.wasm.js',
  './ocr/tesseract-core-lstm.wasm.js',
  './ocr/tesseract-core-simd-lstm.wasm.js',
  './ocr/eng.traineddata.gz'
];

self.addEventListener('install', e => {
  e.waitUntil(
    caches.open(CACHE).then(cache => cache.addAll(ASSETS)).catch(()=>{})
  );
  self.skipWaiting();
});

self.addEventListener('activate', e => {
  e.waitUntil(
    caches.keys().then(keys =>
      Promise.all(keys.filter(k => k !== CACHE).map(k => caches.delete(k)))
    )
  );
  self.clients.claim();
});

// HTML/навігація — завжди спершу мережа (щоб бачити свіжі зміни одразу),
// кеш лише як резерв, якщо офлайн.
function isHtmlRequest(request){
  return request.mode === 'navigate' || request.destination === 'document';
}

self.addEventListener('fetch', e => {
  if(isHtmlRequest(e.request)){
    e.respondWith(
      fetch(e.request).then(res => {
        const resClone = res.clone();
        caches.open(CACHE).then(cache => cache.put(e.request, resClone)).catch(()=>{});
        return res;
      }).catch(() => caches.match(e.request))
    );
    return;
  }

  // Статичні файли (іконки, JS) — спершу кеш, мережа як резерв.
  e.respondWith(
    caches.match(e.request).then(cached => cached || fetch(e.request).then(res => {
      const resClone = res.clone();
      caches.open(CACHE).then(cache => cache.put(e.request, resClone)).catch(()=>{});
      return res;
    }).catch(() => cached))
  );
});
