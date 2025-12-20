package models;

public class SimpleClient extends Client {
    public SimpleClient(String name, String passport, double deposit) {
        super(name, deposit, passport, new NoBonus());
    }

    @Override
    public String getType() {
        return "Обычный клиент";
    }
}
