package libmng.domain;

import libmng.util.IdGenerator;

public class User {
    private final int id;
    private final String name;
    private final String email;
    private int paidFines;

    public User(String name, String email) {
        this.id = IdGenerator.nextId();
        this.name = name;
        this.email = email;
        this.paidFines = 0;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public int getPaidFines() {
        return paidFines;
    }

    public void addPayment(int amount) {
        if (amount < 0) throw new IllegalArgumentException("amount");
        this.paidFines += amount;
    }
}
