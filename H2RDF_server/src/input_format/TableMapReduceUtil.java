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


/**
 * Copyright 2008 The Apache Software Foundation
 *
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.client.HTable;
import org.apache.hadoop.hbase.client.Scan;
import org.apache.hadoop.hbase.io.ImmutableBytesWritable;
import org.apache.hadoop.hbase.mapreduce.HRegionPartitioner;
import org.apache.hadoop.hbase.mapreduce.TableMapper;
import org.apache.hadoop.hbase.mapreduce.TableOutputFormat;
import org.apache.hadoop.hbase.mapreduce.TableReducer;
import org.apache.hadoop.hbase.util.Base64;
import org.apache.hadoop.hbase.util.Bytes;
import org.apache.hadoop.io.Writable;
import org.apache.hadoop.io.WritableComparable;
import org.apache.hadoop.mapreduce.Job;

/**
 * Utility for {@link TableMapper} and {@link TableReducer}
 */
@SuppressWarnings("unchecked")
public class TableMapReduceUtil {
  
  /**
   * Use this before submitting a TableMap job. It will appropriately set up 
   * the job.
   * 
   * @param table  The table name to read from.
   * @param scan  The scan instance with the columns, time range etc.
   * @param mapper  The mapper class to use.
   * @param outputKeyClass  The class of the output key.
   * @param outputValueClass  The class of the output value.
   * @param job  The current job to adjust.
   * @throws IOException When setting up the details fails.
   */
	private static int no;
  public static void initTableMapperJob(String table, Scan scan,
      Class<? extends TableMapper> mapper, 
      Class<? extends WritableComparable> outputKeyClass, 
      Class<? extends Writable> outputValueClass, Job job) throws IOException {
    job.setInputFormatClass(FileTableInputFormat.class);
    if (outputValueClass != null) job.setMapOutputValueClass(outputValueClass);
    if (outputKeyClass != null) job.setMapOutputKeyClass(outputKeyClass);
    job.setMapperClass(mapper);
    job.getConfiguration().set(FileTableInputFormat.INPUT_TABLE, table);
    job.getConfiguration().set(FileTableInputFormat.SCAN,
      convertScanToString(scan));
  }

  
  public static void customInitTableMapperJob(Class<? extends TableMapper> mapper, 
	      Class<? extends WritableComparable> outputKeyClass, 
	      Class<? extends Writable> outputValueClass, Job job) throws IOException {
	    job.setInputFormatClass(FileTableInputFormat.class);
	    if (outputValueClass != null) job.setMapOutputValueClass(outputValueClass);
	    if (outputKeyClass != null) job.setMapOutputKeyClass(outputKeyClass);
	    job.setMapperClass(mapper);
	  }

  
  
  
  
  
  
  
  
  
  /**
   * Writes the given scan into a Base64 encoded string.
   * 
   * @param scan  The scan to write out.
   * @return The scan saved in a Base64 encoded string.
   * @throws IOException When writing the scan fails.
   */
  public static String convertScanToString(Scan scan) throws IOException {
    ByteArrayOutputStream out = new ByteArrayOutputStream();  
    DataOutputStream dos = new DataOutputStream(out);
    scan.write(dos);
    return Base64.encodeBytes(out.toByteArray());
  }

  /**
   * Converts the given Base64 string back into a Scan instance.
   * 
   * @param base64  The scan details.
   * @return The newly created Scan instance.
   * @throws IOException When reading the scan instance fails.
   */
  public static Scan convertStringToScan(String base64) throws IOException {
    ByteArrayInputStream bis = new ByteArrayInputStream(Base64.decode(base64));
    DataInputStream dis = new DataInputStream(bis);
    Scan scan = new Scan();
    scan.readFields(dis);
    return scan;
  }
  
  /**
   * Use this before submitting a TableReduce job. It will
   * appropriately set up the JobConf.
   * 
   * @param table  The output table.
   * @param reducer  The reducer class to use.
   * @param job  The current job to adjust.
   * @throws IOException When determining the region count fails. 
   */
  public static void initTableReducerJob(String table,
    Class<? extends TableReducer> reducer, Job job)
  throws IOException {
    initTableReducerJob(table, reducer, job, null);
  }

  /**
   * Use this before submitting a TableReduce job. It will
   * appropriately set up the JobConf.
   * 
   * @param table  The output table.
   * @param reducer  The reducer class to use.
   * @param job  The current job to adjust.
   * @param partitioner  Partitioner to use. Pass <code>null</code> to use 
   * default partitioner.
   * @throws IOException When determining the region count fails. 
   */
  public static void initTableReducerJob(String table,
    Class<? extends TableReducer> reducer, Job job, Class partitioner)
  throws IOException {
    job.setOutputFormatClass(TableOutputFormat.class);
    if (reducer != null) job.setReducerClass(reducer);
    job.getConfiguration().set(TableOutputFormat.OUTPUT_TABLE, table);
    job.setOutputKeyClass(ImmutableBytesWritable.class);
    job.setOutputValueClass(Writable.class);
    if (partitioner == HRegionPartitioner.class) {
      job.setPartitionerClass(HRegionPartitioner.class);
      HTable outputTable = new HTable(new HBaseConfiguration(
        job.getConfiguration()), table);
      int regions = outputTable.getRegionsInfo().size();
      if (job.getNumReduceTasks() > regions) {
        job.setNumReduceTasks(outputTable.getRegionsInfo().size());
      }
    } else if (partitioner != null) {
      job.setPartitionerClass(partitioner);
    }
  }
  
  /**
   * Ensures that the given number of reduce tasks for the given job 
   * configuration does not exceed the number of regions for the given table. 
   * 
   * @param table  The table to get the region count for.
   * @param job  The current job to adjust.
   * @throws IOException When retrieving the table details fails.
   */
  public static void limitNumReduceTasks(String table, Job job) 
  throws IOException { 
    HTable outputTable = new HTable(new HBaseConfiguration(
      job.getConfiguration()), table);
    int regions = outputTable.getRegionsInfo().size();
    if (job.getNumReduceTasks() > regions)
      job.setNumReduceTasks(regions);
  }

  /**
   * Sets the number of reduce tasks for the given job configuration to the 
   * number of regions the given table has. 
   * 
   * @param table  The table to get the region count for.
   * @param job  The current job to adjust.
   * @throws IOException When retrieving the table details fails.
   */
  public static void setNumReduceTasks(String table, Job job) 
  throws IOException { 
    HTable outputTable = new HTable(new HBaseConfiguration(
      job.getConfiguration()), table);
    int regions = outputTable.getRegionsInfo().size();
    job.setNumReduceTasks(regions);
  }
  
  /**
   * Sets the number of rows to return and cache with each scanner iteration.
   * Higher caching values will enable faster mapreduce jobs at the expense of
   * requiring more heap to contain the cached rows.
   * 
   * @param job The current job to adjust.
   * @param batchSize The number of rows to return in batch with each scanner
   * iteration.
   */
  public static void setScannerCaching(Job job, int batchSize) {
    job.getConfiguration().setInt("hbase.client.scanner.caching", batchSize);
  }


public static void addRow(String vars, String fname, String table, byte[] startrow, byte[] stoprow, String colfamily, Job job) {
	Scan newScan = new Scan();
	newScan.setStartRow(startrow);
	newScan.setStopRow(stoprow);
	newScan.addFamily(Bytes.toBytes(colfamily));
    try {
	    job.getConfiguration().set(FileTableInputFormat.INPUT_TABLE+no, table);
	    job.getConfiguration().set(FileTableInputFormat.VARS+no, vars);
	    job.getConfiguration().set(FileTableInputFormat.FNAME+no, fname);
		job.getConfiguration().set(FileTableInputFormat.SCANLIST+no, convertScanToString(newScan) );
	} catch (IOException e) {
		e.printStackTrace();
	}
	no++;
	
}


public static void newJob() {
	no=0;
}


public static void addCol(String vars, String fname, String table, byte[] startrow, byte[] stoprow, String col, Job job) {
	Scan newScan = new Scan();
	newScan.setStartRow(startrow);
	newScan.setStopRow(stoprow);
	newScan.addFamily(Bytes.toBytes(col));
	//System.out.println(Bytes.toString(newScan.getFamilies()[0]));
    try {
	    job.getConfiguration().set(FileTableInputFormat.INPUT_TABLE+no, table);
	    job.getConfiguration().set(FileTableInputFormat.VARS+no, vars);
	    job.getConfiguration().set(FileTableInputFormat.FNAME+no, fname);
		job.getConfiguration().set(FileTableInputFormat.SCANLIST+no, convertScanToString(newScan) );
	} catch (IOException e) {
		e.printStackTrace();
	}
	no++;
}

  
}
