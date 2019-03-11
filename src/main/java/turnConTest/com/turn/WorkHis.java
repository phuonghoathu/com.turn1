package turnConTest.com.turn;

public class WorkHis {
	private String name;
	private double money;
	private boolean turn;// 1 free 0 count
	private String id;

	public String getName() {
		return name;
	}

	public WorkHis(String name, double money, boolean turn, String id) {
		super();
		this.name = name;
		this.money = money;
		this.turn = turn;
		this.id = id;
	}

	public void setName(String name) {
		this.name = name;
	}

	public double getMoney() {
		return money;
	}

	public void setMoney(double money) {
		this.money = money;
	}

	public boolean isTurn() {
		return turn;
	}

	public void setTurn(boolean turn) {
		this.turn = turn;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

}
