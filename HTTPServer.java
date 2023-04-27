import java.io.*;
import java.net.*;
import java.util.*;
import java.io.File;

public class HTTPServer {    

    public static void handleRequest(Socket socket) {
        try {
            InputStream in = socket.getInputStream();
            OutputStream out = socket.getOutputStream();
            String requestType = "";
            int ch = 0;
            
            while((ch = in.read()) != '\n') {
                requestType = requestType + ((char) ch);
            }

            String[] getFile = requestType.split(" ");
            File findingFile = new File(getFile[1].substring(1));

            if(requestType.contains("GET")) {
                if(findingFile.isFile()) {
                    String contentType = "";
                    
                    if(getFile[1].contains("html")) {
                        contentType = "text/html";
                    }
                    else if(getFile[1].contains("txt")) {
                        contentType = "text/plain";
                    }

                    int contentLength = (int) findingFile.length();

                    StringBuilder fileContent = new StringBuilder();
                    BufferedReader reader = new BufferedReader(new FileReader(getFile[1].substring(1)));
                    String emptyString = "";
                    while ((emptyString = reader.readLine()) != null) {
                        fileContent.append(emptyString);
                    }
                    reader.close();

                    String returnStatement = "\r\n\r\n" + "Content-Type: " + contentType + "\r\n\r\n" + "Length: " + Integer.toString(contentLength) + "\r\n\r\n" + "\r\n\r\n" + fileContent.toString(); 
                    out.write(returnStatement.getBytes());
                }
                else {
                    String errorCode = "HTTP/1.1 404: Not Found" + "\r\n\r\n" + "Error 404: File Not Found";
                    out.write(errorCode.getBytes());
                }
                
            } 
            else if (requestType.contains("POST")) {
                if(findingFile.isFile() && getFile[1].contains("txt")) {
                    StringBuilder fileContent = new StringBuilder();
                    BufferedReader reader = new BufferedReader(new FileReader(getFile[1].substring(1)));
                    String emptyString = "";
                    while ((emptyString = reader.readLine()) != null) {
                        fileContent.append(emptyString);
                    }
                    reader.close();

                    String requestString = "";
                    int character = 0;
                    while(((character = in.read()) != -1) && (requestString.indexOf("\r\n\r\n") == -1)) {
                        requestString = requestString + ((char) character);
                    }
                    String[] postRequest = requestString.split("\n");

                    int contentLength = 0;
                    String contentType = "";
                    for(int i = 0; i < postRequest.length; i++) {
                        if(postRequest[i].contains("Length")) {
                            String[] contentLengthString = postRequest[i].split(" ");
                            contentLength = Integer.parseInt(contentLengthString[1].trim());
                        } 
                        else if(postRequest[i].contains("Type")) {
                            String[] contentTypeString = postRequest[i].split(" ");
                            contentType = contentTypeString[1].trim();
                        }
                    }

                    String message = "" + (char) character;
                    int charc = 0;
                    while(message.length() != contentLength) {
                        charc = in.read();
                        message = message + ((char) charc);
                    }

                    if(contentType.contains("text/plain")) {
                        FileWriter writer = new FileWriter(getFile[1].substring(1));
                        writer.write(fileContent.toString() + " " + message);
                        writer.close();

                        String returnStatement = "HTTP/1.1 200 OK" + "\r\n\r\n" + "Content-Type: " + contentType + "\r\n\r\n" + "Content-Length: " + Integer.toString(contentLength) + "\r\n\r\n" + "\r\n\r\n" + fileContent.toString() + " " + message; 
                        out.write(returnStatement.getBytes());
                    } 
                    else {
                        String errorCode = "HTTP/1.1 405: Method Not Allowed" + "\r\n\r\n";
                        out.write(errorCode.getBytes());
                    }
                    
                }
                else {
                    String errorCode = "HTTP/1.1 405: Method Not Allowed" + "\r\n\r\n";
                    out.write(errorCode.getBytes());
                }
            } 
            else if (requestType.contains("PUT")) {
                if(!findingFile.isFile()) {
                    findingFile.createNewFile();
                }
                
                String requestString = "";
                int character = 0;
                while(((character = in.read()) != -1) && (requestString.indexOf("\r\n\r\n") == -1)) {
                    requestString = requestString + ((char) character);
                }
                String[] postRequest = requestString.split("\n");

                int contentLength = 0;
                String contentType = "";
                for(int i = 0; i < postRequest.length; i++) {
                    if(postRequest[i].contains("Length")) {
                        String[] contentLengthString = postRequest[i].split(" ");
                        contentLength = Integer.parseInt(contentLengthString[1].trim());
                    } 
                    else if(postRequest[i].contains("Type")) {
                        String[] contentTypeString = postRequest[i].split(" ");
                        contentType = contentTypeString[1].trim();
                    }
                }

                String message = "" + (char) character;
                int charc = 0;
                while(message.length() != contentLength) {
                    charc = in.read();
                    message = message + ((char) charc);
                }

                FileWriter writer = new FileWriter(getFile[1].substring(1));
                writer.write(message);
                writer.close();

                String returnStatement = "HTTP/1.1 200" + "\r\n\r\n" + "Content-Type: " + contentType + "\r\n\r\n" + "Content-Length: " + Integer.toString(contentLength) + "\r\n\r\n" + "\r\n\r\n" + message; 

                out.write(returnStatement.getBytes());
            } 
            else if (requestType.contains("DELETE")) {
                findingFile.delete();

                String returnStatement = "HTTP/1.1 200" + "\r\n\r\n"; 
                out.write(returnStatement.getBytes());
            }
            in.close();
            out.close();
            socket.close();
        }
        catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public static void main(String... args) throws Exception {
        ServerSocket server = new ServerSocket(80);
        Socket socket = null;

        while((socket = server.accept()) != null) {
            final Socket threadSocket = socket;
            new Thread( () -> handleRequest(threadSocket)).start();
        }
        System.out.println("Closed");
        server.close();
    }
}