--
-- Copyright (c) 2013 SLL. <http://sll.se>
--
-- This file is part of Invoice-Data.
--
--     Invoice-Data is free software: you can redistribute it and/or modify
--     it under the terms of the GNU Lesser General Public License as published by
--     the Free Software Foundation, either version 3 of the License, or
--     (at your option) any later version.
--
--     Invoice-Data is distributed in the hope that it will be useful,
--     but WITHOUT ANY WARRANTY; without even the implied warranty of
--     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
--     GNU Lesser General Public License for more details.
--
--     You should have received a copy of the GNU Lesser General Public License
--     along with Invoice-Data.  If not, see <http://www.gnu.org/licenses/lgpl.txt>.
--

-- MySQL Administrator dump 1.4
--
-- ------------------------------------------------------
-- Server version	5.5.20


/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8 */;

/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;

--
-- Create user
--

CREATE USER 'vsfuAdmin'@'localhost' IDENTIFIED BY %PASSWORD%;
GRANT ALL PRIVILEGES ON *.* TO 'vsfuAdmin'@'localhost' WITH GRANT OPTION;
--
-- Create schema vsfunderlag
--

CREATE DATABASE IF NOT EXISTS vsfunderlag;
USE vsfunderlag;

--
-- Definition of table `invoice_data`
--

DROP TABLE IF EXISTS `invoice_data`;
CREATE TABLE `invoice_data` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `created_by` varchar(64) NOT NULL,
  `created_timestamp` datetime NOT NULL,
  `end_date` date NOT NULL,
  `payment_responsible` varchar(64) NOT NULL,
  `start_date` date NOT NULL,
  `supplier_id` varchar(64) NOT NULL,
  `total_amount` decimal(8,2) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

--
-- Dumping data for table `invoice_data`
--

/*!40000 ALTER TABLE `invoice_data` DISABLE KEYS */;
/*!40000 ALTER TABLE `invoice_data` ENABLE KEYS */;


--
-- Definition of table `invoice_data_event`
--

DROP TABLE IF EXISTS `invoice_data_event`;
CREATE TABLE `invoice_data_event` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `acknowledged_by` varchar(64) NOT NULL,
  `acknowledged_time` datetime NOT NULL,
  `acknowledgement_id` varchar(64) NOT NULL,
  `created_timestamp` datetime NOT NULL,
  `credit` bit(1) DEFAULT NULL,
  `credited` bit(1) DEFAULT NULL,
  `end_time` datetime NOT NULL,
  `event_id` varchar(64) NOT NULL,
  `healthcare_commission` varchar(64) NOT NULL,
  `healthcare_facility` varchar(64) NOT NULL,
  `payment_responsible` varchar(64) NOT NULL,
  `pending` bit(1) DEFAULT NULL,
  `ref_contract_id` varchar(64) NOT NULL,
  `service_code` varchar(64) NOT NULL,
  `start_time` datetime NOT NULL,
  `supplier_id` varchar(64) NOT NULL,
  `supplier_name` longtext NOT NULL,
  `invoice_data_id` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `invoice_data_event_query_ix_2` (`event_id`),
  KEY `invoice_data_event_query_ix_1` (`supplier_id`,`pending`),
  KEY `invoice_data_event_query_ix_3` (`acknowledgement_id`),
  KEY `FKC29022178FF8CD73` (`invoice_data_id`),
  CONSTRAINT `FKC29022178FF8CD73` FOREIGN KEY (`invoice_data_id`) REFERENCES `invoice_data` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

--
-- Dumping data for table `invoice_data_event`
--

/*!40000 ALTER TABLE `invoice_data_event` DISABLE KEYS */;
/*!40000 ALTER TABLE `invoice_data_event` ENABLE KEYS */;


--
-- Definition of table `invoice_data_event_item`
--

DROP TABLE IF EXISTS `invoice_data_event_item`;
CREATE TABLE `invoice_data_event_item` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `description` longtext NOT NULL,
  `item_id` varchar(64) NOT NULL,
  `price` decimal(8,2) DEFAULT NULL,
  `qty` decimal(8,2) DEFAULT NULL,
  `event_id` bigint(20) NOT NULL,
  PRIMARY KEY (`id`),
  KEY `FK88E0C83BB8B18D8` (`event_id`),
  CONSTRAINT `FK88E0C83BB8B18D8` FOREIGN KEY (`event_id`) REFERENCES `invoice_data_event` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

--
-- Dumping data for table `invoice_data_event_item`
--

/*!40000 ALTER TABLE `invoice_data_event_item` DISABLE KEYS */;
/*!40000 ALTER TABLE `invoice_data_event_item` ENABLE KEYS */;


--
-- Definition of table `invoice_data_pricelist`
--

DROP TABLE IF EXISTS `invoice_data_pricelist`;
CREATE TABLE `invoice_data_pricelist` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `service_code` varchar(64) NOT NULL,
  `supplier_id` varchar(64) NOT NULL,
  `valid_from` date NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `service_code` (`service_code`,`supplier_id`,`valid_from`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

--
-- Dumping data for table `invoice_data_pricelist`
--

/*!40000 ALTER TABLE `invoice_data_pricelist` DISABLE KEYS */;
/*!40000 ALTER TABLE `invoice_data_pricelist` ENABLE KEYS */;


--
-- Definition of table `invoice_data_pricelist_item`
--

DROP TABLE IF EXISTS `invoice_data_pricelist_item`;
CREATE TABLE `invoice_data_pricelist_item` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `item_id` varchar(64) NOT NULL,
  `price` decimal(8,2) NOT NULL,
  `price_list_id` bigint(20) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `item_id` (`item_id`,`price_list_id`),
  KEY `FKED35540EC367E1EB` (`price_list_id`),
  CONSTRAINT `FKED35540EC367E1EB` FOREIGN KEY (`price_list_id`) REFERENCES `invoice_data_pricelist` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

--
-- Dumping data for table `invoice_data_pricelist_item`
--

/*!40000 ALTER TABLE `invoice_data_pricelist_item` DISABLE KEYS */;
/*!40000 ALTER TABLE `invoice_data_pricelist_item` ENABLE KEYS */;




/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
