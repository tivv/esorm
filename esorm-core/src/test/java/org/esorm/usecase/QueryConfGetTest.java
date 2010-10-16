/**
 * 
 * Copyright 2010 Vitalii Tymchyshyn
 * This file is part of EsORM.
 *
 * EsORM is free software: you can redistribute it and/or modify
 * it under the terms of the Lesser GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * EsORM is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with EsORM.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.esorm.usecase;

import javax.sql.DataSource;

import static org.esorm.EsormUtils.*;
import org.esorm.QueryConf;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.unitils.UnitilsJUnit4TestClassRunner;
import org.unitils.database.annotations.TestDataSource;
import org.unitils.dbunit.annotation.DataSet;

/**
 * @author Vitalii Tymchyshyn
 *
 */
@RunWith(UnitilsJUnit4TestClassRunner.class)
@DataSet
public class QueryConfGetTest
{
    @TestDataSource
    DataSource dataSource;
    
    @Test
    public void test() {
        QueryConf conf = new QueryConf()
            .connectionProvider(connect(dataSource))
            .entityLocation(EasyTable.class);
        EasyTable res = conf.get(EasyTable.class, 1L);
        Assert.assertEquals(1l, res.getId());
        Assert.assertEquals("test", res.getName());
    }
    
     
}
