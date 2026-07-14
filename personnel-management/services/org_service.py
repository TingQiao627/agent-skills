"""Organization service for managing organizational hierarchy."""

from typing import List, Optional, Dict
from datetime import datetime
import uuid

from ..models.organization import Organization


class OrganizationService:
    """Service for managing organizations with tree structure support.
    
    Provides CRUD operations, tree building, and hierarchy depth validation.
    Maximum allowed hierarchy depth is 5 levels.
    """
    
    MAX_HIERARCHY_DEPTH = 5
    
    def __init__(self):
        """Initialize the organization service."""
        self._organizations: Dict[str, Organization] = {}
    
    def create(self, name: str, parent_id: Optional[str] = None, 
               description: Optional[str] = None) -> Organization:
        """Create a new organization.
        
        Args:
            name: Organization name.
            parent_id: Optional parent organization ID.
            description: Optional description.
            
        Returns:
            Created organization instance.
            
        Raises:
            ValueError: If parent not found or hierarchy depth exceeds limit.
        """
        # Validate parent exists if provided
        if parent_id and parent_id not in self._organizations:
            raise ValueError(f"Parent organization with id '{parent_id}' not found")
        
        # Validate hierarchy depth
        if parent_id:
            parent = self._organizations[parent_id]
            parent_depth = self._get_depth(parent)
            if parent_depth >= self.MAX_HIERARCHY_DEPTH:
                raise ValueError(
                    f"Hierarchy depth cannot exceed {self.MAX_HIERARCHY_DEPTH} levels"
                )
        
        org = Organization(
            id=str(uuid.uuid4()),
            name=name,
            parent_id=parent_id,
            description=description
        )
        
        self._organizations[org.id] = org
        return org
    
    def get(self, org_id: str) -> Optional[Organization]:
        """Get organization by ID.
        
        Args:
            org_id: Organization ID.
            
        Returns:
            Organization instance or None if not found.
        """
        return self._organizations.get(org_id)
    
    def get_by_name(self, name: str) -> Optional[Organization]:
        """Get organization by name.
        
        Args:
            name: Organization name.
            
        Returns:
            Organization instance or None if not found.
        """
        for org in self._organizations.values():
            if org.name == name:
                return org
        return None
    
    def get_all(self) -> List[Organization]:
        """Get all organizations.
        
        Returns:
            List of all organizations.
        """
        return list(self._organizations.values())
    
    def update(self, org_id: str, name: Optional[str] = None,
               description: Optional[str] = None,
               parent_id: Optional[str] = None) -> Organization:
        """Update an organization.
        
        Args:
            org_id: Organization ID.
            name: New name (optional).
            description: New description (optional).
            parent_id: New parent ID (optional).
            
        Returns:
            Updated organization instance.
            
        Raises:
            ValueError: If organization not found, parent not found,
                       or hierarchy depth exceeds limit.
        """
        if org_id not in self._organizations:
            raise ValueError(f"Organization with id '{org_id}' not found")
        
        org = self._organizations[org_id]
        
        # Validate new parent if provided
        if parent_id is not None and parent_id != org.parent_id:
            if parent_id and parent_id not in self._organizations:
                raise ValueError(f"Parent organization with id '{parent_id}' not found")
            
            # Check for circular reference
            if parent_id and self._is_descendant(org_id, parent_id):
                raise ValueError("Cannot set parent: circular reference detected")
            
            # Check depth limit
            if parent_id:
                new_parent = self._organizations[parent_id]
                new_parent_depth = self._get_depth(new_parent)
                org_depth = self._get_subtree_depth(org)
                if new_parent_depth + org_depth > self.MAX_HIERARCHY_DEPTH:
                    raise ValueError(
                        f"Hierarchy depth cannot exceed {self.MAX_HIERARCHY_DEPTH} levels"
                    )
        
        if name is not None:
            org.name = name
        if description is not None:
            org.description = description
        if parent_id is not None:
            org.parent_id = parent_id if parent_id else None
        
        org.update_timestamp()
        return org
    
    def delete(self, org_id: str) -> bool:
        """Delete an organization.
        
        Args:
            org_id: Organization ID.
            
        Returns:
            True if deleted, False if not found.
            
        Raises:
            ValueError: If organization has children.
        """
        if org_id not in self._organizations:
            return False
        
        # Check for children
        children = self.get_children(org_id)
        if children:
            raise ValueError(
                f"Cannot delete organization with children. "
                f"Found {len(children)} child organizations."
            )
        
        del self._organizations[org_id]
        return True
    
    def get_children(self, org_id: str) -> List[Organization]:
        """Get direct children of an organization.
        
        Args:
            org_id: Organization ID.
            
        Returns:
            List of child organizations.
        """
        return [org for org in self._organizations.values() 
                if org.parent_id == org_id]
    
    def get_descendants(self, org_id: str) -> List[Organization]:
        """Get all descendants of an organization.
        
        Args:
            org_id: Organization ID.
            
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
        """Get all ancestors of an organization.
        
        Args:
            org_id: Organization ID.
            
        Returns:
            List of ancestor organizations from parent to root.
        """
        ancestors = []
        org = self._organizations.get(org_id)
        
        while org and org.parent_id:
            parent = self._organizations.get(org.parent_id)
            if parent:
                ancestors.append(parent)
                org = parent
            else:
                break
        
        return ancestors
    
    def build_tree(self) -> List[Organization]:
        """Build organization tree from all organizations.
        
        Returns:
            List of root organizations with populated children.
        """
        # Clear existing children references
        for org in self._organizations.values():
            org.children = []
        
        # Build parent-child relationships
        root_orgs = []
        for org in self._organizations.values():
            if org.parent_id is None:
                root_orgs.append(org)
            else:
                parent = self._organizations.get(org.parent_id)
                if parent:
                    parent.children.append(org)
        
        return root_orgs
    
    def get_depth(self, org_id: str) -> int:
        """Get the depth of an organization in the hierarchy.
        
        Args:
            org_id: Organization ID.
            
        Returns:
            Depth level (1 for root, increases for each level).
            
        Raises:
            ValueError: If organization not found.
        """
        if org_id not in self._organizations:
            raise ValueError(f"Organization with id '{org_id}' not found")
        
        return self._get_depth(self._organizations[org_id])
    
    def validate_hierarchy_depth(self) -> bool:
        """Validate that hierarchy depth does not exceed maximum.
        
        Returns:
            True if all organizations are within depth limit.
        """
        for org in self._organizations.values():
            depth = self._get_depth(org)
            if depth > self.MAX_HIERARCHY_DEPTH:
                return False
        return True
    
    def _get_depth(self, org: Organization) -> int:
        """Calculate the depth of an organization.
        
        Args:
            org: Organization instance.
            
        Returns:
            Depth level (1 for root).
        """
        depth = 1
        current = org
        
        while current.parent_id:
            parent = self._organizations.get(current.parent_id)
            if not parent:
                break
            depth += 1
            current = parent
        
        return depth
    
    def _get_subtree_depth(self, org: Organization) -> int:
        """Calculate the depth of subtree rooted at organization.
        
        Args:
            org: Organization instance.
            
        Returns:
            Maximum depth of subtree.
        """
        children = self.get_children(org.id)
        if not children:
            return 1
        
        return 1 + max(self._get_subtree_depth(child) for child in children)
    
    def _is_descendant(self, ancestor_id: str, potential_descendant_id: str) -> bool:
        """Check if an organization is a descendant of another.
        
        Args:
            ancestor_id: Potential ancestor ID.
            potential_descendant_id: Potential descendant ID.
            
        Returns:
            True if potential_descendant is a descendant of ancestor.
        """
        org = self._organizations.get(potential_descendant_id)
        
        while org and org.parent_id:
            if org.parent_id == ancestor_id:
                return True
            org = self._organizations.get(org.parent_id)
        
        return False