package utfpr.tsi.jshot;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.DeleteObjectRequest;
import com.amazonaws.services.s3.model.ListObjectsV2Result;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.s3.model.S3ObjectSummary;
import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import org.apache.commons.io.FileUtils;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.ImageOutputStream;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.TimeUnit;


public class HelloController {


    public static void deleteObject(String nomeDoArquivo) {
        s3client().deleteObject("jshot", nomeDoArquivo);
        getObjects();
    }

    @FXML
    public void takeScreenShotAction() throws IOException, AWTException, InterruptedException {


        HelloApplication.minimizar();
        TimeUnit.SECONDS.sleep(3);


        // -------- CRIA O DIRETÓRIO TEMPORÁRIO
        Path temp = Files.createDirectories(Path.of(Paths.get(System.getProperty("user.dir")) + "/tempJShot"));


        // -------- GERA O NOME POR DATA PARA O ARQUIVO
        Date data = new Date();
        SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy HH.mm.ss");
        File compressedImageFile = new File(String.valueOf(temp), sdf.format(new Date()) + ".jpg");

        // -------- SALVA A IMAGEM
        OutputStream os = new FileOutputStream(compressedImageFile);
        Iterator<ImageWriter> writers = ImageIO.getImageWritersByFormatName("jpg");
        ImageWriter writer = (ImageWriter) writers.next();
        ImageOutputStream ios = ImageIO.createImageOutputStream(os);
        writer.setOutput(ios);
        ImageWriteParam param = writer.getDefaultWriteParam();
        param.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
        param.setCompressionQuality(1.0f);  // Change the quality value you pref
        writer.write(null, new IIOImage(capturaTela(), null, null), param);


        // -------- UPA O ARQUIVO PARA AWS S3
        File caminho = new File(String.valueOf(Path.of(Paths.get(System.getProperty("user.dir")) + "/tempJShot")));
        File[] arquivos = caminho.listFiles();
        assert arquivos != null;
        for (File arq : arquivos){
            uploadFile(arq.toString(), arq.getName());
        }
        os.close();
        ios.close();
        writer.dispose();



        // -------- DELETA O DIRETÓRIO TEMPORÁRIO
        FileUtils.deleteDirectory(new File(String.valueOf(Path.of(Paths.get(System.getProperty("user.dir")) + "/tempJShot"))));
        getObjects();


        HelloApplication.maximizar();

    }

    public BufferedImage capturaTela() throws AWTException, IOException {
        Robot r = new Robot();
        Rectangle capture = new Rectangle(Toolkit.getDefaultToolkit().getScreenSize());
        BufferedImage image = r.createScreenCapture(capture);
        return image;
    }

    @FXML
    public void abrirMinhaGaleria() throws IOException {
        HelloApplication.exibirMinhaGaleria();
    }

    @FXML
    public void backToMainButton() throws IOException {
        HelloApplication.backToMainWindow();
    }




    public void uploadFile(String localFilePath, String nomeDoArquivo) {
        try {
            String bucketName = "jshot";
            File file = new File(localFilePath);
            s3client().putObject(new PutObjectRequest(bucketName, nomeDoArquivo, file));
        }
        catch(AmazonServiceException ex) {
            System.out.println(ex.getMessage());
            System.out.println(ex.getStatusCode());
        }
    }


    static String awsId = "";
    static String awsKey = "";
    static String region = "";

    public static AmazonS3 s3client(){
        BasicAWSCredentials awsCred = new BasicAWSCredentials(awsId, awsKey);
        AmazonS3 s3client = AmazonS3ClientBuilder.standard().withRegion(Regions.fromName(region))
                .withCredentials(new AWSStaticCredentialsProvider(awsCred)).build();
        return s3client;
    }

    public static List<String> getObjects(){
        List<String> links = new ArrayList<>();
        BasicAWSCredentials awsCred = new BasicAWSCredentials(awsId, awsKey);
        AmazonS3 s3client = AmazonS3ClientBuilder.standard().withRegion(Regions.fromName(region))
                .withCredentials(new AWSStaticCredentialsProvider(awsCred)).build();
        ListObjectsV2Result result = s3client.listObjectsV2("jshot");
        List<S3ObjectSummary> objects = result.getObjectSummaries();
        for (S3ObjectSummary os : objects) {
            if (os.getKey().endsWith(".jpg") || os.getKey().endsWith(".jpeg") || os.getKey().endsWith(".png")){
                links.add("https://jshot.s3.sa-east-1.amazonaws.com/" + os.getKey().replaceAll("\\s+", "+"));
            }
        }
        return links;
    }




}