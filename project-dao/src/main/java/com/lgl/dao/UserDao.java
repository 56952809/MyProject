package com.lgl.dao;

import com.lgl.entity.User;
import org.springframework.jdbc.core.PreparedStatementCreator;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.*;
import java.util.Collection;
import java.util.concurrent.atomic.AtomicInteger;


@Repository
public class UserDao extends BaseDao {

    private class UserMapper implements RowMapper<User> {


        @Override
        public User mapRow(ResultSet resultSet, int i) throws SQLException {
            User user = new User();
            user.setId(resultSet.getLong("id"));
            user.setName(resultSet.getString("name"));
            user.setPwd(resultSet.getString("pwd"));
            return user;
        }
    }

    /**
     * 根据用户名、密码查询用户，用于登陆
     *
     * @param name 用户名
     * @param pwd  密码
     * @return 查询到的唯一实体
     */
    public User findUser(String name, String pwd) {
        String sql = "SELECT * FROM auth_user WHERE name=? AND pwd=?";
        try {
            return jdbcTemplate.queryForObject(sql, new Object[]{name, pwd}, new UserMapper());
        } catch (Exception e) {
            //logger
            return null;
        }
    }

    /**
     * 保存用户信息
     *
     * @param user 用户实体对象
     */
    public void save(User user) {
        String sql = "insert into auth_user (id,name,pwd) values(?,?,?)";
        //jdbcTemplate.update(sql, user.getId(), user.getName(), user.getPwd());
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(new PreparedStatementCreator() {
            @Override
            public PreparedStatement createPreparedStatement(Connection connection) throws SQLException {
                PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
                ps.setLong(1, user.getId());
                ps.setString(2, user.getName());
                ps.setString(3, user.getPwd());
                return ps;
            }
        }, keyHolder);
        user.setId(keyHolder.getKey().longValue());
    }

    /**
     * 根据用户ID删除指定用户
     *
     * @param id 用户ID
     */
    public void deleteById(Long id) {
        String sql = "delete from auth_user where id=?";
        jdbcTemplate.update(sql, id);
    }

    /**
     * 更新指定用户信息
     *
     * @param user 用户信息
     */
    public void update(User user) {
        String sql = "update auth_user set name=? ,pwd=? where id=? ";
        jdbcTemplate.update(sql, user.getName(), user.getPwd(), user.getId());
    }

    /**
     * 根据用户ID查询用户信息
     *
     * @param id 用户ID
     * @return 用户实体对象
     */
    public User findById(Long id) {
        String sql = "SELECT * FROM auth_user WHERE id=?";
        try {
            return jdbcTemplate.queryForObject(sql, new Object[]{id}, new UserMapper());
        } catch (Exception e) {
            //logger
            return null;
        }
    }

    /**
     * 根据用户集合批量查询用户信息
     *
     * @param ids 用户ID集合
     * @return 用户实体对象集合
     */
    public Collection<User> findByIds(Collection<Long> ids) {
        StringBuilder sql = new StringBuilder("SELECT * FROM auth_user WHERE id in (");
        Object[] args = new Object[ids.size()];
        AtomicInteger index = new AtomicInteger(0);
        ids.forEach((id) -> {
            sql.append(id).append("?,");
            args[index.getAndIncrement()] = id;
        });
        sql.deleteCharAt(sql.length() - 2);
        sql.append(")");
        return jdbcTemplate.query(sql.toString(), ids.toArray(new Object[0]), new UserMapper());
    }
}
