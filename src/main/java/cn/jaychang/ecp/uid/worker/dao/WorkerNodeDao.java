/*
 * Copyright (c) 2017 Baidu, Inc. All Rights Reserve.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package cn.jaychang.ecp.uid.worker.dao;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowCallbackHandler;

import cn.jaychang.ecp.uid.worker.entity.WorkerNode;

/**
 * DAO for M_WORKER_NODE
 *
 * @author yutianbao
 */
public class WorkerNodeDao {

    @Autowired
    private JdbcTemplate jdbcTemplate;
    
    /**
     * Get {@link WorkerNode} by node host
     * 
     * @param host
     * @param port
     * @return
     */
    public WorkerNode getWorkerNodeByHostPort(String host, String port) {
        final WorkerNode workerNode = new WorkerNode();
        String querySql = "SELECT * FROM worker_node where host_name = ? AND port = ? ";
        this.jdbcTemplate.query(querySql, new String[] {host, port}, new RowCallbackHandler() {
            @Override
            public void processRow(ResultSet rs)
                throws SQLException {
                workerNode.setId(rs.getLong("id"));
                workerNode.setCreateTime(rs.getDate("create_time"));
                workerNode.setUpdateTime(rs.getDate("update_time"));
                workerNode.setHostName(rs.getString("host_name"));
                workerNode.setPort(rs.getString("port"));
                workerNode.setType(rs.getInt("type"));
                workerNode.setLaunchDateDate(rs.getDate("launch_date"));
            }
        });
        return workerNode;
    }
    
    /**
     * Add {@link WorkerNode}
     * 
     * @param workerNode
     */
    public void addWorkerNode(WorkerNode workerNode) {
        String sql = "INSERT INTO worker_node(create_time, update_time, host_name, port, type, launch_date) " + " VALUES (NOW(), NOW(), ?, ?, ?, ?)";
        this.jdbcTemplate.update(sql, new Object[] {workerNode.getHostName(), workerNode.getPort(), workerNode.getType(), workerNode.getLaunchDate()});
    }
    
}
