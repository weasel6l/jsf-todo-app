/**
 * JSF Todo App のレンダリング後 HTML を模擬するシンプルなテストサーバー。
 * Playwright E2E テスト実行用 (実際の JSF/Tomcat の代替)。
 */
const http = require('http');
const url  = require('url');

const PORT = 8080;

// ---- インメモリ Todo ストレージ ----
const INITIAL_TODOS = [
  { id: 1, title: '買い物をする',      description: 'スーパーで食材を購入する', completed: false },
  { id: 2, title: 'レポートを書く',    description: 'プロジェクトの進捗レポートを完成させる', completed: false },
  { id: 3, title: '運動する',          description: '30分のジョギング', completed: true  },
];
let todos = JSON.parse(JSON.stringify(INITIAL_TODOS));
let nextId = 4;

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
    .container { max-width: 800px; margin: 0 auto; padding: 2rem 1rem; }
    header { text-align: center; margin-bottom: 2rem; }
    header h1 { font-size: 2rem; color: #2b6cb0; }
    .subtitle { color: #718096; font-size: .9rem; }
    .card { background: #fff; border-radius: 8px; padding: 1.5rem; margin-bottom: 1.5rem; box-shadow: 0 2px 8px rgba(0,0,0,.08); }
    .card h2 { margin-bottom: 1rem; color: #2b6cb0; font-size: 1.1rem; }
    .stats { display: flex; gap: .75rem; margin-bottom: 1.5rem; justify-content: center; flex-wrap: wrap; }
    .stat-badge { padding: .35rem .9rem; border-radius: 20px; font-size: .85rem; font-weight: 600; }
    .stat-badge.total     { background: #ebf4ff; color: #2b6cb0; }
    .stat-badge.completed { background: #f0fff4; color: #276749; }
    .stat-badge.pending   { background: #fffaf0; color: #c05621; }
    .form-group { margin-bottom: 1rem; }
    .label { display: block; font-size: .85rem; font-weight: 600; color: #4a5568; margin-bottom: .4rem; }
    .input-field, .textarea-field {
      width: 100%; padding: .55rem .75rem; border: 1px solid #cbd5e0;
      border-radius: 6px; font-size: .95rem; }
    .textarea-field { resize: vertical; }
    .btn { display: inline-block; padding: .5rem 1.2rem; border: none; border-radius: 6px;
           font-size: .9rem; font-weight: 600; cursor: pointer; text-decoration: none; }
    .btn-primary   { background: #4299e1; color: #fff; }
    .btn-secondary { background: #a0aec0; color: #fff; }
    .btn-danger    { background: #fc8181; color: #fff; }
    .todo-item { border: 1px solid #e2e8f0; border-radius: 6px; padding: .9rem 1rem; margin-bottom: .75rem; }
    .item-completed { background: #f7fafc; opacity: .75; }
    .item-pending   { background: #fff; }
    .todo-row { display: flex; align-items: center; gap: .75rem; flex-wrap: wrap; }
    .todo-title { flex: 1; font-size: .95rem; font-weight: 500; }
    .strikethrough { text-decoration: line-through; color: #a0aec0; }
    .todo-desc { margin-top: .4rem; font-size: .85rem; color: #718096; padding-left: 2.5rem; }
    .todo-actions { display: flex; gap: .4rem; }
    .btn-toggle { width: 2rem; height: 2rem; border: 2px solid; border-radius: 50%;
                  font-size: .85rem; cursor: pointer; background: transparent; }
    .toggle-done { border-color: #48bb78; color: #48bb78; }
    .toggle-open { border-color: #a0aec0; color: #a0aec0; }
    .empty-msg { text-align: center; color: #a0aec0; padding: 2rem; font-style: italic; }
    .messages { display: block; padding: .75rem 1rem; border-radius: 6px; margin-bottom: 1rem;
                background: #ebf8ff; border-left: 4px solid #4299e1; color: #2b6cb0; font-size: .9rem; }
    .info-table { width: 100%; border-collapse: collapse; font-size: .9rem; }
    .info-table th, .info-table td { padding: .5rem .75rem; border-bottom: 1px solid #e2e8f0; text-align: left; }
    .info-table th { width: 30%; color: #718096; font-weight: 600; }
    .status-badge { display: inline-block; padding: .2rem .7rem; border-radius: 20px; font-size: .8rem; font-weight: 700; }
    .badge-completed { background: #f0fff4; color: #276749; }
    .badge-pending   { background: #fffaf0; color: #c05621; }
    .form-actions { display: flex; gap: .75rem; margin-top: 1.25rem; }
    .error-section p { color: #c05621; margin-bottom: 1rem; }
  </style>
</head>
<body>
${body}
</body>
</html>`;
}

function todosPage(flash) {
  const completed = todos.filter(t => t.completed).length;
  const pending   = todos.filter(t => !t.completed).length;
  const msg = flash ? `<p class="messages">${flash}</p>` : '';

  const items = todos.map(t => `
  <div class="todo-item ${t.completed ? 'item-completed' : 'item-pending'}" data-todo-id="${t.id}">
    <div class="todo-row">
      <form method="post" action="/toggle" style="display:inline">
        <input type="hidden" name="id" value="${t.id}"/>
        <button type="submit" class="btn-toggle ${t.completed ? 'toggle-done' : 'toggle-open'}"
                aria-label="${t.completed ? '完了済み' : '未完了'}">
          ${t.completed ? '✓' : '○'}
        </button>
      </form>
      <span class="todo-title ${t.completed ? 'strikethrough' : ''}">${t.title}</span>
      <div class="todo-actions">
        <a href="/detail?id=${t.id}" class="btn btn-secondary">詳細・編集</a>
        <form method="post" action="/delete" style="display:inline"
              onsubmit="return confirm('このTodoを削除しますか？')">
          <input type="hidden" name="id" value="${t.id}"/>
          <button type="submit" class="btn btn-danger">削除</button>
        </form>
      </div>
    </div>
    ${t.description ? `<p class="todo-desc">${t.description}</p>` : ''}
  </div>`).join('');

  const list = todos.length === 0
    ? '<p class="empty-msg">Todoがありません。上のフォームから追加してください。</p>'
    : items;

  return layout('JSF Todo App - 一覧', `
<div class="container">
  <header>
    <h1>📋 JSF Todo App</h1>
    <p class="subtitle">FlashContainerを活用した状態管理</p>
  </header>
  ${msg}
  <div class="stats">
    <span class="stat-badge total">合計: ${todos.length}</span>
    <span class="stat-badge completed">完了: ${completed}</span>
    <span class="stat-badge pending">未完了: ${pending}</span>
  </div>
  <section class="card">
    <h2>✚ 新しいTodoを追加</h2>
    <form method="post" action="/add">
      <div class="form-group">
        <label class="label" for="newTitle">タイトル *</label>
        <input type="text" id="newTitle" name="title" class="input-field"
               placeholder="例: 買い物をする" maxlength="100"/>
      </div>
      <div class="form-group">
        <label class="label" for="newDesc">説明（任意）</label>
        <textarea id="newDesc" name="description" class="textarea-field" rows="3"
                  placeholder="詳細説明を入力（省略可）"></textarea>
      </div>
      <button type="submit" class="btn btn-primary" id="addTodoBtn">追加する</button>
    </form>
  </section>
  <section class="card">
    <h2>📝 Todo 一覧</h2>
    ${list}
  </section>
</div>`);
}

function detailPage(todo, flash) {
  if (!todo) {
    return layout('Todo 詳細 - JSF Todo App', `
<div class="container">
  <header><h1>📋 Todo 詳細・編集</h1></header>
  <section class="card error-section">
    <p>⚠ Todoが見つかりません。セッションが切れた可能性があります。</p>
    <a href="/todos" class="btn btn-secondary">← 一覧に戻る</a>
  </section>
</div>`);
  }
  const msg = flash ? `<p class="messages">${flash}</p>` : '';
  return layout('Todo 詳細 - JSF Todo App', `
<div class="container">
  <header><h1>📋 Todo 詳細・編集</h1></header>
  ${msg}
  <section class="card">
    <h3>📄 基本情報 (Flashから取得)</h3>
    <table class="info-table">
      <tr><th>ID</th><td>${todo.id}</td></tr>
      <tr><th>ステータス</th><td>
        <span class="status-badge ${todo.completed ? 'badge-completed' : 'badge-pending'}">
          ${todo.completed ? '✓ 完了' : '○ 未完了'}
        </span>
      </td></tr>
    </table>
  </section>
  <section class="card">
    <h3>✏ 編集</h3>
    <form method="post" action="/save">
      <input type="hidden" name="id" value="${todo.id}"/>
      <div class="form-group">
        <label class="label" for="editTitle">タイトル *</label>
        <input type="text" id="editTitle" name="title" class="input-field"
               value="${todo.title}" maxlength="100" required/>
      </div>
      <div class="form-group">
        <label class="label" for="editDesc">説明</label>
        <textarea id="editDesc" name="description" class="textarea-field" rows="4">${todo.description}</textarea>
      </div>
      <div class="form-actions">
        <button type="submit" class="btn btn-primary">保存</button>
        <a href="/todos" class="btn btn-secondary">キャンセル</a>
      </div>
    </form>
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
function setFlash(msg) {
  const key = ++flashCounter;
  flashStore[key] = msg;
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
  const path   = parsed.pathname;
  const query  = parsed.query;

  // GET /  →  /todos
  if (req.method === 'GET' && (path === '/' || path === '/index.xhtml')) {
    res.writeHead(302, { Location: '/todos' });
    return res.end();
  }

  // GET /reset  →  テスト用にデータをリセット
  if (req.method === 'GET' && path === '/reset') {
    todos = JSON.parse(JSON.stringify(INITIAL_TODOS));
    nextId = 4;
    res.writeHead(200, { 'Content-Type': 'application/json' });
    return res.end(JSON.stringify({ status: 'reset', count: todos.length }));
  }

  // GET /todos
  if (req.method === 'GET' && (path === '/todos' || path === '/todos.xhtml')) {
    const flash = query.flash ? getFlash(parseInt(query.flash)) : null;
    res.writeHead(200, { 'Content-Type': 'text/html; charset=UTF-8' });
    return res.end(todosPage(flash));
  }

  // POST /add
  if (req.method === 'POST' && path === '/add') {
    const body = await readBody(req);
    if (body.title && body.title.trim()) {
      todos.push({ id: nextId++, title: body.title.trim(),
                   description: (body.description || '').trim(), completed: false });
      const fk = setFlash('Todoを追加しました');
      res.writeHead(302, { Location: `/todos?flash=${fk}` });
    } else {
      res.writeHead(302, { Location: '/todos' });
    }
    return res.end();
  }

  // POST /toggle
  if (req.method === 'POST' && path === '/toggle') {
    const body = await readBody(req);
    const todo = todos.find(t => t.id === parseInt(body.id));
    if (todo) todo.completed = !todo.completed;
    res.writeHead(302, { Location: '/todos' });
    return res.end();
  }

  // POST /delete
  if (req.method === 'POST' && path === '/delete') {
    const body = await readBody(req);
    todos = todos.filter(t => t.id !== parseInt(body.id));
    const fk = setFlash('Todoを削除しました');
    res.writeHead(302, { Location: `/todos?flash=${fk}` });
    return res.end();
  }

  // GET /detail
  if (req.method === 'GET' && (path === '/detail' || path === '/detail.xhtml')) {
    const todo  = todos.find(t => t.id === parseInt(query.id));
    const flash = query.flash ? getFlash(parseInt(query.flash)) : null;
    res.writeHead(200, { 'Content-Type': 'text/html; charset=UTF-8' });
    return res.end(detailPage(todo, flash));
  }

  // POST /save
  if (req.method === 'POST' && path === '/save') {
    const body = await readBody(req);
    const todo = todos.find(t => t.id === parseInt(body.id));
    if (todo && body.title && body.title.trim()) {
      todo.title       = body.title.trim();
      todo.description = (body.description || '').trim();
    }
    const fk = setFlash('Todoを更新しました');
    res.writeHead(302, { Location: `/todos?flash=${fk}` });
    return res.end();
  }

  res.writeHead(404);
  res.end('Not Found');
});

server.listen(PORT, () => {
  console.log(`Test server running at http://localhost:${PORT}`);
});
