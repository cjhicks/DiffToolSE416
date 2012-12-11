package leftPackage;

public class DumbClass {

	private int dumbInt;
	
	public String convertDumbIntToString() {
		return "" + getDumbInt();
	}
	
	public int getDumbInt() {
		return dumbInt;
	}
	
	public void setDumbInt(int i) {
		dumbInt = i;
	}
	
}
