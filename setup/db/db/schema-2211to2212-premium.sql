# Copyright (C) 2011 Citrix Systems, Inc.  All rights reserved
#     
# This software is licensed under the GNU General Public License v3 or later.
# 
# It is free software: you can redistribute it and/or modify
# it under the terms of the GNU General Public License as published by
# the Free Software Foundation, either version 3 of the License, or any later version.
# This program is distributed in the hope that it will be useful,
# but WITHOUT ANY WARRANTY; without even the implied warranty of
# MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
# GNU General Public License for more details.
# 
# You should have received a copy of the GNU General Public License
# along with this program.  If not, see <http://www.gnu.org/licenses/>.
# 
--;
-- Premium schema upgrade from 2.2.11 to 2.2.12;
--;

ALTER TABLE `cloud_usage`.`cloud_usage` ADD INDEX `i_cloud_usage__account_id`(`account_id`);
ALTER TABLE `cloud_usage`.`cloud_usage` ADD INDEX `i_cloud_usage__domain_id`(`domain_id`);
ALTER TABLE `cloud_usage`.`cloud_usage` ADD INDEX `i_cloud_usage__start_date`(`start_date`);
ALTER TABLE `cloud_usage`.`cloud_usage` ADD INDEX `i_cloud_usage__end_date`(`end_date`);

ALTER TABLE `cloud_usage`.`usage_vm_instance` ADD INDEX `i_usage_vm_instance__account_id`(`account_id`);
ALTER TABLE `cloud_usage`.`usage_vm_instance` ADD INDEX `i_usage_vm_instance__start_date`(`start_date`);
ALTER TABLE `cloud_usage`.`usage_vm_instance` ADD INDEX `i_usage_vm_instance__end_date`(`end_date`);

ALTER TABLE `cloud_usage`.`usage_ip_address` ADD INDEX `i_usage_ip_address__account_id`(`account_id`);
ALTER TABLE `cloud_usage`.`usage_ip_address` ADD INDEX `i_usage_ip_address__assigned`(`assigned`);
ALTER TABLE `cloud_usage`.`usage_ip_address` ADD INDEX `i_usage_ip_address__released`(`released`);

ALTER TABLE `cloud_usage`.`usage_job` ADD INDEX `i_usage_job__end_millis`(`end_millis`);

ALTER TABLE `cloud_usage`.`account` ADD INDEX `i_account__removed`(`removed`);

ALTER TABLE `cloud_usage`.`usage_volume` ADD INDEX `i_usage_volume__account_id`(`account_id`);
ALTER TABLE `cloud_usage`.`usage_volume` ADD INDEX `i_usage_volume__created`(`created`);
ALTER TABLE `cloud_usage`.`usage_volume` ADD INDEX `i_usage_volume__deleted`(`deleted`);

ALTER TABLE `cloud_usage`.`usage_storage` ADD INDEX `i_usage_storage__account_id`(`account_id`);
ALTER TABLE `cloud_usage`.`usage_storage` ADD INDEX `i_usage_storage__created`(`created`);
ALTER TABLE `cloud_usage`.`usage_storage` ADD INDEX `i_usage_storage__deleted`(`deleted`);

ALTER TABLE `cloud_usage`.`usage_load_balancer_policy` ADD INDEX `i_usage_load_balancer_policy__account_id`(`account_id`);
ALTER TABLE `cloud_usage`.`usage_load_balancer_policy` ADD INDEX `i_usage_load_balancer_policy__created`(`created`);
ALTER TABLE `cloud_usage`.`usage_load_balancer_policy` ADD INDEX `i_usage_load_balancer_policy__deleted`(`deleted`);

ALTER TABLE `cloud_usage`.`usage_port_forwarding` ADD INDEX `i_usage_port_forwarding__account_id`(`account_id`);
ALTER TABLE `cloud_usage`.`usage_port_forwarding` ADD INDEX `i_usage_port_forwarding__created`(`created`);
ALTER TABLE `cloud_usage`.`usage_port_forwarding` ADD INDEX `i_usage_port_forwarding__deleted`(`deleted`);

ALTER TABLE `cloud_usage`.`usage_network_offering` ADD INDEX `i_usage_network_offering__account_id`(`account_id`);
ALTER TABLE `cloud_usage`.`usage_network_offering` ADD INDEX `i_usage_network_offering__created`(`created`);
ALTER TABLE `cloud_usage`.`usage_network_offering` ADD INDEX `i_usage_network_offering__deleted`(`deleted`);

ALTER IGNORE TABLE  `cloud_usage`.`usage_vm_instance` ADD UNIQUE (`vm_instance_id`, `usage_type`, `start_date`);
ALTER IGNORE TABLE  `cloud_usage`.`usage_ip_address` ADD UNIQUE (`id`, `assigned`);
ALTER IGNORE TABLE  `cloud_usage`.`usage_volume` ADD UNIQUE (`id`, `created`);
ALTER IGNORE TABLE  `cloud_usage`.`usage_storage` ADD UNIQUE (`id`, `storage_type`, `zone_id`, `created`);