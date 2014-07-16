Feature: "sqlserver list" command
    In order to know all of the features available on a SQL Server database
    As a Geogit User
    I want to list all of the features

  Scenario: Try listing from an empty directory
    Given I am in an empty directory
     When I run the command "sqlserver list" on the SQL Server database
     Then the response should start with "Not in a geogit repository"
      
  Scenario: Try listing from a valid directory
    Given I have a repository
     When I run the command "sqlserver list" on the SQL Server database
     Then the response should contain "geogit_sqlserver_test"

