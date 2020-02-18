import org.apache.http.HttpEntity;
import org.apache.http.client.fluent.Request;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.apache.http.client.fluent.Content;

import java.io.*;
import java.math.BigInteger;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.Charset;
import java.security.MessageDigest;

public class CodenationRequest {

    public static void main(String args[]) throws IOException {

        URL url = new URL("https://api.codenation.dev/v1/challenge/dev-ps/generate-data?token=3550dd80aadfc0aaa9589c6d80e940d51e83db9f");
        HttpURLConnection con = (HttpURLConnection) url.openConnection();
        con.setRequestProperty("Content-Type", "application/json");
        con.setRequestMethod("GET");
        String inputLine;
        FileWriter writeFile = null;
        StringBuilder sb = new StringBuilder();

        BufferedReader in = new BufferedReader(new InputStreamReader(
                con.getInputStream()));


        while ((inputLine = in.readLine()) != null) {

            try{
                JSONParser parser = new JSONParser();
                JSONObject jsonObject = (JSONObject) parser.parse(inputLine.toString());
                int numeroCasas = Integer.parseInt(jsonObject.get("numero_casas").toString());
                String cifrado = jsonObject.get("cifrado").toString();
                String decriptado = decrypt(numeroCasas, cifrado);
                jsonObject.put("decifrado", decriptado);
                String sha1 = transformSHA1(decriptado);
                jsonObject.put("resumo_criptografico", sha1);
                System.out.println(jsonObject);

                //Escreve no arquivo conteudo do Objeto JSON
                writeFile = new FileWriter("answer.json");
                writeFile.write(jsonObject.toString());
                writeFile.close();
                sendFile(jsonObject);
            }
            catch(IOException | ParseException e){
                e.printStackTrace();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        in.close();

    }

    public static String encrypt(int shift, String text) {
        StringBuffer result = new StringBuffer();

        for (int i = 0; i < text.length(); i++) {
            if (Character.isLetter( text.charAt( i ) )) {
                 if (Character.isUpperCase( text.charAt( i ) )) {
                    char ch = (char) (((int) text.charAt( i ) +
                            shift - 65) % 26 + 65);
                    result.append( ch );
                } else {
                    char ch = (char) (((int) text.charAt( i ) +
                            shift - 97) % 26 + 97);
                    result.append( ch );
                }
            } else {
                result.append( text.charAt( i ) );
            }
        }

        System.out.println( result );
        return result.toString();
    }

    public static String decrypt(int shift, String text) {
        shift = 26 - shift;
        return encrypt( shift, text );
    }

    public static String transformSHA1(String text) {
        try {
            MessageDigest md = MessageDigest.getInstance( "SHA-1" );

            byte[] messageDigest = md.digest( text.getBytes() );

            BigInteger no = new BigInteger( 1, messageDigest );

            String hashtext = no.toString( 16 );

            while (hashtext.length() < 32) {
                hashtext = "0" + hashtext;
            }

            return hashtext;
        } catch (Exception e) {
            return e.getMessage();
        }
    }

    public static void sendFile(JSONObject jsonObject) throws URISyntaxException, IOException {

        File answerJson = new File("answer.json");
        HttpEntity form =  MultipartEntityBuilder.create()
                .setMode(HttpMultipartMode.BROWSER_COMPATIBLE)
                .setCharset(Charset.forName("UTF-8"))
                .addBinaryBody("answer", answerJson)
                .build();


        Content responsta = Request
                .Post(new URI("https://api.codenation.dev/v1/challenge/dev-ps/submit-solution?token=" + "3550dd80aadfc0aaa9589c6d80e940d51e83db9f"))
                .body(form)
                .execute()
                .returnContent();

        System.out.println("response = " + responsta);
    }

}
