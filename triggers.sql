-- CS61 Lab 2D
-- triggers.sql
-- Shashwat Chaturvedi, James Edwards
-- May 2017

DROP TRIGGER IF EXISTS before_submission;
DROP TRIGGER IF EXISTS before_reviewer_resign;

DELIMITER $$

CREATE TRIGGER before_submission
    BEFORE INSERT ON Manuscript
    FOR EACH ROW BEGIN
		DECLARE signal_message VARCHAR(128);
        IF (SELECT COUNT(reviewer_id) from Reviewer_aoi where aoi_ri_code = NEW.aoi_ri_code) < 3 THEN
            SET signal_message = 'Exception: not enough valid reviewers';
			SIGNAL SQLSTATE '45000' SET message_text = signal_message;
		END IF;
END$$

CREATE TRIGGER before_reviewer_resign
    BEFORE DELETE ON Reviewer
    FOR EACH ROW BEGIN
		UPDATE Manuscript SET manuscript_status = "Submitted" WHERE manuscript_status="UnderReview" AND manuscript_id IN 
		(SELECT manuscript_id FROM 
			(SELECT * FROM Review GROUP BY manuscript_id HAVING COUNT(*) = 1) 
		AS ReviewsForReviewer 
		WHERE reviewer_id = OLD.reviewer_id);
END$$

DELIMITER ;
