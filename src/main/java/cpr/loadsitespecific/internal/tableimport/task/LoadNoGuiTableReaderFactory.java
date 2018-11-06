package cpr.loadsitespecific.internal.tableimport.task;

/*
 * #%L
 * Cytoscape Table Import Impl (table-import-impl)
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


import org.cytoscape.io.read.CyTableReader;
import org.cytoscape.model.CyTable;
import org.cytoscape.model.CyTableManager;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.AbstractTaskFactory;
import org.cytoscape.work.TaskIterator;
import org.cytoscape.work.TaskMonitor;

// replaced by ImportNoGuiTableReaderFactory
// tableimport.internal.CyActivator
// deprecated:  Adam.Treister@gladstone.ucsf.edu

@Deprecated
public class LoadNoGuiTableReaderFactory extends AbstractTaskFactory {
	
	private final boolean fromURL;
	private final CyServiceRegistrar serviceRegistrar;

	/**
	 * Creates a new ImportAttributeTableReaderFactory object.
	 */
	@Deprecated	public LoadNoGuiTableReaderFactory(boolean fromURL, final CyServiceRegistrar serviceRegistrar) {
		this.fromURL = fromURL;
		this.serviceRegistrar = serviceRegistrar;
	}

	@Override
	public TaskIterator createTaskIterator() {
		LoadTableReaderTask readerTask = new LoadTableReaderTask(serviceRegistrar);
		
		if (fromURL) {
			return new TaskIterator(new SelectURLTableTask(readerTask, serviceRegistrar),
					readerTask, new AddLoadedTableTask(readerTask));
		} else {
			return new TaskIterator(new SelectFileTableTask(readerTask, serviceRegistrar),
					readerTask, new AddLoadedTableTask(readerTask));
		}
	}
	
	@Deprecated	class AddLoadedTableTask extends AbstractTask {
		
		private final CyTableReader reader;
		
		AddLoadedTableTask(final CyTableReader reader){
			this.reader = reader;
		}
		
		@Override
		public void run(TaskMonitor taskMonitor) throws Exception {
			if (this.reader != null && this.reader.getTables() != null) {
				final CyTableManager tableMgr = serviceRegistrar.getService(CyTableManager.class);
				
				for (CyTable table : reader.getTables())
					tableMgr.addTable(table);
			}
		}
	}
}
