package models;

public class NoBonus implements BonusStrategy {
    @Override
    public String getDescription() {
        return "Без бонуса";
    }

    @Override
    public double calculateBonus(double deposit) {
        return 0;
    }
}