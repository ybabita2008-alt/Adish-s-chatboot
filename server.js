const http = require('http');
const https = require('https');
const fs = require('fs');
const path = require('path');

// Port 3000 is required by AI Studio Nginx reverse proxy
const PORT = 3000;
const APP_DIR = process.cwd();

// Helper to locate Gemini API Key
function getGeminiApiKey() {
  if (process.env.GEMINI_API_KEY && process.env.GEMINI_API_KEY !== 'MY_GEMINI_API_KEY') {
    return process.env.GEMINI_API_KEY;
  }
  // Try reading from .dev.env.json
  try {
    const devEnvPath = path.resolve(APP_DIR, '..', '.dev.env.json');
    if (fs.existsSync(devEnvPath)) {
      const data = JSON.parse(fs.readFileSync(devEnvPath, 'utf8'));
      if (data.GEMINI_API_KEY) return data.GEMINI_API_KEY;
    }
  } catch (e) {}

  // Try reading from .env
  try {
    const envPath = path.resolve(APP_DIR, '.env');
    if (fs.existsSync(envPath)) {
      const envContent = fs.readFileSync(envPath, 'utf8');
      const match = envContent.match(/^GEMINI_API_KEY=(.*)$/m);
      if (match && match[1] && match[1].trim() !== 'MY_GEMINI_API_KEY') {
        return match[1].trim().replace(/^["']|["']$/g, '');
      }
    }
  } catch (e) {}

  return null;
}

const UNIFIED_SYSTEM_PROMPT = `You are "Adish's chatboot" — a powerful all-in-one AI chatbot and software assistant created by Adish Yadav.
You seamlessly unify all advanced AI capabilities into a single powerful assistant:
1. Omniscient Reasoning & Knowledge: Provide clear, structured, and insightful answers across science, mathematics, philosophy, literature, everyday advice, and general reasoning.
2. Elite Software Engineering: Write production-grade, clean, secure, and idiomatic code across all languages (Kotlin, Python, TypeScript, Java, C++, Rust, Go, SQL, HTML/CSS). Always wrap code snippets in markdown code blocks with syntax highlighting.
3. Root-Cause Debugging: Analyze user code snippets, stack traces, and runtime errors, explaining the exact root cause and providing verified line-by-line solutions.
4. Rapid & High-Density Answers: Provide direct, concise, and scannable answers with bullet points and clear formatting.

Whenever asked about your identity or creator, proudly state that you are "Adish's chatboot", a powerful AI chatbot created by Adish Yadav.`;

// Calls Google Gemini REST API
function callGemini(apiKey, messages) {
  return new Promise((resolve, reject) => {
    // Convert chat history to Gemini format
    const contents = messages.slice(-15).map(m => ({
      role: m.role === 'user' ? 'user' : 'model',
      parts: [{ text: m.content }]
    }));

    const payload = JSON.stringify({
      systemInstruction: {
        parts: [{ text: UNIFIED_SYSTEM_PROMPT }]
      },
      contents: contents,
      generationConfig: {
        temperature: 0.5,
        topP: 0.95
      }
    });

    const modelsToTry = ['gemini-3.8-flash', 'gemini-3.6-flash', 'gemini-2.0-flash'];
    
    function tryModel(index) {
      if (index >= modelsToTry.length) {
        return reject(new Error('All Gemini models exhausted.'));
      }

      const model = modelsToTry[index];
      const options = {
        hostname: 'generativelanguage.googleapis.com',
        path: `/v1beta/models/${model}:generateContent?key=${encodeURIComponent(apiKey)}`,
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
          'Content-Length': Buffer.byteLength(payload)
        },
        timeout: 30000
      };

      const req = https.request(options, (res) => {
        let body = '';
        res.on('data', chunk => body += chunk);
        res.on('end', () => {
          if (res.statusCode >= 200 && res.statusCode < 300) {
            try {
              const json = JSON.parse(body);
              const candidate = json.candidates && json.candidates[0];
              const text = candidate && candidate.content && candidate.content.parts && candidate.content.parts[0] && candidate.content.parts[0].text;
              const tokens = json.usageMetadata && json.usageMetadata.totalTokenCount;
              if (text) {
                return resolve({ reply: text, tokenCount: tokens, model: model });
              }
            } catch (err) {
              return reject(err);
            }
          }
          // If transient error (429 or 503), try next model
          if (res.statusCode === 429 || res.statusCode === 503) {
            return tryModel(index + 1);
          }
          let errorMsg = `HTTP ${res.statusCode}`;
          try {
            const errJson = JSON.parse(body);
            if (errJson.error && errJson.error.message) {
              errorMsg = errJson.error.message;
            }
          } catch (_) {}
          reject(new Error(errorMsg));
        });
      });

      req.on('error', (err) => {
        if (index + 1 < modelsToTry.length) {
          tryModel(index + 1);
        } else {
          reject(err);
        }
      });

      req.write(payload);
      req.end();
    }

    tryModel(0);
  });
}

// Fallback response when no GEMINI_API_KEY is configured
function generateLocalFallback(prompt) {
  const p = prompt.toLowerCase();

  if (p.includes('compose') || p.includes('android') || p.includes('kotlin')) {
    return {
      reply: `### Adish's chatboot Response\n\nHere is a production-grade Jetpack Compose composable with state hoisting, clean animations, and Material 3 design:\n\n\`\`\`kotlin\n@Composable\nfun AnimatedActionCard(\n    title: String,\n    subtitle: String,\n    onClick: () -> Unit,\n    modifier: Modifier = Modifier\n) {\n    var isHovered by remember { mutableStateOf(false) }\n    val scale by animateFloatAsState(\n        targetValue = if (isHovered) 1.02f else 1.0f,\n        label = "cardScale"\n    )\n\n    Card(\n        modifier = modifier\n            .scale(scale)\n            .clickable(onClick = onClick),\n        shape = RoundedCornerShape(16.dp),\n        colors = CardDefaults.cardColors(\n            containerColor = MaterialTheme.colorScheme.surfaceVariant\n        )\n    ) {\n        Column(modifier = Modifier.padding(16.dp)) {\n            Text(\n                text = title,\n                style = MaterialTheme.typography.titleMedium,\n                fontWeight = FontWeight.Bold\n            )\n            Spacer(modifier = Modifier.height(4.dp))\n            Text(\n                text = subtitle,\n                style = MaterialTheme.typography.bodySmall,\n                color = MaterialTheme.colorScheme.onSurfaceVariant\n            )\n        }\n    }\n}\n\`\`\`\n\n*Created by Adish Yadav.*`,
      tokenCount: 220
    };
  }

  if (p.includes('lru') || p.includes('cache') || p.includes('typescript')) {
    return {
      reply: `### Adish's chatboot Response\n\nHere is a high-performance **O(1) LRU Cache** implementation using a doubly-linked list and a Map in TypeScript:\n\n\`\`\`typescript\nclass DNode<K, V> {\n  key: K;\n  val: V;\n  prev: DNode<K, V> | null = null;\n  next: DNode<K, V> | null = null;\n  constructor(key: K, val: V) { this.key = key; this.val = val; }\n}\n\nexport class LRUCache<K, V> {\n  private capacity: number;\n  private map: Map<K, DNode<K, V>> = new Map();\n  private head: DNode<K, V>;\n  private tail: DNode<K, V>;\n\n  constructor(capacity: number) {\n    this.capacity = capacity;\n    this.head = new DNode(null as any, null as any);\n    this.tail = new DNode(null as any, null as any);\n    this.head.next = this.tail;\n    this.tail.prev = this.head;\n  }\n\n  get(key: K): V | undefined {\n    const node = this.map.get(key);\n    if (!node) return undefined;\n    this.moveToFront(node);\n    return node.val;\n  }\n\n  put(key: K, val: V): void {\n    if (this.map.has(key)) {\n      const node = this.map.get(key)!;\n      node.val = val;\n      this.moveToFront(node);\n      return;\n    }\n    if (this.map.size >= this.capacity) {\n      const lru = this.tail.prev!;\n      this.removeNode(lru);\n      this.map.delete(lru.key);\n    }\n    const newNode = new DNode(key, val);\n    this.map.set(key, newNode);\n    this.addNode(newNode);\n  }\n\n  private addNode(node: DNode<K, V>) {\n    node.next = this.head.next;\n    node.prev = this.head;\n    this.head.next!.prev = node;\n    this.head.next = node;\n  }\n\n  private removeNode(node: DNode<K, V>) {\n    node.prev!.next = node.next;\n    node.next!.prev = node.prev;\n  }\n\n  private moveToFront(node: DNode<K, V>) {\n    this.removeNode(node);\n    this.addNode(node);\n  }\n}\n\`\`\`\n\n**Time & Space Complexity**:\n- \`get(key)\`: **O(1)**\n- \`put(key, value)\`: **O(1)**\n- Space: **O(capacity)**\n\n*Created by Adish Yadav.*`,
      tokenCount: 450
    };
  }

  return {
    reply: `Hello! I am **Adish's chatboot** — a powerfull AI chatboot created by Adish Yadav.\n\nI combine all advanced capabilities in one:\n- **Deep Reasoning & Omniscient Knowledge**: Science, math, philosophy, and real-world queries\n- **Elite Software Engineering**: Writing production-grade code across all modern languages\n- **Bug Diagnostics & Root-Cause Debugging**: Fixing issues, compiler errors, and stack traces\n- **Rapid & Concise Explanations**: Delivering clean, actionable insights instantly\n\nHow can I help you today?`,
    tokenCount: 110
  };
}

const server = http.createServer((req, res) => {
  const parsedUrl = new URL(req.url, `http://${req.headers.host || 'localhost'}`);
  const pathname = parsedUrl.pathname;

  // Set standard security and CORS headers
  res.setHeader('Access-Control-Allow-Origin', '*');
  res.setHeader('Access-Control-Allow-Methods', 'GET, POST, OPTIONS');
  res.setHeader('Access-Control-Allow-Headers', 'Content-Type, Authorization');

  if (req.method === 'OPTIONS') {
    res.statusCode = 204;
    return res.end();
  }

  // Health endpoint
  if (pathname === '/health' || pathname === '/api/health') {
    res.setHeader('Content-Type', 'application/json');
    return res.end(JSON.stringify({
      status: 'ok',
      port: PORT,
      uptime: process.uptime(),
      apkReady: true
    }));
  }

  // APK download endpoint
  if (pathname === '/app-debug.apk' || pathname === '/api/download-apk') {
    const possiblePaths = [
      path.resolve(APP_DIR, '.build-outputs', 'app-debug.apk'),
      path.resolve(APP_DIR, 'app', 'build', 'outputs', 'apk', 'debug', 'app-debug.apk')
    ];

    let apkPath = null;
    for (const p of possiblePaths) {
      if (fs.existsSync(p)) {
        apkPath = p;
        break;
      }
    }

    if (apkPath) {
      const stat = fs.statSync(apkPath);
      res.writeHead(200, {
        'Content-Type': 'application/vnd.android.package-archive',
        'Content-Length': stat.size,
        'Content-Disposition': 'attachment; filename="app-debug.apk"'
      });
      return fs.createReadStream(apkPath).pipe(res);
    } else {
      res.statusCode = 404;
      return res.end('APK not found. Run compile_applet to build the APK first.');
    }
  }

  // Chat API endpoint
  if (pathname === '/api/chat' && req.method === 'POST') {
    let body = '';
    req.on('data', chunk => body += chunk);
    req.on('end', async () => {
      try {
        const data = JSON.parse(body || '{}');
        const messages = data.messages || [];
        const mode = data.mode || 'GENERAL';
        const apiKey = getGeminiApiKey();

        if (apiKey) {
          try {
            const result = await callGemini(apiKey, messages);
            res.setHeader('Content-Type', 'application/json');
            return res.end(JSON.stringify(result));
          } catch (apiErr) {
            console.warn('Gemini API call failed, falling back to local reasoning:', apiErr.message);
            // Fall back gracefully if quota exceeded or key invalid
            const lastUserMsg = messages.filter(m => m.role === 'user').pop()?.content || '';
            const fallback = generateLocalFallback(lastUserMsg);
            res.setHeader('Content-Type', 'application/json');
            return res.end(JSON.stringify({
              ...fallback,
              note: `Gemini API note: ${apiErr.message}. Serving assistant guidance.`
            }));
          }
        } else {
          // No API key configured
          const lastUserMsg = messages.filter(m => m.role === 'user').pop()?.content || '';
          const fallback = generateLocalFallback(lastUserMsg);
          res.setHeader('Content-Type', 'application/json');
          return res.end(JSON.stringify(fallback));
        }
      } catch (err) {
        res.statusCode = 400;
        res.setHeader('Content-Type', 'application/json');
        return res.end(JSON.stringify({ error: 'Invalid JSON request payload: ' + err.message }));
      }
    });
    return;
  }

  // Serve index.html for root
  if (pathname === '/' || pathname === '/index.html') {
    const indexPath = path.resolve(APP_DIR, 'index.html');
    if (fs.existsSync(indexPath)) {
      res.setHeader('Content-Type', 'text/html; charset=utf-8');
      return fs.createReadStream(indexPath).pipe(res);
    }
  }

  // Serve static assets from public/ or root
  const safePath = path.normalize(path.join(APP_DIR, pathname));
  if (safePath.startsWith(APP_DIR) && fs.existsSync(safePath) && fs.statSync(safePath).isFile()) {
    const ext = path.extname(safePath).toLowerCase();
    const mimeTypes = {
      '.html': 'text/html',
      '.css': 'text/css',
      '.js': 'application/javascript',
      '.json': 'application/json',
      '.png': 'image/png',
      '.jpg': 'image/jpeg',
      '.svg': 'image/svg+xml',
      '.ico': 'image/x-icon'
    };
    res.setHeader('Content-Type', mimeTypes[ext] || 'application/octet-stream');
    return fs.createReadStream(safePath).pipe(res);
  }

  // Default fallback to index.html for client-side navigation
  const fallbackIndexPath = path.resolve(APP_DIR, 'index.html');
  if (fs.existsSync(fallbackIndexPath)) {
    res.setHeader('Content-Type', 'text/html; charset=utf-8');
    return fs.createReadStream(fallbackIndexPath).pipe(res);
  }

  res.statusCode = 404;
  res.end('Not Found');
});

server.listen(PORT, '0.0.0.0', () => {
  console.log(`AI Chatbot dev server running on http://0.0.0.0:${PORT}`);
});
