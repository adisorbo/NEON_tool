package org.neon.pathsFinder.ui;

import java.awt.BorderLayout;

import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Cursor;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.DefaultCellEditor;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.border.Border;
import javax.swing.border.TitledBorder;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.neon.model.Condition;
import org.neon.model.Heuristic;
import org.neon.pathsFinder.engine.Parser;
import org.neon.pathsFinder.engine.PathsFinder;
import org.neon.pathsFinder.engine.XMLWriter;
import org.neon.pathsFinder.model.GrammaticalPath;
import org.neon.pathsFinder.model.Sentence;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;




public class AddPathsPanel {
	
	private JFrame frame;
	private JTextArea textArea = new JTextArea();
	
	private JFrame pathsFrame;
	private JScrollPane pathsPanel;
	
	private JScrollPane pane;
	
	
	private final Color green = new Color(0x16a085);

	private DefaultTableModel model; 

	
	
	public AddPathsPanel(){
		try{
			this.main();
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	
	
	public void main() throws MalformedURLException {
		SwingUtilities.invokeLater(new Runnable() {

			@Override
			public void run() {
				try{
					init();
				}catch(Exception e){
					e.printStackTrace();
				}
				
			}
			
		});
	}
	
	
	private void init() throws Exception{
		frame = new JFrame("Nlp Patterns Finder");
		frame.setLayout(new BorderLayout());
		
		// MenuBar
		JMenu fileMenu = new JMenu("File");
		JMenuBar menuBar = new JMenuBar();
		fileMenu.setForeground(Color.WHITE);
		
		// create fileMenu
		JMenuItem newItem = new JMenuItem("New");
		JMenuItem item = new JMenuItem("Open text file");
		JMenuItem exitItem = new JMenuItem("Exit");
		
		JMenu helpMenu = new JMenu("Help");
		helpMenu.setForeground(Color.WHITE);
		JMenuItem item2 = new JMenuItem("About...");
		
		item2.addActionListener(new AboutListener());
		item.addActionListener(new OpenListener());
		newItem.addActionListener(new NewListener());

		
		helpMenu.add(item2);
		fileMenu.add(newItem);
		fileMenu.add(item);
		
		
		menuBar.setBackground(green);
		
		menuBar.add(fileMenu);
		menuBar.add(helpMenu);
		
		frame.add(menuBar, BorderLayout.NORTH);
				
		TitledBorder border = new TitledBorder("Insert senentences here");
		border.setTitleJustification(TitledBorder.CENTER);
		pane = new JScrollPane(textArea);
		
		pane.setBorder(border);
		Border lineBorder = BorderFactory.createLineBorder(Color.BLACK);
		textArea.setLineWrap(true);
		textArea.setWrapStyleWord(true);
		textArea.setBorder(lineBorder);
		textArea.setSelectionColor(Color.BLUE);
		textArea.setSelectedTextColor(Color.YELLOW);
	    textArea.setBackground(Color.WHITE);
	    textArea.setForeground(Color.BLACK);
		
		
		
		Container container = new Container();
		container.setLayout(new BorderLayout());
		
		container.add(pane, BorderLayout.CENTER);
		
		frame.add(container, BorderLayout.CENTER);
					
		
		frame.setSize(700, 500);
		frame.setLocationRelativeTo(null);
		frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		frame.setVisible(true);
		JButton button = new JButton("Find Common Patterns");
		
		
		button.addActionListener(new FindPathsListener());

		button.getPreferredSize();
		frame.add(button, BorderLayout.PAGE_END);
		
	}
	
	
		
		
	
	private class OpenListener implements ActionListener{
		public void actionPerformed(ActionEvent e){
			File file;
			BufferedReader br = null;
			JFileChooser fc = new JFileChooser();
			FileNameExtensionFilter filter = new FileNameExtensionFilter("TEXT FILES", "txt", "text");
			fc.setFileSelectionMode(JFileChooser.FILES_ONLY);
			fc.setFileFilter(filter);
			fc.setSelectedFile(fc.getCurrentDirectory());
			fc.setDialogTitle("Open file");
			
			int retVal = fc.showOpenDialog(frame);
			if (retVal == fc.APPROVE_OPTION){
				file = fc.getSelectedFile();
				try{
					br = new BufferedReader(new FileReader(file));
					
				}catch (FileNotFoundException e1){
					JOptionPane.showMessageDialog(pane, "Unable to open"+file.getName(), "Error", JOptionPane.ERROR_MESSAGE);
				}
				try{
					String line = br.readLine();
					textArea.setText("");
					
					while (line!=null){
						textArea.append(line+"\n");
						line = br.readLine();
						
					}
				}catch(IOException e2){
					JOptionPane.showMessageDialog(pane, "Unable to open"+file.getName(), "Error", JOptionPane.ERROR_MESSAGE);
				}
			}
			
		}
	}
	
	private class NewListener implements ActionListener{
		public void actionPerformed(ActionEvent ae){
			textArea.setText("");
			
		}
	}
	
		    	
		    		
	
	
	private class AboutListener implements ActionListener{
		public void actionPerformed(ActionEvent ae){
			String message = "Nlp-based softwarE dOcumentation aNalyzer \n" +
					         "Version 1.0\n" +
					         "\nThis program is free software;"+ 
					         "\nyou can redistribute it and/or modify"+
					         "\nit under the terms of the GNU " +
					         "\nGeneral Public License as published " +
					         "\nby the Free Software Foundation; " +
					         "\neither version 2 of the License, " +
					         "\nor (at your option) any later version."+
					         "\n\nDeveloped by Andrea Di Sorbo\n" +
					         "\n2021";
			
			
			JOptionPane.showMessageDialog(frame, message, "About", JOptionPane.INFORMATION_MESSAGE);
		}
	}
	
	private class TextAreaRenderer extends JScrollPane implements TableCellRenderer
	{
	   private JTextArea area;
	   private static final long serialVersionUID = 3L;
	   
	   public TextAreaRenderer() {
	      area = new JTextArea();
	     
	      
	      area.setLineWrap(true);
	      area.setWrapStyleWord(true);
	      area.setEditable(false);
	         
	      
	      getViewport().add(area);
	   }
	  
	   public Component getTableCellRendererComponent(JTable table, Object value,
	                                  boolean isSelected, boolean hasFocus,
	                                  int row, int column)
	   {
	      if (isSelected) {
	         setForeground(table.getSelectionForeground());
	         setBackground(table.getSelectionBackground());
	         area.setForeground(table.getSelectionForeground());
	         area.setBackground(table.getSelectionBackground());
	      } else {
	         setForeground(table.getForeground());
	         setBackground(table.getBackground());
	         area.setForeground(table.getForeground());
	         area.setBackground(table.getBackground());
	      }
	  
	      area.setText((String) value);
	      area.setCaretPosition(0);
	      return this;
	   }
	}
	
	private class TextAreaEditor extends DefaultCellEditor {
		   protected JScrollPane scrollpane;
		   protected JTextArea textarea;
		   private static final long serialVersionUID = 2L;
		   
		   public TextAreaEditor() {
		      super(new JCheckBox());
		      scrollpane = new JScrollPane();
		      textarea = new JTextArea(); 
		      textarea.setLineWrap(true);
		      textarea.setWrapStyleWord(true);
		      textarea.setEditable(false);
		      scrollpane.getViewport().add(textarea);
		   }
		  
		   public Component getTableCellEditorComponent(JTable table, Object value,
		                                   boolean isSelected, int row, int column) {
		      textarea.setText((String) value);
		  
		      return scrollpane;
		   }
		  
		   public Object getCellEditorValue() {
		      return textarea.getText();
		   }
	}
	
	
	
	private class VectorTableCellRenderer extends JScrollPane implements TableCellRenderer {
	
			   JTextArea area;
			  
			   public VectorTableCellRenderer() {
			      area = new JTextArea();
			     
			      
			      area.setLineWrap(true);
			      area.setWrapStyleWord(true);

			         
			      
			      getViewport().add(area);
			   }
			  
			   public Component getTableCellRendererComponent(JTable table, Object value,
			                                  boolean isSelected, boolean hasFocus,
			                                  int row, int column)
			   {
			      if (isSelected) {
			         setForeground(table.getSelectionForeground());
			         setBackground(table.getSelectionBackground());
			         area.setForeground(table.getSelectionForeground());
			         area.setBackground(table.getSelectionBackground());
			      } else {
			         setForeground(table.getForeground());
			         setBackground(table.getBackground());
			         area.setForeground(table.getForeground());
			         area.setBackground(table.getBackground());
			      }
			      
			      area.setText("");
			      Vector<String> conditions = new Vector<String>((Vector<String>)value);
			      for (String s:conditions){
			    	  area.append(s+"\n");
			      }
			      
			      area.setCaretPosition(0);
			      return this;
			   }
			

		private static final long serialVersionUID = 1L;
	}

	
	
	private class VectorEditor extends DefaultCellEditor {
		   protected JScrollPane scrollpane;
		   protected JTextArea area;
		   private static final long serialVersionUID = 1L;
		   
		   public VectorEditor() {
		      super(new JCheckBox());
		      scrollpane = new JScrollPane();
		      area = new JTextArea(); 
		      area.setLineWrap(true);
		      area.setWrapStyleWord(true);
		      area.setEditable(true);
		      scrollpane.getViewport().add(area);
		   }
		  
		   public Component getTableCellEditorComponent(JTable table, Object value,
		                                   boolean isSelected, int row, int column) {
			   area.setText("");
			   if (value instanceof Vector){
		    	  
		    	  Vector<String> conditions = new Vector<String>((Vector) value);
		    	  for (String s: conditions){
		    		  area.append(s+"\n");
		    	  }
		      }
			         
		      return scrollpane;
		   }
		  
		   public Object getCellEditorValue() {
		      String[] conditions = area.getText().split("\n");
			  Vector<String> vectorOfConditions = new Vector<String>();
			  for (int i = 0; i<conditions.length; i++){
				vectorOfConditions.add(conditions[i]);  
			  }
			   
			   return vectorOfConditions;
		   }
	}

	private class BarThread extends Thread {
		  
		  public BarThread() {

		  }

		  public void run() {
	       
			JFrame frameProgressBar = new JFrame("Progress");
		    JPanel panelProgressBar = new JPanel();

			JProgressBar progressBar = new JProgressBar(0,100);
			progressBar.setValue(0);
		    
		    panelProgressBar.add(progressBar, BorderLayout.NORTH);
		    frameProgressBar.add(panelProgressBar);
		    				    
			frameProgressBar.setSize(300, 100);
			frameProgressBar.setLocationRelativeTo(null);
	        frameProgressBar.setVisible(true); 
           
		
			progressBar.setStringPainted(true);
			progressBar.setValue(0);
			frameProgressBar.update(frameProgressBar.getGraphics());
			
			
						
		    while (progressBar.getValue()<50) {
		          float percentage = Parser.getInstance().getProgress();
	              progressBar.setValue((int) percentage);
	              frameProgressBar.update(frameProgressBar.getGraphics());
	              
	            try {
					Thread.sleep(100);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
          }
		  
		  while (progressBar.getValue()<100) {
	          float percentage = PathsFinder.getScannedSentences();
              progressBar.setValue(50 + ((int) percentage));
              frameProgressBar.update(frameProgressBar.getGraphics());
              
            try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			  
		  }
		    
			Parser.getInstance().refreshProgress();
			PathsFinder.refreshScannedSentences();
			frameProgressBar.dispose();
		  }
		  
	}

	
	
	private class FindPathsListener implements ActionListener{
		@Override
		public void actionPerformed(ActionEvent ae) {
				
				model = new DefaultTableModel(null, new String [] {"Conditions", "Sentence Type","Use", "Heuristic", "Sample Sentence","Dependency Type", "Sentence Category"}) {
			        public Class getColumnClass(int cIndex) {
			          switch(cIndex){
			          case 0: return Vector.class;
			          case 1: return String.class;
			          case 2: return Boolean.class;
			          case 3: return String.class;
			          case 4: return String.class;
			          case 5: return String.class;
			          case 6: return String.class;
			          default: return String.class;
		          		}
		        	}
			        
		    	};
			    
				frame.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
				String textToClassify = textArea.getText();

				BarThread stepper = new BarThread();
				stepper.start();
				
				Parser ue = Parser.getInstance();

				
				
				ArrayList<Sentence> sentences = null;
				sentences = ue.parse(textToClassify);
				
				ArrayList<GrammaticalPath> paths = null;
				
				
				
				
				paths = PathsFinder.getInstance().discoverCommonPaths(sentences);
				
                
				
				
				
				pathsFrame = new JFrame("Discovered Common Patterns");
				
				pathsFrame.setSize(1200, 500);
				pathsFrame.setLocationRelativeTo(null);
				pathsFrame.setLayout(new BorderLayout());
	
			
				int rowCount = model.getRowCount();
				//Remove rows one by one from the end of the table
				for (int i = rowCount - 1; i >= 0; i--) {
				    model.removeRow(i);
				}
				
               JTable table = new JTable(model);
               
               int rowIndex = 0;
               
				for (GrammaticalPath gp: paths){
					
					Vector<String> ta = new Vector<String>();
					ta.addAll(gp.getConditions());
					model.addRow(new Object [] {ta, gp.identifySentenceType(), true, gp.getTemplateText(), gp.getShortestExampleSentence(), gp.getDependenciesPath(), ""});
							
							
					table.setRowHeight(rowIndex++, (table.getRowHeight()*(ta.size()+2)));
				
				
				}
				
			   table.getColumnModel().getColumn(0).setMinWidth(250);
			   table.getColumnModel().getColumn(0).setMaxWidth(250);
			   table.getColumnModel().getColumn(0).setPreferredWidth(250);
			   
			   table.getColumnModel().getColumn(1).setMinWidth(100);
			   table.getColumnModel().getColumn(1).setMaxWidth(100);
			   table.getColumnModel().getColumn(1).setPreferredWidth(100);
			   
			   table.getColumnModel().getColumn(2).setMinWidth(50);
			   table.getColumnModel().getColumn(2).setMaxWidth(50);
			   table.getColumnModel().getColumn(1).setPreferredWidth(50);
			   
			   table.getColumnModel().getColumn(0).setCellEditor(new VectorEditor());
               table.getColumnModel().getColumn(0).setCellRenderer(new VectorTableCellRenderer());
               
               table.getColumnModel().getColumn(4).setCellRenderer(new TextAreaRenderer());
               table.getColumnModel().getColumn(4).setCellEditor(new TextAreaEditor());
               
			   table.getColumnModel().getColumn(4).setPreferredWidth(300);
               table.getColumnModel().getColumn(4).setMinWidth(300);
			   table.getColumnModel().getColumn(4).setMaxWidth(300);
			   
			   table.getColumnModel().getColumn(4).setPreferredWidth(300);
               table.getColumnModel().getColumn(5).setMinWidth(100);
			   table.getColumnModel().getColumn(5).setMaxWidth(100);
			   table.getColumnModel().getColumn(5).setPreferredWidth(100);

               table.getColumnModel().getColumn(0).setResizable(true);
               table.getColumnModel().getColumn(1).setResizable(true);
               table.getColumnModel().getColumn(2).setResizable(true);
               table.getColumnModel().getColumn(3).setResizable(true);
               table.getColumnModel().getColumn(4).setResizable(true);
               table.getColumnModel().getColumn(5).setResizable(true);
               table.getColumnModel().getColumn(6).setResizable(true);
               
               pathsPanel = new JScrollPane(table);
               pathsFrame.add(pathsPanel);
				
               JButton addButton = new JButton("Add Selected Patterns");
               
               
               addButton.addActionListener(new AddListener());
                
               pathsFrame.add(addButton, BorderLayout.SOUTH);
               
               pathsFrame.setVisible(true);
				
				
			   JOptionPane.showMessageDialog(pathsPanel, "DONE!");
			   frame.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));

			
			}
		
	}
	
	private class AddListener implements ActionListener{
			@SuppressWarnings("unchecked")
			@Override
				public void actionPerformed(ActionEvent ae){
					JFileChooser fc2 = new JFileChooser();
			    	fc2.setFileSelectionMode(JFileChooser.FILES_ONLY);
			    	fc2.setDialogTitle("Save XML file as...");
			    	FileNameExtensionFilter filter = new FileNameExtensionFilter("XML FILES", "xml", "text");
			    	fc2.setFileFilter(filter);
			    	int retrivial = fc2.showSaveDialog(pathsFrame);
			    	if (retrivial == JFileChooser.APPROVE_OPTION){
			    		File destination = fc2.getSelectedFile();
				    	if (!destination.getName().contains(".")){
				    		String fileWithExtension = destination.getPath()+".xml";
				    		destination = new File(fileWithExtension);
				    	}
				    	ArrayList<Heuristic> templatesToAdd = new ArrayList<Heuristic>();
				    	for (int k = 0; k<model.getDataVector().size(); k++){
							boolean use = (Boolean) model.getValueAt(k, 2);
							if (use){
								Vector<String> text = (Vector<String>) model.getValueAt(k,0);
								Heuristic heuristic = new Heuristic();
								ArrayList<Condition> conds = new ArrayList<Condition>();
								for (String r:text){
									Condition condition = new Condition();
									condition.setConditionString(r);
									conds.add(condition);
								}
								heuristic.setConditions(conds);
								heuristic.setType((String) model.getValueAt(k, 5));
								heuristic.setSentence_type((String) model.getValueAt(k, 1));
								heuristic.setText((String) model.getValueAt(k, 3));
								heuristic.setSentence_class((String) model.getValueAt(k, 6));
								templatesToAdd.add(heuristic);
							}
				    	}
						try{
							XMLWriter.addXMLHeuristics(destination, templatesToAdd);
							pathsFrame.setVisible(false);
						}catch(Exception ex){
							JOptionPane.showMessageDialog(pathsPanel, "Unable to write XML file", "Error", JOptionPane.ERROR_MESSAGE);
							ex.printStackTrace();
						}
								
			    	}	
						
				}		
						
	}
}
	
		

