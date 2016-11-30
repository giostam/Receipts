CREATE TABLE `users` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `username` varchar(45) NOT NULL,
  `password` varchar(45) NOT NULL,
  `firstname` varchar(45) DEFAULT NULL,
  `surname` varchar(45) DEFAULT NULL,
  `can_import` int(11) NOT NULL DEFAULT '0',
  `can_export` int(11) NOT NULL DEFAULT '0',
  `can_delete` int(11) NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8;

INSERT INTO users (username, password, firstname, can_import, can_export, can_delete) 
VALUES ('admin', 'admin', 'Administrator', 1, 1, 1);

CREATE TABLE `autocomplete_products` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `product` varchar(45) NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8;

CREATE TABLE `stores` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `store_id` varchar(6) NOT NULL,
  `store_date` date NOT NULL,
  `import_date` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `user_id` int(11) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `user_store_fk_idx` (`user_id`),
  CONSTRAINT `user_store_fk` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE SET NULL ON UPDATE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8;

CREATE TABLE `receipts` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `receipt_time` varchar(5) DEFAULT NULL,
  `receipt_img` mediumblob,
  `store_fk` int(11) NOT NULL,
  `user_id` int(11) DEFAULT NULL,
  `complete` int(1) DEFAULT '0',
  `img_name` varchar(45) NOT NULL,
  `complete_date` datetime DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `store_receipt_fk_idx` (`store_fk`),
  KEY `user_receipt_fk_idx` (`user_id`),
  CONSTRAINT `store_receipt_fk` FOREIGN KEY (`store_fk`) REFERENCES `stores` (`id`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `user_receipt_fk` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE SET NULL ON UPDATE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8;

CREATE TABLE `products` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `product_name` varchar(180) NOT NULL,
  `product_price` decimal(10,2) NOT NULL,
  `product_quantity` decimal(5,3) NOT NULL,
  `receipt_fk` int(11) NOT NULL,
  `user_id` int(11) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `receipt_product_fk_idx` (`receipt_fk`),
  KEY `user_product_fk_idx` (`user_id`),
  CONSTRAINT `receipt_product_fk` FOREIGN KEY (`receipt_fk`) REFERENCES `receipts` (`id`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `user_product_fk` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE SET NULL ON UPDATE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8;

