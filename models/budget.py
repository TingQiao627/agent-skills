"""Budget entity class."""
from dataclasses import dataclass
from datetime import datetime
from typing import Optional


@dataclass
class Budget:
    """Budget entity representing a budget allocation.
    
    Attributes:
        level: Budget level (numeric identifier).
        levelName: Human-readable name for the budget level.
        budget: Budget amount.
        currency: Currency code (e.g., 'USD', 'CNY').
        validFrom: Start date of budget validity.
        validTo: End date of budget validity.
    """
    level: int
    levelName: str
    budget: float
    currency: str
    validFrom: datetime
    validTo: datetime
    
    def __post_init__(self):
        """Validate fields after initialization."""
        if not isinstance(self.level, int) or self.level < 0:
            raise ValueError("level must be a non-negative integer")
        if not self.levelName:
            raise ValueError("levelName cannot be empty")
        if self.budget < 0:
            raise ValueError("budget must be non-negative")
        if not self.currency:
            raise ValueError("currency cannot be empty")
        if self.validFrom > self.validTo:
            raise ValueError("validFrom must be before or equal to validTo")
    
    def is_valid_at(self, date: datetime) -> bool:
        """Check if budget is valid at a given date.
        
        Args:
            date: The date to check validity for.
            
        Returns:
            True if the budget is valid at the given date, False otherwise.
        """
        return self.validFrom <= date <= self.validTo
    
    def is_currently_valid(self) -> bool:
        """Check if budget is currently valid.
        
        Returns:
            True if the budget is valid at the current time, False otherwise.
        """
        return self.is_valid_at(datetime.now())
    
    def to_dict(self) -> dict:
        """Convert budget to dictionary representation.
        
        Returns:
            Dictionary containing all budget fields.
        """
        return {
            'level': self.level,
            'levelName': self.levelName,
            'budget': self.budget,
            'currency': self.currency,
            'validFrom': self.validFrom.isoformat(),
            'validTo': self.validTo.isoformat(),
        }
    
    @classmethod
    def from_dict(cls, data: dict) -> 'Budget':
        """Create Budget instance from dictionary.
        
        Args:
            data: Dictionary containing budget data.
            
        Returns:
            Budget instance.
            
        Raises:
            KeyError: If required fields are missing.
            ValueError: If field values are invalid.
        """
        return cls(
            level=data['level'],
            levelName=data['levelName'],
            budget=data['budget'],
            currency=data['currency'],
            validFrom=datetime.fromisoformat(data['validFrom']) if isinstance(data['validFrom'], str) else data['validFrom'],
            validTo=datetime.fromisoformat(data['validTo']) if isinstance(data['validTo'], str) else data['validTo'],
        )