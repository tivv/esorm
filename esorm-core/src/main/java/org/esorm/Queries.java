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
package org.esorm;

import org.esorm.qbuilder.QueryBuilder;

import java.util.Collection;

/**
 * @author Vitalii Tymchyshyn
 */
public class Queries {
    public static <R> QueryBuilder<R> byId(QueryRunner runner, final EntityConfiguration configuration) {
        return runner.<R>buildQuery().select(configuration).filter().id().eq().param().done();
    }

    public static <R> QueryBuilder<R> byIds(QueryRunner runner, final EntityConfiguration configuration, Object... ids) {
        return runner.<R>buildQuery().select(configuration).filter().id().in().values(ids).done();
    }

    public static <R> QueryBuilder<R> byIds(QueryRunner runner, final EntityConfiguration configuration, Collection ids) {
        return runner.<R>buildQuery().select(configuration).filter().id().in().values(ids).done();
    }

    public static <R> QueryBuilder<R> byIds(QueryRunner runner, final EntityConfiguration configuration, Iterable ids, int blockSize) {
        return runner.<R>buildQuery().select(configuration).filter().id().in().values(blockSize, ids).done();
    }
}
