-- CS61 Lab 2D
-- triggertest.sql
-- Shashwat Chaturvedi, James Edwards
-- May 2017

-- The below code should NOT activate our triggers

-- 1. INSERT manuscript that has aoi with 3 reviewers
-- This insertion should succeed
INSERT INTO Reviewer_aoi (aoi_ri_code, reviewer_id) VALUES 
(25, 3),
(25, 2),
(25, 1);
INSERT INTO Manuscript 
(manuscript_title, manuscript_blob, manuscript_update_date, manuscript_status, aoi_ri_code, author_id, editor_id) 
VALUES 
("A","Blob1","1999-10-17","Accepted",25,10,14);

-- 2. DELETE reviewer who has reviews that are not the only one for a manuscript
-- This deletion should not cause any updates to the manuscript status
-- In accordance with our database design, deleting a reviewer also deletes all his assocated 
-- Reviewer_aoi and Review entries
INSERT INTO Review 
(manuscript_id, reviewer_id, review_date_sent, review_date_returned, 
review_recommendation, review_appropriateness, review_clarity, review_contribution, review_methodology) 
VALUES
(15,10,"1998-02-18","1998-05-16","Accept",8,5,3,6),
(15,2,"1998-08-13","1998-07-10","Reject",2,1,4,8);

DELETE FROM Reviewer WHERE reviewer_id = 10;

-- Initial look at data

SELECT * FROM Manuscript;
SELECT * FROM Review;
SELECT * FROM Reviewer;
SELECT * FROM Reviewer_aoi;

-- The below code SHOULD activate our triggers

-- INSERT manuscript with aoi code that only has 2 reviewers -- should fail w/ error message
INSERT INTO Reviewer_aoi (aoi_ri_code, reviewer_id) VALUES 
(24, 3),
(24, 2);
INSERT INTO Manuscript 
(manuscript_title, manuscript_blob, manuscript_update_date, manuscript_status, aoi_ri_code, author_id, editor_id) 
VALUES 
("A","Blob1","1999-10-17","Submitted",24,10,14);

-- DELETION causes update to manuscript status
-- Also removes associated Reviewer_aoi and Review entries
INSERT INTO Review 
(manuscript_id, reviewer_id, review_date_sent, review_date_returned, 
review_recommendation, review_appropriateness, review_clarity, review_contribution, review_methodology) 
VALUES
(16,1,"1998-02-18","1998-05-16","Accept",8,5,3,6);

DELETE FROM Reviewer WHERE reviewer_id = 1;

-- Final look at data. Manuscript status has now changed for manuscript w/ ID 16

SELECT * FROM Manuscript;
SELECT * FROM Review;
SELECT * FROM Reviewer;
SELECT * FROM Reviewer_aoi;

