-- phpMyAdmin SQL Dump
-- version 4.9.1
-- https://www.phpmyadmin.net/
--
-- Hôte : localhost
-- Généré le :  jeu. 17 mars 2022 à 12:50
-- Version du serveur :  8.0.19
-- Version de PHP :  7.4.3

SET SQL_MODE = "NO_AUTO_VALUE_ON_ZERO";
SET AUTOCOMMIT = 0;
START TRANSACTION;
SET time_zone = "+00:00";


/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8mb4 */;

--
-- Base de données :  `gradeScopeIstic`
--

-- --------------------------------------------------------

--
-- Structure de la table `comments`
--

CREATE TABLE `comments` (
  `id` bigint NOT NULL,
  `json_data` varchar(255) DEFAULT NULL,
  `student_response_id` bigint DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- --------------------------------------------------------

--
-- Structure de la table `course`
--

CREATE TABLE `course` (
  `id` bigint NOT NULL,
  `name` varchar(255) NOT NULL,
  `prof_id` bigint NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

--
-- Déchargement des données de la table `course`
--

INSERT INTO `course` (`id`, `name`, `prof_id`) VALUES
(1, 'TLC', 5),
(2, 'ExamSteven', 6);

-- --------------------------------------------------------

--
-- Structure de la table `course_group`
--

CREATE TABLE `course_group` (
  `id` bigint NOT NULL,
  `group_name` varchar(255) NOT NULL,
  `course_id` bigint DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- --------------------------------------------------------

--
-- Structure de la table `course_group_students`
--

CREATE TABLE `course_group_students` (
  `students_id` bigint NOT NULL,
  `course_group_id` bigint NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- --------------------------------------------------------

--
-- Structure de la table `DATABASECHANGELOG`
--

CREATE TABLE `DATABASECHANGELOG` (
  `ID` varchar(255) NOT NULL,
  `AUTHOR` varchar(255) NOT NULL,
  `FILENAME` varchar(255) NOT NULL,
  `DATEEXECUTED` datetime NOT NULL,
  `ORDEREXECUTED` int NOT NULL,
  `EXECTYPE` varchar(10) NOT NULL,
  `MD5SUM` varchar(35) DEFAULT NULL,
  `DESCRIPTION` varchar(255) DEFAULT NULL,
  `COMMENTS` varchar(255) DEFAULT NULL,
  `TAG` varchar(255) DEFAULT NULL,
  `LIQUIBASE` varchar(20) DEFAULT NULL,
  `CONTEXTS` varchar(255) DEFAULT NULL,
  `LABELS` varchar(255) DEFAULT NULL,
  `DEPLOYMENT_ID` varchar(10) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

--
-- Déchargement des données de la table `DATABASECHANGELOG`
--

INSERT INTO `DATABASECHANGELOG` (`ID`, `AUTHOR`, `FILENAME`, `DATEEXECUTED`, `ORDEREXECUTED`, `EXECTYPE`, `MD5SUM`, `DESCRIPTION`, `COMMENTS`, `TAG`, `LIQUIBASE`, `CONTEXTS`, `LABELS`, `DEPLOYMENT_ID`) VALUES
('00000000000001', 'jhipster', 'config/liquibase/changelog/00000000000000_initial_schema.xml', '2022-03-17 12:49:53', 1, 'EXECUTED', '8:23fa9c990d79fc7435997274f42c6e4a', 'createTable tableName=jhi_user; createTable tableName=jhi_authority; createTable tableName=jhi_user_authority; addPrimaryKey tableName=jhi_user_authority; addForeignKeyConstraint baseTableName=jhi_user_authority, constraintName=fk_authority_name, ...', '', NULL, '4.7.1', NULL, NULL, '7517793122'),
('00000000000002', 'jhipster', 'config/liquibase/changelog/00000000000000_initial_schema.xml', '2022-03-17 12:49:53', 2, 'EXECUTED', '8:fc8e5b393c90215202ec38612ed14f68', 'createTable tableName=jhi_date_time_wrapper', '', NULL, '4.7.1', 'test', NULL, '7517793122'),
('20220309164635-1', 'jhipster', 'config/liquibase/changelog/20220309164635_added_entity_Course.xml', '2022-03-17 12:49:53', 3, 'EXECUTED', '8:db512eb130666ca40a8f7673b442574f', 'createTable tableName=course', '', NULL, '4.7.1', NULL, NULL, '7517793122'),
('20220309164635-1-relations', 'jhipster', 'config/liquibase/changelog/20220309164635_added_entity_Course.xml', '2022-03-17 12:49:54', 4, 'EXECUTED', '8:d41d8cd98f00b204e9800998ecf8427e', 'empty', '', NULL, '4.7.1', NULL, NULL, '7517793122'),
('20220309164635-1-data', 'jhipster', 'config/liquibase/changelog/20220309164635_added_entity_Course.xml', '2022-03-17 12:49:54', 5, 'EXECUTED', '8:2c78a265053cc2aee36368e5dfe6ad91', 'loadData tableName=course', '', NULL, '4.7.1', 'faker', NULL, '7517793122'),
('20220309164735-1', 'jhipster', 'config/liquibase/changelog/20220309164735_added_entity_CourseGroup.xml', '2022-03-17 12:49:54', 6, 'EXECUTED', '8:d188b416506914358263aee3712138d2', 'createTable tableName=course_group', '', NULL, '4.7.1', NULL, NULL, '7517793122'),
('20220309164735-1-relations', 'jhipster', 'config/liquibase/changelog/20220309164735_added_entity_CourseGroup.xml', '2022-03-17 12:49:54', 7, 'EXECUTED', '8:06207811dcd6216c2207902cf1b9b0f1', 'createTable tableName=course_group_students; addPrimaryKey tableName=course_group_students', '', NULL, '4.7.1', NULL, NULL, '7517793122'),
('20220309164735-1-data', 'jhipster', 'config/liquibase/changelog/20220309164735_added_entity_CourseGroup.xml', '2022-03-17 12:49:54', 8, 'EXECUTED', '8:c4d72deeb78a9555bdb4cdb3ea752376', 'loadData tableName=course_group', '', NULL, '4.7.1', 'faker', NULL, '7517793122'),
('20220309164835-1', 'jhipster', 'config/liquibase/changelog/20220309164835_added_entity_Student.xml', '2022-03-17 12:49:54', 9, 'EXECUTED', '8:25a81938f58ebb51407752952f2312d4', 'createTable tableName=student', '', NULL, '4.7.1', NULL, NULL, '7517793122'),
('20220309164835-1-relations', 'jhipster', 'config/liquibase/changelog/20220309164835_added_entity_Student.xml', '2022-03-17 12:49:54', 10, 'EXECUTED', '8:c84e3bbda6b1b27e2d369f50f04c98de', 'createTable tableName=student_exam_sheets; addPrimaryKey tableName=student_exam_sheets', '', NULL, '4.7.1', NULL, NULL, '7517793122'),
('20220309164835-1-data', 'jhipster', 'config/liquibase/changelog/20220309164835_added_entity_Student.xml', '2022-03-17 12:49:54', 11, 'EXECUTED', '8:503c34cf76296c7d1765daa6e075fa7f', 'loadData tableName=student', '', NULL, '4.7.1', 'faker', NULL, '7517793122'),
('20220309164935-1', 'jhipster', 'config/liquibase/changelog/20220309164935_added_entity_Exam.xml', '2022-03-17 12:49:54', 12, 'EXECUTED', '8:307a85dcef95c80e2819ef9112f65c85', 'createTable tableName=exam', '', NULL, '4.7.1', NULL, NULL, '7517793122'),
('20220309164935-1-relations', 'jhipster', 'config/liquibase/changelog/20220309164935_added_entity_Exam.xml', '2022-03-17 12:49:54', 13, 'EXECUTED', '8:d41d8cd98f00b204e9800998ecf8427e', 'empty', '', NULL, '4.7.1', NULL, NULL, '7517793122'),
('20220309164935-1-data', 'jhipster', 'config/liquibase/changelog/20220309164935_added_entity_Exam.xml', '2022-03-17 12:49:54', 14, 'EXECUTED', '8:a392753764bfcb99d8dd86e5bc7e92d0', 'loadData tableName=exam', '', NULL, '4.7.1', 'faker', NULL, '7517793122'),
('20220309165035-1', 'jhipster', 'config/liquibase/changelog/20220309165035_added_entity_Template.xml', '2022-03-17 12:49:54', 15, 'EXECUTED', '8:73151da07861b56b381bca9a47059575', 'createTable tableName=template', '', NULL, '4.7.1', NULL, NULL, '7517793122'),
('20220309165035-1-relations', 'jhipster', 'config/liquibase/changelog/20220309165035_added_entity_Template.xml', '2022-03-17 12:49:54', 16, 'EXECUTED', '8:d41d8cd98f00b204e9800998ecf8427e', 'empty', '', NULL, '4.7.1', NULL, NULL, '7517793122'),
('20220309165035-1-data', 'jhipster', 'config/liquibase/changelog/20220309165035_added_entity_Template.xml', '2022-03-17 12:49:54', 17, 'EXECUTED', '8:9ed877764489e819d0ac7e53608d2c25', 'loadData tableName=template', '', NULL, '4.7.1', 'faker', NULL, '7517793122'),
('20220309165135-1', 'jhipster', 'config/liquibase/changelog/20220309165135_added_entity_Question.xml', '2022-03-17 12:49:54', 18, 'EXECUTED', '8:76366ba82da5a03428d8070294c58d05', 'createTable tableName=question', '', NULL, '4.7.1', NULL, NULL, '7517793122'),
('20220309165135-1-relations', 'jhipster', 'config/liquibase/changelog/20220309165135_added_entity_Question.xml', '2022-03-17 12:49:54', 19, 'EXECUTED', '8:d41d8cd98f00b204e9800998ecf8427e', 'empty', '', NULL, '4.7.1', NULL, NULL, '7517793122'),
('20220309165135-1-data', 'jhipster', 'config/liquibase/changelog/20220309165135_added_entity_Question.xml', '2022-03-17 12:49:54', 20, 'EXECUTED', '8:579ee03aa571258eef110730bb4e3d5b', 'loadData tableName=question', '', NULL, '4.7.1', 'faker', NULL, '7517793122'),
('20220309165235-1', 'jhipster', 'config/liquibase/changelog/20220309165235_added_entity_ExamSheet.xml', '2022-03-17 12:49:54', 21, 'EXECUTED', '8:d3505fb79e3d973df7f8edea74d71a02', 'createTable tableName=exam_sheet', '', NULL, '4.7.1', NULL, NULL, '7517793122'),
('20220309165235-1-relations', 'jhipster', 'config/liquibase/changelog/20220309165235_added_entity_ExamSheet.xml', '2022-03-17 12:49:54', 22, 'EXECUTED', '8:d41d8cd98f00b204e9800998ecf8427e', 'empty', '', NULL, '4.7.1', NULL, NULL, '7517793122'),
('20220309165235-1-data', 'jhipster', 'config/liquibase/changelog/20220309165235_added_entity_ExamSheet.xml', '2022-03-17 12:49:54', 23, 'EXECUTED', '8:156c607fce057a6e9ee9753a1b4b3de7', 'loadData tableName=exam_sheet', '', NULL, '4.7.1', 'faker', NULL, '7517793122'),
('20220309165335-1', 'jhipster', 'config/liquibase/changelog/20220309165335_added_entity_Scan.xml', '2022-03-17 12:49:54', 24, 'EXECUTED', '8:0416221a146e4c61273424166e93c14b', 'createTable tableName=scan', '', NULL, '4.7.1', NULL, NULL, '7517793122'),
('20220309165335-1-relations', 'jhipster', 'config/liquibase/changelog/20220309165335_added_entity_Scan.xml', '2022-03-17 12:49:54', 25, 'EXECUTED', '8:d41d8cd98f00b204e9800998ecf8427e', 'empty', '', NULL, '4.7.1', NULL, NULL, '7517793122'),
('20220309165335-1-data', 'jhipster', 'config/liquibase/changelog/20220309165335_added_entity_Scan.xml', '2022-03-17 12:49:54', 26, 'EXECUTED', '8:4b4da57ce89f358de4b85d38a0b3a5aa', 'loadData tableName=scan', '', NULL, '4.7.1', 'faker', NULL, '7517793122'),
('20220309165435-1', 'jhipster', 'config/liquibase/changelog/20220309165435_added_entity_FinalResult.xml', '2022-03-17 12:49:54', 27, 'EXECUTED', '8:fabf8d5113ebb437910639c34b3d4932', 'createTable tableName=final_result', '', NULL, '4.7.1', NULL, NULL, '7517793122'),
('20220309165435-1-relations', 'jhipster', 'config/liquibase/changelog/20220309165435_added_entity_FinalResult.xml', '2022-03-17 12:49:54', 28, 'EXECUTED', '8:d41d8cd98f00b204e9800998ecf8427e', 'empty', '', NULL, '4.7.1', NULL, NULL, '7517793122'),
('20220309165435-1-data', 'jhipster', 'config/liquibase/changelog/20220309165435_added_entity_FinalResult.xml', '2022-03-17 12:49:54', 29, 'EXECUTED', '8:7463be5b87dfeab63ecf7391149abcce', 'loadData tableName=final_result', '', NULL, '4.7.1', 'faker', NULL, '7517793122'),
('20220309165535-1', 'jhipster', 'config/liquibase/changelog/20220309165535_added_entity_StudentResponse.xml', '2022-03-17 12:49:54', 30, 'EXECUTED', '8:8b0be71d2017c529be6632d7fa8ff9cb', 'createTable tableName=student_response', '', NULL, '4.7.1', NULL, NULL, '7517793122'),
('20220309165535-1-relations', 'jhipster', 'config/liquibase/changelog/20220309165535_added_entity_StudentResponse.xml', '2022-03-17 12:49:54', 31, 'EXECUTED', '8:d41d8cd98f00b204e9800998ecf8427e', 'empty', '', NULL, '4.7.1', NULL, NULL, '7517793122'),
('20220309165535-1-data', 'jhipster', 'config/liquibase/changelog/20220309165535_added_entity_StudentResponse.xml', '2022-03-17 12:49:54', 32, 'EXECUTED', '8:376324e2fc132a6be1396ce89a1bca8b', 'loadData tableName=student_response', '', NULL, '4.7.1', 'faker', NULL, '7517793122'),
('20220309165635-1', 'jhipster', 'config/liquibase/changelog/20220309165635_added_entity_Comments.xml', '2022-03-17 12:49:54', 33, 'EXECUTED', '8:d30b81e0be76cae7aee7f3c5b80480f8', 'createTable tableName=comments', '', NULL, '4.7.1', NULL, NULL, '7517793122'),
('20220309165635-1-relations', 'jhipster', 'config/liquibase/changelog/20220309165635_added_entity_Comments.xml', '2022-03-17 12:49:54', 34, 'EXECUTED', '8:d41d8cd98f00b204e9800998ecf8427e', 'empty', '', NULL, '4.7.1', NULL, NULL, '7517793122'),
('20220309165635-1-data', 'jhipster', 'config/liquibase/changelog/20220309165635_added_entity_Comments.xml', '2022-03-17 12:49:54', 35, 'EXECUTED', '8:689d765ae061203a715999952c8b2615', 'loadData tableName=comments', '', NULL, '4.7.1', 'faker', NULL, '7517793122'),
('20220309165735-1', 'jhipster', 'config/liquibase/changelog/20220309165735_added_entity_Zone.xml', '2022-03-17 12:49:54', 36, 'EXECUTED', '8:1a9c1de6e7145e397f3394fd762f277e', 'createTable tableName=zone', '', NULL, '4.7.1', NULL, NULL, '7517793122'),
('20220309165735-1-relations', 'jhipster', 'config/liquibase/changelog/20220309165735_added_entity_Zone.xml', '2022-03-17 12:49:54', 37, 'EXECUTED', '8:d41d8cd98f00b204e9800998ecf8427e', 'empty', '', NULL, '4.7.1', NULL, NULL, '7517793122'),
('20220309165735-1-data', 'jhipster', 'config/liquibase/changelog/20220309165735_added_entity_Zone.xml', '2022-03-17 12:49:55', 38, 'EXECUTED', '8:c5da0f4a31bcbefd29835d3c7b975974', 'loadData tableName=zone', '', NULL, '4.7.1', 'faker', NULL, '7517793122'),
('20220311140708-1', 'jhipster', 'config/liquibase/changelog/20220311140708_added_entity_QuestionType.xml', '2022-03-17 12:49:55', 39, 'EXECUTED', '8:2ae422403ed0c8c62e8e8e245623480e', 'createTable tableName=question_type', '', NULL, '4.7.1', NULL, NULL, '7517793122'),
('20220311140708-1-relations', 'jhipster', 'config/liquibase/changelog/20220311140708_added_entity_QuestionType.xml', '2022-03-17 12:49:55', 40, 'EXECUTED', '8:d41d8cd98f00b204e9800998ecf8427e', 'empty', '', NULL, '4.7.1', NULL, NULL, '7517793122'),
('20220311140708-1-data', 'jhipster', 'config/liquibase/changelog/20220311140708_added_entity_QuestionType.xml', '2022-03-17 12:49:55', 41, 'EXECUTED', '8:9795601c8b37a663ce092738dfd66353', 'loadData tableName=question_type', '', NULL, '4.7.1', 'faker', NULL, '7517793122'),
('20220309164635-2', 'jhipster', 'config/liquibase/changelog/20220309164635_added_entity_constraints_Course.xml', '2022-03-17 12:49:55', 42, 'EXECUTED', '8:eaf11ab06ee85bdcfefc3cac1235d9e4', 'addForeignKeyConstraint baseTableName=course, constraintName=fk_course_prof_id, referencedTableName=jhi_user', '', NULL, '4.7.1', NULL, NULL, '7517793122'),
('20220309164735-2', 'jhipster', 'config/liquibase/changelog/20220309164735_added_entity_constraints_CourseGroup.xml', '2022-03-17 12:49:55', 43, 'EXECUTED', '8:80ad08e5f58e98334f1b7e1b65613f37', 'addForeignKeyConstraint baseTableName=course_group_students, constraintName=fk_course_group_students_course_group_id, referencedTableName=course_group; addForeignKeyConstraint baseTableName=course_group_students, constraintName=fk_course_group_stu...', '', NULL, '4.7.1', NULL, NULL, '7517793122'),
('20220309164835-2', 'jhipster', 'config/liquibase/changelog/20220309164835_added_entity_constraints_Student.xml', '2022-03-17 12:49:55', 44, 'EXECUTED', '8:91339e2307b7722f5799a0ad33001d54', 'addForeignKeyConstraint baseTableName=student_exam_sheets, constraintName=fk_student_exam_sheets_student_id, referencedTableName=student; addForeignKeyConstraint baseTableName=student_exam_sheets, constraintName=fk_student_exam_sheets_exam_sheets_...', '', NULL, '4.7.1', NULL, NULL, '7517793122'),
('20220309164935-2', 'jhipster', 'config/liquibase/changelog/20220309164935_added_entity_constraints_Exam.xml', '2022-03-17 12:49:56', 45, 'EXECUTED', '8:f0a92fdc5476b8a5c411a24ace23217d', 'addForeignKeyConstraint baseTableName=exam, constraintName=fk_exam_template_id, referencedTableName=template; addForeignKeyConstraint baseTableName=exam, constraintName=fk_exam_idzone_id, referencedTableName=zone; addForeignKeyConstraint baseTable...', '', NULL, '4.7.1', NULL, NULL, '7517793122'),
('20220309165135-2', 'jhipster', 'config/liquibase/changelog/20220309165135_added_entity_constraints_Question.xml', '2022-03-17 12:49:56', 46, 'EXECUTED', '8:50e161ef520b83167e5c28102b1b5849', 'addForeignKeyConstraint baseTableName=question, constraintName=fk_question_zone_id, referencedTableName=zone; addForeignKeyConstraint baseTableName=question, constraintName=fk_question_type_id, referencedTableName=question_type; addForeignKeyConst...', '', NULL, '4.7.1', NULL, NULL, '7517793122'),
('20220309165235-2', 'jhipster', 'config/liquibase/changelog/20220309165235_added_entity_constraints_ExamSheet.xml', '2022-03-17 12:49:56', 47, 'EXECUTED', '8:ffd88660dfaddb6b4674f9026bbaefb6', 'addForeignKeyConstraint baseTableName=exam_sheet, constraintName=fk_exam_sheet_scan_id, referencedTableName=scan', '', NULL, '4.7.1', NULL, NULL, '7517793122'),
('20220309165435-2', 'jhipster', 'config/liquibase/changelog/20220309165435_added_entity_constraints_FinalResult.xml', '2022-03-17 12:49:56', 48, 'EXECUTED', '8:6cf696852bf6e7509d3934dab1ec21aa', 'addForeignKeyConstraint baseTableName=final_result, constraintName=fk_final_result_student_id, referencedTableName=student; addForeignKeyConstraint baseTableName=final_result, constraintName=fk_final_result_exam_id, referencedTableName=exam', '', NULL, '4.7.1', NULL, NULL, '7517793122'),
('20220309165535-2', 'jhipster', 'config/liquibase/changelog/20220309165535_added_entity_constraints_StudentResponse.xml', '2022-03-17 12:49:56', 49, 'EXECUTED', '8:83e4be3ffcfe10c807016cb1edeee489', 'addForeignKeyConstraint baseTableName=student_response, constraintName=fk_student_response_question_id, referencedTableName=question; addForeignKeyConstraint baseTableName=student_response, constraintName=fk_student_response_student_id, referenced...', '', NULL, '4.7.1', NULL, NULL, '7517793122'),
('20220309165635-2', 'jhipster', 'config/liquibase/changelog/20220309165635_added_entity_constraints_Comments.xml', '2022-03-17 12:49:56', 50, 'EXECUTED', '8:37b194172b96f67cc11134fabd053b5d', 'addForeignKeyConstraint baseTableName=comments, constraintName=fk_comments_student_response_id, referencedTableName=student_response', '', NULL, '4.7.1', NULL, NULL, '7517793122');

-- --------------------------------------------------------

--
-- Structure de la table `DATABASECHANGELOGLOCK`
--

CREATE TABLE `DATABASECHANGELOGLOCK` (
  `ID` int NOT NULL,
  `LOCKED` bit(1) NOT NULL,
  `LOCKGRANTED` datetime DEFAULT NULL,
  `LOCKEDBY` varchar(255) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

--
-- Déchargement des données de la table `DATABASECHANGELOGLOCK`
--

INSERT INTO `DATABASECHANGELOGLOCK` (`ID`, `LOCKED`, `LOCKGRANTED`, `LOCKEDBY`) VALUES
(1, b'0', NULL, NULL);

-- --------------------------------------------------------

--
-- Structure de la table `exam`
--

CREATE TABLE `exam` (
  `id` bigint NOT NULL,
  `name` varchar(255) NOT NULL,
  `template_id` bigint DEFAULT NULL,
  `idzone_id` bigint DEFAULT NULL,
  `namezone_id` bigint DEFAULT NULL,
  `firstnamezone_id` bigint DEFAULT NULL,
  `notezone_id` bigint DEFAULT NULL,
  `scanfile_id` bigint DEFAULT NULL,
  `course_id` bigint DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- --------------------------------------------------------

--
-- Structure de la table `exam_sheet`
--

CREATE TABLE `exam_sheet` (
  `id` bigint NOT NULL,
  `name` varchar(255) NOT NULL,
  `pagemin` int DEFAULT NULL,
  `pagemax` int DEFAULT NULL,
  `scan_id` bigint DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- --------------------------------------------------------

--
-- Structure de la table `final_result`
--

CREATE TABLE `final_result` (
  `id` bigint NOT NULL,
  `note` int DEFAULT NULL,
  `student_id` bigint DEFAULT NULL,
  `exam_id` bigint DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- --------------------------------------------------------

--
-- Structure de la table `jhi_authority`
--

CREATE TABLE `jhi_authority` (
  `name` varchar(50) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

--
-- Déchargement des données de la table `jhi_authority`
--

INSERT INTO `jhi_authority` (`name`) VALUES
('ROLE_ADMIN'),
('ROLE_USER');

-- --------------------------------------------------------

--
-- Structure de la table `jhi_date_time_wrapper`
--

CREATE TABLE `jhi_date_time_wrapper` (
  `id` bigint NOT NULL,
  `instant` timestamp NULL DEFAULT NULL,
  `local_date_time` timestamp NULL DEFAULT NULL,
  `offset_date_time` timestamp NULL DEFAULT NULL,
  `zoned_date_time` timestamp NULL DEFAULT NULL,
  `local_time` time DEFAULT NULL,
  `offset_time` time DEFAULT NULL,
  `local_date` date DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- --------------------------------------------------------

--
-- Structure de la table `jhi_persistent_audit_event`
--

CREATE TABLE `jhi_persistent_audit_event` (
  `event_id` bigint NOT NULL,
  `principal` varchar(50) NOT NULL,
  `event_date` timestamp NULL DEFAULT NULL,
  `event_type` varchar(255) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- --------------------------------------------------------

--
-- Structure de la table `jhi_persistent_audit_evt_data`
--

CREATE TABLE `jhi_persistent_audit_evt_data` (
  `event_id` bigint NOT NULL,
  `name` varchar(150) NOT NULL,
  `value` varchar(255) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- --------------------------------------------------------

--
-- Structure de la table `jhi_user`
--

CREATE TABLE `jhi_user` (
  `id` bigint NOT NULL,
  `login` varchar(50) NOT NULL,
  `password_hash` varchar(60) NOT NULL,
  `first_name` varchar(50) DEFAULT NULL,
  `last_name` varchar(50) DEFAULT NULL,
  `email` varchar(191) DEFAULT NULL,
  `image_url` varchar(256) DEFAULT NULL,
  `activated` bit(1) NOT NULL,
  `lang_key` varchar(10) DEFAULT NULL,
  `activation_key` varchar(20) DEFAULT NULL,
  `reset_key` varchar(20) DEFAULT NULL,
  `created_by` varchar(50) NOT NULL,
  `created_date` timestamp NULL,
  `reset_date` timestamp NULL DEFAULT NULL,
  `last_modified_by` varchar(50) DEFAULT NULL,
  `last_modified_date` timestamp NULL DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

--
-- Déchargement des données de la table `jhi_user`
--

INSERT INTO `jhi_user` (`id`, `login`, `password_hash`, `first_name`, `last_name`, `email`, `image_url`, `activated`, `lang_key`, `activation_key`, `reset_key`, `created_by`, `created_date`, `reset_date`, `last_modified_by`, `last_modified_date`) VALUES
(1, 'system', '$2a$10$mE.qmcV0mFU5NcKh73TZx.z4ueI/.bDWbj0T1BYyqP481kGGarKLG', 'System', 'System', 'system@localhost', '', b'1', 'fr', NULL, NULL, 'system', NULL, NULL, 'system', NULL),
(2, 'anonymoususer', '$2a$10$j8S5d7Sr7.8VTOYNviDPOeWX8KcYILUVJBsYV83Y5NtECayypx9lO', 'Anonymous', 'User', 'anonymous@localhost', '', b'1', 'fr', NULL, NULL, 'system', NULL, NULL, 'system', NULL),
(3, 'admin', '$2a$10$gSAhZrxMllrbgj/kkK9UceBPpChGWJA7SYIb1Mqo.n5aNLq1/oRrC', 'Administrator', 'Administrator', 'admin@localhost', '', b'1', 'fr', NULL, NULL, 'system', NULL, NULL, 'system', NULL),
(4, 'user', '$2a$10$VEjxo0jq2YG9Rbk2HmX9S.k1uZBGYUHdUcid3g/vfiEl7lwWgOH/K', 'User', 'User', 'user@localhost', '', b'1', 'fr', NULL, NULL, 'system', NULL, NULL, 'system', NULL),
(5, 'barais', '$2a$10$VEjxo0jq2YG9Rbk2HmX9S.k1uZBGYUHdUcid3g/vfiEl7lwWgOH/K', 'Barais', 'Olivier', 'barais@irisa.fr', '', b'1', 'fr', NULL, NULL, 'system', NULL, NULL, 'system', NULL),
(6, 'derrien', '$2a$10$VEjxo0jq2YG9Rbk2HmX9S.k1uZBGYUHdUcid3g/vfiEl7lwWgOH/K', 'Derrien', 'Steven', 'steven.derrien@irisa.fr', '', b'1', 'fr', NULL, NULL, 'system', NULL, NULL, 'system', NULL);

-- --------------------------------------------------------

--
-- Structure de la table `jhi_user_authority`
--

CREATE TABLE `jhi_user_authority` (
  `user_id` bigint NOT NULL,
  `authority_name` varchar(50) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

--
-- Déchargement des données de la table `jhi_user_authority`
--

INSERT INTO `jhi_user_authority` (`user_id`, `authority_name`) VALUES
(1, 'ROLE_ADMIN'),
(3, 'ROLE_ADMIN'),
(1, 'ROLE_USER'),
(3, 'ROLE_USER'),
(4, 'ROLE_USER'),
(5, 'ROLE_USER'),
(6, 'ROLE_USER');

-- --------------------------------------------------------

--
-- Structure de la table `question`
--

CREATE TABLE `question` (
  `id` bigint NOT NULL,
  `numero` int NOT NULL,
  `point` int DEFAULT NULL,
  `zone_id` bigint DEFAULT NULL,
  `type_id` bigint DEFAULT NULL,
  `exam_id` bigint DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- --------------------------------------------------------

--
-- Structure de la table `question_type`
--

CREATE TABLE `question_type` (
  `id` bigint NOT NULL,
  `algo_name` varchar(255) NOT NULL,
  `endpoint` varchar(255) DEFAULT NULL,
  `js_function` varchar(255) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

--
-- Déchargement des données de la table `question_type`
--

INSERT INTO `question_type` (`id`, `algo_name`, `endpoint`, `js_function`) VALUES
(1, 'tsflowcharacter', '', ''),
(2, 'manual', '', '');

-- --------------------------------------------------------

--
-- Structure de la table `scan`
--

CREATE TABLE `scan` (
  `id` bigint NOT NULL,
  `name` varchar(255) NOT NULL,
  `content` longblob,
  `content_content_type` varchar(255) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- --------------------------------------------------------

--
-- Structure de la table `student`
--

CREATE TABLE `student` (
  `id` bigint NOT NULL,
  `name` varchar(255) NOT NULL,
  `firstname` varchar(255) DEFAULT NULL,
  `ine` varchar(255) NOT NULL,
  `caslogin` varchar(255) DEFAULT NULL,
  `mail` varchar(255) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- --------------------------------------------------------

--
-- Structure de la table `student_exam_sheets`
--

CREATE TABLE `student_exam_sheets` (
  `exam_sheets_id` bigint NOT NULL,
  `student_id` bigint NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- --------------------------------------------------------

--
-- Structure de la table `student_response`
--

CREATE TABLE `student_response` (
  `id` bigint NOT NULL,
  `note` int DEFAULT NULL,
  `question_id` bigint DEFAULT NULL,
  `student_id` bigint DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- --------------------------------------------------------

--
-- Structure de la table `template`
--

CREATE TABLE `template` (
  `id` bigint NOT NULL,
  `name` varchar(255) NOT NULL,
  `content` longblob,
  `content_content_type` varchar(255) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- --------------------------------------------------------

--
-- Structure de la table `zone`
--

CREATE TABLE `zone` (
  `id` bigint NOT NULL,
  `page` int DEFAULT NULL,
  `x_init` int DEFAULT NULL,
  `y_init` int DEFAULT NULL,
  `width` int DEFAULT NULL,
  `height` int DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

--
-- Index pour les tables déchargées
--

--
-- Index pour la table `comments`
--
ALTER TABLE `comments`
  ADD PRIMARY KEY (`id`),
  ADD KEY `fk_comments_student_response_id` (`student_response_id`);

--
-- Index pour la table `course`
--
ALTER TABLE `course`
  ADD PRIMARY KEY (`id`),
  ADD KEY `fk_course_prof_id` (`prof_id`);

--
-- Index pour la table `course_group`
--
ALTER TABLE `course_group`
  ADD PRIMARY KEY (`id`),
  ADD KEY `fk_course_group_course_id` (`course_id`);

--
-- Index pour la table `course_group_students`
--
ALTER TABLE `course_group_students`
  ADD PRIMARY KEY (`course_group_id`,`students_id`),
  ADD KEY `fk_course_group_students_students_id` (`students_id`);

--
-- Index pour la table `DATABASECHANGELOGLOCK`
--
ALTER TABLE `DATABASECHANGELOGLOCK`
  ADD PRIMARY KEY (`ID`);

--
-- Index pour la table `exam`
--
ALTER TABLE `exam`
  ADD PRIMARY KEY (`id`),
  ADD UNIQUE KEY `ux_exam_template_id` (`template_id`),
  ADD UNIQUE KEY `ux_exam_idzone_id` (`idzone_id`),
  ADD UNIQUE KEY `ux_exam_namezone_id` (`namezone_id`),
  ADD UNIQUE KEY `ux_exam_firstnamezone_id` (`firstnamezone_id`),
  ADD UNIQUE KEY `ux_exam_notezone_id` (`notezone_id`),
  ADD UNIQUE KEY `ux_exam_scanfile_id` (`scanfile_id`),
  ADD KEY `fk_exam_course_id` (`course_id`);

--
-- Index pour la table `exam_sheet`
--
ALTER TABLE `exam_sheet`
  ADD PRIMARY KEY (`id`),
  ADD KEY `fk_exam_sheet_scan_id` (`scan_id`);

--
-- Index pour la table `final_result`
--
ALTER TABLE `final_result`
  ADD PRIMARY KEY (`id`),
  ADD KEY `fk_final_result_student_id` (`student_id`),
  ADD KEY `fk_final_result_exam_id` (`exam_id`);

--
-- Index pour la table `jhi_authority`
--
ALTER TABLE `jhi_authority`
  ADD PRIMARY KEY (`name`);

--
-- Index pour la table `jhi_date_time_wrapper`
--
ALTER TABLE `jhi_date_time_wrapper`
  ADD PRIMARY KEY (`id`);

--
-- Index pour la table `jhi_persistent_audit_event`
--
ALTER TABLE `jhi_persistent_audit_event`
  ADD PRIMARY KEY (`event_id`),
  ADD KEY `idx_persistent_audit_event` (`principal`,`event_date`);

--
-- Index pour la table `jhi_persistent_audit_evt_data`
--
ALTER TABLE `jhi_persistent_audit_evt_data`
  ADD PRIMARY KEY (`event_id`,`name`),
  ADD KEY `idx_persistent_audit_evt_data` (`event_id`);

--
-- Index pour la table `jhi_user`
--
ALTER TABLE `jhi_user`
  ADD PRIMARY KEY (`id`),
  ADD UNIQUE KEY `ux_user_login` (`login`),
  ADD UNIQUE KEY `ux_user_email` (`email`);

--
-- Index pour la table `jhi_user_authority`
--
ALTER TABLE `jhi_user_authority`
  ADD PRIMARY KEY (`user_id`,`authority_name`),
  ADD KEY `fk_authority_name` (`authority_name`);

--
-- Index pour la table `question`
--
ALTER TABLE `question`
  ADD PRIMARY KEY (`id`),
  ADD UNIQUE KEY `ux_question_zone_id` (`zone_id`),
  ADD KEY `fk_question_type_id` (`type_id`),
  ADD KEY `fk_question_exam_id` (`exam_id`);

--
-- Index pour la table `question_type`
--
ALTER TABLE `question_type`
  ADD PRIMARY KEY (`id`);

--
-- Index pour la table `scan`
--
ALTER TABLE `scan`
  ADD PRIMARY KEY (`id`);

--
-- Index pour la table `student`
--
ALTER TABLE `student`
  ADD PRIMARY KEY (`id`);

--
-- Index pour la table `student_exam_sheets`
--
ALTER TABLE `student_exam_sheets`
  ADD PRIMARY KEY (`student_id`,`exam_sheets_id`),
  ADD KEY `fk_student_exam_sheets_exam_sheets_id` (`exam_sheets_id`);

--
-- Index pour la table `student_response`
--
ALTER TABLE `student_response`
  ADD PRIMARY KEY (`id`),
  ADD KEY `fk_student_response_question_id` (`question_id`),
  ADD KEY `fk_student_response_student_id` (`student_id`);

--
-- Index pour la table `template`
--
ALTER TABLE `template`
  ADD PRIMARY KEY (`id`);

--
-- Index pour la table `zone`
--
ALTER TABLE `zone`
  ADD PRIMARY KEY (`id`);

--
-- AUTO_INCREMENT pour les tables déchargées
--

--
-- AUTO_INCREMENT pour la table `comments`
--
ALTER TABLE `comments`
  MODIFY `id` bigint NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT pour la table `course`
--
ALTER TABLE `course`
  MODIFY `id` bigint NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=3;

--
-- AUTO_INCREMENT pour la table `course_group`
--
ALTER TABLE `course_group`
  MODIFY `id` bigint NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT pour la table `exam`
--
ALTER TABLE `exam`
  MODIFY `id` bigint NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT pour la table `exam_sheet`
--
ALTER TABLE `exam_sheet`
  MODIFY `id` bigint NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT pour la table `final_result`
--
ALTER TABLE `final_result`
  MODIFY `id` bigint NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT pour la table `jhi_date_time_wrapper`
--
ALTER TABLE `jhi_date_time_wrapper`
  MODIFY `id` bigint NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT pour la table `jhi_persistent_audit_event`
--
ALTER TABLE `jhi_persistent_audit_event`
  MODIFY `event_id` bigint NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT pour la table `jhi_user`
--
ALTER TABLE `jhi_user`
  MODIFY `id` bigint NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=7;

--
-- AUTO_INCREMENT pour la table `question`
--
ALTER TABLE `question`
  MODIFY `id` bigint NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT pour la table `question_type`
--
ALTER TABLE `question_type`
  MODIFY `id` bigint NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=3;

--
-- AUTO_INCREMENT pour la table `scan`
--
ALTER TABLE `scan`
  MODIFY `id` bigint NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT pour la table `student`
--
ALTER TABLE `student`
  MODIFY `id` bigint NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT pour la table `student_response`
--
ALTER TABLE `student_response`
  MODIFY `id` bigint NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT pour la table `template`
--
ALTER TABLE `template`
  MODIFY `id` bigint NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT pour la table `zone`
--
ALTER TABLE `zone`
  MODIFY `id` bigint NOT NULL AUTO_INCREMENT;

--
-- Contraintes pour les tables déchargées
--

--
-- Contraintes pour la table `comments`
--
ALTER TABLE `comments`
  ADD CONSTRAINT `fk_comments_student_response_id` FOREIGN KEY (`student_response_id`) REFERENCES `student_response` (`id`);

--
-- Contraintes pour la table `course`
--
ALTER TABLE `course`
  ADD CONSTRAINT `fk_course_prof_id` FOREIGN KEY (`prof_id`) REFERENCES `jhi_user` (`id`);

--
-- Contraintes pour la table `course_group`
--
ALTER TABLE `course_group`
  ADD CONSTRAINT `fk_course_group_course_id` FOREIGN KEY (`course_id`) REFERENCES `course` (`id`);

--
-- Contraintes pour la table `course_group_students`
--
ALTER TABLE `course_group_students`
  ADD CONSTRAINT `fk_course_group_students_course_group_id` FOREIGN KEY (`course_group_id`) REFERENCES `course_group` (`id`),
  ADD CONSTRAINT `fk_course_group_students_students_id` FOREIGN KEY (`students_id`) REFERENCES `student` (`id`);

--
-- Contraintes pour la table `exam`
--
ALTER TABLE `exam`
  ADD CONSTRAINT `fk_exam_course_id` FOREIGN KEY (`course_id`) REFERENCES `course` (`id`),
  ADD CONSTRAINT `fk_exam_firstnamezone_id` FOREIGN KEY (`firstnamezone_id`) REFERENCES `zone` (`id`),
  ADD CONSTRAINT `fk_exam_idzone_id` FOREIGN KEY (`idzone_id`) REFERENCES `zone` (`id`),
  ADD CONSTRAINT `fk_exam_namezone_id` FOREIGN KEY (`namezone_id`) REFERENCES `zone` (`id`),
  ADD CONSTRAINT `fk_exam_notezone_id` FOREIGN KEY (`notezone_id`) REFERENCES `zone` (`id`),
  ADD CONSTRAINT `fk_exam_scanfile_id` FOREIGN KEY (`scanfile_id`) REFERENCES `scan` (`id`),
  ADD CONSTRAINT `fk_exam_template_id` FOREIGN KEY (`template_id`) REFERENCES `template` (`id`);

--
-- Contraintes pour la table `exam_sheet`
--
ALTER TABLE `exam_sheet`
  ADD CONSTRAINT `fk_exam_sheet_scan_id` FOREIGN KEY (`scan_id`) REFERENCES `scan` (`id`);

--
-- Contraintes pour la table `final_result`
--
ALTER TABLE `final_result`
  ADD CONSTRAINT `fk_final_result_exam_id` FOREIGN KEY (`exam_id`) REFERENCES `exam` (`id`),
  ADD CONSTRAINT `fk_final_result_student_id` FOREIGN KEY (`student_id`) REFERENCES `student` (`id`);

--
-- Contraintes pour la table `jhi_persistent_audit_evt_data`
--
ALTER TABLE `jhi_persistent_audit_evt_data`
  ADD CONSTRAINT `fk_evt_pers_audit_evt_data` FOREIGN KEY (`event_id`) REFERENCES `jhi_persistent_audit_event` (`event_id`);

--
-- Contraintes pour la table `jhi_user_authority`
--
ALTER TABLE `jhi_user_authority`
  ADD CONSTRAINT `fk_authority_name` FOREIGN KEY (`authority_name`) REFERENCES `jhi_authority` (`name`),
  ADD CONSTRAINT `fk_user_id` FOREIGN KEY (`user_id`) REFERENCES `jhi_user` (`id`);

--
-- Contraintes pour la table `question`
--
ALTER TABLE `question`
  ADD CONSTRAINT `fk_question_exam_id` FOREIGN KEY (`exam_id`) REFERENCES `exam` (`id`),
  ADD CONSTRAINT `fk_question_type_id` FOREIGN KEY (`type_id`) REFERENCES `question_type` (`id`),
  ADD CONSTRAINT `fk_question_zone_id` FOREIGN KEY (`zone_id`) REFERENCES `zone` (`id`);

--
-- Contraintes pour la table `student_exam_sheets`
--
ALTER TABLE `student_exam_sheets`
  ADD CONSTRAINT `fk_student_exam_sheets_exam_sheets_id` FOREIGN KEY (`exam_sheets_id`) REFERENCES `exam_sheet` (`id`),
  ADD CONSTRAINT `fk_student_exam_sheets_student_id` FOREIGN KEY (`student_id`) REFERENCES `student` (`id`);

--
-- Contraintes pour la table `student_response`
--
ALTER TABLE `student_response`
  ADD CONSTRAINT `fk_student_response_question_id` FOREIGN KEY (`question_id`) REFERENCES `question` (`id`),
  ADD CONSTRAINT `fk_student_response_student_id` FOREIGN KEY (`student_id`) REFERENCES `student` (`id`);
COMMIT;

/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
