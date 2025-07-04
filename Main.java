import java.time.*;
import java.util.ArrayList;
import java.util.List;



abstract class Product {
    protected String name;
    protected double price;
    protected int availableQuantity;

    public Product(String name, double price, int availableQuantity) {
        this.name = name;
        this.setPrice(price);
        this.availableQuantity = availableQuantity;
    }

    public String getName() {
        return name;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double newPrice) {
        if (newPrice >= 0) {
            this.price = newPrice;
        } else {
            System.out.println("NEGATIVE PRICE !? ARE YOU KIDDING ME?");
        }
    }

    ////////////// Quantity Management //////////////
    public int getAvailableQuantity() {
        return availableQuantity;
    }

    public void addQuantity(int addedQuantity) {
        if (addedQuantity > 0) {
            this.availableQuantity += addedQuantity;
        } else {
            System.out.println("ENTER A VALID AMOUNT TO ADD");
        }
    }

    public void decreaseQuantity(int amount) {
        if (amount > 0 && amount <= availableQuantity) {
            this.availableQuantity -= amount;
        } else {
            System.out.println("ENTER A VALID AMOUNT");
        }
    }

    public boolean isExpired() {
        return false; // EL DEFAULT 
    }
}

abstract class ExpirableProduct extends Product {
    protected LocalDate expiryDate;

    public ExpirableProduct(String name, double price, int availableQuantity, LocalDate expiryDate) {
        super(name, price, availableQuantity);
        this.expiryDate = expiryDate;
    }

    public LocalDate getExpiryDate() {
        return expiryDate;
    }

    public void setExpiryDate(LocalDate newExpiryDate) {
        this.expiryDate = newExpiryDate;
    }

    @Override
    public boolean isExpired() {
        return LocalDate.now().isAfter(expiryDate);
    }
}

interface ShippableProduct {
    String getName();
    double getWeight();
}

///////////////////////// PRODUCTS /////////////////////////
class Cheese extends ExpirableProduct implements ShippableProduct {
    private double weight;

    public Cheese(String name, double price, int quantity, LocalDate expiryDate, double weight) {
        super(name, price, quantity, expiryDate);
        this.weight = weight;
    }

    @Override
    public double getWeight() {
        return weight;
    }
}


class Biscuits extends ExpirableProduct {
    public Biscuits(String name, double price, int quantity, LocalDate expiryDate) {
        super(name, price, quantity, expiryDate);
    }
}


class TV extends Product implements ShippableProduct {
    private double weight;

    public TV(String name, double price, int quantity, double weight) {
        super(name, price, quantity);
        this.weight = weight;
    }

    @Override
    public double getWeight() {
        return weight;
    }
}


class MobileScratchCard extends Product {
    public MobileScratchCard(String name, double price, int quantity) {
        super(name, price, quantity);
    }
}




class Customer {
    private String name;
    private double balance;
    private Cart cart;

    public Customer(String name, double balance) {
        this.name = name;
        this.balance = balance;
        this.cart = new Cart(); 
    }

    public String getName() {
        return name;
    }

    public double getBalance() {
        return balance;
    }

    public void pay(double amount) {
        if (amount <= balance) {
            balance -= amount;
        } else {
            System.out.println("GO TO WORK => GET MORE MONEY => COME BACK");
        }
    }

    public void addBalance(double amount) {
        if (amount > 0) {
            balance += amount;
        }
    }

    public Cart getCart() {
        return cart;
    }
}


class CartItem {
    private Product product;
    private int quantity;

    public CartItem(Product product, int quantity) {
        this.product = product;
        this.quantity = quantity;
    }

    public Product getProduct() {
        return product;
    }

    public int getQuantity() {
        return quantity;
    }

    public double getTotalPrice() {
        return product.getPrice() * quantity;
    }

    public double getTotalWeight() {
        if (isShippable()) {
            return ((ShippableProduct) product).getWeight() * quantity;
        }
        return 0; // Non-shippable products have no weight
    }

    public boolean isExpired() {
        return product.isExpired();
    }

    public boolean isShippable() {
        return product instanceof ShippableProduct;
    }
}


class Cart {
    private List<CartItem> items = new ArrayList<>();

    public void addProduct(Product product, int quantity) {
        if (quantity > product.getAvailableQuantity()) {
            System.out.println("OOPS ! WE DON'T HAVE ENOUGH QUANTITY FOR " + product.getName());
            return;
        }

        if (product.isExpired()) {
            System.out.println(product.getName() + " IS EXPIRED !");
            return;
        }

        items.add(new CartItem(product, quantity));
        // System.out.println("Added to cart: " + product.getName() + " x" + quantity);
    }

    public void checkout(Customer customer) {
        if (items.isEmpty()) {
            System.out.println("CART IS EMPTY, GO BUY SOMETHING");
            return;
        }

        double subtotal = 0;
        double shippingFees = 0;
        List<CartItem> shippables = new ArrayList<>();




        for (CartItem item : items) {
            Product product = item.getProduct();

            if (item.isExpired()) {
                System.out.println(product.getName() + " IS EXPIRED !");
                return;
            }

            if (item.getQuantity() > product.getAvailableQuantity()) {
                System.out.println("OOPS ! WE DON'T HAVE ENOUGH QUANTITY FOR " + product.getName());
                return;
            }
            
            subtotal += item.getTotalPrice();

            if (item.isShippable()) {
                shippingFees += ( item.getTotalWeight() / 10 ) * 15; // 15 EGP for each 10 kg
                shippables.add(item);
            }
        }


        double total = subtotal + shippingFees;


        if (customer.getBalance() < total) {
            System.out.println("GO TO WORK => GET MORE MONEY => COME BACK");
            return;
        }

        customer.pay(total);

        // Deduct quantities
        for (CartItem item : items) {
            item.getProduct().decreaseQuantity(item.getQuantity());
        }



        // Send to shipping service if needed
        if (!shippables.isEmpty()) {
            ShippingService.ship(shippables);
        }

        System.out.println("** Checkout receipt **");

        for (CartItem item : items) {
            System.out.println(item.getQuantity() + "x " + item.getProduct().getName() + "        " + item.getTotalPrice());
        }

        System.out.println("-------------------------");
        System.out.println("Subtotal:        " + subtotal);
        System.out.println("Shipping:        " + shippingFees);
        System.out.println("Amount:          " + total);


        items.clear(); // Empty the cart after checkout
    }
}



class ShippingService {
    public static void ship(List<CartItem> products) {
        int totalWeight = 0;

        System.out.println();
        System.out.println("** Shipment Notice **");

        for (CartItem p : products) {


            System.out.println(p.getQuantity() + "x " + p.getProduct().getName() + "        " + p.getTotalWeight());

            
            totalWeight += p.getTotalWeight();
        }
        System.out.println("Total package weight: " + totalWeight);
        System.out.println();
    }
}

class Main {
    public static void main(String[] args) {

        Cheese cheese = new Cheese("Cheese", 50.0, 10, LocalDate.of(2024, 8, 1), 2.0);
        Biscuits biscuits = new Biscuits("Biscuits", 20.0, 5, LocalDate.of(2025, 12, 31));
        TV tv = new TV("Smart TV", 500.0, 2, 15.0);
        MobileScratchCard scratchCard = new MobileScratchCard("Mob-Card", 10.0, 100);


        Customer customer = new Customer("Mohamed Amir", 10000.0);

        customer.getCart().addProduct(cheese, 2);  
        customer.getCart().addProduct(biscuits, 12);
        customer.getCart().addProduct(tv, 1);
        customer.getCart().addProduct(scratchCard, 5);  

        customer.getCart().checkout(customer);
    }
}