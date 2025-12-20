package models;

public class FixedBonus implements BonusStrategy {
    private final double bonusAmount;

    @Override
    public String getDescription() {
        return "Фиксированный";
    }

    public FixedBonus(double amount) {
        this.bonusAmount = amount;
    }

    @Override
    public double calculateBonus(double deposit) {
        return bonusAmount;
    }
}
