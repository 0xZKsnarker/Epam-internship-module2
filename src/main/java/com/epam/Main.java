// Path: src/main/java/com/epam/Main.java
package com.epam;

import com.epam.config.AppConfig;
import com.epam.facade.GymFacade;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

public class Main {
    public static void main(String[] args) {
        ApplicationContext context = new AnnotationConfigApplicationContext(AppConfig.class);

        GymFacade gymFacade = context.getBean(GymFacade.class);
        System.out.println("Gym CRM Application Started!");
        System.out.println("\nGym CRM Application Finished.");
    }
}