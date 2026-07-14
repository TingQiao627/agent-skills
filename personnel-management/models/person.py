"""Person entity model with validation logic."""

from dataclasses import dataclass, field
from datetime import datetime
from typing import Optional
from uuid import uuid4
import re

from .enums import PersonStatus


class PersonValidationError(Exception):
    """Exception raised when person data validation fails."""
    pass


@dataclass
class Person:
    """Person entity representing an individual in the organization.
    
    Attributes:
        id: Unique identifier for the person.
        name: Full name of the person.
        employeeNo: Employee number.
        organizationId: ID of the organization this person belongs to.
        status: Current status of the person.
        level: Optional level for budget association.
        createdAt: Timestamp when the record was created.
        updatedAt: Timestamp when the record was last updated.
    """
    
    id: str = field(default_factory=lambda: str(uuid4()))
    name: str = ""
    employeeNo: str = ""
    organizationId: str = ""
    status: PersonStatus = PersonStatus.ACTIVE
    level: Optional[int] = None
    createdAt: datetime = field(default_factory=datetime.now)
    updatedAt: datetime = field(default_factory=datetime.now)
    
    def __post_init__(self):
        """Validate all fields after initialization."""
        self._validate_id()
        self._validate_name()
        self._validate_employee_no()
        self._validate_organization_id()
        self._validate_status()
        self._validate_level()
        self._validate_timestamps()
    
    def _validate_id(self) -> None:
        """Validate person ID - must be valid UUID."""
        if not self.id:
            raise PersonValidationError("Person id cannot be empty")
        try:
            from uuid import UUID
            UUID(self.id)
        except ValueError:
            raise PersonValidationError(f"Invalid UUID format for person id: {self.id}")
    
    def _validate_name(self) -> None:
        """Validate name - required, max 100 characters."""
        if not self.name or not self.name.strip():
            raise PersonValidationError("Person name cannot be empty")
        if len(self.name) > 100:
            raise PersonValidationError(f"Person name exceeds max length (100): {len(self.name)}")
    
    def _validate_employee_no(self) -> None:
        """Validate employee number - required, alphanumeric/hyphen/underscore, max 50 chars."""
        if not self.employeeNo or not self.employeeNo.strip():
            raise PersonValidationError("Employee number cannot be empty")
        if len(self.employeeNo) > 50:
            raise PersonValidationError(f"Employee number exceeds max length (50): {len(self.employeeNo)}")
        if not re.match(r'^[a-zA-Z0-9_-]+$', self.employeeNo):
            raise PersonValidationError(
                f"Employee number contains invalid characters: {self.employeeNo}. "
                "Only letters, numbers, hyphens, and underscores allowed."
            )
    
    def _validate_organization_id(self) -> None:
        """Validate organization ID - must be valid UUID."""
        if not self.organizationId:
            raise PersonValidationError("Organization id cannot be empty")
        try:
            from uuid import UUID
            UUID(self.organizationId)
        except ValueError:
            raise PersonValidationError(f"Invalid UUID format for organization id: {self.organizationId}")
    
    def _validate_status(self) -> None:
        """Validate status - must be PersonStatus enum."""
        if not isinstance(self.status, PersonStatus):
            raise PersonValidationError(
                f"Invalid status type: {type(self.status).__name__}. Expected PersonStatus."
            )
    
    def _validate_level(self) -> None:
        """Validate level - if provided, must be integer 1-100."""
        if self.level is not None:
            if not isinstance(self.level, int):
                raise PersonValidationError(f"Level must be integer, got: {type(self.level).__name__}")
            if self.level < 1:
                raise PersonValidationError(f"Level must be >= 1, got: {self.level}")
            if self.level > 100:
                raise PersonValidationError(f"Level must be <= 100, got: {self.level}")
    
    def _validate_timestamps(self) -> None:
        """Validate timestamps - updatedAt >= createdAt."""
        if not isinstance(self.createdAt, datetime):
            raise PersonValidationError(f"createdAt must be datetime, got: {type(self.createdAt).__name__}")
        if not isinstance(self.updatedAt, datetime):
            raise PersonValidationError(f"updatedAt must be datetime, got: {type(self.updatedAt).__name__}")
        if self.updatedAt < self.createdAt:
            raise PersonValidationError(
                f"updatedAt ({self.updatedAt}) cannot be earlier than createdAt ({self.createdAt})"
            )
    
    def is_active(self) -> bool:
        """Check if person is in active status (ACTIVE or PROBATION)."""
        return self.status in (PersonStatus.ACTIVE, PersonStatus.PROBATION)
    
    def to_dict(self) -> dict:
        """Convert Person to dictionary representation."""
        return {
            "id": self.id,
            "name": self.name,
            "employeeNo": self.employeeNo,
            "organizationId": self.organizationId,
            "status": self.status.value,
            "level": self.level,
            "createdAt": self.createdAt.isoformat(),
            "updatedAt": self.updatedAt.isoformat()
        }
    
    @classmethod
    def from_dict(cls, data: dict) -> "Person":
        """Create Person from dictionary representation."""
        return cls(
            id=data.get("id", str(uuid4())),
            name=data.get("name", ""),
            employeeNo=data.get("employeeNo", ""),
            organizationId=data.get("organizationId", ""),
            status=PersonStatus(data.get("status", "ACTIVE")),
            level=data.get("level"),
            createdAt=datetime.fromisoformat(data["createdAt"]) if "createdAt" in data else datetime.now(),
            updatedAt=datetime.fromisoformat(data["updatedAt"]) if "updatedAt" in data else datetime.now()
        )
    
    def update_timestamp(self) -> None:
        """Update the updatedAt timestamp."""
        self.updatedAt = datetime.now()