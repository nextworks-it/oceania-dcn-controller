package it.nextworks.nephele.OFTranslator;

public class IPValidator {

	public static boolean validate(Integer[] IP) {
		if (IP == null) return false;
		if (IP.length != 4)
		{ return false; }
		for (Integer part : IP){
			if (!((-1<part) && (part<256))){
				return false;
			}
		}
		return true;
	}
}
