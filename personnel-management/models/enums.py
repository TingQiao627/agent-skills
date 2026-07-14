"""Enumeration types for personnel management system."""

from enum import Enum, unique


@unique
class PersonStatus(Enum):
    """Status of a person in the organization.
    
    Attributes:
        ACTIVE: 在职 - Person is currently active in the organization.
        RESIGNED: 离职 - Person has resigned from the organization.
        PROBATION: 试用期 - Person is in probation period.
        SUSPENDED: 停职 - Person is suspended from the organization.
        RETIRED: 退休 - Person has retired from the organization.
    """
    ACTIVE = "ACTIVE"
    RESIGNED = "RESIGNED"
    PROBATION = "PROBATION"
    SUSPENDED = "SUSPENDED"
    RETIRED = "RETIRED"