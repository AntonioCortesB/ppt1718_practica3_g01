/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package client;
//Importo las librerías:
import java.io.BufferedReader; //Para leer y escribir en ficheros.
import java.io.DataOutputStream; //Para escribir y leer datos en ficheros binarios
import java.io.IOException; //Para los streams
import java.io.InputStreamReader; //Para leer los streams de entrada
import java.net.InetAddress; //Para proporcionar objetos para manipular direcciones IP y nombres de dominio
import java.net.Socket; //Para dar soporte a los sockets
import java.net.UnknownHostException; //Para controlar la excepción del host desconocido

/**
 *
 * @author Antonio Cortés
 */
//Con la clase thread a veces no podemos extender otro objeto, así que usamos
//la clase runnable para que podamos crear hilos trabajadores.
//Solo implementa el método run.
public class Client implements Runnable {


    private String clientid=""; //Implemento la variable privada "clientid"
    
    public Client(String ID){ //La hago pública como "ID"
        clientid=ID;


    
}//Fin de  public Client
   
    public synchronized void run() { //Este es el método run, único método de Runnable
   
        try{       //Con un try-catch realizo una serie de acciones en caso de que no haya excepción
            InetAddress destination = InetAddress.getByName("www10.ujaen.es"); 
//la dirección destino la cojo con getByName,que funciona gracias a haber implementado la librería InetAddress
    
//Ahora muestro por pantalla un mensaje de que se está inicializando el cliente (Con la ID cliente)
            System.out.println("-------------------\r\nInitializing client... "+clientid+"\r\n--------------------");
//Muestro por pantalla un mensaje de que se está conectando con el socket.            
            System.out.println("Connecting with socket... "+destination.toString());
//Construyo el objeto socket pasándole destino y el puerto 80            
            Socket socket = new Socket(destination,80); //El puerto 80 por defecto
            BufferedReader input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            DataOutputStream output = new DataOutputStream(socket.getOutputStream());
            output.write("GET / HTTP/1.1\r\nhost:www10.ujaen.es\r\nConnection:close\r\n\r\n".getBytes()); //Con getBytes obtengo una matriz de bytes
            String line=""; //Declaro el string de línea 
            int i=0;        //Declaro "i" para el iterador
            while((line=input.readLine())!=null) { //Mientras la línea de entrada no sea nula
                if(i==0)                           //Si estamos en la primera iteración
                    System.out.println("<"+clientid+"> "+line);  //Muestro el "clientid" y la línea
                i++;                                            //Siguiente iteración
            }  //Fin del while 
            
        }catch (UnknownHostException e) {
            System.out.println("\tUnable to find address for");
        } catch(IOException ex){
        
            System.out.println("\tError: " +ex.getMessage()+"\r\n"+ex.getStackTrace()); //Muestro los errores en caso de haberlos
        }
    } //Fin de public synchronized void run 
} //Fin de public class Client
