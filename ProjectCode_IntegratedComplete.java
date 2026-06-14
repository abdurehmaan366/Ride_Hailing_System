 // Version -2
 package Ride_HailingProject;

 /*
 OOP PROJECT - RIDE HAILING SYSTEM
 Features clean menus and demonstrates core OOP principles
 */

 import java.util.ArrayList;
 import java.util.Scanner;
 import java.io.*;
 import java.text.SimpleDateFormat;

 // ==================== ENUMS ====================

 enum VehicleType {
     CAR,
     BIKE,
     VAN
 }

 enum PaymentMethod {
     WALLET,
     CASH,
     CARD
 }

 enum RideStatus {
     PENDING,
     ACCEPTED,
     IN_PROGRESS,
     COMPLETED,
     CANCELLED
 }

 // ==================== EXCEPTIONS ====================

 class NoDriverAvailableException extends Exception {
     public NoDriverAvailableException(String message) {
         super(message);
     }
 }

 class PaymentFailedException extends Exception {
     public PaymentFailedException(String message) {
         super(message);
     }
 }

 // ==================== CORE CLASSES ====================

 // ENCAPSULATION: Private fields with public getters/setters
 class Vehicle implements Serializable {
     private String id;
     private String regNumber;
     private String model;
     private VehicleType type;
     private int capacity;

     public Vehicle(String id, String regNumber, String model, VehicleType type, int capacity) {
         this.id = id;
         this.regNumber = regNumber;
         this.model = model;
         this.type = type;
         this.capacity = capacity;
     }

     // ENCAPSULATION: Getters
     public String getId() { return id; }
     public String getRegNumber() { return regNumber; }
     public String getModel() { return model; }
     public VehicleType getType() { return type; }
     public int getCapacity() { return capacity; }

     // ENCAPSULATION: Setters with validation
     public void setRegNumber(String regNumber) {
         if (regNumber == null || regNumber.isEmpty()) {
             throw new IllegalArgumentException("Registration number cannot be null or empty");
         }
         this.regNumber = regNumber;
     }

     public void setModel(String model) {
         if (model == null || model.isEmpty()) {
             throw new IllegalArgumentException("Model cannot be null or empty");
         }
         this.model = model;
     }

     // METHOD OVERRIDING: toString()
     @Override
     public String toString() {
         return id + " " + regNumber + " " + model + " " + type;
     }
 }

 // ENCAPSULATION: Private fields with validation
 class Location implements Serializable {
     private double latitude;
     private double longitude;
     private String address;

     public Location(double latitude, double longitude, String address) {
         setLatitude(latitude);
         setLongitude(longitude);
         this.address = address;
     }

     // ENCAPSULATION: Getters
     public double getLatitude() { return latitude; }
     public double getLongitude() { return longitude; }
     public String getAddress() { return address; }

     // ENCAPSULATION: Setters with validation
     public void setLatitude(double latitude) {
         if (latitude >= 33.53 && latitude <= 35.86) {
             this.latitude = latitude;
         } else {
             throw new IllegalArgumentException("Latitude must be between 33.53 and 35.86 for Islamabad.");
         }
     }

     public void setLongitude(double longitude) {
         if (longitude >= 72.78 && longitude <= 73.23) {
             this.longitude = longitude;
         } else {
             throw new IllegalArgumentException("Longitude must be between 72.78 and 73.23 for Islamabad.");
         }
     }

     public void setAddress(String address) { this.address = address; }

     // Member function
     public double calculateDistance(Location destination) {
         if (destination == null) {
             throw new IllegalArgumentException("Destination location cannot be null");
         }
         final int R = 6371; // km
         double latDis = Math.toRadians(destination.getLatitude() - this.latitude);
         double lonDis = Math.toRadians(destination.getLongitude() - this.longitude);
         double a = Math.sin(latDis / 2) * Math.sin(latDis / 2) +
                 Math.cos(Math.toRadians(this.latitude)) * Math.cos(Math.toRadians(destination.getLatitude())) *
                         Math.sin(lonDis / 2) * Math.sin(lonDis / 2);
         double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
         return R * c;
     }

     // METHOD OVERRIDING: toString()
     @Override
     public String toString() {
         return "(" + latitude + ", " + longitude + ") " + address;
     }
 }

 // SINGLETON PATTERN: Config class
 class Config implements Serializable {
     private static Config instance;
     private boolean surgeOn;
     private double surgeMultiplier;
     private String currencyCode;

     private Config() {
         this.surgeOn = false;
         this.surgeMultiplier = 2.0;
         this.currencyCode = "PKR";
     }

     // SINGLETON: getInstance method
     public static Config getInstance() {
         if (instance == null) {
             instance = new Config();
         }
         return instance;
     }

     public boolean isSurgeOn() { return surgeOn; }
     public void setSurgeOn(boolean surgeOn) { this.surgeOn = surgeOn; }

     public double getSurgeMultiplier() { return surgeMultiplier; }
     public void setSurgeMultiplier(double surgeMultiplier) {
         if (surgeMultiplier < 1.0 || surgeMultiplier > 3.0) {
             throw new IllegalArgumentException("Surge multiplier must be between 1.0 and 3.0");
         }
         this.surgeMultiplier = surgeMultiplier;
     }

     public String getCurrencyCode() { return currencyCode; }
     public void setCurrencyCode(String currencyCode) { this.currencyCode = currencyCode; }
 }

 // Service class
 class RouteService {
     // STATIC FINAL: Constant
     private static final double SPEED = 40.0; // km/h

     public double calculateDistanceKm(Location from, Location to) {
         if (from == null || to == null) {
             throw new IllegalArgumentException("Start and end locations cannot be null");
         }
         return from.calculateDistance(to);
     }

     public long estimateTimeMinutes(Location from, Location to) {
         double distance = calculateDistanceKm(from, to);
         if (distance == 0) {
             return 0;
         }
         double time = (distance / SPEED) * 60;
         return (long) time;
     }
 }

 // INTERFACE: FareCalculator with abstract method
 interface FareCalculator {
     double calculateFare(Ride ride, RouteService routeService, Config config);
 }

 // POLYMORPHISM: NormalFareCalculator implements FareCalculator
 class NormalFareCalculator implements FareCalculator {
     private double baseFare;
     private double perKmRate;

     public NormalFareCalculator(double baseFare, double perKmRate) {
         this.baseFare = baseFare;
         this.perKmRate = perKmRate;
     }

     // POLYMORPHISM: Implementation of interface method
     @Override
     public double calculateFare(Ride ride, RouteService routeService, Config config) {
         if (ride == null) {
             throw new IllegalArgumentException("Ride cannot be null");
         }

         double distanceKm = routeService.calculateDistanceKm(ride.getPickup(), ride.getDrop());
         ride.setDistanceKm(distanceKm);

         double fare = baseFare + (perKmRate * distanceKm);
         if (fare < 0) fare = 0;

         ride.setFare(fare);
         return fare;
     }
 }

 // POLYMORPHISM: SurgeFareCalculator implements FareCalculator
 class SurgeFareCalculator implements FareCalculator {
     private double baseFare;
     private double perKmRate;

     public SurgeFareCalculator(double baseFare, double perKmRate) {
         this.baseFare = baseFare;
         this.perKmRate = perKmRate;
     }

     // POLYMORPHISM: Different implementation of same interface method
     @Override
     public double calculateFare(Ride ride, RouteService routeService, Config config) {
         if (ride == null) {
             throw new IllegalArgumentException("Ride cannot be null");
         }

         double distanceKm = routeService.calculateDistanceKm(ride.getPickup(), ride.getDrop());
         ride.setDistanceKm(distanceKm);

         double fare = baseFare + (perKmRate * distanceKm);

         if (config != null && config.isSurgeOn()) {
             fare *= config.getSurgeMultiplier();
         }

         if (fare < 0) fare = 0;

         ride.setFare(fare);
         return fare;
     }
 }

 // COMPOSITION: Payment is part of Ride
 class Payment implements Serializable {
     private String id;
     private String rideId;
     private double amount;
     private PaymentMethod method;
     private boolean successful;
     private long timestamp;

     // CONSTRUCTOR OVERLOADING
     public Payment(String id, String rideId, double amount, PaymentMethod method, boolean successful, long timestamp) {
         this.id = id;
         this.rideId = rideId;
         this.amount = amount;
         this.method = method;
         this.successful = successful;
         this.timestamp = timestamp;
     }

     // CONSTRUCTOR OVERLOADING: Different parameters
     public Payment(String id, String rideId, double amount, PaymentMethod method) {
         this(id, rideId, amount, method, false, System.currentTimeMillis());
     }

     public String getId() { return id; }
     public String getRideId() { return rideId; }
     public double getAmount() { return amount; }
     public PaymentMethod getMethod() { return method; }
     public boolean isSuccess() { return successful; }
     public void markSuccess() { this.successful = true; }
     public void markFailed() { this.successful = false; }
 }

 class PaymentService {
     public Payment processPayment(Ride ride, Rider rider, PaymentMethod method) throws PaymentFailedException {
         if (ride == null || rider == null || method == null) {
             throw new IllegalArgumentException("ride, rider and method cant be null");
         }

         double amount = ride.getFare();
         if (amount < 0) {
             throw new PaymentFailedException("Fare cannot be negative");
         }

         Payment payment = new Payment(
                 "PAY-" + ride.getRideId(),
                 ride.getRideId(),
                 amount,
                 method
         );

         switch (method) {
             case WALLET:
                 if (rider.getWalletBalance() < amount) {
                     payment.markFailed();
                     throw new PaymentFailedException("Insufficient balance");
                 }
                 boolean deducted = rider.deductFromWallet(amount);
                 if (!deducted) {
                     payment.markFailed();
                     throw new PaymentFailedException("Failed to deduct payment!");
                 }
                 payment.markSuccess();
                 break;

             case CASH:
             case CARD:
                 payment.markSuccess();
                 break;

             default:
                 payment.markFailed();
                 throw new PaymentFailedException("Unsupported Payment method");
         }
         return payment;
     }
 }

 class UserRepository {
     private ArrayList<Rider> riders = new ArrayList<>();
     private ArrayList<Driver> drivers = new ArrayList<>();
     private ArrayList<Admin> admins = new ArrayList<>();

     public ArrayList<Rider> getAllRiders() { return riders; }
     public ArrayList<Driver> getAllDrivers() { return drivers; }
     public ArrayList<Admin> getAllAdmins() { return admins; }

     public Rider findRiderByPhone(String phone) {
         for (Rider r : riders) {
             if (r.getPhone().equals(phone)) return r;
         }
         return null;
     }

     public Driver findDriverByPhone(String phone) {
         for (Driver d : drivers) {
             if (d.getPhone().equals(phone)) return d;
         }
         return null;
     }

     public Admin findAdminByPhone(String phone) {
         for (Admin a : admins) {
             if (a.getPhone().equals(phone)) return a;
         }
         return null;
     }

     public void addRider(Rider rider) { riders.add(rider); }
     public void addDriver(Driver driver) { drivers.add(driver); }
     public void addAdmin(Admin admin) { admins.add(admin); }
 }

 // ==================== USER CLASSES ====================

 // ABSTRACTION: Abstract User class
 abstract class User implements Serializable {
     private String id;
     private String name;
     private String phone;
     private String email;

     // CONSTRUCTOR
     protected User(String id, String name, String phone, String email) {
         this.id = id;
         this.name = name;
         this.phone = phone;
         this.email = email;
     }

     // ENCAPSULATION: Getters and Setters
     public String getId() { return id; }
     public void setId(String id) { this.id = id; }

     public String getName() { return name; }
     public void setName(String name) { this.name = name; }

     public String getPhone() { return phone; }
     public void setPhone(String phone) { this.phone = phone; }

     public String getEmail() { return email; }
     public void setEmail(String email) { this.email = email; }

     // METHOD OVERRIDING: toString
     @Override
     public String toString() { return id + " " + name + " " + phone + " " + email; }

     // ABSTRACTION: Abstract method to be implemented by subclasses
     public abstract String getRole();
 }

 // INHERITANCE: Rider extends User
 class Rider extends User implements Serializable {
     private double walletBalance;
     private ArrayList<String> rideIds;
     private double averageRating;
     private int ratingCount;

     // Behavioral tracking
     private int totalRides;
     private int cancelledRides;

     // CONSTRUCTOR: Calls parent constructor using super
     protected Rider(String id, String name, String phone, String email) {
         super(id, name, phone, email);  // SUPER keyword
         this.walletBalance = 0;
         this.rideIds = new ArrayList<>();
         this.averageRating = 5;
         this.ratingCount = 0;
         this.totalRides = 0;
         this.cancelledRides = 0;
     }

     // ENCAPSULATION: Getters and Setters
     public double getWalletBalance() { return walletBalance; }
     public void setWalletBalance(double walletBalance) { this.walletBalance = walletBalance; }

     public ArrayList<String> getRideIds() { return rideIds; }
     public double getAverageRating() { return averageRating; }
     public int getRatingCount() { return ratingCount; }
     public int getTotalRides() { return totalRides; }
     public int getCancelledRides() { return cancelledRides; }

     // METHOD OVERRIDING: toString
     @Override
     public String toString() {
         return this.getId() + " " + this.getName() + " " + this.getPhone() + " " +
                 this.getEmail() + " " + walletBalance + " " + averageRating;
     }

     // METHOD OVERRIDING: getRole from abstract parent
     @Override
     public String getRole() { return "RIDER"; }

     // Member methods
     public void addToWallet(double amount) {
         if (amount > 0) {
             this.walletBalance += amount;
         }
     }

     public boolean deductFromWallet(double amount) {
         if (amount <= 0) {
             return true;
         }
         if (amount > this.walletBalance) {
             return false;
         }
         walletBalance -= amount;
         return true;
     }

     public void addRideId(String id) {
         this.rideIds.add(id);
         this.totalRides++;
     }

     public void incrementCancelledRides() {
         this.cancelledRides++;
     }

     void updateAverageRating(int newRating) {
         if (newRating < 1 || newRating > 5) {
             return;
         }
         ratingCount++;
         averageRating = ((averageRating * (ratingCount - 1)) + newRating) / ratingCount;
     }

     public double getCancellationRate() {
         if (totalRides == 0) return 0.0;
         return (double) cancelledRides / totalRides;
     }
 }

 // INHERITANCE: Driver extends User
 class Driver extends User {
     // COMPOSITION: Driver HAS-A Vehicle (Vehicle dies if Driver is removed from system)
     private Vehicle vehicle;
     private boolean available;
     private double averageRating;
     private int ratingCount;
     private ArrayList<String> assignedRideIds;

     // Performance tracking
     private int totalRides;
     private int completedRides;
     private int cancelledRides;
     private double totalEarnings;

     // CONSTRUCTOR with SUPER
     protected Driver(String id, String name, String phone, String email, Vehicle vehicle, boolean available) {
         super(id, name, phone, email);  // SUPER keyword
         this.vehicle = vehicle;
         this.available = available;
         this.averageRating = 5;
         this.ratingCount = 0;
         this.assignedRideIds = new ArrayList<>();
         this.totalRides = 0;
         this.completedRides = 0;
         this.cancelledRides = 0;
         this.totalEarnings = 0.0;
     }

     // ENCAPSULATION: Getters and Setters
     public Vehicle getVehicle() { return vehicle; }
     public void setVehicle(Vehicle vehicle) { this.vehicle = vehicle; }

     public boolean isAvailable() { return available; }
     public void setAvailable(boolean available) { this.available = available; }

     public double getAverageRating() { return averageRating; }
     public int getTotalRides() { return totalRides; }
     public int getCompletedRides() { return completedRides; }
     public int getCancelledRides() { return cancelledRides; }
     public double getTotalEarnings() { return totalEarnings; }
     public ArrayList<String> getAssignedRideIds() { return assignedRideIds; }

     // METHOD OVERRIDING: toString
     @Override
     public String toString() {
         return this.getId() + " " + this.getName() + " " + this.getPhone() + " " +
                 this.getEmail() + " " + vehicle + " " + available + " " + averageRating;
     }

     // METHOD OVERRIDING: getRole from abstract parent
     @Override
     public String getRole() { return "DRIVER"; }

     // Member methods
     public void addRideId(String id) {
         this.assignedRideIds.add(id);
         this.totalRides++;
     }

     public void incrementCompletedRides() {
         this.completedRides++;
     }

     public void incrementCancelledRides() {
         this.cancelledRides++;
     }

     public void addEarnings(double amount) {
         this.totalEarnings += amount;
     }

     public void updateAverageRating(int newRating) {
         if (newRating < 1 || newRating > 5) {
             return;
         }
         ratingCount++;
         averageRating = ((averageRating * (ratingCount - 1)) + newRating) / ratingCount;
     }

     public double getCompletionRate() {
         if (totalRides == 0) return 1.0;
         return (double) completedRides / totalRides;
     }
 }

 // INHERITANCE: Admin extends User
 class Admin extends User {
     private String password;

     protected Admin(String id, String name, String phone, String email, String password) {
         super(id, name, phone, email);
         this.password = password;
     }

     @Override
     public String getRole() { return "ADMIN"; }

     public boolean verifyPassword(String password) {
         return this.password.equals(password);
     }
 }

 // AGGREGATION: Ride uses Rider and Driver (they exist independently)
 // COMPOSITION: Ride owns Payment and Location (created for this ride)
 class Ride implements Serializable {
     private String rideId;

     // AGGREGATION: Rider exists independently in UserRepository
     private Rider rider;

     // AGGREGATION: Driver exists independently in UserRepository
     private Driver driver;

     // COMPOSITION: Vehicle reference (part of Driver)
     private Vehicle vehicle;

     // COMPOSITION: Locations created specifically for this ride
     private Location pickup;
     private Location drop;

     private double fare;
     private RideStatus status;
     private long estimatedTimeMinutes;
     private double distanceKm;
     private long requestTime;
     private long startTime;
     private long endTime;
     private int riderRating;
     private int driverRating;

     // COMPOSITION: Payment belongs to this Ride
     private Payment payment;

     public Ride(String rideId, Rider rider, Location pickup, Location drop) {
         this.rideId = rideId;
         this.rider = rider;
         this.pickup = pickup;
         this.drop = drop;
         this.status = RideStatus.PENDING;
         this.requestTime = System.currentTimeMillis();
     }

     // ENCAPSULATION: Getters and Setters
     public String getRideId() { return rideId; }
     public Rider getRider() { return rider; }
     public Driver getDriver() { return driver; }
     public Vehicle getVehicle() { return vehicle; }
     public Location getPickup() { return pickup; }
     public Location getDrop() { return drop; }
     public double getFare() { return fare; }
     public void setFare(double fare) { this.fare = fare; }
     public RideStatus getStatus() { return status; }
     public long getRequestTime() { return requestTime; }
     public long getStartTime() { return startTime; }
     public long getEndTime() { return endTime; }
     public int getRiderRating() { return riderRating; }
     public void setRiderRating(int riderRating) { this.riderRating = riderRating; }
     public int getDriverRating() { return driverRating; }
     public void setDriverRating(int driverRating) { this.driverRating = driverRating; }
     public long getEstimatedTimeMinutes() { return estimatedTimeMinutes; }
     public void setEstimatedTimeMinutes(long estimatedTimeMinutes) {
         this.estimatedTimeMinutes = estimatedTimeMinutes;
     }
     public double getDistanceKm() { return distanceKm; }
     public void setDistanceKm(double distanceKm) { this.distanceKm = distanceKm; }
     public Payment getPayment() { return payment; }
     public void setPayment(Payment payment) { this.payment = payment; }

     // METHOD OVERRIDING: toString
     @Override
     public String toString() {
         String driverStr = (driver != null) ? driver.getName() : "NO_DRIVER";
         return rideId + " | Rider: " + rider.getName() + " | Driver: " + driverStr +
                 " | Status: " + status + " | Fare: " + fare;
     }

     // Member methods
     public void assignDriver(Driver driver) {
         this.driver = driver;
         this.vehicle = driver.getVehicle();
         this.status = RideStatus.ACCEPTED;
     }

     public void markStarted() {
         this.status = RideStatus.IN_PROGRESS;
         this.startTime = System.currentTimeMillis();
     }

     public void markCompleted() {
         this.status = RideStatus.COMPLETED;
         this.endTime = System.currentTimeMillis();
     }

     public void markCancelled() {
         this.status = RideStatus.CANCELLED;
         this.endTime = System.currentTimeMillis();
     }
 }

 class RatingService {
     void rateDriver(Ride ride, int rating) {
         if (ride == null || ride.getDriver() == null) {
             return;
         }
         ride.setDriverRating(rating);
         ride.getDriver().updateAverageRating(rating);
     }

     void rateRider(Ride ride, int rating) {
         if (ride == null || ride.getRider() == null) {
             return;
         }
         ride.setRiderRating(rating);
         ride.getRider().updateAverageRating(rating);
     }
 }

 class NotificationService {
     public void notifyRider(Rider rider, String message) {
         System.out.println("[RIDER NOTIFICATION to " + rider.getName() + "] " + message);
     }

     public void notifyDriver(Driver driver, String message) {
         System.out.println("[DRIVER NOTIFICATION to " + driver.getName() + "] " + message);
     }
 }

 class RideRepository {
     private ArrayList<Ride> rides = new ArrayList<>();

     public void addRide(Ride ride) {
         rides.add(ride);
     }

     public ArrayList<Ride> getRidesForRider(String riderID) {
         ArrayList<Ride> riderRides = new ArrayList<>();
         for (Ride ride : rides) {
             if (ride.getRider().getId().equals(riderID)) {
                 riderRides.add(ride);
             }
         }
         return riderRides;
     }

     public ArrayList<Ride> getRidesForDriver(String driverID) {
         ArrayList<Ride> driverRides = new ArrayList<>();
         for (Ride ride : rides) {
             if (ride.getDriver() != null && ride.getDriver().getId().equals(driverID)) {
                 driverRides.add(ride);
             }
         }
         return driverRides;
     }

     public Ride findRideById(String rideID) {
         for (Ride ride : rides) {
             if (ride.getRideId().equals(rideID)) {
                 return ride;
             }
         }
         return null;
     }

     public ArrayList<Ride> getAllRides() {
         return rides;
     }
 }

 class ObjectFileStorageService {
     private static final String RIDERS_FILE = "riders.dat";
     private static final String DRIVERS_FILE = "drivers.dat";
     private static final String RIDES_FILE = "rides.dat";
     private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

     public void saveAll(UserRepository userRepository, RideRepository rideRepository) {
         saveListToFile(RIDERS_FILE, userRepository.getAllRiders());
         saveListToFile(DRIVERS_FILE, userRepository.getAllDrivers());
         saveListToFile(RIDES_FILE, rideRepository.getAllRides());
     }

     private void saveListToFile(String fileName, ArrayList<?> list) {
         try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(fileName))) {
             oos.writeObject(list);
             System.out.println("✅ Saved " + list.size() + " object(s) to " + fileName);
         } catch (IOException e) {
             System.out.println("❌ Error saving to " + fileName + ": " + e.getMessage());
         }
     }

     @SuppressWarnings("unchecked")
     public void loadAll(UserRepository userRepo, RideRepository rideRepo) {
         ArrayList<Rider> riders = (ArrayList<Rider>) loadFromFile(RIDERS_FILE, new ArrayList<Rider>());
         ArrayList<Driver> drivers = (ArrayList<Driver>) loadFromFile(DRIVERS_FILE, new ArrayList<Driver>());
         ArrayList<Ride> rides = (ArrayList<Ride>) loadFromFile(RIDES_FILE, new ArrayList<Ride>());

         for (Rider r : riders) userRepo.addRider(r);
         for (Driver d : drivers) userRepo.addDriver(d);
         for (Ride r : rides) rideRepo.addRide(r);
     }

     private Object loadFromFile(String fileName, Object defaultValue) {
         try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(fileName))) {
             return ois.readObject();
         } catch (FileNotFoundException e) {
             System.out.println("No " + fileName + " found, starting fresh.");
             return defaultValue;
         } catch (Exception e) {
             System.out.println("❌ Error loading " + fileName + ": " + e.getMessage());
             return defaultValue;
         }
     }
 }

 // ==================== MAIN RIDE SERVICE ====================

 class RideService {
     // Nested class for driver options
     public static class DriverFareOption {
         private final Driver driver;
         private final double fare;

         public DriverFareOption(Driver driver, double fare) {
             this.driver = driver;
             this.fare = fare;
         }

         public Driver getDriver() { return driver; }
         public double getFare() { return fare; }
     }

     private UserRepository userRepository;
     private RideRepository rideRepository;
     private RouteService routeService;
     private FareCalculator fareCalculator;
     private PaymentService paymentService;
     private RatingService ratingService;
     private NotificationService notificationService;
     private Config config;

     public RideService(UserRepository userRepository, RideRepository rideRepository,
                        RouteService routeService, FareCalculator fareCalculator,
                        PaymentService paymentService, RatingService ratingService,
                        NotificationService notificationService, Config config) {
         this.userRepository = userRepository;
         this.rideRepository = rideRepository;
         this.routeService = routeService;
         this.fareCalculator = fareCalculator;
         this.paymentService = paymentService;
         this.ratingService = ratingService;
         this.notificationService = notificationService;
         this.config = config;
     }

     public Rider loginRider(String phone) {
         return userRepository.findRiderByPhone(phone);
     }

     public Driver loginDriver(String phone) {
         return userRepository.findDriverByPhone(phone);
     }

     public Admin loginAdmin(String phone, String password) {
         Admin admin = userRepository.findAdminByPhone(phone);
         if (admin != null && admin.verifyPassword(password)) {
             return admin;
         }
         return null;
     }

     public ArrayList<DriverFareOption> getDriverFareOptions(Location pickup, Location drop)
             throws NoDriverAvailableException {

         ArrayList<DriverFareOption> options = new ArrayList<>();
         ArrayList<Driver> allDrivers = userRepository.getAllDrivers();

         double distanceKm = routeService.calculateDistanceKm(pickup, drop);
         long etaMinutes = routeService.estimateTimeMinutes(pickup, drop);

         // COMPOSITION
         Ride tempRide = new Ride("TEMP", null, pickup, drop);
         tempRide.setDistanceKm(distanceKm);
         tempRide.setEstimatedTimeMinutes(etaMinutes);

         for (Driver d : allDrivers) {
             if (!d.isAvailable()) continue;

             double baseFare = fareCalculator.calculateFare(tempRide, routeService, config);
             double factor = 0.9 + Math.random() * 0.2;
             double offeredFare = Math.round(baseFare * factor);

             options.add(new DriverFareOption(d, offeredFare));
         }

         if (options.isEmpty()) {
             throw new NoDriverAvailableException("No drivers available for this route.");
         }

         return options;
     }

     public Ride confirmRideWithDriver(Rider rider, Driver selectedDriver, Location pickup,
                                       Location drop, double agreedFare, PaymentMethod paymentMethod)
             throws NoDriverAvailableException, PaymentFailedException {

         Driver driverInRepo = null;
         for (Driver d : userRepository.getAllDrivers()) {
             if (d.getId().equals(selectedDriver.getId())) {
                 driverInRepo = d;
                 break;
             }
         }

         if (driverInRepo == null || !driverInRepo.isAvailable()) {
             throw new NoDriverAvailableException("Selected driver is no longer available.");
         }

         String rideId = "R" + (rideRepository.getAllRides().size() + 1);
         Ride ride = new Ride(rideId, rider, pickup, drop);
         ride.assignDriver(driverInRepo);

         double distanceKm = routeService.calculateDistanceKm(pickup, drop);
         long etaMinutes = routeService.estimateTimeMinutes(pickup, drop);
         ride.setDistanceKm(distanceKm);
         ride.setEstimatedTimeMinutes(etaMinutes);
         ride.setFare(agreedFare);

         Payment payment = paymentService.processPayment(ride, rider, paymentMethod);
         ride.setPayment(payment);

         rider.addRideId(rideId);
         driverInRepo.addRideId(rideId);
         driverInRepo.setAvailable(false);

         rideRepository.addRide(ride);

         notificationService.notifyDriver(driverInRepo, "New Ride Assigned " + rideId);
         notificationService.notifyRider(rider, "Ride booked with driver " + driverInRepo.getName()
                 + ", fare: " + agreedFare);

         return ride;
     }

     public boolean cancelRide(Rider rider, String rideId) {
         Ride ride = rideRepository.findRideById(rideId);
         if (ride == null) {
             return false;
         }
         if (ride.getStatus() == RideStatus.CANCELLED || ride.getStatus() == RideStatus.COMPLETED) {
             return false;
         }
         if (ride.getStatus() != RideStatus.ACCEPTED) {
             return false;
         }

         ride.markCancelled();
         rider.incrementCancelledRides();

         if (ride.getDriver() != null) {
             notificationService.notifyDriver(ride.getDriver(),
                     "Ride: " + ride.getRideId() + " was cancelled by rider " + rider.getName());
             ride.getDriver().setAvailable(true);
             ride.getDriver().incrementCancelledRides();
         }

         return true;
     }

     public boolean startRide(Driver driver, String rideId) {
         Ride ride = rideRepository.findRideById(rideId);

         if (ride == null) {
             System.out.println("❌ Ride not found.");
             return false;
         }

         if (ride.getDriver() == null || !ride.getDriver().getId().equals(driver.getId())) {
             System.out.println("❌ This ride does NOT belong to you.");
             return false;
         }

         if (ride.getStatus() != RideStatus.ACCEPTED) {
             System.out.println("❌ Ride is not in ACCEPTED state, cannot start.");
             return false;
         }

         ride.markStarted();
         notificationService.notifyRider(ride.getRider(),
                 "Your ride " + ride.getRideId() + " has started.");

         System.out.println("✅ Ride " + rideId + " started successfully.");
         return true;
     }

     public boolean completeRide(Driver driver, String rideId) {
         Ride ride = rideRepository.findRideById(rideId);
         if (ride == null) {
             return false;
         }
         if (ride.getStatus() != RideStatus.IN_PROGRESS) {
             System.out.println("❌ Can't complete a ride not yet started by Driver");
             return false;
         }

         ride.markCompleted();
         driver.setAvailable(true);
         driver.incrementCompletedRides();
         driver.addEarnings(ride.getFare());

         return true;
     }

     public ArrayList<Ride> getRidesForRider(Rider rider) {
         return rideRepository.getRidesForRider(rider.getId());
     }

     public ArrayList<Ride> getRidesForDriver(Driver driver) {
         return rideRepository.getRidesForDriver(driver.getId());
     }

     public RatingService getRatingService() {
         return ratingService;
     }

     public void showRideAssignedToDriver(Driver driver) {
         ArrayList<Ride> rides = rideRepository.getRidesForDriver(driver.getId());

         boolean found = false;
         for (Ride ride : rides) {
             if (ride.getStatus() == RideStatus.ACCEPTED) {
                 found = true;
                 System.out.println("➡️ Ride available: " + ride.getRideId());
             }
         }

         if (!found) {
             System.out.println("❌ No available rides found");
         }
     }

     public void showRidesStarted(Driver driver) {
         ArrayList<Ride> rides = rideRepository.getRidesForDriver(driver.getId());

         boolean found = false;
         for (Ride ride : rides) {
             if (ride.getStatus() == RideStatus.IN_PROGRESS) {
                 found = true;
                 System.out.println("➡️ Ride in Progress: " + ride.getRideId());
             }
         }

         if (!found) {
             System.out.println("❌ No Rides Started yet");
         }
     }
 }

 // ==================== MAIN PROGRAM ====================

 public class ProjectCode_IntegratedComplete {

     private static void showMainMenu() {
         System.out.println("\n" + "=".repeat(50));
         System.out.println("🚗 RIDE HAILING SYSTEM");
         System.out.println("=".repeat(50));
         System.out.println("1) Login as Rider");
         System.out.println("2) Login as Driver");
         System.out.println("3) Login as Admin");
         System.out.println("4) Exit");
         System.out.println("=".repeat(50));
     }

     private static void showRiderMenu(String riderName) {
         System.out.println("\n" + "=".repeat(50));
         System.out.println("🚗 RIDER MENU - Welcome " + riderName + "!");
         System.out.println("=".repeat(50));
         System.out.println("1) Request Ride");
         System.out.println("2) Cancel Ride");
         System.out.println("3) View My Rides");
         System.out.println("4) Logout");
         System.out.println("=".repeat(50));
     }

     private static void showDriverMenu(String driverName) {
         System.out.println("\n" + "=".repeat(50));
         System.out.println("🚕 DRIVER MENU - Welcome " + driverName + "!");
         System.out.println("=".repeat(50));
         System.out.println("1) View Assigned Rides");
         System.out.println("2) Start Ride");
         System.out.println("3) Complete Ride");
         System.out.println("4) View My Rides");
         System.out.println("5) Logout");
         System.out.println("=".repeat(50));
     }

     private static void showAdminMenu() {
         System.out.println("\n" + "=".repeat(50));
         System.out.println("👨‍💼 ADMIN MENU - System Management");
         System.out.println("=".repeat(50));
         System.out.println("1) View All Riders");
         System.out.println("2) View All Drivers");
         System.out.println("3) View All Rides");
         System.out.println("4) View System Statistics");
         System.out.println("5) Toggle Surge Pricing");
         System.out.println("6) Logout");
         System.out.println("=".repeat(50));
     }

     public static void main(String[] args) {
         Scanner sc = new Scanner(System.in);

         // Initialize repositories and services
         UserRepository userRepo = new UserRepository();
         RideRepository rideRepo = new RideRepository();
         RouteService routeService = new RouteService();
         PaymentService paymentService = new PaymentService();
         RatingService ratingService = new RatingService();
         NotificationService notificationService = new NotificationService();
         Config config = Config.getInstance();
         ObjectFileStorageService storageService = new ObjectFileStorageService();

         // POLYMORPHISM: FareCalculator interface with different implementations
         FareCalculator fareCalculator;
         // Up-Casting
         if (config.isSurgeOn()) {
             fareCalculator = new SurgeFareCalculator(50, 25);
         } else {
             fareCalculator = new NormalFareCalculator(50, 25);
         }

         RideService rideService = new RideService(
                 userRepo, rideRepo, routeService, fareCalculator,
                 paymentService, ratingService, notificationService, config
         );

         // Seed demo data
         Rider rider1 = new Rider("RID1", "Noah", "03001234567", "noah@mail.com");
         rider1.addToWallet(100000);
         userRepo.addRider(rider1);

         Rider rider2 = new Rider("RID2", "Emma", "03009876543", "emma@mail.com");
         rider2.addToWallet(50000);
         userRepo.addRider(rider2);

         Vehicle v1 = new Vehicle("V1", "ABC-123", "Honda Civic", VehicleType.CAR, 4);
         Driver driver1 = new Driver("DRV1", "Ali", "03211223344", "ali@mail.com", v1, true);
         userRepo.addDriver(driver1);

         Vehicle v2 = new Vehicle("V2", "XYZ-456", "Toyota Corolla", VehicleType.CAR, 4);
         Driver driver2 = new Driver("DRV2", "Sara", "03331234567", "sara@mail.com", v2, true);
         userRepo.addDriver(driver2);

         Vehicle v3 = new Vehicle("V3", "LMN-789", "Suzuki Cultus", VehicleType.CAR, 4);
         Driver driver3 = new Driver("DRV3", "Ahmed", "03445566778", "ahmed@mail.com", v3, true);
         userRepo.addDriver(driver3);

         Admin admin = new Admin("ADM1", "System Admin", "03000000000", "admin@system.com", "admin123");
         userRepo.addAdmin(admin);

         // Load existing data
         storageService.loadAll(userRepo, rideRepo);

         Rider loggedRider = null;
         Driver loggedDriver = null;
         Admin loggedAdmin = null;
         String currentRole = null;

         while (true) {
             try {
                 if (currentRole == null) {
                     // Main menu
                     showMainMenu();
                     System.out.print("Choose option: ");
                     int choice = sc.nextInt();
                     sc.nextLine();

                     switch (choice) {
                         case 1:
                             System.out.print("Enter rider phone: ");
                             String phone = sc.nextLine();
                             loggedRider = rideService.loginRider(phone);
                             if (loggedRider != null) {
                                 currentRole = "RIDER";
                                 System.out.println("✅ Rider logged in: " + loggedRider.getName());
                             } else {
                                 System.out.println("❌ Rider not found.");
                             }
                             break;

                         case 2:
                             System.out.print("Enter driver phone: ");
                             phone = sc.nextLine();
                             loggedDriver = rideService.loginDriver(phone);
                             if (loggedDriver != null) {
                                 currentRole = "DRIVER";
                                 System.out.println("✅ Driver logged in: " + loggedDriver.getName());
                             } else {
                                 System.out.println("❌ Driver not found.");
                             }
                             break;

                         case 3:
                             System.out.print("Enter admin phone: ");
                             phone = sc.nextLine();
                             System.out.print("Enter admin password: ");
                             String password = sc.nextLine();
                             loggedAdmin = rideService.loginAdmin(phone, password);
                             if (loggedAdmin != null) {
                                 currentRole = "ADMIN";
                                 System.out.println("✅ Admin logged in: " + loggedAdmin.getName());
                             } else {
                                 System.out.println("❌ Invalid credentials.");
                             }
                             break;

                         case 4:
                             System.out.println("\n👋 Thank you for using Ride Hailing System!");
                             storageService.saveAll(userRepo, rideRepo);
                             sc.close();
                             return;

                         default:
                             System.out.println("❌ Invalid choice. Try again.");
                     }

                 } else if (currentRole.equals("RIDER")) {
                     // Rider menu
                     showRiderMenu(loggedRider.getName());
                     System.out.print("Choose option: ");
                     int choice = sc.nextInt();
                     sc.nextLine();

                     switch (choice) {
                         case 1:
                             // Request Ride
                             System.out.println("\n📍 Enter Pickup Location:");
                             System.out.print("  Latitude: ");
                             double pLat = sc.nextDouble();
                             System.out.print("  Longitude: ");
                             double pLon = sc.nextDouble();
                             sc.nextLine();
                             System.out.print("  Address: ");
                             String pAddr = sc.nextLine();

                             System.out.println("\n📍 Enter Drop Location:");
                             System.out.print("  Latitude: ");
                             double dLat = sc.nextDouble();
                             System.out.print("  Longitude: ");
                             double dLon = sc.nextDouble();
                             sc.nextLine();
                             System.out.print("  Address: ");
                             String dAddr = sc.nextLine();

                             try {
                                 Location pickup = new Location(pLat, pLon, pAddr);
                                 Location drop = new Location(dLat, dLon, dAddr);

                                 ArrayList<RideService.DriverFareOption> options =
                                         rideService.getDriverFareOptions(pickup, drop);

                                 System.out.println("\n📋 Available drivers & offers:");
                                 System.out.println("-".repeat(70));
                                 for (int i = 0; i < options.size(); i++) {
                                     RideService.DriverFareOption opt = options.get(i);
                                     Driver d = opt.getDriver();
                                     System.out.printf("%d) %s%n", (i + 1), d.getName());
                                     System.out.printf("   Rating: %.1f⭐ | Vehicle: %s%n",
                                             d.getAverageRating(), d.getVehicle().getModel());
                                     System.out.printf("   Fare: Rs.%.0f%n", opt.getFare());
                                     System.out.println("-".repeat(70));
                                 }

                                 System.out.println("0) Cancel");
                                 System.out.print("\nChoose driver: ");
                                 int choiceDriver = sc.nextInt();
                                 sc.nextLine();

                                 if (choiceDriver == 0) {
                                     System.out.println("❌ Ride request cancelled.");
                                     break;
                                 }

                                 if (choiceDriver < 1 || choiceDriver > options.size()) {
                                     System.out.println("❌ Invalid choice.");
                                     break;
                                 }

                                 RideService.DriverFareOption selectedOption = options.get(choiceDriver - 1);

                                 Ride rideBooked = rideService.confirmRideWithDriver(
                                         loggedRider,
                                         selectedOption.getDriver(),
                                         pickup,
                                         drop,
                                         selectedOption.getFare(),
                                         PaymentMethod.WALLET
                                 );

                                 System.out.println("\n✅ Ride booked! ID: " + rideBooked.getRideId());
                                 System.out.printf("   Driver: %s%n", rideBooked.getDriver().getName());
                                 System.out.printf("   Fare: Rs.%.0f%n", rideBooked.getFare());
                                 System.out.printf("   Distance: %.2f km%n", rideBooked.getDistanceKm());
                                 System.out.printf("   ETA: %d min%n", rideBooked.getEstimatedTimeMinutes());

                                 storageService.saveAll(userRepo, rideRepo);

                             } catch (NoDriverAvailableException e) {
                                 System.out.println("❌ " + e.getMessage());
                             } catch (PaymentFailedException e) {
                                 System.out.println("❌ Payment failed: " + e.getMessage());
                             } catch (IllegalArgumentException e) {
                                 System.out.println("❌ Invalid location: " + e.getMessage());
                             }
                             break;

                         case 2:
                             // Cancel Ride
                             ArrayList<Ride> rides = rideService.getRidesForRider(loggedRider);
                             ArrayList<Ride> activeRides = new ArrayList<>();
                             for (Ride r : rides) {
                                 if (r.getStatus() == RideStatus.ACCEPTED) {
                                     activeRides.add(r);
                                 }
                             }

                             if (activeRides.isEmpty()) {
                                 System.out.println("❌ No active rides to cancel.");
                                 break;
                             }

                             System.out.println("\n📋 Your active rides:");
                             for (Ride r : activeRides) {
                                 System.out.printf("  %s - Driver: %s - Status: %s%n",
                                         r.getRideId(), r.getDriver().getName(), r.getStatus());
                             }

                             System.out.print("\nEnter Ride ID to cancel: ");
                             String rideId = sc.nextLine();
                             if (rideService.cancelRide(loggedRider, rideId)) {
                                 System.out.println("✅ Ride cancelled successfully.");
                                 storageService.saveAll(userRepo, rideRepo);
                             } else {
                                 System.out.println("❌ Failed to cancel ride.");
                             }
                             break;

                         case 3:
                             // View My Rides
                             rides = rideService.getRidesForRider(loggedRider);
                             System.out.println("\n📋 YOUR RIDES:");
                             if (rides.isEmpty()) {
                                 System.out.println("No rides found.");
                             } else {
                                 for (Ride r : rides) {
                                     System.out.println("\n" + r);
                                     System.out.printf("  Distance: %.2f km | Status: %s%n",
                                             r.getDistanceKm(), r.getStatus());

                                     if (r.getStatus() == RideStatus.COMPLETED && r.getDriverRating() == 0) {
                                         System.out.print("Rate driver for " + r.getRideId() + " (1-5): ");
                                         int rating = sc.nextInt();
                                         sc.nextLine();
                                         ratingService.rateDriver(r, rating);
                                         System.out.println("⭐ Driver rated!");
                                     }
                                 }
                                 storageService.saveAll(userRepo, rideRepo);
                             }
                             break;

                         case 4:
                             // Logout
                             System.out.println("👋 Goodbye, " + loggedRider.getName() + "!");
                             loggedRider = null;
                             currentRole = null;
                             break;

                         default:
                             System.out.println("❌ Invalid choice. Try again.");
                     }

                 } else if (currentRole.equals("DRIVER")) {
                     // Driver menu
                     showDriverMenu(loggedDriver.getName());
                     System.out.print("Choose option: ");
                     int choice = sc.nextInt();
                     sc.nextLine();

                     switch (choice) {
                         case 1:
                             // View Assigned Rides
                             rideService.showRideAssignedToDriver(loggedDriver);
                             break;

                         case 2:
                             // Start Ride
                             rideService.showRideAssignedToDriver(loggedDriver);
                             System.out.print("\nEnter Ride ID to start: ");
                             String rideId = sc.nextLine();
                             if (rideService.startRide(loggedDriver, rideId)) {
                                 storageService.saveAll(userRepo, rideRepo);
                             }
                             break;

                         case 3:
                             // Complete Ride
                             rideService.showRidesStarted(loggedDriver);
                             System.out.print("\nEnter Ride ID to complete: ");
                             rideId = sc.nextLine();
                             if (rideService.completeRide(loggedDriver, rideId)) {
                                 System.out.println("✅ Ride completed successfully.");

                                 Ride ride = rideRepo.findRideById(rideId);
                                 if (ride != null) {
                                     System.out.print("Rate the rider (1-5): ");
                                     int rating = sc.nextInt();
                                     sc.nextLine();
                                     ratingService.rateRider(ride, rating);
                                     System.out.println("⭐ Rider rated successfully.");
                                 }

                                 storageService.saveAll(userRepo, rideRepo);
                             }
                             break;

                         case 4:
                             // View My Rides
                             ArrayList<Ride> rides = rideService.getRidesForDriver(loggedDriver);
                             System.out.println("\n📋 YOUR RIDES:");
                             if (rides.isEmpty()) {
                                 System.out.println("No rides found.");
                             } else {
                                 for (Ride r : rides) {
                                     System.out.println("\n" + r);
                                     System.out.printf("  Distance: %.2f km | Status: %s%n",
                                             r.getDistanceKm(), r.getStatus());
                                 }
                             }
                             break;

                         case 5:
                             // Logout
                             System.out.println("👋 Goodbye, " + loggedDriver.getName() + "!");
                             loggedDriver = null;
                             currentRole = null;
                             break;

                         default:
                             System.out.println("❌ Invalid choice. Try again.");
                     }

                 } else if (currentRole.equals("ADMIN")) {
                     // Admin menu
                     showAdminMenu();
                     System.out.print("Choose option: ");
                     int choice = sc.nextInt();
                     sc.nextLine();

                     switch (choice) {
                         case 1:
                             // View All Riders
                             ArrayList<Rider> riders = userRepo.getAllRiders();
                             System.out.println("\n👥 ALL RIDERS:");
                             System.out.println("-".repeat(80));
                             for (Rider r : riders) {
                                 System.out.printf("ID: %s | Name: %s | Phone: %s%n",
                                         r.getId(), r.getName(), r.getPhone());
                                 System.out.printf("Wallet: Rs.%.0f | Rating: %.2f⭐%n",
                                         r.getWalletBalance(), r.getAverageRating());
                                 System.out.printf("Total Rides: %d | Cancelled: %d%n",
                                         r.getTotalRides(), r.getCancelledRides());
                                 System.out.println("-".repeat(80));
                             }
                             break;

                         case 2:
                             // View All Drivers
                             ArrayList<Driver> drivers = userRepo.getAllDrivers();
                             System.out.println("\n🚗 ALL DRIVERS:");
                             System.out.println("-".repeat(80));
                             for (Driver d : drivers) {
                                 System.out.printf("ID: %s | Name: %s | Phone: %s%n",
                                         d.getId(), d.getName(), d.getPhone());
                                 System.out.printf("Vehicle: %s (%s)%n",
                                         d.getVehicle().getModel(), d.getVehicle().getRegNumber());
                                 System.out.printf("Available: %s | Rating: %.2f⭐%n",
                                         d.isAvailable() ? "Yes" : "No", d.getAverageRating());
                                 System.out.printf("Total Rides: %d | Completed: %d%n",
                                         d.getTotalRides(), d.getCompletedRides());
                                 System.out.println("-".repeat(80));
                             }
                             break;

                         case 3:
                             // View All Rides
                             ArrayList<Ride> rides = rideRepo.getAllRides();
                             System.out.println("\n🚕 ALL RIDES:");
                             System.out.println("-".repeat(80));
                             for (Ride r : rides) {
                                 System.out.printf("Ride ID: %s | Status: %s%n",
                                         r.getRideId(), r.getStatus());
                                 System.out.printf("Rider: %s | Driver: %s%n",
                                         r.getRider().getName(),
                                         r.getDriver() != null ? r.getDriver().getName() : "N/A");
                                 System.out.printf("Fare: Rs.%.0f | Distance: %.2f km%n",
                                         r.getFare(), r.getDistanceKm());
                                 System.out.printf("Pickup: %s%n", r.getPickup().getAddress());
                                 System.out.printf("Drop: %s%n", r.getDrop().getAddress());
                                 System.out.println("-".repeat(80));
                             }
                             break;

                         case 4:
                             // View System Statistics
                             int totalRiders = userRepo.getAllRiders().size();
                             int totalDrivers = userRepo.getAllDrivers().size();
                             int totalRides = rideRepo.getAllRides().size();

                             int completed = 0, cancelled = 0, active = 0;
                             double totalRevenue = 0.0;

                             for (Ride r : rideRepo.getAllRides()) {
                                 if (r.getStatus() == RideStatus.COMPLETED) {
                                     completed++;
                                     totalRevenue += r.getFare();
                                 } else if (r.getStatus() == RideStatus.CANCELLED) {
                                     cancelled++;
                                 } else if (r.getStatus() == RideStatus.ACCEPTED ||
                                         r.getStatus() == RideStatus.IN_PROGRESS) {
                                     active++;
                                 }
                             }

                             System.out.println("\n📊 SYSTEM STATISTICS:");
                             System.out.println("=".repeat(50));
                             System.out.println("Total Riders: " + totalRiders);
                             System.out.println("Total Drivers: " + totalDrivers);
                             System.out.println("Total Rides: " + totalRides);
                             System.out.println("  ✅ Completed: " + completed);
                             System.out.println("  ❌ Cancelled: " + cancelled);
                             System.out.println("  🚗 Active: " + active);
                             System.out.printf("Total Revenue: Rs.%.0f%n", totalRevenue);
                             System.out.println("Surge Status: " + (config.isSurgeOn() ? "ON" : "OFF"));
                             System.out.println("=".repeat(50));
                             break;

                         case 5:
                             // Toggle Surge Pricing
                             config.setSurgeOn(!config.isSurgeOn());
                             double multiplier = config.isSurgeOn() ? config.getSurgeMultiplier() : 1.0;
                             System.out.printf("🔄 Surge pricing %s (x%.1f)%n",
                                     config.isSurgeOn() ? "ENABLED" : "DISABLED", multiplier);
                             break;

                         case 6:
                             // Logout
                             System.out.println("👋 Goodbye, " + loggedAdmin.getName() + "!");
                             loggedAdmin = null;
                             currentRole = null;
                             break;

                         default:
                             System.out.println("❌ Invalid choice. Try again.");
                     }
                 }

             } catch (Exception e) {
                 System.out.println("❌ An error occurred: " + e.getMessage());
                 sc.nextLine(); // clear buffer
             }
         }
     }
 }