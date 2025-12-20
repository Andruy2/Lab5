package models;

public class PensionerClient extends Client {
    public PensionerClient(String name, String passport, double deposit) {
        super(name, deposit, passport, new FixedBonus(3000));
    }

    @Override
    public String getType() {
        return "Пенсионер";
    }
}