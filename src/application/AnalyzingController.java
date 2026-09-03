package application;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Scanner;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

public class AnalyzingController {
	
	@FXML
	Stage stage;
	@FXML
	Parent root;
	@FXML
	Scene scene;
	@FXML
	TextField pathTextField;
	@FXML
	TextField directoryTextField;
	@FXML
	Button exitButton;
	@FXML
	AnchorPane scenePane;
	@FXML
	CheckBox checkboxAllFiles;
	@FXML
	CheckBox checkboxSepFiles;
	
	private String fileName;
	private String filePath;
	private String line;
	private String dirPath;
	private String fileListAsText;
	private File defaultDir;
	private File dir;
	private List<File> fileList;
	private List<String> klawisze = new ArrayList<String>();
	private Integer i = 0;
	private Integer licznik = 0;
	private String[] parametry;
	private String[] parametryP;
	private String[] parametryR;
	private Queue<String> queueP = new LinkedList<>();
	private Queue<String> queueR = new LinkedList<>();
	private Long pressTime;
	private Long flyTime;
	private Long pressToPressTime;
	private Long tempP;
	private Long tempR;
	private FileWriter fileWriter;
	private FileWriter fileSepWriter;
	
	private SceneController mainController;
	
	public void setMainController(SceneController mainController) {
        this.mainController = mainController;
    }
	
	public void chooseFile(ActionEvent event) {	
		
		FileChooser fileChooser = new FileChooser();
		FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter("Pliki CSV (*.csv)", "*.csv");
		fileChooser.getExtensionFilters().add(extFilter);
		
		fileChooser.setTitle("Wybierz pliki CSV, z których chcesz analizować dane");
		defaultDir = new File(System.getProperty("user.home"));
		fileChooser.setInitialDirectory(defaultDir);
		
		fileList = fileChooser.showOpenMultipleDialog(stage);
		
		fileListAsText = "";
		if(fileList != null) {
			for(File f : fileList) {
				filePath = f.getAbsolutePath();
				fileListAsText += filePath + "; ";
			}
		}
		pathTextField.setText(fileListAsText);
	}
	
	public void chooseDir(ActionEvent event) {
		
		DirectoryChooser directoryChooser = new DirectoryChooser();
		
		directoryChooser.setTitle("Wybierz folder, do którego chcesz zapisać analizowane dane");
		defaultDir = new File(System.getProperty("user.home"));
		directoryChooser.setInitialDirectory(defaultDir);
		
		dir = directoryChooser.showDialog(stage);
		
		dirPath = "";
		if(dir != null) {
			dirPath = dir.getAbsolutePath();
		}
		directoryTextField.setText(dirPath);
		
	}
	
	public void submit(ActionEvent event) throws IOException {
		
		if(fileList != null && dir != null) {
			if(checkboxAllFiles.isSelected() || checkboxSepFiles.isSelected()) {
				for(File f : fileList) {
					try {
						logToConsole("Rozpoczęto analizę dla pliku " + f.getName() + "\n");
						Scanner sc = new Scanner(new File(f.getAbsolutePath()));
						sc.useDelimiter("\\R");
						
						if(checkboxAllFiles.isSelected()) {
							fileName = f.getName().replaceAll("(?<!^)[.]" + "[^.]*$", "");
							File outputFile = new File(dirPath, fileName + "_ana_ALL.csv");
							fileWriter = new FileWriter(outputFile);
						}
						
						klawisze.clear();
						while(sc.hasNext()) {
							line = sc.next();
							parametry = line.split(",");
							i = parametry.length - 1;
							licznik = 0;
							for(String s: klawisze) {
								if(s.equals(parametry[i])) {
									licznik++;
								}
							}
							if(licznik == 0) {
								klawisze.add(parametry[i]);
							}
						}
						sc.close();
						
						for(String s: klawisze) {
							Scanner sc1 = new Scanner(new File(f.getAbsolutePath()));
							sc1.useDelimiter("\\R");
							queueP.clear();
							queueR.clear();
							
							if(checkboxSepFiles.isSelected()) {
								fileName = f.getName().replaceAll("(?<!^)[.]" + "[^.]*$", "");
								File outputFile = new File(dirPath, fileName + "_ana_" + s + ".csv");
								fileSepWriter = new FileWriter(outputFile);
							}
							while(sc1.hasNext()) {
								line = sc1.next();
								parametry = line.split(",");
								i = parametry.length - 1;
								if(s.equals(parametry[i])) {
									if(parametry[0].equals("K")) {
										queueP.add(parametry[i] + "," + parametry[1]);
									} else {
										queueR.add(parametry[i] + "," + parametry[1]);
									}
								}
							}
							sc1.close();
							licznik = 0;
							for(String d: queueP) {
								if(queueP.peek() != null && queueR.peek() != null) {
									parametryP = d.split(",");
									parametryR = queueR.remove().split(",");
									if(licznik != 0) {
										pressToPressTime = Long.parseLong(parametryP[1]) - tempP;
										flyTime = Long.parseLong(parametryP[1]) - tempR;
										
										line = parametryP[0] + "," + "Fly Time" + "," + flyTime;
										
										if(checkboxAllFiles.isSelected()) {
											fileWriter.write(line);
											fileWriter.write(System.getProperty("line.separator"));
										}
										if(checkboxSepFiles.isSelected()) {
											fileSepWriter.write(line);
											fileSepWriter.write(System.getProperty("line.separator"));
										}
										
										line = parametryP[0] + "," + "Press To Press Time" + "," + pressToPressTime;
										
										if(checkboxAllFiles.isSelected()) {
											fileWriter.write(line);
											fileWriter.write(System.getProperty("line.separator"));
										}
										if(checkboxSepFiles.isSelected()) {
											fileSepWriter.write(line);
											fileSepWriter.write(System.getProperty("line.separator"));
										}
									}
									licznik++;
									tempP = Long.parseLong(parametryP[1]);
									tempR = Long.parseLong(parametryR[1]);
									pressTime = tempR - tempP;
									line = parametryP[0] + "," + "Press Time" + "," + pressTime;
									
									if(checkboxAllFiles.isSelected()) {
										fileWriter.write(line);
										fileWriter.write(System.getProperty("line.separator"));
									}						
									if(checkboxSepFiles.isSelected()) {
										fileSepWriter.write(line);
										fileSepWriter.write(System.getProperty("line.separator"));
									}
								} else {
									break;
								}
							}
							if(fileSepWriter != null) {
								fileSepWriter.close();
							}
						}
						if(fileWriter != null) {
							fileWriter.close();
						}
						logToConsole("Zakończono analizę dla pliku " + f.getName() + "\n");	
					} catch (FileNotFoundException e) {
						if(dir.exists()) {
							logToConsole("Nie odnaleziono pliku \n");
						} else {
							logToConsole("Nie odnaleziono folderu końcowego \n");
						}
					}
				}
			} else {
				logToConsole("Musisz wybrać conajmniej jedną z dwóch opcji na zapis danych \n");
			}
		}
		else {
			logToConsole("Nie wypełniono wszystkich danych \n");
		}
	}
	
	private void logToConsole(String message) {
        if (mainController != null) {
            mainController.addToConsole(message);
        }
    }
}
