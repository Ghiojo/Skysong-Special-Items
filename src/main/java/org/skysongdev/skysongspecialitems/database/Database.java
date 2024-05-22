package org.skysongdev.skysongspecialitems.database;

import org.bukkit.Bukkit;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class Database {
    private final String HOST;
    private final String PORT;
    private final String USER;
    private final String PASSWORD;
    private final String DATABASE_NAME;

    public Database(String HOST, String PORT, String USER, String PASSWORD, String DATABASE_NAME) {
        this.HOST = HOST;
        this.PORT = PORT;
        this.USER = USER;
        this.PASSWORD = PASSWORD;
        this.DATABASE_NAME = DATABASE_NAME;
    }

    private Connection sqlConnection;
    public Connection getConnection() throws SQLException{
        if(sqlConnection != null){
            return sqlConnection;
        }
        String url = "jdbc:mysql://" + this.HOST + "/" + this.DATABASE_NAME;

        sqlConnection = DriverManager.getConnection(url, this.USER, this.PASSWORD);

        Bukkit.getLogger().info("Connected to the database");
        return sqlConnection;
    }

    public void initializeDatabase() throws SQLException{

        Statement buildStatement = getConnection().createStatement();
        String sql = "CREATE TABLE IF NOT EXISTS book_descriptions(id int AUTO_INCREMENT primary key, name TINYTEXT, bookcontent MEDIUMTEXT)";
        buildStatement.execute(sql);


        Bukkit.getLogger().info("Created the book_descriptions table");
    }

    public BookDescriptions findBookByID(int id) throws SQLException {
        Statement statement = getConnection().createStatement();
        String sql = "SELECT * FROM book_descriptions WHERE id = " + id;
        ResultSet results = statement.executeQuery(sql);

        if(results.next()){
            BookDescriptions bookDescriptions = new BookDescriptions(id, results.getString("name"), results.getString("bookcontent"));

            statement.close();

            return bookDescriptions;
        }
        statement.close();
        return null;
    }

    public int findLatestId() throws SQLException{
        int id = -1;
        Statement statement = getConnection().createStatement();
        String sql = "SELECT MAX(id) AS id FROM book_descriptions";
        ResultSet results = statement.executeQuery(sql);
        if(results.next()) {
            id = results.getInt("id");
        }
        statement.close();
        return id;
    }
    public void createBookDescription(BookDescriptions bookDesc) throws SQLException{
        PreparedStatement statement = getConnection().prepareStatement("INSERT INTO book_descriptions(name, bookcontent) VALUES (?, ?)");
        statement.setString(1, bookDesc.getName());
        statement.setString(2, bookDesc.getBookcontent());

        statement.executeUpdate();
        statement.close();
    }

    public void updateBookDescription(BookDescriptions bookDesc) throws SQLException{
        PreparedStatement statement = getConnection().prepareStatement("UPDATE book_descriptions SET name = ?, bookcontent = ? WHERE id = ?");
        statement.setString(1, bookDesc.getName());
        statement.setString(2, bookDesc.getBookcontent());
        statement.setInt(3, bookDesc.getId());

        statement.executeUpdate();
        statement.close();
    }

    public void deleteBookDescription(int id) throws SQLException{
        PreparedStatement statement = getConnection().prepareStatement("DELETE FROM book_descriptions WHERE id = ?");
        statement.setInt(1, id);
        statement.executeUpdate();
        statement.close();
    }

    public List<BookDescriptions> listAllBooks() throws SQLException{
        Statement statement = getConnection().createStatement();
        String sql = "SELECT * FROM book_descriptions ORDER BY id";
        ResultSet results = statement.executeQuery(sql);
        List<BookDescriptions> books = new ArrayList<>();

        while(results.next()){
            int id = results.getInt("id");
            String name = results.getString("name");
            books.add(new BookDescriptions(id, name, ""));
        }

        statement.close();

        return books;
    }
}
