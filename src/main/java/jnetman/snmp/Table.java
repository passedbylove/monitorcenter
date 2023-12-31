package jnetman.snmp;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.snmp4j.smi.OID;
import org.snmp4j.smi.Variable;

public class Table {

	private Map<OID, Row> table;

	public Table() {
		table = new HashMap<OID, Row>();
	}

	public Row getRow(OID index) {
		return this.table.get(index);
	}

	public Row getRow(int index)
	{
		return this.table.get(new OID(new int[]{index}));
	}

	public void putRow(OID index, Row row) {
		this.table.put(index, row);
	}

	public Variable getVariable(OID index, OID column) {
		if (table.containsKey(index))
			return table.get(index).getVariable(column);
		else
			return null;
	}

	public void putVariable(OID index, OID column, Variable var) {
		if (!table.containsKey(index))
			table.put(index, new Row(index));
		table.get(index).putVariable(column, var);
	}

	public Map<OID, Row> getMap() {
		return this.table;
	}

	public String toString() {
		String text = "";
		for (Row row : this.table.values()) {
			text = text + "\nRow Index: " + row.getIndex() + " >> "
					+ row.toString();
		}
		return text;

	}

	public int getRowCount()
	{
		return this.table.values().size();
	}

	public int getColumnCount()
	{
		Optional<Row> firstRow = this.table.values().stream().findFirst();
		if(firstRow.isPresent())
		{
			return firstRow.get().getCount();
		}
		return 0;
	}

	public boolean isEmpty()
	{
		return  this.table.values().isEmpty();
	}

	public class Row {

		private OID index;
		// OID is the OID of the column
		private Map<OID, Variable> row = new HashMap<OID, Variable>();

		public Row(OID rowIndex) {
			this.index = rowIndex;
		}

		public OID getIndex() {
			return this.index;
		}

		public void putVariable(OID column, Variable var) {
			this.row.put(column, var);
		}

		public Variable getVariable(OID column) {
			return this.row.get(column);
		}

		public int getCount()
		{
			return row.values().size();
		}

		public String toString() {
			return Arrays.toString(row.values().toArray());
		}

		public Map<OID, Variable> getMap() {
			return this.row;
		}
	}

}
