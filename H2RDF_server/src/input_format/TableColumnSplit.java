/*******************************************************************************
 * Copyright (c) 2012 Nikos Papailiou. 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 * 
 * Contributors:
 *     Nikos Papailiou - initial API and implementation
 ******************************************************************************/
package input_format;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import javaewah.EWAHCompressedBitmap;

import org.apache.hadoop.hbase.HConstants;
import org.apache.hadoop.hbase.util.Bytes;
import org.apache.hadoop.io.Writable;

public class TableColumnSplit extends MyInputSplit 
	implements Writable, Comparable<TableColumnSplit> {
	
	private String table;
	private String vars;
	private String fname;
	private byte[] startRow;
	private byte[] stopRow;
	private String col;
	private String m_regionLocation;
	private EWAHCompressedBitmap regionBitmap;
	

	public TableColumnSplit() {
		this("", "", "", HConstants.EMPTY_BYTE_ARRAY, HConstants.EMPTY_BYTE_ARRAY, "", "", new EWAHCompressedBitmap());
		super.type=1;
	}




	public TableColumnSplit(String table, String vars, String fname, byte[] startrow, byte[] stoprow,  String col, final String location,
			EWAHCompressedBitmap regionBitmap) {
		super.type=1;
		this.table = table;
		this.vars = vars;
		this.fname = fname;
		this.startRow = startrow;
		this.stopRow = stoprow;
		this.col = col;
		this.m_regionLocation = location;
		this.regionBitmap = regionBitmap;
		
	}


	@Override
	public long getLength() throws IOException, InterruptedException {
		return 0;
	}

	@Override
	public String[] getLocations() throws IOException, InterruptedException {
		return new String[] {this.m_regionLocation};
	}

	public void readFields(DataInput in) throws IOException {
	     this.startRow = Bytes.readByteArray(in);
	     this.stopRow = Bytes.readByteArray(in);
	     this.table = Bytes.toString(Bytes.readByteArray(in));
	     this.col = Bytes.toString(Bytes.readByteArray(in));
	     this.vars = Bytes.toString(Bytes.readByteArray(in));
	     this.fname = Bytes.toString(Bytes.readByteArray(in));
	     this.m_regionLocation = Bytes.toString(Bytes.readByteArray(in));
	     this.regionBitmap = new EWAHCompressedBitmap();
	     regionBitmap.deserialize(in);
	}

	public void write(DataOutput out) throws IOException {
		Bytes.writeByteArray(out, this.startRow);
		Bytes.writeByteArray(out, this.stopRow);
		Bytes.writeByteArray(out, Bytes.toBytes(this.table));
		Bytes.writeByteArray(out, Bytes.toBytes(this.col));
		Bytes.writeByteArray(out, Bytes.toBytes(this.vars));
		Bytes.writeByteArray(out, Bytes.toBytes(this.fname));
		Bytes.writeByteArray(out, Bytes.toBytes(this.m_regionLocation));
		regionBitmap.serialize(out);
	}

	public int compareTo(TableColumnSplit o) {
		return Bytes.compareTo(getStartRow(), o.getStartRow());
	}


	public String getM_regionLocation() {
		return m_regionLocation;
	}


	public void setM_regionLocation(String location) {
		m_regionLocation = location;
	}

	public void init() {
		
	}


	public String getFname() {
		return fname;
	}


	public void setFname(String fname) {
		this.fname = fname;
	}


	public String getVars() {
		return vars;
	}


	public void setVars(String vars) {
		this.vars = vars;
	}


	public String getCol() {
		return col;
	}


	public void setCol(String col) {
		this.col = col;
	}


	public byte[] getStartRow() {
		return startRow;
	}


	public void setStartRow(byte[] startRow) {
		this.startRow = startRow;
	}


	public byte[] getStopRow() {
		return stopRow;
	}


	public void setStopRow(byte[] stopRow) {
		this.stopRow = stopRow;
	}


	public String getTable() {
		return table;
	}


	public void setTable(String table) {
		this.table = table;
	}


	public EWAHCompressedBitmap getRegionBitmap() {
		return regionBitmap;
	}


	public void setRegionBitmap(EWAHCompressedBitmap regionBitmap) {
		this.regionBitmap = regionBitmap;
	}
	

	

}
