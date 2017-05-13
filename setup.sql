-- CS61 Lab 2D
-- setup.sql
-- Shashwat Chaturvedi, James Edwards
-- May 2017

-- The below code creates our tables and inserts data into them
-- It is taken from our tables.sql and insert.sql files from L

-- Delete exisitng tables
DROP TABLE  IF EXISTS Secondary_author;
DROP TABLE  IF EXISTS Review;
DROP TABLE  IF EXISTS Article;
DROP TABLE  IF EXISTS Manuscript;
DROP TABLE  IF EXISTS Reviewer_aoi;
DROP TABLE  IF EXISTS Author;
DROP TABLE  IF EXISTS Reviewer;
DROP TABLE  IF EXISTS Issue;
DROP TABLE  IF EXISTS Editor;
DROP TABLE  IF EXISTS Aoi;


-- -----------------------------------------------------
-- Table Aoi (Area of interest)
-- -----------------------------------------------------
CREATE TABLE Aoi (
  aoi_ri_code INT NOT NULL AUTO_INCREMENT,
  aoi_name VARCHAR(45) NOT NULL,
  PRIMARY KEY (aoi_ri_code)
  );


-- -----------------------------------------------------
-- Table Editor
-- -----------------------------------------------------
CREATE TABLE Editor (
  editor_id INT NOT NULL AUTO_INCREMENT,
  editor_lname VARCHAR(45) NOT NULL,
  editor_fname VARCHAR(45) NOT NULL,
  PRIMARY KEY (editor_id)
  );


-- -----------------------------------------------------
-- Table Issue
-- -----------------------------------------------------
CREATE TABLE Issue (
  issue_id INT NOT NULL AUTO_INCREMENT,
  issue_year INT NOT NULL,
  issue_period INT NOT NULL,
  issue_print_date DATE NULL,
  PRIMARY KEY (issue_id)
  );


-- -----------------------------------------------------
-- Table Reviewer
-- -----------------------------------------------------
CREATE TABLE Reviewer (
  reviewer_id INT NOT NULL AUTO_INCREMENT,
  reviewer_lname VARCHAR(45) NOT NULL,
  reviewer_fname VARCHAR(45) NOT NULL,
  reviewer_affiliation VARCHAR(45) NOT NULL,
  reviewer_email VARCHAR(45) NOT NULL,
  PRIMARY KEY (reviewer_id)
  );


-- -----------------------------------------------------
-- Table Author
-- -----------------------------------------------------
CREATE TABLE Author (
  author_id INT NOT NULL AUTO_INCREMENT,
  author_lname VARCHAR(45) NOT NULL,
  author_fname VARCHAR(45) NOT NULL,
  author_address VARCHAR(45) NOT NULL,
  author_affiliation VARCHAR(45) NOT NULL,
  author_email VARCHAR(45) NOT NULL,
  PRIMARY KEY (author_id)
  );


-- -----------------------------------------------------
-- Table Reviewer_aoi
-- -----------------------------------------------------
CREATE TABLE Reviewer_aoi (
  aoi_ri_code INT NOT NULL,
  reviewer_id INT NOT NULL,
  PRIMARY KEY (aoi_ri_code, reviewer_id),
  INDEX `reviewer_id_idx` (reviewer_id ASC),
  CONSTRAINT ri_code
    FOREIGN KEY (aoi_ri_code)
    REFERENCES Aoi (aoi_ri_code),
  -- If a reviewer is removed from the system
  -- delete his areas of interest
  CONSTRAINT reviewer_id
    FOREIGN KEY (reviewer_id)
    REFERENCES Reviewer (reviewer_id)
    ON DELETE CASCADE
    );


-- -----------------------------------------------------
-- Table Manuscript
-- -----------------------------------------------------
CREATE TABLE Manuscript (
  manuscript_id INT NOT NULL AUTO_INCREMENT,
  manuscript_title VARCHAR(45) NOT NULL,
  manuscript_blob MEDIUMBLOB DEFAULT NULL,
  manuscript_update_date DATE NOT NULL,
  manuscript_status VARCHAR(45) NOT NULL,
  aoi_ri_code INT NOT NULL,
  author_id INT NOT NULL,
  editor_id INT NOT NULL,
  PRIMARY KEY (manuscript_id),
  INDEX `editor_id_idx` (editor_id ASC),
  INDEX `ri_code_idx` (aoi_ri_code ASC),
  INDEX `author_id_idx` (author_id ASC),
  CONSTRAINT editor_id
    FOREIGN KEY (editor_id)
    REFERENCES Editor (editor_id),
  CONSTRAINT aoi_ri_code
    FOREIGN KEY (aoi_ri_code)
    REFERENCES Aoi (aoi_ri_code),
  CONSTRAINT author_id
    FOREIGN KEY (author_id)
    REFERENCES Author (author_id)
    );


-- -----------------------------------------------------
-- Table Article
-- -----------------------------------------------------
CREATE TABLE Article (
  manuscript_id INT NOT NULL,
  article_num_pages INT NULL,
  article_order_num INT NULL,
  article_start_page INT NULL,
  issue_id INT NULL,
  PRIMARY KEY (manuscript_id),
  INDEX `issue_id_idx` (issue_id ASC),
  -- If a manuscript is removed from the system,
  -- delete any pending articles
  CONSTRAINT manuscript_article_id
    FOREIGN KEY (manuscript_id)
    REFERENCES Manuscript (manuscript_id)
    ON DELETE CASCADE,
  CONSTRAINT issue_article_id
    FOREIGN KEY (issue_id)
    REFERENCES Issue (issue_id)
    );


-- -----------------------------------------------------
-- Table Review
-- -----------------------------------------------------
CREATE TABLE Review (
  manuscript_id INT NOT NULL,
  reviewer_id INT NOT NULL,
  review_date_sent DATE NOT NULL,
  review_date_returned DATE NULL,
  review_recommendation VARCHAR(45) NULL,
  review_appropriateness INT NULL,
  review_clarity INT NULL,
  review_contribution INT NULL,
  review_methodology INT NULL,
  PRIMARY KEY (manuscript_id, reviewer_id),
  INDEX `reviewer_id_idx` (reviewer_id ASC),
  -- If a reviewer or manuscript is removed from the system
  -- their reviews should be deleted
  CONSTRAINT manuscript_id_review
    FOREIGN KEY (manuscript_id)
    REFERENCES Manuscript (manuscript_id)
    ON DELETE CASCADE,
  CONSTRAINT reviewer_id_review
    FOREIGN KEY (reviewer_id)
    REFERENCES Reviewer (reviewer_id)
    ON DELETE CASCADE
    );


-- -----------------------------------------------------
-- Table Secondary_author
-- -----------------------------------------------------
CREATE TABLE Secondary_author (
  sauthor_order_num INT NOT NULL,
  manuscript_id INT NOT NULL,
  sauthor_lname VARCHAR(45) NOT NULL,
  sauthor_fname VARCHAR(45) NOT NULL,
  sauthor_address VARCHAR(45) NOT NULL,
  sauthor_email VARCHAR(45) NOT NULL,
  PRIMARY KEY (sauthor_order_num, manuscript_id),
  CONSTRAINT manuscript_id_second_author
    FOREIGN KEY (manuscript_id)
    REFERENCES Manuscript (manuscript_id)
    -- If a manuscript is removed from the system, delete all secondary authors
    -- associated with it
    ON DELETE CASCADE
    );

-- Insert data into tables

-- Aoi (Area of interest) Codes --
INSERT INTO Aoi (aoi_name) VALUES
('Agricultural engineering'),
('Biochemical engineering'),
('Biomechanical engineering'),
('Ergonomics'),
('Food engineering'),
('Bioprocess engineering'),
('Genetic engineering'),
('Human genetic engineering'),
('Metabolic engineering'),
('Molecular engineering'),
('Neural engineering'),
('Protein engineering'),
('Rehabilitation engineering'),
('Tissue engineering'),
('Aquatic and environmental engineering'),
('Architectural engineering'),
('Civionic engineering'),
('Construction engineering'),
('Earthquake engineering'),
('Earth systems engineering and management'),
('Ecological engineering'),
('Environmental engineering'),
('Geomatics engineering'),
('Geotechnical engineering'),
('Highway engineering'),
('Hydraulic engineering'),
('Landscape engineering'),
('Land development engineering'),
('Pavement engineering'),
('Railway systems engineering'),
('River engineering'),
('Sanitary engineering'),
('Sewage engineering'),
('Structural engineering'),
('Surveying'),
('Traffic engineering'),
('Transportation engineering'),
('Urban engineering'),
('Irrigation and agriculture engineering'),
('Explosives engineering'),
('Biomolecular engineering'),
('Ceramics engineering'),
('Broadcast engineering'),
('Building engineering'),
('Signal Processing'),
('Computer engineering'),
('Power systems engineering'),
('Control engineering'),
('Telecommunications engineering'),
('Electronic engineering'),
('Instrumentation engineering'),
('Network engineering'),
('Neuromorphic engineering'),
('Engineering Technology'),
('Integrated engineering'),
('Value engineering'),
('Cost engineering'),
('Fire protection engineering'),
('Domain engineering'),
('Engineering economics'),
('Engineering management'),
('Engineering psychology'),
('Ergonomics'),
('Facilities Engineering'),
('Logistic engineering'),
('Model-driven engineering'),
('Performance engineering'),
('Process engineering'),
('Product Family Engineering'),
('Quality engineering'),
('Reliability engineering'),
('Safety engineering'),
('Security engineering'),
('Support engineering'),
('Systems engineering'),
('Metallurgical Engineering'),
('Surface Engineering'),
('Biomaterials Engineering'),
('Crystal Engineering'),
('Amorphous Metals'),
('Metal Forming'),
('Ceramic Engineering'),
('Plastics Engineering'),
('Forensic Materials Engineering'),
('Composite Materials'),
('Casting'),
('Electronic Materials'),
('Nano materials'),
('Corrosion Engineering'),
('Vitreous Materials'),
('Welding'),
('Acoustical engineering'),
('Aerospace engineering'),
('Audio engineering'),
('Automotive engineering'),
('Building services engineering'),
('Earthquake engineering'),
('Forensic engineering'),
('Marine engineering'),
('Mechatronics'),
('Nanoengineering'),
('Naval architecture'),
('Sports engineering'),
('Structural engineering'),
('Vacuum engineering'),
('Military engineering'),
('Combat engineering'),
('Offshore engineering'),
('Optical engineering'),
('Geophysical engineering'),
('Mineral engineering'),
('Mining engineering'),
('Reservoir engineering'),
('Climate engineering'),
('Computer-aided engineering'),
('Cryptographic engineering'),
('Information engineering'),
('Knowledge engineering'),
('Language engineering'),
('Release engineering'),
('Teletraffic engineering'),
('Usability engineering'),
('Web engineering'),
('Systems engineering');


-- Editors --
INSERT INTO Editor (editor_lname, editor_fname) VALUES
("Reese","Cally"),
("Hunter","MacKenzie"),
("Jacobson","Lillian"),
("Blair","Kyla"),
("Harvey","Amber"),
("Harding","Melanie"),
("Potter","Jolie"),
("Alvarado","Mollie"),
("Whitney","Xandra"),
("Burris","Alea"),
("Velazquez","Inga"),
("Butler","Heidi"),
("Hayes","Imani"),
("Black","Willow"),
("Poole","Amity"),
("Lang","Taylor"),
("Wolf","Karen"),
("Clay","Leslie"),
("Clements","Astra"),
("Roberson","Gisela"),
("Leach","Kelsie"),
("Nguyen","Chantale"),
("Medina","Angela"),
("Spears","Buffy"),
("Ingram","Pearl"),
("Mercer","Maxine"),
("Skinner","Karyn"),
("Daugherty","Sybil"),
("Hines","Ursula"),
("Savage","Katelyn"),
("Bullock","Haley"),
("Ware","Beverly"),
("Riggs","Pearl");

-- Issues --
INSERT INTO Issue (issue_year, issue_period, issue_print_date) VALUES
(1996, 1, '1996-01-11'),
(1996, 2, '1996-04-10'),
(1996, 3, '1996-08-25'),
(1996, 4, '1996-11-20'),
(1997, 1, '1997-01-11'),
(1997, 2, '1997-04-10'),
(1997, 3, '1997-08-25'),
(1997, 4, '1997-11-20'),
(1998, 1, '1997-01-11'),
(1998, 2, '1997-04-10'),
(1998, 3, '1997-08-25'),
(1998, 4, '1997-11-20'),
(1999, 1, '1997-01-11'),
(1999, 2, '1997-04-10'),
(1999, 3, '1997-08-25'),
(1999, 4, '1997-11-20');



-- Reviewer --
INSERT INTO Reviewer (reviewer_lname, reviewer_fname, reviewer_affiliation, reviewer_email) VALUES
("Parker","Rhea","Dartmouth","sapien.Aenean.massa@egestasSedpharetra.net"),
("Alford","Blake","Princeton","arcu.Morbi@nostra.net"),
("Robbins","Rhea","Penn","ut.mi@urna.net"),
("Bryan","Pandora","Cornell","dui.Suspendisse.ac@Nunc.net"),
("Mcdowell","Aidan","Penn","Aliquam.ornare@ametrisusDonec.org"),
("Kennedy","Lana","Harvard","penatibus.et@faucibusut.com"),
("Combs","Unity","Penn","tempor.arcu@gravidaPraesenteu.ca"),
("Pruitt","Rhoda","Dartmouth","amet@augueacipsum.edu"),
("Gomez","Bree","Cornell","sit.amet@sodalesMauris.ca"),
("Bonner","Nayda","Penn","vel.venenatis.vel@venenatis.com"),
("Conrad","Myles","Brown","ac.risus@scelerisquesedsapien.co.uk"),
("Garcia","Asher","Brown","Donec@ornaresagittisfelis.co.uk"),
("Cooley","Paul","Brown","placerat.Cras.dictum@magnisdis.org"),
("Christian","Morgan","Princeton","magna.Lorem.ipsum@egetvenenatis.org"),
("Oneal","Josephine","Columbia","eget.nisi@lacusvariuset.co.uk"),
("Simon","Keiko","Brown","risus@SuspendissesagittisNullam.com"),
("Goodman","Nelle","Columbia","enim.Mauris.quis@iaculis.edu"),
("Mayo","Stuart","Columbia","sit.amet@est.co.uk"),
("Love","Jameson","Cornell","Duis.ac@nuncInat.net"),
("Patel","Amal","Yale","ligula.eu.enim@magnisdis.org"),
("Odonnell","Florence","Penn","vel.arcu.eu@variuset.co.uk"),
("Rush","Gretchen","Columbia","semper.tellus.id@elitEtiamlaoreet.edu"),
("Langley","Cadman","Harvard","sed.hendrerit.a@inceptoshymenaeos.net"),
("Woodard","Urielle","Penn","neque.sed.sem@pharetraNam.org"),
("Daugherty","Michael","Cornell","tincidunt.aliquam@ultriciesdignissimlacus.edu"),
("Juarez","Quynn","Yale","nonummy@bibendum.ca"),
("Davidson","Erasmus","Cornell","pellentesque.Sed.dictum@ultricies.com"),
("Mcknight","Xerxes","Brown","nec.leo.Morbi@dui.net"),
("Dillard","Duncan","Yale","at.pretium@sollicitudin.ca");


-- Author --
INSERT INTO Author (author_lname, author_fname, author_address, author_affiliation, author_email) VALUES
("Burris","Hayley","7356 Magnis Rd.","Harvard","luctus.ipsum@uteros.co.uk"),
("Greene","Scarlett","Ap #697-2917 Sed Ave","Columbia","pede.Cras.vulputate@per.net"),
("Maddox","Giselle","Ap #771-8173 Montes, Avenue","Yale","interdum@Quisqueimperdieterat.ca"),
("Bird","Jasmine","Ap #168-4951 Malesuada Road","Cornell","eleifend@iaculisaliquet.ca"),
("Brady","Tom","44 College Road","Columbia","felis.Donec@Donecfelis.co.uk"),
("Melton","Abel","P.O. Box 577, 2780 Eget Rd.","Harvard","ligula.Aliquam.erat@eratVivamus.edu"),
("Cox","Dustin","5724 Cubilia Avenue","Cornell","Suspendisse.non@gravidamolestie.co.uk"),
("Rivers","Savannah","P.O. Box 551, 4706 Dui St.","Princeton","Nam.consequat@facilisis.com"),
("Camden","Tate","44 College Road","Dartmouth","in@pedenec.edu"),
("Rivers","Phillip","44 College Road.","Columbia","mi.lorem.vehicula@accumsanconvallis.net");

-- Reviewer_aoi --
INSERT INTO Reviewer_aoi (aoi_ri_code, reviewer_id) VALUES
(4, 2),
(5, 2),
(5, 1),
(30, 8),
(3, 8),
(10, 8),
(11, 11),
(8, 12),
(9, 14),
(20, 14),
(2, 3),
(2, 4),
(31, 3),
(8, 9),
(7, 7),
(10, 4),
(8, 4),
(12, 5);


-- Manuscript --
INSERT INTO Manuscript
(manuscript_title, manuscript_blob, manuscript_update_date, manuscript_status, aoi_ri_code, author_id, editor_id)
VALUES
("A","Blob1","1999-10-17","Accepted",8,10,14),
("B","Blob2","1997-03-17","UnderReview",12,6,1),
("C","Blob3","1999-03-26","UnderReview",16,8,13),
("D","Blob4","1998-03-19","Accepted",11,9,12),
("E","Blob5","1998-11-01","UnderReview",23,1,10),
("F","Blob6","1998-09-22","Accepted",15,4,13),
("G","Blob7","1996-02-07","Typeset",13,3,14),
("H","Blob8","1997-01-29","Published",20,3,15),
("I","Blob9","1998-05-19","Rejected",5,4,4),
("J","Blob10","1996-03-03","Scheduled",22,4,11),
("K","Blob11","1998-12-10","Scheduled",21,1,2),
("L","Blob12","1999-09-10","Typeset",15,9,8),
("M","Blob13","1999-02-13","Scheduled",22,2,11),
("N","Blob14","1998-02-01","Accepted",20,6,10),
("O","Blob15","1997-05-05","Published",17,3,15),
("P","Blob16","1999-08-10","UnderReview",28,10,14),
("Q","Blob17","1996-12-08","Published",7,4,6),
("R","Blob18","1997-05-03","Rejected",24,9,10),
("S","Blob19","1997-03-22","Accepted",3,10,10),
("T","Blob20","1997-03-22","Submitted",4,5,6),
("U","Blob21","1997-03-22","Submitted",3,2,5);


-- Article --
INSERT INTO Article (manuscript_id, article_num_pages, article_order_num, article_start_page, issue_id) VALUES
(2, 3, 1, 1, 6),
(3, 3, 1, 1, 7),
(7, 3, 1, 1, 8),
(8, 3, 1, 1, 9),
(10, 3, 1, 1, 1),
(11, 3, 1, 1, 3),
(12, 3, 1, 1, 4),
(13, 3, 1, 1, 5),
(14, 3, 1, 1, 10),
(15, 90, 2, 91, 9),
(17, 90, 1, 4, 10);

-- Review --
INSERT INTO Review
(manuscript_id, reviewer_id, review_date_sent, review_date_returned,
review_recommendation, review_appropriateness, review_clarity, review_contribution, review_methodology)
VALUES
(3,8,"1998-02-18","1998-05-16","Accept",8,5,3,6),
(2,2,"1998-08-13","1998-07-10","Reject",2,1,4,8),
(2,6,"1999-10-17","1996-08-04","Reject",2,10,3,5),
(1,6,"1996-08-21","1997-02-07","Accept",7,10,5,6),
(5,2,"1996-11-03","1997-09-21","Accept",6,5,7,7),
(1,5,"1996-11-12","1997-01-21","Accept",9,4,6,8),
(1,7,"1997-01-16","1999-10-23","Reject",9,9,3,10),
(3,10,"1999-06-21","1999-10-07","Reject",8,4,6,1),
(9,7,"1999-12-21","1999-02-07","Reject",4,4,1,2),
(6,8,"1996-08-01","1996-09-05","Reject",4,8,10,5);

-- Secondary_author --
INSERT INTO Secondary_author
(sauthor_order_num, manuscript_id, sauthor_lname, sauthor_fname, sauthor_address, sauthor_email)
VALUES
(1, 1, 'Camden', 'Tate', '44 College Road, Hanover, NH 03755', 'tate@dartmouth.edu'),
(2, 1, 'Smith', 'Alex', '44 College Road, Hanover, NH 03755', 'alex@dartmouth.edu'),
(3, 1, 'Adams', 'Jamaal', '44 College Road, Hanover, NH 03755', 'jamaal@dartmouth.edu'),
(1, 2, 'Rivers', 'Phillip', '44 College Road, NYC, NY 03755', 'phillip@columbia.edu'),
(2, 2, 'Brady', 'Tom', '44 College Road, NYC, NY 03755', 'tom@columbia.edu'),
(3, 2, 'Manning', 'Eli', '44 College Road, NYC, NY 03755', 'eli@dartmouth.edu'),
(1, 3, 'Thomas', 'Solomon', '44 College Road, SF, CA 03755', 'solomon@stanford.edu'),
(2, 3, 'James', 'Lebron', '44 College Road, SF, CA 03755', 'lebron@stanford.edu'),
(3, 3, 'Mariota', 'Marcus', '44 College Road, SF, CA 03755', 'marcus@stanford.edu');
