/**
 *
 * Copyright 2010-2011 Vitalii Tymchyshyn
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
package org.esorm.impl;

import org.esorm.PreparedQuery;
import org.esorm.QueryRunner;
import org.esorm.qbuilder.QueryBuilder;

import java.util.Map;

/**
 * @author Vitalii Tymchyshyn
 */
public abstract class AQueryBuilder implements QueryBuilder{
    private final QueryRunner runner;

    public AQueryBuilder(QueryRunner runner) {
        this.runner = runner;
    }

    public <R> PreparedQuery<R> prepare() {
        return build().prepare(runner.getConnectionProvider().takeConnection());
    }

    public <R> PreparedQuery<R> prepare(Map<String, Object> params) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }
}
