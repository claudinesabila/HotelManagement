package hotel;

import java.io.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.Random;
import java.util.Calendar;
import java.util.concurrent.TimeUnit;

import java.text.*;


public class HotelImpl implements Hotel {

    public static ArrayList<Room> roomList;
    public static ArrayList<Booking> bookingList;
    public static ArrayList<Guest> guestList;
    public static ArrayList<VIPGuest> vipGuestList;
    public static ArrayList<Payment> paymentList;
    public static final SimpleDateFormat ft = new SimpleDateFormat("yyyy-MM-dd");


    public HotelImpl(String roomsTxtFileName, String guestsTxtFileName,
                           String bookingsTxtFileName, String paymentsTxtFileName){
        importAllData(roomsTxtFileName, guestsTxtFileName, bookingsTxtFileName, paymentsTxtFileName);
    }


    public boolean removeRoom(long roomNumber) {
        for (Booking book : bookingList ) {
            Date d1 = new Date();
            if (book.getRoomNumber() == roomNumber && d1.after(book.getCheckOutDate())) {
                bookingList.remove(book);
                return true;
            }
        }
        return false;
    }


    public boolean addGuest(String fName, String lName, boolean vipState) {

        assert fName != null && lName != null
                && (Boolean)vipState instanceof Boolean: "Please enter the right input for this method";

        boolean unique = true;
        long guestID;
        try{
            if(vipState==true){
                while(true){
                    guestID = new Random().nextLong() & 0xffffffffL;
                    for(VIPGuest guest: vipGuestList){
                        if(guest.getGuestID() == guestID){unique = false;}
                    }
                    if(unique == true){break;}
                }

                Calendar end_Date = Calendar.getInstance();
                end_Date.setTime(new Date());
                end_Date.add(Calendar.YEAR,1);
                Date date_expire = new Date();
                date_expire.setTime(end_Date.getTimeInMillis());
                VIPGuest vipGuest = new VIPGuest(guestID, fName, lName, new Date(), new Date(), date_expire);
                Payment payment = new Payment(new Date(), vipGuest.getGuestID(), 50.00, "VIPmembership");

                paymentList.add(payment);
                vipGuestList.add(vipGuest);
            }else{
                while(true){
                    guestID = new Random().nextLong() & 0xffffffffL;
                    for(Guest guest: guestList){
                        if(guest.getGuestID() == guestID){unique = false;}
                    }
                    if(unique == true){break;}
                }
                Guest guest = new Guest(guestID, fName, lName, new Date());
                guestList.add(guest);

            }
        }catch(Exception e){
            System.out.print("An Error Has Occured adding a Guest... ");
            System.out.print(e + "\n");
            return false;
        }
        return true;
       }

    public boolean removeGuest(long guestID) {

        assert (Long)guestID instanceof Long : "Please enter the guestID correctly";

        try{
            for(Booking book: bookingList){
                if(book.getGuestID() == guestID && new Date().after(book.getCheckOutDate())){ 
                    for(Guest guest : guestList){
                        if(guest.getGuestID() == guestID){
                            guestList.remove(guest);
                        }
                    }
                    for(Guest guest : vipGuestList) {
                        if (guest.getGuestID() == guestID) {
                            vipGuestList.remove(guest);
                        }
                    }
                    return true;
                }
            }return false;

        }catch(Exception e){
            System.out.print("An Error Has Occured... ");
            System.out.print(e + "\n");
            return false;
        }

    }

    public boolean addRoom(long roomNumber, String roomType, double roomPrice, int capacity, String facilities) {

        assert (Long)roomNumber instanceof Long && roomType != null && (Double)roomPrice instanceof Double && (Integer)capacity instanceof Integer
                && facilities != null : "Please provide the correct information for the addRoom method";

        for(Room room : roomList) {
            if(room.getRoomNumber() == roomNumber){return false;}
        }
        Room room = new Room(roomNumber, roomType, roomPrice, capacity, facilities);
        roomList.add(room);
        return true;
    }

    public boolean checkRoomAvailable(long roomNumber, Date checkInDate, Date checkOutDate){
        for (Booking book : bookingList){
            if(book.getRoomNumber() == roomNumber && checkInDate.before(book.getCheckOutDate()) && checkInDate.after(book.getCheckInDate())){
                return false;
            }
        }
        return true;
    }

    public ArrayList<Long> findAvailableRooms(String roomType, Date checkInDate, Date checkOutDate){
        ArrayList<Long> availableRooms = new ArrayList<Long>();
        for(Room room: roomList){
            if(room.getRoomType() == roomType && checkRoomAvailable(room.getRoomNumber(), checkInDate, checkOutDate)){ //Checks the room type as well as the availability between the check in dates and check out dates
                availableRooms.add(room.getRoomNumber());
            }
        }
        if (availableRooms.size() == 0) {
            System.out.println("There are no available rooms!");
            return null;
        } else {
            return availableRooms; 
        }
    }

    public boolean makeBooking(String roomType, long guestID, Date checkInDate, Date checkOutDate) {

        assert roomType != null && (Long)guestID instanceof Long && (Date)checkInDate instanceof Date && (Date)checkOutDate instanceof Date : "Please enter the right input for this method";

        boolean guestExists = false;
        for(Guest guest: guestList){
            if(guest.getGuestID() == guestID){guestExists = true;}} 
        if(guestExists == false){return false;}


        if(new Date().after(checkInDate)){return false;}

        long roomNumber;
        ArrayList<Long> availableRooms = findAvailableRooms(roomType, checkInDate, checkOutDate);
        try{
            roomNumber = availableRooms.get(new Random().nextInt(availableRooms.size()));
        }catch(Exception e){
            System.out.println("Error occured while making a booking");
            return false;
        }

        boolean unique = true;
        long bookingID;
        Room bookedRoom = null;
        while(true){
            bookingID = new Random().nextLong() & 0xffffffffL;
            for(Booking book: bookingList){
                if(book.getBookingID() == bookingID){unique = false;}
            }
            if(unique == true){break;}
        }

        for(Room room: roomList){
            if(room.getRoomNumber() == roomNumber){bookedRoom = room;}
        }

        long diff = checkOutDate.getTime() - checkInDate.getTime();
        long daysDiff = TimeUnit.DAYS.convert(diff, TimeUnit.MILLISECONDS);
        double totalAmount = daysDiff*bookedRoom.getRoomPrice();

        for(VIPGuest guest: vipGuestList){
            if(guest.getGuestID() == guestID && checkOutDate.before(guest.getVIPExpiryDate())){
                totalAmount = totalAmount * 0.9;
            }
        }
        Booking booking = new Booking(bookingID, guestID, roomNumber, new Date(), checkInDate, checkOutDate, totalAmount); // We initialize it using a constructor from the class method at the bottom
        Payment payment = new Payment(new Date(), guestID, totalAmount, "booking");

        paymentList.add(payment);
        bookingList.add(booking);
        return true;

    }

    public boolean importAllData(String roomsTxtFileName, String guestsTxtFileName, String bookingsTxtFileName, String paymentsTxtFileName){
        try{
            importRoomsData(roomsTxtFileName);
            importGuestsData(guestsTxtFileName);
            importBookingsData(bookingsTxtFileName);
            importPaymentsData(paymentsTxtFileName);
            return true;
        }catch(Exception e){
            System.out.println("ERROR: an issue occured importing data");
            System.out.println(e);
            return false;
        }
    }

    public boolean importRoomsData(String roomsTxtFileName) {

        assert roomsTxtFileName != null : "Please enter the right input for this method";

        try
        {
            File file = new File(roomsTxtFileName);
            BufferedReader br = new BufferedReader(new FileReader(file));
            String st;
            roomList = new ArrayList<Room>();

            while ((st = br.readLine()) != null)
            {
                String[] room_info = st.split(",");
                Room room = new Room(Long.valueOf(room_info[0]), room_info[1], Double.valueOf(room_info[2]), Integer.valueOf(room_info[3]), room_info[4]); // We initialize the object using its own-defined constructor
                roomList.add(room);
            }
            br.close();    
            return true;
        }
        catch(Exception e)
        {
            System.out.println("Error Occured when reading rooms data...");
            return false;
        }
    }

    public boolean importGuestsData(String guestsTxtFileName){
        try{
            File file = new File(guestsTxtFileName);
            BufferedReader br = new BufferedReader(new FileReader(file));
            String st;
            guestList = new ArrayList<Guest>();
            vipGuestList = new ArrayList<VIPGuest>();
            while ((st = br.readLine()) != null) {
                String[] guest_info = st.split(",");
                if(guest_info.length > 4){
                    VIPGuest vipGuest = new VIPGuest(Long.valueOf(guest_info[0]), guest_info[1], guest_info[2], ft.parse(guest_info[3]), ft.parse(guest_info[4]), ft.parse(guest_info[5])); // We use its own-defined constructor
                    vipGuestList.add(vipGuest);
                }else{
                    Guest guest = new Guest(Long.valueOf(guest_info[0]), guest_info[1], guest_info[2], ft.parse(guest_info[3]));
                    guestList.add(guest);
                }

            }
            br.close();
            return true;
        }catch(Exception e){
            System.out.println("Error Occured when reading Guests data...");
            return false;
        }
    }


    public boolean importBookingsData(String bookingsTxtFileName){
        try{
            File file = new File(bookingsTxtFileName);
            BufferedReader br = new BufferedReader(new FileReader(file));
            String st;
            bookingList = new ArrayList<Booking>();
            while((st = br.readLine()) != null){
                String[] booking_info = st.split(",");
                Booking booking = new Booking(Long.valueOf(booking_info[0]), Long.valueOf(booking_info[1]),
                    Long.valueOf(booking_info[2]),ft.parse(booking_info[3]),
                    ft.parse(booking_info[4]), ft.parse(booking_info[5]),
                    Double.valueOf(booking_info[6]));
                bookingList.add(booking);
            }
            br.close();
            return true;
        }
        catch(Exception e){
            System.out.println("Error Occured when reading booking data...");
            return false;
        }
    }

    public boolean importPaymentsData(String paymentsTxtFileName){
        try{
            File file = new File(paymentsTxtFileName);
            BufferedReader br = new BufferedReader(new FileReader(file));
            String st;
            SimpleDateFormat ft = new SimpleDateFormat ("yyyy-MM-dd");
            paymentList = new ArrayList<Payment>();
            while ((st = br.readLine()) != null){
                String[] payment_info = st.split(",");
                Payment payment = new Payment(ft.parse(payment_info[0]), Long.valueOf(payment_info[1]),
                    Double.valueOf(payment_info[2]), payment_info[3]);
                paymentList.add(payment);
            }
            br.close();
            return true;
        }catch(Exception e){
            System.out.println("Error Occured when reading payment data...");
            return false;
        }
    }

    public boolean checkOut(long bookingID){
        Booking booking = null;
        try{
            for(Booking book: bookingList){
                if(book.getBookingID()==bookingID){booking=book;}
            }

            if(new Date().after(booking.getCheckOutDate())|| new Date().before(booking.getCheckInDate())){return false;}

            bookingList.remove(booking);
            return true;
        }catch(Exception e){
            e.printStackTrace();
            return false;
        }
    }

    public ArrayList<Long> searchGuest(String firstName, String lastName) {
        ArrayList<Long> result = new ArrayList<Long>();
        for(Guest guest : guestList) {
            if (guest.getfName().toLowerCase().equals(firstName.toLowerCase()) && guest.getlName().toLowerCase().equals(lastName.toLowerCase())) {
                result.add(guest.getGuestID());
            }
        }
        for(VIPGuest guest : vipGuestList) {
            if (guest.getfName().toLowerCase().equals(firstName.toLowerCase()) && guest.getlName().toLowerCase().equals(lastName.toLowerCase())) {
                result.add(guest.getGuestID());
            }
        }
        return result;
    }

    public Guest searchGuestByID(long guestID) {
        for(Guest guest : guestList) {
            if(guest.getGuestID() == guestID) {
                return guest;
            }
        }
        for(VIPGuest guest : vipGuestList) {
            if(guest.getGuestID() == guestID) {
                return guest;
            }
        }

        return null;
    }


    public boolean saveRoomsData(String roomsTxtFileName) {
        File fnew = new File(roomsTxtFileName);
        try {
            FileWriter roomsFile = new FileWriter(fnew, false);
            PrintWriter roomsWriter = new PrintWriter(roomsFile);
            for(Room room: roomList){roomsWriter.println(room.getRoomNumber()+","+room.getRoomType()+","+room.getRoomPrice()+","+room.getCapacity()+","+room.getFacilities());}
            roomsFile.close();
            return true;
        }catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean saveGuestsData(String guestsTxtFileName) {
        File fnew = new File(guestsTxtFileName);
        try {
            FileWriter guestsFile = new FileWriter(fnew, false);
            PrintWriter guestsWriter = new PrintWriter(guestsFile);
            for(Guest guest: guestList){guestsWriter.println(guest.getGuestID() +","+guest.getfName()+","+guest.getlName()+","+guest.getDateJoin());} // This is how the file is modified to the new source data
            for(VIPGuest guest: vipGuestList){guestsWriter.println(guest.getGuestID() +","+guest.getfName()+","+guest.getlName()+","+guest.getDateJoin()+","+guest.getVIPStartDate()+","+guest.getVIPExpiryDate());}
            guestsFile.close();
            return true;
        }
        catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean saveBookingsData(String bookingsTxtFileName) {
        File fnew = new File(bookingsTxtFileName);
        try {
            FileWriter bookingsFile = new FileWriter(fnew, false);
            PrintWriter bookingsWriter = new PrintWriter(bookingsFile);
            for(Booking booking: bookingList){bookingsWriter.println(booking.getBookingID()+","+booking.getGuestID()+","+booking.getRoomNumber()+","+booking.getBookingDate()+","+booking.getCheckInDate()+","+booking.getCheckOutDate()+","+booking.getTotalAmount());} // This opens and then closes in the next line the file that we created
            bookingsFile.close();
            return true;
        }
        catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean savePaymentsData(String paymentsTxtFileName) {
        File fnew = new File(paymentsTxtFileName);
        try {
            FileWriter paymentsFile = new FileWriter(fnew, false); 
            PrintWriter paymentsWriter = new PrintWriter(paymentsFile);
            for(Payment payment: paymentList){paymentsWriter.println(payment.getDate()+","+payment.getGuestID()+","+payment.getAmount()+","+payment.getPayReason());}
            paymentsFile.close();
            return true;
        }
        catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean saveAllData(String roomsTxtFileName, String guestsTxtFileName,
                           String bookingsTxtFileName, String paymentsTxtFileName){
        try{
            savePaymentsData(paymentsTxtFileName);
            saveRoomsData(roomsTxtFileName);
            saveBookingsData(bookingsTxtFileName);
            saveGuestsData(guestsTxtFileName);
            return true;
        }catch(Exception e){
            e.printStackTrace();
            return false;
        }
    }

    public boolean cancelBooking(long bookingID){
        try{
            Booking booking=null;
            for(Booking book : bookingList){
                if(book.getBookingID() == bookingID){booking = book;}
            }
            
            long diff = new Date().getTime() - booking.getCheckInDate().getTime();
            long daysDiff = TimeUnit.DAYS.convert(diff, TimeUnit.MILLISECONDS);
            if(daysDiff>2){
                Payment refund = new Payment(new Date(), booking.getGuestID(), booking.getTotalAmount()*(-1), "refund");
                paymentList.add(refund);
            }
            bookingList.remove(booking);
            return true;
        }catch(Exception e){ 
            System.out.print("An error occured while canceling a booking....");
            System.out.print(e + "\n");
            return false;
        }
    }

    public void displayBookingsOnDate(Date date){
        Room booked_room=null;
        for(Booking book: bookingList){
            if(book.getCheckOutDate().before(date) && book.getCheckInDate().after(date)){ 
                Guest guest = searchGuestByID(book.getGuestID());
                for(Room room: roomList){
                    if(room.getRoomNumber()==book.getRoomNumber()){booked_room = room;}
                }
                System.out.print("bookingID: " + book.getBookingID() + " Name: " + guest.getlName() +" " + guest.getfName() + " Room Number" + book.getRoomNumber() + " Room Type: " + booked_room.getRoomType() + " Room Price: " + booked_room.getRoomPrice() + " Payment Price: " + book.getTotalAmount() +"\n");

            }
        }
    }

    public void displayPaymentsOnDate(Date date){
        for(Payment payment: paymentList){
            if(payment.getDate() == date){
                System.out.print("Guest ID: "+ payment.getGuestID() + " Payment Ammount: " +payment.getAmount() + " Payment Reason:" + payment.getPayReason());

            }
        }
    }

    public void displayGuestBooking(long guestID){
        try{
            Room booked_room=null;
            Guest guest = searchGuestByID(guestID);
            System.out.println("Displaying all bookings by "+guest.getfName() + " " +guest.getlName());
            for(Booking book: bookingList){
                if(book.getGuestID() == guest.getGuestID()){
                    for(Room room: roomList){if(room.getRoomNumber()==book.getRoomNumber()){booked_room = room;}}
                    System.out.println("bookingID: " + book.getBookingID() + " Name: " + guest.getlName() +" " + guest.getfName() + " Room Number: " + book.getRoomNumber() + " Room Type: " + booked_room.getRoomType() + " Room Price: " + booked_room.getRoomPrice() + " Payment Price: " + book.getTotalAmount());
                }
            }
        }catch(Exception e){
            e.printStackTrace();
        }
    } 

    public void displayAllGuests(){
        System.out.println("Displaying Guests: ");
        for(Guest guest: guestList){
            System.out.println("Guest ID: " +guest.getGuestID() +" Guest Name: " +guest.getlName() +" " +guest.getfName() + " Date Joined: " + guest.getDateJoin());
        }
        System.out.println("Displaying VIP Guests: ");
        for(VIPGuest guest: vipGuestList){
            System.out.println("Guest ID: "+ guest.getGuestID() + " Guest Name: "+ guest.getlName() + " " +guest.getfName() + " Date Joined: " + guest.getDateJoin() + " VIP Start Date: " + guest.getVIPStartDate() + " VIP End Date: " + guest.getVIPExpiryDate());
        }
    }

    public void displayAllRooms(){
        System.out.println("Displaing Rooms: ");
        for(Room room: roomList){
            // The following line prints all the info about a room, after having traversed the whole array of objects that represent each and every individual room
            System.out.println("Room Number: " +room.getRoomNumber() + " Room Type: " + room.getRoomType() + " Room Price: " + room.getRoomPrice() + " Room Capacity: " + room.getCapacity() + " Facilities: " +room.getFacilities());
        }
    }

    public void displayAllBookings(){
        System.out.println("Displaying Bookings: ");
        Room booked_room=null;
        for(Booking book: bookingList){
            Guest guest = searchGuestByID(book.getGuestID());
            for(Room room: roomList){
                if(room.getRoomNumber()==book.getRoomNumber()){booked_room = room;}}
            System.out.println("bookingID: " + book.getBookingID() + " Name: " + guest.getlName() +" " + guest.getfName() + " Room Number" + book.getRoomNumber() + " Room Type: " + booked_room.getRoomType() + " Room Price: " + booked_room.getRoomPrice() + " Payment Price: " + book.getTotalAmount()); 
        }
    }

    public void displayAllPayments(){
        System.out.println("Displaying Payments: ");
        for(Payment payment: paymentList){
            System.out.println("Guest ID: "+ payment.getGuestID() + " Payment Ammount: " +payment.getAmount() + " Payment Reason:" + payment.getPayReason());

        }
    }

    static class Room {
        private long roomNumber;
        private String roomType;
        private double roomPrice;
        private int capacity;
        private String facilities;

        public Room(long roomNumber, String roomType, double roomPrice, int capacity, String facilities) {
            this.roomNumber = roomNumber;
            this.roomType = roomType;
            this.roomPrice = roomPrice;
            this.capacity = capacity;
            this.facilities = facilities;
        }

        public long getRoomNumber() {return this.roomNumber;}
        public String getRoomType(){return this.roomType;}
        public double getRoomPrice(){return this.roomPrice;}
        public int getCapacity(){return this.capacity;}
        public String getFacilities(){return this.facilities;}
    }

    static class Booking{
        private long bookingID;
        private long guestID;
        private long roomNumber;
        private Date bookingDate;
        private Date checkInDate;
        private Date checkOutDate;
        private double totalAmount;


        public Booking(long bookingID, long guestID, long roomNumber, Date bookingDate, Date checkInDate, Date checkOutDate, double totalAmount) {
            this.bookingID = bookingID;
            this.guestID = guestID;
            this.roomNumber = roomNumber;
            this.bookingDate = bookingDate;
            this.checkInDate = checkInDate;
            this.checkOutDate = checkOutDate;
            this.totalAmount = totalAmount;
        }


        public long getBookingID(){return this.bookingID;}
        public long getGuestID(){return this.guestID;}
        public long getRoomNumber(){return this.roomNumber;}
        public Date getBookingDate(){return this.bookingDate;}
        public Date getCheckInDate(){return this.checkInDate;}
        public Date getCheckOutDate(){return this.checkOutDate;}
        public double getTotalAmount(){return this.totalAmount;}
    }

    class Guest {
        private long guestID;
        private String fName;
        private String lName;
        private Date dateJoin;

        public Guest(long guestID, String fName, String lName, Date dateJoin) {
            this.guestID = guestID;
            this.fName = fName;
            this.lName = lName;
            this.dateJoin = dateJoin;
        }

        public long getGuestID() {return this.guestID;}
        public String getfName() {return this.fName;}
        public String getlName() {return this.lName;}
        public Date getDateJoin() {return this.dateJoin;}
    }

    class VIPGuest extends Guest {
        private Date VIPStartDate;
        private Date VIPExpiryDate;

        public VIPGuest(long guestID, String fName, String lName, Date dateJoin, Date VIPStartDate,
                Date VIPExpiryDate) {

            super(guestID, fName, lName, dateJoin);
            this.VIPStartDate = VIPStartDate;
            this.VIPExpiryDate = VIPExpiryDate;
        }

        public void setVIP(Date VIPStartDate, Date VIPExpiryDate) {
            this.VIPStartDate = VIPStartDate;
            this.VIPExpiryDate = VIPExpiryDate;
        }

        public Date getVIPStartDate() { return this.VIPStartDate; }
        public Date getVIPExpiryDate() { return this.VIPExpiryDate; }

    }


    static class Payment {
        private Date date;
        private long guestID;
        private double amount;
        private String payReason;

        public Payment(Date date, long guestID, double amount, String payReason) {
            this.date = date;
            this.guestID = guestID;
            this.amount = amount;
            this.payReason = payReason;
        }

        public Date getDate() {return this.date;}
        public long getGuestID() {return this.guestID;}
        public double getAmount() {return this.amount;}
        public String getPayReason() {return this.payReason;}
    }
}