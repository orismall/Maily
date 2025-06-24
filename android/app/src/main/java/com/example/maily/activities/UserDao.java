package com.example.maily.activities;

@Dao
public interface UserDao {
    @Insert
    void insert(User user);

    @Query("SELECT * FROM user WHERE id = :id")
    User get(int id);

}
