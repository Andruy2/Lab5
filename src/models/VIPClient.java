package models;

public class VIPClient extends Client {
    public VIPClient(String name, String passport, double deposit) {
        super(name, deposit, passport, new PercentageBonus(0.1)); // VIP +10%
    }

    @Override
    public String getType() {
        return "Вип";
    }
}