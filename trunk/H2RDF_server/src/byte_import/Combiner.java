package byte_import;

import java.io.IOException;

import org.apache.hadoop.hbase.io.ImmutableBytesWritable;
import org.apache.hadoop.hbase.util.Bytes;
import org.apache.hadoop.mapreduce.Reducer;

public class Combiner extends Reducer<ImmutableBytesWritable, ImmutableBytesWritable, ImmutableBytesWritable, ImmutableBytesWritable> {
  	
	 

		private byte[] non=Bytes.toBytes("");
		public void reduce(ImmutableBytesWritable key, Iterable<ImmutableBytesWritable> values, Context context) throws IOException {

			try {
				context.write(key, new ImmutableBytesWritable(non,0,0));
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		    
		}
		
	}
