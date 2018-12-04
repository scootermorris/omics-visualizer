package dk.ku.cpr.OmicsVisualizer.internal.io.read;

/*
 * #%L
 * Cytoscape IO Impl (io-impl)
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


import org.cytoscape.io.DataCategory;
import org.cytoscape.io.read.CyTableReader;
import org.cytoscape.io.util.StreamUtil;

import dk.ku.cpr.OmicsVisualizer.internal.api_io.read.CyTableDoubleIDReaderManager;
import dk.ku.cpr.OmicsVisualizer.internal.api_io.read.InputStreamTaskFactory;

public class CyTableDoubleIDReaderManagerImpl extends GenericReaderManager<InputStreamTaskFactory, CyTableReader> implements
		CyTableDoubleIDReaderManager {

	public CyTableDoubleIDReaderManagerImpl(final StreamUtil streamUtil) {
		super(DataCategory.TABLE, streamUtil);
	}
}