package leftPackage;

public class DumbClassComments {

	private int dumbInt;
	
	// this is a comment
	// to be removed to one line
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
