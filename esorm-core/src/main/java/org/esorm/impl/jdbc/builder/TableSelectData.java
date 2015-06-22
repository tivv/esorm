/*-------------------------------------------------------------------------
*
* Copyright (c) 2003-2014, PostgreSQL Global Development Group
* Copyright (c) 2004, Open Cloud Limited.
*
*
*-------------------------------------------------------------------------
*/
package org.esorm.impl.jdbc.builder;

import org.esorm.entity.db.Column;
import org.esorm.entity.db.FromExpression;
import org.esorm.entity.db.FromExpressionQueryData;

class TableSelectData implements FromExpressionQueryData {
    private String alias;
    private String joinType;
    private FromExpression joinTo;
    private Iterable<Column> joinColumns;
    private Iterable<Column> joinToColumns;

    TableSelectData(String alias) {
        this.alias = alias;
    }

    TableSelectData(String alias, String joinType, FromExpression joinTo, Iterable<Column> joinColumns, Iterable<Column> joinToColumns) {
        this.alias = alias;
        this.joinType = joinType;
        this.joinTo = joinTo;
        this.joinColumns = joinColumns;
        this.joinToColumns = joinToColumns;
    }

    @Override
    public String getAlias() {
        return alias;
    }

    public String getJoinType() {
        return joinType;
    }

    public FromExpression getJoinTo() {
        return joinTo;
    }

    public Iterable<Column> getJoinColumns() {
        return joinColumns;
    }

    public Iterable<Column> getJoinToColumns() {
        return joinToColumns;
    }
}
