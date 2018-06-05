CREATE DATABASE `game` /*!40100 DEFAULT CHARACTER SET utf8 */;

USE `game`;

CREATE TABLE `subreddits` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `name` varchar(20) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `id_UNIQUE` (`id`),
  UNIQUE KEY `name_UNIQUE` (`name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE `questions` (
  `id` varchar(6) NOT NULL,
  `question` varchar(300) NOT NULL,
  `answer` varchar(20) NOT NULL,
  PRIMARY KEY (`id`) 
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='This table contains all questions that are used for the game. Datatype length limits are determined by the official guidelines of the website "https://www.reddit.com".';

CREATE TABLE `users` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `name` varchar(45) NOT NULL,
  `password` varchar(64) NOT NULL,
  `oldest_answered` varchar(6) DEFAULT NULL,
  `newest_answered` varchar(6) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `id_UNIQUE` (`id`),
  UNIQUE KEY `name_UNIQUE` (`name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE `predictions` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `question` varchar(6) NOT NULL,
  `answer` varchar(20) NOT NULL,
  `correct` bit(1) NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE `answers` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `responder` int(11) NOT NULL DEFAULT '1',
  `question` varchar(6) NOT NULL,
  `answer` varchar(20) NOT NULL,
  `correct` bit(1) NOT NULL,
  `difficulty` varchar(6) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `id_UNIQUE` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

/*INSERT INTO `game`.`users`
(`name`, `password`)
VALUES
("AI", "lgAwf1aLsYv4Drb2dobZDTYe2AikhRW63b4XUH4H9ayWabY0kRH1gdUf0nm8lzWm");*/

