#!/usr/bin/env python3
"""
测试报告生成器 (Test Report Generator)
从解析结果 JSON 生成 Markdown/HTML/JSON 格式报告
"""

import argparse
import json
import os
import sys
from datetime import datetime


def generate_markdown(data: dict) -> str:
    """生成 Markdown 格式报告"""
    summary = data.get('summary', {})
    failures = data.get('failures', [])
    test_cases = data.get('test_cases', [])
    coverage = data.get('coverage')
    errors = data.get('errors', [])

    lines = []

    # 1. 报告头
    lines.append(f"# 测试报告 — {data.get('project_name', 'Unknown')}")
    lines.append("")
    lines.append("| 属性 | 值 |")
    lines.append("|------|-----|")
    lines.append(f"| 项目 | {data.get('project_name', '未知')} |")
    lines.append(f"| 生成时间 | {data.get('timestamp', datetime.now().strftime('%Y-%m-%d %H:%M:%S'))} |")
    lines.append(f"| 执行命令 | `{data.get('command', '未知')}` |")
    lines.append(f"| 框架 | {data.get('framework', '未知')} |")
    lines.append("")

    # 2. 结果摘要
    total = summary.get('total', 0)
    passed = summary.get('passed', 0)
    failed = summary.get('failed', 0)
    skipped = summary.get('skipped', 0)
    pass_rate = summary.get('pass_rate', 0)
    duration = summary.get('duration', 0)
    conclusion = summary.get('conclusion', 'unknown')

    lines.append("## 📊 结果摘要")
    lines.append("")
    lines.append("| 指标 | 值 |")
    lines.append("|------|-----|")
    lines.append(f"| 用例总数 | {total} |")
    lines.append(f"| ✅ 通过 | {passed} |")
    lines.append(f"| ❌ 失败 | {failed} |")
    lines.append(f"| ⏭️ 跳过 | {skipped} |")
    lines.append(f"| 通过率 | {pass_rate}% |")
    lines.append(f"| 总耗时 | {duration}s |")
    lines.append("")

    conclusion_icon = "✅" if conclusion == "pass" else "❌"
    conclusion_text = "全部通过" if conclusion == "pass" else "存在失败"
    lines.append(f"**结论：{conclusion_icon} {conclusion_text}**")
    lines.append("")

    # 3. 失败用例分析
    if failures:
        lines.append("## 🔴 失败用例分析")
        lines.append("")
        for i, failure in enumerate(failures, 1):
            lines.append(f"### {i}. {failure.get('name', '未知用例')}")
            lines.append("")
            lines.append(f"- **文件**: `{failure.get('file', '未知')}`")
            err_msg = failure.get('error', '')
            if err_msg:
                # 截断过长的错误信息
                if len(err_msg) > 300:
                    err_msg = err_msg[:300] + "..."
                lines.append(f"- **错误**: {err_msg}")
            stack = failure.get('stack', '')
            if stack:
                lines.append("- **堆栈**:")
                lines.append("  ```")
                for line in stack.strip().split('\n')[:15]:
                    lines.append(f"  {line}")
                lines.append("  ```")
            lines.append("")
    elif conclusion == "pass":
        lines.append("## ✅ 无失败用例")
        lines.append("")
        lines.append("所有测试用例均已通过。")
        lines.append("")

    # 4. 用例明细
    lines.append("## 📋 用例明细")
    lines.append("")

    MAX_DISPLAY = 200
    if len(test_cases) > MAX_DISPLAY:
        lines.append(f"> ⚠️ 用例总数 {len(test_cases)} 条，超过 {MAX_DISPLAY} 条上限，仅展示前 {MAX_DISPLAY} 条。")
        lines.append("")

    # 按文件分组
    file_groups = {}
    for tc in test_cases[:MAX_DISPLAY]:
        fname = tc.get('file', '未知文件')
        if fname not in file_groups:
            file_groups[fname] = []
        file_groups[fname].append(tc)

    for fname, cases in file_groups.items():
        display_name = _shorten_path(fname)
        lines.append(f"### {display_name}")
        lines.append("")
        lines.append("| 用例 | 状态 | 耗时 |")
        lines.append("|------|------|------|")
        for tc in cases:
            status_icon = {"passed": "✅", "failed": "❌", "skipped": "⏭️"}.get(tc.get('status', ''), '❓')
            lines.append(f"| {tc.get('name', '?')} | {status_icon} | {tc.get('duration', 0)}ms |")
        lines.append("")

    # 5. 覆盖率
    lines.append("## 📈 覆盖率")
    lines.append("")
    if coverage:
        lines.append("| 类型 | 覆盖率 |")
        lines.append("|------|--------|")
        lines.append(f"| 语句 (Statements) | {coverage.get('statements', 0)}% |")
        lines.append(f"| 分支 (Branches) | {coverage.get('branches', 0)}% |")
        lines.append(f"| 函数 (Functions) | {coverage.get('functions', 0)}% |")
        lines.append(f"| 行 (Lines) | {coverage.get('lines', 0)}% |")
        lines.append("")

        low_files = coverage.get('low_files', [])
        if low_files:
            lines.append("### ⚠️ 覆盖率低于阈值的文件")
            lines.append("")
            lines.append("| 文件 | 行覆盖率 |")
            lines.append("|------|----------|")
            for lf in low_files:
                lines.append(f"| {_shorten_path(lf.get('file', ''))} | {lf.get('rate', 0)}% |")
            lines.append("")
    else:
        lines.append("> 覆盖率数据未获取（如未配置 coverage reporter）")
        lines.append("")

    # 6. 附录
    lines.append("## 📎 附录")
    lines.append("")
    artifacts = data.get('artifacts', [])
    if artifacts:
        for art in artifacts:
            lines.append(f"- 原始结果文件：`{art}`")
    lines.append("- 生成工具：test-report-generation v1.0")
    lines.append("")

    if errors:
        lines.append("### ⚠️ 解析警告")
        for err in errors:
            lines.append(f"- {err}")
        lines.append("")

    return '\n'.join(lines)


def generate_html(data: dict) -> str:
    """生成 HTML 格式报告（简化版）"""
    md_content = generate_markdown(data)
    html = f"""<!DOCTYPE html>
<html lang="zh-CN">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>测试报告 — {data.get('project_name', 'Unknown')}</title>
    <style>
        body {{ font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', sans-serif; max-width: 960px; margin: 0 auto; padding: 20px; color: #333; }}
        h1 {{ border-bottom: 2px solid #eee; padding-bottom: 10px; }}
        h2 {{ margin-top: 30px; color: #555; }}
        table {{ border-collapse: collapse; width: 100%; margin: 10px 0; }}
        th, td {{ border: 1px solid #ddd; padding: 8px 12px; text-align: left; }}
        th {{ background-color: #f5f5f5; }}
        pre {{ background: #f8f8f8; border: 1px solid #eee; border-radius: 4px; padding: 12px; overflow-x: auto; font-size: 13px; }}
        .pass {{ color: #22c55e; }} .fail {{ color: #ef4444; }} .skip {{ color: #f59e0b; }}
    </style>
</head>
<body>
    <pre style="white-space: pre-wrap;">{md_content}</pre>
</body>
</html>"""
    return html


def generate_json(data: dict) -> str:
    """生成 JSON 格式报告"""
    return json.dumps(data, ensure_ascii=False, indent=2)


def _shorten_path(path: str, max_len: int = 60) -> str:
    if len(path) <= max_len:
        return path
    return '...' + path[-(max_len - 3):]


def main():
    parser = argparse.ArgumentParser(description='测试报告生成器')
    parser.add_argument('--parsed-file', required=True, help='解析结果 JSON 文件路径')
    parser.add_argument('--output', required=True, help='输出报告文件路径')
    parser.add_argument('--format', default='markdown',
                        choices=['markdown', 'html', 'json'],
                        help='输出格式 (默认 markdown)')
    parser.add_argument('--project-name', default='', help='项目名称')
    parser.add_argument('--command', default='', help='测试命令')

    args = parser.parse_args()

    if not os.path.exists(args.parsed_file):
        print(f"错误: 解析结果文件不存在: {args.parsed_file}", file=sys.stderr)
        sys.exit(1)

    with open(args.parsed_file, 'r', encoding='utf-8') as f:
        data = json.load(f)

    if args.project_name:
        data['project_name'] = args.project_name
    if args.command:
        data['command'] = args.command

    generators = {
        'markdown': generate_markdown,
        'html': generate_html,
        'json': generate_json,
    }

    content = generators[args.format](data)

    os.makedirs(os.path.dirname(args.output) or '.', exist_ok=True)
    with open(args.output, 'w', encoding='utf-8') as f:
        f.write(content)

    print(f"报告已生成: {args.output}")

    # 输出摘要
    summary = data.get('summary', {})
    print(f"\n📊 结果摘要:")
    print(f"  用例总数: {summary.get('total', 0)}")
    print(f"  通过: {summary.get('passed', 0)} | 失败: {summary.get('failed', 0)} | 跳过: {summary.get('skipped', 0)}")
    print(f"  通过率: {summary.get('pass_rate', 0)}% | 耗时: {summary.get('duration', 0)}s")

    failures = data.get('failures', [])
    if failures:
        print(f"\n🔴 关键失败用例 (前 3 条):")
        for f in failures[:3]:
            print(f"  - {f.get('name', '?')}: {f.get('error', '')[:100]}")


if __name__ == '__main__':
    main()