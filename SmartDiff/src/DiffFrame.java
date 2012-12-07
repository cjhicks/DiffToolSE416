import javax.swing.*;

import java.awt.*;
import java.awt.event.*;
import java.io.*;

import javax.swing.JFrame;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultHighlighter;
import javax.swing.text.DefaultStyledDocument;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyleContext;
import javax.swing.text.StyledDocument;


public class DiffFrame extends JFrame {
	
	private JTextPane LeftTextPane;
	private JTextPane RightTextPane;
	private JavaSourceData leftSourceData;
	private JavaSourceData rightSourceData;
	private StyleContext sc;
	private Style defaultStyle;
	private Style matchStyle;
	private Style deletedStyle;
	private Style addedStyle;
	private Style boldStyle;
	
	public DiffFrame() {
		this.setTitle("Differencing Tool");
		this.setSize(1000, 600);
		this.setDefaultCloseOperation(EXIT_ON_CLOSE);	
		this.setContentPane(createPanel());
	}

	private JPanel createPanel() {
		JPanel panel = new JPanel();
		panel.setLayout(new BorderLayout());
		panel.add(createMenuBar(), BorderLayout.NORTH);
		panel.add(createScrollPane(), BorderLayout.CENTER);
		return panel;
	}
	
	private JScrollPane createScrollPane() {
		JPanel panel = new JPanel();
		
		LeftTextPane = createTextPane();
		RightTextPane = createTextPane();
		
		leftSourceData = new JavaSourceData();
		rightSourceData = new JavaSourceData();
		
		panel.add(LeftTextPane, BorderLayout.WEST);
		panel.add(RightTextPane, BorderLayout.EAST);
		
		setStyles();
		
		return new JScrollPane(panel);
	}
	
	private void setStyles() {
		sc = new StyleContext();
		
		defaultStyle = sc.getStyle(StyleContext.DEFAULT_STYLE);
		
		matchStyle = sc.addStyle("MatchStyle", defaultStyle); 
		StyleConstants.setBackground(matchStyle, Color.WHITE);
		
		deletedStyle = sc.addStyle("DeletedStyle", defaultStyle);
		StyleConstants.setBackground(deletedStyle, Color.RED);
		
		addedStyle = sc.addStyle("AddedStyle", defaultStyle);
		StyleConstants.setBackground(addedStyle, Color.GREEN);
		
		boldStyle = sc.addStyle("BoldStyle", defaultStyle);
		StyleConstants.setBold(boldStyle, true);
		
	}

	private JTextPane createTextPane() {
		JTextPane textPane = new JTextPane();
		textPane.setEditable(false);
		textPane.setPreferredSize(new Dimension(this.getWidth()/2 - 10, this.getHeight()));
		return textPane;
	}

	private JMenuBar createMenuBar() {
		JMenuBar menuBar = new JMenuBar();
		
		JMenu File = new JMenu("File");
		JMenuItem openLeft = new JMenuItem("Open Left");
		openLeft.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				openDialog(LeftTextPane, leftSourceData);
			}
		});
		
		JMenuItem openRight = new JMenuItem("Open Right");
		openRight.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				openDialog(RightTextPane, rightSourceData);
			}
		});
		
		JMenuItem diff = new JMenuItem("Diff");
		diff.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				try {
					Diff();
				} catch (BadLocationException e) {
					e.printStackTrace();
				}
			}
		});
		
		JMenuItem parse = new JMenuItem("Parse");
		parse.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				try {
					parseDoc(LeftTextPane, leftSourceData);
					parseDoc(RightTextPane, rightSourceData);
				} catch (BadLocationException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
			}
		});

		File.add(openLeft);
		File.add(openRight);
		File.add(diff);
		File.add(parse);
		menuBar.add(File);
		
		return menuBar;
	}
	
	protected void parseDoc(JTextPane textPane, JavaSourceData sourceData) throws BadLocationException {
		if(textPane.getText() == null || textPane.getText().isEmpty())
			return;
		
		StyledDocument styledDoc = textPane.getStyledDocument();
		// parse doc to find method and field names
		
		for(String fieldName : sourceData.getFieldList()) {
			//TODO WARNING!!! This is NOT robust!!! Must add logic so it accurately seeks for the method and NOT instances of the method!!!!!!
			styledDoc.setCharacterAttributes(textPane.getText().replaceAll("\n", "").indexOf(fieldName), fieldName.length(), boldStyle, false);
		}
		
		for(String methodName : sourceData.getMethodList()) {
			//TODO WARNING!!! This is NOT robust!!! Must add logic so it accurately seeks for the method and NOT instances of the method!!!!!!
			styledDoc.setCharacterAttributes(textPane.getText().replaceAll("\n", "").lastIndexOf(methodName), methodName.length(), boldStyle, false);
		}
	}
	
	
	
	
	private void openDialog(JTextPane textPane, JavaSourceData sourceData) {
		if(!textPane.getText().isEmpty())
			textPane.setText(new String());
		
		textPane.setStyledDocument(new DefaultStyledDocument(sc));
		
		JFileChooser fileChooser = new JFileChooser();
		
		fileChooser.showOpenDialog(null);
		File file = fileChooser.getSelectedFile();
		
		if(file == null)
			return;
		/*
		* Obtained from homework SE/ComS 319 ...
		*/
		// for reading from "file"
		try
		{
			sourceData.setFile(file);
			
			BufferedReader reader = new BufferedReader(new FileReader(file));
			String line = null;
			textPane.getStyledDocument().insertString(0, "", null);
			
			while ((line = reader.readLine()) != null) {
				textPane.getStyledDocument().insertString(textPane.getStyledDocument().getLength(), line + "\n", null);
			}
			
			reader.close();
		} catch (IOException e) { 
			e.printStackTrace();
		} catch (BadLocationException e) {
			e.printStackTrace();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private void Diff() throws BadLocationException {
		
		StyledDocument rightDoc = new DefaultStyledDocument(sc);
		StyledDocument leftDoc = new DefaultStyledDocument(sc);

		String [] leftArray = LeftTextPane.getText().split("\n");
		String [] rightArray = RightTextPane.getText().split("\n");
		
		int j = 0;
		for(int i=0; i<leftArray.length; i++) {
			if(j >= rightArray.length) { // if more left over on left, then it was deleted

				leftArray[i] = leftArray[i].concat("\n");
				leftDoc.insertString(leftDoc.getLength(), leftArray[i], deletedStyle);
				
			}
			else if(leftArray[i].equals(rightArray[j])) { //continue
				
				printMatch(leftArray, rightArray, i, j, leftDoc, rightDoc, matchStyle);
				j++;
			} else {
				
				if(addedToRight(leftArray[i], rightArray, j)) { // if added to right
					while(!leftArray[i].equals(rightArray[j])) {

						leftDoc.insertString(leftDoc.getLength(), "\n", matchStyle); 					
						rightArray[j] = rightArray[j].concat("\n");
						rightDoc.insertString(rightDoc.getLength(), rightArray[j], addedStyle); 
						j++;
					}
					
					printMatch(leftArray, rightArray, i, j, leftDoc, rightDoc, matchStyle);
					j++;
					
				} else { //for now, this is the deleted case
					leftArray[i] = leftArray[i].concat("\n");
					leftDoc.insertString(leftDoc.getLength(), leftArray[i], deletedStyle); 
					rightDoc.insertString(rightDoc.getLength(), "\n", matchStyle); 	
				}
			}
		}

		for(;j<rightArray.length; j++) {
			rightArray[j] = rightArray[j].concat("\n");
			rightDoc.insertString(rightDoc.getLength(), rightArray[j], matchStyle);
		}
		
		LeftTextPane.setDocument(leftDoc);
		RightTextPane.setDocument(rightDoc);
	}

	private void printMatch(String[] leftArray, String[] rightArray, int i,
			int j, StyledDocument leftDoc, StyledDocument rightDoc, Style matchStyle) throws BadLocationException {
		
		leftArray[i] = leftArray[i].concat("\n");
		rightArray[j] = rightArray[j].concat("\n");
		
		leftDoc.insertString(leftDoc.getLength(), leftArray[i], matchStyle); 
		rightDoc.insertString(rightDoc.getLength(), rightArray[j], matchStyle); 
	}

	/**
	 * For now, a line was added to the right if somewhere in the right text, the
	 * line were searching for on the left is found. Then every line in between was
	 * added to the right.
     *
	 * @param leftArray
	 * @param rightArray
	 * @param leftIndex
	 * @param rightIndex
	 * @return
	 */
	private boolean addedToRight(String leftArrayString, String[] rightArray, int rightIndex) {
		for(int i=rightIndex; i<rightArray.length; i++) {
			if(leftArrayString.equals(rightArray[i])) {
				return true;
			}
		}
		return false;
	}

	/**
	 * @param args
	 * @throws Exception 
	 * @throws Exception 
	 */
	public static void main(String[] args) throws Exception {
		DiffFrame df = new DiffFrame();
		df.setVisible(true);
		
	}


}
