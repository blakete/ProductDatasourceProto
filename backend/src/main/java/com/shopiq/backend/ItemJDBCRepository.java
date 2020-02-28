package com.shopiq.backend;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

@Repository
public class ItemJDBCRepository {
    @Autowired
    JdbcTemplate jdbcTemplate;

    class ItemRowMapper implements RowMapper < Item > {
        @Override
        public Item mapRow(ResultSet rs, int rowNum) throws SQLException {
            Item item = new Item();
            item.setName(rs.getString("name"));
            item.setBarcode(rs.getInt("barcode"));
            item.setBarcodeType(rs.getInt("barcode_type"));
            item.setStores(rs.getString("stores"));
            return item;
        }
    }

    public List < Item > findAll() {
        return jdbcTemplate.query("select * from items_table", new ItemRowMapper());
    }

    public int insert(Item aItem) {
        return jdbcTemplate.update("insert into items_table (name, barcode, barcode_type, stores) values(?, ?, ?, ?)",
                aItem.getName(), aItem.getBarcode(), aItem.getBarcodeType(), aItem.getStores());
    }

    public int removeItem(int barcode)
    {
        return jdbcTemplate.update("delete from items_table where barcode=?", new Object[] {
                barcode
        });
    }

    public int removeItem(String name)
    {
        return jdbcTemplate.update("delete from items_table where name=?", new Object[] {
                name
        });
    }

    public int update(Item aItem) {
        return jdbcTemplate.update("insert into items_table (name, barcode, barcode_type, stores) values(?, ?, ?, ?)",
                aItem.getName(), aItem.getBarcode(), aItem.getBarcodeType(), aItem.getStores());
    }

    public Optional < Item > findItemByBarcode(String barcode) {
        return Optional.of(jdbcTemplate.queryForObject("select * from items_table where barcode=?", new Object[] {
                        barcode
                },
                new BeanPropertyRowMapper< Item >(Item.class)));
    }

//    public Optional < Item > findById(long id) {
//        return Optional.of(jdbcTemplate.queryForObject("select * from employees where id=?", new Object[] {
//                        id
//                },
//                new BeanPropertyRowMapper< Item >(Item.class)));
//    }



//
//    public int deleteById(long id) {
//        return jdbcTemplate.update("delete from employees where id=?", new Object[] {
//                id
//        });
//    }
//

//
//    public int update(Employee employee) {
//        return jdbcTemplate.update("update employees " + " set first_name = ?, last_name = ?, email_address = ? " + " where id = ?",
//                new Object[] {
//                        employee.getFirstName(), employee.getLastName(), employee.getEmailId(), employee.getId()
//                });
//    }
}