-- Drop authentication tables
-- This migration removes the account and session tables as authentication
-- has been moved to an external system.
--
-- Migration: V10__Drop_auth_tables.sql
-- Feature: 001-remove-auth-features
-- Date: 2025-12-25

-- Step 1: Drop t_session first (has foreign key to t_account)
DROP TABLE IF EXISTS t_session;

-- Step 2: Drop t_account
DROP TABLE IF EXISTS t_account;
