package project.restaurant.controller;

import java.io.IOException;
import java.net.URL;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;

import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import javafx.util.Duration;
import project.restaurant.dao.OrderPadDao;
import project.restaurant.dao.UserDao;
import project.restaurant.model.OrderPad;
import project.restaurant.model.User;
import project.restaurant.resources.ImpressoraPDF;

public class MenuController implements Initializable {

	private User user;

	 @FXML
    private HBox pnlStatusBar;

    @FXML
    private Label lblUsuario;

    @FXML
    private Label lblData;

    @FXML
    private Label lblHora;

    @FXML
    private Button btnNovaComanda;

    @FXML
    private Button btnRelatorio;

    @FXML
    private Button btnGrafico;
    
    @FXML
    private AnchorPane orderPadContainer;
    
    private List<OrderPad> orderPads;
    
    private OrderPadDao orderPadDao;
    
	private Stage stage;

	// Configura��es iniciais da tela de menu
	@Override
	public void initialize(URL location, ResourceBundle resources) {
		this.setOrderPadDao(new OrderPadDao());
		this.configuraBarraStatus();
		this.configuraStage();
	}

	public void setUser(User user) {
		this.user = user;
	}

	public User getUser() {
		return user;
	}

	public Stage getStage() {
		return stage;
	}

	public void setStage(Stage stage) {
		this.stage = stage;
	}
	
	
	// Quando abre a tela e coloca o nome do usu�rio da tela de status
	public void onShow() {
		this.lblUsuario.setText("Usu�rio: " + this.getUser().getName());
		this.loadOrderPads();
	}

	@FXML
	void mnoSair(ActionEvent event) {
		if (this.onCloseQuery()) {
			System.exit(0);
		} else {
			event.consume();
		}
	}

	// Evento de fechamento da tela com pergunta
	public boolean onCloseQuery() {
		Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
		alert.setTitle("Pergunta");
		alert.setHeaderText("Deseja sair do sistema?");
		ButtonType buttonTypeNO = ButtonType.NO;
		ButtonType buttonTypeYES = ButtonType.YES;
		alert.getButtonTypes().setAll(buttonTypeYES, buttonTypeNO);
		Optional<ButtonType> result = alert.showAndWait();
		return result.get() == buttonTypeYES ? true : false;
	}
	
	public void loadOrderPads() {
		try {
			this.setOrderPads(this.getOrderPadDao().getByUserId(this.getUser().getId()));
			HBox orderPadOrganizer = new HBox();
			orderPadOrganizer.setHgrow(orderPadOrganizer, Priority.ALWAYS);
			
			for(int i = 0; i < this.getOrderPads().size(); i++) {
				FXMLLoader fxmlLoader = new FXMLLoader();
				Pane orderPadCard = fxmlLoader.load(getClass().getResource("/project/restaurant/view/OrderPadCard.fxml").openStream());
				OrderPadCardController orderPadCardController = (OrderPadCardController) fxmlLoader.getController();
				
				OrderPad orderPad = this.getOrderPads().get(i);
				
				if(orderPad.getPaid()) {
					orderPadCard.setStyle("-fx-background-color: #3c3");
				} else {
					orderPadCard.setStyle("-fx-background-color: #c33");
				}
				
				orderPadCardController.setOrderPad(orderPad);
				
				orderPadCardController.onShow();
				
				orderPadOrganizer.getChildren().add(orderPadCard);
				
			}
			orderPadContainer.getChildren().add(orderPadOrganizer);
			
			
		} catch(Exception err) {
			err.printStackTrace();
		}
		
		
	}
	
	// Configura a tela inicialmente
	public void configuraStage() {
		this.setStage(new Stage());
		this.getStage().initModality(Modality.APPLICATION_MODAL);
		this.getStage().resizableProperty().setValue(Boolean.FALSE);
	}

	// Configura a barra de status para mostrar a hora e nome do usu�rio
	public void configuraBarraStatus() {
		DateTimeFormatter dataFormatada = DateTimeFormatter.ofPattern("dd/MM/yyyy");
		this.lblData.setText("Data: " + LocalDateTime.now().format(dataFormatada));

		Timeline clock = new Timeline(new KeyFrame(Duration.ZERO, e -> {
			DateTimeFormatter horaFormatada = DateTimeFormatter.ofPattern("HH:mm:ss");
			this.lblHora.setText("Hora: " + LocalDateTime.now().format(horaFormatada));
		}), new KeyFrame(Duration.seconds(1)));
		clock.setCycleCount(Animation.INDEFINITE);
		clock.play();
	}
	
	 @FXML
    void OnNewComanda(ActionEvent event) {
		OrderPad orderPad = new OrderPad();
		boolean btnConfirmarClic = this.showTelaUsuarioEditar(orderPad);
		if (btnConfirmarClic) {
			this.getOrderPadDao().save(orderPad);
			this.loadOrderPads();
		}
    }
	 
	 public boolean showTelaUsuarioEditar(OrderPad orderPad) {
			try {
				FXMLLoader loader = new FXMLLoader(getClass().getResource("/project/restaurant/view/OrderPadAdd.fxml"));
				Parent orderPadEditXML = loader.load();

				// Criando uma janela e colocando o layout do xml nessa janela
				Stage windowsOrderPadEdit = new Stage();
				windowsOrderPadEdit.setTitle("Cadastro de usu�rio");
				windowsOrderPadEdit.initModality(Modality.APPLICATION_MODAL);
				windowsOrderPadEdit.resizableProperty().setValue(Boolean.FALSE);

				Scene orderPadEditLayout = new Scene(orderPadEditXML);
				windowsOrderPadEdit.setScene(orderPadEditLayout);

				// Setando o cliente no Controller.
				OrderPadAddController orderPadAddController = loader.getController();
				orderPadAddController.setWindowOrderPadEdit(windowsOrderPadEdit);
				orderPadAddController.setOrderPad(orderPad);
				orderPadAddController.setUser(user);

				// Mostra o Dialog e espera at� que o usu�rio feche
				windowsOrderPadEdit.showAndWait();

				return orderPadAddController.isOkClick();

			} catch (Exception e) {
				e.printStackTrace();
			}
			return false;
		}

    @FXML
    void onGrafico(ActionEvent event) {
    	try {
			// Carregando o arquivo da tela de usuario
			FXMLLoader loader = new FXMLLoader(
					getClass().getResource("/project/restaurant/view/TenMostExpensiveOrderPadsGraph.fxml"));
			Parent orderPadGraficoXML = loader.load();

			// Carregando a classe de controle do arquivo da tela de login
			TenMostExpensiveOrderPadsGraphController tenMostExpensiveOrderPadsGraphController = loader.getController();
			Scene orderPadListLayout = new Scene(orderPadGraficoXML);

			this.getStage().setScene(orderPadListLayout);
			this.getStage().setTitle("Gr�fico");

			this.getStage().show();
		} catch (Exception e) {
			e.printStackTrace();
		}
    }

    @FXML
    void onRelatorio(ActionEvent event) {
    	try {
			UserLogController userLogController = new UserLogController();

			ImpressoraPDF.criarArquivo(userLogController.LOG_FILE,
			userLogController.LOG_TITLE, userLogController.LOG_HEADER,
			userLogController.LogData());
			Alert alert = new Alert(AlertType.INFORMATION);
			alert.setTitle("Informa��o");
			alert.setHeaderText(null);
			alert.setContentText("Relat�rio criado!\nDispon�vel em: " + ImpressoraPDF.caminhoRelatorio);
			alert.showAndWait();
		} catch (Exception e) {
			e.printStackTrace();
		}
    }

	public List<OrderPad> getOrderPads() {
		return orderPads;
	}

	public void setOrderPads(List<OrderPad> orderPads) {
		this.orderPads = orderPads;
	}

	public OrderPadDao getOrderPadDao() {
		return orderPadDao;
	}

	public void setOrderPadDao(OrderPadDao orderPadDao) {
		this.orderPadDao = orderPadDao;
	}
    
    

}
