package application;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URI;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Scanner;
import java.util.function.UnaryOperator;

import javafx.fxml.FXML;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextField;
import javafx.scene.control.TextFormatter;
import javafx.scene.control.TextFormatter.Change;
import javafx.scene.layout.AnchorPane;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;
import javafx.util.converter.IntegerStringConverter;

public class ProcessingController {
	
	@FXML
	Stage stage;
	@FXML
	Parent root;
	@FXML
	Scene scene;
	@FXML
	TextField directoryTextField;
	@FXML
	Button exitButton;
	@FXML
	AnchorPane scenePane;
	
	@FXML
	CheckBox pTimeChckbx;
	@FXML
	CheckBox fTimeChckbx;
	@FXML
	CheckBox ptpTimeChckbx;
	
	@FXML
	TextField timeOutField;
	@FXML
	TextField actOutField;
	@FXML
	TextField timeBrField;
	
	@FXML
	RadioButton radButtAdd;
	@FXML
	RadioButton radButtRem;
	
	@FXML
	CheckBox outChckbx;
	@FXML
	CheckBox brChckbx;
	
	@FXML
	Label radButtAddLbl;
	@FXML
	Label radButtRemLbl;
	
	
	@FXML
	private void initialize() {
		timeOutField.disableProperty().bind(outChckbx.selectedProperty().not());
		actOutField.disableProperty().bind(outChckbx.selectedProperty().not());
		
		timeBrField.disableProperty().bind(brChckbx.selectedProperty().not());
		radButtAdd.disableProperty().bind(brChckbx.selectedProperty().not());
		radButtRem.disableProperty().bind(brChckbx.selectedProperty().not());
		radButtAddLbl.disableProperty().bind(brChckbx.selectedProperty().not());
		radButtRemLbl.disableProperty().bind(brChckbx.selectedProperty().not());
		
		UnaryOperator<Change> integerFilter = change -> {
		    String newText = change.getControlNewText();
		    if (newText.matches("^(?:[1-9][0-9]*|)$")) { 
		        return change;
		    }
		    return null;
		};
		
		timeOutField.setTextFormatter(new TextFormatter<Integer>(new IntegerStringConverter(), null, integerFilter));
		actOutField.setTextFormatter(new TextFormatter<Integer>(new IntegerStringConverter(), null, integerFilter));
		timeBrField.setTextFormatter(new TextFormatter<Integer>(new IntegerStringConverter(), null, integerFilter));
	}
	
	
	private String dirPath;
	private File defaultDir;
	private File dir;
	private ArrayList<String[]> listP = new ArrayList<>();
	private ArrayList<String[]> listR = new ArrayList<>();
	private String line;
	private String[] prevPara;
	private Long prevPT;
	private Queue<String> writeQueue = new LinkedList<>();
	private Integer actionCounter = 0;
	private Long maxP;
	private Long maxR;
	
	private SceneController mainController;
	
	public void setMainController(SceneController mainController) {
        this.mainController = mainController;
    }
	
	public void chooseDir() {
		
		DirectoryChooser directoryChooser = new DirectoryChooser();
		
		directoryChooser.setTitle("Choose a directory you want to process data from");
		defaultDir = new File(System.getProperty("user.home"));
		directoryChooser.setInitialDirectory(defaultDir);
		
		dir = directoryChooser.showDialog(stage);
		
		dirPath = "";
		if(dir != null) {
			dirPath = dir.getAbsolutePath();
		}
		directoryTextField.setText(dirPath);
		
	}
	
	public void assign() throws IOException {
		if(dir != null) {
			if(pTimeChckbx.isSelected() || fTimeChckbx.isSelected() || ptpTimeChckbx.isSelected()) {
				if(((brChckbx.isSelected() && !timeBrField.getText().equals("")) || !brChckbx.isSelected()) && ((outChckbx.isSelected() && !timeOutField.getText().equals("") && !actOutField.getText().equals("")) || !outChckbx.isSelected())) {
				    logToConsole("Processing data has started \n");
					
					Path rootPath = Paths.get(dir.getAbsolutePath());
					
					List<Path> allFiles = new ArrayList<>();
					
					try {
						listAllFiles(rootPath, allFiles);
				    
						Path mainDescPath = Paths.get(rootPath.getParent().toString(), rootPath.getFileName().toString());
						Path newFolderPath = Paths.get(mainDescPath.toString() + "_pro");
						
						Files.createDirectories(newFolderPath);
						
						 for(Path p : allFiles) {
					        	URI path1 = p.getParent().toUri();
					        	URI path2 = rootPath.toUri();
					        	
					        	URI rel = path2.relativize(path1);
					        	
					        	String path = rel.getPath();
					        	
					        	Path newPath = Paths.get(newFolderPath.toString(), path);
					        	Files.createDirectories(newPath);
						        
						        if(p.getFileName().toString().charAt(0) == '.' && p.getFileName().toString().charAt(1) == '_') {
						        	Path filePath = Paths.get(newPath.toString(), p.getFileName().toString());
						        	Files.copy(p, filePath);
						        	continue;
						        }
						          
						        Scanner sc = new Scanner(new File(p.toString()));
								sc.useDelimiter("\\R");
								
								File outputFile = new File(newPath.toString(), p.getFileName().toString());
								FileWriter fileWriter = new FileWriter(outputFile);
								
								listP.clear();
								listR.clear();
								line = "";
								prevPara = null;
								prevPT = null;
								actionCounter = 0;
								writeQueue.clear();
								
								while(sc.hasNext()) {
									line = sc.next();
									String[] parametry = line.split(",");
									maxP = 0L;
									maxR = 0L;
									
									if(parametry[0].equals("K")) {
										listP.add(parametry);
									} else {
										for(Integer i = 0; i < listR.size(); i++) {
											if(parametry[2].equals(listR.get(i)[2])) {
												if(maxR < Long.parseLong(listR.get(i)[1])) {
													maxR = Long.parseLong(listR.get(i)[1]);
												}
											}
										}
										
										for(Integer i = 0; i < listP.size(); i++) {
											if(parametry[2].equals(listP.get(i)[2])) {
												if(maxP < Long.parseLong(listP.get(i)[1])) {
													maxP = Long.parseLong(listP.get(i)[1]);
												}
											}
										}
										
										if(maxP > maxR) {
											if(parametry[2].equals(listP.get(0)[2])) {
												listR.add(0, parametry);
											} else {
												listR.add(parametry);
											}
										} else {
											continue;
										}
											
										
										for(Integer i = 0; i < listR.size(); i++) {
											if(listR.get(i)[2].equals(listP.get(0)[2])) {
												Long pressTS = Long.parseLong(listP.get(0)[1]);
												Long releaseTS = Long.parseLong(listR.get(i)[1]);
												Long pressTime = releaseTS - pressTS;
												
												if(prevPara != null && prevPT != null) {
													Long flightTime = pressTS - Long.parseLong(prevPara[1]);
													Long pressPressTime = prevPT + flightTime;
													if(!(brChckbx.isSelected() && radButtRem.isSelected() && pressPressTime >= Long.parseLong(timeBrField.getText())*1000)) {
														if(fTimeChckbx.isSelected()) {
															writeQueue.offer(flightTime + ",FT," + prevPara[2]);
														}
														
														if(ptpTimeChckbx.isSelected()) {
															writeQueue.offer(pressPressTime + ",DD," + prevPara[2]);
														}
													}
													
													if(brChckbx.isSelected() && radButtAdd.isSelected() && pressPressTime >= Long.parseLong(timeBrField.getText())*1000) {
														writeQueue.offer(flightTime + ",BR,LongWorkBreak");
													}
													
													if(outChckbx.isSelected() && pressPressTime >= Long.parseLong(timeOutField.getText())) {
														if(actionCounter >= Integer.parseInt(actOutField.getText())) {
															while(!writeQueue.isEmpty()) {
																fileWriter.write(writeQueue.poll());
																fileWriter.write(System.getProperty("line.separator"));
															}
															actionCounter = 0;
														} else {
															writeQueue.clear();
															actionCounter = 0;
														}
													}
												}
												
												if(pTimeChckbx.isSelected()) {
													writeQueue.offer(pressTime + ",DT," + listR.get(i)[2]);
												}
												
												actionCounter++;
												
												if(!outChckbx.isSelected()) {
													while(!writeQueue.isEmpty()) {
														fileWriter.write(writeQueue.poll());
														fileWriter.write(System.getProperty("line.separator"));
													}
												}
												
												prevPara = listR.get(i);
												prevPT = pressTime;
												
												for(Integer j = 0; j < listP.size(); j++) {
													if(releaseTS >= Long.parseLong(listP.get(j)[1])) {
														if(listP.get(j)[2].equals(listR.get(i)[2])) {
															listP.remove(listP.get(j));
															j--;
														}
													} else {
														break;
													}
												}										
												
												listR.remove(listR.get(i));
												i = -1;
											}
										}
									}
								}
								while(!listR.isEmpty()) {
									String[] tempP = listP.get(0);
									listP.remove(0);
									listP.add(tempP);
									for(Integer i = 0; i < listR.size(); i++) {
										if(listR.get(i)[2].equals(listP.get(0)[2])) {
											Long pressTS = Long.parseLong(listP.get(0)[1]);
											Long releaseTS = Long.parseLong(listR.get(i)[1]);
											Long pressTime = releaseTS - pressTS;
											
											if(prevPara != null && prevPT != null) {
												Long flightTime = pressTS - Long.parseLong(prevPara[1]);
												Long pressPressTime = prevPT + flightTime;
												
												if(!(brChckbx.isSelected() && radButtRem.isSelected() && pressPressTime >= Long.parseLong(timeBrField.getText())*1000)) {
													if(fTimeChckbx.isSelected()) {
														writeQueue.offer(flightTime + ",FT," + prevPara[2]);
													}
													
													if(ptpTimeChckbx.isSelected()) {
														writeQueue.offer(pressPressTime + ",DD," + prevPara[2]);
													}
												}
												
												if(brChckbx.isSelected() && radButtAdd.isSelected() && pressPressTime >= Long.parseLong(timeBrField.getText())*1000) {
													writeQueue.offer(flightTime + ",BR,LongWorkBreak");
												}
												
												if(outChckbx.isSelected() && pressPressTime >= Long.parseLong(timeOutField.getText())) {
													if(actionCounter >= Integer.parseInt(actOutField.getText())) {
														while(!writeQueue.isEmpty()) {
															fileWriter.write(writeQueue.poll());
															fileWriter.write(System.getProperty("line.separator"));
														}
														actionCounter = 0;
													} else {
														writeQueue.clear();
														actionCounter = 0;
													}
												}
											}
											
											if(pTimeChckbx.isSelected()) {
												writeQueue.offer(pressTime + ",DT," + listR.get(i)[2]);
											}
											
											actionCounter++;
											
											if(!outChckbx.isSelected()) {
												while(!writeQueue.isEmpty()) {
													fileWriter.write(writeQueue.poll());
													fileWriter.write(System.getProperty("line.separator"));
												}
											}
											
											prevPara = listR.get(i);
											prevPT = pressTime;
											
											for(Integer j = 0; j < listP.size(); j++) {
												if(releaseTS >= Long.parseLong(listP.get(j)[1])) {
													if(listP.get(j)[2].equals(listR.get(i)[2])) {
														listP.remove(listP.get(j));
														j--;
													}
												} else {
													break;
												}
											}										
											
											listR.remove(listR.get(i));
											i = -1;
										}
									}
								}
								
								if((outChckbx.isSelected() && actionCounter >= Integer.parseInt(actOutField.getText())) || !outChckbx.isSelected()) {
									while(!writeQueue.isEmpty()) {
										fileWriter.write(writeQueue.poll());
										fileWriter.write(System.getProperty("line.separator"));
									}
								} else {
									writeQueue.clear();
									actionCounter = 0;
								}
								sc.close();
								fileWriter.close();
								
								if(outputFile.exists() && outputFile.length() == 0) {
									outputFile.delete();
								}
						 }
						 logToConsole("Processing data has ended \n");
					} catch (Exception e) {
						logToConsole("There was a problem with chosen directory \n");
					}
				} else {
					logToConsole("Additional parameters had been selected but not provided \n");
				}
			} else {
				logToConsole("None of the options are selected \n");
			}
		} else {
			logToConsole("Directory isn't selected \n");
		}
	}
	
	private static void listAllFiles(Path currentPath, List<Path> allFiles) throws IOException  { 
		try (DirectoryStream<Path> stream = Files.newDirectoryStream(currentPath))  { 
			for (Path entry : stream) { 
				if (Files.isDirectory(entry)) { 
					listAllFiles(entry, allFiles); 
				} else { 
					allFiles.add(entry); 
		        } 
			} 
		} 
	} 
	
	private void logToConsole(String message) {
        if (mainController != null) {
            mainController.addToConsole(message);
        }
    }
}
