CREATE DATABASE IF NOT EXISTS `paganini`
  DEFAULT CHARACTER SET = utf8mb4
  DEFAULT COLLATE = utf8mb4_unicode_ci;

USE `paganini`;

CREATE TABLE `user` (
  `id` int NOT NULL AUTO_INCREMENT,
  `nombre` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `apellido` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `correo` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL,
  `telefono` varchar(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `cognito_username` varchar(128) COLLATE utf8mb4_unicode_ci NOT NULL,
  `created_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `codigo_qr` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci,
  `saldo` decimal(15,2) NOT NULL DEFAULT '0.00',
  PRIMARY KEY (`id`),
  UNIQUE KEY `Correo` (`correo`),
  UNIQUE KEY `cognito_username` (`cognito_username`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;


CREATE TABLE `device_token` (
  `id` int NOT NULL AUTO_INCREMENT,
  `user_id` int NOT NULL,
  `plataforma` enum('ANDROID','IOS') COLLATE utf8mb4_unicode_ci NOT NULL,
  `token` varchar(512) COLLATE utf8mb4_unicode_ci NOT NULL,
  `endpoint_arn` varchar(512) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `estado` enum('activo','inactivo') COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'activo',
  `created_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_token` (`token`),
  KEY `idx_dt_user` (`user_id`),
  CONSTRAINT `fk_dt_user` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;


CREATE TABLE `payment_request` (
  `id` int NOT NULL AUTO_INCREMENT,
  `requester_id` int NOT NULL,
  `amount` decimal(15,2) NOT NULL,
  `payload` varchar(512) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `qr_base64` longtext COLLATE utf8mb4_unicode_ci,
  `status` enum('activo','inactivo') COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'activo',
  `created_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_pr_requester` (`requester_id`),
  CONSTRAINT `fk_pr_requester` FOREIGN KEY (`requester_id`) REFERENCES `user` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `user_contact` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `owner_id` int NOT NULL,
  `contact_id` int NOT NULL,
  `status` enum('PENDING','ACCEPTED','BLOCKED') COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'ACCEPTED',
  `created_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_owner_contact` (`owner_id`,`contact_id`),
  KEY `fk_uc_contact` (`contact_id`),
  CONSTRAINT `fk_uc_contact` FOREIGN KEY (`contact_id`) REFERENCES `user` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_uc_owner` FOREIGN KEY (`owner_id`) REFERENCES `user` (`id`) ON DELETE CASCADE,
  CONSTRAINT `chk_owner_neq_contact` CHECK ((`owner_id` <> `contact_id`))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `metodo_pago` (
  `Id` int NOT NULL AUTO_INCREMENT,
  `UserId` int NOT NULL,
  `Tipo` enum('tarjeta','cuentabanco','ewallet') COLLATE utf8mb4_unicode_ci NOT NULL,
  `Estado` enum('activo','inactivo') COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'inactivo',
  PRIMARY KEY (`Id`),
  KEY `idx_metodopago_user` (`UserId`),
  CONSTRAINT `fk_metodopago_user` FOREIGN KEY (`UserId`) REFERENCES `User` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `transacciones` (
  `Id` int NOT NULL AUTO_INCREMENT,
  `SenderId` int DEFAULT NULL,
  `ReceiverId` int DEFAULT NULL,
  `MetodoPagoId` int DEFAULT NULL,
  `Tipo` enum('envio','recibo','recarga','retiro') COLLATE utf8mb4_unicode_ci NOT NULL,
  `Origen` enum('correo','qr','qr_monto') COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `Monto` decimal(18,2) NOT NULL,
  `Estado` enum('completado','pendiente','fallido') COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'pendiente',
  `CreatedAt` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `UpdatedAt` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`Id`),
  KEY `idx_trans_sender` (`SenderId`),
  KEY `idx_trans_receiver` (`ReceiverId`),
  KEY `idx_trans_metodo` (`MetodoPagoId`),
  CONSTRAINT `fk_trans_metodo` FOREIGN KEY (`MetodoPagoId`) REFERENCES `metodo_pago` (`Id`) ON DELETE RESTRICT,
  CONSTRAINT `fk_trans_receiver` FOREIGN KEY (`ReceiverId`) REFERENCES `user` (`id`) ON DELETE SET NULL,
  CONSTRAINT `fk_trans_sender` FOREIGN KEY (`SenderId`) REFERENCES `user` (`id`) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `Criptocoin` (
  `id` int NOT NULL AUTO_INCREMENT,
  `abreviacion` varchar(6) COLLATE utf8mb4_unicode_ci NOT NULL,
  `nombre` varchar(30) COLLATE utf8mb4_unicode_ci NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=6 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;


CREATE TABLE `ewallet` (
  `MetodoPagoId` int NOT NULL,
  `Direccion` varchar(50) COLLATE utf8mb4_unicode_ci NOT NULL,
  `criptocoinId` int NOT NULL,
  PRIMARY KEY (`MetodoPagoId`),
  KEY `fk_ewallet_criptocoin` (`criptocoinId`),
  CONSTRAINT `fk_ewallet_criptocoin` FOREIGN KEY (`criptocoinId`) REFERENCES `Criptocoin` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_ewallet_metodopago` FOREIGN KEY (`MetodoPagoId`) REFERENCES `metodo_pago` (`Id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;


INSERT INTO `Criptocoin` (`abreviacion`, `nombre`) VALUES
  ('BTC',   'Bitcoin'),
  ('ETH',   'Ethereum'),
  ('USDT',  'Tether'),
  ('BNB',   'Binance Coin'),
  ('SOL',   'Solana');

CREATE TABLE `card` (
  `MetodoPagoId` int NOT NULL,
  `NumeroTarjeta` varbinary(256) NOT NULL,
  `Titular` varchar(50) COLLATE utf8mb4_unicode_ci NOT NULL,
  `Mes` int NOT NULL,
  `Year` int NOT NULL,
  `Cvv` varchar(5) COLLATE utf8mb4_unicode_ci NOT NULL,
  `Tipo` char(1) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT 'D = débito, C = crédito',
  `Red` enum('VISA','MASTERCARD','AMEX','DINERS') COLLATE utf8mb4_unicode_ci NOT NULL COMMENT 'Red de la tarjeta',
  PRIMARY KEY (`MetodoPagoId`),
  CONSTRAINT `fk_creditcard_metodopago` FOREIGN KEY (`MetodoPagoId`) REFERENCES `metodo_pago` (`Id`) ON DELETE CASCADE,
  CONSTRAINT `chk_card_tipo` CHECK ((`Tipo` in (_utf8mb4'D',_utf8mb4'C')))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `bank_account` (
  `MetodoPagoId` int NOT NULL,
  `NombreBanco` varchar(20) COLLATE utf8mb4_unicode_ci NOT NULL,
  `NumeroCuenta` varbinary(256) NOT NULL,
  `TipoCuenta` enum('Ahorro','Corriente') COLLATE utf8mb4_unicode_ci NOT NULL,
  `Titular` varchar(50) COLLATE utf8mb4_unicode_ci NOT NULL,
  `Identificacion` varchar(13) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT 'Identificación nacional (10–13 dígitos)',
  PRIMARY KEY (`MetodoPagoId`),
  CONSTRAINT `fk_bankaccount_metodopago` FOREIGN KEY (`MetodoPagoId`) REFERENCES `metodo_pago` (`Id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
