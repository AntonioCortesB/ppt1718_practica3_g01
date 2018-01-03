/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package client;
//Importo una serie de librerías
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.Socket;
import java.util.Calendar;
import java.util.Random;

/**
 *
 * @author Antonio Cortés
 */
public class HTTPSocketConnection implements Runnable{
    public static final String HTTP_Ok="200";
    public static final String HTTPStatusLine_505="HTTP/1.1 505 HTTP VERSION NOT SUPPORTED\r\n";
    public static final String RecursoHTML_505="<html><body><h1>VERSION HTTP NO SOPORTADA</h1></body></html>";
    public static final String HTTPStatusLine_404="HTTP/1.1 404 NOT FOUND\r\n";
    public static final String RecursoHTML_404="<html><body><h1>RECURSO NO ENCONTRADO</h1></body></html>";
    public static final String HTTPStatusLine_200="HTTP/1.1 200 OK\r\n";
    public static final String HTTPStatusLine_400="HTTP/1.1 400 BAD REQUEST\r\n";
    public static final String RecursoHTML_400="<html><body><h1>PETICION INCORRECTA</h1></body></html>";
    public static final String HTTPStatusLine_405="HTTP/1.1 405 METHOD NOT ALLOWED\r\n";
    public static final String RecursoHTML_405="<html><body><h1>METODO NO PERMITIDO</h1></body></html>";
    private Socket mSocket=null;
    private static int check=0; //Para comprobar los casos de errores
    private static String cadena;
    /**
     * Se recibe el socket conectado con el cliente
     * @param s Socket conectado con el cliente
     */
    public HTTPSocketConnection(Socket s){
        mSocket = s;
    }
    public void run() {
        Random r = new Random(System.currentTimeMillis());
        int n=r.nextInt();
        String request_line="";
        BufferedReader input;
        DataOutputStream output;
        FileInputStream input_file;
        try {
            byte[] HTTP_Response=null;
            String HTTP_Status_Line="";
            input = new BufferedReader(new InputStreamReader(mSocket.getInputStream()));
            output = new DataOutputStream(mSocket.getOutputStream());
            
            String resourceFile="index.html";
           
                
                request_line= input.readLine();
                
                
                String parts[]=request_line.split(" "); //para tener en cuenta los espacios 
                
                if(parts.length==3){ //Para ver que haya 3 espacios y si no los hay realizar las operaciones siguientes, así compruebo los tipos de errores
                    if(request_line.startsWith("GET ") || request_line.startsWith("get ")){
   
                        if(!parts[2].equalsIgnoreCase("HTTP/1.1") && !parts[2].equalsIgnoreCase("HTTP/1.0") 
                             && !parts[2].equalsIgnoreCase("http/1.1") && !parts[2].equalsIgnoreCase("http/1.0")   ){
                            HTTP_Status_Line=HTTPStatusLine_505;
                            cadena=RecursoHTML_505;
                            HTTP_Response=cadena.getBytes();
                            check=1;
                        }else{
                            if(parts[1].equalsIgnoreCase("/")){
                                resourceFile="/index.html";
                            }else{
                                resourceFile=parts[1];
                            }
                            String cadena2=getExtension(resourceFile);
                            HTTP_Response=readResource(resourceFile);
                            
                            if(HTTP_Response==null){ //Aquí compruebo si existe el recurso en el servidor para dar error 404notfound en caso de no encontrarlo
                                HTTP_Status_Line=HTTPStatusLine_404;
                                cadena=RecursoHTML_404;
                                HTTP_Response=cadena.getBytes();
                                check=1;
                            }else{
                                HTTP_Status_Line=HTTPStatusLine_200;
                                check=0;
                            }
                        }
                        
                        
                    }else{
                        HTTP_Status_Line=HTTPStatusLine_405;
                        cadena=RecursoHTML_405;
                        HTTP_Response=cadena.getBytes();
                        check=1;
                    }
                    
                    
                    
                }else{
                    HTTP_Status_Line=HTTPStatusLine_400;
                    cadena=RecursoHTML_400;
                    HTTP_Response=cadena.getBytes();
                    check=1;
                }

              do{   
            
                  request_line= input.readLine();
                       
  
                System.out.println(request_line);
            }while(request_line.compareTo("")!=0);
            
            //CABECERAS
            
            int tamaño=HTTP_Response.length;
            //Implementación de cabeceras: 
            String connection="Connection:close\r\n"; //CABECERA CONNECTION
            String contentlength="Content-length:" + String.valueOf(tamaño)+"\r\n"; //CABECERA CONTENT LENGTH
            String contenttype=""; //CABECERA CONTENT TYPE
            String date=getDate(); //CABECERA PARA LA INFORMACIÓN DE LA FECHA Y HORA (ES OPCIONAL)
            String server="Server: Servidor de Antonio Cortes y Daniel Mesa\r\n";//CABECERA SERVER PARA DAR INFORMACION (ES OPCIONAL)
            String allow="Allow: GET\r\n"; //CABECERA ALLOW (ES OPCIONAL)
            if(check==1){ //+CABECERA CONTENT TYPE, ADAPTADA PARA COMPROBAR LOS RECURSOS
                contenttype="Content-type:text/html\r\n";
                
            }else{
                if(getExtension(resourceFile).equalsIgnoreCase("jpeg")){
                     contenttype="Content-type:image/jpeg\r\n";                    //para imagen jpeg
                }else if(getExtension(resourceFile).equalsIgnoreCase("html")) {
                    contenttype="Content-type:text/html; charset=utf-8\r\n";       //para html
                }else if(getExtension(resourceFile).equalsIgnoreCase("txt")){
                    contenttype="Content-type:text/plain\r\n";                     //para texto plano
                }
                
            }
            String cadena1="\r\n";
            output.write(HTTP_Status_Line.getBytes());
            output.write(connection.getBytes());
            output.write(contenttype.getBytes());
            output.write(date.getBytes());
            output.write(server.getBytes());
            output.write(allow.getBytes());
            output.write(contentlength.getBytes());
            output.write(cadena1.getBytes());
            output.write(HTTP_Response);
            
            //RECURSO
           
            input.close();
            output.close();
            mSocket.close();
    
        } catch (IOException e) {
            System.err.println("Exception" + e.getMessage());
        }
        }

    
    /**
     * 
     * Metodo para leer un recurso del disco
     * @param resourceFile
     * @return los bytes del archivo o null si este no existe
     */
    
    public byte[] readResource(String resourceFile) {

        String cadena="." + resourceFile;
        try {

        
        File f= new File(cadena);
        int tam=(int) f.length();
            
         
        FileInputStream fis = new FileInputStream(f);
        byte[]data=new byte[tam];
        fis.read(data);
         
        
         
       
         return data;
         
      }
      catch(Exception e){
         e.printStackTrace();
          return null;
      }
        
        
        
    }
    public static String getExtension(String filename) {

            String parts[]=filename.split("\\."); //Para separar las partes de la peticion
            int longitud=parts.length;
            
            return parts[longitud-1];
            
    }
    
    public static String getDate(){
                
        Calendar fecha = Calendar.getInstance();
      
       String datechain="";
       int year = fecha.get(Calendar.YEAR);
       int month = fecha.get(Calendar.MONTH);
       int day = fecha.get(Calendar.DAY_OF_MONTH);
       int time = fecha.get(Calendar.HOUR_OF_DAY);
       int minute = fecha.get(Calendar.MINUTE);
       int second = fecha.get(Calendar.SECOND);
       int day2=fecha.get(Calendar.DAY_OF_WEEK);
       String month2="";
       String dayOfWeek="";
       if(day2==1){
           dayOfWeek="Sun";
       }else if(day2==2){
           dayOfWeek="Mon";
       }else if(day2==3){
           dayOfWeek="Tue";
       }else if(day2==4){
           dayOfWeek="Wed";
       }else if(day2==5){
           dayOfWeek="Thu";
       }else if(day2==6){
           dayOfWeek="Fri";
       }else if(day2==7){
           dayOfWeek="Sat";
       }
       
       if(month==0){
           month2="Jan";
       }else if(month==1){
           month2="Feb";
       }else if(month==2){
           month2="Mar";
       }else if(month==3){
           month2="Apr";
       }else if(month==4){
           month2="May";
       }else if(month==5){
           month2="Jun";
       }else if(month==6){
           month2="Jul";
       }else if(month==7){
           month2="Aug";
       }else if(month==8){
           month2="Sep";
       }else if(month==9){
           month2="Oct";
       }else if(month==10){
           month2="Nov";
       }else if(month==11){
           month2="Dec";
       }
       
       datechain="Date:" + " " + dayOfWeek+ "," + " " + day + " " + month2 + " " + year
               + " " + time + ":" + minute + ":" + second + " "  + "GMT" + "\r\n";
       
       return datechain;
      }
    
    
    
}
