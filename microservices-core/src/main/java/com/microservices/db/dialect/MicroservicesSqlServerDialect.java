package com.microservices.db.dialect;

import java.util.List;

import com.jfinal.plugin.activerecord.dialect.SqlServerDialect;
import com.microservices.db.model.Column;
import com.microservices.exception.MicroservicesException;
import com.microservices.utils.ArrayUtils;
import com.microservices.utils.StringUtils;

public class MicroservicesSqlServerDialect extends SqlServerDialect implements IMicroservicesModelDialect {

	@Override
	public String forFindByColumns(String table, String loadColumns, List<Column> columns, String orderBy, Object limit) {
		StringBuilder sqlBuilder = new StringBuilder("SELECT ");
		sqlBuilder.append(loadColumns).append(" FROM ").append(table).append(" ");

		appIfNotEmpty(columns, sqlBuilder);

		if (StringUtils.isNotBlank(orderBy)) {
			sqlBuilder.append(" ORDER BY ").append(orderBy);
		}

		if (limit == null) {
			return sqlBuilder.toString();
		}

		if (limit instanceof Number) {
			StringBuilder ret = new StringBuilder();
			ret.append("SELECT * FROM ( SELECT row_number() over (order by tempcolumn) temprownumber, * FROM ");
			ret.append(" ( SELECT TOP ").append(limit).append(" tempcolumn=0,");
			ret.append(sqlBuilder.toString().replaceFirst("(?i)select", ""));
			ret.append(")vip)mvp ");
			return ret.toString();

		} else if (limit instanceof String && limit.toString().contains(",")) {
			String[] startAndEnd = limit.toString().split(",");
			String start = startAndEnd[0];
			String end = startAndEnd[1];

			StringBuilder ret = new StringBuilder();
			ret.append("SELECT * FROM ( SELECT row_number() over (order by tempcolumn) temprownumber, * FROM ");
			ret.append(" ( SELECT TOP ").append(end).append(" tempcolumn=0,");
			ret.append(sqlBuilder.toString().replaceFirst("(?i)select", ""));
			ret.append(")vip)mvp where temprownumber>").append(start);
			return ret.toString();
		} else {
			throw new MicroservicesException("sql limit is error!,limit must is Number of String like \"0,10\"");
		}

	}

	@Override
	public String forPaginateSelect(String loadColumns) {
		return "SELECT " + loadColumns;
	}

	@Override
	public String forPaginateFrom(String table, List<Column> columns, String orderBy) {
		StringBuilder sqlBuilder = new StringBuilder(" FROM ").append(table);

		appIfNotEmpty(columns, sqlBuilder);

		if (StringUtils.isNotBlank(orderBy)) {
			sqlBuilder.append(" ORDER BY ").append(orderBy);
		}

		return sqlBuilder.toString();
	}

	private void appIfNotEmpty(List<Column> columns, StringBuilder sqlBuilder) {
		if (ArrayUtils.isNotEmpty(columns)) {
			sqlBuilder.append(" WHERE ");

			int index = 0;
			for (Column column : columns) {
				sqlBuilder.append(String.format(" %s %s ? ", column.getName(), column.getLogic()));
				if (index != columns.size() - 1) {
					sqlBuilder.append(" AND ");
				}
				index++;
			}
		}
	}

}
