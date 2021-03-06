/*
 * Copyright 2006-2007 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.openspaces.itest.jdbc.dbcp;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.PreparedStatementCallback;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.AbstractTransactionalJUnit4SpringContextTests;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.sql.PreparedStatement;
import java.sql.SQLException;

import static org.junit.Assert.assertEquals;

/**
 * @author kimchy
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("classpath:/org/openspaces/itest/jdbc/dbcp/jdbc.xml")
public class DbcpJdbcTests  extends AbstractTransactionalJUnit4SpringContextTests {

    protected String[] getConfigLocations() {
        return new String[]{"/org/openspaces/itest/jdbc/dbcp/jdbc.xml"};
    }

     @Test public void testSimpleOperation() {
        jdbcTemplate.execute("create table Person(FirstName varchar2 INDEX, LastName varchar2)");
        jdbcTemplate.execute("insert into Person values(?,?)", new PreparedStatementCallback() {
            public Object doInPreparedStatement(PreparedStatement preparedStatement) throws SQLException, DataAccessException {
                for (int i = 0; i < 10; i++) {
                    preparedStatement.setString(1, "FirstName" + i);
                    preparedStatement.setString(2, "LastName" + i);
                    preparedStatement.executeUpdate();
                }
                return null;
            }
        });
        long count = jdbcTemplate.queryForLong("select count(*) from Person");
        assertEquals(10, count);
    }
}

