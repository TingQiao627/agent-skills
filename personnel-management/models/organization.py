"""Organization entity class with tree structure support."""

from dataclasses import dataclass, field
from datetime import datetime
from typing import Optional, List


@dataclass
class Organization:
    """Organization entity representing a node in the organizational hierarchy tree.
    
    Attributes:
        id: Unique identifier for the organization.
        name: Display name of the organization.
        code: Unique code identifier for the organization.
        parentId: ID of the parent organization, None for root nodes.
        level: Depth level in the tree (root = 0).
        path: Full path from root to this node (e.g., "/root/parent/child").
        createdAt: Timestamp when the organization was created.
    """
    
    id: str
    name: str
    code: str
    parentId: Optional[str] = None
    level: int = 0
    path: str = ""
    createdAt: datetime = field(default_factory=datetime.utcnow)
    
    # Runtime-only field for tree navigation (not persisted)
    _children: List['Organization'] = field(default_factory=list, repr=False)
    
    @property
    def is_root(self) -> bool:
        """Check if this organization is a root node (no parent)."""
        return self.parentId is None
    
    @property
    def children(self) -> List['Organization']:
        """Get list of child organizations."""
        return self._children
    
    def add_child(self, child: 'Organization') -> None:
        """Add a child organization to this node.
        
        Args:
            child: The child organization to add.
        """
        child.parentId = self.id
        child.level = self.level + 1
        child.path = f"{self.path}/{child.id}" if self.path else f"/{child.id}"
        self._children.append(child)
    
    def remove_child(self, child_id: str) -> bool:
        """Remove a child organization by ID.
        
        Args:
            child_id: ID of the child to remove.
            
        Returns:
            True if child was found and removed, False otherwise.
        """
        for i, child in enumerate(self._children):
            if child.id == child_id:
                self._children.pop(i)
                return True
        return False
    
    def get_child(self, child_id: str) -> Optional['Organization']:
        """Get a child organization by ID.
        
        Args:
            child_id: ID of the child to find.
            
        Returns:
            The child organization if found, None otherwise.
        """
        for child in self._children:
            if child.id == child_id:
                return child
        return None
    
    def find_descendant(self, org_id: str) -> Optional['Organization']:
        """Find a descendant organization by ID (recursive search).
        
        Args:
            org_id: ID of the descendant to find.
            
        Returns:
            The descendant organization if found, None otherwise.
        """
        # Check direct children first
        child = self.get_child(org_id)
        if child:
            return child
        
        # Recursively search in children
        for child in self._children:
            descendant = child.find_descendant(org_id)
            if descendant:
                return descendant
        
        return None
    
    def get_all_descendants(self) -> List['Organization']:
        """Get all descendant organizations (depth-first traversal).
        
        Returns:
            List of all descendant organizations.
        """
        descendants = []
        for child in self._children:
            descendants.append(child)
            descendants.extend(child.get_all_descendants())
        return descendants
    
    def get_ancestors_path(self, all_orgs: dict) -> List['Organization']:
        """Get list of ancestors from root to this node.
        
        Args:
            all_orgs: Dictionary mapping org IDs to Organization instances.
            
        Returns:
            List of ancestor organizations from root to parent.
        """
        ancestors = []
        current_id = self.parentId
        
        while current_id and current_id in all_orgs:
            parent = all_orgs[current_id]
            ancestors.insert(0, parent)
            current_id = parent.parentId
        
        return ancestors
    
    def update_path(self, all_orgs: dict) -> None:
        """Update this node's path and level based on parent.
        
        Args:
            all_orgs: Dictionary mapping org IDs to Organization instances.
        """
        if self.parentId is None:
            self.level = 0
            self.path = f"/{self.id}"
        elif self.parentId in all_orgs:
            parent = all_orgs[self.parentId]
            self.level = parent.level + 1
            self.path = f"{parent.path}/{self.id}"
        else:
            # Parent not found, treat as root-level error case
            self.level = 0
            self.path = f"/{self.id}"
    
    def __str__(self) -> str:
        """String representation showing organization hierarchy."""
        indent = "  " * self.level
        return f"{indent}[{self.code}] {self.name} (id={self.id}, level={self.level})"
    
    def __repr__(self) -> str:
        return (
            f"Organization(id={self.id!r}, name={self.name!r}, "
            f"code={self.code!r}, parentId={self.parentId!r}, "
            f"level={self.level}, path={self.path!r})"
        )
    
    def to_dict(self) -> dict:
        """Convert organization to dictionary for serialization.
        
        Returns:
            Dictionary representation of the organization.
        """
        return {
            'id': self.id,
            'name': self.name,
            'code': self.code,
            'parentId': self.parentId,
            'level': self.level,
            'path': self.path,
            'createdAt': self.createdAt.isoformat() if self.createdAt else None,
        }
    
    @classmethod
    def from_dict(cls, data: dict) -> 'Organization':
        """Create an Organization instance from a dictionary.
        
        Args:
            data: Dictionary containing organization data.
            
        Returns:
            Organization instance.
        """
        created_at = data.get('createdAt')
        if isinstance(created_at, str):
            created_at = datetime.fromisoformat(created_at)
        elif created_at is None:
            created_at = datetime.utcnow()
        
        return cls(
            id=data['id'],
            name=data['name'],
            code=data['code'],
            parentId=data.get('parentId'),
            level=data.get('level', 0),
            path=data.get('path', ''),
            createdAt=created_at,
        )