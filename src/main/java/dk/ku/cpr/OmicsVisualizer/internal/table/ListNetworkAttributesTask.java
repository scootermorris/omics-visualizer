package dk.ku.cpr.OmicsVisualizer.internal.table;

/*
 * #%L
 * Cytoscape Core Task Impl (core-task-impl)
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2006 - 2013 The Cytoscape Consortium
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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Arrays;

import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.command.StringToModel;
import org.cytoscape.model.CyColumn;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyTable;
import org.cytoscape.model.CyTableManager;
import org.cytoscape.work.ObservableTask;
import org.cytoscape.work.ContainsTunables;
import org.cytoscape.work.TaskMonitor;
import org.cytoscape.work.Tunable;
import org.cytoscape.work.json.JSONResult;

import dk.ku.cpr.OmicsVisualizer.internal.utils.CoreImplDocumentationConstants;
import dk.ku.cpr.OmicsVisualizer.internal.utils.DataUtils;

import org.cytoscape.util.json.CyJSONUtil;
import org.cytoscape.service.util.CyServiceRegistrar;

public class ListNetworkAttributesTask extends AbstractTableDataTask implements ObservableTask {
	final CyApplicationManager appMgr;
	private final CyServiceRegistrar serviceRegistrar;
	Collection<CyColumn> columnList = null;

	@Tunable(description="Network", context="nogui", longDescription=StringToModel.CY_NETWORK_LONG_DESCRIPTION, exampleStringValue=StringToModel.CY_NETWORK_EXAMPLE_STRING)
	public CyNetwork network = null;

	@Tunable (description="Namespace for table", context="nogui", longDescription=CoreImplDocumentationConstants.COLUMN_NAMESPACE_LONG_DESCRIPTION, exampleStringValue=CoreImplDocumentationConstants.COLUMN_NAMESPACE_EXAMPLE_STRING)
	public String namespace = "default";

	public ListNetworkAttributesTask(CyTableManager mgr, CyApplicationManager appMgr, CyServiceRegistrar serviceRegistrar) {
		super(mgr);
		this.appMgr = appMgr;
		this.serviceRegistrar = serviceRegistrar;
	}

	@Override
	public void run(final TaskMonitor taskMonitor) {
		if (network == null) network = appMgr.getCurrentNetwork();

		CyTable networkTable = getNetworkTable(network, CyNetwork.class, namespace);

		columnList = networkTable.getColumns();

		taskMonitor.showMessage(TaskMonitor.Level.INFO, "   Attributes for network "+DataUtils.getNetworkName(network)+":");
		for (CyColumn column: columnList) {
			if (column.getType().equals(List.class))
				taskMonitor.showMessage(TaskMonitor.Level.INFO, 
				            "        "+column.getName()+": "+DataUtils.getType(column.getListElementType())+" list");
			else
				taskMonitor.showMessage(TaskMonitor.Level.INFO, 
				            "        "+column.getName()+": "+DataUtils.getType(column.getType()));
		}
	}

	public Object getResults(Class requestedType) {
		if (requestedType.equals(String.class)) {
			String returnString = "[";
			for (CyColumn col: columnList) {
				returnString += col.getName()+",";
			}
			return returnString.substring(0, returnString.length()-1)+"]";
		} else if (requestedType.equals(JSONResult.class)) {
			JSONResult res = () -> {
				CyJSONUtil cyJSONUtil = serviceRegistrar.getService(CyJSONUtil.class);
				return cyJSONUtil.cyColumnsToJson(columnList);
			};
			return res;

		}
		return new ArrayList<CyColumn>(columnList);
	}

	public List<Class<?>> getResultClasses() {
		return Arrays.asList(List.class, String.class, JSONResult.class);
	}

}