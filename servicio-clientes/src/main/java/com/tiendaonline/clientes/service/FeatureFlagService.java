package com.tiendaonline.clientes.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.statsig.sdk.Statsig;
import com.statsig.sdk.StatsigUser;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;

@Service 
public class FeatureFlagService {

    //Traemos la llave secreta de Statsig desde el application.yml.
    @Value("${statsig.secret-key:}")
    private String secretKey;


    private Boolean inicializado = false;

    //Se ejecuta automáticamente apenas Spring termina de crear este servicio al arrancar la app.
    @PostConstruct
    public void init(){
        //Verificamos haya una clave configurada.
        if(secretKey != null && !secretKey.isBlank()){
            try {
                //Conectamos con Statsig y descargamos las reglas de los flags.
                Statsig.initializeAsync(secretKey).get();
                inicializado = true;
            } catch (Exception e) {
                System.out.println("No se pudo inicializar statsig: " + e.getMessage());
            }
        }
    }

    //Consultamos si el flag mask-pii esta prendido o apagado para ese usr.
    public Boolean isMaskPiiActivo(String idUsuario){
        //Si statsig no se pudo inicializar retornamos false
        if(!inicializado){
            return false;
        }
        StatsigUser usuario = new StatsigUser(idUsuario);

        //Evaluamos el gate mask-pii y eso devuelve true o false.
        return Statsig.checkGateSync(usuario,"mask-pii");
    }

    //Se ejecuta automaticamente cuando apagamos el microservicio, cerrando la conexion con statsig.
    @PreDestroy
    public void shutdown(){
        if(inicializado){
            Statsig.shutdown();
        }
    }

}
