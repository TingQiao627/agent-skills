"""Organization repository for JSON file-based CRUD operations and tree queries."""

import json
import os
from typing import Optional, List, Dict, Any
from pathlib import Path
from datetime import datetime

from models.organization import Organization


class OrganizationRepository:
    """Repository for managing Organization entities stored in JSON files.
    
    Provides CRUD operations and tree structure queries for organizations.
    
    Attributes:
        file_path: Path to the JSON file storing organizations.
    """
    
    def __init__(self, file_path: str = "data/organizations.json"):
        """Initialize the repository.
        
        Args:
            file_path: Path to the JSON file. Defaults to 'data/organizations.json'.
        """
        self.file_path = Path(file_path)
        self._ensure_file_exists()
    
    def _ensure_file_exists(self):
        """Ensure the JSON file and its directory exist."""
        self.file_path.parent.mkdir(parents=True, exist_ok=True)
        if not self.file_path.exists():
            self._write_data([])
    
    def _read_data(self) -> List[Dict[str, Any]]:
        """Read organization data from JSON file.
        
        Returns:
            List of organization dictionaries.
        """
        try:
            with open(self.file_path, 'r', encoding='utf-8') as f:
                return json.load(f)
        except (json.JSONDecodeError, FileNotFoundError):
            return []
    
    def _write_data(self, data: List[Dict[str, Any]]):
        """Write organization data to JSON file.
        
        Args:
            data: List of organization dictionaries to write.
        """
        with open(self.file_path, 'w', encoding='utf-8') as f:
            json.dump(data, f, indent=2, ensure_ascii=False)
    
    # ==================== CRUD Operations ====================
    
    def create(self, org: Organization) -> Organization:
        """Create a new organization.
        
        Args:
            org: Organization to create.
            
        Returns:
            Created organization with generated ID.
            
        Raises:
            ValueError: If organization name is empty.
        """
        if not org.name:
            raise ValueError("Organization name is required")
        
        data = self._read_data()
        
        # Check for duplicate name at same level
        for existing in data:
            if existing["name"] == org.name and existing["parent_id"] == org.parent_id:
                raise ValueError(f"Organization with name '{org.name}' already exists at this level")
        
        org.created_at = datetime.now().isoformat()
        org.updated_at = datetime.now().isoformat()
        data.append(org.to_dict())
        self._write_data(data)
        return org
    
    def get_by_id(self, org_id: str) -> Optional[Organization]:
        """Get an organization by ID.
        
        Args:
            org_id: ID of the organization to retrieve.
            
        Returns:
            Organization if found, None otherwise.
        """
        data = self._read_data()
        for item in data:
            if item["id"] == org_id:
                return Organization.from_dict(item)
        return None
    
    def get_all(self) -> List[Organization]:
        """Get all organizations.
        
        Returns:
            List of all organizations.
        """
        data = self._read_data()
        return [Organization.from_dict(item) for item in data]
    
    def update(self, org_id: str, updates: Dict[str, Any]) -> Optional[Organization]:
        """Update an organization.
        
        Args:
            org_id: ID of the organization to update.
            updates: Dictionary of fields to update.
            
        Returns:
            Updated organization if found, None otherwise.
            
        Raises:
            ValueError: If trying to set parent_id to itself or a descendant.
        """
        data = self._read_data()
        
        for i, item in enumerate(data):
            if item["id"] == org_id:
                # Check for circular reference
                if "parent_id" in updates:
                    new_parent_id = updates["parent_id"]
                    if new_parent_id == org_id:
                        raise ValueError("Cannot set parent_id to self")
                    
                    # Check if new parent is a descendant
                    if new_parent_id and self._is_descendant(org_id, new_parent_id, data):
                        raise ValueError("Cannot set parent_id to a descendant")
                
                # Apply updates
                for key, value in updates.items():
                    if key in item and key != "id":
                        item[key] = value
                
                item["updated_at"] = datetime.now().isoformat()
                data[i] = item
                self._write_data(data)
                return Organization.from_dict(item)
        
        return None
    
    def delete(self, org_id: str) -> bool:
        """Delete an organization.
        
        Args:
            org_id: ID of the organization to delete.
            
        Returns:
            True if deleted, False if not found.
            
        Raises:
            ValueError: If organization has children.
        """
        data = self._read_data()
        
        # Check if organization has children
        for item in data:
            if item["parent_id"] == org_id:
                raise ValueError("Cannot delete organization with children")
        
        for i, item in enumerate(data):
            if item["id"] == org_id:
                data.pop(i)
                self._write_data(data)
                return True
        
        return False
    
    # ==================== Tree Structure Queries ====================
    
    def get_children(self, parent_id: str) -> List[Organization]:
        """Get all direct children of an organization.
        
        Args:
            parent_id: ID of the parent organization.
            
        Returns:
            List of child organizations.
        """
        data = self._read_data()
        return [
            Organization.from_dict(item) 
            for item in data 
            if item["parent_id"] == parent_id
        ]
    
    def get_root_organizations(self) -> List[Organization]:
        """Get all root organizations (those without parents).
        
        Returns:
            List of root organizations.
        """
        data = self._read_data()
        return [
            Organization.from_dict(item) 
            for item in data 
            if item["parent_id"] is None
        ]
    
    def get_descendants(self, org_id: str) -> List[Organization]:
        """Get all descendants of an organization (children, grandchildren, etc.).
        
        Args:
            org_id: ID of the organization.
            
        Returns:
            List of all descendant organizations.
        """
        descendants = []
        children = self.get_children(org_id)
        
        for child in children:
            descendants.append(child)
            descendants.extend(self.get_descendants(child.id))
        
        return descendants
    
    def get_ancestors(self, org_id: str) -> List[Organization]:
        """Get all ancestors of an organization (parent, grandparent, etc.).
        
        Args:
            org_id: ID of the organization.
            
        Returns:
            List of all ancestor organizations from immediate parent to root.
        """
        ancestors = []
        org = self.get_by_id(org_id)
        
        while org and org.parent_id:
            parent = self.get_by_id(org.parent_id)
            if parent:
                ancestors.append(parent)
                org = parent
            else:
                break
        
        return ancestors
    
    def get_tree(self, org_id: Optional[str] = None) -> Dict[str, Any]:
        """Get organization tree structure.
        
        Args:
            org_id: ID of the root organization. If None, returns entire tree.
            
        Returns:
            Nested dictionary representing the organization tree.
        """
        if org_id:
            org = self.get_by_id(org_id)
            if not org:
                return {}
            return self._build_tree(org)
        else:
            roots = self.get_root_organizations()
            return {"roots": [self._build_tree(root) for root in roots]}
    
    def _build_tree(self, org: Organization) -> Dict[str, Any]:
        """Build a tree node with its children.
        
        Args:
            org: Organization to build tree from.
            
        Returns:
            Dictionary with organization data and children.
        """
        children = self.get_children(org.id)
        return {
            "id": org.id,
            "name": org.name,
            "parent_id": org.parent_id,
            "description": org.description,
            "created_at": org.created_at,
            "updated_at": org.updated_at,
            "children": [self._build_tree(child) for child in children]
        }
    
    def _is_descendant(self, ancestor_id: str, potential_descendant_id: str, data: List[Dict]) -> bool:
        """Check if an ID is a descendant of another ID.
        
        Args:
            ancestor_id: ID of the potential ancestor.
            potential_descendant_id: ID of the potential descendant.
            data: List of all organization data.
            
        Returns:
            True if potential_descendant_id is a descendant of ancestor_id.
        """
        # Build a lookup dict
        lookup = {item["id"]: item.get("parent_id") for item in data}
        
        # Traverse up from potential descendant
        current_id = potential_descendant_id
        while current_id:
            current_id = lookup.get(current_id)
            if current_id == ancestor_id:
                return True
        
        return False
    
    def get_path_to_root(self, org_id: str) -> List[Organization]:
        """Get the path from an organization to root.
        
        Args:
            org_id: ID of the organization.
            
        Returns:
            List of organizations from the given org to root (inclusive).
        """
        path = []
        org = self.get_by_id(org_id)
        
        if org:
            path.append(org)
            path.extend(self.get_ancestors(org_id))
        
        return path
    
    def count_descendants(self, org_id: str) -> int:
        """Count total number of descendants.
        
        Args:
            org_id: ID of the organization.
            
        Returns:
            Number of descendants.
        """
        return len(self.get_descendants(org_id))
    
    def move_organization(self, org_id: str, new_parent_id: Optional[str]) -> Optional[Organization]:
        """Move an organization to a new parent.
        
        Args:
            org_id: ID of the organization to move.
            new_parent_id: ID of the new parent, or None for root.
            
        Returns:
            Updated organization if successful, None otherwise.
            
        Raises:
            ValueError: If move would create a circular reference.
        """
        data = self._read_data()
        
        # Check for circular reference
        if new_parent_id:
            if new_parent_id == org_id:
                raise ValueError("Cannot move organization to itself")
            
            # Check if new_parent_id is a descendant
            if self._is_descendant(org_id, new_parent_id, data):
                raise ValueError("Cannot move organization to one of its descendants")
        
        return self.update(org_id, {"parent_id": new_parent_id})