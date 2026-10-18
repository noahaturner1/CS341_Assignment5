package turner;
import java.awt.EventQueue;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JButton;
import javax.swing.JTextArea;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import javax.swing.JFileChooser;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.io.File;
import javax.swing.JScrollPane;
import javax.swing.ScrollPaneConstants;

public class NumberCruncher {

	private String filePath = "";
	private JFrame frame;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					NumberCruncher window = new NumberCruncher();
					window.frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the application.
	 */
	public NumberCruncher() {
		initialize();
	}

	/**
	 * Initialize the contents of the frame.
	 */
	private void initialize() {
		frame = new JFrame();
		frame.setBounds(100, 100, 450, 300);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.getContentPane().setLayout(null);
		
		JLabel lbl_header = new JLabel("Number Cruncher");
		lbl_header.setBounds(168, 0, 111, 16);
		frame.getContentPane().add(lbl_header);
		
		JScrollPane scrollPane = new JScrollPane();
		scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
		scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS);
		scrollPane.setBounds(33, 88, 381, 156);
		frame.getContentPane().add(scrollPane);
		
		JTextArea txtArea_results = new JTextArea();
		scrollPane.setViewportView(txtArea_results);
		txtArea_results.setText("No data set chosen. Click 'Choose File' to input a data set");
		
		JButton btn_chooseFile = new JButton("Choose File");
		btn_chooseFile.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				
				//create a JFileChooser object and prompt the user to select a file to use as input
				
				JFileChooser fileChooser = new JFileChooser();
		        fileChooser.setDialogTitle("Select a data file");
		        fileChooser.setFileFilter(new FileNameExtensionFilter("Text files (*.txt)", "txt"));

		        int result = fileChooser.showOpenDialog(frame);

		        if (result == JFileChooser.APPROVE_OPTION) {
		            File selectedFile = fileChooser.getSelectedFile();
		            filePath = selectedFile.getAbsolutePath();
		            txtArea_results.setText(StatsCalculator.calculateResults(filePath));
		        }
				
			}
		});
		btn_chooseFile.setBounds(168, 47, 117, 29);
		frame.getContentPane().add(btn_chooseFile);
		
	}
}
