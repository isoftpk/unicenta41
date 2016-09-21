--    uniCenta oPOS - Touch Friendly Point Of Sale
--    Copyright (c) 2009-2016 uniCenta
--    http://www.unicenta.net
--
--    This file is part of uniCenta oPOS.
--
--    uniCenta oPOS is free software: you can redistribute it and/or modify
--    it under the terms of the GNU General Public License as published by
--    the Free Software Foundation, either version 3 of the License, or
--    (at your option) any later version.
--
--    uniCenta oPOS is distributed in the hope that it will be useful,
--    but WITHOUT ANY WARRANTY; without even the implied warranty of
--    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
--    GNU General Public License for more details.
--
--    You should have received a copy of the GNU General Public License
--    along with uniCenta oPOS.  If not, see <http://www.gnu.org/licenses/>.

-- Database upgrade script for MySQL
-- v4.1.0 - v4.1.1 5FEB2016

--
-- CLEAR THE DECKS
--
DELETE FROM sharedtickets;

-- 
-- UPDATE ROLES
--
UPDATE `roles` SET `permissions` = $FILE{/com/openbravo/pos/templates/Role.Administrator.xml} WHERE `id` = '0';
UPDATE `roles` SET `permissions` = $FILE{/com/openbravo/pos/templates/Role.Manager.xml} WHERE `id` = '1';

-- 
-- UPDATE PRODUCTS
--
ALTER TABLE `products` DROP INDEX `products_name_inx`;
ALTER TABLE `products` ADD INDEX `products_name_inx` (name);

-- UPDATE App' version
UPDATE applications SET NAME = $APP_NAME{}, VERSION = $APP_VERSION{} WHERE ID = $APP_ID{};