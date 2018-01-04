package inmethod.android.bt;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ScanRecord {
	int iLength = -999;
	byte byteType = (byte) 0;
	byte[] byteData = null;

	public int getLength() {
		return iLength;
	}

	public byte getType() {
		return byteType;
	}

	public byte[] getData() {
		return byteData;
	}

	public String ByteArrayToString(byte[] ba) {
		StringBuilder hex = new StringBuilder(ba.length * 2);
		for (byte b : ba)
			hex.append(b + " ");

		return hex.toString();
	}

	public ScanRecord(int length, byte type, byte[] data) {
		String decodedRecord = "";
		try {
			decodedRecord = new String(data, "UTF-8");

		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		iLength = length;
		byteType = type;
		byteData = data;
	}

	// ...

	public static List<ScanRecord> parseScanRecord(byte[] scanRecord) {
		List<ScanRecord> records = new ArrayList<ScanRecord>();
		if( scanRecord==null) return records;

		int index = 0;
		while (index < scanRecord.length) {
			int length = scanRecord[index++];
			// Done once we run out of records
			if (length == 0)
				break;

			byte type = scanRecord[index];
			// Done if our record isn't a valid type
			if ((type & 0xff) == 0) {
				System.out.println("parse scanrecord error = unknow type");
				break;
			}

			byte[] data = Arrays.copyOfRange(scanRecord, index + 1, index + length);

			records.add(new ScanRecord(length, type, data));
			// Advance
			index += length;
		}

		return records;
	}

}