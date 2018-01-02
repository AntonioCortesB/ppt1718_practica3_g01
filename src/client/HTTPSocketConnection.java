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
    public static final String RecursoHTML_505="<html><body><h1>Versi&oacute;n HTTP no soportada</h1></body></html>";
    public static final String HTTPStatusLine_404="HTTP/1.1 404 NOT FOUND\r\n";
    public static final String RecursoHTML_404="<html><body><h1>Recurso No encontrado</h1></body></html>";
    public static final String HTTPStatusLine_200="HTTP/1.1 200 OK\r\n";
    public static final String HTTPStatusLine_400="HTTP/1.1 400 Bad Request\r\n";
    public static final String RecursoHTML_400="<html><body><h1>Petici(&oacute)n incorrecta</h1></body></html>";
    public static final String HTTPStatusLine_405="HTTP/1.1 405 METHOD NOT ALLOWED\r\n";
    public static final String RecursoHTML_405="<html><body><h1>M&eacute;todo no permitido</h1></body></html>";
    private Socket mSocket=null;
    private static int comprobacion=0;
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
                
                
                String parts[]=request_line.split(" ");
                
                if(parts.length==3){ //COMPROBACION DE ERRORES PARA METODO_HTTP,RECURSO,VERSION Y LONGITUD DE PETICION HTTP
                    if(request_line.startsWith("GET ") || request_line.startsWith("get ")){
   
                        if(!parts[2].equalsIgnoreCase("HTTP/1.1") && !parts[2].equalsIgnoreCase("HTTP/1.0") 
                             && !parts[2].equalsIgnoreCase("http/1.1") && !parts[2].equalsIgnoreCase("http/1.0")   ){
                            HTTP_Status_Line=HTTPStatusLine_505;
                            cadena=RecursoHTML_505;
                            HTTP_Response=cadena.getBytes();
                            comprobacion=1;
                        }else{
                            if(parts[1].equalsIgnoreCase("/")){
                                resourceFile="/index.html";
                            }else{
                                resourceFile=parts[1];
                            }
                            String cadena2=getExtension(resourceFile);
                            HTTP_Response=leerRecurso(resourceFile);
                            
                            if(HTTP_Response==null){ //COMPROBACION SI EXISTE EL RECURSO EN EL SERVIDOR
                                HTTP_Status_Line=HTTPStatusLine_404;
                                cadena=RecursoHTML_404;
                                HTTP_Response=cadena.getBytes();
                                comprobacion=1;
                            }else{
                                HTTP_Status_Line=HTTPStatusLine_200;
                                comprobacion=0;
                            }
                        }
                        
                        
                    }else{
                        HTTP_Status_Line=HTTPStatusLine_405;
                        cadena=RecursoHTML_405;
                        HTTP_Response=cadena.getBytes();
                        comprobacion=1;
                    }
                    
                    
                    
                }else{
                    HTTP_Status_Line=HTTPStatusLine_400;
                    cadena=RecursoHTML_400;
                    HTTP_Response=cadena.getBytes();
                    comprobacion=1;
                }

              do{   
            
                  request_line= input.readLine();
                       
  
                System.out.println(request_line);
            }while(request_line.compareTo("")!=0);
            
            //CABECERAS
            
            int tamaño=HTTP_Response.length;
            
            String connection="Connection:close\r\n"; //caecera connection
            String contentlength="Content-length:" + String.valueOf(tamaño)+"\r\n"; // cabecera content length
            String contenttype="";
            String date=obtenerFecha(); //caecera date a partir del metodo obtener fecha (opcional)
            String server="Server: Servidor de Fran y Javier\r\n";//cabecera server(opional)
            String allow="Allow: GET\r\n"; //cabecera allow(opcional)
            if(comprobacion==1){ //cabecera contenttype en funcion del tipo de recurso
                contenttype="Content-type:text/html\r\n";
                
            }else{
                if(getExtension(resourceFile).equalsIgnoreCase("jpeg")){
                     contenttype="Content-type:image/jpeg\r\n";
                }else if(getExtension(resourceFile).equalsIgnoreCase("html")) {
                    contenttype="Content-type:text/html; charset=utf-8\r\n"; 
                }else if(getExtension(resourceFile).equalsIgnoreCase("txt")){
                    contenttype="Content-type:text/plain\r\n";
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
            
            //recurso
           
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
    
    public byte[] leerRecurso(String resourceFile) {

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

            String parts[]=filename.split("\\.");
            int longitud=parts.length;
            
            return parts[longitud-1];
            
    }
    
    public static String obtenerFecha(){
                
        Calendar fecha = Calendar.getInstance();
      
       String cadena="";
       int anio = fecha.get(Calendar.YEAR);
       int mes = fecha.get(Calendar.MONTH);
       int dia = fecha.get(Calendar.DAY_OF_MONTH);
       int hora = fecha.get(Calendar.HOUR_OF_DAY);
       int minuto = fecha.get(Calendar.MINUTE);
       int segundo = fecha.get(Calendar.SECOND);
       int dia2=fecha.get(Calendar.DAY_OF_WEEK);
       String mes2="";
       String diaSemana="";
       if(dia2==1){
           diaSemana="Sun";
       }else if(dia2==2){
           diaSemana="Mon";
       }else if(dia2==3){
           diaSemana="Tue";
       }else if(dia2==4){
           diaSemana="Wed";
       }else if(dia2==5){
           diaSemana="Thu";
       }else if(dia2==6){
           diaSemana="Fri";
       }else if(dia2==7){
           diaSemana="Sat";
       }
       
       if(mes==0){
           mes2="Jan";
       }else if(mes==1){
           mes2="Feb";
       }else if(mes==2){
           mes2="Mar";
       }else if(mes==3){
           mes2="Apr";
       }else if(mes==4){
           mes2="May";
       }else if(mes==5){
           mes2="Jun";
       }else if(mes==6){
           mes2="Jul";
       }else if(mes==7){
           mes2="Aug";
       }else if(mes==8){
           mes2="Sep";
       }else if(mes==9){
           mes2="Oct";
       }else if(mes==10){
           mes2="Nov";
       }else if(mes==11){
           mes2="Dec";
       }
       
       cadena="Date:" + " " + diaSemana+ "," + " " + dia + " " + mes2 + " " + anio
               + " " + hora + ":" + minuto + ":" + segundo + " "  + "GMT" + "\r\n";
       
       return cadena;
      }
    
    
    
}
