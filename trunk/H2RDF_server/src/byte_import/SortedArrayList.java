package byte_import;

import java.util.ArrayList;

import org.apache.hadoop.hbase.util.Bytes;

class SortedArrayList {
	ArrayList<byte[]> arr;
    public SortedArrayList() {
    	arr = new ArrayList<byte[]>();
	}

    public void insertSorted(byte[] value) {
        arr.add(value);
        for (int i = arr.size()-1; i > 0 && Bytes.BYTES_COMPARATOR.compare(value, arr.get(i-1)) < 0; i--) {
            byte[] tmp = arr.get(i);
            arr.set(i, arr.get(i-1));
            arr.set(i-1, tmp);
        }
    }

	public int size() {
		return arr.size();
	}

	public byte[] get(int i) {
		return arr.get(i);
	}
}

