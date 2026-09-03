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
import java.util.HashMap;
import java.util.List;
import java.util.Scanner;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.layout.AnchorPane;
import javafx.stage.DirectoryChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class FilteringController {
	
	@FXML
	Stage stage;
	@FXML
	Parent root;
	@FXML
	Scene scene;
	@FXML
	TextField directoryTextField;
	@FXML
	TextField typeField;
	@FXML
	TextArea consoleTextArea;
	@FXML
	Button exitButton;
	@FXML
	AnchorPane scenePane;
	
	@FXML
	CheckBox keyUpChck;
	@FXML
	CheckBox keyDownChck;
	@FXML
	TextField keyUpText;
	@FXML
	TextField keyDownText;
	
	@FXML
	CheckBox descParaChck;
	@FXML
	CheckBox nameParaChck;
	@FXML
	CheckBox mainFileChck;
	@FXML
	CheckBox descFileChck;
	
	@FXML
	private void initialize() {
		keyUpText.disableProperty().bind(keyUpChck.selectedProperty().not());
		keyDownText.disableProperty().bind(keyDownChck.selectedProperty().not());
	}
	
	private String line;
	private String dirPath;
	private File defaultDir;
	private String[] parametry;
	private String[] tempOrder;
	private File dir;
	private ArrayList<Integer> orderArray;
	private Path newPathTemp;
	private File descFile;
	private FileWriter descFileWriter;
	private HashMap<String, Integer> descCounter;
	private String[] firstLine;
	
	private SceneController mainController;
	
	public void setMainController(SceneController mainController) {
        this.mainController = mainController;
    }
	
	public void chooseDir() {
		
		DirectoryChooser directoryChooser = new DirectoryChooser();
		
		directoryChooser.setTitle("Choose a directory you want to filter data from");
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
			if(typeField.getText() != "") {
				if(keyUpChck.isSelected() || keyDownChck.isSelected()) {
					Path rootPath = Paths.get(dir.getAbsolutePath()); 
						
				    List<Path> allFiles = new ArrayList<>();
				    
				    try {
				    	listAllFiles(rootPath, allFiles); 
				    
					    for(Path p : allFiles) {
					    	Scanner sc = new Scanner(new File(p.toString()));
							sc.useDelimiter("\\R");
		
							while(sc.hasNext()) {
								line = sc.next();
								parametry = null;
								tempOrder = null;
								orderArray = new ArrayList<Integer>();
								parametry = line.split(typeField.getText());
								
									for(Integer m = 0; m < parametry.length; m++) {
										if((keyUpChck.isSelected() && parametry[m].equals(keyUpText.getText())) || (keyDownChck.isSelected() && parametry[m].equals(keyDownText.getText()))) {
											tempOrder = popUpWindow(line, parametry);
											if(tempOrder != null) {
												for(String s: tempOrder) {
													if(s == null) {
														sc.close();
														logToConsole("Parameters aren't arranged \n");
														return;
													}
												}
												for(Integer i = 0; i < tempOrder.length; i++) {
													for(Integer j = 0; j < parametry.length; j++) {
														if(tempOrder[i].equals(parametry[j])) {
															orderArray.add(j);
															break;
														}
													}
												}
												sc.close();
												firstLine = parametry;
												submit(orderArray);
												return;
											} else {
												sc.close();
												logToConsole("Parameters aren't arranged \n");
												return;
											}
										}
									}
								}
								sc.close();
					        }
					        logToConsole("Couldn't find data with provided parameters \n");
					    } catch (Exception e) {
					    	logToConsole("There was a problem with chosen directory \n");
					    }
					} else {
						logToConsole("None of the key options are selected \n");
					}
			} else {
				logToConsole("Separator type isn't provided \n");
			}
		} else {
			logToConsole("Directory isn't selected \n");
		}
	}
	
	private String[] popUpWindow(String line, String[] parametry) throws IOException {
		Stage popupStage = new Stage();
        popupStage.initModality(Modality.APPLICATION_MODAL);
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/resources/fxml/arrangingScene.fxml"));
        Parent root = loader.load();
		Scene scene = new Scene(root);
		popupStage.getIcons().add(new Image(getClass().getResource("/resources/icon/icon.png").toExternalForm()));
		popupStage.setTitle("Adrian Budny Praca Magisterska");
		String css = this.getClass().getResource("/resources/css/application.css").toExternalForm();
		scene.getStylesheets().add(css);
        popupStage.setScene(scene);
        
        ArrangingController controller = loader.getController();
        controller.setChoice(line, parametry);
		
        popupStage.showAndWait();
        
        return controller.getNewParametry();
	}
	
	private void submit(ArrayList<Integer> orderArray) throws IOException {
		
		if(orderArray != null && !orderArray.isEmpty()) {
			logToConsole("Filtering data has started \n");
			
			newPathTemp = null;
			descFile = null;
			
			Path rootPath = Paths.get(dir.getAbsolutePath()); 
			
			ArrayList<String> uniqueId = new ArrayList<String>();
			
	        List<Path> allFiles = new ArrayList<>();
	        
	        try {
		        listAllFiles(rootPath, allFiles);
		        
				Path mainDescPath = Paths.get(rootPath.getParent().toString(), rootPath.getFileName().toString());
				Path newFolderPath = Paths.get(mainDescPath.toString() + "_flt");
				
				Files.createDirectories(newFolderPath);
		  
		        for(Path p : allFiles) {
		        	URI path1 = p.getParent().toUri();
		        	URI path2 = rootPath.toUri();
		        	
		        	URI rel = path2.relativize(path1);
		        	
		        	String userName = p.getFileName().toString().substring(0, 3);
		        	
		        	String path = rel.getPath();
		        	
		        	Path newPath = Paths.get(newFolderPath.toString(), path);
			        Files.createDirectories(newPath);
			        
			        if(descFileChck.isSelected()) {
			        	if(newPathTemp == null || newPath.compareTo(newPathTemp) != 0)  {
				        	if(newPathTemp != null) {
				        		descFileWriter = new FileWriter(descFile);
				        		descFileWriter.write("All filtered lines: " + descCounter.get("all") + System.getProperty("line.separator")  + "Filtered key presses: " + descCounter.get("keyDown") + System.getProperty("line.separator") + "Filtered key releases: " + descCounter.get("keyUp") + System.getProperty("line.separator") + "Filtered lines without keys: " + descCounter.get("none"));
				        		descFileWriter.close();
				        	}
				        	descFile = new File(newPath.toString(), "._desc.txt");
				        }
			        }
			        
			        if(newPathTemp == null || newPath.compareTo(newPathTemp) != 0)  {
			        	descCounter = new HashMap<String, Integer>();
			        	descCounter.put("all", 0);
			        	descCounter.put("keyDown", 0);
			        	descCounter.put("keyUp", 0);
			        	descCounter.put("none", 0);
			        	newPathTemp = newPath;
			        }
			        
		        	Scanner sc = new Scanner(new File(p.toString()));
					sc.useDelimiter("\\R");
					
					File outputFile = new File(newPath.toString(), p.getFileName().toString());
					FileWriter fileWriter = new FileWriter(outputFile);
					
					while(sc.hasNext()) {
						descCounter.put("all", descCounter.get("all")+1);
						line = sc.next();
						String[] parametry = line.split(typeField.getText());
						
						if(parametry.length != 0) {
							Integer flagLine = 0;
							
							for(Integer m = 0; m < parametry.length; m++) {
								if((keyUpChck.isSelected() && parametry[m].equals(keyUpText.getText())) || (keyDownChck.isSelected() && parametry[m].equals(keyDownText.getText()))) {
									
									if(keyUpChck.isSelected() && parametry[m].equals(keyUpText.getText())) {
										descCounter.put("keyUp", descCounter.get("keyUp")+1);
									} else if(keyDownChck.isSelected() && parametry[m].equals(keyDownText.getText())) {
										descCounter.put("keyDown", descCounter.get("keyDown")+1);
									}
											
									flagLine = 1;
									
									for(Integer i = 0; i < orderArray.size(); i++) {
										if(orderArray.get(i) >= parametry.length) {
											fileWriter.write("null");
										} else {
											if(keyDownChck.isSelected() && parametry[orderArray.get(i)].equals(keyDownText.getText())) {
												fileWriter.write("K");
											} else if(keyUpChck.isSelected() && parametry[orderArray.get(i)].equals(keyUpText.getText())) {
												fileWriter.write("k");
											} else {
												fileWriter.write(parametry[orderArray.get(i)]);
											}
										}
										
										if(i+1 != orderArray.size()) {
											fileWriter.write(",");
										}
										
									}
									
									if(!(orderArray.get(2) >= parametry.length)) {
										if(!uniqueId.contains(parametry[orderArray.get(2)])) {
											uniqueId.add(parametry[orderArray.get(2)]);
										}
									}
									
									if(descParaChck.isSelected()) {
										fileWriter.write(",");
										for(Integer j = 0; j < parametry.length; j++) {
											Integer flagDesc = 0;
											for(Integer d = 0; d < orderArray.size(); d++) {
												if(j == orderArray.get(d)) {
													flagDesc = 1;
													break;
												}
											}
											if(flagDesc == 0) {
												fileWriter.write(parametry[j]);
											}
										}
									}
									
									if(nameParaChck.isSelected()) {
										fileWriter.write(",");
										fileWriter.write(userName);
									}
									
									fileWriter.write(System.getProperty("line.separator"));
									break;
								}
							}
							if(flagLine == 0) {
								descCounter.put("none", descCounter.get("none")+1);
							}
						}
					}
					sc.close();
					fileWriter.close();
					
					if(outputFile.exists() && outputFile.length() == 0) {
						outputFile.delete();
					}
		        }
		        
		        if(descFileChck.isSelected()) {
		        	descFileWriter = new FileWriter(descFile);
		        	descFileWriter.write("All filtered lines: " + descCounter.get("all") + System.getProperty("line.separator")  + "Filtered key presses: " + descCounter.get("keyDown") + System.getProperty("line.separator") + "Filtered key releases: " + descCounter.get("keyUp") + System.getProperty("line.separator") + "Filtered lines without keys: " + descCounter.get("none"));
		    		descFileWriter.close();
		        }
				
				if(mainFileChck.isSelected()) {
					File mainDesc = new File(newFolderPath.toString(), "._mainDesc.txt");
		    		
		    		FileWriter mainDescFW = new FileWriter(mainDesc);
		    		
		    		for(Integer j = 0; j < orderArray.size(); j++) {
		    			if(orderArray.get(j) != j) {
		    				mainDescFW.write("Moved parameter '" + firstLine[orderArray.get(j)] + "' to position number " + (j+1));
		    				mainDescFW.write(System.getProperty("line.separator"));
		    			}
		        	}
		    		
		    		if(descParaChck.isSelected()) {
		    			mainDescFW.write("Rest of unused parameters are saved in one continuous string in postion number 4"); 
		    			mainDescFW.write(System.getProperty("line.separator"));
		    		}
		    		
		    		if(nameParaChck.isSelected()) {
		    			mainDescFW.write("Name of the directory which a file is located inside is saved in the last position"); 
		    			mainDescFW.write(System.getProperty("line.separator"));
		    		}
		    		
		    		if(!keyDownText.getText().equals("K") && keyDownChck.isSelected() && descCounter.get("keyDown") > 0) {
	        			mainDescFW.write("Changed parameter '" + keyDownText.getText() + "' to parameter 'K'");
	        			mainDescFW.write(System.getProperty("line.separator"));
	        		}
	        		
	        		if(!keyUpText.getText().equals("k") && keyUpChck.isSelected() && descCounter.get("keyUp") > 0) {
	        			mainDescFW.write("Changed parameter '" + keyUpText.getText() + "' to parameter 'k'");
	        			mainDescFW.write(System.getProperty("line.separator"));
	        		}
		    		
		    		for(String s: uniqueId) {
		    			mainDescFW.write(s);
		    			mainDescFW.write(System.getProperty("line.separator"));
		    		}
		    		mainDescFW.close();
				}
				
				logToConsole("Filtering data has ended \n");
			} catch (Exception e) {
				logToConsole("There was a problem with chosen directory \n");
			}
		} else {
			logToConsole("Couldn't find data with provided parameters \n");
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
