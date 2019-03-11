package turnConTest.com.turn;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.Month;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

/**
 * Root resource (exposed at "myresource" path)
 * http://localhost:8080/com.turn/api/chicago/2016-08-27
 */
@Path("/")
public class MyResource {
	LocalDateTime aDateTime = LocalDateTime.of(2018, Month.OCTOBER, 31, 19, 30, 40);

	LocalDateTime now = LocalDateTime.now();
	HashMap<String, Employee> employee = new HashMap<>();
	ArrayList<ArrayList<Employee>> arrOfArrEmployee = new ArrayList<>();
	public static final int STEP_TURN = 15;

	/**
	 * Method handling HTTP GET requests. The returned object will be sent to the
	 * client as "text/plain" media type.
	 *
	 * @return String that will be returned as a text/plain response.
	 */

	@GET
	@Produces({ MediaType.APPLICATION_JSON })
	public String getEmployee() {
		employee = EmployeeDAO.getEmployee();
		return buildJson(updatePosition(new ArrayList<Employee>(employee.values())));
	}

	// http://localhost:8080/com.turn/api/addUser/abff
	@GET
	@Path("/addUser/{userName}")
	@Produces({ MediaType.APPLICATION_JSON })
	public String addEmployee(@PathParam("userName") String userName) {
		employee = EmployeeDAO.addEmployee(userName);
		return buildJson(updatePosition(new ArrayList<Employee>(employee.values())));
	}

	// http://localhost:8080/com.turn/api/addGroup/{name}/{money}/{free}
	@GET
	@Path("/addGroup/{id}/{name}/{money}/{free}")
	@Produces({ MediaType.APPLICATION_JSON })
	public String addGroup(@PathParam("id") String id, @PathParam("name") String name, @PathParam("money") double money,
			@PathParam("free") String free) {
		Employee employee1 = EmployeeDAO.getEmployee(id);
		// check null
		employee1.getTurnListD().add(new WorkHis(name, money, "1".equals(free) ? true : false,
				Integer.toString(employee1.getTurnListD().size() + 1)));
		if ("0".equals(free)) {
			employee1.setTotalTurn(employee1.getTotalTurn() + money);
		}
		employee1.setTotal(employee1.getTotal() + money);
		employee = EmployeeDAO.addEmployee(id, employee1);
		return buildJson(updatePosition(new ArrayList<Employee>(employee.values())));
	}

	@GET
	@Path("/upGroup/{id}/{groupid}/{name}/{money}/{free}")
	@Produces({ MediaType.APPLICATION_JSON })
	public String updateGroup(@PathParam("id") String id, @PathParam("groupid") String groudid,
			@PathParam("name") String name, @PathParam("money") double money, @PathParam("free") String free) {
		Employee employee1 = EmployeeDAO.getEmployee(id);
		// check null
		int index = -1;
		for (int i = 0; i < employee1.getTurnListD().size(); i++) {
			if (employee1.getTurnListD().get(i).getId().equals(groudid)) {
				index = i;
				break;
			}
		}
		WorkHis wk = employee1.getTurnListD().get(index);
		if ("0".equals(free) && !wk.isTurn()) {
			employee1.setTotalTurn(employee1.getTotalTurn() + money - wk.getMoney());
		} else if ("0".equals(free) && wk.isTurn()) {
			employee1.setTotalTurn(employee1.getTotalTurn() + money);
		} else if ("1".equals(free) && !wk.isTurn()) {
			employee1.setTotalTurn(employee1.getTotalTurn() - wk.getMoney());
		}
		employee1.setTotal(employee1.getTotal() + money - wk.getMoney());
		wk.setId(groudid);
		wk.setMoney(money);
		wk.setName(name);
		wk.setMoney(money);
		wk.setTurn(free == "1" ? true : false);
		employee1.getTurnListD().remove(index);
		employee1.getTurnListD().add(wk);
		employee = EmployeeDAO.addEmployee(id, employee1);
		return buildJson(updatePosition(new ArrayList<Employee>(employee.values())));
	}

	public static ArrayList<ArrayList<Employee>> updatePosition(ArrayList<Employee> employee) {
// total 10, active 6 , inactive 4
// Get active, inactive number
		ArrayList<ArrayList<Employee>> arrOfArrEmployee = new ArrayList<>();
		int numberOfEmployee = employee.size();
		int numberActive = 0;
		int numberInActive = 0;
		int numberBusyWorker = 0;
		int numberFreeWorker = 0;
		BubbleSort b = new BubbleSort();
		for (int i = 0; i < numberOfEmployee; i++) {
			if (employee.get(i).isActive()) {
				numberActive++;
				if (employee.get(i).isIsWorking()) {
					numberBusyWorker++;
				}
			} else {
				numberInActive++;
			}
		}
		numberFreeWorker = numberActive - numberBusyWorker;

// Process Free Worker 
		if (numberFreeWorker > 0) {
			ArrayList<Employee> tmpFreeWorker = new ArrayList<>(numberFreeWorker);
			for (int i = 0; i < employee.size(); i++) {
				if (employee.get(i).isActive() && !employee.get(i).isIsWorking()) {
					tmpFreeWorker.add(employee.get(i));
				}
			}
			// System.out.println("\nBEFORE SORT:");
			// printAddr(tmpFreeWorker);
			b.bubbleSortTotalTurn(tmpFreeWorker);

//index group by Step_Turn
			int tmpIndexGroup = 1;
			if (tmpFreeWorker.size() > 0) {
				tmpFreeWorker.get(0).setIndexGroup(tmpIndexGroup);
				// System.out.println("Employee: " + tmpFreeWorker.get(0).getEmpName() + "
				// total_Turn: " + tmpFreeWorker.get(0).getTotalTurn());
				if (tmpFreeWorker.size() > 1) {
					for (int i = 1; i < tmpFreeWorker.size(); i++) {
						if ((tmpFreeWorker.get(i).getTotalTurn()
								- tmpFreeWorker.get(i - 1).getTotalTurn()) >= STEP_TURN) {
							tmpIndexGroup++;
							tmpFreeWorker.get(i).setIndexGroup(tmpIndexGroup);
						} else {
							tmpFreeWorker.get(i).setIndexGroup(tmpIndexGroup);
						}
						// System.out.println("Employee: " + tmpFreeWorker.get(i).getEmpName() + "
						// total_Turn: " + tmpFreeWorker.get(i).getTotalTurn());
					}
				}
			}
			// System.out.println("\nAFTER SORT:");
			// printAddr(tmpFreeWorker);

			/*
			 * =============================================================================
			 * ========
			 */
			// arrOfArrEmployee = new ArrayList<ArrayList<Employee>>(tmpIndexGroup);
			arrOfArrEmployee.clear();
			if (tmpFreeWorker.size() > 0) {
				ArrayList<Employee> tmp = new ArrayList<Employee>();
				tmp.add(tmpFreeWorker.get(0));
				for (int i = 1; i < tmpFreeWorker.size(); i++) {
					if (tmpFreeWorker.get(i).getIndexGroup() != tmpFreeWorker.get(i - 1).getIndexGroup()) {
						arrOfArrEmployee.add(tmp);
						System.out.println(
								"Added group: " + arrOfArrEmployee.size() + " && with " + tmp.size() + " elements");
						tmp = new ArrayList<Employee>();
						tmp.add(tmpFreeWorker.get(i));
					} else {
						tmp.add(tmpFreeWorker.get(i));
					}
				}
				arrOfArrEmployee.add(tmp);
				System.out.println(
						"Added last group: " + arrOfArrEmployee.size() + " && with " + tmp.size() + " elements");
			}

			int position = 1;
			for (int i = 0; i < arrOfArrEmployee.size(); i++) {
				b.bubbleSortTime(arrOfArrEmployee.get(i));
				for (int j = 0; j < arrOfArrEmployee.get(i).size(); j++) {
					arrOfArrEmployee.get(i).get(j).setPosition(position);
					position++;
				}
			}

// Create tmp array of busy worker and sort by total , index position
// Process Busy worker array
			if (numberBusyWorker > 0) {
				ArrayList<Employee> tmpBusyWorker = new ArrayList<>(numberBusyWorker);
				for (int i = 0; i < employee.size(); i++) {
					if (employee.get(i).isActive() && employee.get(i).isIsWorking()) {
						tmpBusyWorker.add(employee.get(i));
					}
				}
				b.bubbleSortTotal(tmpBusyWorker);
				// set Position
				for (int i = 0; i < tmpBusyWorker.size(); i++) {
					tmpBusyWorker.get(i).setPosition(numberActive - numberBusyWorker + i + 1);
				}
				arrOfArrEmployee.add(tmpBusyWorker);
			}

//Create tmp array of inactive and sort inactive & index  position 
//Process Inactive worker array
			if (numberInActive > 0) {
				ArrayList<Employee> tmpInActive = new ArrayList<>(numberInActive);
				for (int i = 0; i < employee.size(); i++) {
					if (employee.get(i).isActive() == false) {
						tmpInActive.add(employee.get(i));
					}
				}
				b.bubbleSortTime(tmpInActive);
				for (int i = 0; i < tmpInActive.size(); i++) {
					tmpInActive.get(i).setPosition(i + 1 + numberActive);
				}
				arrOfArrEmployee.add(tmpInActive);
			}
		}
		return arrOfArrEmployee;
		// print(arrOfArrEmployee);
	}

	private String buildJson() {
		List<Employee> list = new ArrayList<Employee>(employee.values());
		DateTimeFormatter dtf = DateTimeFormatter.ofPattern("HH:mm:ss");
		String s = "[";
		for (Employee emp : list) {
			s += "{";
			s += "\"id\" : \"" + emp.getEmployeeID() + "\",";
			s += "\"name\" : \"" + emp.getEmpName() + "\",";
			s += "\"sortOrder\" : \"" + emp.getPosition() + "\",";
			s += "\"turn\" : \"" + emp.getTotalTurn() + "\",";
			s += "\"turnAll\" : \"" + emp.getTotal() + "\",";
			s += "\"status\" : \"" + ((emp.isActive()) ? "1" : "0") + "\",";
			s += "\"working\" : \"" + ((emp.isIsWorking()) ? "1" : "0") + "\",";
			s += "\"loginTime\" : \"" + dtf.format(emp.getCheckInTime()) + "\",";
			s += "\"workHis\" : [";
			for (WorkHis work : emp.getTurnListD()) {
				s += "{";
				s += "\"groudId\" : \"" + work.getId() + "\",";
				s += "\"name\" : \"" + work.getName() + "\",";
				s += "\"free\" : \"" + ((work.isTurn()) ? "1" : "0") + "\",";
				s += "\"money\" : \"" + work.getMoney() + "\"";
				s += "}";
			}
			s += "]";
			s += "}";
		}
		s += "]";
		return s;
	}

	private String buildJson(ArrayList<ArrayList<Employee>> employee) {
		DateTimeFormatter dtf = DateTimeFormatter.ofPattern("HH:mm:ss");
		System.out.println("\nEmployee Table Details:");
		String s = "[";
		// tb.addRow("EmployeeID", "EmployeeName", "CheckInTime", "Total", "Total_Turn",
		// "Is_Working", "Status", "Position", "Turn_List", "Index_Group");
		int k = 0;
		int l = 0;
		for (int j = 0; j < employee.size(); j++) {
			for (int i = 0; i < employee.get(j).size(); i++) {
				int index = i;
				if (l > 0)
					s += ",";
				s += "{";
				l++;
				s += "\"id\" : \"" + employee.get(j).get(index).getEmployeeID() + "\",";
				s += "\"name\" : \"" + employee.get(j).get(index).getEmpName() + "\",";
				s += "\"sortOrder\" : \"" + employee.get(j).get(index).getPosition() + "\",";
				s += "\"turn\" : \"" + employee.get(j).get(index).getTotalTurn() + "\",";
				s += "\"turnAll\" : \"" + employee.get(j).get(index).getTotal() + "\",";
				s += "\"status\" : \"" + ((employee.get(j).get(index).isActive()) ? "1" : "0") + "\",";
				s += "\"working\" : \"" + ((employee.get(j).get(index).isIsWorking()) ? "1" : "0") + "\",";
				s += "\"loginTime\" : \"" + dtf.format(employee.get(j).get(index).getCheckInTime()) + "\",";
				s += "\"workHis\" : [";
				k = 0;
				for (WorkHis work : employee.get(j).get(index).getTurnListD()) {
					if (k == 0) {
						s += "{";
						k++;
					} else
						s += ",{";
					s += "\"id\" : \"" + work.getId() + "\",";
					s += "\"name\" : \"" + work.getName() + "\",";
					s += "\"free\" : \"" + ((work.isTurn()) ? "1" : "0") + "\",";
					s += "\"money\" : \"" + work.getMoney() + "\"";
					s += "}";
				}
				s += "]";
				s += "}";
			}
		}
		s += "]";

		return s;
	}
}
