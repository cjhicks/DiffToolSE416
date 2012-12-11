package rightPackage;

public class DumbClass {

	private int dumbInt;
	
	public String convertDumbIntToString() {
		return String.valueOf(getDumbInt());
	}
	
	public void setDumbInt(int i) {
		dumbInt = i;
	}
	
	public int getDumbInt() {
		return dumbInt;
	}
	
}
