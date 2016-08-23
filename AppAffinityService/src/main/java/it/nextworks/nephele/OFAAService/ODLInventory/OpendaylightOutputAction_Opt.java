package it.nextworks.nephele.OFAAService.ODLInventory;


import it.nextworks.nephele.OFAAService.Inventory.OptOFOutput;

public class OpendaylightOutputAction_Opt extends OpendaylightOutputAction {
	
	private String timeslot;
	
	private int wavelength;
	
	
	public OpendaylightOutputAction_Opt(OptOFOutput inOutAction){
		super(inOutAction);
		
		timeslot = pad(inOutAction.gettBitmap());
		
		wavelength = inOutAction.getLambda();
		
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
