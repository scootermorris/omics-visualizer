package dk.ku.cpr.OmicsVisualizer.internal.ui.table;

/*
 * #%L
 * Cytoscape Table Browser Impl (table-browser-impl)
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2010 - 2013 The Cytoscape Consortium
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


import static javax.swing.GroupLayout.DEFAULT_SIZE;
import static javax.swing.GroupLayout.Alignment.LEADING;

import java.awt.BorderLayout;
import java.awt.Component;
import java.util.List;

import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.RowSorter;
import javax.swing.RowSorter.SortKey;
import javax.swing.SortOrder;
import javax.swing.UIManager;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableModel;

import org.cytoscape.model.CyColumn;
import org.cytoscape.util.swing.IconManager;

import dk.ku.cpr.OmicsVisualizer.internal.model.OVManager;

public final class OVTableHeaderRenderer extends JPanel implements TableCellRenderer {

	private static final long serialVersionUID = 4656466166588715282L;

	private final JLabel nameLabel;
	private final JLabel sharedLabel;
	private final JLabel sortLabel;

	public OVTableHeaderRenderer(OVManager ovManager) {
		IconManager iconManager = ovManager.getService(IconManager.class);
		
		setBorder(UIManager.getBorder("TableHeader.cellBorder"));
		setBackground(UIManager.getColor("TableHeader.background"));
		
		nameLabel = new JLabel();
		nameLabel.setFont(UIManager.getFont("TableHeader.font"));
		nameLabel.setHorizontalAlignment(JLabel.CENTER);
		nameLabel.setForeground(UIManager.getColor("TableHeader.foreground"));
		
		sharedLabel = new JLabel();
		sharedLabel.setFont(iconManager.getIconFont(12.0f));
		sharedLabel.setForeground(UIManager.getColor("TextField.inactiveForeground"));
		
		sortLabel = new JLabel(IconManager.ICON_ANGLE_UP);
		sortLabel.setFont(iconManager.getIconFont(12.0f));
		sortLabel.setMinimumSize(sortLabel.getPreferredSize());
		sortLabel.setSize(sortLabel.getPreferredSize());
		
		JPanel panel = new JPanel();
		panel.setOpaque(false);
		setLayout(new BorderLayout());
		add(panel, BorderLayout.SOUTH);
		
		final GroupLayout layout = new GroupLayout(panel);
		panel.setLayout(layout);
		layout.setAutoCreateContainerGaps(false);
		layout.setAutoCreateGaps(true);
		
		layout.setHorizontalGroup(layout.createSequentialGroup()
			.addGroup(layout.createParallelGroup(Alignment.CENTER)
//				.addComponent(namespaceIconLabel)
				.addComponent(sharedLabel)
			)
			.addGroup(layout.createParallelGroup(Alignment.CENTER)
//				.addComponent(namespaceLabel, DEFAULT_SIZE, DEFAULT_SIZE, Short.MAX_VALUE)
				.addComponent(nameLabel, DEFAULT_SIZE, DEFAULT_SIZE, Short.MAX_VALUE)
			)
			.addComponent(sortLabel)
			.addGap(4)
		);
		
		layout.setVerticalGroup(layout.createSequentialGroup()
//			.addGroup(layout.createParallelGroup(LEADING, false)
//				.addComponent(namespaceIconLabel)
//				.addComponent(namespaceLabel)
//			)
//			.addGap(2)
			.addGroup(layout.createParallelGroup(LEADING, false)
				.addComponent(sharedLabel)
				.addComponent(nameLabel)
				.addComponent(sortLabel)
			)
			.addGap(2)
		);
	}
	
	@Override
	public Component getTableCellRendererComponent(final JTable table, final Object value, boolean isSelected,
			boolean hasFocus, int row, int col) {
		// 'value' is column header value of column 'col'
		// rowIndex is always -1
		// isSelected is always false
		// hasFocus is always false

		// Configure the component with the specified value
		final String colName = value != null ? value.toString() : "";
		
		
		nameLabel.setText(colName);
		
		sharedLabel.setText("");
		
		sortLabel.setText(" ");
		sortLabel.setForeground(UIManager.getColor("TextField.inactiveForeground"));
		
		setToolTipText(colName);
		
		if (!(table.getModel() instanceof OVTableModel)) {
			invalidate();
			return this;
		}

		final OVTableModel model = (OVTableModel) table.getModel();
		final CyColumn column = model.getDataTable().getColumn(colName);
		
		if (column != null) {
			StringBuilder toolTip = new StringBuilder("<html><div style=\"text-align: center;\">");
	
			if (column.getType() == List.class)
				toolTip.append("<b>").append(column.getName()).append("</b><br />(List of ")
					.append(getMinimizedType(column.getListElementType().getName())).append("s)");
			else
				toolTip.append("<b>").append(column.getName()).append("</b><br />(")
					.append(getMinimizedType(column.getType().getName())).append(")");
			
			if (column.getVirtualColumnInfo().isVirtual()) {
				toolTip.append("<br /><i>Network Collection Column</i></div></html>");
				sharedLabel.setText(IconManager.ICON_SITEMAP);
			} else {
				toolTip.append("</div></html>");
			}
	
			// Set tool tip if desired
			setToolTipText(toolTip.toString());
	
			//*****sorting icon**
			int index = -1;
			boolean ascending = true;
			
			RowSorter<? extends TableModel> rowSorter = table.getRowSorter();
			int modelColumn = table.convertColumnIndexToModel(col);
			List<? extends SortKey> sortKeys = rowSorter.getSortKeys();
			
			if (sortKeys.size() > 0) {
				SortKey key = sortKeys.get(0);
				
				if (key.getColumn() == modelColumn) {
					index = col;
					ascending = key.getSortOrder() == SortOrder.ASCENDING;
				}
			}
			
			if (col == index)
				sortLabel.setText(ascending ? IconManager.ICON_ANGLE_UP : IconManager.ICON_ANGLE_DOWN);
			
			if (col == index)
				sortLabel.setForeground(UIManager.getColor("TableHeader.foreground"));
		}

		invalidate();
		
		return this;
	}
	
	private String getMinimizedType (String type){
		return type.substring(type.lastIndexOf('.')+1);
	}
}
