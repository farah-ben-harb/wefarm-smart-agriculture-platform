package utils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DataBase {
    final String URL="jdbc:mysql://localhost:3306/projetpidev";

    final String USERNAME="root";
    final String PASSWORD="";
    Connection connection;

    static DataBase instance;

    private DataBase(){
        try {
            connection= DriverManager.getConnection(URL,USERNAME,PASSWORD);
            System.out.println("Connexion établie");
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    public static   DataBase getInstance(){
        if (instance==null){
            instance= new DataBase();
        }
        return instance;
    }

    public Connection getConnection() {
        return connection;
    }
}
