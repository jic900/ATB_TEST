-- ----------------------------------------------------
DELIMITER $$
DROP PROCEDURE IF EXISTS `fnc_omd_some_clients_server_status` $$
CREATE PROCEDURE `fnc_omd_some_clients_server_status`(IN user_id INT)
BEGIN
SELECT c.client_id, c.client_name, dc.category_name, dt.model, v.vendor_name,
(CASE WHEN du.used_perc IS NOT NULL THEN du.used_perc / 100 ELSE 'Unknown' END) AS 'disk_usage', 
mu.mem_total AS 'memory_total',
mu.mem_used AS 'memory_used',
mu.mem_total AS 'memory_free',
mu.mem_buffers AS 'memory_buffers',
mu.mem_cached AS 'memory_cached',
mu.swap_total AS 'swap_total',
mu.swap_used AS 'swap_used',
mu.swap_free AS 'swap_free',
mu.updated_on AS 'memory_updated_on',
sl.1MIN AS '1min_server_load',
sl.5MIN AS '5min_server_load',
sl.15MIN AS '15min_server_load',
sl.updated_on AS 'server_load_updated_on',
hb.heartbeat AS 'last_heartbeat', 
dhcp.service_running AS 'dhcp_service_running',
dhcp.leases_count AS 'dhcp_leases_count',
dhcp.leases_filesize AS 'dhcp_leases_filesize',
dhcp.updated_on AS 'dhcp_updated_on',
(CASE WHEN dhcp.service_running = 1 THEN 'Up' ELSE 'Down' END) 'dhcp_status',
(CASE WHEN dcsc.check_status IN (1, 2) THEN 'Up' ELSE (CASE WHEN dcsc.check_status = 3 THEN 'Down' ELSE 'Unknown' END) END) 'dns_status'  
FROM (SELECT DISTINCT c1.client_id, c1.client_name FROM organization_users ou1 
			JOIN clients c1 ON ou1.org_id = c1.management_org_id
			WHERE ou1.uid = user_id
	UNION 
	
SELECT c2.client_id, c2.client_name FROM users u2 
	JOIN client_users cu ON u2.uid = cu.uid
	JOIN clients c2	ON cu.client_id = c2.client_id
	WHERE u2.uid = user_id) c 
JOIN omd_device d ON c.client_id=d.client_id
INNER JOIN omd_device_type dt ON d.device_type_id = dt.device_type_id
INNER JOIN omd_device_category dc ON dt.device_category_id =  dc.device_category_id
INNER JOIN omd_vendor v ON (dt.vendor_id = v.vendor_id)
LEFT JOIN omd_client_heartbeat hb ON hb.client_id=c.client_id
LEFT JOIN omd_disk_usage du ON du.device_id = d.device_id
LEFT JOIN omd_memory_usage mu ON mu.device_id = d.device_id
LEFT JOIN omd_server_load sl ON sl.device_id = d.device_id
LEFT JOIN omd_dhcp_usage dhcp ON dhcp.device_id=d.device_id
LEFT JOIN omd_device_check_status_current dcsc ON dcsc.device_id=d.device_id

WHERE d.mode = 1 AND dc.device_category_id = 25 AND du.filesystem LIKE '%rootvol%' AND dcsc.check_id = 3 GROUP BY d.device_id;


END $$
DELIMITER ; 
  
  --          --------------------

DELIMITER $$

DROP PROCEDURE IF EXISTS `fnc_omd_some_clients_device_summary`$$

CREATE PROCEDURE `fnc_omd_some_clients_device_summary`(IN user_id INT)
BEGIN
SELECT
c.client_id AS  'CLIENT ID', 
c.client_name AS 'CLIENT NAME',
SUM(CASE WHEN dm.`mode_desc` = 'Production' AND dc.device_category_id IN (3, 5, 7, 11, 15, 17, 31)  THEN 1 ELSE 0 END) AS 'SWITCH PRODUCTION',
SUM(CASE WHEN dm.`mode_desc` = 'Spare' AND dc.device_category_id IN (3, 5, 7, 11, 15, 17, 31) THEN 1 ELSE 0 END) AS 'SWITCH SPARE',
SUM(CASE WHEN dm.`mode_desc` = 'Out of Service' AND dc.device_category_id IN (3, 5, 7, 11, 15, 17, 31) THEN 1 ELSE 0 END) AS 'SWITCH OUT OF SERVICE' ,
SUM(CASE WHEN ds.`description` = 'Host Unreachable' AND dm.`mode_desc` = 'Production' AND dc.device_category_id IN (3, 5, 7, 11, 15, 17, 31) THEN 1 ELSE 0 END) AS 'SWITCH OFF-LINE' ,
SUM(CASE WHEN ds.`description` <> 'Host Unreachable' AND ds.`description` IS NOT NULL AND dm.`mode_desc` = 'Production' AND dc.device_category_id IN (3, 5, 7, 11, 15, 17, 31) THEN 1 ELSE 0 END) AS 'SWITCH ON-LINE' ,
SUM(CASE WHEN ds.`description` IS NULL AND dm.`mode_desc` = 'Production' AND dc.device_category_id IN (3, 5, 7, 11, 15, 17, 31) THEN 1 ELSE 0 END) AS 'SWITCH UNKNOWN' ,
SUM(CASE WHEN dc.device_category_id IN (3, 5, 7, 11, 15, 17, 31) THEN 1 ELSE 0 END) AS 'SWITCH TOTAL DEVICES' ,
GROUP_CONCAT(DISTINCT(CASE WHEN dc.device_category_id IN (3, 5, 7, 11, 15, 17, 31) THEN v.vendor_name END)) AS 'SWITCH VENDOR' ,
GROUP_CONCAT(DISTINCT(CASE WHEN dc.device_category_id IN (3, 5, 7, 11, 15, 17, 31) THEN dt.model END))  AS 'SWITCH MODEL',
GROUP_CONCAT(DISTINCT(CASE WHEN dc.device_category_id IN (3, 5, 7, 11, 15, 17, 31) THEN dc.category_name END)) AS 'SWITCH CATEGORY',
SUM(CASE WHEN dm.`mode_desc` = 'Production' AND dc.device_category_id IN (9,19)  THEN 1 ELSE 0 END) AS 'WIRELESS PRODUCTION',
SUM(CASE WHEN dm.`mode_desc` = 'Spare' AND dc.device_category_id IN (9,19) THEN 1 ELSE 0 END) AS 'WIRELESS SPARE',
SUM(CASE WHEN dm.`mode_desc` = 'Out of Service' AND dc.device_category_id IN (9,19) THEN 1 ELSE 0 END) AS 'WIRELESS OUT OF SERVICE' ,
SUM(CASE WHEN ds.`description` = 'Host Unreachable' AND dm.`mode_desc` = 'Production' AND dc.device_category_id IN (9,19) THEN 1 ELSE 0 END) AS 'WIRELESS OFF-LINE' ,
SUM(CASE WHEN ds.`description` <> 'Host Unreachable' AND ds.`description` IS NOT NULL AND dm.`mode_desc` = 'Production' AND dc.device_category_id IN (9,19) THEN 1 ELSE 0 END) AS 'WIRELESS ON-LINE' ,
SUM(CASE WHEN ds.`description` IS NULL AND dm.`mode_desc` = 'Production' AND dc.device_category_id IN (9,19) THEN 1 ELSE 0 END) AS 'WIRELESS UNKNOWN' ,
SUM(CASE WHEN dc.device_category_id IN (9,19) THEN 1 ELSE 0 END) AS 'WIRELESS TOTAL DEVICES' ,
GROUP_CONCAT(DISTINCT(CASE WHEN dc.device_category_id IN (9,19) THEN v.vendor_name END)) AS 'WIRELESS VENDOR' ,
GROUP_CONCAT(DISTINCT(CASE WHEN dc.device_category_id IN (9,19) THEN dt.model END))  AS 'WIRELESS MODEL',
GROUP_CONCAT(DISTINCT(CASE WHEN dc.device_category_id IN (9,19) THEN dc.category_name END)) AS 'WIRELESS CATEGORY'
FROM (SELECT DISTINCT c1.client_id, c1.client_name FROM organization_users ou1 
			JOIN clients c1 ON ou1.org_id = c1.management_org_id
			WHERE ou1.uid = user_id
UNION 	
SELECT c2.client_id, c2.client_name FROM users u2 
JOIN client_users cu ON u2.uid = cu.uid
JOIN clients c2	ON cu.client_id = c2.client_id
WHERE u2.uid = user_id) c 
INNER JOIN `omd_device` d ON (c.`client_id`=d.`client_id`)
INNER JOIN omd_device_type dt ON (d.`device_type_id`=dt.`device_type_id`)
INNER JOIN omd_device_category dc ON (dt.device_category_id = dc.device_category_id)
INNER JOIN omd_vendor v ON (dt.vendor_id = v.vendor_id)
INNER JOIN `omd_device_mode` dm ON (d.`mode`=dm.`device_mode_id`)
LEFT JOIN `omd_device_status_current` dsc ON (d.`device_id`=dsc.`device_id`)
INNER JOIN `omd_device_state` ds ON (ds.`device_state_id`=dsc.`current_state`)
GROUP BY c.client_id ;

END$$

DELIMITER ;

-- ----------------------------------------------------------------
DELIMITER $$
DROP PROCEDURE IF EXISTS fnc_omd_get_alerts_distro_users $$
CREATE PROCEDURE `fnc_omd_get_alerts_distro_users`(IN userId INT,IN t_userId INT, IN clientId INT)
BEGIN
-- ***********************************************************
-- SP fnc_omd_get_alerts_distro_users (IN userId INT,IN t_userId INT, IN clientId INT)
-- UserId	t_userId	clientId	Return value
--   X			0			0		a notification distribution list for the all clients  that login user (userId) assigned to 
--   X			X			0		a notification distribution list for all clients that  targeted user ( t_userId) assigned to
--   X			X			X		a notification distribution recorder for the specified client (clientId) that targeted  user assigned to
-- 	 X			0			X		a notification distribution list for the specified client (clientId)
-- ***********************************************************
-- client is not specified 
SET @distro_key := 0 ;
IF (clientId = 0) THEN
	-- A indicator to determine external user or internal user.  ( external user : clientAssigned > 0 ; otherwise internal user )
	-- SET @clientAssigned = (Select count(*) users u join osd_users os on os.email = u.email_address WHERE os.user_id = userId AND u.internal = false);

SET @clientAssigned = (Select count(*) 
							FROM (SELECT DISTINCT u.* 
									FROM users u 
									JOIN osd_users os ON os.email = u.email_address
									JOIN organization_users ou ON ou.uid = u.uid AND ou.org_id != 1
									JOIN (SELECT * FROM `organizations` WHERE `name_tree_path` NOT LIKE '/GTCORP/%' AND `name_tree_path` NOT LIKE '/Unknown/%') o ON o.org_id = ou.org_id  
									WHERE os.user_id = userId
								   UNION 
								   SELECT u.* 
									 FROM users u  
									 JOIN client_users cu
									 JOIN osd_users os
									 WHERE os.user_id = userId AND os.email = u.email_address AND cu.uid = u.uid ) t
							 );
	-- targeted user is not specified 
	IF (t_userId = 0) THEN
		-- a user list from all client hotels which managed by user(userId)
		SELECT * FROM (
		SELECT IFNULL(ea.distro_id,  @distro_key := @distro_key -1) as distro_id ,ea.alert_id, cu.client_id, ea.email_address, ea.email_type, ou.user_id, IFNULL(ea.alert_enabled,0) as alert_enabled
		FROM users u 
		JOIN (SELECT DISTINCT uid, client_id FROM client_users WHERE 
					-- extrnal User:
					(CASE WHEN  @clientAssigned > 0 THEN
						client_id IN (SELECT od.client_id from omd_device od 
						JOIN
							(SELECT distinct c1.client_id FROM users u1 
							JOIN organization_users ou ON u1.uid = ou.uid AND ou.org_id != 1 
							JOIN (SELECT * FROM `organizations` WHERE `name_tree_path` NOT LIKE '/GTCORP/%' AND `name_tree_path` NOT LIKE '/Unknown/%') o ON o.org_id = ou.org_id  
							JOIN clients c1 ON ou.org_id = c1.management_org_id
							JOIN osd_users ouser ON ouser.email = u1.email_address
							WHERE ouser.user_id = userId
							UNION 
							SELECT c2.client_id FROM users u2 
							JOIN client_users cu ON u2.uid = cu.uid
							JOIN clients c2	ON cu.client_id = c2.client_id
							WHERE u2.email_address = (Select email from osd_users where user_id = userId)) t 
						ON t.client_id = od.client_id 
						JOIN omd_client_install oci on od.client_id = oci.client_id  
						WHERE od.mode = 1 AND oci.client_status = 1 GROUP BY od.client_id 
						)
					-- internalUser :
					ELSE client_id IN ( SELECT ch.client_id from omd_client_heartbeat ch JOIN omd_client_install oci on ch.client_id = oci.client_id AND oci.client_status = 1)		
					END	)
	
				) cu ON u.uid = cu.uid 
		LEFT JOIN osd_users ou ON u.email_address = ou.email  
		LEFT JOIN omd_email_alerts_client_distro ea ON ou.user_id = ea.user_id and ea.client_id = cu.client_id
		WHERE ou.user_id IS NOT NULL
		ORDER BY ea.alert_enabled 
		) t
		GROUP BY t.user_id;

	-- targeted user is specified 		
	ELSE
		-- External User
		 IF ( @clientAssigned > 0 ) THEN 
			SELECT IFNULL(eacd.distro_id,  @distro_key := @distro_key -1) as distro_id, eacd.alert_id, c.client_id, eacd.email_address, eacd.email_type, IFNULL(eacd.user_id, t_userId) as user_id,IFNULL(eacd.alert_enabled,0) as alert_enabled 
			FROM omd_email_alerts_client_distro eacd
			RIGHT OUTER JOIN 
				(SELECT distinct c1.client_id FROM users u1 
				JOIN organization_users ou ON u1.uid = ou.uid AND ou.org_id != 1 
				JOIN (SELECT * FROM `organizations` WHERE `name_tree_path` NOT LIKE '/GTCORP/%' AND `name_tree_path` NOT LIKE '/Unknown/%') o ON o.org_id = ou.org_id  
				JOIN clients c1 ON ou.org_id = c1.management_org_id
				JOIN osd_users ouser ON ouser.email = u1.email_address
				WHERE ouser.user_id = userId
				UNION 
				SELECT c2.client_id FROM users u2 
				JOIN client_users cu ON u2.uid = cu.uid
				JOIN clients c2	ON cu.client_id = c2.client_id
				WHERE u2.email_address = (Select email from osd_users where user_id = userId)) t 
				ON t.client_id = od.client_id 
				JOIN omd_client_install oci on od.client_id = oci.client_id
				WHERE od.mode = 1 AND oci.client_status = 1 GROUP BY od.client_id 
			) c
			ON c.client_id = eacd.client_id and eacd.user_id = t_userId ;
			
		ELSE 
		 -- Internal User
			 SELECT IFNULL(eacd.distro_id, @distro_key := @distro_key -1) as distro_id, eacd.alert_id, c.client_id, eacd.email_address, eacd.email_type, IFNULL(eacd.user_id, t_userId) as user_id,IFNULL(eacd.alert_enabled,0) as alert_enabled 
			 FROM omd_email_alerts_client_distro eacd
			 RIGHT OUTER JOIN 
				(SELECT ch.client_id from omd_client_heartbeat ch JOIN omd_client_install oci on ch.client_id = oci.client_id AND oci.client_status = 1) c
			 ON c.client_id = eacd.client_id and eacd.user_id = t_userId ;
			 
		END IF;
	END IF ;

-- client is specified
ELSE
	-- a user list for a specific client hotel(clientId), contains alerts enable info
	-- when specify a target user (t_userId != 0 ), return a single user
		SELECT IFNULL(ea.distro_id, @distro_key := @distro_key -1) as distro_id, ea.alert_id, t.client_id, ea.email_address, ea.email_type, ou.user_id, IFNULL(ea.alert_enabled,0) as alert_enabled  
		FROM (SELECT u.email_address,cu.client_id FROM users u 
				JOIN client_users  cu ON u.uid = cu.uid 
				UNION
				SELECT distinct u1.email_address, c1.client_id FROM users u1 
				JOIN organization_users ou ON u1.uid = ou.uid AND ou.org_id != 1
				JOIN clients c1 ON ou.org_id = c1.management_org_id ) t
				JOIN (SELECT * FROM `organizations` WHERE `name_tree_path` NOT LIKE '/GTCORP/%' AND `name_tree_path` NOT LIKE '/Unknown/%') o ON o.org_id = ou.org_id 				
		LEFT JOIN osd_users ou ON t.email_address = ou.email
		LEFT JOIN omd_email_alerts_client_distro ea ON ou.user_id = ea.user_id and ea.client_id = t.client_id
		WHERE t.client_id = clientId and ( CASE WHEN t_userId = 0 THEN ou.user_id IS NOT NULL ELSE ou.user_id = t_userId END ) 
		UNION	
		SELECT eacd.* FROM omd_email_alerts_client_distro eacd
		WHERE eacd.client_id = clientId and ( CASE WHEN t_userId = 0 THEN eacd.user_id IS NOT NULL ELSE eacd.user_id = t_userId END );

END IF;

END $$
DELIMITER ;
-- ---------------------------------
-- SP fnc_omd_get_alerts_for_client ---------------------------------------------------------
DELIMITER $$
DROP PROCEDURE IF EXISTS fnc_omd_get_alerts_for_client $$
CREATE PROCEDURE `fnc_omd_get_alerts_for_client`(IN userId INT,IN alertId INT, IN clientId INT)
BEGIN

IF (clientId = 0) THEN
	
	SET @clientAssigned = (Select count(*) 
							FROM (SELECT DISTINCT u.* 
									FROM users u 
									JOIN organization_users ou ON u.uid = ou.uid AND ou.org_id != 1
									JOIN (SELECT * FROM `organizations` WHERE `name_tree_path` NOT LIKE '/GTCORP/%' AND `name_tree_path` NOT LIKE '/Unknown/%') o ON o.org_id = ou.org_id  
									JOIN osd_users os ON os.email = u.email_address 
									WHERE os.user_id = userId 
									UNION 
								   SELECT u.* 
									 FROM users u  
									 JOIN client_users cu
									 JOIN osd_users os
									 WHERE os.user_id = userId AND os.email = u.email_address AND cu.uid = u.uid ) t
							 );

	SELECT alert_id, client_id=0 AS client_id, MAX(alert_enabled) AS alert_enabled, MIN(interval_hours) AS interval_hours
	FROM omd_email_alerts_client 
	WHERE alert_id = alertId 
		 AND
		-- extrnal User:
			(CASE WHEN  @clientAssigned > 0 THEN
				client_id IN (
				SELECT DISTINCT c1.client_id FROM users u1 
				JOIN organization_users ou ON u1.uid = ou.uid AND ou.org_id !=1
				JOIN (SELECT * FROM `organizations` WHERE `name_tree_path` NOT LIKE '/GTCORP/%' AND `name_tree_path` NOT LIKE '/Unknown/%') o ON o.org_id = ou.org_id  
				JOIN clients c1 ON ou.org_id = c1.management_org_id
				JOIN osd_users os ON os.email = u.email_address 
				WHERE os.user_id = userId 
				UNION 
				SELECT c2.client_id FROM users u2 
				JOIN client_users cu ON u2.uid = cu.uid
				JOIN clients c2	ON cu.client_id = c2.client_id
				WHERE u2.email_address = (Select email from osd_users where user_id = userId)
				) 
			-- internalUser :
			ELSE client_id IN ( SELECT ch.client_id from omd_client_heartbeat ch )		
			END	) ;

	ELSE
	 SELECT * 
	 FROM omd_email_alerts_client
	 WHERE  alert_id = alertId AND client_id = clientId ;


END IF;

END $$
DELIMITER ;


-- SP fnc_omd_get_clients_for_user ---------------------------------------------------------
DELIMITER $$
DROP PROCEDURE IF EXISTS fnc_omd_get_clients_for_ext_user $$
CREATE PROCEDURE `fnc_omd_get_clients_for_ext_user`(IN userId INT)
BEGIN
		SELECT c.* From		
			( SELECT DISTINCT c1.* FROM users u1 
				JOIN organization_users ou ON u1.uid = ou.uid AND ou.org_id != 1
				JOIN (SELECT * FROM `organizations` WHERE `name_tree_path` NOT LIKE '/GTCORP/%' AND `name_tree_path` NOT LIKE '/Unknown/%') o ON o.org_id = ou.org_id  
				JOIN clients c1 ON ou.org_id = c1.management_org_id				
				JOIN osd_users os ON os.email = u.email_address 
				WHERE os.user_id = userId 
				UNION 
				SELECT c2.* FROM users u2 
				JOIN client_users cu ON u2.uid = cu.uid
				JOIN clients c2	ON cu.client_id = c2.client_id
				WHERE u2.email_address = (Select email from osd_users where user_id = userId)) c
		LEFT JOIN omd_device od on c.client_id = od.client_id 
		LEFT JOIN omd_client_install oci on c.client_id = oci.client_id 
		WHERE od.mode = 1 and oci.client_status = 1 GROUP BY od.client_id ;
END $$
DELIMITER ;


-- ----------------------------------------------------
DELIMITER $$

DROP PROCEDURE IF EXISTS `fnc_omd_some_kasennas`;

CREATE PROCEDURE `fnc_omd_some_kasennas`(IN user_id INT)
BEGIN
SELECT c.client_id,
c.client_name,
d.device_id,
kd.cpu_idle_time,
kd.mem_util,
kd.disk_bandwidth,
kd.disk_space,
kd.hpn0_util,
kd.hpn1_util,
kd.eth0_util,
kd.last_updated
FROM omd_device d
	JOIN (SELECT c1.client_id, c1.client_name FROM organization_users ou1 
			JOIN clients c1 ON ou1.org_id = c1.management_org_id 
			WHERE ou1.uid = user_id AND ou1.org_id != 1
		UNION 
		SELECT c2.client_id, c2.client_name FROM users u2 
		JOIN client_users cu ON u2.uid = cu.uid
		JOIN clients c2	ON cu.client_id = c2.client_id
		WHERE u2.uid = user_id) c ON c.client_id = d.client_id
	JOIN omd_device_type t ON t.device_type_id=d.device_type_id
	LEFT JOIN omd_kasenna_details kd ON kd.device_id = d.device_id
WHERE t.device_category_id = 35;
    END$$

DELIMITER ;
-- ------------------------------------------------------------------------------

DELIMITER $$

DROP PROCEDURE IF EXISTS `fnc_omd_some_ovms`$$

CREATE PROCEDURE `fnc_omd_some_ovms`(IN user_id INT)
BEGIN

SELECT c.client_id, c.client_name, d.device_id, od.total_movies_avail, od.current_movie_streams, od.current_order_size, od.error_msg_size, od.last_updated, 
	SUM(CASE WHEN ss.stb_status = 3 THEN 1 ELSE 0 END) AS stb_offline,
	SUM(CASE WHEN ss.stb_status = 1 THEN 1 ELSE 0 END) AS stb_online,
	COUNT(ss.device_id) AS stb_total


FROM omd_device d
	JOIN (SELECT DISTINCT c1.client_id, c1.client_name FROM organization_users ou1 
			JOIN clients c1 ON ou1.org_id = c1.management_org_id
			WHERE ou1.uid = user_id
		UNION 
		SELECT c2.client_id, c2.client_name FROM users u2 
		JOIN client_users cu ON u2.uid = cu.uid
		JOIN clients c2	ON cu.client_id = c2.client_id
		WHERE u2.uid = user_id) c  ON c.client_id = d.client_id
	JOIN omd_device_type t ON t.device_type_id=d.device_type_id
	LEFT JOIN omd_ovm_details od ON od.device_id = d.device_id
	LEFT JOIN omd_stb_status ss ON ss.device_id = d.device_id
WHERE t.device_category_id = 27
GROUP BY c.client_id, d.device_id;

    END$$

DELIMITER ;
-- ------------------------------------------------------------------------------

DELIMITER $$

DROP PROCEDURE IF EXISTS `fnc_omd_some_clients_pms_statistics`$$

CREATE PROCEDURE `fnc_omd_some_clients_pms_statistics`(IN user_id INT)
BEGIN

	SELECT c.client_id, c.client_name, pms.pms_name, pms.pms_link_status, pms.total_rooms, pms.checkin_rooms, pms.billing_queue_size, pms.last_sent_message_time, pms.last_received_message_time 
	FROM (SELECT DISTINCT c1.client_id, c1.client_name FROM organization_users ou1 
			JOIN clients c1 ON ou1.org_id = c1.management_org_id
			WHERE ou1.uid = user_id 
		UNION 

		SELECT c2.client_id, c2.client_name FROM users u2 
		JOIN client_users cu ON u2.uid = cu.uid
		JOIN clients c2	ON cu.client_id = c2.client_id
		WHERE u2.uid = user_id) c 
	INNER JOIN omd_device d ON c.client_id=d.client_id
	INNER JOIN omd_pms_statistics pms ON pms.device_id = d.device_id;

  END$$

DELIMITER;

-- ---------------------------------------------------

DELIMITER $$

DROP PROCEDURE IF EXISTS `fnc_omd_some_disk_usage`$$

CREATE PROCEDURE `fnc_omd_some_disk_usage`(IN user_id INT)
BEGIN
	SELECT @curRow := @curRow + 1 AS row_number, d.client_id, du.*
		FROM omd_device d
		JOIN (SELECT DISTINCT c1.client_id FROM organization_users ou1 
			JOIN clients c1 ON ou1.org_id = c1.management_org_id
			WHERE ou1.uid = user_id
			UNION 
			
			SELECT c2.client_id FROM users u2 
			JOIN client_users cu ON u2.uid = cu.uid
			JOIN clients c2	ON cu.client_id = c2.client_id
			WHERE u2.uid = user_id) c ON c.client_id = d.client_id
		JOIN omd_disk_usage du ON du.device_id = d.device_id
		JOIN (SELECT @curRow := 0) rowCounter;

  END$$

DELIMITER;



-- ---------------------------------------------------

DELIMITER $$

DROP PROCEDURE IF EXISTS `fnc_omd_some_bandwidth_utilization`$$

CREATE PROCEDURE `fnc_omd_some_bandwidth_utilization`(IN user_id INT)
BEGIN
	SELECT @curRow := @curRow + 1 AS row_number, c.client_id, c.client_name, bw.* 
		FROM (SELECT DISTINCT c1.client_id, c1.client_name FROM organization_users ou1 
			JOIN clients c1 ON ou1.org_id = c1.management_org_id
			WHERE ou1.uid = user_id 
			UNION 
			
		SELECT c2.client_id, c2.client_name FROM users u2 
			JOIN client_users cu ON u2.uid = cu.uid
			JOIN clients c2	ON cu.client_id = c2.client_id
			WHERE u2.uid = user_id) c 
		INNER JOIN omd_device d ON c.client_id=d.client_id
		INNER JOIN omd_client_bandwidth_util_current bw ON bw.device_id = d.device_id
		JOIN (SELECT @curRow := 0) rowCounter;


  END$$

DELIMITER;



-- ---------------------------------------------------

DELIMITER $$

DROP PROCEDURE IF EXISTS `fnc_omd_some_checks`$$

CREATE PROCEDURE `fnc_omd_some_checks`(IN user_id INT)
BEGIN
	SELECT @curRow := @curRow + 1 AS row_number, c.client_id, c.client_name, che.check_name, csc.check_output, csc.last_updated , cs.status_desc
		FROM (SELECT DISTINCT c1.client_id, c1.client_name FROM organization_users ou1 
			JOIN clients c1 ON ou1.org_id = c1.management_org_id
			WHERE ou1.uid = user_id
			UNION 
			
			SELECT c2.client_id, c2.client_name FROM users u2 
			JOIN client_users cu ON u2.uid = cu.uid
			JOIN clients c2	ON cu.client_id = c2.client_id
			WHERE u2.uid = user_id) c 
		INNER JOIN omd_device d ON c.client_id=d.client_id
		INNER JOIN omd_device_type t ON t.device_type_id = d.device_type_id 
		INNER JOIN omd_device_category cat ON cat.device_category_id = t.device_category_id 
		INNER JOIN omd_device_check_status_current csc ON csc.device_id = d.device_id
		INNER JOIN omd_checks che ON che.check_id = csc.check_id
		INNER JOIN omd_check_status cs ON cs.check_status_id = csc.check_status
		JOIN (SELECT @curRow := 0) rowCounter
		WHERE cat.category_name LIKE '%OVI%';


  END$$

DELIMITER;
	
	