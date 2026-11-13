-- Creates a dedicated database and user for each application.
-- Runs once on first startup of the Postgres container.

CREATE USER shopping WITH PASSWORD 'shopping';
CREATE DATABASE shoppingdb OWNER shopping;

CREATE USER analytics WITH PASSWORD 'analytics';
CREATE DATABASE analyticsdb OWNER analytics;
