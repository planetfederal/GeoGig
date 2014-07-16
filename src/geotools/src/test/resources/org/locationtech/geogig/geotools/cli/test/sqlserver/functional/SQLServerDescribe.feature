Feature: "sqlserver describe" command
    In order to understand the structure of a table in a SQL Server database
    As a Geogit User
    I want Geogit to describe the table

  Scenario: Try describing a SQL Server table from an empty directory
    Given I am in an empty directory
     When I run the command "sqlserver describe --table geogit_sqlserver_test" on the SQL Server database
     Then the response should start with "Not in a geogit repository"
      
  Scenario: Try describing a SQL Server table
    Given I have a repository
     When I run the command "sqlserver describe --table geogit_sqlserver_test" on the SQL Server database
     Then the response should contain "Table : geogit_sqlserver_test"
     
  Scenario: Try to describe a SQL Server table that doesn't exit in the database
    Given I have a repository
     When I run the command "sqlserver describe --table nonexistant_table" on the SQL Server database
     Then the response should contain "Could not find the specified table."
