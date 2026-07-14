"""Repository for Person entity with JSON file storage."""

import json
import os
from pathlib import Path
from typing import List, Optional

from models import Person


class PersonRepository:
    """Repository for managing Person entities with JSON file storage.
    
    Provides CRUD operations (Create, Read, Update, Delete) and query methods
    for Person entities, persisting data to a JSON file.
    
    Attributes:
        file_path: Path to the JSON file storing person data.
    """
    
    def __init__(self, file_path: str = "data/persons.json"):
        """Initialize the repository.
        
        Args:
            file_path: Path to the JSON file for storage. Defaults to 'data/persons.json'.
        """
        self.file_path = Path(file_path)
        self._ensure_file_exists()
    
    def _ensure_file_exists(self) -> None:
        """Create the data file and parent directories if they don't exist."""
        self.file_path.parent.mkdir(parents=True, exist_ok=True)
        if not self.file_path.exists():
            self._write_data([])
    
    def _read_data(self) -> List[dict]:
        """Read all person data from JSON file.
        
        Returns:
            List of person dictionaries.
        """
        try:
            with open(self.file_path, 'r', encoding='utf-8') as f:
                return json.load(f)
        except (json.JSONDecodeError, FileNotFoundError):
            return []
    
    def _write_data(self, data: List[dict]) -> None:
        """Write person data to JSON file.
        
        Args:
            data: List of person dictionaries to write.
        """
        with open(self.file_path, 'w', encoding='utf-8') as f:
            json.dump(data, f, indent=2, ensure_ascii=False)
    
    def create(self, person: Person) -> Person:
        """Create a new person record.
        
        Args:
            person: Person instance to create.
            
        Returns:
            The created Person instance.
            
        Raises:
            ValueError: If a person with the same ID already exists.
        """
        persons = self._read_data()
        
        # Check for duplicate ID
        if any(p.get('id') == person.id for p in persons):
            raise ValueError(f"Person with id '{person.id}' already exists")
        
        persons.append(person.to_dict())
        self._write_data(persons)
        return person
    
    def read(self, person_id: str) -> Optional[Person]:
        """Read a person by ID.
        
        Args:
            person_id: The unique identifier of the person.
            
        Returns:
            Person instance if found, None otherwise.
        """
        persons = self._read_data()
        for p in persons:
            if p.get('id') == person_id:
                return Person.from_dict(p)
        return None
    
    def update(self, person: Person) -> Person:
        """Update an existing person record.
        
        Args:
            person: Person instance with updated data.
            
        Returns:
            The updated Person instance.
            
        Raises:
            ValueError: If person with the given ID doesn't exist.
        """
        persons = self._read_data()
        person.update_timestamp()
        
        for i, p in enumerate(persons):
            if p.get('id') == person.id:
                persons[i] = person.to_dict()
                self._write_data(persons)
                return person
        
        raise ValueError(f"Person with id '{person.id}' not found")
    
    def delete(self, person_id: str) -> bool:
        """Delete a person by ID.
        
        Args:
            person_id: The unique identifier of the person to delete.
            
        Returns:
            True if the person was deleted, False if not found.
        """
        persons = self._read_data()
        original_count = len(persons)
        persons = [p for p in persons if p.get('id') != person_id]
        
        if len(persons) < original_count:
            self._write_data(persons)
            return True
        return False
    
    def find_all(self) -> List[Person]:
        """Get all persons.
        
        Returns:
            List of all Person instances.
        """
        persons = self._read_data()
        return [Person.from_dict(p) for p in persons]
    
    def find_by_organization(self, organization_id: str) -> List[Person]:
        """Find all persons belonging to an organization.
        
        Args:
            organization_id: The organization ID to filter by.
            
        Returns:
            List of Person instances in the organization.
        """
        persons = self._read_data()
        return [
            Person.from_dict(p) for p in persons
            if p.get('organization_id') == organization_id
        ]
    
    def find_by_status(self, status: str) -> List[Person]:
        """Find all persons with a specific status.
        
        Args:
            status: The status to filter by (e.g., 'ACTIVE', 'ON_LEAVE').
            
        Returns:
            List of Person instances with the given status.
        """
        persons = self._read_data()
        return [
            Person.from_dict(p) for p in persons
            if p.get('status') == status
        ]
    
    def find_by_employee_no(self, employee_no: str) -> Optional[Person]:
        """Find a person by employee number.
        
        Args:
            employee_no: The employee number to search for.
            
        Returns:
            Person instance if found, None otherwise.
        """
        persons = self._read_data()
        for p in persons:
            if p.get('employee_no') == employee_no:
                return Person.from_dict(p)
        return None
    
    def count(self) -> int:
        """Get the total count of persons.
        
        Returns:
            Number of person records.
        """
        return len(self._read_data())
    
    def clear(self) -> None:
        """Remove all person records."""
        self._write_data([])