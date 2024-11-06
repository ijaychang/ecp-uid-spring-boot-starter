package cn.jaychang.ecp.uid.leaf.dao;


import cn.jaychang.ecp.uid.leaf.model.LeafAlloc;
import org.springframework.jdbc.core.JdbcTemplate;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

public class IDAllocDao {

    private final JdbcTemplate jdbcTemplate;

    public IDAllocDao(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private static LeafAlloc toLeafAlloc(ResultSet rs, int rowNum) throws SQLException {
        LeafAlloc leafAlloc = new LeafAlloc();
        toLeafAllocWithoutUpdateTime(rs, rowNum);
        leafAlloc.setUpdateTime(rs.getString("update_time"));
        return leafAlloc;
    }


    private static LeafAlloc toLeafAllocWithoutUpdateTime(ResultSet rs, int rowNum) throws SQLException {
        LeafAlloc leafAlloc = new LeafAlloc();
        leafAlloc.setKey(rs.getString("biz_tag"));
        leafAlloc.setMaxId(rs.getLong("max_id"));
        leafAlloc.setStep(rs.getInt("step"));
        return leafAlloc;
    }

    public List<LeafAlloc> getAllLeafAllocs() {
        String sql = "SELECT biz_tag, max_id, step, update_time FROM leaf_alloc";
        return jdbcTemplate.query(sql, IDAllocDao::toLeafAlloc);
    }

    public LeafAlloc getLeafAlloc(String tag) {
        String sql = "SELECT biz_tag, max_id, step FROM leaf_alloc WHERE biz_tag = ?";
        return jdbcTemplate.queryForObject(sql, IDAllocDao::toLeafAllocWithoutUpdateTime, tag);
    }

    public void updateMaxId(String tag) {
        String sql = "UPDATE leaf_alloc SET max_id = max_id + step WHERE biz_tag = ?";
        jdbcTemplate.update(sql, tag);
    }

    public void updateMaxIdByCustomStep(LeafAlloc leafAlloc) {
        String sql = "UPDATE leaf_alloc SET max_id = max_id + ? WHERE biz_tag = ?";
        jdbcTemplate.update(sql, leafAlloc.getStep(), leafAlloc.getKey());
    }

    public List<String> getAllTags() {
        String sql = "SELECT biz_tag FROM leaf_alloc";
        return jdbcTemplate.queryForList(sql, String.class);
    }
}
