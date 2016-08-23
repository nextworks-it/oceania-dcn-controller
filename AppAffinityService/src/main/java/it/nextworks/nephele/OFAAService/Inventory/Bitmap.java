package it.nextworks.nephele.OFAAService.Inventory;

public class Bitmap {
	
	private boolean[] bmp = new boolean[Const.T];
	
	//returns the element of index ind as an integer (i.e. 0 or 1)
	public Integer el(Integer ind){
		if (ind < Const.T) {
			if (bmp[ind]) return 1;
			else return 0;
		}
		else {
			throw new IllegalArgumentException("Access to inexistent timeslot");}
		
	}
	
	public String getBitmap() {
		String out = new String();
		for (Integer i=0; i<Const.T; i++) {
			out = out + el(i).toString();
		}
		return out;
	}

	public Bitmap(String in){
		if (in.length()!= Const.T) throw new IllegalArgumentException("Invalid timeslot length");
		for (Integer i=0; i<Const.T; i++){
			if (in.charAt(i)=='1') bmp[i]=true;
			else if (in.charAt(i)=='0') bmp[i]=false;
			else throw new IllegalArgumentException("Invalid timeslot character (not 1 nor 0)");
		}
	}
	
	public Bitmap(String in, boolean invert){
		if (in.length()!= Const.T) throw new IllegalArgumentException("Invalid timeslot length");
		for (Integer i=0; i<Const.T; i++){
			if ( (!(in.charAt(i)=='1')) && (!(in.charAt(i)=='0'))) throw new IllegalArgumentException("Invalid timeslot character (not 1 nor 0)");
			if (!(in.charAt(i)=='1' && invert ) ) bmp[i]=true;
			else bmp[i]=false;
		}
	}
	
	
	public Bitmap(boolean[] in){
		if (in.length!= Const.T) throw new IllegalArgumentException("Invalid timeslot length");
		else bmp = in;
	}
	

	public Bitmap(){
		bmp = new boolean[Const.T];
	}
	
	public Bitmap invert(){
		Bitmap bmp2= new Bitmap();
		for (Integer i=0;i<Const.T;i++){bmp2.bmp[i]= !this.bmp[i];}
		return bmp2;
	}
}
