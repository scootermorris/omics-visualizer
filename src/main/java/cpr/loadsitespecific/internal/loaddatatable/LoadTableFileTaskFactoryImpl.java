package cpr.loadsitespecific.internal.loaddatatable;

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



import java.io.File;

import org.cytoscape.io.read.CyTableReader;
import org.cytoscape.io.read.CyTableReaderManager;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.task.read.LoadTableFileTaskFactory;
import org.cytoscape.work.AbstractTaskFactory;
import org.cytoscape.work.TaskIterator;

import cpr.loadsitespecific.internal.api_io.read.CyTableDoubleIDReaderManager;
import cpr.loadsitespecific.internal.io.read.CyTableDoubleIDReaderManagerImpl;
import cpr.loadsitespecific.internal.task.LoadSiteSpecificTableReaderTask;


public class LoadTableFileTaskFactoryImpl extends AbstractTaskFactory implements LoadTableFileTaskFactory {

	private final CyServiceRegistrar serviceRegistrar;

	public LoadTableFileTaskFactoryImpl(final CyServiceRegistrar serviceRegistrar) {
		this.serviceRegistrar = serviceRegistrar;
	}

	public TaskIterator createTaskIterator() {
		System.out.println("LoadTableFileTaskFactoryImpl::createTaskIterator()");
		return new TaskIterator(2, new LoadTableFileTask(serviceRegistrar));
	}

	@Override
	public TaskIterator createTaskIterator(final File file) {
		//*
		final CyTableReaderManager tableReaderMgr = serviceRegistrar.getService(CyTableReaderManager.class);
		final CyTableReader reader = tableReaderMgr.getReader(file.toURI(), file.toURI().toString());
		//*/
		//final CyTableReader reader = new LoadSiteSpecificTableReaderTask(serviceRegistrar);
		System.out.println("LoadTableFileTaskFactoryImpl::createTaskIterator("+file+")");

		return new TaskIterator(new CombineReaderAndMappingTask(reader, serviceRegistrar));
	}
}
