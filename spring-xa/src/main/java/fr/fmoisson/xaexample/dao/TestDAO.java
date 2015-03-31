package fr.fmoisson.xaexample.dao;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

/**
 * Created by fmoisson on 14/03/15.
 */
@Repository
public class TestDAO {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Transactional
    public void insert(Integer key, String value) {
        jdbcTemplate.update("INSERT INTO test VALUES (?, ?)", key, value);
    }

    @Transactional
    public void insert(String value) {
        Integer maxId = jdbcTemplate.queryForObject("SELECT MAX(pk) FROM test", Integer.class);
        Integer nextId = maxId != null ? maxId + 1 : 0;
        jdbcTemplate.update("INSERT INTO test VALUES (?, ?)", nextId, value);
    }
}
