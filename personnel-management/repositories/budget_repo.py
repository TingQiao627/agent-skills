"""
Budget Repository - 预算数据存储层

提供基于JSON文件的预算数据持久化操作
"""

import json
import os
from typing import List, Dict, Any, Optional
from pathlib import Path
from dataclasses import dataclass, asdict
from datetime import datetime


@dataclass
class Budget:
    """预算数据模型"""
    id: str
    name: str
    amount: float
    department_id: Optional[str] = None
    parent_id: Optional[str] = None  # 父级预算ID，用于层级结构
    level: int = 0  # 层级深度，0为根级
    fiscal_year: Optional[str] = None
    category: Optional[str] = None
    created_at: Optional[str] = None
    updated_at: Optional[str] = None
    metadata: Optional[Dict[str, Any]] = None

    def to_dict(self) -> Dict[str, Any]:
        """转换为字典"""
        data = asdict(self)
        if data.get('created_at') is None:
            data['created_at'] = datetime.now().isoformat()
        if data.get('updated_at') is None:
            data['updated_at'] = datetime.now().isoformat()
        return data

    @classmethod
    def from_dict(cls, data: Dict[str, Any]) -> 'Budget':
        """从字典创建实例"""
        return cls(**data)


class BudgetRepository:
    """
    预算仓库类
    
    提供预算数据的JSON文件持久化存储，支持：
    - JSON文件读写
    - CRUD操作（创建、读取、更新、删除）
    - 按层级查询（树形结构查询）
    """

    def __init__(self, file_path: str = "data/budgets.json"):
        """
        初始化预算仓库
        
        Args:
            file_path: JSON数据文件路径，默认为 data/budgets.json
        """
        self.file_path = Path(file_path)
        self._ensure_data_file()

    def _ensure_data_file(self) -> None:
        """确保数据文件存在，如不存在则创建空文件"""
        self.file_path.parent.mkdir(parents=True, exist_ok=True)
        if not self.file_path.exists():
            self._write_data([])

    def _read_data(self) -> List[Dict[str, Any]]:
        """
        读取JSON文件数据
        
        Returns:
            预算数据列表
        """
        try:
            with open(self.file_path, 'r', encoding='utf-8') as f:
                return json.load(f)
        except (json.JSONDecodeError, FileNotFoundError):
            return []

    def _write_data(self, data: List[Dict[str, Any]]) -> None:
        """
        写入JSON文件数据
        
        Args:
            data: 预算数据列表
        """
        with open(self.file_path, 'w', encoding='utf-8') as f:
            json.dump(data, f, ensure_ascii=False, indent=2)

    # ==================== CRUD 操作 ====================

    def create(self, budget: Budget) -> Budget:
        """
        创建新预算记录
        
        Args:
            budget: 预算对象
            
        Returns:
            创建后的预算对象（包含时间戳）
        """
        data = self._read_data()
        
        # 设置时间戳
        budget_dict = budget.to_dict()
        budget_dict['created_at'] = budget_dict.get('created_at') or datetime.now().isoformat()
        budget_dict['updated_at'] = datetime.now().isoformat()
        
        data.append(budget_dict)
        self._write_data(data)
        
        return Budget.from_dict(budget_dict)

    def get_by_id(self, budget_id: str) -> Optional[Budget]:
        """
        根据ID获取预算记录
        
        Args:
            budget_id: 预算ID
            
        Returns:
            预算对象，如不存在返回None
        """
        data = self._read_data()
        for item in data:
            if item.get('id') == budget_id:
                return Budget.from_dict(item)
        return None

    def get_all(self) -> List[Budget]:
        """
        获取所有预算记录
        
        Returns:
            预算对象列表
        """
        data = self._read_data()
        return [Budget.from_dict(item) for item in data]

    def update(self, budget_id: str, updates: Dict[str, Any]) -> Optional[Budget]:
        """
        更新预算记录
        
        Args:
            budget_id: 预算ID
            updates: 更新字段字典
            
        Returns:
            更新后的预算对象，如不存在返回None
        """
        data = self._read_data()
        
        for i, item in enumerate(data):
            if item.get('id') == budget_id:
                # 合并更新数据
                item.update(updates)
                item['updated_at'] = datetime.now().isoformat()
                data[i] = item
                self._write_data(data)
                return Budget.from_dict(item)
        
        return None

    def delete(self, budget_id: str) -> bool:
        """
        删除预算记录
        
        Args:
            budget_id: 预算ID
            
        Returns:
            是否成功删除
        """
        data = self._read_data()
        original_length = len(data)
        
        data = [item for item in data if item.get('id') != budget_id]
        
        if len(data) < original_length:
            self._write_data(data)
            return True
        
        return False

    def delete_by_parent_id(self, parent_id: str) -> int:
        """
        删除指定父级下的所有子预算
        
        Args:
            parent_id: 父级预算ID
            
        Returns:
            删除的记录数
        """
        data = self._read_data()
        original_length = len(data)
        
        data = [item for item in data if item.get('parent_id') != parent_id]
        deleted_count = original_length - len(data)
        
        if deleted_count > 0:
            self._write_data(data)
        
        return deleted_count

    # ==================== 按层级查询 ====================

    def get_root_budgets(self) -> List[Budget]:
        """
        获取根级预算（level=0，无父级）
        
        Returns:
            根级预算列表
        """
        data = self._read_data()
        return [
            Budget.from_dict(item) 
            for item in data 
            if item.get('level', 0) == 0 or item.get('parent_id') is None
        ]

    def get_children(self, parent_id: str) -> List[Budget]:
        """
        获取指定预算的所有直接子预算
        
        Args:
            parent_id: 父级预算ID
            
        Returns:
            子预算列表
        """
        data = self._read_data()
        return [
            Budget.from_dict(item) 
            for item in data 
            if item.get('parent_id') == parent_id
        ]

    def get_by_level(self, level: int) -> List[Budget]:
        """
        获取指定层级的所有预算
        
        Args:
            level: 层级深度（0为根级）
            
        Returns:
            该层级的预算列表
        """
        data = self._read_data()
        return [
            Budget.from_dict(item) 
            for item in data 
            if item.get('level', 0) == level
        ]

    def get_budget_tree(self, budget_id: str) -> Dict[str, Any]:
        """
        获取预算及其所有子预算的树形结构
        
        Args:
            budget_id: 根预算ID
            
        Returns:
            树形结构字典，包含预算信息和children子节点列表
        """
        budget = self.get_by_id(budget_id)
        if not budget:
            return {}
        
        tree = budget.to_dict()
        tree['children'] = []
        
        children = self.get_children(budget_id)
        for child in children:
            tree['children'].append(self.get_budget_tree(child.id))
        
        return tree

    def get_full_tree(self) -> List[Dict[str, Any]]:
        """
        获取完整的预算树形结构（从根节点开始）
        
        Returns:
            完整树形结构列表
        """
        roots = self.get_root_budgets()
        return [self.get_budget_tree(root.id) for root in roots]

    def get_descendants(self, budget_id: str) -> List[Budget]:
        """
        获取指定预算的所有后代预算（递归获取所有子级）
        
        Args:
            budget_id: 预算ID
            
        Returns:
            所有后代预算列表
        """
        descendants = []
        children = self.get_children(budget_id)
        
        for child in children:
            descendants.append(child)
            descendants.extend(self.get_descendants(child.id))
        
        return descendants

    def get_ancestors(self, budget_id: str) -> List[Budget]:
        """
        获取指定预算的所有祖先预算（从父级到根级）
        
        Args:
            budget_id: 预算ID
            
        Returns:
            祖先预算列表（从直接父级到根级）
        """
        ancestors = []
        budget = self.get_by_id(budget_id)
        
        while budget and budget.parent_id:
            parent = self.get_by_id(budget.parent_id)
            if parent:
                ancestors.append(parent)
                budget = parent
            else:
                break
        
        return ancestors

    def get_siblings(self, budget_id: str) -> List[Budget]:
        """
        获取同级兄弟预算（同一父级下的其他预算）
        
        Args:
            budget_id: 预算ID
            
        Returns:
            兄弟预算列表
        """
        budget = self.get_by_id(budget_id)
        if not budget:
            return []
        
        data = self._read_data()
        return [
            Budget.from_dict(item) 
            for item in data 
            if item.get('parent_id') == budget.parent_id and item.get('id') != budget_id
        ]

    # ==================== 其他查询方法 ====================

    def get_by_department(self, department_id: str) -> List[Budget]:
        """
        获取指定部门的预算
        
        Args:
            department_id: 部门ID
            
        Returns:
            该部门的预算列表
        """
        data = self._read_data()
        return [
            Budget.from_dict(item) 
            for item in data 
            if item.get('department_id') == department_id
        ]

    def get_by_fiscal_year(self, fiscal_year: str) -> List[Budget]:
        """
        获取指定财年的预算
        
        Args:
            fiscal_year: 财年
            
        Returns:
            该财年的预算列表
        """
        data = self._read_data()
        return [
            Budget.from_dict(item) 
            for item in data 
            if item.get('fiscal_year') == fiscal_year
        ]

    def get_by_category(self, category: str) -> List[Budget]:
        """
        获取指定类别的预算
        
        Args:
            category: 预算类别
            
        Returns:
            该类别的预算列表
        """
        data = self._read_data()
        return [
            Budget.from_dict(item) 
            for item in data 
            if item.get('category') == category
        ]

    def search(self, **filters) -> List[Budget]:
        """
        多条件搜索预算
        
        Args:
            **filters: 过滤条件（字段名=值）
            
        Returns:
            匹配的预算列表
        """
        data = self._read_data()
        results = []
        
        for item in data:
            match = True
            for key, value in filters.items():
                if item.get(key) != value:
                    match = False
                    break
            if match:
                results.append(Budget.from_dict(item))
        
        return results

    def count(self) -> int:
        """
        获取预算总数
        
        Returns:
            预算记录总数
        """
        data = self._read_data()
        return len(data)

    def exists(self, budget_id: str) -> bool:
        """
        检查预算是否存在
        
        Args:
            budget_id: 预算ID
            
        Returns:
            是否存在
        """
        return self.get_by_id(budget_id) is not None

    def clear(self) -> None:
        """清空所有预算数据"""
        self._write_data([])