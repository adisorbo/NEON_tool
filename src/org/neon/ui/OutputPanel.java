package org.neon.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.Window;

import java.awt.Label;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;


import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.border.Border;
import javax.swing.border.TitledBorder;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.text.DefaultHighlighter;
import javax.swing.text.DefaultHighlighter.DefaultHighlightPainter;
import javax.swing.text.Highlighter;
import javax.swing.text.Highlighter.Highlight;

import org.neon.engine.Parser;
import org.neon.engine.XMLReader;
import org.neon.engine.XMLWriter;
import org.neon.model.Result;
import org.neon.pathsFinder.ui.AddPathsPanel;

public class OutputPanel {
	
	private JFrame frame;
	private JTextArea textArea = new JTextArea();
	private JScrollPane pane;
	private JPanel legendaGroup;
	private JScrollPane legendaScroll;
	private ArrayList<Result> results;
	private HashMap<String, Color> colours;
	
	public OutputPanel(){
		try{
			this.main();
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	
	
	protected void main() throws MalformedURLException {
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
		frame = new JFrame("Nlp-based softwarE dOcumentation aNalyzer");
		frame.setLayout(new BorderLayout());
		
		// MenuBar
		JMenu fileMenu = new JMenu("File");
		JMenuBar menuBar = new JMenuBar();
		
		// create file menu items
		JMenuItem newItem = new JMenuItem("New...");
		JMenuItem item = new JMenuItem("Open text file...");
		JMenuItem exportItem = new JMenuItem("Export results...");
		JMenuItem addPaths = new JMenuItem("Add new heuristics...");
		JMenuItem exitItem = new JMenuItem("Exit");
		
		// create help menu items
		JMenu helpMenu = new JMenu("Help");
		JMenuItem item2 = new JMenuItem("About...");
		
		item2.addActionListener(new AboutListener());
		item.addActionListener(new OpenListener());
		addPaths.addActionListener(new ActionListener(){

			@Override
			public void actionPerformed(ActionEvent arg0) {
				AddPathsPanel panel = new AddPathsPanel();
				
			}
			
		});
		
		exportItem.addActionListener(new ExportListener());
		
		newItem.addActionListener(new NewListener());
		exitItem.addActionListener(new ActionListener(){

			@Override
			public void actionPerformed(ActionEvent arg0) {
				System.exit(0);
				
			}
			
		});
		
		// Populating scroll menu
		helpMenu.add(item2);
		fileMenu.add(item);
		fileMenu.add(newItem);
		fileMenu.add(addPaths);
		fileMenu.add(exportItem);
		fileMenu.add(exitItem);
		menuBar.setBackground(Color.orange);
		menuBar.add(fileMenu);
		menuBar.add(helpMenu);
		
		
		// Adding scroll menu to the main frame
		frame.add(menuBar, BorderLayout.NORTH);
		
		// Adding the textArea to the main frame
		
		TitledBorder border = new TitledBorder("Insert the message here");
		border.setTitleJustification(TitledBorder.CENTER);
		pane = new JScrollPane(textArea);
		pane.setBorder(border);
		Border lineBorder = BorderFactory.createLineBorder(Color.BLACK);
		textArea.setLineWrap(true);
		textArea.setWrapStyleWord(true);
		textArea.setBorder(lineBorder);
		textArea.setSelectionColor(Color.BLACK);
		textArea.setSelectedTextColor(Color.WHITE);
	
		// Constructing legenda panel
		legendaGroup = new JPanel();
		legendaGroup.setLayout(new BoxLayout(legendaGroup, BoxLayout.Y_AXIS));
		legendaGroup.setPreferredSize(new Dimension(200,300));
		
		TitledBorder legendaBorder = new TitledBorder("Classes");
		legendaBorder.setTitleJustification(TitledBorder.CENTER);
		legendaGroup.setBorder(legendaBorder);
		legendaScroll = new JScrollPane(legendaGroup, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		
		
		
		frame.add(pane, BorderLayout.CENTER);
		frame.add(legendaScroll, BorderLayout.AFTER_LINE_ENDS);
		frame.setSize(800, 500);
		frame.setLocationRelativeTo(null);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setVisible(true);
		JButton button = new JButton("Classify");
		button.addActionListener(new ClassifyListener());

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
					e1.printStackTrace();
				}
				try{
					String line = br.readLine();
					textArea.setText("");
					while (line!=null){
						textArea.append(line+"\n");
						line = br.readLine();
						
					}
				}catch(IOException e2){
					e2.printStackTrace();
				}
			}
			
		}
	}
	
	private class NewListener implements ActionListener{
		public void actionPerformed(ActionEvent ae){
			textArea.setText("");
			legendaGroup.removeAll();
			legendaGroup.setPreferredSize(new Dimension(200,300));
			legendaGroup.revalidate();
			legendaGroup.repaint();
			legendaScroll.revalidate();
			legendaScroll.repaint();
			results = null;
			colours = null;
		}
	}
	
	private class ExportListener implements ActionListener{
		@Override
		public void actionPerformed(ActionEvent arg0) {
			Highlighter highlighter = textArea.getHighlighter();
			Highlight[] hls = highlighter.getHighlights();
			ArrayList<Result> sentencesToExport = new ArrayList<Result>();
			
			
			
			if (results != null && !results.isEmpty()) {
			
			
			for (Highlight hl : hls) {
				DefaultHighlighter.DefaultHighlightPainter painter = (DefaultHighlighter.DefaultHighlightPainter) hl.getPainter();
				Color color = painter.getColor();
				String category = "";
				
				
				
				for (String s: colours.keySet()) {
					if (colours.get(s) == color) {
						category = s;
						break;
					}
				}
				
				String highlightedSentence = textArea.getText().substring(hl.getStartOffset(), hl.getEndOffset());
				
				Result currentResult = new Result();
				currentResult.setSentence(highlightedSentence);
				currentResult.setSentenceClass(category);
				
				
				
				
				boolean found = false;
				
				for (Result res: sentencesToExport) {
					if (res.getSentence().equalsIgnoreCase(currentResult.getSentence()) &&
						 res.getSentenceClass().equalsIgnoreCase(currentResult.getSentenceClass())) {
						found = true;
						break;
					}
				}
				
				if (!found) {
					// The following code is required if we want 
					// to also report the identified templates in the exported results file
					
//					ArrayList<String> templates = new ArrayList<String>();
//					for (Result r: results) {
//						if (r.getSentence().equalsIgnoreCase(currentResult.getSentence()) &&
//								 r.getSentenceClass().equalsIgnoreCase(currentResult.getSentenceClass())) {
//							templates.add(r.getHeuristic());
//						}
//					}
//					currentResult.setHeuristic(templates.toString());
					
					
					sentencesToExport.add(currentResult);
				}
			}
			
			JFileChooser fc2 = new JFileChooser();
	    	fc2.setFileSelectionMode(JFileChooser.FILES_ONLY);
	    	fc2.setDialogTitle("Save XML file as...");
	    	FileNameExtensionFilter filter = new FileNameExtensionFilter("XML FILES", "xml", "text");
	    	fc2.setFileFilter(filter);
	    	int retrivial = fc2.showSaveDialog(frame);
	    	if (retrivial == JFileChooser.APPROVE_OPTION){
	    		File destination = fc2.getSelectedFile();
		    	if (!destination.getName().contains(".")){
		    		String fileWithExtension = destination.getPath()+".xml";
		    		destination = new File(fileWithExtension);
		    	}
		    	try {
		    		XMLWriter.addXMLSentences(destination, sentencesToExport);
		    	}catch(Exception ex){
					JOptionPane.showMessageDialog(frame, "Unable to write XML file", "Error", JOptionPane.ERROR_MESSAGE);
					ex.printStackTrace();
				}
			
	    	}
			}else {
				JOptionPane.showMessageDialog(frame, "No classified sentences to export", "Warning", JOptionPane.WARNING_MESSAGE);
			}
	
		}	
	}
	
	
	private class AboutListener implements ActionListener{
		public void actionPerformed(ActionEvent ae){
			String message = "Nlp-based softwarE dOcumentation aNalyzer\n" +
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
	
	
	
	private class BarThread extends Thread {
		  
		  public BarThread() {

		  }

		  public void run() {
			Parser parser = Parser.getInstance();  
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
			
			
						
		    while (progressBar.getValue()<100) {
		       float percentage = parser.getScannedSentences() * 100.0f;
	              progressBar.setValue((int) percentage);
	              frameProgressBar.update(frameProgressBar.getGraphics());
	              
	            try {
					Thread.sleep(100);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
            }
			progressBar.setStringPainted(true);
			frameProgressBar.dispose();
			parser.refreshScannedSentences();
		  }
		  
	}
	
	
	
	
	private class ClassifyListener implements ActionListener{
		
		
		@Override
		public void actionPerformed(ActionEvent ae) {
			    Highlighter hilite = textArea.getHighlighter();
			    hilite.removeAllHighlights();

				File strategy;
				JFileChooser fc = new JFileChooser();
				
		        FileNameExtensionFilter filter = new FileNameExtensionFilter("XML FILES", "xml", "text");
				
				fc.setFileSelectionMode(JFileChooser.FILES_ONLY);
				fc.setFileFilter(filter);
				fc.setSelectedFile(fc.getCurrentDirectory());
				fc.setDialogTitle("Choose the heuristics file to use...");
			
				int retVal = fc.showOpenDialog(frame);
				if (retVal == fc.APPROVE_OPTION){
					strategy = fc.getSelectedFile();
					
					ArrayList<String> categories = XMLReader.getSentenceClasses(strategy);
					colours = new HashMap<String, Color>();
					Random random = new Random();
					
					// Creating legenda 
					for (String c : categories){
						Color color;
						
						
						
						do{
						    color = new Color(255,255,255);
							int red = random.nextInt(256);
						    int green = random.nextInt(256);
						    int blue = random.nextInt(256);
						    red = (red + color.getRed()) / 2;
						    green = (green + color.getGreen()) / 2;
						    blue = (blue + color.getBlue()) / 2;
						    color = new Color(red, green, blue);						    
						    
						}while(colours.values().contains(color));
						colours.put(c, color);
					}
					
					// Updating the legenda panel
					
					
					legendaGroup.removeAll();
					
					
					int index = 0;
					for (String cl: colours.keySet()){
						
						if (!cl.isEmpty()){
							index++;
							TitledBorder legBorder = new TitledBorder(cl);
							legBorder.setTitleJustification(TitledBorder.CENTER);
							JPanel legendaElement = new JPanel();
							legendaElement.setBorder(legBorder);
							legendaElement.setPreferredSize(new Dimension(180,50));
							legendaElement.setMaximumSize(new Dimension(180,50));
							legendaElement.setMinimumSize(new Dimension(180,50));
							
							legendaElement.setLayout(new BoxLayout(legendaElement, BoxLayout.Y_AXIS));
							Label l = new Label();
							l.setBackground(colours.get(cl));
							l.addMouseListener(new LabelListener());
							legendaElement.add(l);
							legendaGroup.add(legendaElement);
							
						}
					}
					index++;
					if (colours != null && !colours.isEmpty()) {
						TitledBorder legBorder = new TitledBorder("ALL CATEGORIES");
						legBorder.setTitleJustification(TitledBorder.CENTER);
						JPanel legendaElement = new JPanel();
						legendaElement.setBorder(legBorder);
						legendaElement.setPreferredSize(new Dimension(180,50));
						legendaElement.setMaximumSize(new Dimension(180,50));
						legendaElement.setMinimumSize(new Dimension(180,50));
						
						legendaElement.setLayout(new BoxLayout(legendaElement, BoxLayout.Y_AXIS));
						Label l = new Label();
						l.setBackground(Color.WHITE);
						l.addMouseListener(new LabelListener());
						legendaElement.add(l);
						legendaGroup.add(legendaElement);
						index++;
					}
							
					
					
					
					legendaGroup.setPreferredSize(new Dimension(200,(50*index)));
					legendaGroup.revalidate();
					legendaGroup.repaint();
					legendaScroll.revalidate();
					legendaScroll.repaint();
					
					frame.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
					String textToClassify = textArea.getText();
					Parser ue = Parser.getInstance();
										
			        
			        BarThread stepper = new BarThread();
					stepper.start();
					
					
					results = ue.extract(textToClassify,strategy);
			        			
					for (Result r: results){
						String s = r.getSentence();
						int pos = 0;
						while ((pos = textArea.getText().indexOf(s,pos))>=0){	
							try{
								Color color = colours.get(r.getSentenceClass().toUpperCase());
								

								textArea.getHighlighter().addHighlight(pos, pos+s.length(), 
										new DefaultHighlighter.DefaultHighlightPainter(color));
								pos = pos+s.length();
							}catch(Exception ex){
								ex.printStackTrace();
							}
						}
	
					}
					frame.repaint();
					
//					JOptionPane.showMessageDialog(pane, "DONE!");
					frame.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
					
					

				}
		}
		
	}
	
	private class LabelListener implements MouseListener{

		@Override
		public void mouseClicked(MouseEvent me) {
			Label l = (Label) me.getSource();
			JPanel panel = (JPanel) l.getParent();
		    TitledBorder b = (TitledBorder) panel.getBorder();
		    Highlighter hilite = textArea.getHighlighter();
		    hilite.removeAllHighlights();
		    if (!b.getTitle().equalsIgnoreCase("ALL CATEGORIES")) {
			    for (Result r: results){
			    	if (r.getSentenceClass().equalsIgnoreCase(b.getTitle())){
						String s = r.getSentence();
						int pos = 0;
						while ((pos = textArea.getText().indexOf(s,pos))>=0){	
							try{
								
								Color color = colours.get(r.getSentenceClass().toUpperCase());
								

								textArea.getHighlighter().addHighlight(pos, pos+s.length(), 
										new DefaultHighlighter.DefaultHighlightPainter(color));
								pos = pos+s.length();
							}catch(Exception ex){
								ex.printStackTrace();
							}
						}

			    	}
			    }
		    }else {
		    	for (Result r: results) {
					String s = r.getSentence();
					int pos = 0;
					while ((pos = textArea.getText().indexOf(s,pos))>=0){	
						try{
							
							Color color = colours.get(r.getSentenceClass().toUpperCase());
							

							textArea.getHighlighter().addHighlight(pos, pos+s.length(), 
									new DefaultHighlighter.DefaultHighlightPainter(color));
							pos = pos+s.length();
						}catch(Exception ex){
							ex.printStackTrace();
						}
					}
		    		
		    	}
		    }
		    
		    
		}

		@Override
		public void mouseEntered(MouseEvent arg0) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void mouseExited(MouseEvent arg0) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void mousePressed(MouseEvent arg0) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void mouseReleased(MouseEvent arg0) {
			// TODO Auto-generated method stub
			
		}
		
	}
	
	
	
}
