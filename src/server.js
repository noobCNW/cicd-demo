const http = require('http');
const PORT = process.env.PORT || 8080;
const VERSION = process.env.APP_VERSION || '1.0.0';

const server = http.createServer((req, res) => {
  if (req.url === '/health') {
    res.writeHead(200, { 'Content-Type': 'application/json' });
    res.end(JSON.stringify({ status: 'ok', version: VERSION, service: 'cicd-demo' }));
    return;
  }
  res.writeHead(200, { 'Content-Type': 'text/plain; charset=utf-8' });
  res.end(`Hello from cicd-demo v${VERSION}\n`);
});

server.listen(PORT, () => console.log(`cicd-demo listening on :${PORT}`));
