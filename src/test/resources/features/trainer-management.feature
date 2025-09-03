@component
Feature: Trainer Management
  As a gym administrator
  I want to manage trainers
  So that I can assign them to trainees

  Background:
    Given the system is initialized

  @positive
  Scenario: Register a new trainer successfully
    When I register a new trainer with:
      | firstName | lastName | specializationId |
      | John      | Smith    | 1                |
    Then the response status should be 201
    And the response should contain field "username" with value "John.Smith"
    And the trainer "John.Smith" should exist in the database

  @positive
  Scenario: Register trainer with duplicate name gets serial number
    Given a trainer "Jane.Doe" exists
    When I register a new trainer with:
      | firstName | lastName | specializationId |
      | Jane      | Doe      | 1                |
    Then the response status should be 201
    And the trainer should be created with username pattern "Jane.Doe.[0-9]+"

  @negative
  Scenario: Register trainer with missing first name
    When I register a new trainer with:
      | lastName | specializationId |
      | Smith    | 1                |
    Then the response status should be 400
    And the error message should contain "must not be blank"