"""Person service module - business logic for Person operations."""

from typing import List, Optional
from pathlib import Path
import json

from ..models.person import Person
from ..models.organization import Organization
from ..models.enums import PersonStatus


class PersonService:
    """Service class for managing Person entities with cross-entity validation.
    
    This service encapsulates all Person-related business logic including:
    - CRUD operations
    - Cross-entity validation (ensuring organizationId is valid)
    - Status transitions
    
    Attributes:
        data_dir: Directory path for data storage.
        persons_file: Path to the persons data file.
        orgs_file: Path to the organizations data file.
    """
    
    def __init__(self, data_dir: Optional[str] = None):
        """Initialize PersonService.
        
        Args:
            data_dir: Optional custom data directory path. 
                     Defaults to 'personnel-management/data'.
        """
        if data_dir:
            self.data_dir = Path(data_dir)
        else:
            self.data_dir = Path(__file__).parent.parent / "data"
        
        self.persons_file = self.data_dir / "persons.json"
        self.orgs_file = self.data_dir / "organizations.json"
        
        # Ensure data directory exists
        self.data_dir.mkdir(parents=True, exist_ok=True)
    
    # ==================== Internal Data Access ====================
    
    def _load_persons(self) -> List[Person]:
        """Load all persons from storage.
        
        Returns:
            List of Person objects.
        """
        if not self.persons_file.exists():
            return []
        
        with open(self.persons_file, 'r', encoding='utf-8') as f:
            data = json.load(f)
        
        return [Person.from_dict(item) for item in data]
    
    def _save_persons(self, persons: List[Person]) -> None:
        """Save persons to storage.
        
        Args:
            persons: List of Person objects to save.
        """
        data = [p.to_dict() for p in persons]
        with open(self.persons_file, 'w', encoding='utf-8') as f:
            json.dump(data, f, indent=2, ensure_ascii=False)
    
    def _load_organizations(self) -> List[Organization]:
        """Load all organizations from storage.
        
        Returns:
            List of Organization objects.
        """
        if not self.orgs_file.exists():
            return []
        
        with open(self.orgs_file, 'r', encoding='utf-8') as f:
            data = json.load(f)
        
        return [Organization.from_dict(item) for item in data]
    
    def _validate_organization_exists(self, organization_id: str) -> bool:
        """Validate that an organization with the given ID exists.
        
        Args:
            organization_id: The organization ID to validate.
            
        Returns:
            True if the organization exists, False otherwise.
        """
        organizations = self._load_organizations()
        return any(org.id == organization_id for org in organizations)
    
    # ==================== Public CRUD Operations ====================
    
    def create_person(self, 
                      name: str,
                      employee_no: str,
                      organization_id: str,
                      status: PersonStatus = PersonStatus.ACTIVE,
                      level: Optional[int] = None) -> Person:
        """Create a new person with validation.
        
        Args:
            name: Person's full name.
            employee_no: Employee number.
            organization_id: ID of the organization the person belongs to.
            status: Person's status (default: ACTIVE).
            level: Optional level for budget association.
            
        Returns:
            The created Person object.
            
        Raises:
            ValueError: If the organization_id does not reference a valid organization.
        """
        # Cross-entity validation: ensure organization exists
        if not self._validate_organization_exists(organization_id):
            raise ValueError(
                f"Invalid organizationId: organization with ID '{organization_id}' does not exist"
            )
        
        person = Person(
            name=name,
            employeeNo=employee_no,
            organizationId=organization_id,
            status=status,
            level=level
        )
        
        persons = self._load_persons()
        persons.append(person)
        self._save_persons(persons)
        
        return person
    
    def get_person(self, person_id: str) -> Optional[Person]:
        """Get a person by ID.
        
        Args:
            person_id: The unique identifier of the person.
            
        Returns:
            Person object if found, None otherwise.
        """
        persons = self._load_persons()
        for person in persons:
            if person.id == person_id:
                return person
        return None
    
    def get_person_by_employee_no(self, employee_no: str) -> Optional[Person]:
        """Get a person by employee number.
        
        Args:
            employee_no: The employee number to search for.
            
        Returns:
            Person object if found, None otherwise.
        """
        persons = self._load_persons()
        for person in persons:
            if person.employeeNo == employee_no:
                return person
        return None
    
    def list_persons(self, 
                      organization_id: Optional[str] = None,
                      status: Optional[PersonStatus] = None) -> List[Person]:
        """List persons with optional filtering.
        
        Args:
            organization_id: Filter by organization ID (optional).
            status: Filter by status (optional).
            
        Returns:
            List of Person objects matching the filters.
        """
        persons = self._load_persons()
        
        if organization_id:
            persons = [p for p in persons if p.organizationId == organization_id]
        
        if status:
            persons = [p for p in persons if p.status == status]
        
        return persons
    
    def update_person(self,
                      person_id: str,
                      name: Optional[str] = None,
                      employee_no: Optional[str] = None,
                      organization_id: Optional[str] = None,
                      status: Optional[PersonStatus] = None,
                      level: Optional[int] = None) -> Person:
        """Update a person's information.
        
        Args:
            person_id: The unique identifier of the person to update.
            name: New name (optional).
            employee_no: New employee number (optional).
            organization_id: New organization ID (optional).
            status: New status (optional).
            level: New level (optional).
            
        Returns:
            The updated Person object.
            
        Raises:
            ValueError: If person not found or organization_id is invalid.
        """
        persons = self._load_persons()
        person = None
        
        for i, p in enumerate(persons):
            if p.id == person_id:
                person = p
                break
        
        if not person:
            raise ValueError(f"Person with ID '{person_id}' not found")
        
        # Cross-entity validation if organization is being changed
        if organization_id and not self._validate_organization_exists(organization_id):
            raise ValueError(
                f"Invalid organizationId: organization with ID '{organization_id}' does not exist"
            )
        
        # Apply updates
        if name is not None:
            person.name = name
        if employee_no is not None:
            person.employeeNo = employee_no
        if organization_id is not None:
            person.organizationId = organization_id
        if status is not None:
            person.status = status
        if level is not None:
            person.level = level
        
        person.update_timestamp()
        self._save_persons(persons)
        
        return person
    
    def delete_person(self, person_id: str) -> bool:
        """Delete a person by ID.
        
        Args:
            person_id: The unique identifier of the person to delete.
            
        Returns:
            True if deletion was successful, False if person was not found.
        """
        persons = self._load_persons()
        original_count = len(persons)
        persons = [p for p in persons if p.id != person_id]
        
        if len(persons) < original_count:
            self._save_persons(persons)
            return True
        return False
    
    # ==================== Status Management ====================
    
    def change_status(self, person_id: str, new_status: PersonStatus) -> Person:
        """Change a person's status.
        
        Args:
            person_id: The unique identifier of the person.
            new_status: The new status to set.
            
        Returns:
            The updated Person object.
            
        Raises:
            ValueError: If person not found.
        """
        return self.update_person(person_id, status=new_status)
    
    def activate(self, person_id: str) -> Person:
        """Set person status to ACTIVE.
        
        Args:
            person_id: The unique identifier of the person.
            
        Returns:
            The updated Person object.
        """
        return self.change_status(person_id, PersonStatus.ACTIVE)
    
    def suspend(self, person_id: str) -> Person:
        """Set person status to SUSPENDED.
        
        Args:
            person_id: The unique identifier of the person.
            
        Returns:
            The updated Person object.
        """
        return self.change_status(person_id, PersonStatus.SUSPENDED)
    
    def set_on_leave(self, person_id: str) -> Person:
        """Set person status to ON_LEAVE.
        
        Args:
            person_id: The unique identifier of the person.
            
        Returns:
            The updated Person object.
        """
        return self.change_status(person_id, PersonStatus.ON_LEAVE)
    
    def resign(self, person_id: str) -> Person:
        """Set person status to RESIGNED.
        
        Args:
            person_id: The unique identifier of the person.
            
        Returns:
            The updated Person object.
        """
        return self.change_status(person_id, PersonStatus.RESIGNED)