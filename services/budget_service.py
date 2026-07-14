"""Budget service module."""
from datetime import datetime
from typing import List, Optional
from models.budget import Budget


class BudgetService:
    """Service class for managing Budget entities.
    
    Provides CRUD operations and query methods for Budget entities,
    including filtering by level and time range.
    """
    
    def __init__(self):
        """Initialize BudgetService with empty storage."""
        self._budgets: List[Budget] = []
    
    def add_budget(self, budget: Budget) -> None:
        """Add a budget to the service.
        
        Args:
            budget: Budget instance to add.
            
        Raises:
            ValueError: If budget is None or invalid.
        """
        if budget is None:
            raise ValueError("budget cannot be None")
        if not isinstance(budget, Budget):
            raise ValueError("budget must be a Budget instance")
        self._budgets.append(budget)
    
    def remove_budget(self, budget: Budget) -> bool:
        """Remove a budget from the service.
        
        Args:
            budget: Budget instance to remove.
            
        Returns:
            True if budget was removed, False if not found.
        """
        try:
            self._budgets.remove(budget)
            return True
        except ValueError:
            return False
    
    def get_all_budgets(self) -> List[Budget]:
        """Get all budgets.
        
        Returns:
            List of all Budget instances.
        """
        return self._budgets.copy()
    
    def get_budget_by_level(self, level: int) -> List[Budget]:
        """Get all budgets for a specific level.
        
        Args:
            level: Budget level to filter by.
            
        Returns:
            List of Budget instances with matching level.
        """
        return [b for b in self._budgets if b.level == level]
    
    def get_valid_budgets_by_level(self, level: int, date: Optional[datetime] = None) -> List[Budget]:
        """Get valid budgets for a specific level at a given date.
        
        Args:
            level: Budget level to filter by.
            date: Date to check validity for. Defaults to current datetime.
            
        Returns:
            List of valid Budget instances with matching level.
        """
        check_date = date if date is not None else datetime.now()
        return [
            b for b in self._budgets 
            if b.level == level and b.is_valid_at(check_date)
        ]
    
    def get_budgets_in_time_range(self, start_date: datetime, end_date: datetime) -> List[Budget]:
        """Get budgets that overlap with a time range.
        
        A budget overlaps with the time range if its validity period
        intersects with [start_date, end_date].
        
        Args:
            start_date: Start of the time range.
            end_date: End of the time range.
            
        Returns:
            List of Budget instances overlapping with the time range.
            
        Raises:
            ValueError: If start_date > end_date.
        """
        if start_date > end_date:
            raise ValueError("start_date must be before or equal to end_date")
        
        return [
            b for b in self._budgets
            if b.validFrom <= end_date and b.validTo >= start_date
        ]
    
    def get_valid_budgets_in_time_range(
        self, 
        start_date: datetime, 
        end_date: datetime,
        level: Optional[int] = None
    ) -> List[Budget]:
        """Get valid budgets in a time range, optionally filtered by level.
        
        A budget is considered valid in the time range if its validity period
        overlaps with [start_date, end_date].
        
        Args:
            start_date: Start of the time range.
            end_date: End of the time range.
            level: Optional budget level to filter by.
            
        Returns:
            List of valid Budget instances in the time range.
            
        Raises:
            ValueError: If start_date > end_date.
        """
        budgets = self.get_budgets_in_time_range(start_date, end_date)
        
        if level is not None:
            budgets = [b for b in budgets if b.level == level]
        
        return budgets
    
    def get_currently_valid_budgets(self, level: Optional[int] = None) -> List[Budget]:
        """Get all currently valid budgets, optionally filtered by level.
        
        Args:
            level: Optional budget level to filter by.
            
        Returns:
            List of currently valid Budget instances.
        """
        budgets = [b for b in self._budgets if b.is_currently_valid()]
        
        if level is not None:
            budgets = [b for b in budgets if b.level == level]
        
        return budgets
    
    def clear_all_budgets(self) -> None:
        """Remove all budgets from the service."""
        self._budgets.clear()
    
    def count_budgets(self) -> int:
        """Get the total number of budgets.
        
        Returns:
            Number of budgets in the service.
        """
        return len(self._budgets)
    
    def count_valid_budgets(self, date: Optional[datetime] = None) -> int:
        """Get the number of valid budgets at a given date.
        
        Args:
            date: Date to check validity for. Defaults to current datetime.
            
        Returns:
            Number of valid budgets.
        """
        check_date = date if date is not None else datetime.now()
        return sum(1 for b in self._budgets if b.is_valid_at(check_date))