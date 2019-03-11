package turnConTest.com.turn;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;


public class EmployeeDAO {
	private static final HashMap<String,Employee> employee = new HashMap<>();
	public static  HashMap<String, Employee> addEmployee( String userName) {
        String employeeName =userName;
        int size = employee.size();
        String employeeID = Integer.toString(size + 1);
        LocalDateTime checkIn = LocalDateTime.now();
        employee.put(employeeID, new Employee(employeeID, employeeName, checkIn));
        return employee;
   }
	public static  HashMap<String, Employee> addEmployee( String id, Employee e) {
        employee.put(id, e);
        return employee;
   }
	public static  Employee getEmployee( String id) {
        return employee.get(id);
   }
	public static  HashMap<String, Employee> getEmployee( ) {
        return employee;
   }
}
