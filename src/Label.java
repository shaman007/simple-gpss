/**
 * Defines GPSS LABEL structure:
 * "[a-zA-Z]+[0-9]*:"
 * Label 24.02.2004
 *
 * @author  Andrey Bondarenko 
 * @e-mail  bondarenko007@aport2000.ru
 * @license GPL
 */
public class Label {
	/**
	 * Label name
	 */
	public String name;
	/**
	 * Labeled block location in Schema::Global_Chain
	 */
	public int location;
	/** Creates a new instance of Label */
	public Label(String in, int i) {
		try {
			name = new String();
			name = in;
			location = i;
		} catch (Exception e) {
			System.out.println("<EXCEPTION function=Label(String in, int i)\">"
					+ e + "</EXCEPTION>");
		}
	}
}
