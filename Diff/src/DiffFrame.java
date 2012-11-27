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
	
	
	public DiffFrame() {
		this.setTitle("Differencing Tool");
		this.setSize(900, 500);
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
		panel.add(LeftTextPane, BorderLayout.WEST);
		panel.add(RightTextPane, BorderLayout.EAST);
		
		return new JScrollPane(panel);
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
						openLeftDialog();
			}
		});
		
		JMenuItem openRight = new JMenuItem("Open Right");
		openRight.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				openRightDialog();
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

		File.add(openLeft);
		File.add(openRight);
		File.add(diff);
		menuBar.add(File);
		
		return menuBar;
	}
	
	private void openLeftDialog() {
		if(!LeftTextPane.getText().isEmpty())
			LeftTextPane.setText(new String());
		
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
			BufferedReader reader = new BufferedReader(new FileReader(file));
			String line = null;
			LeftTextPane.getDocument().insertString(0, "", null);
			
			while ((line = reader.readLine()) != null) {
				LeftTextPane.getDocument().insertString(LeftTextPane.getDocument().getLength(), line + "\n", null);
			}
			reader.close();
		} catch (IOException e) { 
			e.printStackTrace();
		} catch (BadLocationException e) {
			e.printStackTrace();
		}
		
	}
	
	private void openRightDialog() {
		if(!RightTextPane.getText().isEmpty())
			RightTextPane.setText(new String());
		
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
			BufferedReader reader = new BufferedReader(new FileReader(file));
			String line = null;
			RightTextPane.getDocument().insertString(0, "", null);
			
			while ((line = reader.readLine()) != null) {
				RightTextPane.getDocument().insertString(RightTextPane.getDocument().getLength(), line + "\n", null);
			}
			
			reader.close();
		} catch (IOException e) { 
			e.printStackTrace();
		} catch (BadLocationException e) {
			e.printStackTrace();
		}
	}
	
	private void Diff() throws BadLocationException {
		// styles, may want to extract method later TODO
		StyleContext sc = new StyleContext();
		StyledDocument rightDoc = new DefaultStyledDocument(sc);
		StyledDocument leftDoc = new DefaultStyledDocument(sc);
		
		Style defaultStyle = sc.getStyle(StyleContext.DEFAULT_STYLE);
		
		Style matchStyle = sc.addStyle("MatchStyle", defaultStyle); 
		StyleConstants.setBackground(matchStyle, Color.WHITE);
		
		Style deletedStyle = sc.addStyle("DeletedStyle", defaultStyle);
		StyleConstants.setBackground(deletedStyle, Color.RED);
		
		Style addedStyle = sc.addStyle("AddedStyle", defaultStyle);
		StyleConstants.setBackground(addedStyle, Color.GREEN);

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
	 */
	public static void main(String[] args) {
		DiffFrame df = new DiffFrame();
		df.setVisible(true);

	}

}
