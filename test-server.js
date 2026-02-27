/**
 * JSF EC サイトのレンダリング後HTMLを模擬するシンプルなテストサーバー。
 * Playwright E2E テスト実行用 (実際の JSF/Tomcat の代替)。
 */
const http = require('http');
const url = require('url');

const PORT = 8080;

// ---- インメモリ商品ストレージ ----
const PRODUCTS = [
  { id: 1, name: 'ノートパソコン', price: 99800, description: '高性能14インチノートPC。仕事にぴったり。', stock: 5 },
  { id: 2, name: 'ワイヤレスマウス', price: 2980, description: 'コンパクトで持ち運びやすい。', stock: 20 },
  { id: 3, name: 'USBハブ', price: 1980, description: '複数デバイスを接続可能。', stock: 15 },
  { id: 4, name: 'USBメモリ 32GB', price: 1280, description: 'コンパクトで高速転送。', stock: 50 },
];

// ---- カートストレージ (セッション形式) ----
const cartStore = {};
let cartCounter = 0;
function createCart() {
  const cartId = ++cartCounter;
  cartStore[cartId] = [];
  return cartId;
}
function getCart(cartId) {
  if (!cartStore[cartId]) {
    cartStore[cartId] = [];
  }
  return cartStore[cartId];
}
function addToCart(cartId, productId, quantity) {
  const cart = getCart(cartId);
  const item = cart.find(i => i.productId === productId);
  if (item) {
    item.quantity += quantity;
  } else {
    cart.push({ productId, quantity });
  }
}
function removeFromCart(cartId, productId) {
  const cart = getCart(cartId);
  cartStore[cartId] = cart.filter(i => i.productId !== productId);
}
function clearCart(cartId) {
  cartStore[cartId] = [];
}

// ---- HTML ヘルパー ----
function layout(title, body) {
  return `<!DOCTYPE html>
<html lang="ja">
<head>
  <meta charset="UTF-8"/>
  <meta name="viewport" content="width=device-width, initial-scale=1.0"/>
  <title>${title}</title>
  <style>
    *, *::before, *::after { box-sizing: border-box; margin: 0; padding: 0; }
    body { font-family: 'Segoe UI', Arial, sans-serif; background: #f0f4f8; color: #2d3748; }
    .container { max-width: 1000px; margin: 0 auto; padding: 2rem 1rem; }
    header { text-align: center; margin-bottom: 2rem; background: #2b6cb0; color: white; padding: 2rem; border-radius: 8px; }
    header h1 { font-size: 2rem; }
    .subtitle { color: #e2e8f0; font-size: .9rem; margin-top: 0.5rem; }
    nav { background: #fff; padding: 1rem; border-radius: 8px; margin-bottom: 2rem; box-shadow: 0 2px 8px rgba(0,0,0,.08); }
    nav a { margin-right: 1rem; text-decoration: none; color: #2b6cb0; font-weight: 600; }
    nav a:hover { text-decoration: underline; }
    .card { background: #fff; border-radius: 8px; padding: 1.5rem; margin-bottom: 1.5rem; box-shadow: 0 2px 8px rgba(0,0,0,.08); }
    .card h2 { margin-bottom: 1rem; color: #2b6cb0; font-size: 1.1rem; }
    .product-grid { display: grid; grid-template-columns: repeat(auto-fill, minmax(250px, 1fr)); gap: 1.5rem; }
    .product-card { border: 1px solid #e2e8f0; border-radius: 6px; padding: 1rem; text-align: center; }
    .product-card img { width: 100%; max-width: 200px; margin-bottom: 0.5rem; }
    .product-name { font-size: 1rem; font-weight: 600; margin: 0.5rem 0; }
    .product-price { font-size: 1.3rem; color: #e94052; font-weight: 700; margin: 0.5rem 0; }
    .product-stock { font-size: 0.85rem; color: #718096; margin: 0.5rem 0; }
    .btn { display: inline-block; padding: .5rem 1.2rem; border: none; border-radius: 6px; font-size: .9rem; font-weight: 600; cursor: pointer; text-decoration: none; }
    .btn-primary { background: #4299e1; color: #fff; }
    .btn-primary:hover { background: #2b7fb9; }
    .btn-secondary { background: #a0aec0; color: #fff; }
    .btn-secondary:hover { background: #8894a6; }
    .btn-danger { background: #fc8181; color: #fff; }
    .btn-danger:hover { background: #f56565; }
    .form-group { margin-bottom: 1rem; }
    .label { display: block; font-size: .85rem; font-weight: 600; color: #4a5568; margin-bottom: .4rem; }
    .input-field { width: 100%; padding: .55rem .75rem; border: 1px solid #cbd5e0; border-radius: 6px; font-size: .95rem; }
    .select-field { width: 100%; padding: .55rem .75rem; border: 1px solid #cbd5e0; border-radius: 6px; font-size: .95rem; }
    .messages { display: block; padding: .75rem 1rem; border-radius: 6px; margin-bottom: 1rem; background: #ebf8ff; border-left: 4px solid #4299e1; color: #2b6cb0; font-size: .9rem; }
    .error-msg { background: #fff5f5; border-left-color: #fc8181; color: #c05621; }
    .success-msg { background: #f0fff4; border-left-color: #48bb78; color: #276749; }
    .cart-summary { display: flex; justify-content: space-between; align-items: center; padding: 1rem; background: #f7fafc; border-radius: 6px; margin: 1rem 0; }
    .cart-summary .total { font-size: 1.3rem; font-weight: 700; color: #2b6cb0; }
    .cart-item { border-bottom: 1px solid #e2e8f0; padding: 1rem 0; display: flex; justify-content: space-between; align-items: center; }
    .cart-item:last-child { border-bottom: none; }
    .info-table { width: 100%; border-collapse: collapse; font-size: .9rem; }
    .info-table th, .info-table td { padding: .5rem .75rem; border-bottom: 1px solid #e2e8f0; text-align: left; }
    .info-table th { width: 30%; color: #718096; font-weight: 600; }
    .empty-msg { text-align: center; color: #a0aec0; padding: 2rem; font-style: italic; }
    .form-actions { display: flex; gap: .75rem; margin-top: 1.25rem; }
  </style>
</head>
<body>
${body}
</body>
</html>`;
}

// ---- ページレンダリング関数 ----
function productsPage(flash) {
  const msg = flash ? `<p class="messages ${flash.type === 'error' ? 'error-msg' : 'success-msg'}">${flash.text}</p>` : '';
  const products = PRODUCTS.map(p => `
  <div class="product-card">
    <h3 class="product-name">${p.name}</h3>
    <div class="product-price">¥${p.price.toLocaleString()}</div>
    <div class="product-stock">在庫: ${p.stock}個</div>
    <div style="margin-top: 1rem;">
      <a href="/product?id=${p.id}" class="btn btn-secondary">詳細</a>
    </div>
  </div>`).join('');

  return layout('EC サイト - 商品リスト', `
<div class="container">
  <header>
    <h1>🛍️ EC サイト</h1>
    <p class="subtitle">JavaServer Faces (JSF) + Playwright E2E テスト</p>
  </header>
  <nav>
    <a href="/products">商品一覧</a>
    <a href="/cart">🛒 カート</a>
  </nav>
  ${msg}
  <section class="card">
    <h2>📦 おすすめ商品</h2>
    <div class="product-grid">
      ${products}
    </div>
  </section>
</div>`);
}

function productPage(productId, flash) {
  const product = PRODUCTS.find(p => p.id === parseInt(productId));
  if (!product) {
    return layout('エラー', `
<div class="container">
  <header><h1>エラー</h1></header>
  <section class="card error-section">
    <p>⚠ 商品が見つかりません。</p>
    <a href="/products" class="btn btn-secondary">← 商品一覧に戻る</a>
  </section>
</div>`);
  }

  const msg = flash ? `<p class="messages ${flash.type === 'error' ? 'error-msg' : 'success-msg'}">${flash.text}</p>` : '';
  return layout(`商品詳細 - ${product.name}`, `
<div class="container">
  <header><h1>${product.name}</h1></header>
  <nav>
    <a href="/products">← 商品一覧に戻る</a>
  </nav>
  ${msg}
  <section class="card">
    <h3>${product.name}</h3>
    <p>${product.description}</p>
    <table class="info-table">
      <tr><th>価格</th><td>¥${product.price.toLocaleString()}</td></tr>
      <tr><th>在庫</th><td>${product.stock}個</td></tr>
    </table>
  </section>
  <section class="card">
    <h3>カートに追加</h3>
    <form method="post" action="/add-to-cart">
      <input type="hidden" name="productId" value="${product.id}"/>
      <div class="form-group">
        <label class="label" for="quantity">数量</label>
        <select id="quantity" name="quantity" class="select-field">
          ${Array.from({ length: Math.min(10, product.stock) }, (_, i) => `<option value="${i + 1}">${i + 1}</option>`).join('')}
        </select>
      </div>
      <div class="form-actions">
        <button type="submit" class="btn btn-primary" id="addToCartBtn">カートに追加</button>
        <a href="/products" class="btn btn-secondary">キャンセル</a>
      </div>
    </form>
  </section>
</div>`);
}

function cartPage(cartId, flash) {
  const cart = getCart(cartId);
  const msg = flash ? `<p class="messages ${flash.type === 'error' ? 'error-msg' : 'success-msg'}">${flash.text}</p>` : '';

  let items = '';
  let total = 0;
  if (cart.length === 0) {
    items = '<p class="empty-msg">カートが空です</p>';
  } else {
    items = cart.map(item => {
      const product = PRODUCTS.find(p => p.id === item.productId);
      if (!product) return '';
      const subtotal = product.price * item.quantity;
      total += subtotal;
      return `
      <div class="cart-item">
        <div>
          <strong>${product.name}</strong><br/>
          ¥${product.price.toLocaleString()} × ${item.quantity}個
        </div>
        <div style="text-align: right;">
          <div>¥${subtotal.toLocaleString()}</div>
          <form method="post" action="/remove-from-cart" style="display:inline; margin-top: 0.5rem;">
            <input type="hidden" name="productId" value="${product.id}"/>
            <button type="submit" class="btn btn-danger" style="padding: 0.3rem 0.8rem; font-size: 0.8rem;">削除</button>
          </form>
        </div>
      </div>`;
    }).join('');
  }

  const checkoutBtn = cart.length > 0 ? `<a href="/checkout?cartId=${cartId}" class="btn btn-primary" id="checkoutBtn">チェックアウト</a>` : '';

  return layout('カート', `
<div class="container">
  <header><h1>🛒 カート</h1></header>
  <nav>
    <a href="/products">← 商品一覧に戻る</a>
  </nav>
  ${msg}
  <section class="card">
    <h2>カートの内容</h2>
    ${items}
    ${cart.length > 0 ? `
    <div class="cart-summary">
      <span>合計</span>
      <span class="total">¥${total.toLocaleString()}</span>
    </div>
    <div class="form-actions">
      ${checkoutBtn}
      <a href="/products" class="btn btn-secondary">買い物を続ける</a>
    </div>` : ''}
  </section>
</div>`);
}

function checkoutPage(cartId, flash) {
  const cart = getCart(cartId);
  const msg = flash ? `<p class="messages ${flash.type === 'error' ? 'error-msg' : 'success-msg'}">${flash.text}</p>` : '';

  let total = 0;
  const items = cart.map(item => {
    const product = PRODUCTS.find(p => p.id === item.productId);
    if (!product) return '';
    const subtotal = product.price * item.quantity;
    total += subtotal;
    return `<tr><td>${product.name}</td><td>${item.quantity}個</td><td>¥${subtotal.toLocaleString()}</td></tr>`;
  }).join('');

  return layout('チェックアウト', `
<div class="container">
  <header><h1>💳 チェックアウト</h1></header>
  ${msg}
  <section class="card">
    <h2>ご注文内容確認</h2>
    <table class="info-table">
      <tr><th>商品</th><th>数量</th><th>金額</th></tr>
      ${items}
      <tr style="font-weight: bold;"><th colspan="2">合計</th><td>¥${total.toLocaleString()}</td></tr>
    </table>
  </section>
  <section class="card">
    <h2>配送先情報 (ダミー)</h2>
    <table class="info-table">
      <tr><th>お名前</th><td>テスト顧客</td></tr>
      <tr><th>住所</th><td>東京都渋谷区</td></tr>
      <tr><th>電話番号</th><td>09X-XXXX-XXXX</td></tr>
    </table>
  </section>
  <section class="card">
    <h2>注文確定</h2>
    <form method="post" action="/confirm-purchase">
      <input type="hidden" name="cartId" value="${cartId}"/>
      <p style="margin-bottom: 1rem;">以上の内容でご注文いただくことに同意します。</p>
      <div class="form-actions">
        <button type="submit" class="btn btn-primary" id="confirmPurchaseBtn">注文を確定する</button>
        <a href="/cart?cartId=${cartId}" class="btn btn-secondary">戻る</a>
      </div>
    </form>
  </section>
</div>`);
}

function orderCompletePage(orderId) {
  return layout('注文完了', `
<div class="container">
  <header><h1>✅ ご注文ありがとうございます</h1></header>
  <section class="card">
    <p style="text-align: center; font-size: 1.1rem; margin: 2rem 0;">
      ご注文が確定いたしました。
    </p>
    <table class="info-table">
      <tr><th>注文番号</th><td id="orderId">${orderId}</td></tr>
      <tr><th>ステータス</th><td>✓ 注文確定</td></tr>
    </table>
    <div style="margin-top: 2rem; text-align: center;">
      <a href="/products" class="btn btn-primary" id="backToShopBtn">ショッピングを続ける</a>
    </div>
  </section>
</div>`);
}

// ---- POST ボディ パーサー ----
function readBody(req) {
  return new Promise((resolve) => {
    let body = '';
    req.on('data', chunk => body += chunk);
    req.on('end', () => {
      const params = new URLSearchParams(body);
      const obj = {};
      for (const [k, v] of params) obj[k] = v;
      resolve(obj);
    });
  });
}

// ---- フラッシュストレージ (簡易) ----
const flashStore = {};
let flashCounter = 0;
function setFlash(msg, type = 'success') {
  const key = ++flashCounter;
  flashStore[key] = { text: msg, type };
  return key;
}
function getFlash(key) {
  const msg = flashStore[key];
  delete flashStore[key];
  return msg || null;
}

// ---- HTTP サーバー ----
const server = http.createServer(async (req, res) => {
  const parsed = url.parse(req.url, true);
  const path = parsed.pathname;
  const query = parsed.query;

  // GET /  →  /products
  if (req.method === 'GET' && (path === '/' || path === '/index.xhtml')) {
    res.writeHead(302, { Location: '/products' });
    return res.end();
  }

  // GET /reset  →  テスト用にデータをリセット
  if (req.method === 'GET' && path === '/reset') {
    Object.keys(cartStore).forEach(key => delete cartStore[key]);
    res.writeHead(200, { 'Content-Type': 'application/json' });
    return res.end(JSON.stringify({ status: 'reset' }));
  }

  // GET /products
  if (req.method === 'GET' && (path === '/products' || path === '/products.xhtml')) {
    const flash = query.flash ? getFlash(parseInt(query.flash)) : null;
    res.writeHead(200, { 'Content-Type': 'text/html; charset=UTF-8' });
    return res.end(productsPage(flash));
  }

  // GET /product
  if (req.method === 'GET' && (path === '/product' || path === '/product.xhtml')) {
    const flash = query.flash ? getFlash(parseInt(query.flash)) : null;
    res.writeHead(200, { 'Content-Type': 'text/html; charset=UTF-8' });
    return res.end(productPage(query.id, flash));
  }

  // POST /add-to-cart
  if (req.method === 'POST' && path === '/add-to-cart') {
    const body = await readBody(req);
    const cartId = query.cartId || createCart();
    const productId = parseInt(body.productId);
    const quantity = parseInt(body.quantity) || 1;

    const product = PRODUCTS.find(p => p.id === productId);
    if (!product) {
      const fk = setFlash('商品が見つかりません', 'error');
      res.writeHead(302, { Location: `/product?id=${productId}&flash=${fk}` });
      return res.end();
    }

    if (quantity > product.stock) {
      const fk = setFlash('在庫を超えています', 'error');
      res.writeHead(302, { Location: `/product?id=${productId}&flash=${fk}` });
      return res.end();
    }

    addToCart(cartId, productId, quantity);
    const fk = setFlash(`${product.name} をカートに追加しました`);
    res.writeHead(302, { Location: `/cart?cartId=${cartId}&flash=${fk}` });
    return res.end();
  }

  // GET /cart
  if (req.method === 'GET' && (path === '/cart' || path === '/cart.xhtml')) {
    const cartId = query.cartId || createCart();
    const flash = query.flash ? getFlash(parseInt(query.flash)) : null;
    res.writeHead(200, { 'Content-Type': 'text/html; charset=UTF-8' });
    return res.end(cartPage(cartId, flash));
  }

  // POST /remove-from-cart
  if (req.method === 'POST' && path === '/remove-from-cart') {
    const body = await readBody(req);
    const cartId = query.cartId || 1;
    const productId = parseInt(body.productId);
    removeFromCart(cartId, productId);
    const fk = setFlash('商品をカートから削除しました');
    res.writeHead(302, { Location: `/cart?cartId=${cartId}&flash=${fk}` });
    return res.end();
  }

  // GET /checkout
  if (req.method === 'GET' && path === '/checkout') {
    const cartId = query.cartId || 1;
    const flash = query.flash ? getFlash(parseInt(query.flash)) : null;
    res.writeHead(200, { 'Content-Type': 'text/html; charset=UTF-8' });
    return res.end(checkoutPage(cartId, flash));
  }

  // POST /confirm-purchase
  if (req.method === 'POST' && path === '/confirm-purchase') {
    const body = await readBody(req);
    const cartId = parseInt(body.cartId);
    const orderId = `ORD-${Date.now()}-${Math.floor(Math.random() * 10000)}`;
    clearCart(cartId);
    res.writeHead(200, { 'Content-Type': 'text/html; charset=UTF-8' });
    return res.end(orderCompletePage(orderId));
  }

  res.writeHead(404);
  res.end('Not Found');
});

server.listen(PORT, () => {
  console.log(`Test server running at http://localhost:${PORT}`);
});
