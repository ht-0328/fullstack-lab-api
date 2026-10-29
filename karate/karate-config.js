// 接続先は環境変数 BASE_URL で指定する（compose.test.yaml では http://api:8080）
function fn() {
  karate.configure('connectTimeout', 5000);
  karate.configure('readTimeout', 10000);
  return { baseUrl: java.lang.System.getenv('BASE_URL') || 'http://api:8080' };
}
