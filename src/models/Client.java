package models;

public abstract class Client {
    private final String name;
    private String passport;
    private double deposit;
    private BonusStrategy bonusStrategy;

    protected Client(String name, double initialDeposit, String passport, BonusStrategy strategy) {
        if (initialDeposit < 0) throw new IllegalArgumentException("Вклад не может быть отрицательным");
        this.passport = passport;
        this.name = name;
        this.deposit = initialDeposit + strategy.calculateBonus(initialDeposit);
        this.bonusStrategy = strategy;
    }

    public abstract String getType();

    public String getPassport() { return passport; }
    public void setPassport(String passport) { this.passport = passport; }

    public double getDeposit() { return deposit; }
    public void setDeposit(double deposit) { this.deposit = deposit; }

    public String getName() { return name; }

    public BonusStrategy getBonusStrategy() { return bonusStrategy; }
    public void setBonusStrategy(BonusStrategy strategy) {
        this.bonusStrategy = strategy;
        // Пересчитываем депозит при изменении стратегии
        this.deposit = this.deposit - this.bonusStrategy.calculateBonus(this.deposit) + strategy.calculateBonus(this.deposit);
    }

    public String getClientInfo() {
        return String.format("Клиент: %s\nКатегория: %s\nПаспорт: %s\nБонус: %s\nВклад (с учетом бонуса): %.2f руб.",
                name, getType(), passport, bonusStrategy.getDescription(), deposit);
    }
}