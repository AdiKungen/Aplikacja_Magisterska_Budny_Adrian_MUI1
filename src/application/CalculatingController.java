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
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;
import java.util.function.UnaryOperator;
import java.util.HashMap;

import javafx.fxml.FXML;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextField;
import javafx.scene.control.TextFormatter;
import javafx.scene.control.TextFormatter.Change;
import javafx.scene.layout.AnchorPane;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;
import javafx.util.converter.IntegerStringConverter;

public class CalculatingController {
	
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
	RadioButton radButtAll;
	@FXML
	RadioButton radButtLR;
	
	@FXML
	TextField actAmoField;
	
	@FXML
	private void initialize() {
		UnaryOperator<Change> integerFilter = change -> {
		    String newText = change.getControlNewText();
		    if (newText.matches("^(?:[1-9][0-9]*|)$")) { 
		        return change;
		    }
		    return null;
		};
		
		actAmoField.setTextFormatter(new TextFormatter<Integer>(new IntegerStringConverter(), null, integerFilter));
	}
	
	
	private String dirPath;
	private File defaultDir;
	private File dir;
	private String line;
	private Map<String, ArrayList<Integer>> counters = new HashMap<>();

	private String[] keysArr = {
		    "A","B","C","D","E","F","G","H","I","J","K","L","M","N","O","P","Q","R","S","T","U","V","W","X","Y","Z",
		    "D1","D2","D3","D4","D5","D6","D7","D8","D9","D0"
	};

	private Set<String> keysArrLL = Set.of(
			"Q","W","E","R","T",
			"A","S","D","F","G",
			"Z","X","C","V","B"
	);
	
	private Set<String> keysArrRL = Set.of(
			"Y","U","I","O","P",
			"H","J","K","L",
			"N","M"
	);
	
	private Set<String> keysArrLD = Set.of(
			"D1","D2","D3","D4","D5"
	);
	
	private Set<String> keysArrRD = Set.of(
			"D6","D7","D8","D9","D0"
	);
	
	private double[] orderArr = new double[4];
	
	private String header = "";
	private Integer flagFW = 0;
	private String resultStr = "";
	private double resultDbl = 0f;
	private double standDev = 0f;
	private double mean = 0f;
	private Integer sum = 0;
	
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
	
	public void submit() throws IOException {
		if(dir != null) {
			if(!actAmoField.getText().equals("")) {
			    logToConsole("Calculating data has started \n");
				
				Path rootPath = Paths.get(dir.getAbsolutePath());
				
				List<Path> allFiles = new ArrayList<>();
				
				Integer actionAmount = Integer.parseInt(actAmoField.getText());
				
				try {
					listAllFiles(rootPath, allFiles);
			    
					Path mainDescPath = Paths.get(rootPath.getParent().toString(), rootPath.getFileName().toString());
					Path newFolderPath = Paths.get(mainDescPath.toString() + "_cal");
					
					if(radButtAll.isSelected()) {
						newFolderPath = Paths.get(newFolderPath.toString() + "_full");
					} else {
						newFolderPath = Paths.get(newFolderPath.toString() + "_shrt");
					}
					
					Files.createDirectories(newFolderPath);
					
					for(Path p : allFiles) {
					 	counters.clear();
					 	header = "";
					 	
					 	if(radButtAll.isSelected()) {
					 		for(Integer i = 0; i < keysArr.length; i++) {
								counters.put(keysArr[i], new ArrayList<>());
								header += keysArr[i] + ",";
							}
					 		header += "User";
					 	} else {
							counters.put("LL", new ArrayList<>());
							counters.put("RL", new ArrayList<>());
							counters.put("LD", new ArrayList<>());
							counters.put("RD", new ArrayList<>());
							header = "LL,RL,LD,RD,User";
					 	}
						
			        	URI path1 = p.getParent().toUri();
			        	URI path2 = rootPath.toUri();
			        	
			        	URI rel = path2.relativize(path1);
			        	
			        	String userName = p.getFileName().toString().substring(0, 3);
			        	
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
						
						flagFW = 0;
						resultStr = "";
						resultDbl = 0;
						sum = 0;
						standDev = 0f;
						mean = 0f;
						
						while(sc.hasNext()) {
							line = sc.next();
							String[] parametry = line.split(",");
							
							if(radButtAll.isSelected()) {
								if(counters.containsKey(parametry[2]) && parametry[1].equals("DT")) {
									counters.get(parametry[2]).add(Integer.parseInt(parametry[0]));
									
									if(counters.get(parametry[2]).size() == actionAmount) {
										if(flagFW == 0) {
											fileWriter.write(header);
											fileWriter.write(System.getProperty("line.separator"));
											flagFW = 1;
										}
										
										for(Integer i = 0; i < counters.size(); i++) {
											if(counters.get(keysArr[i]).size() != 0) {
												for(Integer j = 0; j < counters.get(keysArr[i]).size(); j++) {
													sum += counters.get(keysArr[i]).get(j);
												}
												
												mean = (double) sum / counters.get(keysArr[i]).size();
												
												for(Integer k = 0; k < counters.get(keysArr[i]).size(); k++) {
													standDev += Math.pow((double) counters.get(keysArr[i]).get(k) - mean, 2);
												}
												
												resultDbl = Math.sqrt((double) standDev / counters.get(keysArr[i]).size());
												
												standDev = 0;
												sum = 0;
												resultStr += resultDbl;
											} else {
												resultStr += 0.0f;
											}
											
											resultStr += ",";
										}
										
										resultStr += userName;
										
										fileWriter.write(resultStr);
										fileWriter.write(System.getProperty("line.separator"));
										
										resultStr = "";
										
										counters.get(parametry[2]).clear();
									}	
								}
							} else {
								if(parametry[1].equals("DT")) {
									String switchChoice = "";
									
									if(keysArrLL.contains(parametry[2])) {
										switchChoice = "LL";
									} else if(keysArrRL.contains(parametry[2])) {
										switchChoice = "RL";
									} else if(keysArrLD.contains(parametry[2])) {
										switchChoice = "LD";
									} else if(keysArrRD.contains(parametry[2])) {
										switchChoice = "RD";
									} else {
										continue;
									}
																		
									counters.get(switchChoice).add(Integer.parseInt(parametry[0]));
									
									if(counters.get(switchChoice).size() == actionAmount) {
										if(flagFW == 0) {
											fileWriter.write(header);
											fileWriter.write(System.getProperty("line.separator"));
											flagFW = 1;
										}

										Integer loopCount = 0;
										for(String key : counters.keySet()) {
											Integer i = 0;
											switch(key) {
												case "LL":
													i = 0;
													break;
												case "RL":
													i = 1;
													break;
												case "LD":
													i = 2;
													break;
												case "RD":
													i = 3;
													break;
											}
											loopCount++;
											if(counters.get(key).size() != 0) {
												for(Integer j = 0; j < counters.get(key).size(); j++) {
													sum += counters.get(key).get(j);
												}
												
												mean = (double) sum / counters.get(key).size();
												
												for(Integer k = 0; k < counters.get(key).size(); k++) {
													standDev += Math.pow((double) counters.get(key).get(k) - mean, 2);
												}
												
												resultDbl = Math.sqrt((double) standDev / counters.get(key).size());
												
												standDev = 0;
												sum = 0;
												orderArr[i] = resultDbl;
											} else {
												orderArr[i] = 0.0f;
											}
										}
										
										for(Integer i = 0; i < orderArr.length; i++) {
											resultStr += orderArr[i];
											resultStr += ",";
										}
										
										resultStr += userName;
										
										fileWriter.write(resultStr);
										fileWriter.write(System.getProperty("line.separator"));
										
										resultStr = "";
										
										counters.get(switchChoice).clear();
									}	
								}
							}
						}
						
						sc.close();
						fileWriter.close();
						
						if(outputFile.exists() && outputFile.length() == 0) {
							outputFile.delete();
						}
					 }
					 logToConsole("Calculating data has ended \n");
				} catch (Exception e) {
					logToConsole("There was a problem with chosen directory \n" + e);
				}
			} else {
				logToConsole("Amount of actions isn't provided \n");
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
