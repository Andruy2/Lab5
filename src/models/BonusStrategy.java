package models;

public interface BonusStrategy {
    double calculateBonus(double initialDeposit);
    String getDescription();
}
