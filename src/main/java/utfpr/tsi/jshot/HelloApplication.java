package utfpr.tsi.jshot;

import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.io.IOException;
import java.util.List;

public class HelloApplication extends Application {

    private static Stage stageGlobal;
    private static Scene sceneMain;


    private static FXMLLoader fxmlLoader;
    static TableView<String> tabelaDeImagens = new TableView<>();


    @Override
    public void start(Stage stage) throws IOException {
        stageGlobal = stage;
        fxmlLoader = new FXMLLoader(HelloApplication.class.getResource("hello-view.fxml"));
        sceneMain = new Scene(fxmlLoader.load(), 400, 500);
        stageGlobal.setTitle("JShot");
        stageGlobal.getIcons().add(new Image("http://simpleicon.com/wp-content/uploads/camera-256x256.png"));
        stageGlobal.setScene(sceneMain);
        stageGlobal.show();
    }

    public static void main(String[] args) {
        launch();
    }

    public static void exibirMinhaGaleria() throws IOException {
        stageGlobal.setTitle("JShot");
        List<String> images = HelloController.getObjects();

        GridPane gridPane = montaGridGaleria(images);

        ScrollPane scrollPane = new ScrollPane();
        scrollPane.setContent(gridPane);
        scrollPane.setPannable(true);


        Scene scene = new Scene(scrollPane);
        stageGlobal.setScene(scene);
        stageGlobal.show();
        stageGlobal.setMaximized(true);
    }

    public static GridPane montaGridGaleria(List<String> imagesLinks){
        GridPane gridPane = new GridPane();
        double fracaoTela = Toolkit.getDefaultToolkit().getScreenSize().getWidth() / 3;

        int cont = 0;

        for (int i = 0; i < imagesLinks.size(); i++) {
            for (int j = 0; j < 3; j++) {
                if (cont >= imagesLinks.size()){
                    break;
                }
                gridPane.add(montaCardGaleria(cont, fracaoTela, imagesLinks), j, i);
                cont++;
            }
        }
        return gridPane;
    }

    private static Node montaCardGaleria(int cont, double fracaoTela, List<String> imagesLinks) {
        Button botaoExcluir = new Button("Excluir");
        Button botaoCopiar = new Button("Copiar URL");


        estilizarBotaoCopiarURL(botaoCopiar);
        estilizarBotaoExcluir(botaoExcluir);

        ImageView img = new ImageView(imagesLinks.get(cont));
        VBox sp = new VBox();
        HBox hb = new HBox();
        hb.getChildren().add(botaoCopiar);
        hb.getChildren().add(botaoExcluir);
        sp.getChildren().add(img);
        sp.getChildren().add(hb);
        final int finalCont = cont;
        botaoCopiar.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent actionEvent) {
                Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
                clipboard.setContents(new StringSelection(imagesLinks.get(finalCont)),
                        null);
            }
        });
        botaoExcluir.setOnAction(actionEvent -> HelloController.deleteObject(imagesLinks.get(finalCont).replace("https://jshot.s3.sa-east-1.amazonaws.com/", "").replace("+", "\s")));

        img.setPreserveRatio(true);
        img.setFitWidth(fracaoTela);
        return sp;
    }

    public static void estilizarBotaoExcluir(Button bt){
        bt.setStyle("-fx-background-color: #8f0000;" +
                "-fx-text-fill: #FFFFFF");
        bt.setUnderline(true);
    }

    public static void estilizarBotaoCopiarURL(Button bt){
        bt.setStyle("-fx-background-color: #2196F3;");
        bt.setUnderline(true);
    }

    public static void minimizar(){
        stageGlobal.setIconified(true);

    }

    public static void maximizar(){
        stageGlobal.setIconified(false);
    }



    public static void backToMainWindow() throws IOException {
        stageGlobal.hide();
        stageGlobal.setMaximized(false);
        fxmlLoader = new FXMLLoader(HelloApplication.class.getResource("hello-view.fxml"));
        sceneMain = new Scene(fxmlLoader.load(), 400, 500);
        stageGlobal.setTitle("JShot");
        stageGlobal.getIcons().add(new Image("http://simpleicon.com/wp-content/uploads/camera-256x256.png"));
        stageGlobal.setScene(sceneMain);
        stageGlobal.show();
    }





}

