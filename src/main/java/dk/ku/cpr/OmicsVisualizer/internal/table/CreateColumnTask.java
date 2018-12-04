package dk.ku.cpr.OmicsVisualizer.internal.table;

import java.util.Arrays;
import java.util.List;

import org.cytoscape.model.CyColumn;
import org.cytoscape.model.CyTable;
import org.cytoscape.model.CyTableManager;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.util.json.CyJSONUtil;
import org.cytoscape.work.ContainsTunables;
import org.cytoscape.work.ObservableTask;
import org.cytoscape.work.TaskMonitor;
import org.cytoscape.work.Tunable;
import org.cytoscape.work.json.JSONResult;

import dk.ku.cpr.OmicsVisualizer.internal.utils.ColumnTypeTunable;
import dk.ku.cpr.OmicsVisualizer.internal.utils.DataUtils;
import dk.ku.cpr.OmicsVisualizer.internal.utils.TableTunable;

/*
 * #%L
 * Cytoscape Core Task Impl (core-task-impl)
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2006 - 2017 The Cytoscape Consortium
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as 
 * published by the Free Software Foundation, either version 2.1 of the 
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Lesser Public License for more details.
 * 
 * You should have received a copy of the GNU General Lesser Public 
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/lgpl-2.1.html>.
 * #L%
 */

public class CreateColumnTask extends AbstractTableDataTask implements ObservableTask {
	
	@ContainsTunables
	public TableTunable tableTunable;

	@Tunable(description = "Name of column", context = "nogui", longDescription="The new column name", exampleStringValue = "Uncertainty")
	public String columnName;

	@ContainsTunables
	public ColumnTypeTunable columnType;

	CyTable table = null;

	public CreateColumnTask(final CyTableManager tableMgr) {
		super(tableMgr);
		tableTunable = new TableTunable(tableMgr);
		columnType = new ColumnTypeTunable();
	}

	@Override
	public void run(final TaskMonitor taskMonitor) {
		table = tableTunable.getTable();
		
		if (table == null) {
			taskMonitor.showMessage(TaskMonitor.Level.ERROR,  "Unable to find table '"+tableTunable.getTableString()+"'");
			return;
		}

		if (columnName == null) {
			taskMonitor.showMessage(TaskMonitor.Level.ERROR, "Column name must be specified");
			return;
		}
		
		columnName = columnName.trim();
		
		if (columnName.isEmpty()) {
			taskMonitor.showMessage(TaskMonitor.Level.ERROR, "Column name must not be blank");
			return;
		}

		CyColumn c = table.getColumn(columnName);
		if (c != null) {
			taskMonitor.showMessage(TaskMonitor.Level.ERROR, "Column '"+columnName+"' already exists in table: "+table.toString());
			return;
		}

		String baseTypeName = columnType.getColumnType();
		if (baseTypeName == null) {
			taskMonitor.showMessage(TaskMonitor.Level.ERROR, "Column type must be specified.");
			return;
		}

		Class<?> baseType = DataUtils.getType(baseTypeName);
		if (baseType.equals(List.class)) {
			String listTypeName = columnType.getListElementType();
			
			if (listTypeName == null) {
				taskMonitor.showMessage(TaskMonitor.Level.ERROR, "List element type must be specified for list columns.");
				return;
			}
			
			Class<?> listType = DataUtils.getType(listTypeName);
			table.createListColumn(columnName, listType, false);
			taskMonitor.showMessage(TaskMonitor.Level.INFO, "Created list column: "+columnName);
		} else {
			table.createColumn(columnName, baseType, false);
			taskMonitor.showMessage(TaskMonitor.Level.INFO, "Created column: "+columnName);
		}
	}

	@Override
	public List<Class<?>> getResultClasses() {	
		return Arrays.asList(CyColumn.class, String.class, JSONResult.class);	
	}

	@Override
	public Object getResults(Class requestedType) {
		if (requestedType.equals(CyColumn.class)) 
			return tableTunable.getTable().getColumn(columnName);
		if (requestedType.equals(String.class)) 		
			return columnName;

		if (requestedType.equals(JSONResult.class)) {
			JSONResult res = () -> {
				if (table == null || columnName == null) return "{}";
				return "{\"table\":"+table.getSUID()+",\"column\" : \"" + columnName + "\"}";
			};
			return res;
		}
		return null;
	}

}