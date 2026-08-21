"""Tests for hello_world.py"""
import io
import sys
import pytest
from hello_world import main


def test_main_prints_hello_world():
    """Verify main() prints 'Hello, World!' to stdout."""
    # Arrange
    captured_output = io.StringIO()
    sys.stdout = captured_output

    try:
        # Act
        main()
    finally:
        # Reset stdout
        sys.stdout = sys.__stdout__

    # Assert
    output = captured_output.getvalue().strip()
    assert output == "Hello, World!", f"Expected 'Hello, World!', got '{output}'"


def test_main_executes_without_exception():
    """Verify main() runs without throwing exceptions."""
    # pytest treats any unhandled exception as a test failure
    captured_output = io.StringIO()
    sys.stdout = captured_output
    try:
        main()
    finally:
        sys.stdout = sys.__stdout__


def test_module_import():
    """Verify the module can be imported without error."""
    import hello_world
    assert hasattr(hello_world, 'main')
