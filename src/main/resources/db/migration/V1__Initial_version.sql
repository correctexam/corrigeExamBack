-- phpMyAdmin SQL Dump
-- version 5.2.1deb1ubuntu1
-- https://www.phpmyadmin.net/
--
-- Hôte : localhost:3306
-- Généré le : mer. 06 mars 2024 à 16:04
-- Version du serveur : 8.0.36-0ubuntu0.23.10.1
-- Version de PHP : 8.2.10-2ubuntu1

SET SQL_MODE = "NO_AUTO_VALUE_ON_ZERO";
SET AUTOCOMMIT = 0;
START TRANSACTION;
SET time_zone = "+00:00";


/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8mb4 */;

--
-- Base de données : `gradeScopeIstic`
--

-- --------------------------------------------------------

--
-- Structure de la table `answer_2_hybrid_graded_comment`
--

CREATE TABLE `answer_2_hybrid_graded_comment` (
  `id` bigint NOT NULL,
  `step_value` int DEFAULT NULL,
  `hybridcomments_id` bigint DEFAULT NULL,
  `student_response_id` bigint DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- --------------------------------------------------------

--
-- Structure de la table `comments`
--

CREATE TABLE `comments` (
  `id` bigint NOT NULL,
  `zonegeneratedid` varchar(255) DEFAULT NULL,
  `json_data` longtext,
  `student_response_id` bigint DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- --------------------------------------------------------

--
-- Structure de la table `course`
--

CREATE TABLE `course` (
  `id` bigint NOT NULL,
  `name` varchar(255) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

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
-- Structure de la table `course_prof`
--

CREATE TABLE `course_prof` (
  `prof_id` bigint NOT NULL,
  `course_id` bigint NOT NULL
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

-- --------------------------------------------------------

--
-- Structure de la table `databasechangelog`
--

CREATE TABLE `databasechangelog` (
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

-- --------------------------------------------------------

--
-- Structure de la table `databasechangeloglock`
--

CREATE TABLE `databasechangeloglock` (
  `ID` int NOT NULL,
  `LOCKED` bit(1) NOT NULL,
  `LOCKGRANTED` datetime DEFAULT NULL,
  `LOCKEDBY` varchar(255) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

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
  `exam_id` bigint DEFAULT NULL,
  `frozen` bit(1) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- --------------------------------------------------------

--
-- Structure de la table `graded_comment`
--

CREATE TABLE `graded_comment` (
  `id` bigint NOT NULL,
  `zonegeneratedid` varchar(255) DEFAULT NULL,
  `text` varchar(255) DEFAULT NULL,
  `description` longtext,
  `grade` int DEFAULT NULL,
  `question_id` bigint DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- --------------------------------------------------------

--
-- Structure de la table `hybrid_graded_comment`
--

CREATE TABLE `hybrid_graded_comment` (
  `id` bigint NOT NULL,
  `description` longtext,
  `grade` int DEFAULT NULL,
  `relative` bit(1) DEFAULT NULL,
  `step` int DEFAULT NULL,
  `text` varchar(255) DEFAULT NULL,
  `question_id` bigint DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- --------------------------------------------------------

--
-- Structure de la table `jhi_authority`
--

CREATE TABLE `jhi_authority` (
  `name` varchar(50) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

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
  `created_date` timestamp NULL DEFAULT NULL,
  `reset_date` timestamp NULL DEFAULT NULL,
  `last_modified_by` varchar(50) DEFAULT NULL,
  `last_modified_date` timestamp NULL DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- --------------------------------------------------------

--
-- Structure de la table `jhi_user_authority`
--

CREATE TABLE `jhi_user_authority` (
  `user_id` bigint NOT NULL,
  `authority_name` varchar(50) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- --------------------------------------------------------

--
-- Structure de la table `question`
--

CREATE TABLE `question` (
  `id` bigint NOT NULL,
  `numero` int NOT NULL,
  `point` int DEFAULT NULL,
  `step` int DEFAULT NULL,
  `valid_expression` varchar(255) DEFAULT NULL,
  `libelle` varchar(255) DEFAULT NULL,
  `grade_type` varchar(255) DEFAULT NULL,
  `zone_id` bigint DEFAULT NULL,
  `type_id` bigint DEFAULT NULL,
  `exam_id` bigint DEFAULT NULL,
  `defaultpoint` int DEFAULT NULL
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
  `star` bit(1) DEFAULT NULL,
  `worststar` bit(1) DEFAULT b'0',
  `question_id` bigint DEFAULT NULL,
  `sheet_id` bigint DEFAULT NULL,
  `lastmodified` datetime(6) DEFAULT NULL,
  `correctedby_id` bigint DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- --------------------------------------------------------

--
-- Structure de la table `student_response_gradedcomments`
--

CREATE TABLE `student_response_gradedcomments` (
  `gradedcomments_id` bigint NOT NULL,
  `student_response_id` bigint NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- --------------------------------------------------------

--
-- Structure de la table `student_response_textcomments`
--

CREATE TABLE `student_response_textcomments` (
  `textcomments_id` bigint NOT NULL,
  `student_response_id` bigint NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- --------------------------------------------------------

--
-- Structure de la table `template`
--

CREATE TABLE `template` (
  `id` bigint NOT NULL,
  `name` varchar(255) NOT NULL,
  `content` longblob,
  `content_content_type` varchar(255) DEFAULT NULL,
  `mark` bit(1) DEFAULT NULL,
  `auto_map_student_copy_to_list` bit(1) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- --------------------------------------------------------

--
-- Structure de la table `text_comment`
--

CREATE TABLE `text_comment` (
  `id` bigint NOT NULL,
  `text` varchar(255) DEFAULT NULL,
  `description` longtext,
  `zonegeneratedid` varchar(255) DEFAULT NULL,
  `question_id` bigint DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- --------------------------------------------------------

--
-- Structure de la table `zone`
--

CREATE TABLE `zone` (
  `id` bigint NOT NULL,
  `page_number` int DEFAULT NULL,
  `x_init` int DEFAULT NULL,
  `y_init` int DEFAULT NULL,
  `width` int DEFAULT NULL,
  `height` int DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

--
-- Index pour les tables déchargées
--

--
-- Index pour la table `answer_2_hybrid_graded_comment`
--
ALTER TABLE `answer_2_hybrid_graded_comment`
  ADD PRIMARY KEY (`id`),
  ADD UNIQUE KEY `UniqueHybridcommentsIdAndStudentResponseId` (`student_response_id`,`hybridcomments_id`),
  ADD KEY `FK9ijm3itpjwpgf534m94df8dt6` (`hybridcomments_id`);

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
  ADD PRIMARY KEY (`id`);

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
-- Index pour la table `course_prof`
--
ALTER TABLE `course_prof`
  ADD PRIMARY KEY (`course_id`,`prof_id`),
  ADD KEY `fk_course_prof_prof_id` (`prof_id`);

--
-- Index pour la table `DATABASECHANGELOGLOCK`
--
ALTER TABLE `DATABASECHANGELOGLOCK`
  ADD PRIMARY KEY (`ID`);

--
-- Index pour la table `databasechangeloglock`
--
ALTER TABLE `databasechangeloglock`
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
  ADD UNIQUE KEY `cannot ahve two finalresult for the same student and same exam` (`student_id`,`exam_id`),
  ADD KEY `fk_final_result_student_id` (`student_id`),
  ADD KEY `fk_final_result_exam_id` (`exam_id`);

--
-- Index pour la table `graded_comment`
--
ALTER TABLE `graded_comment`
  ADD PRIMARY KEY (`id`),
  ADD KEY `fk_graded_comment_question_id` (`question_id`);

--
-- Index pour la table `hybrid_graded_comment`
--
ALTER TABLE `hybrid_graded_comment`
  ADD PRIMARY KEY (`id`),
  ADD KEY `FKrrl2y7dngtnqlklwt0scsy8jq` (`question_id`);

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
  ADD UNIQUE KEY `question_id` (`question_id`,`sheet_id`),
  ADD KEY `fk_student_response_question_id` (`question_id`),
  ADD KEY `fk_student_response_sheet_id` (`sheet_id`),
  ADD KEY `FKinrpshecm7c6aiqo6000ju87c` (`correctedby_id`);

--
-- Index pour la table `student_response_gradedcomments`
--
ALTER TABLE `student_response_gradedcomments`
  ADD PRIMARY KEY (`student_response_id`,`gradedcomments_id`),
  ADD UNIQUE KEY `gradedcomments_id` (`gradedcomments_id`,`student_response_id`),
  ADD KEY `fk_student_response_gradedcomments_gradedcomments_id` (`gradedcomments_id`);

--
-- Index pour la table `student_response_textcomments`
--
ALTER TABLE `student_response_textcomments`
  ADD PRIMARY KEY (`student_response_id`,`textcomments_id`),
  ADD KEY `fk_student_response_textcomments_textcomments_id` (`textcomments_id`);

--
-- Index pour la table `template`
--
ALTER TABLE `template`
  ADD PRIMARY KEY (`id`);

--
-- Index pour la table `text_comment`
--
ALTER TABLE `text_comment`
  ADD PRIMARY KEY (`id`),
  ADD KEY `fk_text_comment_question_id` (`question_id`);

--
-- Index pour la table `zone`
--
ALTER TABLE `zone`
  ADD PRIMARY KEY (`id`);

--
-- AUTO_INCREMENT pour les tables déchargées
--

--
-- AUTO_INCREMENT pour la table `answer_2_hybrid_graded_comment`
--
ALTER TABLE `answer_2_hybrid_graded_comment`
  MODIFY `id` bigint NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT pour la table `comments`
--
ALTER TABLE `comments`
  MODIFY `id` bigint NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT pour la table `course`
--
ALTER TABLE `course`
  MODIFY `id` bigint NOT NULL AUTO_INCREMENT;

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
-- AUTO_INCREMENT pour la table `graded_comment`
--
ALTER TABLE `graded_comment`
  MODIFY `id` bigint NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT pour la table `hybrid_graded_comment`
--
ALTER TABLE `hybrid_graded_comment`
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
  MODIFY `id` bigint NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT pour la table `question`
--
ALTER TABLE `question`
  MODIFY `id` bigint NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT pour la table `question_type`
--
ALTER TABLE `question_type`
  MODIFY `id` bigint NOT NULL AUTO_INCREMENT;

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
-- AUTO_INCREMENT pour la table `text_comment`
--
ALTER TABLE `text_comment`
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
-- Contraintes pour la table `answer_2_hybrid_graded_comment`
--
ALTER TABLE `answer_2_hybrid_graded_comment`
  ADD CONSTRAINT `FK9ijm3itpjwpgf534m94df8dt6` FOREIGN KEY (`hybridcomments_id`) REFERENCES `hybrid_graded_comment` (`id`),
  ADD CONSTRAINT `FKqxflsw40s622dtyt99himou2k` FOREIGN KEY (`student_response_id`) REFERENCES `student_response` (`id`);

--
-- Contraintes pour la table `comments`
--
ALTER TABLE `comments`
  ADD CONSTRAINT `fk_comments_student_response_id` FOREIGN KEY (`student_response_id`) REFERENCES `student_response` (`id`);

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
-- Contraintes pour la table `course_prof`
--
ALTER TABLE `course_prof`
  ADD CONSTRAINT `fk_course_prof_course_id` FOREIGN KEY (`course_id`) REFERENCES `course` (`id`),
  ADD CONSTRAINT `fk_course_prof_prof_id` FOREIGN KEY (`prof_id`) REFERENCES `jhi_user` (`id`);

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
-- Contraintes pour la table `graded_comment`
--
ALTER TABLE `graded_comment`
  ADD CONSTRAINT `fk_graded_comment_question_id` FOREIGN KEY (`question_id`) REFERENCES `question` (`id`);

--
-- Contraintes pour la table `hybrid_graded_comment`
--
ALTER TABLE `hybrid_graded_comment`
  ADD CONSTRAINT `FKrrl2y7dngtnqlklwt0scsy8jq` FOREIGN KEY (`question_id`) REFERENCES `question` (`id`);

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
  ADD CONSTRAINT `fk_student_response_sheet_id` FOREIGN KEY (`sheet_id`) REFERENCES `exam_sheet` (`id`),
  ADD CONSTRAINT `FKinrpshecm7c6aiqo6000ju87c` FOREIGN KEY (`correctedby_id`) REFERENCES `jhi_user` (`id`);

--
-- Contraintes pour la table `student_response_gradedcomments`
--
ALTER TABLE `student_response_gradedcomments`
  ADD CONSTRAINT `fk_student_response_gradedcomments_gradedcomments_id` FOREIGN KEY (`gradedcomments_id`) REFERENCES `graded_comment` (`id`),
  ADD CONSTRAINT `fk_student_response_gradedcomments_student_response_id` FOREIGN KEY (`student_response_id`) REFERENCES `student_response` (`id`);

--
-- Contraintes pour la table `student_response_textcomments`
--
ALTER TABLE `student_response_textcomments`
  ADD CONSTRAINT `fk_student_response_textcomments_student_response_id` FOREIGN KEY (`student_response_id`) REFERENCES `student_response` (`id`),
  ADD CONSTRAINT `fk_student_response_textcomments_textcomments_id` FOREIGN KEY (`textcomments_id`) REFERENCES `text_comment` (`id`);

--
-- Contraintes pour la table `text_comment`
--
ALTER TABLE `text_comment`
  ADD CONSTRAINT `fk_text_comment_question_id` FOREIGN KEY (`question_id`) REFERENCES `question` (`id`);
COMMIT;

/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;


INSERT INTO `jhi_user` (`id`, `login`, `password_hash`, `first_name`, `last_name`, `email`, `image_url`, `activated`, `lang_key`, `activation_key`, `reset_key`, `created_by`, `created_date`, `reset_date`, `last_modified_by`, `last_modified_date`) VALUES
(1, 'system', '$2a$10$mE.qmcV0mFU5NcKh73TZx.z4ueI/.bDWbj0T1BYyqP481kGGarKLG', 'System', 'System', 'system@localhost', '', b'1', 'fr', NULL, NULL, 'system', NULL, NULL, 'system', NULL),
(2, 'anonymoususer', '$2a$10$j8S5d7Sr7.8VTOYNviDPOeWX8KcYILUVJBsYV83Y5NtECayypx9lO', 'Anonymous', 'User', 'anonymous@localhost', '', b'1', 'fr', NULL, NULL, 'system', NULL, NULL, 'system', NULL),
(3, 'admin', '$2a$10$gSAhZrxMllrbgj/kkK9UceBPpChGWJA7SYIb1Mqo.n5aNLq1/oRrC', 'Administrator', 'Administrator', 'admin@localhost', '', b'1', 'fr', NULL, NULL, 'system', NULL, NULL, 'system', NULL),
(4, 'user', '$2a$10$VEjxo0jq2YG9Rbk2HmX9S.k1uZBGYUHdUcid3g/vfiEl7lwWgOH/K', 'User', 'User', 'user@localhost', '', b'1', 'fr', NULL, NULL, 'system', NULL, NULL, 'system', NULL),

-- --------------------------------------------------------

--
-- Déchargement des données de la table `jhi_authority`
--

INSERT INTO `jhi_authority` (`name`) VALUES
('ROLE_ADMIN'),
('ROLE_USER');
--
-- Déchargement des données de la table `jhi_user_authority`
--

INSERT INTO `jhi_user_authority` (`user_id`, `authority_name`) VALUES
(1, 'ROLE_ADMIN'),
(3, 'ROLE_ADMIN'),
(1, 'ROLE_USER'),
(3, 'ROLE_USER'),
(4, 'ROLE_USER'),

COMMIT;



