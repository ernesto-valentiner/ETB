package evidentia.GUI;

import java.awt.Desktop;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Optional;

import evidentia.Entity;
import evidentia.Evidentia;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceDialog;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextInputDialog;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class UserInterface extends Application {

	private VBox infoBox = new VBox();
	private Evidentia evidentia = new Evidentia();

	public static void main(String[] args) {
		// TODO discuss with Tewo
		launch(args);
	}

	@Override
	public void start(Stage primaryStage) {
		primaryStage.setTitle("Evidentia App");

		// Console
		TextArea textArea = new TextArea();
		textArea.setEditable(false);
		Console console = new Console(textArea);
		PrintStream ps = new PrintStream(console, true);
		System.setOut(ps);
		System.setErr(ps);

		// Menus
		Menu evidentia = new Menu("Evidentia");
		MenuItem evidentiaItem1 = new MenuItem("Java Doc");
		evidentiaItem1.setOnAction(new EventHandler<ActionEvent>() {

			@Override
			public void handle(ActionEvent event) {
				URI uri;
				try {
					uri = new URI("file:///Users/sellami/Documents/evidentia/target/apidocs/index.html");
					uri.normalize();
					Desktop.getDesktop().browse(uri);
				} catch (URISyntaxException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		});
		MenuItem evidentiaItem2 = new MenuItem("About...");
		evidentiaItem2.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				final Stage aboutDialog = new Stage();
				aboutDialog.initModality(Modality.APPLICATION_MODAL);

				Button okButton = new Button("CLOSE");
				okButton.setOnAction(new EventHandler<ActionEvent>() {

					@Override
					public void handle(ActionEvent e) {
						aboutDialog.close();
					}

				});

				VBox vBox = new VBox(new Text("About Evidentia...."), okButton);
				vBox.setSpacing(30.);
				vBox.setPadding(new Insets(5, 5, 5, 5));

				Scene dialogScene = new Scene(vBox);

				aboutDialog.setScene(dialogScene);
				aboutDialog.show();
			}
		});
		MenuItem evidentiaItem3 = new MenuItem("Exit");
		evidentiaItem3.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				Platform.exit();
			}
		});
		evidentia.getItems().addAll(evidentiaItem1, evidentiaItem2, evidentiaItem3);

		Menu entity = new Menu("Entity");
		MenuItem nodeItem1 = new MenuItem("Initialize");
		nodeItem1.setOnAction(new FileChooserEventHandler("-init", textArea, primaryStage));
		MenuItem nodeItem2 = new MenuItem("Information");
		nodeItem2.setOnAction(new CommandEventHandler("-show-info", textArea));
		MenuItem nodeItem3 = new MenuItem("Modes");
		nodeItem3.setOnAction(new CommandEventHandler("-show-modes", textArea));
		MenuItem nodeItem4 = new MenuItem("Clean");
		nodeItem4.setOnAction(new CommandEventHandler("-clean", textArea));
		MenuItem nodeItem5 = new MenuItem("Uninitialize");
		nodeItem5.setOnAction(new CommandEventHandler("-uninit", textArea));
		MenuItem nodeItem6 = new MenuItem("Set Port");
		nodeItem6.setOnAction(
				new TextInputDialogEventHandler("-set-port", textArea, "Set Port", "Enter the port number", "Port:"));
		MenuItem nodeItem7 = new MenuItem("Set Repository");
		nodeItem7.setOnAction(new DirectoryChooserEventHandler("-set-repo", textArea, primaryStage));
		MenuItem nodeItem8 = new MenuItem("Set Mode");
		nodeItem8.setOnAction(new ChoiceDialogEventHandler("-set-mode", textArea, "Set Mode", "Select the mode",
				"Mode:", new String[] { "-noDEN", "-DEN" }));
		MenuItem nodeItem9 = new MenuItem("Import...");
		nodeItem9.setOnAction(new DirectoryChooserEventHandler("-import", textArea, primaryStage));
		MenuItem nodeItem10 = new MenuItem("Export...");
		nodeItem10.setOnAction(new CommandEventHandler("-export", textArea));

		entity.getItems().addAll(nodeItem1, nodeItem2, nodeItem3, nodeItem4, nodeItem5, nodeItem6, nodeItem7, nodeItem8,
				nodeItem9, nodeItem10);

		Menu service = new Menu("Service");
		MenuItem serviceItem1 = new MenuItem("Add");
		serviceItem1.setOnAction(new FileChooserEventHandler("-add-service", textArea, primaryStage));
		MenuItem serviceItem2 = new MenuItem("Remove");
		serviceItem2.setOnAction(new TextInputDialogEventHandler("-rm-service", textArea, "Remove Service",
				"Enter the service name", "Name:"));
		service.getItems().addAll(serviceItem1, serviceItem2);

		Menu workflow = new Menu("Workflow");
		MenuItem workflowItem1 = new MenuItem("Add");
		workflowItem1.setOnAction(new FileChooserEventHandler("-add-workflow", textArea, primaryStage));
		MenuItem workflowItem2 = new MenuItem("Remove");
		workflowItem2.setOnAction(new TextInputDialogEventHandler("-rm-workflow", textArea, "Remove Workflow",
				"Enter the workflow name", "Name:"));
		workflow.getItems().addAll(workflowItem1, workflowItem2);

		Menu claim = new Menu("Claim");
		MenuItem claimItem1 = new MenuItem("Add");
		claimItem1.setOnAction(
				new TextInputDialogEventHandler("-add-claim", textArea, "Add Claim", "Enter the claim name", "Name:"));
		MenuItem claimItem2 = new MenuItem("Remove");
		claimItem2.setOnAction(
				new TextInputDialogEventHandler("-rm-claim", textArea, "Remove Claim", "Enter the claim id", "ID:"));
		claim.getItems().addAll(claimItem1, claimItem2);

		Menu help = new Menu("Help");
		MenuItem helpItem1 = new MenuItem("?");
		helpItem1.setOnAction(new CommandEventHandler("-help", textArea));
		help.getItems().add(helpItem1);

		// Menu Bar
		MenuBar menuBar = new MenuBar();
		menuBar.getMenus().addAll(evidentia, entity, service, workflow, claim, help);

		updateInfoBox();
		HBox hBox = new HBox(textArea, infoBox);

		VBox vBox = new VBox(menuBar, hBox);

		primaryStage.setScene(new Scene(vBox, 960, 600));
		textArea.setPrefHeight(3000);
		textArea.setPrefWidth(primaryStage.getScene().getWidth() * 0.66);
		primaryStage.show();
	}

	private void updateInfoBox() {
		Entity test = new Entity(true);
		Text name = new Text("Name: " + test.getName());
		Text ip = new Text("IP: " + test.getIP());
		Text port = new Text("Port: " + test.getPort());
		Text numServices = new Text("Number of services: " + test.getServicePack().toJSONObject().size());
		/*
		 * ListView<String> services = new ListView<String>();
		 * services.getItems().addAll(test.getServicePack().getServices().keySet());
		 */
		ComboBox<String> services = new ComboBox<String>();
		services.getItems().addAll(test.getServicePack().getServices().keySet());
		services.getSelectionModel().selectFirst();
		if (services.getItems().size() == 0)
			services.setDisable(true);
		Text numWorkflows = new Text("Number of workflows: " + test.getWorkflowsPack().toJSONObject().size());
		ComboBox<String> workflows = new ComboBox<String>();
		workflows.getItems().addAll(test.getWorkflowsPack().getWorkflows().keySet());
		workflows.getSelectionModel().selectFirst();
		if (workflows.getItems().size() == 0)
			workflows.setDisable(true);
		Text numClaims = new Text("Number of claims: " + test.getClaims().toJSONObject().size());
		TextFlow noDENMode = new TextFlow();
		Text noDENModeText1 = new Text("DEN Mode: ");
		Text noDENModeText2 = new Text();
		if (evidentia.isModeNoDEN()) {
			noDENModeText2.setText("OFF");
			noDENModeText2.setFill(Color.RED);
		}
		else {
			noDENModeText2.setText("ON");
			noDENModeText2.setFill(Color.GREEN);
		}
		noDENMode.getChildren().addAll(noDENModeText1, noDENModeText2);

		infoBox.getChildren().clear();
		infoBox.getChildren().addAll(name, ip, port, numServices, services, numWorkflows, workflows, numClaims, noDENMode);
	}

	private static class Console extends OutputStream {

		private TextArea output;

		public Console(TextArea ta) {
			this.output = ta;
		}

		@Override
		public void write(int i) throws IOException {
			output.appendText(String.valueOf((char) i));
		}
	}

	private class CommandEventHandler implements EventHandler<ActionEvent> {

		private final String command;
		private final TextArea textArea;

		public CommandEventHandler(String command, TextArea textArea) {
			this.command = command;
			this.textArea = textArea;
		}

		@Override
		public void handle(ActionEvent event) {
			try {
				textArea.clear();
				evidentia.run(new String[] { command });
				updateInfoBox();
			} catch (Exception e) {
				e.printStackTrace();
			}

		}

	}

	private class FileChooserEventHandler implements EventHandler<ActionEvent> {

		private final String command;
		private final TextArea textArea;
		private final Stage primaryStage;
		private final FileChooser fileChooser = new FileChooser();

		public FileChooserEventHandler(String command, TextArea textArea, Stage primaryStage) {
			this.command = command;
			this.textArea = textArea;
			this.primaryStage = primaryStage;
		}

		@Override
		public void handle(final ActionEvent event) {
			File file = fileChooser.showOpenDialog(primaryStage);
			if (file != null) {
				try {
					textArea.clear();
					evidentia.run(new String[] { command, file.getPath() });
					updateInfoBox();
				} catch (Exception e1) {
					e1.printStackTrace();
				}
			}
		}
	}

	private class DirectoryChooserEventHandler implements EventHandler<ActionEvent> {

		private final String command;
		private final TextArea textArea;
		private final Stage primaryStage;
		private final DirectoryChooser directoryChooser = new DirectoryChooser();

		public DirectoryChooserEventHandler(String command, TextArea textArea, Stage primaryStage) {
			this.command = command;
			this.textArea = textArea;
			this.primaryStage = primaryStage;
		}

		@Override
		public void handle(final ActionEvent event) {
			File directory = directoryChooser.showDialog(primaryStage);
			if (directory != null) {
				try {
					textArea.clear();
					evidentia.run(new String[] { command, directory.getPath() });
					updateInfoBox();
				} catch (Exception e1) {
					e1.printStackTrace();
				}
			}
		}
	}

	private class TextInputDialogEventHandler implements EventHandler<ActionEvent> {

		private final String command;
		private final TextArea textArea;
		private final TextInputDialog dialog = new TextInputDialog("");

		public TextInputDialogEventHandler(String command, TextArea textArea, String title, String header,
				String content) {
			this.command = command;
			this.textArea = textArea;
			dialog.setTitle(title);
			dialog.setHeaderText(header);
			dialog.setContentText(content);
		}

		@Override
		public void handle(final ActionEvent event) {

			Optional<String> result = dialog.showAndWait();

			result.ifPresent(name -> {
				try {
					textArea.clear();
					evidentia.run(new String[] { command, name });
					updateInfoBox();
				} catch (Exception e1) {
					e1.printStackTrace();
				}
			});
		}
	}

	private class ChoiceDialogEventHandler implements EventHandler<ActionEvent> {

		private final String command;
		private final TextArea textArea;
		private final ChoiceDialog<String> dialog;

		public ChoiceDialogEventHandler(String command, TextArea textArea, String title, String header, String content,
				String[] options) {
			this.command = command;
			this.textArea = textArea;
			dialog = new ChoiceDialog<String>(options[0], options);
			dialog.setTitle(title);
			dialog.setHeaderText(header);
			dialog.setContentText(content);
		}

		@Override
		public void handle(final ActionEvent event) {

			Optional<String> result = dialog.showAndWait();

			result.ifPresent(name -> {
				try {
					textArea.clear();
					evidentia.run(new String[] { command, name });
					updateInfoBox();
				} catch (Exception e1) {
					e1.printStackTrace();
				}
			});
		}
	}
}
