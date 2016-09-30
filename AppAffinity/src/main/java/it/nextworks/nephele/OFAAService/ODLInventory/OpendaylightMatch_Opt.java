package it.nextworks.nephele.OFAAService.ODLInventory;

import it.nextworks.nephele.OFAAService.Inventory.OFComprehensiveMatch;

public class OpendaylightMatch_Opt extends OpendaylightMatch {
	
	//TODO Make these two container adhere to the recursive construction policy too.
	
	private String timeslot;
	
	private int wavelength;
	
	
	public OpendaylightMatch_Opt(OFComprehensiveMatch inMatch){
		super(inMatch);
				
		timeslot = pad(inMatch.getTimeBitmap());
		
		wavelength = inMatch.getLambda();
		
	}
	
	public String pad(String str){
		while (str.length() < 80){
			str = str + "0";
		}
		return str;
	}

	public String getTimeslot() {
		return timeslot;
	}

	public int getWavelength() {
		return wavelength;
	}
	
}
