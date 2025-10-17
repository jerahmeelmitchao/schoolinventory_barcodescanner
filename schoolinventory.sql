-- phpMyAdmin SQL Dump
-- version 5.2.1
-- https://www.phpmyadmin.net/
--
-- Host: 127.0.0.1
-- Generation Time: Oct 17, 2025 at 07:53 AM
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
(1, 'Jerahmeel', 'BSIT2', 'Student'),
(3, 'Jerahme', 'ds', 'Staff');

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
(1, 'Juan Dela Cruz', 'Manager', '09123456789', 3),
(2, 'Maria Santos', 'Supervisor', '09998887777', 2),
(3, 'Paolo Reyes', 'Technician', '09123451234', 3);

-- --------------------------------------------------------

--
-- Table structure for table `items`
--

CREATE TABLE `items` (
  `item_id` int(11) NOT NULL,
  `item_name` varchar(200) NOT NULL,
  `barcode` varchar(100) DEFAULT NULL,
  `category_id` int(11) DEFAULT NULL,
  `quantity` int(11) NOT NULL DEFAULT 0 CHECK (`quantity` >= 0),
  `unit` varchar(50) DEFAULT NULL,
  `date_acquired` date DEFAULT NULL,
  `last_scanned` datetime DEFAULT NULL,
  `serviceability_status` enum('Serviceable','Unserviceable') NOT NULL DEFAULT 'Serviceable',
  `condition_status` enum('OK','Damaged','Disposed') DEFAULT 'OK',
  `storage_location` varchar(100) DEFAULT NULL,
  `availability_status` enum('Available','Unavailable') NOT NULL DEFAULT 'Available',
  `incharge_id` int(11) DEFAULT NULL,
  `added_by` varchar(100) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

--
-- Dumping data for table `items`
--

INSERT INTO `items` (`item_id`, `item_name`, `barcode`, `category_id`, `quantity`, `unit`, `date_acquired`, `last_scanned`, `serviceability_status`, `condition_status`, `storage_location`, `availability_status`, `incharge_id`, `added_by`) VALUES
(1, 'Laptop Acer Aspire', 'ELEC001', 1, 12, 'pcs', '2025-01-12', '2025-10-05 10:00:00', 'Serviceable', 'OK', 'Room 101', 'Available', 1, 'admin'),
(2, 'Projector Epson X300', 'ELEC002', 1, 3, 'pcs', '2025-02-20', '2025-10-08 14:00:00', 'Serviceable', 'Damaged', 'Room 102', 'Unavailable', 1, 'staff1'),
(3, 'Office Chair', 'FUR001', 2, 20, 'pcs', '2025-03-15', '2025-08-10 09:00:00', 'Serviceable', 'OK', 'Room 201', 'Available', 2, 'staff2'),
(4, 'Basketball', 'SPT001', 3, 6, 'pcs', '2025-04-10', '2025-09-25 17:00:00', 'Serviceable', 'OK', 'Gym', 'Available', 3, 'staff1'),
(5, 'Volleyball', 'SPT002', 3, 2, 'pcs', '2025-04-15', NULL, 'Serviceable', 'OK', 'Gym', 'Available', 3, 'staff3'),
(6, 'Printer HP Deskjet', 'ELEC003', 1, 4, 'pcs', '2025-05-01', '2025-07-05 11:30:00', 'Unserviceable', 'Disposed', 'Room 103', 'Unavailable', 1, 'staff1'),
(7, 'Ballpen Blue', 'STA001', 4, 120, 'pcs', '2025-06-10', NULL, 'Serviceable', 'OK', 'Storage A', 'Available', 2, 'staff2'),
(8, 'Hammer', 'TOO001', 5, 1, 'pcs', '2025-07-12', '2025-10-10 09:30:00', 'Serviceable', 'OK', 'Workshop', 'Available', 2, 'admin'),
(9, 'Ruler 12-inch', 'STA002', 4, 10, 'pcs', '2025-08-05', '2025-10-06 13:00:00', 'Serviceable', 'OK', 'Storage A', 'Available', 2, 'staff2'),
(10, 'Badminton Racket', 'SPT003', 3, 4, 'pcs', '2025-09-01', NULL, 'Serviceable', 'OK', 'Gym', 'Available', 3, 'staff3');

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
(1, 1, '2025-10-01 10:00:00'),
(2, 1, '2025-10-02 14:00:00'),
(3, 1, '2025-10-03 15:00:00'),
(4, 2, '2025-10-08 09:00:00'),
(5, 3, '2025-10-10 10:00:00'),
(6, 3, '2025-10-12 11:00:00'),
(7, 4, '2025-09-20 08:00:00'),
(8, 4, '2025-09-21 09:00:00'),
(9, 8, '2025-10-10 10:00:00'),
(10, 9, '2025-10-05 11:00:00'),
(11, 9, '2025-10-07 11:30:00');

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
(1, 'jareth', '123', '2025-10-01 13:01:49');

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
  MODIFY `record_id` bigint(20) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=3;

--
-- AUTO_INCREMENT for table `categories`
--
ALTER TABLE `categories`
  MODIFY `category_id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=9;

--
-- AUTO_INCREMENT for table `incharge`
--
ALTER TABLE `incharge`
  MODIFY `incharge_id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=4;

--
-- AUTO_INCREMENT for table `items`
--
ALTER TABLE `items`
  MODIFY `item_id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=11;

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
