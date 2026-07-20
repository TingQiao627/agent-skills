#!/usr/bin/env python3
"""
测试结果解析器 (Test Result Parser)
支持格式：JUnit XML、Jest JSON、Vitest JSON
输出统一 JSON 结构到 stdout 或指定文件
"""

import argparse
import json
import os
import sys
import xml.etree.ElementTree as ET
from datetime import datetime
from pathlib import Path


class ParsedResult:
    """统一解析结果数据结构"""
    def __init__(self):
        self.project_name = ""
        self.framework = "unknown"
        self.command = ""
        self.timestamp = datetime.now().strftime("%Y-%m-%d %H:%M:%S")
        self.summary = {
            "total": 0,
            "passed": 0,
            "failed": 0,
            "skipped": 0,
            "pass_rate": 0.0,
            "duration": 0.0,
            "conclusion": "pass"
        }
        self.failures = []
        self.test_cases = []
        self.coverage = None
        self.artifacts = []
        self.errors = []

    def to_dict(self):
        return {
            "project_name": self.project_name,
            "framework": self.framework,
            "command": self.command,
            "timestamp": self.timestamp,
            "summary": self.summary,
            "failures": self.failures,
            "test_cases": self.test_cases,
            "coverage": self.coverage,
            "artifacts": self.artifacts,
            "errors": self.errors
        }


class BaseParser:
    """解析器基类 (插件式设计)"""
    def parse(self, file_path: str) -> ParsedResult:
        raise NotImplementedError

    def detect(self, file_path: str) -> bool:
        """检测此解析器是否适用于该文件"""
        raise NotImplementedError


class JUnitXMLParser(BaseParser):
    """JUnit XML 格式解析器"""

    def detect(self, file_path: str) -> bool:
        try:
            with open(file_path, 'r', encoding='utf-8') as f:
                first_line = f.readline(200)
                return '<testsuite' in first_line or '<testsuites' in first_line
        except Exception:
            return False

    def parse(self, file_path: str) -> ParsedResult:
        result = ParsedResult()
        result.framework = "junit"
        result.artifacts.append(file_path)

        try:
            tree = ET.parse(file_path)
            root = tree.getroot()

            total = 0
            passed = 0
            failed = 0
            skipped = 0
            duration = 0.0

            test_suites = root.findall('.//testsuite')

            if not test_suites and root.tag == 'testsuite':
                test_suites = [root]

            for suite in test_suites:
                suite_name = suite.get('name', '')
                total += int(suite.get('tests', 0))
                failed += int(suite.get('failures', 0)) + int(suite.get('errors', 0))
                skipped += int(suite.get('skipped', 0))
                duration += float(suite.get('time', 0))

                for testcase in suite.findall('testcase'):
                    tc_name = testcase.get('name', '')
                    tc_class = testcase.get('classname', '')
                    tc_file = testcase.get('file', '')
                    tc_time = float(testcase.get('time', 0))

                    failure_el = testcase.find('failure')
                    error_el = testcase.find('error')
                    skipped_el = testcase.find('skipped')

                    status = "passed"
                    if failure_el is not None or error_el is not None:
                        status = "failed"
                        error_msg = (failure_el.get('message', '') if failure_el is not None else
                                     error_el.get('message', '') if error_el is not None else '')
                        error_text = (failure_el.text or '') if failure_el is not None else (error_el.text or '') if error_el is not None else ''

                        result.failures.append({
                            "name": f"{tc_class}.{tc_name}" if tc_class else tc_name,
                            "file": tc_file or suite_name,
                            "error": error_msg or error_text[:200],
                            "stack": _truncate_stack(error_text)
                        })
                    elif skipped_el is not None:
                        status = "skipped"

                    result.test_cases.append({
                        "name": tc_name,
                        "classname": tc_class,
                        "file": tc_file or suite_name,
                        "status": status,
                        "duration": round(tc_time * 1000, 2)
                    })

            passed = total - failed - skipped
            result.summary = {
                "total": total,
                "passed": passed,
                "failed": failed,
                "skipped": skipped,
                "pass_rate": round(passed / total * 100, 2) if total > 0 else 0.0,
                "duration": round(duration, 2),
                "conclusion": "pass" if failed == 0 else "fail"
            }

        except ET.ParseError as e:
            result.errors.append(f"JUnit XML 解析错误: {e}")
            result.summary["conclusion"] = "error"
        except Exception as e:
            result.errors.append(f"解析异常: {e}")
            result.summary["conclusion"] = "error"

        return result


class JestJSONParser(BaseParser):
    """Jest JSON 格式解析器"""

    def detect(self, file_path: str) -> bool:
        try:
            with open(file_path, 'r', encoding='utf-8') as f:
                data = json.load(f)
                return 'testResults' in data and 'numTotalTests' in data
        except Exception:
            return False

    def parse(self, file_path: str) -> ParsedResult:
        result = ParsedResult()
        result.framework = "jest"
        result.artifacts.append(file_path)

        try:
            with open(file_path, 'r', encoding='utf-8') as f:
                data = json.load(f)

            total = data.get('numTotalTests', 0)
            passed = data.get('numPassedTests', 0)
            failed = data.get('numFailedTests', 0)
            skipped = data.get('numPendingTests', 0)

            # 计算耗时
            duration = 0.0
            start_time = data.get('startTime')
            if start_time and isinstance(start_time, (int, float)):
                duration = round((datetime.now().timestamp() * 1000 - start_time) / 1000, 2)

            for test_result in data.get('testResults', []):
                file_path_item = test_result.get('testFilePath', '')
                for assertion in test_result.get('assertionResults', []):
                    tc_name = ' > '.join(assertion.get('ancestorTitles', [])) + ' > ' + assertion.get('title', '')
                    tc_status = assertion.get('status', 'unknown')
                    tc_duration = assertion.get('duration', 0)

                    result.test_cases.append({
                        "name": tc_name,
                        "file": file_path_item,
                        "status": tc_status,
                        "duration": tc_duration
                    })

                    if tc_status == 'failed':
                        failure_msgs = assertion.get('failureMessages', [])
                        error_msg = failure_msgs[0] if failure_msgs else ''
                        result.failures.append({
                            "name": tc_name,
                            "file": file_path_item,
                            "error": error_msg[:200],
                            "stack": _truncate_stack(error_msg)
                        })

            result.summary = {
                "total": total,
                "passed": passed,
                "failed": failed,
                "skipped": skipped,
                "pass_rate": round(passed / total * 100, 2) if total > 0 else 0.0,
                "duration": round(duration, 2),
                "conclusion": "pass" if failed == 0 else "fail"
            }

        except json.JSONDecodeError as e:
            result.errors.append(f"Jest JSON 解析错误: {e}")
            result.summary["conclusion"] = "error"
        except Exception as e:
            result.errors.append(f"解析异常: {e}")
            result.summary["conclusion"] = "error"

        return result


class VitestJSONParser(BaseParser):
    """Vitest JSON 格式解析器"""

    def detect(self, file_path: str) -> bool:
        try:
            with open(file_path, 'r', encoding='utf-8') as f:
                data = json.load(f)
                return 'testResults' in data and 'numTotalTestSuites' in data
        except Exception:
            return False

    def parse(self, file_path: str) -> ParsedResult:
        result = ParsedResult()
        result.framework = "vitest"
        result.artifacts.append(file_path)

        try:
            with open(file_path, 'r', encoding='utf-8') as f:
                data = json.load(f)

            total = data.get('numTotalTests', 0)
            passed = data.get('numPassedTests', 0)
            failed = data.get('numFailedTests', 0)
            skipped = data.get('numPendingTests', 0) + data.get('numTodoTests', 0)

            duration = 0.0
            start_time = data.get('startTime')
            end_time = data.get('endTime')
            if start_time and end_time and isinstance(start_time, (int, float)) and isinstance(end_time, (int, float)):
                duration = round((end_time - start_time) / 1000, 2)

            for test_result in data.get('testResults', []):
                file_path_item = test_result.get('name', '')
                for assertion in test_result.get('assertionResults', []):
                    tc_name = ' > '.join(assertion.get('ancestorTitles', [])) + ' > ' + assertion.get('title', '')
                    tc_status = assertion.get('status', 'unknown')
                    tc_duration = assertion.get('duration', 0)

                    result.test_cases.append({
                        "name": tc_name,
                        "file": file_path_item,
                        "status": tc_status,
                        "duration": tc_duration
                    })

                    if tc_status == 'failed':
                        failure_msgs = assertion.get('failureMessages', [])
                        error_msg = failure_msgs[0] if failure_msgs else ''
                        result.failures.append({
                            "name": tc_name,
                            "file": file_path_item,
                            "error": error_msg[:200],
                            "stack": _truncate_stack(error_msg)
                        })

            result.summary = {
                "total": total,
                "passed": passed,
                "failed": failed,
                "skipped": skipped,
                "pass_rate": round(passed / total * 100, 2) if total > 0 else 0.0,
                "duration": round(duration, 2),
                "conclusion": "pass" if failed == 0 else "fail"
            }

        except json.JSONDecodeError as e:
            result.errors.append(f"Vitest JSON 解析错误: {e}")
            result.summary["conclusion"] = "error"
        except Exception as e:
            result.errors.append(f"解析异常: {e}")
            result.summary["conclusion"] = "error"

        return result


def _truncate_stack(stack: str, max_lines: int = 20) -> str:
    """截断堆栈信息至可读长度"""
    if not stack:
        return ""
    lines = stack.strip().split('\n')
    if len(lines) <= max_lines:
        return stack.strip()
    return '\n'.join(lines[:max_lines]) + f"\n... (共 {len(lines)} 行，已截断)"


def parse_coverage(coverage_file: str) -> dict:
    """解析覆盖率数据"""
    if not coverage_file or not os.path.exists(coverage_file):
        return None

    try:
        with open(coverage_file, 'r', encoding='utf-8') as f:
            data = json.load(f)

        # Jest/Vitest coverage-summary.json 格式
        if 'total' in data:
            total = data['total']
            return {
                "statements": total.get('statements', {}).get('pct', 0),
                "branches": total.get('branches', {}).get('pct', 0),
                "functions": total.get('functions', {}).get('pct', 0),
                "lines": total.get('lines', {}).get('pct', 0),
                "low_files": _extract_low_coverage_files(data)
            }
    except (json.JSONDecodeError, Exception):
        pass

    # 尝试 cobertura XML 格式
    try:
        tree = ET.parse(coverage_file)
        root = tree.getroot()
        if root.tag in ('coverage', 'report'):
            line_rate = float(root.get('line-rate', 0)) * 100
            branch_rate = float(root.get('branch-rate', 0)) * 100
            return {
                "statements": round(line_rate, 1),
                "branches": round(branch_rate, 1),
                "functions": 0,
                "lines": round(line_rate, 1),
                "low_files": []
            }
    except Exception:
        pass

    return None


def _extract_low_coverage_files(data: dict, threshold: float = 50.0) -> list:
    """提取低于阈值的覆盖率文件"""
    low_files = []
    for file_path, metrics in data.items():
        if file_path == 'total':
            continue
        pct = metrics.get('lines', {}).get('pct', 100)
        if pct < threshold:
            low_files.append({"file": file_path, "rate": pct})
    return low_files


def auto_detect_format(file_path: str) -> str:
    """自动检测结果文件格式"""
    parsers = [
        ('junit', JUnitXMLParser()),
        ('jest', JestJSONParser()),
        ('vitest', VitestJSONParser()),
    ]
    for fmt, parser in parsers:
        if parser.detect(file_path):
            return fmt
    return 'unknown'


def get_parser(fmt: str) -> BaseParser:
    """获取指定格式的解析器"""
    parsers = {
        'junit': JUnitXMLParser(),
        'jest': JestJSONParser(),
        'vitest': VitestJSONParser(),
    }
    return parsers.get(fmt)


def main():
    parser = argparse.ArgumentParser(description='测试结果解析器')
    parser.add_argument('--result-file', required=True, help='结果文件路径')
    parser.add_argument('--format', default='auto',
                        choices=['auto', 'junit', 'jest', 'vitest'],
                        help='结果格式 (默认自动检测)')
    parser.add_argument('--coverage-file', default=None, help='覆盖率文件路径')
    parser.add_argument('--output', default=None, help='输出 JSON 文件路径')
    parser.add_argument('--project-name', default='', help='项目名称')
    parser.add_argument('--command', default='', help='测试命令')

    args = parser.parse_args()

    if not os.path.exists(args.result_file):
        print(f"错误: 结果文件不存在: {args.result_file}", file=sys.stderr)
        sys.exit(1)

    # 检测格式
    fmt = args.format
    if fmt == 'auto':
        fmt = auto_detect_format(args.result_file)
        if fmt == 'unknown':
            print(f"错误: 无法自动识别结果文件格式: {args.result_file}", file=sys.stderr)
            sys.exit(1)

    # 解析结果
    result_parser = get_parser(fmt)
    if result_parser is None:
        print(f"错误: 不支持的格式: {fmt}", file=sys.stderr)
        sys.exit(1)

    result = result_parser.parse(args.result_file)

    result.project_name = args.project_name or Path(os.getcwd()).name
    result.command = args.command

    # 解析覆盖率
    if args.coverage_file:
        result.coverage = parse_coverage(args.coverage_file)

    output = result.to_dict()

    if args.output:
        with open(args.output, 'w', encoding='utf-8') as f:
            json.dump(output, f, ensure_ascii=False, indent=2)
        print(f"解析结果已写入: {args.output}")
    else:
        print(json.dumps(output, ensure_ascii=False, indent=2))


if __name__ == '__main__':
    main()