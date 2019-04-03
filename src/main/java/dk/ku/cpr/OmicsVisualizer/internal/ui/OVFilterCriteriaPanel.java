package dk.ku.cpr.OmicsVisualizer.internal.ui;

import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

import javax.swing.JComboBox;
import javax.swing.JTextField;

import dk.ku.cpr.OmicsVisualizer.internal.model.OVFilter;
import dk.ku.cpr.OmicsVisualizer.internal.model.OVFilterCriteria;
import dk.ku.cpr.OmicsVisualizer.internal.model.OVShared;
import dk.ku.cpr.OmicsVisualizer.internal.model.OVTable;
import dk.ku.cpr.OmicsVisualizer.internal.model.operators.Operator;

public class OVFilterCriteriaPanel extends OVFilterPanel implements ActionListener {
	private static final long serialVersionUID = 5410071931920025590L;
	
	private static String CHOOSE = "Choose column...";
	
	private boolean isInitialized;

	private JComboBox<String> selectColumn;
	private JComboBox<Operator> selectOperator;
	private JTextField fieldValue;
	private JComboBox<String> selectValue;

	private boolean isBool;
	
	public OVFilterCriteriaPanel(OVFilterPanel parent, OVTable ovTable) {
		super(parent, ovTable);
		
		this.isInitialized=false; // We do not want to trigger updates before it is fully initialized
		
		this.selectColumn = new JComboBox<>();
		this.selectColumn.addActionListener(this);
		this.selectColumn.setToolTipText("Choose the column for which the condition should apply.");

		this.selectOperator = new JComboBox<>();
		this.selectOperator.addActionListener(this);
		this.selectOperator.setToolTipText("Choose the operator used for the comparison.");

		this.fieldValue = new JTextField(10);
		this.fieldValue.setToolTipText("Type the value to compare with. It can be a regular expression.");

		this.selectValue = new JComboBox<>();
		this.selectValue.addItem("true");
		this.selectValue.addItem("false");
		this.selectValue.setToolTipText("Choose the value to compare with.");

		this.selectColumn.addItem(CHOOSE);
		for(String colName : ovTable.getColNames()) {
			if(!OVShared.isOVCol(colName) && ovTable.getColType(colName) != List.class) {
				this.selectColumn.addItem(colName);
			}
		}
		
		this.update(false);
		
		this.isInitialized=true;
	}
	
	@Override
	public OVFilter getFilter() {
		String colName = (String)this.selectColumn.getSelectedItem();
		
		if(colName.equals(CHOOSE)) {
			return null;
		}
		
		Operator operator = (Operator)this.selectOperator.getSelectedItem();
		String reference;
		
		if(this.isBool) {
			reference = (String)this.selectValue.getSelectedItem();
		} else {
			reference = this.fieldValue.getText();
		}
		
		return new OVFilterCriteria(colName, operator, reference);
	}

	@Override
	public void setFilter(OVFilter ovFilter) {
		if(ovFilter == null) {
			this.selectColumn.setSelectedIndex(0);
			return;
		}
		
		if(ovFilter instanceof OVFilterCriteria) {
			OVFilterCriteria filterCrit = (OVFilterCriteria) ovFilter;
			
			this.selectColumn.setSelectedItem(filterCrit.getColName());
			this.selectOperator.setSelectedItem(filterCrit.getOperator());
			this.selectValue.setSelectedItem(filterCrit.getReference());
			this.fieldValue.setText(filterCrit.getReference());
		} else {
			throw new ClassCastException("Cannot cast " + ovFilter.getClass().getName() + " into OVFilterCriteria.");
		}
	}
	
	@Override
	public void update(boolean up) {
		this.setLayout(new GridBagLayout());
		MyGridBagConstraints c = new MyGridBagConstraints();
		c.expandHorizontal().setAnchor("NW").setInsets(0, 0, 0, 0);
		
		this.add(this.selectColumn, c);
		
		if(!this.selectColumn.getSelectedItem().equals(CHOOSE)) {
			this.add(this.selectOperator, c.nextCol());
	
			if(!((Operator)this.selectOperator.getSelectedItem()).isUnary()) {
				c.nextRow().useNCols(2);
				if(this.isBool) {
					this.selectValue.setVisible(true);
					this.fieldValue.setVisible(false);
					this.add(this.selectValue, c);
				} else {
					this.selectValue.setVisible(false);
					this.fieldValue.setVisible(true);
					this.add(this.fieldValue, c);
				}
			} else {
				this.selectValue.setVisible(false);
				this.fieldValue.setVisible(false);
			}
		}
		
		if(this.isInitialized && up) {
			this.parent.update(up);
		}
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if(e.getSource() == this.selectColumn) {
			// We display only operators that are compatible with the column type
			String colName = (String) this.selectColumn.getSelectedItem();
			if(colName == null) {
				return;
			}
			
			if(colName.equals(CHOOSE)) {
				this.selectOperator.setEnabled(false);
				this.fieldValue.setEnabled(false);
				this.selectValue.setEnabled(false);
				
				return;
			} else {
				this.selectOperator.setEnabled(true);
				this.fieldValue.setEnabled(true);
				this.selectValue.setEnabled(true);
			}
			
			Operator previousSelected = (Operator)this.selectOperator.getSelectedItem();

			Class<?> colType = ovTable.getColType(colName);
			boolean isNumeric = (colType == Integer.class) ||
					(colType == Long.class) ||
					(colType == Double.class);
			boolean isString = (colType == String.class);
			this.isBool = (colType == Boolean.class);

			this.selectOperator.removeAllItems();
			for(Operator op : Operator.values()) {
				if(isNumeric && op.isNumeric()) {
					this.selectOperator.addItem(op);
				} else if(isString && op.isString()) {
					this.selectOperator.addItem(op);
				} else if(this.isBool && op.isBool()) {
					this.selectOperator.addItem(op);
				}
			}
			
			if(previousSelected != null) {
				this.selectOperator.setSelectedItem(previousSelected);
			}
			
			this.update(true);
		} else if(e.getSource() == this.selectOperator) {
			Operator selectedOperator = (Operator)this.selectOperator.getSelectedItem();
			if(selectedOperator == null) {
				return;
			}

			this.update(true);
		}
	}

}
