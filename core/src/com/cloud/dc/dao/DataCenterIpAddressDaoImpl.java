/**
 *  Copyright (C) 2010 Cloud.com, Inc.  All rights reserved.
 * 
 * This software is licensed under the GNU General Public License v3 or later.
 * 
 * It is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * 
 */
package com.cloud.dc.dao;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Date;
import java.util.List;

import javax.ejb.Local;

import org.apache.log4j.Logger;

import com.cloud.dc.DataCenterIpAddressVO;
import com.cloud.utils.db.DB;
import com.cloud.utils.db.GenericDaoBase;
import com.cloud.utils.db.GenericSearchBuilder;
import com.cloud.utils.db.SearchBuilder;
import com.cloud.utils.db.SearchCriteria;
import com.cloud.utils.db.SearchCriteria.Func;
import com.cloud.utils.db.Transaction;
import com.cloud.utils.exception.CloudRuntimeException;
import com.cloud.utils.net.NetUtils;

@Local(value={DataCenterIpAddressDao.class}) @DB(txn=false)
public class DataCenterIpAddressDaoImpl extends GenericDaoBase<DataCenterIpAddressVO, Long> implements DataCenterIpAddressDao {
    private static final Logger s_logger = Logger.getLogger(DataCenterIpAddressDaoImpl.class);
    
    private final SearchBuilder<DataCenterIpAddressVO> AllFieldsSearch;
    private final GenericSearchBuilder<DataCenterIpAddressVO, Integer> AllIpCount;
    private final GenericSearchBuilder<DataCenterIpAddressVO, Integer> AllAllocatedIpCount;
    
    @DB
    public DataCenterIpAddressVO takeIpAddress(long dcId, long podId, long instanceId, String reservationId) {
        SearchCriteria<DataCenterIpAddressVO> sc = AllFieldsSearch.create();
        sc.setParameters("pod", podId);
        sc.setParameters("taken", (Date)null);
        
        Transaction txn = Transaction.currentTxn();
        txn.start();
        DataCenterIpAddressVO  vo = lockOneRandomRow(sc, true);
        if (vo == null) {
            return null;
        }
        vo.setTakenAt(new Date());
        vo.setInstanceId(instanceId);
        vo.setReservationId(reservationId);
        update(vo.getId(), vo);
        txn.commit();
        return vo;
    }
    
    @Override
    public boolean deleteIpAddressByPod(long podId) {
        SearchCriteria<DataCenterIpAddressVO> sc = AllFieldsSearch.create();
        sc.setParameters("pod", podId);
        return remove(sc) > 0;
    }
    
    @Override
    public boolean mark(long dcId, long podId, String ip) {
        SearchCriteria<DataCenterIpAddressVO> sc = AllFieldsSearch.create();
        sc.setParameters("pod", podId);
        sc.setParameters("ipAddress", ip);
        
        DataCenterIpAddressVO vo = createForUpdate();
        vo.setTakenAt(new Date());
        
        return update(vo, sc) >= 1;
    }
    
    @DB
    public void addIpRange(long dcId, long podId, String start, String end) {
        Transaction txn = Transaction.currentTxn();
        String insertSql = "INSERT INTO op_dc_ip_address_alloc (ip_address, data_center_id, pod_id) VALUES (?, ?, ?)";
        PreparedStatement stmt = null;
        
        long startIP = NetUtils.ip2Long(start);
        long endIP = NetUtils.ip2Long(end);
        
        try {
            txn.start();
            stmt = txn.prepareAutoCloseStatement(insertSql);
            while (startIP <= endIP) {
                stmt.setString(1, NetUtils.long2Ip(startIP++));
                stmt.setLong(2, dcId);
                stmt.setLong(3, podId);
                stmt.addBatch();
            }
            txn.commit();
        } catch (SQLException ex) {
            throw new CloudRuntimeException("Unable to persist ip address range ", ex);
        }
    }
    
    public void releaseIpAddress(String ipAddress, long dcId, Long instanceId) {
    	if (s_logger.isDebugEnabled()) {
    		s_logger.debug("Releasing ip address: " + ipAddress + " data center " + dcId);
    	}
        SearchCriteria<DataCenterIpAddressVO> sc = AllFieldsSearch.create();
        sc.setParameters("ip", ipAddress);
        sc.setParameters("dc", dcId);
        sc.setParameters("instance", instanceId);

        DataCenterIpAddressVO vo = createForUpdate();
        
        vo.setTakenAt(null);
        vo.setInstanceId(null);
        vo.setReservationId(null);
        update(vo, sc);
    }
    
    public void releaseIpAddress(long nicId, String reservationId) {
        if (s_logger.isDebugEnabled()) {
            s_logger.debug("Releasing ip address for reservationId=" + reservationId + ", instance=" + nicId);
        }
        SearchCriteria<DataCenterIpAddressVO> sc = AllFieldsSearch.create();
        sc.setParameters("instance", nicId);
        sc.setParameters("reservation", reservationId);
        
        DataCenterIpAddressVO vo = createForUpdate();
        vo.setTakenAt(null);
        vo.setInstanceId(null);
        vo.setReservationId(null);
        update(vo, sc);
    }
    
    public List<DataCenterIpAddressVO> listByPodIdDcId(long podId, long dcId) {
		SearchCriteria<DataCenterIpAddressVO> sc = AllFieldsSearch.create();
		sc.setParameters("pod", podId);
		return listBy(sc);
	}
    
    @Override
    public List<DataCenterIpAddressVO> listByPodIdDcIdIpAddress(long podId, long dcId, String ipAddress) {
    	SearchCriteria<DataCenterIpAddressVO> sc = AllFieldsSearch.create();
		sc.setParameters("pod", podId);
		sc.setParameters("ipAddress", ipAddress);
		return listBy(sc);
    }
    
    @Override
    public int countIPs(long podId, long dcId, boolean onlyCountAllocated) {
        SearchCriteria<Integer> sc;
        if (onlyCountAllocated) { 
            sc = AllAllocatedIpCount.create();
        } else {
            sc = AllIpCount.create();
        }
        
        sc.setParameters("pod", podId);
        List<Integer> count = customSearch(sc, null);
        return count.get(0);
	}
    
    protected DataCenterIpAddressDaoImpl() {
        super();
        
        AllFieldsSearch = createSearchBuilder();
        AllFieldsSearch.and("ip", AllFieldsSearch.entity().getIpAddress(), SearchCriteria.Op.EQ);
        AllFieldsSearch.and("dc", AllFieldsSearch.entity().getDataCenterId(), SearchCriteria.Op.EQ);
        AllFieldsSearch.and("pod", AllFieldsSearch.entity().getPodId(), SearchCriteria.Op.EQ);
        AllFieldsSearch.and("instance", AllFieldsSearch.entity().getInstanceId(), SearchCriteria.Op.EQ);
        AllFieldsSearch.and("ipAddress", AllFieldsSearch.entity().getIpAddress(), SearchCriteria.Op.EQ);
        AllFieldsSearch.and("reservation", AllFieldsSearch.entity().getReservationId(), SearchCriteria.Op.EQ);
        AllFieldsSearch.and("taken", AllFieldsSearch.entity().getTakenAt(), SearchCriteria.Op.EQ);
        AllFieldsSearch.done();
        
        AllIpCount = createSearchBuilder(Integer.class);
        AllIpCount.select(null, Func.COUNT, AllIpCount.entity().getId());
        AllIpCount.and("pod", AllIpCount.entity().getPodId(), SearchCriteria.Op.EQ);
        AllIpCount.done();
        
        AllAllocatedIpCount = createSearchBuilder(Integer.class);
        AllAllocatedIpCount.select(null, Func.COUNT, AllAllocatedIpCount.entity().getId());
        AllAllocatedIpCount.and("pod", AllAllocatedIpCount.entity().getPodId(), SearchCriteria.Op.EQ);
        AllAllocatedIpCount.and("removed", AllAllocatedIpCount.entity().getTakenAt(), SearchCriteria.Op.NNULL);
        AllAllocatedIpCount.done();
    }
}
