import hotel.HotelImpl;
import java.util.*;
import java.text.*;

public class test_hotel{
	public static final String roomsTxtFileName = "C:/Users/DELL/Documents/Programming/HotelBookingManagementSystem/data/rooms.txt";
	public static final String guestsTxtFileName = "C:/Users/DELL/Documents/Programming/HotelBookingManagementSystem/data/guests.txt";
	public static final String bookingsTxtFileName = "C:/Users/DELL/Documents/Programming/HotelBookingManagementSystem/data/bookings.txt";
	public static final String paymentsTxtFileName = "C:/Users/DELL/Documents/Programming/HotelBookingManagementSystem/data/payments.txt";
	public static final SimpleDateFormat ft = new SimpleDateFormat("yyyy-MM-dd");


	public static void main(String[] args) {

		HotelImpl hotel = new HotelImpl(roomsTxtFileName, guestsTxtFileName, bookingsTxtFileName, paymentsTxtFileName);
		try{
		Date checkin = ft.parse("2019-03-22");
		Date checkout = ft.parse("2019-03-23");
		hotel.addGuest("John", "Mayer", false);
		hotel.addGuest("Christian", "Ronaldo", true);
		ArrayList<Long> list = hotel.searchGuest("Gilang", "Sasmito"); 
		hotel.addRoom(1002, "Single", 90.00, 5, "own bathroom");
		hotel.makeBooking("Single", list.get(0),checkin, checkout);
		System.out.println("");
		hotel.displayAllRooms();
		System.out.println("");
		hotel.displayAllBookings();
		System.out.println("");
		hotel.displayAllPayments();
		System.out.println("");
		hotel.displayAllGuests();
		}catch(Exception e){e.printStackTrace();}
	}
} 