package dk.ku.cpr.OmicsVisualizer.internal.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontFormatException;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.AbstractButton;
import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListCellRenderer;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.GroupLayout.ParallelGroup;
import javax.swing.GroupLayout.SequentialGroup;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JToolBar;
import javax.swing.LayoutStyle.ComponentPlacement;
import javax.swing.SwingUtilities;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;

import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.application.swing.CyColumnPresentationManager;
import org.cytoscape.application.swing.CyColumnSelector;
import org.cytoscape.application.swing.CytoPanelComponent2;
import org.cytoscape.application.swing.CytoPanelName;
import org.cytoscape.command.AvailableCommands;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNetworkManager;
import org.cytoscape.model.CyRow;
import org.cytoscape.model.events.RowSetRecord;
import org.cytoscape.model.events.RowsSetEvent;
import org.cytoscape.model.events.RowsSetListener;
import org.cytoscape.model.events.SelectedNodesAndEdgesEvent;
import org.cytoscape.model.events.SelectedNodesAndEdgesListener;
import org.cytoscape.model.subnetwork.CyRootNetwork;
import org.cytoscape.model.subnetwork.CyRootNetworkManager;
import org.cytoscape.task.destroy.DeleteTableTaskFactory;
import org.cytoscape.util.swing.IconManager;
import org.cytoscape.util.swing.LookAndFeelUtil;
import org.cytoscape.work.TaskIterator;
import org.cytoscape.work.swing.DialogTaskManager;

import dk.ku.cpr.OmicsVisualizer.internal.model.OVConnection;
import dk.ku.cpr.OmicsVisualizer.internal.model.OVManager;
import dk.ku.cpr.OmicsVisualizer.internal.model.OVShared;
import dk.ku.cpr.OmicsVisualizer.internal.model.OVTable;
import dk.ku.cpr.OmicsVisualizer.internal.task.FilterTaskFactory;
import dk.ku.cpr.OmicsVisualizer.internal.utils.ViewUtil;

public class OVCytoPanel extends JPanel
implements CytoPanelComponent2,
ActionListener,
PopupMenuListener,
RowsSetListener,
SelectedNodesAndEdgesListener {

	private static final long serialVersionUID = 1L;

	private OVManager ovManager;

	private JTable mainTable=null;
	private JScrollPane scrollPane=null;
	private OVTableModel mainTableModel=null;
	private final Font iconFont;
	private Font iconStringFont=null;
	private final Color filterActive;
	private final Color filterInactive;

	private IconManager iconManager=null;

	private GlobalTableChooser tableChooser=null;

	private JButton selectButton=null;
	private JButton filterButton=null;
	private JButton deleteTableButton=null;
	private JButton retrieveNetworkButton=null;
	private JButton connectButton=null;
	private JButton vizButton=null;

	private JPopupMenu columnSelectorPopupMenu=null;
	private CyColumnSelector columnSelector=null;

	private OVFilterWindow filterWindow=null;
	private OVConnectWindow connectWindow=null;
	private OVVisualizationWindow vizWindow=null;
	private OVRetrieveStringNetworkWindow retrieveWindow=null;

	private JPanel toolBarPanel=null;
	private SequentialGroup hToolBarGroup=null;
	private ParallelGroup vToolBarGroup=null;

	private OVTable displayedTable=null;

	private final  float ICON_FONT_SIZE = 22.0f;

	public OVCytoPanel(OVManager ovManager) {
		this.setLayout(new BorderLayout());
		this.setOpaque(!LookAndFeelUtil.isAquaLAF());
		this.ovManager=ovManager;

		iconManager = this.ovManager.getServiceRegistrar().getService(IconManager.class);
		iconFont = iconManager.getIconFont(ICON_FONT_SIZE);
		
		try {
			this.iconStringFont = Font.createFont(Font.TRUETYPE_FONT, OVCytoPanel.class.getResourceAsStream("/fonts/string.ttf"));
			this.iconStringFont = this.iconStringFont.deriveFont(ICON_FONT_SIZE);
		} catch (FontFormatException e) {
			this.iconStringFont=null;
		} catch (IOException e) {
			this.iconStringFont=null;
		}

		filterActive = new Color(0,153,0); // Green
		filterInactive = Color.BLACK;

		this.reload();
	}

	public void reload() {
		tableChooser = new GlobalTableChooser();
		tableChooser.addActionListener(this);
		final Dimension d = new Dimension(400, tableChooser.getPreferredSize().height);
		tableChooser.setMaximumSize(d);
		tableChooser.setMinimumSize(d);
		tableChooser.setPreferredSize(d);
		tableChooser.setSize(d);

		GlobalTableComboBoxModel tcModel = (GlobalTableComboBoxModel)tableChooser.getModel();
		for(OVTable table : ovManager.getOVTables()) {
			tcModel.addAndSetSelectedItem(table);

			// We look for a potential filter previously applied to the table
			if(table.getFilter() != null) {
				FilterTaskFactory factory = new FilterTaskFactory(this.ovManager, this);
				TaskIterator ti = factory.createTaskIterator(table);

				this.ovManager.executeTask(ti);
			}
		}

		initPanel(null);
	}

	private OVTable getLastAddedTable() {
		List<OVTable> ovTables = this.ovManager.getOVTables();

		if(ovTables.size() == 0)
			return null;

		return ovTables.get(ovTables.size()-1);
	}

	public String getIdentifier() {
		return OVShared.CYTOPANEL_NAME;
	}

	public Component getComponent() {
		return this;
	}

	public CytoPanelName getCytoPanelName() {
		return CytoPanelName.SOUTH;
	}

	public String getTitle() {
		return "Omics Visualizer Tables";
	}

	public Icon getIcon() {
		return null;
	}

	public OVTable getDisplayedTable() {
		return this.displayedTable;
	}

	public void addToolBarComponent(final JComponent component, final ComponentPlacement placement) {
		if (placement != null)
			hToolBarGroup.addPreferredGap(placement);

		hToolBarGroup.addComponent(component);
		vToolBarGroup.addComponent(component, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE);
	}

	protected void styleButton(final AbstractButton btn, final Font font) {
		this.styleButton(btn, font, null);
	}

	protected void styleButton(final AbstractButton btn, final Font font, final Color color) {
		btn.setFont(font);
		btn.setBorder(null);
		btn.setContentAreaFilled(false);
		btn.setBorderPainted(false);

		if(color != null) {
			btn.setForeground(color);
		}

		int w = 32, h = 32;

		if (tableChooser != null)
			h = Math.max(h, tableChooser.getPreferredSize().height);

		btn.setMinimumSize(new Dimension(w, h));
		btn.setPreferredSize(new Dimension(w, h));
	}

	private JPopupMenu getColumnSelectorPopupMenu() {
		if (columnSelectorPopupMenu == null) {
			columnSelectorPopupMenu = new JPopupMenu();
			columnSelectorPopupMenu.add(getColumnSelector());
			columnSelectorPopupMenu.addPopupMenuListener(this);
			columnSelectorPopupMenu.addMouseListener(new MouseAdapter() {
				@Override
				public void mouseClicked(MouseEvent e) {
					if (SwingUtilities.isRightMouseButton(e)) {
						columnSelectorPopupMenu.setVisible(false);
					}
				}
			});
		}

		return columnSelectorPopupMenu;
	}

	private CyColumnSelector getColumnSelector() {
		if (columnSelector == null) {
			IconManager iconManager = ovManager.getService(IconManager.class);
			CyColumnPresentationManager presentationManager = ovManager.getService(CyColumnPresentationManager.class);
			columnSelector = new CyColumnSelector(iconManager, presentationManager);
		}

		return columnSelector;
	}
	
	public OVFilterWindow getFilterWindow() {
		if(this.filterWindow == null) {
			this.filterWindow = new OVFilterWindow(this.ovManager);
		}
		
		return this.filterWindow;
	}

	public OVConnectWindow getConnectWindow() {
		if(this.connectWindow == null) {
			this.connectWindow = new OVConnectWindow(this.ovManager);
		}

		return this.connectWindow;
	}

	public OVVisualizationWindow getVisualizationWindow() {
		if(this.vizWindow == null) {
			this.vizWindow = new OVVisualizationWindow(this.ovManager);
		}

		return this.vizWindow;
	}

	public OVRetrieveStringNetworkWindow getRetrieveWindow() {
		if(this.retrieveWindow == null) {
			this.retrieveWindow = new OVRetrieveStringNetworkWindow(this.ovManager);
		}

		return this.retrieveWindow;
	}
	
	public void initPanel(OVTable ovTable) {
		this.initPanel(ovTable, this.ovManager.getService(CyApplicationManager.class).getCurrentNetwork());
	}

	public void initPanel(OVTable ovTable, CyNetwork currentNetwork) {
		this.removeAll();

		if(ovTable==null) {
			ovTable = this.getLastAddedTable();
		}

		if(!ovTable.equals(this.displayedTable)) {
			if(this.connectWindow != null) {
				this.connectWindow.setVisible(false);
			}
			if(this.vizWindow != null) {
				this.vizWindow.setVisible(false);
			}
		}

		this.displayedTable = ovTable;

		// We check for selected rows
		CyApplicationManager applicationManager = this.ovManager.getService(CyApplicationManager.class);
		this.displayedTable.displaySelectedRows(applicationManager.getCurrentNetwork());

		JTable currentTable=ovTable.getJTable();

		JToolBar toolBar = new JToolBar();
		toolBar.setFloatable(false);
		toolBar.setOpaque(!LookAndFeelUtil.isAquaLAF());
		toolBar.setOrientation(JToolBar.HORIZONTAL);

		final GroupLayout layout = new GroupLayout(toolBar);
		toolBar.setLayout(layout);
		hToolBarGroup = layout.createSequentialGroup();
		vToolBarGroup = layout.createParallelGroup(Alignment.CENTER, false);

		// Layout information.
		layout.setHorizontalGroup(layout.createParallelGroup(Alignment.LEADING).addGroup(hToolBarGroup));
		layout.setVerticalGroup(vToolBarGroup);

		if (selectButton == null) {
			selectButton = new JButton(IconManager.ICON_COLUMNS);
			selectButton.setToolTipText("Show Columns");
			styleButton(selectButton, iconFont);

			selectButton.addActionListener(e -> {
				if (this.mainTableModel != null) {
					getColumnSelector().update(this.displayedTable.getColumnsInOrder(),
							this.displayedTable.getVisibleColumns());
					getColumnSelectorPopupMenu().pack();
					getColumnSelectorPopupMenu().show(selectButton, 0, selectButton.getHeight());
				}
			});
		}
		if (filterButton == null) {
			filterButton = new JButton(IconManager.ICON_FILTER);
			filterButton.setToolTipText("Filter rows");

			filterButton.addActionListener(e -> {
//				FilterTaskFactory factory = new FilterTaskFactory(this.ovManager, this);
//				this.ovManager.executeTask(factory.createTaskIterator());
				this.getFilterWindow().setVisible(true);
			});
		}
		if(this.displayedTable.getFilter() == null) {
			styleButton(filterButton, iconFont, filterInactive);
		} else {
			styleButton(filterButton, iconFont, filterActive);
		}

		if (deleteTableButton == null) {
			deleteTableButton = new JButton(IconManager.ICON_TABLE + "" + IconManager.ICON_TIMES_CIRCLE);
			deleteTableButton.setToolTipText("Delete Table...");
			styleButton(deleteTableButton, iconManager.getIconFont(ICON_FONT_SIZE / 2.0f));

			// Create pop-up window for deletion
			deleteTableButton.addActionListener(e -> removeTable());
		}
		if(retrieveNetworkButton == null) {
//			retrieveNetworkButton = new JButton(IconManager.ICON_NAVICON);
			if(this.iconStringFont == null) { // We use the image instead of the font
				retrieveNetworkButton = new JButton(new ImageIcon(OVCytoPanel.class.getResource("/images/string_logo_22.png")));
				styleButton(retrieveNetworkButton, iconFont);
			} else {
				// In the "String Font", the character "a" is the String logo
				retrieveNetworkButton = new JButton("a");
				styleButton(retrieveNetworkButton, iconStringFont);
			}
			retrieveNetworkButton.setToolTipText("Retrieve and connect the table with a String Network...");
			
			retrieveNetworkButton.addActionListener(e -> {
				AvailableCommands availableCommands = (AvailableCommands) this.ovManager.getService(AvailableCommands.class);
				if (!availableCommands.getNamespaces().contains("string")) {
					JOptionPane.showMessageDialog(null,
							"You need to install stringApp from the App Manager or Cytoscape App Store.",
							"Dependency error", JOptionPane.ERROR_MESSAGE);
					return;
				}
				
				this.getRetrieveWindow().setVisible(true);
			});
		}
		if (connectButton == null ) {
			connectButton = new JButton(IconManager.ICON_LINK);
			connectButton.setToolTipText("Manage table connections...");
			styleButton(connectButton, iconFont);

			connectButton.addActionListener(e -> {
				if(this.displayedTable != null) {
					this.getConnectWindow().setVisible(true);
				}
			});
		}
		connectButton.setEnabled(this.displayedTable != null && this.ovManager.getNetworkManager().getNetworkSet().size() != 0);
		if (vizButton == null ) {
			vizButton = new JButton(IconManager.ICON_PAINT_BRUSH);
			vizButton.setToolTipText("Apply visualization to the connected networks...");
			styleButton(vizButton, iconFont);

			vizButton.addActionListener(e -> {
				if(this.displayedTable != null && this.displayedTable.isConnected()) {
					//					resetCharts();

					AvailableCommands availableCommands = (AvailableCommands) this.ovManager.getService(AvailableCommands.class);
					if (!availableCommands.getNamespaces().contains("enhancedGraphics")) {
						JOptionPane.showMessageDialog(null,
								"You need to install enhancedGraphics from the App Manager or Cytoscape App Store.",
								"Dependency error", JOptionPane.ERROR_MESSAGE);
						return;
					}

					this.getVisualizationWindow().setTable(this.displayedTable);
					this.getVisualizationWindow().setVisible(true);
				}
			});
		}
		vizButton.setEnabled(this.displayedTable != null && this.displayedTable.isConnectedTo(currentNetwork));

		addToolBarComponent(selectButton, ComponentPlacement.RELATED);
		// TODO Version 1.0: Without filters
//		addToolBarComponent(filterButton, ComponentPlacement.RELATED);
		addToolBarComponent(deleteTableButton, ComponentPlacement.RELATED);
		addToolBarComponent(retrieveNetworkButton, ComponentPlacement.RELATED);
		addToolBarComponent(connectButton, ComponentPlacement.RELATED);
		addToolBarComponent(vizButton, ComponentPlacement.RELATED);

		if (tableChooser != null) {
			hToolBarGroup.addGap(0, 20, Short.MAX_VALUE);
			addToolBarComponent(tableChooser, ComponentPlacement.UNRELATED);
		}

		toolBarPanel = new JPanel();
		toolBarPanel.setLayout(new BorderLayout());
		toolBarPanel.setOpaque(!LookAndFeelUtil.isAquaLAF());
		toolBarPanel.add(toolBar, BorderLayout.CENTER);

		// System.out.println("show table: " + showTable);
		scrollPane = new JScrollPane(currentTable);

		this.setLayout(new BorderLayout());
		this.add(scrollPane, BorderLayout.CENTER);
		this.add(toolBarPanel, BorderLayout.NORTH);

		final GlobalTableComboBoxModel comboBoxModel = (GlobalTableComboBoxModel) tableChooser.getModel();
		comboBoxModel.addAndSetSelectedItem(ovTable);

		this.mainTable = currentTable;
		this.mainTableModel = (OVTableModel)this.mainTable.getModel();

		this.revalidate();
		this.repaint();
	}

	public void update() {
		this.initPanel(this.displayedTable);
	}

	private void removeTable() {
		final OVTable table = this.displayedTable;

		String title = "Please confirm this action";
		String msg = "Delete table \""+table.getTitle()+"\"?";
		int confirmValue = JOptionPane.showConfirmDialog(this, msg, title, JOptionPane.YES_NO_OPTION);

		// if user selects yes delete the table
		if (confirmValue == JOptionPane.YES_OPTION) {
			table.disconnectAll();

			final DialogTaskManager taskMgr = ovManager.getService(DialogTaskManager.class);
			final DeleteTableTaskFactory deleteTableTaskFactory =
					ovManager.getService(DeleteTableTaskFactory.class);

			taskMgr.execute(deleteTableTaskFactory.createTaskIterator(table.getCyTable()));
			removeTable(table);
		}
	}

	private void removeTable(OVTable ovTable) {
		this.ovManager.removeOVTable(ovTable);

		final GlobalTableComboBoxModel comboBoxModel = (GlobalTableComboBoxModel) tableChooser.getModel();
		comboBoxModel.removeItem(ovTable);

		if(this.ovManager.getOVTables().size() == 0) {
			// No more Omics Visualizer tables, we unregister the panel
			this.ovManager.unregisterOVCytoPanel();
		} else {
			initPanel(null);
		}
	}

	@Override
	public void actionPerformed(final ActionEvent e) {
		final OVTable table = (OVTable) tableChooser.getSelectedItem();

		if (table == displayedTable || table == null)
			return;

		//		serviceRegistrar.getService(CyApplicationManager.class).setCurrentTable(table);
		//		showSelectedTable();
		initPanel(table);
	}

	// Code from cytoscape/table-browser-impl view/GlobalTableBrowser.java
	@SuppressWarnings("serial")
	private class GlobalTableChooser extends JComboBox<OVTable> {

		private final Map<OVTable, String> tableToStringMap;

		GlobalTableChooser() {
			tableToStringMap = new HashMap<>();
			setModel(new GlobalTableComboBoxModel(tableToStringMap));
			setRenderer(new TableChooserCellRenderer(tableToStringMap));
		}
	}

	// Code from cytoscape/table-browser-impl view/GlobalTableBrowser.java
	@SuppressWarnings("serial")
	private class GlobalTableComboBoxModel extends DefaultComboBoxModel<OVTable> {

		private final Comparator<OVTable> tableComparator;
		private final Map<OVTable, String> tableToStringMap;
		private final List<OVTable> tables;

		GlobalTableComboBoxModel(final Map<OVTable, String> tableToStringMap) {
			this.tableToStringMap = tableToStringMap;
			tables = new ArrayList<>();
			tableComparator = new Comparator<OVTable>() {
				@Override
				public int compare(final OVTable table1, final OVTable table2) {
					return table1.getTitle().compareTo(table2.getTitle());
				}
			};
		}

		private void updateTableToStringMap() {
			tableToStringMap.clear();

			for (final OVTable table : tables)
				tableToStringMap.put(table, table.getTitle());
		}

		@Override
		public int getSize() {
			return tables.size();
		}

		@Override
		public OVTable getElementAt(int index) {
			return tables.get(index);
		}

		void addAndSetSelectedItem(final OVTable newTable) {
			if (!tables.contains(newTable)) {
				tables.add(newTable);
				Collections.sort(tables, tableComparator);
				updateTableToStringMap();
				fireContentsChanged(this, 0, tables.size() - 1);
			}

			// This is necessary to avoid deadlock!
			ViewUtil.invokeOnEDT(() -> {
				setSelectedItem(newTable);
			});
		}

		void removeItem(final OVTable deletedTable) {
			if (tables.contains(deletedTable)) {
				tables.remove(deletedTable);

				if (tables.size() > 0) {
					Collections.sort(tables, tableComparator);
					setSelectedItem(tables.get(0));
				} else {
					setSelectedItem(null);
				}
			}
		}
	}

	@SuppressWarnings("serial")
	private class TableChooserCellRenderer extends DefaultListCellRenderer {

		private final Map<OVTable, String> tableToStringMap;

		TableChooserCellRenderer(final Map<OVTable, String> tableToStringMap) {
			this.tableToStringMap = tableToStringMap;
		}

		@Override
		public Component getListCellRendererComponent(final JList<?> list, final Object value,
				final int index, final boolean isSelected, final boolean cellHasFocus) {
			super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);

			if (isSelected) {
				setBackground(list.getSelectionBackground());
				setForeground(list.getSelectionForeground());
			} else {
				setBackground(list.getBackground());
				setForeground(list.getForeground());
			}

			if (value instanceof OVTable == false) {
				setText("-- No Table --");
				return this;
			}

			final OVTable table = (OVTable) value;
			String label = tableToStringMap.get(table);

			if (label == null)
				label = table == null ? "-- No Table --" : table.getTitle();

			setText(label);

			return this;
		}
	}

	@Override
	public void popupMenuCanceled(PopupMenuEvent e) {
		// Do nothing
	}

	@Override
	public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {
		// Update actual table
		try {
			if(e.getSource()==this.columnSelectorPopupMenu) {
				final Set<String> visibleAttributes = getColumnSelector().getSelectedColumnNames();
				displayedTable.setVisibleColumns(visibleAttributes);
				//			updateEnableState();
			}
		} catch (Exception ex) {
		}
	}

	@Override
	public void popupMenuWillBecomeVisible(PopupMenuEvent e) {
		// Do nothing
	}
	
	/**
	 * OVTable that was filtered by the selected network.
	 * In case a network is selected, 2 RowsSetEvent are triggered:
	 * - First the selected network event;
	 * - Then the unselected network event.
	 * If the two networks are connected to the same table, we want to filter the selected rows only to the selected network, not the unselected one.
	 */
	private OVTable selectedTable;

	@Override
	public void handleEvent(RowsSetEvent e) {
		// This method is called when a table change, it can be Network, Node or Edge
		// Here we only take care of Network selection changes, Node changes are taken care in another method
		// The CyApplicationManager.getCurrentNetwork() has not been changed yet, so we can not use it
		CyNetworkManager networkManager = this.ovManager.getNetworkManager();
		CyRootNetworkManager rootNetManager = this.ovManager.getService(CyRootNetworkManager.class);

		// We only look for "selected" changes
		if (e.containsColumn(CyNetwork.SELECTED)) {
			OVConnection ovCon = null;
			CyNetwork changedNetwork = null;
			CyRootNetwork changedRootNetwork = null;
			Boolean selected=false;

			Collection<RowSetRecord> columnRecords = e.getColumnRecords(CyNetwork.SELECTED);
			if(columnRecords.size() == 1) { // When there is a change with a network, only one row is modified
				// Even if there is only 1 record, we have to use a for-loop to access the data of a Collection
				for (RowSetRecord rec : columnRecords) {
					CyRow row = rec.getRow();

					// I do not know what FACADE is, but when a row is changed, the RowSetEvent is created twice:
					// one "regular" and one FACADE, the FACADE is the second one so we do not want to deal with the same event a second time
					if (row.toString().indexOf("FACADE") >= 0) {
						continue;
					}
					
					Long networkID = row.get(CyNetwork.SUID, Long.class);
					selected = row.get(CyNetwork.SELECTED, Boolean.class);
					// SUID is unique within all Cytoscape
					// Here we verify if the SUID we have is the SUID of a network
					if (networkManager.networkExists(networkID)) {
						changedNetwork = networkManager.getNetwork(networkID);
						changedRootNetwork =  rootNetManager.getRootNetwork(changedNetwork);
					}
				}

				if (changedRootNetwork != null) {
					// We have a network, we check if he is connected with an OVTable
					ovCon = this.ovManager.getConnection(changedRootNetwork);

					if(ovCon != null) {
						if(selected) {
							// If the network is selected, we display it
							this.initPanel(ovCon.getOVTable(), changedNetwork);
							// Some nodes may have already been selected before, we only display it
							ovCon.getOVTable().displaySelectedRows(changedNetwork);
							// We save the connected OVTable
							selectedTable = ovCon.getOVTable();
						} else { // The network is unselected
							// We check that the table is different from the one we just selected
							if(selectedTable==null || !ovCon.getOVTable().equals(selectedTable)) {
								ovCon.getOVTable().selectAllRows();
							}
							
							// We do not keep the table into memory
							selectedTable=null;
						}
					}
				}
			}
		}
	}

	@Override
	public void handleEvent(SelectedNodesAndEdgesEvent event) {
		if(event.nodesChanged()) {
			CyNetwork cyNetwork = event.getNetwork();
			if(cyNetwork == null) {
				return;
			}
			
			CyRootNetworkManager rootNetManager = this.ovManager.getService(CyRootNetworkManager.class);
			OVConnection ovCon = this.ovManager.getConnection(rootNetManager.getRootNetwork(cyNetwork));

			if(ovCon != null) {
				ovCon.getOVTable().displaySelectedRows(cyNetwork);
				this.update();
			}
		}
	}
}
