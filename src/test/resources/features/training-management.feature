@integration
Feature: Microservices Integration via JMS
  As a distributed gym management system
  I want training operations to trigger workload updates
  So that trainer schedules are synchronized across services

  Background:
    Given both microservices are running
    And ActiveMQ is available
    And I am authenticated as "admin"

  @positive @async
  Scenario: Training creation sends workload update message
    Given a trainer "integration.trainer" exists
    And a trainee "integration.trainee" exists
    When I create a training with:
      | trainerUsername   | traineeUsername   | trainingName      | duration | date       |
      | integration.trainer | integration.trainee | Integration Test  | 60       | 2025-01-01 |
    Then the response status should be 201
    And a JMS message should be sent to the workload queue
    And the message should contain:
      | value               |
      | integration.trainer |

  @positive @async
  Scenario: Multiple trainings send multiple messages
    Given a trainer "batch.trainer" exists
    And a trainee "batch.trainee" exists
    When I create 3 trainings for the same trainer
    Then 3 messages should be sent to the workload queue
    And all messages should have actionType "ADD"

  @security
  Scenario: Unauthenticated requests are rejected
    Given I am not authenticated
    When I create a training with:
      | trainerUsername   | traineeUsername   | trainingName      | duration | date       |
      | any.trainer       | any.trainee       | Security Test     | 30       | 2025-02-01 |
    Then the response status should be 401