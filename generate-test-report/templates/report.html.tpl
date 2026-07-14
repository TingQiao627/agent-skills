<!DOCTYPE html>
<html lang="zh-CN">
<head>
  <meta charset="UTF-8">
  <meta name="viewport" content="width=device-width, initial-scale=1.0">
  <title>测试报告</title>
  <style>
    body { font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, sans-serif; margin: 40px; }
    .header { background: #f5f5f5; padding: 20px; border-radius: 8px; }
    .summary { margin: 20px 0; }
    .summary table { width: 100%; border-collapse: collapse; }
    .summary th, .summary td { padding: 10px; border: 1px solid #ddd; text-align: left; }
    .pass { color: #28a745; }
    .fail { color: #dc3545; }
    .failure-detail { background: #fff3cd; padding: 15px; border-radius: 4px; margin: 10px 0; }
    .coverage { margin-top: 30px; }
    code { background: #f8f9fa; padding: 2px 6px; border-radius: 3px; }
  </style>
</head>
<body>
  <div class="header">
    <h1>测试报告</h1>
    <p><strong>生成时间</strong>: {{timestamp}}</p>
    <p><strong>框架</strong>: {{framework}} {{version}}</p>
    <p><strong>执行命令</strong>: <code>{{command}}</code></p>
  </div>

  <div class="summary">
    <h2>结果摘要</h2>
    <table>
      <tr><th>指标</th><th>值</th></tr>
      <tr><td>用例总数</td><td>{{total}}</td></tr>
      <tr><td>通过</td><td class="pass">{{passed}}</td></tr>
      <tr><td>失败</td><td class="fail">{{failed}}</td></tr>
      <tr><td>跳过</td><td>{{skipped}}</td></tr>
      <tr><td>通过率</td><td>{{passRate}}%</td></tr>
      <tr><td>总耗时</td><td>{{duration}}s</td></tr>
      <tr><td>结论</td><td class="{{statusClass}}">{{status}}</td></tr>
    </table>
  </div>

  {{#if failures}}
  <div class="failures">
    <h2>失败用例分析</h2>
    {{#each failures}}
    <div class="failure-detail">
      <h3>❌ {{testCaseName}}</h3>
      <p><strong>文件</strong>: {{file}}</p>
      <p><strong>错误</strong>: {{errorMessage}}</p>
      <pre><code>{{stackTrace}}</code></pre>
    </div>
    {{/each}}
  </div>
  {{/if}}

  <div class="coverage">
    <h2>覆盖率</h2>
    {{#if coverage}}
    <table>
      <tr><th>类型</th><th>覆盖率</th></tr>
      <tr><td>语句</td><td>{{coverage.statements}}%</td></tr>
      <tr><td>分支</td><td>{{coverage.branches}}%</td></tr>
      <tr><td>函数</td><td>{{coverage.functions}}%</td></tr>
      <tr><td>行</td><td>{{coverage.lines}}%</td></tr>
    </table>
    {{else}}
    <p><strong>未获取</strong></p>
    {{/if}}
  </div>

  <div class="appendix">
    <h2>附录</h2>
    <p><strong>原始结果文件</strong>: {{resultFile}}</p>
    <p><strong>生成工具</strong>: generate-test-report v1.0.0</p>
  </div>
</body>
</html>