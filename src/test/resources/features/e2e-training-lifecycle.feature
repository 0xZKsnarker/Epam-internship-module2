@e2e
Feature: End-to-End Training Lifecycle
  As a gym management system
  I want to verify the complete training lifecycle
  So that all components work together correctly

  @smoke @critical
  Scenario Outline: Complete training lifecycle from creation to workload update
    Given the system is initialized
    And I am authenticated as <username>
    When I register a new trainer with:
      | firstName | lastName | specializationId |
      | E2E       | Trainer  | 1                |
    Then the response status should be <expectedStatus>
    And the trainer <username1> should exist in the database
    When I register a new trainee with:
      | firstName | lastName |
      | E2E       | Trainee  |
    Then the response status should be <expectedStatus>
    And the trainee <username2> should exist in the database
    When I create a training with:
      | trainerUsername | traineeUsername | trainingName | duration | date       |
      | <username1>     | <username2>     | E2E Test     | 60       | 2025-01-01 |
    Then the response status should be <expectedStatus>
    And the training should exist in the database
    And a JMS message should be sent to the workload queue
    Examples:
      | username | expectedStatus | username1   | username2   |
      | admin    | 201            | E2E.Trainer | E2E.Trainee |