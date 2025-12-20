package models;

import java.util.ArrayList;
import java.util.List;

public class Bank {
    private List<Client> clients = new ArrayList<>();

    public void addClient(String name, double deposit, String passport, String clientType) {
        Client client = createClient(name, deposit, passport, clientType.toLowerCase());
        clients.add(client);
    }

    public void addClient(Client client) {
        clients.add(client);
    }

    public void removeClient(String passport) {
        clients.removeIf(client -> client.getPassport().equals(passport));
    }

    public void updateClient(String passport, Client updatedClient) {
        for (int i = 0; i < clients.size(); i++) {
            if (clients.get(i).getPassport().equals(passport)) {
                clients.set(i, updatedClient);
                break;
            }
        }
    }

    public Client getClientByPassport(String passport) {
        return clients.stream()
                .filter(client -> client.getPassport().equals(passport))
                .findFirst()
                .orElse(null);
    }

    private Client createClient(String name, double deposit, String passport, String type) {
        return switch (type) {
            case "regular", "обычный", "обычный клиент" -> new SimpleClient(name, passport, deposit);
            case "pensioner", "пенсионер" -> new PensionerClient(name, passport, deposit);
            case "vip", "вип" -> new VIPClient(name, passport, deposit);
            default -> throw new IllegalArgumentException("Неизвестный тип: " + type);
        };
    }

    public double getTotalDeposits() {
        double total = 0;
        for (Client client : clients) {
            total += client.getDeposit();
        }
        return total;
    }

    public List<Client> getAllClients() {
        return new ArrayList<>(clients);
    }

    public void sortByName() {
        clients.sort((c1, c2) -> c1.getName().compareToIgnoreCase(c2.getName()));
    }

    public void sortByDeposit() {
        clients.sort((c1, c2) -> Double.compare(c1.getDeposit(), c2.getDeposit()));
    }

    public void sortByType() {
        clients.sort((c1, c2) -> c1.getType().compareToIgnoreCase(c2.getType()));
    }
}