-- phpMyAdmin SQL Dump
-- version 5.2.1
-- https://www.phpmyadmin.net/
--
-- Host: 127.0.0.1
-- Generation Time: Dec 06, 2025 at 10:36 AM
-- Server version: 10.4.32-MariaDB
-- PHP Version: 8.2.12

SET SQL_MODE = "NO_AUTO_VALUE_ON_ZERO";
START TRANSACTION;
SET time_zone = "+00:00";


/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8mb4 */;

--
-- Database: `schoolinventory`
--

-- --------------------------------------------------------

--
-- Table structure for table `audit_log`
--

CREATE TABLE `audit_log` (
  `audit_id` bigint(20) NOT NULL,
  `event_time` timestamp NOT NULL DEFAULT current_timestamp(),
  `user_who` varchar(100) DEFAULT 'system',
  `event_type` varchar(50) DEFAULT NULL,
  `description` text DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- --------------------------------------------------------

--
-- Table structure for table `borrowers`
--

CREATE TABLE `borrowers` (
  `borrower_id` int(11) NOT NULL,
  `borrower_name` varchar(150) NOT NULL,
  `position` varchar(100) DEFAULT NULL,
  `borrower_type` enum('Student','Teacher','Staff') NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

--
-- Dumping data for table `borrowers`
--

INSERT INTO `borrowers` (`borrower_id`, `borrower_name`, `position`, `borrower_type`) VALUES
(1, 'John Dela Cruz', 'BSIT3', 'Student'),
(2, 'Mary Ann', 'Teacher I', 'Teacher'),
(3, 'James Tan', 'Technician', 'Staff');

-- --------------------------------------------------------

--
-- Table structure for table `borrow_records`
--

CREATE TABLE `borrow_records` (
  `record_id` bigint(20) NOT NULL,
  `item_id` int(11) NOT NULL,
  `borrower_id` int(11) NOT NULL,
  `borrow_date` datetime NOT NULL DEFAULT current_timestamp(),
  `return_date` datetime DEFAULT NULL,
  `quantity_borrowed` int(11) NOT NULL CHECK (`quantity_borrowed` > 0),
  `status` enum('Borrowed','Returned','Overdue','Cancelled') NOT NULL DEFAULT 'Borrowed',
  `remarks` varchar(255) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

--
-- Dumping data for table `borrow_records`
--

INSERT INTO `borrow_records` (`record_id`, `item_id`, `borrower_id`, `borrow_date`, `return_date`, `quantity_borrowed`, `status`, `remarks`) VALUES
(1, 1, 1, '2025-09-01 09:00:00', '2025-09-03 15:00:00', 1, 'Returned', 'Routine use'),
(2, 3, 2, '2025-09-10 08:30:00', '2025-09-14 10:00:00', 2, 'Returned', 'Meeting room'),
(3, 5, 1, '2025-09-15 11:00:00', NULL, 1, 'Borrowed', 'Sports event'),
(4, 8, 3, '2025-10-10 13:00:00', NULL, 1, 'Borrowed', 'Workshop use');

-- --------------------------------------------------------

--
-- Table structure for table `categories`
--

CREATE TABLE `categories` (
  `category_id` int(11) NOT NULL,
  `category_name` varchar(100) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

--
-- Dumping data for table `categories`
--

INSERT INTO `categories` (`category_id`, `category_name`) VALUES
(1, 'Electronics'),
(2, 'Furniture'),
(3, 'Sports'),
(4, 'Stationery'),
(5, 'Tools');

-- --------------------------------------------------------

--
-- Table structure for table `incharge`
--

CREATE TABLE `incharge` (
  `incharge_id` int(11) NOT NULL,
  `incharge_name` varchar(150) NOT NULL,
  `position` varchar(100) DEFAULT NULL,
  `contact_info` varchar(150) DEFAULT NULL,
  `assigned_category_id` int(11) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

--
-- Dumping data for table `incharge`
--

INSERT INTO `incharge` (`incharge_id`, `incharge_name`, `position`, `contact_info`, `assigned_category_id`) VALUES
(1, 'Mark Santos', 'Electronics Supervisor', '09121234567', 1),
(2, 'Anna Lopez', 'Furniture Manager', '09221234567', 2),
(3, 'Carlos Reyes', 'Gym Attendant', '09331234567', 3),
(4, 'Lisa Ramos', 'Inventory Clerk', '09441234567', 4),
(5, 'Ben Castillo', 'Workshop Lead', '09551234567', 5);

-- --------------------------------------------------------

--
-- Table structure for table `items`
--

CREATE TABLE `items` (
  `item_id` int(11) NOT NULL,
  `item_name` varchar(200) NOT NULL,
  `barcode` varchar(100) DEFAULT NULL,
  `category_id` int(11) DEFAULT NULL,
  `unit` varchar(50) DEFAULT NULL,
  `date_acquired` date DEFAULT NULL,
  `last_scanned` datetime DEFAULT NULL,
  `storage_location` varchar(100) DEFAULT NULL,
  `incharge_id` int(11) DEFAULT NULL,
  `added_by` varchar(100) DEFAULT NULL,
  `status` enum('Available','Damaged','Borrowed','Missing','Disposed') NOT NULL DEFAULT 'Available'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

--
-- Dumping data for table `items`
--

INSERT INTO `items` (`item_id`, `item_name`, `barcode`, `category_id`, `unit`, `date_acquired`, `last_scanned`, `storage_location`, `incharge_id`, `added_by`, `status`) VALUES
(1, 'Laptop Dell Inspiron', 'ELEC001', 1, 'pcs', '2025-01-10', '2025-10-10 09:30:00', 'Room 101', 1, 'admin', 'Available'),
(2, 'Smart TV Samsung', 'ELEC002', 1, 'pcs', '2025-02-15', '2025-08-25 10:00:00', 'AV Room', 1, 'admin', 'Available'),
(3, 'Office Table', 'FUR001', 2, 'pcs', '2025-03-20', '2025-09-05 14:00:00', 'Room 201', 2, 'admin', 'Available'),
(4, 'Whiteboard', 'FUR002', 2, 'pcs', '2025-04-02', NULL, 'Room 202', 2, 'staff1', 'Available'),
(5, 'Basketball', 'SPT001', 3, 'pcs', '2025-05-01', '2025-07-22 16:00:00', 'Gym', 3, 'staff1', 'Available'),
(6, 'Volleyball Net', 'SPT002', 3, 'pcs', '2025-06-15', NULL, 'Gym', 3, 'staff2', 'Available'),
(7, 'Bond Paper A4', 'STA001', 4, 'sheets', '2025-07-01', '2025-09-30 11:00:00', 'Storage Room', 4, 'staff2', 'Available'),
(8, 'Hammer', 'TOO001', 5, 'pcs', '2025-08-10', '2025-10-05 09:30:00', 'Workshop', 5, 'admin', 'Available'),
(9, 'Screwdriver Set', 'TOO002', 5, 'sets', '2025-09-01', NULL, 'Workshop', 5, 'admin', 'Available'),
(10, 'Printer Canon LBP', 'ELEC003', 1, 'pcs', '2025-09-15', '2025-10-01 08:45:00', 'Room 103', 1, 'staff1', 'Available');

-- --------------------------------------------------------

--
-- Table structure for table `scan_log`
--

CREATE TABLE `scan_log` (
  `scan_id` bigint(20) NOT NULL,
  `item_id` int(11) NOT NULL,
  `scan_date` datetime DEFAULT current_timestamp()
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `scan_log`
--

INSERT INTO `scan_log` (`scan_id`, `item_id`, `scan_date`) VALUES
(1, 1, '2025-05-02 09:00:00'),
(2, 1, '2025-06-12 14:30:00'),
(3, 2, '2025-07-15 13:00:00'),
(4, 3, '2025-08-22 10:00:00'),
(5, 5, '2025-08-25 11:00:00'),
(6, 7, '2025-09-10 09:15:00'),
(7, 8, '2025-10-05 09:30:00'),
(8, 10, '2025-10-10 14:00:00');

-- --------------------------------------------------------

--
-- Table structure for table `users`
--

CREATE TABLE `users` (
  `id` int(11) NOT NULL,
  `username` varchar(50) NOT NULL,
  `password` varchar(255) NOT NULL,
  `created_at` timestamp NOT NULL DEFAULT current_timestamp()
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `users`
--

INSERT INTO `users` (`id`, `username`, `password`, `created_at`) VALUES
(1, 'administrator', 'Admin_123', '2025-10-01 13:01:49');

--
-- Indexes for dumped tables
--

--
-- Indexes for table `audit_log`
--
ALTER TABLE `audit_log`
  ADD PRIMARY KEY (`audit_id`);

--
-- Indexes for table `borrowers`
--
ALTER TABLE `borrowers`
  ADD PRIMARY KEY (`borrower_id`);

--
-- Indexes for table `borrow_records`
--
ALTER TABLE `borrow_records`
  ADD PRIMARY KEY (`record_id`),
  ADD KEY `idx_borrow_item` (`item_id`),
  ADD KEY `idx_borrow_borrower` (`borrower_id`);

--
-- Indexes for table `categories`
--
ALTER TABLE `categories`
  ADD PRIMARY KEY (`category_id`),
  ADD UNIQUE KEY `category_name` (`category_name`);

--
-- Indexes for table `incharge`
--
ALTER TABLE `incharge`
  ADD PRIMARY KEY (`incharge_id`),
  ADD KEY `assigned_category_id` (`assigned_category_id`);

--
-- Indexes for table `items`
--
ALTER TABLE `items`
  ADD PRIMARY KEY (`item_id`),
  ADD UNIQUE KEY `barcode` (`barcode`),
  ADD KEY `fk_items_incharge` (`incharge_id`),
  ADD KEY `idx_items_category` (`category_id`),
  ADD KEY `idx_items_barcode` (`barcode`);

--
-- Indexes for table `scan_log`
--
ALTER TABLE `scan_log`
  ADD PRIMARY KEY (`scan_id`),
  ADD KEY `item_id` (`item_id`);

--
-- Indexes for table `users`
--
ALTER TABLE `users`
  ADD PRIMARY KEY (`id`),
  ADD UNIQUE KEY `username` (`username`);

--
-- AUTO_INCREMENT for dumped tables
--

--
-- AUTO_INCREMENT for table `audit_log`
--
ALTER TABLE `audit_log`
  MODIFY `audit_id` bigint(20) NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT for table `borrowers`
--
ALTER TABLE `borrowers`
  MODIFY `borrower_id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=4;

--
-- AUTO_INCREMENT for table `borrow_records`
--
ALTER TABLE `borrow_records`
  MODIFY `record_id` bigint(20) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=5;

--
-- AUTO_INCREMENT for table `categories`
--
ALTER TABLE `categories`
  MODIFY `category_id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=9;

--
-- AUTO_INCREMENT for table `incharge`
--
ALTER TABLE `incharge`
  MODIFY `incharge_id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=6;

--
-- AUTO_INCREMENT for table `items`
--
ALTER TABLE `items`
  MODIFY `item_id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=13;

--
-- AUTO_INCREMENT for table `scan_log`
--
ALTER TABLE `scan_log`
  MODIFY `scan_id` bigint(20) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=12;

--
-- AUTO_INCREMENT for table `users`
--
ALTER TABLE `users`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=2;

--
-- Constraints for dumped tables
--

--
-- Constraints for table `borrow_records`
--
ALTER TABLE `borrow_records`
  ADD CONSTRAINT `fk_borrow_borrower` FOREIGN KEY (`borrower_id`) REFERENCES `borrowers` (`borrower_id`) ON UPDATE CASCADE,
  ADD CONSTRAINT `fk_borrow_item` FOREIGN KEY (`item_id`) REFERENCES `items` (`item_id`) ON UPDATE CASCADE;

--
-- Constraints for table `incharge`
--
ALTER TABLE `incharge`
  ADD CONSTRAINT `incharge_ibfk_1` FOREIGN KEY (`assigned_category_id`) REFERENCES `categories` (`category_id`) ON DELETE SET NULL ON UPDATE CASCADE;

--
-- Constraints for table `items`
--
ALTER TABLE `items`
  ADD CONSTRAINT `fk_items_category` FOREIGN KEY (`category_id`) REFERENCES `categories` (`category_id`) ON DELETE SET NULL ON UPDATE CASCADE,
  ADD CONSTRAINT `fk_items_incharge` FOREIGN KEY (`incharge_id`) REFERENCES `incharge` (`incharge_id`) ON DELETE SET NULL ON UPDATE CASCADE;

--
-- Constraints for table `scan_log`
--
ALTER TABLE `scan_log`
  ADD CONSTRAINT `scan_log_ibfk_1` FOREIGN KEY (`item_id`) REFERENCES `items` (`item_id`) ON DELETE CASCADE;
COMMIT;

/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
