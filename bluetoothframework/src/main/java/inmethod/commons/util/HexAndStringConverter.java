package inmethod.commons.util;

public class HexAndStringConverter {

	private static final String HEXES = "0123456789ABCDEF";

	/**
	 * byte(array) convert to Hex String
	 * 
	 * @param raw
	 *            byte array
	 * @return Hex string
	 */
	public static String convertHexByteToHexString(byte[] raw) {
		if (raw == null) {
			return null;
		}
		final StringBuilder hex = new StringBuilder(2 * raw.length);
		for (final byte b : raw) {
			hex.append(HEXES.charAt((b & 0xF0) >> 4)).append(HEXES.charAt((b & 0x0F)));
		}
		return hex.toString();
	}

	/**
	 * byte convert to Hex String
	 * 
	 * @param raw
	 *            byte
	 * @return Hex string
	 */
	public static String convertHexByteToHexString(byte raw) {
		final StringBuilder hex = new StringBuilder(2);
		hex.append(HEXES.charAt((raw & 0xF0) >> 4)).append(HEXES.charAt((raw & 0x0F)));
		return hex.toString();
	}

	/**
	 * Hex String to Ascii code. String s = "48"; will be convert to "H";
	 * 
	 * @param strValue
	 *            hex String
	 * @return byte array(hex)
	 */
	public static String convertHexStringToAsciiString(String strValue) {
		int intCounts = strValue.length() / 2;
		String strReturn = "";
		byte byteReturn = 0;
		String strHex = "";
		int intHex = 0;
		byte byteData[] = new byte[intCounts];
		try {
			for (int intI = 0; intI < intCounts; intI++) {
				strHex = strValue.substring(0, 2);
				strValue = strValue.substring(2);
				intHex = Integer.parseInt(strHex, 16);
				if (intHex > 128)
					intHex = intHex - 256;
				byteData[intI] = (byte) intHex;
			}
			// byteReturn = byteData[1];
			// System.out.println(byteReturn );
			strReturn = new String(byteData, "ISO8859-1");
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return strReturn;
	}

	/**
	 * Convert Hex String to Byte . ex: Stirng s="48" will convert to byte 0x48
	 * 
	 * @param s
	 * 
	 * @return
	 */
	public static byte[] convertHexStringToHexByte(String s) throws NotByteException {
		try {
			int len = s.length();
			byte[] data = new byte[len / 2];
			for (int i = 0; i < len; i += 2) {
				if (Character.digit(s.charAt(i), 16) > 15 || Character.digit(s.charAt(i), 16) < 0)
					throw new NotByteException("not 0~F byte format!");
				data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4) + Character.digit(s.charAt(i + 1), 16));
			}
			return data;
		} catch (Exception ee) {
			ee.printStackTrace();
			throw new NotByteException("not byte stirng fromat");
		}
	}

	public static void main(String a[]) {
		try {
			System.out.println(convertHexStringToHexByte("gy0f")[0]);
		} catch (NotByteException ee) {
			System.out.println(ee.getMessage());
		}
		System.out.println(convertHexStringToAsciiString("48"));
		byte aByte[] = new byte[2];
		aByte[0] = 0x48;
		aByte[1] = 0x49;
		System.out.println(convertHexByteToHexString(aByte));
	}

	/**
	 * Byte Array value convert to Long Integer
	 * 
	 * @param data
	 * @return 0 if null or length=0
	 * 
	 */
	public static long convertByteArrayValueToLongInteger(byte[] data) {
		if (data == null)
			return 0x0;
		// ----------
		return (long) ((long) (0xff & data[0]) << 40 | (long) (0xff & data[1]) << 32 | (long) (0xff & data[2]) << 24
				| (long) (0xff & data[3]) << 16 | (long) (0xff & data[4]) << 8 | (long) (0xff & data[5]) << 0);
	}

	/**
	 * check bluetooth MAC address is between start MAC address and end MAC
	 * address. Mac Addres is 6 bytes , format is xx:xx:xx:yy:yy:yy
	 * 
	 * @param sSrcMac
	 *            target Check MAC Address
	 * @param sStartMac
	 *            start MAC Address
	 * @param sEndMan
	 *            End MAC Address
	 * @return
	 */
	public static boolean checkBlueToothAddressBetweenStartAndEnd(String sSrcMac, String sStartMac, String sEndMac)
			throws NotByteException {
		String[] macAddressParts = sSrcMac.split(":");
		// convert hex string to byte values
		byte[] macAddressBytes = new byte[6];
		for (int i = 0; i < 6; i++) {
			Integer hex = Integer.parseInt(macAddressParts[i], 16);
			macAddressBytes[i] = hex.byteValue();
		}
		byte[] startMac = new byte[6];
		byte[] endMac = new byte[6];
		startMac = HexAndStringConverter.convertHexStringToHexByte(sStartMac.replace(":", ""));
		endMac = HexAndStringConverter.convertHexStringToHexByte(sEndMac.replace(":", ""));

		System.out.println("start mac=" + sStartMac + ", long = " + convertByteArrayValueToLongInteger(startMac));
		System.out.println("end mac=" + sEndMac + ", long = " + convertByteArrayValueToLongInteger(endMac));
		System.out.println("target mac = " + sSrcMac + ",long=" + convertByteArrayValueToLongInteger(macAddressBytes));

		if (convertByteArrayValueToLongInteger(macAddressBytes) < convertByteArrayValueToLongInteger(startMac)
				|| convertByteArrayValueToLongInteger(macAddressBytes) > convertByteArrayValueToLongInteger(endMac)) {
			return false;
		} else
			return true;
	}
}
