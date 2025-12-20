package models;

public class PercentageBonus implements BonusStrategy {
    private final double percentage;

    @Override
    public String getDescription() {
        return "Процентный";
    }

    public PercentageBonus(double percentage) {
        if (percentage < 0) throw new IllegalArgumentException("Процент не может быть отрицательным");
        this.percentage = percentage;
    }

    @Override
    public double calculateBonus(double deposit) {
        return deposit * percentage;
    }
}