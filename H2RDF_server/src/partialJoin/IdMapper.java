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
package partialJoin;

import java.io.IOException;
import java.util.Random;

import org.apache.hadoop.hbase.io.ImmutableBytesWritable;
import org.apache.hadoop.hbase.util.Bytes;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Mapper.Context;

public class IdMapper extends
Mapper<ImmutableBytesWritable, Text, ImmutableBytesWritable, Text> {

public void map(ImmutableBytesWritable key, Text value, Context context) throws IOException, InterruptedException {
	
	Random g1= new Random();
	Random g2 = new Random( g1.nextInt() );
	ImmutableBytesWritable k=new ImmutableBytesWritable();
	k.set(Bytes.toBytes(g2.nextLong()));
	context.write(k, value);

}
}
