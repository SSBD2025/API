-- testowy init.sql
CREATE USER ssbd02mok WITH ENCRYPTED PASSWORD 'P@ssw0rd';
CREATE USER ssbd02mod WITH ENCRYPTED PASSWORD 'P@ssw0rd';
GRANT CONNECT ON DATABASE ssbd TO ssbd02mok;
GRANT USAGE ON SCHEMA public TO ssbd02mok;
GRANT SELECT, INSERT, UPDATE, DELETE ON TABLE account TO ssbd02mok;
GRANT SELECT, INSERT, UPDATE, DELETE ON TABLE admin TO ssbd02mok;
GRANT SELECT, INSERT, UPDATE, DELETE ON TABLE client TO ssbd02mok;
GRANT SELECT, INSERT, UPDATE, DELETE ON TABLE dietician TO ssbd02mok;
GRANT SELECT, INSERT, UPDATE, DELETE ON TABLE user_data TO ssbd02mok;
GRANT SELECT, INSERT, UPDATE, DELETE ON TABLE user_role TO ssbd02mok;
GRANT SELECT, INSERT, UPDATE, DELETE ON TABLE token_entity TO ssbd02mok;
GRANT SELECT ON TABLE dietary_restrictions TO ssbd02mok;
GRANT SELECT ON TABLE survey TO ssbd02mok;

GRANT CONNECT ON DATABASE ssbd TO ssbd02mod;
GRANT USAGE ON SCHEMA public TO ssbd02mod;
GRANT SELECT ON TABLE account TO ssbd02mod;
GRANT SELECT, INSERT, UPDATE ON TABLE blood_test_results TO ssbd02mod;
GRANT SELECT, INSERT, UPDATE ON TABLE client_blood_test_reports TO ssbd02mod;
GRANT SELECT ON TABLE client TO ssbd02mod;
GRANT SELECT, INSERT, UPDATE ON TABLE client_food_pyramid TO ssbd02mod;
GRANT SELECT, INSERT, UPDATE ON TABLE dietary_restrictions TO ssbd02mod;
GRANT SELECT ON TABLE dietician TO ssbd02mod;
GRANT SELECT, INSERT, UPDATE, DELETE ON TABLE feedback TO ssbd02mod;
GRANT SELECT, INSERT, UPDATE ON TABLE food_pyramid TO ssbd02mod;
GRANT SELECT, INSERT, UPDATE ON TABLE periodic_survey TO ssbd02mod;
GRANT SELECT, INSERT, UPDATE ON TABLE survey TO ssbd02mod;
GRANT SELECT, INSERT, UPDATE ON TABLE survey_allergies TO ssbd02mod;
GRANT SELECT, INSERT, UPDATE ON TABLE survey_diet_preferences TO ssbd02mod;
GRANT SELECT, INSERT, UPDATE ON TABLE survey_illnesses TO ssbd02mod;
GRANT SELECT, INSERT, UPDATE ON TABLE survey_meal_times TO ssbd02mod;
GRANT SELECT, INSERT, UPDATE ON TABLE survey_medications TO ssbd02mod;
GRANT SELECT ON TABLE user_data TO ssbd02mod;
GRANT SELECT ON TABLE user_role TO ssbd02mod;

-- -- 1. Insert into account
-- INSERT INTO account (
--     id, login, email, password,
--     active, verified, language, version
-- ) VALUES (
--              '11111111-1111-1111-1111-111111111111',
--              'adminlogin',
--              'admin@example.com',
--              '$2a$10$iALHwDyAc05S3/4xrlobOObjPveBXZtbjs6HnNeWyB10UCre3nbUm',
--              TRUE,
--              TRUE,
--              0,
--              0
--          );
--
-- -- 2. Insert into user_role
-- INSERT INTO user_role (
--     id, user_id, role, active, version
-- ) VALUES (
--              '22222222-2222-2222-2222-222222222222',
--              '11111111-1111-1111-1111-111111111111',
--              'ADMIN',
--              TRUE,
--              0
--          );
--
-- -- 3. Insert into user_data
-- INSERT INTO user_data (
--     id, first_name, last_name
-- ) VALUES (
--              '11111111-1111-1111-1111-111111111111',
--              'Test',
--              'Admin'
--          );
--
-- -- 4. Insert into admin
-- INSERT INTO admin (
--     id
-- ) VALUES (
--              '22222222-2222-2222-2222-222222222222'
--          );