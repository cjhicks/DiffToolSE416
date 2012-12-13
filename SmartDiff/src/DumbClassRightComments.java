package rightPackage;

public class DumbClassComments {

	private int dumbInt;
	
	// this is a comment
	public String convertDumbIntToString() {
		return String.valueOf(getDumbInt());
	}
	
	public void setDumbInt(int i) {
		dumbInt = i;
	}
	
	// hey I added a comment
	public int getDumbInt() {
		return dumbInt;
	}
	
}
